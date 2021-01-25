package cafe.josh.comfydns.rfc1035.service.search.state;

import cafe.josh.comfydns.rfc1035.cache.CacheAccessException;
import cafe.josh.comfydns.rfc1035.message.field.header.OpCode;
import cafe.josh.comfydns.rfc1035.message.struct.Header;
import cafe.josh.comfydns.rfc1035.message.struct.Message;
import cafe.josh.comfydns.rfc1035.message.struct.Question;
import cafe.josh.comfydns.rfc1035.service.RecursiveResolverTask;
import cafe.josh.comfydns.rfc1035.service.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Consumer;

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
    public void run(ResolverContext rCtx, SearchContext sCtx, RecursiveResolverTask self) throws CacheAccessException, NameResolutionException, StateTransitionCountLimitExceededException {
        Optional<SList.SListServer> best = sCtx.getSList().getBestServer();
        if(best.isEmpty()) {
            throw new NameResolutionException("While looking to send a query for zone " + sCtx.getSList().getZone() +
                    ", we couldn't find any healthy servers to service the request.");
        }
        SList.SListServer bestServer = best.get();
        if(bestServer.getIp() == null) {
            log.info("While looking to send a query for zone " + sCtx.getSList().getZone() +
                    ", all the servers we found did not include matching A records for their NS records");
            self.setState(new SendNSDNameLookup(sCtx.getSList().getServers()));
            self.run();
            return; // TODO is this the best place to do this or should we transition from the FindBestServerToAsk state
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

        Consumer<byte[]> onSuccess = payload -> {
            try {
                self.setState(new HandleResponseToZoneQuery(bestServer, m, payload));
            } catch (StateTransitionCountLimitExceededException e) {
                self.setImmediateDeathState();
            }
            rCtx.getPool().submit(self);
        };

        Consumer<Throwable> onError = e -> {
            try {
                self.setState(new HandleResponseToZoneQuery(bestServer, m, e));
            } catch (StateTransitionCountLimitExceededException e2) {
                self.setImmediateDeathState();
            }
            rCtx.getPool().submit(self);
        };

        log.info("QUERY: " + bestServer.getHostname());
        if(useNonTruncating) {
            if(useId != null) {
                m.getHeader().setId(useId);
            }
            rCtx.getFallback().send(m.write(), bestServer.getIp(), onSuccess, onError);
        } else {
            rCtx.getPrimary().send(m.write(), bestServer.getIp(), onSuccess, onError);
        }
    }

    @Override
    public RequestStateName getName() {
        return RequestStateName.SEND_SERVER_QUERY;
    }
}
