package com.comfydns.resolver.resolver.rfc1035.service.search.state;

import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolver.rfc1035.message.InvalidMessageException;
import com.comfydns.resolver.resolver.rfc1035.message.UnsupportedRRTypeException;
import com.comfydns.resolver.resolver.rfc1035.message.field.header.RCode;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRClass;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.NSRData;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.SOARData;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Question;
import com.comfydns.resolver.resolver.rfc1035.message.struct.RR;
import com.comfydns.resolver.resolver.rfc1035.service.search.*;
import io.prometheus.client.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class HandleResponseToZoneQuery implements RequestState {
    private static final Logger log = LoggerFactory.getLogger(HandleResponseToZoneQuery.class);
    private static final Counter slistServerFailures = Counter.build()
            .name("slist_server_failures")
            .help("Times servers in the slist have behaved poorly while trying to ask them for records.")
            .register();

    private final SList.SListServer serverQueried;
    private final Message sent;
    private final byte[] response;
    private final Throwable error;

    public HandleResponseToZoneQuery(SList.SListServer serverQueried, Message sent, byte[] response) {
        this.serverQueried = serverQueried;
        this.sent = sent;
        this.response = response;
        this.error = null;
    }

    public HandleResponseToZoneQuery(SList.SListServer serverQueried, Message sent, Throwable error) {
        this.serverQueried = serverQueried;
        this.sent = sent;
        this.error = error;
        this.response = null;
    }

    @Override
    public Optional<RequestState> run(ResolverContext rCtx, SearchContext sCtx) throws CacheAccessException, NameResolutionException, StateTransitionCountLimitExceededException, OptionalFeatureNotImplementedException {
        if(error != null) {
            serverQueried.incrementFailureCount();
            slistServerFailures.inc();
            sCtx.getQSet().remove(serverQueried.getIp(), sent.getQuestions().get(0));
            sCtx.forEachListener(l -> l.onUpstreamQueryResult(serverQueried, Optional.empty(), Optional.of(error)));
            log.debug("[{}]: Zone query resulted in error: {} {}", sCtx.getRequest().getId(), error.getClass().getSimpleName(), error.getMessage());
            return Optional.of(new SendServerQuery(false));
        }

        if(response == null) {
            throw new RuntimeException("Error was null but response was null???");
        }

        Message m;
        try {
            m = Message.read(response);
            log.debug("[{}]: Message received: {}", sCtx.getRequest().getId(), m);
        } catch (InvalidMessageException e) {
            log.debug("Error while reading zone query response", e);
            serverQueried.incrementFailureCount();
            sCtx.getQSet().remove(serverQueried.getIp(), sent.getQuestions().get(0));
            sCtx.forEachListener(l -> l.onUpstreamQueryResult(serverQueried, Optional.empty(), Optional.of(e)));
            return Optional.of(new SendServerQuery(false));
        } catch (UnsupportedRRTypeException e) {
            throw new OptionalFeatureNotImplementedException("Encountered an unsupported RRType while reading zone response",
                    e);
        }

        sCtx.forEachListener(l -> l.onUpstreamQueryResult(serverQueried, Optional.of(m), Optional.empty()));

        if(m.getHeader().getId() != sent.getHeader().getId()) {
            log.warn("[{}]: Received message w/ nonmatching ID: expected {} but found {}", sCtx.getRequest().getId(), sent.getHeader().getId(), m.getHeader().getId());
            throw new NameResolutionException("Received message with nonmatching ID: expected " +
                    sent.getHeader().getId() + ", but found " + m.getHeader().getId());
        }

        if(m.getHeader().getTC()) {
            log.debug("Response had TC=1, retrying via tcp.");
            sCtx.removeFromQSet(serverQueried.getIp(), m.getQuestions().get(0));
            return Optional.of(new SendServerQuery(true, sent.getHeader().getId()));
        }


        RCode rCode = m.getHeader().getRCode();
        switch(rCode) {
            case NO_ERROR:
                break;
            case SERVER_FAILURE:
                serverQueried.incrementFailureCount();
                log.debug("[{}]: We got a SERVER_FAILURE back from {}: {}", sCtx.getRequest().getId(), serverQueried.getHostname(), m.toString());
                return Optional.of(new SendServerQuery(false));
            case REFUSED:
            case NOT_IMPLEMENTED:
            case FORMAT_ERROR:
                sCtx.getSList().removeServer(serverQueried);
                return Optional.of(new SendServerQuery(false));
            case NAME_ERROR:
                if(m.getHeader().getANCount() > 0 && m.getAnswerRecords().stream().anyMatch(r -> r.getRrType() == KnownRRType.CNAME)) {
                    /*
                    This is specifically to fix console.aws.amazon.com. For some reason this is how their DNS servers behave. Go figure.
                     */
                    log.debug("We received a NAME_ERROR but they DID give us a CNAME answer back. Ignoring the name error.");
                    break;
                } else if(m.getHeader().getAA()) {
                    log.debug("[{}] Received authoritative name error.", sCtx.getRequest().getId());
                    handleNegativeCache(rCtx, sCtx, m);
                    log.debug("{}Received authoritative name error.", sCtx.getRequestLogPrefix());
                    return Optional.of(new DoubleCheckSendState(sCtx.buildNameErrorResponse()));
                } else {
                    log.debug("[{}] Received non-authoritative name error.", sCtx.getRequest().getId());
                    sCtx.getSList().removeServer(serverQueried);
                    return Optional.of(new SendServerQuery(false));
                }
            default:
                throw new RuntimeException("Unhandled RCODE:" + rCode);

        }

        if(m.getHeader().getANCount() == 0 && m.getHeader().getNSCount() == 0) {
            log.debug("Server gave us an empty response, what the heck.");
            sCtx.getSList().removeServer(serverQueried);
            return Optional.of(new SendServerQuery(false));
        }

        if(handleNegativeCache(rCtx, sCtx, m)) {
            log.debug("{}Received negative cache instruction (SOA record) + NAME_ERROR", sCtx.getRequestLogPrefix());
            return Optional.of(new DoubleCheckSendState(sCtx.buildNameErrorResponse()));
        }


        log.debug("[{}]: Processing RRs in response.", sCtx.getRequest().getId());
        List<RR<?>> rrs = new ArrayList<>();
        final List<RR<?>> aRecords = new ArrayList<>(), nsRecords = new ArrayList<>();

        m.forEach(rr -> {
            if(sCtx.getCurrentQuestion().getqClass() == KnownRRClass.IN) {
                if (rr.getRrType() == KnownRRType.A) {
                    aRecords.add(rr);
                } else if(rr.getRrType() == KnownRRType.NS) {
                    nsRecords.add(rr);
                } else {
                    rrs.add(rr);
                }
            } else {
                rrs.add(rr);
            }
        });

        List<RR<?>> clampedNSRecords = clampNSDNameTTLsIfTheyreInTheirOwnZone(aRecords, nsRecords);

        rrs.addAll(clampedNSRecords);
        rrs.addAll(aRecords);

        Set<RR<NSRData>> badRecords = filterNSDNamesInTheirOwnZoneWithoutARecords(aRecords, clampedNSRecords);
        if(m.getHeader().getANCount() > 0) {
            rrs.removeAll(badRecords);
        } else {
            if(!badRecords.isEmpty()) {
                sCtx.forEachListener(l -> l.remark("Server " + serverQueried + " had NS records that needed glue records but had no glue records. Filtering results and treating request as failed."));
                serverQueried.incrementFailureCount();
                sCtx.getQSet().remove(serverQueried.getIp(), m.getQuestions().get(0));
                return Optional.of(new SendServerQuery(false));
            }
        }

        for (RR<?> rr : rrs) {
            if(rr.getTtl() == 0) {
                sCtx.getRequestCache().cache(rr, OffsetDateTime.now());
            } else {
                rCtx.getGlobalCache().cache(rr, OffsetDateTime.now());
            }
        }

        return Optional.of(new TryToAnswerWithLocalInformation());
    }

    private boolean handleNegativeCache(ResolverContext rCtx, SearchContext sCtx, Message m) throws CacheAccessException {
        log.debug("Checking for SOA -> nameerror");
        Optional<RR<?>> soaFound = m.getAuthorityRecords()
                .stream()
                .filter(rr -> rr.getRrType() == KnownRRType.SOA)
                .filter(soa -> sCtx.getSName().endsWith(soa.getName())).findFirst();

        if(soaFound.isPresent()) {
            Question q = sCtx.getCurrentQuestion();
            rCtx.getNegativeCache().cacheNegative(
                    sCtx.getSName(),
                    q.getqType(), q.getqClass(),
                    ((SOARData) soaFound.get().getRData()).getMinimum(),
                    OffsetDateTime.now()
            );
            return true;
        }

        return false;
    }

    /**
     *
     * @param aRecords
     * @param nsRecords
     * @return nsRecords but with clamped TTLs
     */
    public static List<RR<?>> clampNSDNameTTLsIfTheyreInTheirOwnZone(
            List<RR<?>> aRecords, List<RR<?>> nsRecords) {
        Map<String, Integer> aRecordNamesFound = aRecords.stream()
                .collect(Collectors.toMap(RR::getName, RR::getTtl, (i1, i2) -> i1 > i2 ? i1 : i2));

        return nsRecords.stream()
                .map(rr -> (RR<NSRData>) rr)
                .map( r -> {
                    String nsDName = r.getRData().getNsDName();
                    String[] split = nsDName.split("\\.", 2);
                    boolean nameOrParentDomainMatches = split.length == 2 && !split[1].isBlank() &&
                            (nsDName.equals(r.getName()) || split[1].equals(r.getName()));
                    if(!(nameOrParentDomainMatches || nsDName.endsWith(r.getName()))) {
                        return r;
                    }

                    NSRData rData = r.getRData();
                    Integer ttl = aRecordNamesFound.get(rData.getNsDName());
                    if(ttl != null && r.getTtl() > ttl) {
                        return new RR<>(r.getName(), r.getRrType(), r.getRrClass(),
                                ttl, rData);
                    } else {
                        return r;
                    }
                })
                .collect(Collectors.toList());
    }

    public static Set<RR<NSRData>> filterNSDNamesInTheirOwnZoneWithoutARecords(
            List<RR<?>> aRecords,
            List<RR<?>> nsRecords
    ) {
        /*

        NAME: zdns.google, TYPE: NS, CLASS: IN, TTL: 10800, RDATA:
         NSDNAME: ns3.zdns.google
         */
        Map<String, Integer> aRecordNamesFound = aRecords.stream()
                .collect(Collectors.toMap(RR::getName, RR::getTtl, (i1, i2) -> i1 > i2 ? i1 : i2));

        return nsRecords.stream()
                .map(rr -> (RR<NSRData>) rr)
                .filter(rr -> {
                    String[] split = rr.getRData().getNsDName().split("\\.", 2);
                    if(split.length == 2 && !split[1].isBlank()) {
                        return rr.getRData().getNsDName().equals(rr.getName()) || split[1].equals(rr.getName());
                    }
                    return rr.getRData().getNsDName().endsWith(rr.getName());
                })
                .filter(rr -> !aRecordNamesFound.containsKey(rr.getRData().getNsDName()) ||
                        aRecordNamesFound.get(rr.getRData().getNsDName()) < rr.getTtl())
                .collect(Collectors.toSet());
    }

    @Override
    public RequestStateName getName() {
        return RequestStateName.HANDLE_RESPONSE_TO_ZONE_QUERY;
    }
}
