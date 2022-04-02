package com.comfydns.resolver.resolver.rfc1035.service.search.state;

import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolver.rfc1035.message.field.header.OpCode;
import com.comfydns.resolver.resolver.rfc1035.message.field.query.QOnlyType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Question;
import com.comfydns.resolver.resolver.rfc1035.service.request.Request;
import com.comfydns.resolver.resolver.rfc1035.service.search.*;
import io.prometheus.client.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Optional;

public class InitialCheckingState implements RequestState {
    private static final Logger log = LoggerFactory.getLogger(InitialCheckingState.class);
    private static final Counter dnsQuestions = Counter.build()
            .name("dns_questions").help("DNS Questions received.")
            .labelNames("rrtype", "source").register();
    @Override
    public Optional<RequestState> run(ResolverContext rCtx, SearchContext sCtx) throws CacheAccessException, NameResolutionException, StateTransitionCountLimitExceededException, OptionalFeatureNotImplementedException {
        Request r = sCtx.getRequest();
        r.recordStart();
        if(!r.isSubquery()) {
            Optional<InetAddress> rAddr = r.getRemoteAddress();
            for (Question q : r.getMessage().getQuestions()) {
                log.info("[Q] [{}]: {} | {}", rAddr.map(InetAddress::getHostAddress).orElse("N/A"), q, r.getId());
            }
        }

        Message m = sCtx.getRequest().getMessage();
        if(m.getHeader().getOpCode() == OpCode.IQUERY) {
            throw new OptionalFeatureNotImplementedException("OpCode not implemented: IQUERY");
        }

        for (Question q : sCtx.getRequest().getMessage().getQuestions()) {
            dnsQuestions.labels(q.getqType() instanceof KnownRRType ? q.getqType().getType().toLowerCase() : "other",
                    sCtx.getRequest().isSubquery() ? "internal" : "external");
        }

        if(sCtx.getRequest().getMessage().getQuestions().stream().anyMatch(q -> q.getqType() == QOnlyType.AXFR)) {
            if(sCtx.getRequest().getMessage().getQuestions().size() != 1) {
                throw new OptionalFeatureNotImplementedException("Multi-question where one question is AXFR is not supported.");
            }

            return Optional.of(new ZoneTransferState());
        }

        if(sCtx.getRequest().getMessage().getQuestions().stream().anyMatch(q -> q.getQName().isBlank())) {
            sCtx.sendFormatErrorResponse("QNAME must not be empty.");
            return Optional.empty();
        }
        return Optional.of(new SNameCheckingState());
    }

    @Override
    public RequestStateName getName() {
        return RequestStateName.INITIAL_CHECKING;
    }
}
