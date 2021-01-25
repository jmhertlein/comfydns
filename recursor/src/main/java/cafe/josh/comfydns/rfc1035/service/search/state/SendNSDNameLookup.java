package cafe.josh.comfydns.rfc1035.service.search.state;

import cafe.josh.comfydns.rfc1035.cache.CacheAccessException;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRClass;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRType;
import cafe.josh.comfydns.rfc1035.message.struct.Header;
import cafe.josh.comfydns.rfc1035.message.struct.Message;
import cafe.josh.comfydns.rfc1035.message.struct.Question;
import cafe.josh.comfydns.rfc1035.service.InternalRequest;
import cafe.josh.comfydns.rfc1035.service.RecursiveResolverTask;
import cafe.josh.comfydns.rfc1035.service.search.*;

import java.util.List;
import java.util.stream.Collectors;

public class SendNSDNameLookup implements RequestState {
    private final List<SList.SListServer> servers;

    public SendNSDNameLookup(List<SList.SListServer> servers) {

        this.servers = servers;
    }

    @Override
    public void run(ResolverContext rCtx, SearchContext sCtx, RecursiveResolverTask self) throws CacheAccessException, NameResolutionException, NameErrorException {
        Message m = new Message();
        m.setHeader(new Header());
        m.getHeader().setQDCount(servers.size());
        List<Question> questions = servers.stream().map(s -> new Question(s.getHostname(), KnownRRType.A, KnownRRClass.IN)).collect(Collectors.toList());
        m.getQuestions().addAll(questions);
        m.getHeader().setRD(true);
        m.getHeader().setIdRandomly();

        InternalRequest req = new InternalRequest(m, message -> {
            try {
                self.setState(new HandleResponseToNSDNameLookup(message, servers));
            } catch (StateTransitionCountLimitExceededException e) {
                self.setImmediateDeathState();
            }
            rCtx.getPool().submit(self);
        });

        rCtx.getRecursiveResolver().resolve(req);
    }

    @Override
    public RequestStateName getName() {
        return RequestStateName.SEND_NSDNAME_LOOKUP;
    }
}
