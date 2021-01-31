package cafe.josh.comfydns.rfc1035.service.search.state;

import cafe.josh.comfydns.rfc1035.cache.CacheAccessException;
import cafe.josh.comfydns.rfc1035.message.InvalidMessageException;
import cafe.josh.comfydns.rfc1035.message.UnsupportedRRTypeException;
import cafe.josh.comfydns.rfc1035.message.field.header.RCode;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRType;
import cafe.josh.comfydns.rfc1035.message.field.rr.rdata.NSRData;
import cafe.josh.comfydns.rfc1035.message.field.rr.rdata.SOARData;
import cafe.josh.comfydns.rfc1035.message.struct.Message;
import cafe.josh.comfydns.rfc1035.message.struct.Question;
import cafe.josh.comfydns.rfc1035.message.struct.RR;
import cafe.josh.comfydns.rfc1035.service.RecursiveResolverTask;
import cafe.josh.comfydns.rfc1035.service.search.*;
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
    public void run(ResolverContext rCtx, SearchContext sCtx, RecursiveResolverTask self) throws CacheAccessException, NameResolutionException, NameErrorException, StateTransitionCountLimitExceededException, OptionalFeatureNotImplementedException {
        if(error != null) {
            serverQueried.incrementFailureCount();
            slistServerFailures.inc();
            log.warn("[{}]: Zone query resulted in error: {} {}", sCtx.getRequest().getId(), error.getClass().getSimpleName(), error.getMessage());
            self.setState(new SendServerQuery(false));
            self.run();
            return;
        }

        if(response == null) {
            throw new RuntimeException("Error was null but response was null???");
        }

        Message m;
        try {
            m = Message.read(response);
            log.debug("[{}]: Message received: {}", sCtx.getRequest().getId(), m);
        } catch (InvalidMessageException e) {
            log.warn("Error while reading zone query response", e);
            serverQueried.incrementFailureCount();
            self.setState(new SendServerQuery(false));
            self.run();
            return;
        } catch (UnsupportedRRTypeException e) {
            throw new OptionalFeatureNotImplementedException("Encountered an unsupported RRType while reading zone response",
                    e);
        }

        if(m.getHeader().getId() != sent.getHeader().getId()) {
            log.warn("[{}]: Received message w/ nonmatching ID: expected {} but found {}", sCtx.getRequest().getId(), sent.getHeader().getId(), m.getHeader().getId());
            throw new NameResolutionException("Received message with nonmatching ID: expected " +
                    sent.getHeader().getId() + ", but found " + m.getHeader().getId());
        }

        if(m.getHeader().getTC()) {
            log.debug("Response had TC=1, retrying via tcp.");
            self.setState(new SendServerQuery(true, sent.getHeader().getId()));
            self.run();
            return;
        }


        RCode rCode = m.getHeader().getRCode();
        switch(rCode) {
            case SERVER_FAILURE:
                serverQueried.incrementFailureCount();
                log.warn("[{}]: We got a SERVER_FAILURE back from {}: {}", sCtx.getRequest().getId(), serverQueried.getHostname(), m.toString());
                self.setState(new SendServerQuery(false));
                self.run();
                return;
            case REFUSED:
            case NOT_IMPLEMENTED:
            case FORMAT_ERROR:
                sCtx.getSList().removeServer(serverQueried);
                self.setState(new SendServerQuery(false));
                self.run();
                return;
            case NAME_ERROR:
                if(m.getHeader().getAA()) {
                    log.debug("[{}] Received authoritative name error.", sCtx.getRequest().getId());
                    throw new NameErrorException("Received authoritative name error.");
                } else {
                    log.debug("[{}] Received non-authoritative name error.", sCtx.getRequest().getId());
                    sCtx.getSList().removeServer(serverQueried);
                    self.setState(new SendServerQuery(false));
                    self.run();
                }
                return;
        }

        if(rCode != RCode.NO_ERROR) {
            throw new RuntimeException("Unhandled RCODE:" + rCode);
        }

        log.debug("Checking for SOA -> nameerror");
        if(m.getHeader().getAA()) {
            Optional<RR<?>> soaFound = m.getAuthorityRecords()
                    .stream()
                    .filter(rr -> rr.getRrType() == KnownRRType.SOA)
                    .filter(soa -> sCtx.getSName().endsWith(soa.getName())).findFirst();

            if(soaFound.isPresent()) {
                Question q = sCtx.getCurrentQuestion();
                rCtx.getNegativeCache().cacheNegative(
                        sCtx.getSName(),
                        q.getqType(), q.getqClass(),
                        ((SOARData) soaFound.get().getTData()).getMinimum(),
                        OffsetDateTime.now()
                );
                throw new NameErrorException();
            }
        }

        log.debug("[{}]: Processing RRs in response.", sCtx.getRequest().getId());
        List<RR<?>> rrs = new ArrayList<>();
        m.forEach(rrs::add);
        if(m.getHeader().getANCount() > 0) {
            Set<RR<NSRData>> badRecords = filterNSDNamesInTheirOwnZoneWithoutARecords(m);
            rrs.removeAll(badRecords);
        } else {
            if(!filterNSDNamesInTheirOwnZoneWithoutARecords(m).isEmpty()) {
                serverQueried.incrementFailureCount();
                self.setState(new SendServerQuery(false));
                self.run();
                return;
            }
        }

        for (RR<?> rr : rrs) {
            if(rr.getTtl() == 0) {
                sCtx.getRequestCache().cache(rr, OffsetDateTime.now());
            } else {
                rCtx.getGlobalCache().cache(rr, OffsetDateTime.now());
            }
        }

        self.setState(new TryToAnswerWithLocalInformation());
        self.run();
    }

    public static Set<RR<NSRData>> filterNSDNamesInTheirOwnZoneWithoutARecords(Message m) {
        /*

        NAME: zdns.google, TYPE: NS, CLASS: IN, TTL: 10800, RDATA:
         NSDNAME: ns3.zdns.google
         */
        Set<String> aRecordNamesFound = m.getAdditionalRecords().stream()
                .filter(r -> r.getRrType() == KnownRRType.A)
                .map(RR::getName)
                .collect(Collectors.toSet());

        return m.getAuthorityRecords().stream()
                .filter(rr -> rr.getRrType() == KnownRRType.NS)
                .map(rr -> (RR<NSRData>) rr)
                .filter(rr -> {
                    String[] split = rr.getTData().getNsDName().split("\\.", 2);
                    if(split.length == 2 && !split[1].isBlank()) {
                        return rr.getTData().getNsDName().equals(rr.getName()) || split[1].equals(rr.getName());
                    }
                    return rr.getTData().getNsDName().endsWith(rr.getName());
                })
                .filter(rr -> !aRecordNamesFound.contains(rr.getTData().getNsDName()))
                .collect(Collectors.toSet());
    }

    @Override
    public RequestStateName getName() {
        return RequestStateName.HANDLE_RESPONSE_TO_ZONE_QUERY;
    }
}
