package com.comfydns.resolver.resolve.rfc1035.service.search.state;

import com.comfydns.resolver.resolve.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolve.rfc1035.cache.CachedNegative;
import com.comfydns.resolver.resolve.rfc1035.cache.RRSource;
import com.comfydns.resolver.resolve.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolve.rfc1035.message.field.rr.rdata.CNameRData;
import com.comfydns.resolver.resolve.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolve.rfc1035.message.struct.Question;
import com.comfydns.resolver.resolve.rfc1035.message.struct.RR;
import com.comfydns.resolver.resolve.rfc1035.service.search.*;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TryToAnswerWithLocalInformation implements RequestState {
    private static final Logger log = LoggerFactory.getLogger(TryToAnswerWithLocalInformation.class);

    private static final Histogram completedRequestStateTransitionCount =
            Histogram.build().name("completed_request_state_transition_count")
            .help("When requests are done, how many state transitions did it take?")
            .labelNames("no_error").buckets(10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 130)
            .register();

    private static final Histogram completedRequestSubQueryCount =
            Histogram.build().name("completed_request_subquery_count")
                    .help("When requests are done, how many (internal) subqueries did it take?")
                    .labelNames("no_error").buckets(10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 130)
                    .register();

    private static final Counter cachedNegativesUsed =
            Counter.build().name("cached_negatives_used")
            .help("How many responses were a result of a cached negative.")
            .register();

    @Override
    public RequestState run(ResolverContext rCtx, SearchContext sCtx) throws CacheAccessException, StateTransitionCountLimitExceededException {
        Question q = sCtx.getCurrentQuestion();

        Optional<CachedNegative> cachedNegative = rCtx.getNegativeCache().cachedNegative(sCtx.getSName(), q.getqType(), q.getqClass(), OffsetDateTime.now());
        if(cachedNegative.isPresent()) {
            cachedNegativesUsed.inc();
            log.debug("{}Cached negative used.", sCtx.getRequestLogPrefix());
            sCtx.forEachListener(l -> l.onNegativeCacheUse(sCtx.getSName(), q.getqType(), q.getqClass()));
            Message ret = sCtx.buildNameErrorResponse(cachedNegative.get().getSoaRR());
            ret.getHeader().setRCode(cachedNegative.get().getRCode());
            return new ResponseReadyState(ret);
        }

        List<RRSource> sources = new ArrayList<>();
        sources.add(rCtx.getAuthorityZones());
        sources.add(sCtx.getOverlay());
        for (RRSource source : sources) {
            List<RR<?>> cachedAnswer = source.search(
                    sCtx.getSName(), q.getqType(), q.getqClass(), OffsetDateTime.now());
            if(!cachedAnswer.isEmpty()) {
                cachedAnswer.forEach(sCtx::addAnswerRR);
                cachedAnswer.forEach(a -> sCtx.forEachListener(l -> l.onAnswerAdded(a)));
                sCtx.updateAnswerAuthoritative(source.isAuthoritative());
                sCtx.nextQuestion();
                if(sCtx.allQuestionsAnswered()) {
                    completedRequestStateTransitionCount.labels("no_error").observe(sCtx.getStateTransitionCount());
                    completedRequestSubQueryCount.labels("no_error").observe(sCtx.getSubQueriesMade());
                    return new ResponseReadyState(sCtx.buildResponse());
                } else {
                    return new SNameCheckingState();
                }
            }

            List<RR<?>> cnameSearch = source.search(sCtx.getSName(), KnownRRType.CNAME, q.getqClass(), OffsetDateTime.now());
            if(!cnameSearch.isEmpty()) {
                sCtx.addAnswerRR(cnameSearch.get(0));
                sCtx.updateAnswerAuthoritative(source.isAuthoritative());
                String oldSName = sCtx.getSName();
                sCtx.setsName(((CNameRData) cnameSearch.get(0).getRData()).getDomainName());
                sCtx.forEachListener(l -> l.onAnswerAdded(cnameSearch.get(0)));
                sCtx.forEachListener(l -> l.onSNameChange(oldSName, sCtx.getSName()));
                return new SNameCheckingState();
            }
        }



        if(shouldRecurse(sCtx)) {
            return new FindBestServerToAsk();
        } else {
            log.debug("{}No local answer found and client did not request recursion or recursion not allowed.", sCtx.getRequestLogPrefix());
            return new ResponseReadyState(sCtx.buildNameErrorResponse());
        }
    }

    private boolean shouldRecurse(SearchContext sCtx) {
        if(!sCtx.getRequest().getMessage().getHeader().getRD()) {
            return false;
        }

        String qName = sCtx.getCurrentQuestion().getQName();
        if(sCtx.getCurrentQuestion().getqType() == KnownRRType.PTR && qName.endsWith("in-addr.arpa")) {
            try {
                String rawInvertedIP = qName.substring(0, qName.length() - "in-addr.arpa".length());
                String forwardIP = String.join(".", List.of(rawInvertedIP.split("\\.")).reversed());

                InetAddress inetAddress = InetAddress.ofLiteral(forwardIP);
                if(inetAddress.isSiteLocalAddress()) {
                    sCtx.forEachListener(l -> l.remark("PTR record is private IP, refusing to recurse."));
                    return false;
                }
            } catch (IllegalArgumentException ignore) {
            }
        }

        return true;
    }

    @Override
    public RequestStateName getName() {
        return RequestStateName.TRY_TO_ANSWER_WITH_LOCAL_INFORMATION;
    }
}
