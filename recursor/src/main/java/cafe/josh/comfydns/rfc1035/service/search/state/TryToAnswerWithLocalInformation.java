package cafe.josh.comfydns.rfc1035.service.search.state;

import cafe.josh.comfydns.rfc1035.cache.CacheAccessException;
import cafe.josh.comfydns.rfc1035.message.struct.Question;
import cafe.josh.comfydns.rfc1035.message.struct.RR;
import cafe.josh.comfydns.rfc1035.service.RecursiveResolverTask;
import cafe.josh.comfydns.rfc1035.service.search.RequestState;
import cafe.josh.comfydns.rfc1035.service.search.RequestStateName;
import cafe.josh.comfydns.rfc1035.service.search.ResolverContext;
import cafe.josh.comfydns.rfc1035.service.search.SearchContext;

import java.time.OffsetDateTime;
import java.util.List;

public class TryToAnswerWithLocalInformation implements RequestState {

    @Override
    public void run(ResolverContext rCtx, SearchContext sCtx, RecursiveResolverTask self) throws CacheAccessException {
        Question q = sCtx.getCurrentQuestion();
        List<RR<?>> potentialAnswer = rCtx.getOverlay().search(q.getQName(), q.getqType(), q.getqClass(), OffsetDateTime.now());
        if(!potentialAnswer.isEmpty()) {
            sCtx.getAnswer().addAll(potentialAnswer);
            sCtx.nextQuestion();
            if(sCtx.allQuestionsAnswered()) {
                sCtx.sendAnswer();
            } else {
                self.setState(new TryToAnswerWithLocalInformation());
                self.run();
            }
        } else {
            self.setState(new FindBestServerToAsk());
            self.run();
        }
    }

    @Override
    public RequestStateName getName() {
        return RequestStateName.TRY_TO_ANSWER_WITH_LOCAL_INFORMATION;
    }
}
