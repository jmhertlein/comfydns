package cafe.josh.comfydns.rfc1035.service.search.state;

import cafe.josh.comfydns.rfc1035.cache.CacheAccessException;
import cafe.josh.comfydns.rfc1035.cache.RRSource;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRType;
import cafe.josh.comfydns.rfc1035.message.field.rr.rdata.CNameRData;
import cafe.josh.comfydns.rfc1035.message.struct.Question;
import cafe.josh.comfydns.rfc1035.message.struct.RR;
import cafe.josh.comfydns.rfc1035.service.RecursiveResolverTask;
import cafe.josh.comfydns.rfc1035.service.search.*;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class TryToAnswerWithLocalInformation implements RequestState {
    private static final Histogram completedRequestStateTransitionCount =
            Histogram.build().name("completed_request_state_transition_count")
            .help("When requests are done, how many state transitions did it take?")
            .labelNames("no_error").buckets(10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 130)
            .register();

    @Override
    public void run(ResolverContext rCtx, SearchContext sCtx, RecursiveResolverTask self) throws CacheAccessException, StateTransitionCountLimitExceededException, NameErrorException {
        Question q = sCtx.getCurrentQuestion();

        List<RRSource> sources = new ArrayList<>();
        sources.add(rCtx.getAuthorityZones());
        sources.add(sCtx.getOverlay());
        for (RRSource source : sources) {
            List<RR<?>> potentialAnswer = source.search(
                    sCtx.getSName(), q.getqType(), q.getqClass(), OffsetDateTime.now());
            if(!potentialAnswer.isEmpty()) {
                sCtx.getAnswer().addAll(potentialAnswer);
                sCtx.updateAnswerAuthoritative(source.isAuthoritative());
                sCtx.nextQuestion();
                if(sCtx.allQuestionsAnswered()) {
                    completedRequestStateTransitionCount.labels("no_error").observe(self.getStateTransitionCount());
                    sCtx.sendAnswer();
                } else {
                    self.setState(new TryToAnswerWithLocalInformation());
                    self.run();
                }
                return;
            }

            List<RR<?>> cnameSearch = source.search(sCtx.getSName(), KnownRRType.CNAME, q.getqClass(), OffsetDateTime.now());
            if(!cnameSearch.isEmpty()) {
                sCtx.getAnswer().add(cnameSearch.get(0));
                sCtx.updateAnswerAuthoritative(source.isAuthoritative());
                sCtx.setsName(((CNameRData) cnameSearch.get(0).getTData()).getDomainName());
                self.setState(new TryToAnswerWithLocalInformation());
                self.run();
                return;
            }
        }

        if(sCtx.getRequest().getMessage().getHeader().getRD()) {
            self.setState(new FindBestServerToAsk());
            self.run();
        } else {
            throw new NameErrorException("Recursion not desired by client.");
        }
    }

    @Override
    public RequestStateName getName() {
        return RequestStateName.TRY_TO_ANSWER_WITH_LOCAL_INFORMATION;
    }
}
