package com.comfydns.resolver.resolver.rfc1035.service.search.state;

import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolver.rfc1035.cache.RRSource;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.CNameRData;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Question;
import com.comfydns.resolver.resolver.rfc1035.message.struct.RR;
import com.comfydns.resolver.resolver.rfc1035.service.search.*;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public Optional<RequestState> run(ResolverContext rCtx, SearchContext sCtx) throws CacheAccessException, StateTransitionCountLimitExceededException {
        Question q = sCtx.getCurrentQuestion();

        if(rCtx.getNegativeCache().cachedNegative(sCtx.getSName(), q.getqType(), q.getqClass(), OffsetDateTime.now())) {
            cachedNegativesUsed.inc();
            log.debug("{}Cached negative used.", sCtx.getRequestLogPrefix());
            return Optional.of(new DoubleCheckSendState(sCtx.buildNameErrorResponse()));
        }

        List<RRSource> sources = new ArrayList<>();
        sources.add(rCtx.getAuthorityZones());
        sources.add(sCtx.getOverlay());
        for (RRSource source : sources) {
            List<RR<?>> potentialAnswer = source.search(
                    sCtx.getSName(), q.getqType(), q.getqClass(), OffsetDateTime.now());
            if(!potentialAnswer.isEmpty()) {
                potentialAnswer.forEach(sCtx::addAnswerRR);
                sCtx.updateAnswerAuthoritative(source.isAuthoritative());
                sCtx.nextQuestion();
                if(sCtx.allQuestionsAnswered()) {
                    completedRequestStateTransitionCount.labels("no_error").observe(sCtx.getStateTransitionCount());
                    completedRequestSubQueryCount.labels("no_error").observe(sCtx.getSubQueriesMade());
                    return Optional.of(new DoubleCheckSendState(sCtx.buildResponse()));
                } else {
                    return Optional.of(new TryToAnswerWithLocalInformation());
                }
            }

            List<RR<?>> cnameSearch = source.search(sCtx.getSName(), KnownRRType.CNAME, q.getqClass(), OffsetDateTime.now());
            if(!cnameSearch.isEmpty()) {
                sCtx.addAnswerRR(cnameSearch.get(0));
                sCtx.updateAnswerAuthoritative(source.isAuthoritative());
                sCtx.setsName(((CNameRData) cnameSearch.get(0).getRData()).getDomainName());
                return Optional.of(new SNameCheckingState());
            }
        }

        if(sCtx.getRequest().getMessage().getHeader().getRD()) {
            return Optional.of(new FindBestServerToAsk());
        } else {
            log.debug("{}No local answer found and client did not request recursion.", sCtx.getRequestLogPrefix());
            DoubleCheckResultState.doubleCheckResults.labels("skipped").inc();
            return Optional.of(new SendResponseState(sCtx.buildNameErrorResponse()));
        }
    }

    @Override
    public RequestStateName getName() {
        return RequestStateName.TRY_TO_ANSWER_WITH_LOCAL_INFORMATION;
    }
}
