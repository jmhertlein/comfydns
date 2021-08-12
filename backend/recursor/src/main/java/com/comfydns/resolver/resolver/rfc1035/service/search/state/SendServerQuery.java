package com.comfydns.resolver.resolver.rfc1035.service.search.state;

import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolver.rfc1035.message.field.header.OpCode;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Header;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Question;
import com.comfydns.resolver.resolver.rfc1035.message.struct.RR;
import com.comfydns.resolver.resolver.rfc1035.service.RecursiveResolverTask;
import com.comfydns.resolver.resolver.rfc1035.service.search.*;
import io.prometheus.client.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * This assumes the SList has been populated to the best of the
 * ability of the local information.
 * It attempts to contact the best available server, potentially
 * sending requests to resolve an NSDNAME if such information wasn't
 * available (this should be rare). Failure to look up an NSDNAME counts as a failure
 * for that server overall.
 */
public class SendServerQuery implements RequestState {
    private static final Logger log = LoggerFactory.getLogger(SendServerQuery.class);
    private static final Counter externalQueriesSent = Counter.build()
            .name("external_requests_sent").help("Requests sent to other DNS servers.").register();

    private final boolean useNonTruncating;
    private final Integer useId;

    public SendServerQuery(boolean useNonTruncating) {
        this.useNonTruncating = useNonTruncating;
        this.useId = null;
    }

    public SendServerQuery(boolean useNonTruncating, Integer useId) {
        this.useNonTruncating = useNonTruncating;
        this.useId = useId;
    }

    @Override
    public Optional<RequestState> run(ResolverContext rCtx, SearchContext sCtx) throws CacheAccessException, NameResolutionException, StateTransitionCountLimitExceededException {
        Optional<SList.SListServer> best = sCtx.getSList().getBestServer();
        if(best.isEmpty()) {
            log.debug("{}While looking to send a query for zone {}, we couldn't find any healthy servers to service the request. Question was: {}", sCtx.getRequestLogPrefix(), sCtx.getSList().getZone(), sCtx.getCurrentQuestion());
            return Optional.of(new DoubleCheckSendState(sCtx.buildNameErrorResponse()));
        }
        SList.SListServer bestServer = best.get();
        if(bestServer.getIp() == null) {
            log.debug("While looking to send a query for zone " + sCtx.getSList().getZone() +
                    ", all the servers we found did not include matching A records for their NS records");

            if(sCtx.getSList().getServers().stream().anyMatch(s -> s.getHostname().equals(sCtx.getSName()))) {
                log.debug("While trying to answer [{}], we have been told that {} is its own nameserver. This is going to cause" +
                        " infinite recursion. I'm going to bust the cache records for this and fail the request.", sCtx.getCurrentQuestion(), sCtx.getSName());
                List<RR<?>> removals = sCtx.getSList().getServers().stream()
                        .filter(s -> s.getNsRecord().isPresent())
                        .map(s -> s.getNsRecord().get())
                        .collect(Collectors.toList());
                log.debug("Expunging these RRs from the cache: \n{}\n", removals.stream().map(RR::toString)
                .collect(Collectors.joining("\n")));
                rCtx.getGlobalCache().expunge(removals);
                throw new NameResolutionException("We almost went into infinite recursion. Try again later.");
            }
            return Optional.of(new SendNSDNameLookup(sCtx.getSList().getServers()));
        }

        Question q = sCtx.getCurrentQuestion();

        Message m = new Message();
        Header h = new Header();
        h.setRD(false);
        h.setQDCount(1);
        h.setIdRandomly();
        h.setOpCode(OpCode.QUERY);
        m.setHeader(h);
        m.getQuestions().add(new Question(sCtx.getSName(), q.getqType(), q.getqClass()));

        if(sCtx.isInQset(bestServer.getIp(), m.getQuestions().get(0))) {
            log.debug("[{}]: Refusing to ask the same question twice: {}",
                    sCtx.getRequest().getId(), m.getQuestions().get(0));
            throw new NameResolutionException("Refusing to ask the same question twice.");
        }

        Consumer<byte[]> onSuccess = payload -> {
            RecursiveResolverTask t;
            try {
                t = new RecursiveResolverTask(
                        sCtx,
                        rCtx,
                        new HandleResponseToZoneQuery(bestServer, m, payload));
            } catch (StateTransitionCountLimitExceededException e) {
                t = new RecursiveResolverTask(sCtx, rCtx);
                t.setImmediateDeathState();
            }
            rCtx.getPool().submit(t);
        };

        Consumer<Throwable> onError = e -> {
            RecursiveResolverTask t;
            try {
                t = new RecursiveResolverTask(sCtx, rCtx, new HandleResponseToZoneQuery(bestServer, m, e));
            } catch (StateTransitionCountLimitExceededException e2) {
                t = new RecursiveResolverTask(sCtx, rCtx);
                t.setImmediateDeathState();
            }
            rCtx.getPool().submit(t);
        };

        log.debug("[{}]: QUERY: {} ({})", sCtx.getRequest().getId(), bestServer.getHostname(), bestServer.getIp());
        sCtx.addToQSet(bestServer.getIp(), m.getQuestions().get(0));
        externalQueriesSent.inc();
        if(useNonTruncating) {
            if(useId != null) {
                m.getHeader().setId(useId);
            }
            rCtx.getFallback().send(m.write(), bestServer.getIp(), onSuccess, onError);
        } else {
            rCtx.getPrimary().send(m.write(), bestServer.getIp(), onSuccess, onError);
        }
        return Optional.empty();
    }

    @Override
    public RequestStateName getName() {
        return RequestStateName.SEND_SERVER_QUERY;
    }
}
