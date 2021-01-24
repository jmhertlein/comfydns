package cafe.josh.comfydns.rfc1035.service.search.state;

import cafe.josh.comfydns.rfc1035.cache.CacheAccessException;
import cafe.josh.comfydns.rfc1035.message.field.header.OpCode;
import cafe.josh.comfydns.rfc1035.message.struct.Header;
import cafe.josh.comfydns.rfc1035.message.struct.Message;
import cafe.josh.comfydns.rfc1035.message.struct.Question;
import cafe.josh.comfydns.rfc1035.service.RecursiveResolverTask;
import cafe.josh.comfydns.rfc1035.service.search.*;

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
    private final boolean useNonTruncating;

    public SendServerQuery(boolean useNonTruncating) {
        this.useNonTruncating = useNonTruncating;
    }

    @Override
    public void run(ResolverContext rCtx, SearchContext sCtx, RecursiveResolverTask self) throws CacheAccessException, NameResolutionException {
        Optional<SList.SListServer> best = sCtx.getSList().getBestServer();
        if(best.isEmpty()) {
            throw new NameResolutionException("While looking to send a query for zone " + sCtx.getSList().getZone() +
                    ", we couldn't find any healthy servers to service the request.");
        }
        SList.SListServer bestServer = best.get();
        if(bestServer.getIp() == null) {
            throw new NameResolutionException("While looking to send a query for zone " + sCtx.getSList().getZone() +
                    ", all the servers we found did not include matching A records for their NS records. I'm " +
                    "going to try not implementing this and see how it goes.");
        }

        Question q = sCtx.getCurrentQuestion();

        Message m = new Message();
        Header h = new Header();
        h.setRD(false);
        h.setQDCount(1);
        h.setIdRandomly();
        h.setOpCode(OpCode.QUERY);
        m.setHeader(h);
        m.getQuestions().add(new Question(q.getQName(), q.getqType(), q.getqClass()));

        Consumer<byte[]> onSuccess = payload -> {
            self.setState(new HandleResponseToZoneQuery(bestServer, payload));
            rCtx.getPool().submit(self);
        };

        Consumer<Throwable> onError = e -> {
            self.setState(new HandleResponseToZoneQuery(bestServer, e));
            rCtx.getPool().submit(self);
        };

        if(useNonTruncating) {
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
