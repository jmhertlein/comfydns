package com.comfydns.resolver.resolver.rfc1035.service.search.state;

import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolver.rfc1035.message.field.header.OpCode;
import com.comfydns.resolver.resolver.rfc1035.message.field.query.QOnlyType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Question;
import com.comfydns.resolver.resolver.rfc1035.service.RecursiveResolverTask;
import com.comfydns.resolver.resolver.rfc1035.service.search.*;
import io.prometheus.client.Counter;

public class InitialCheckingState implements RequestState {
    private static final Counter dnsQuestions = Counter.build()
            .name("dns_questions").help("DNS Questions received.")
            .labelNames("rrtype", "source").register();
    @Override
    public void run(ResolverContext rCtx, SearchContext sCtx, RecursiveResolverTask self) throws CacheAccessException, NameResolutionException, StateTransitionCountLimitExceededException, OptionalFeatureNotImplementedException {
        Message m = sCtx.getRequest().getMessage();
        if(m.getHeader().getOpCode() == OpCode.IQUERY) {
            throw new OptionalFeatureNotImplementedException("OpCode not implemented: IQUERY");
        }

        for (Question q : sCtx.getRequest().getMessage().getQuestions()) {
            dnsQuestions.labels(q.getqType() instanceof KnownRRType ? q.getqType().getType().toLowerCase() : "other",
                    sCtx.getRequest().isInternal() ? "internal" : "external");
        }

        if(sCtx.getRequest().getMessage().getQuestions().stream().anyMatch(q -> q.getqType() == QOnlyType.AXFR)) {
            if(sCtx.getRequest().getMessage().getQuestions().size() != 1) {
                throw new OptionalFeatureNotImplementedException("Multi-question where one question is AXFR is not supported.");
            }

            self.setState(new ZoneTransferState());
            self.run();
            return;
        }

        self.setState(new SNameCheckingState());
        self.run();
    }

    @Override
    public RequestStateName getName() {
        return RequestStateName.INITIAL_CHECKING;
    }
}