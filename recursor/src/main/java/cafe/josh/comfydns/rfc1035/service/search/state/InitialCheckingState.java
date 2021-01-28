package cafe.josh.comfydns.rfc1035.service.search.state;

import cafe.josh.comfydns.rfc1035.cache.CacheAccessException;
import cafe.josh.comfydns.rfc1035.message.field.header.OpCode;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRType;
import cafe.josh.comfydns.rfc1035.message.struct.Message;
import cafe.josh.comfydns.rfc1035.message.struct.Question;
import cafe.josh.comfydns.rfc1035.service.RecursiveResolverTask;
import cafe.josh.comfydns.rfc1035.service.search.*;
import io.prometheus.client.Counter;

public class InitialCheckingState implements RequestState {
    private static final Counter dnsQuestions = Counter.build()
            .name("dns_questions").help("DNS Questions received.")
            .labelNames("rrtype", "source").register();
    @Override
    public void run(ResolverContext rCtx, SearchContext sCtx, RecursiveResolverTask self) throws CacheAccessException, NameResolutionException, NameErrorException, StateTransitionCountLimitExceededException, OptionalFeatureNotImplementedException {
        Message m = sCtx.getRequest().getMessage();
        if(m.getHeader().getOpCode() == OpCode.IQUERY) {
            throw new OptionalFeatureNotImplementedException("OpCode not implemented: IQUERY");
        }

        for (Question q : sCtx.getRequest().getMessage().getQuestions()) {
            dnsQuestions.labels(q.getqType() instanceof KnownRRType ? q.getqType().getType().toLowerCase() : "other",
                    sCtx.getRequest().isInternal() ? "internal" : "external");
        }

        self.setState(new TryToAnswerWithLocalInformation());
        self.run();
    }

    @Override
    public RequestStateName getName() {
        return RequestStateName.INITIAL_CHECKING;
    }
}
