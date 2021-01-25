package cafe.josh.comfydns.rfc1035.service.search.state;

import cafe.josh.comfydns.rfc1035.cache.CacheAccessException;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRClass;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRType;
import cafe.josh.comfydns.rfc1035.message.field.rr.rdata.CNameRData;
import cafe.josh.comfydns.rfc1035.message.struct.Question;
import cafe.josh.comfydns.rfc1035.message.struct.RR;
import cafe.josh.comfydns.rfc1035.service.RecursiveResolverTask;
import cafe.josh.comfydns.rfc1035.service.search.*;

import java.time.OffsetDateTime;
import java.util.List;

public class TryToAnswerWithLocalInformation implements RequestState {

    @Override
    public void run(ResolverContext rCtx, SearchContext sCtx, RecursiveResolverTask self) throws CacheAccessException, StateTransitionCountLimitExceededException {
        Question q = sCtx.getCurrentQuestion();
        List<RR<?>> potentialAnswer = rCtx.getOverlay().search(sCtx.getSName(), q.getqType(), q.getqClass(), OffsetDateTime.now());
        if(!potentialAnswer.isEmpty()) {
            sCtx.getAnswer().addAll(potentialAnswer);
            sCtx.nextQuestion();
            if(sCtx.allQuestionsAnswered()) {
                sCtx.sendAnswer();
            } else {
                self.setState(new TryToAnswerWithLocalInformation());
                self.run();
            }
            return;
        }

        List<RR<?>> cnameSearch = rCtx.getOverlay().search(sCtx.getSName(), KnownRRType.CNAME, q.getqClass(), OffsetDateTime.now());
        if(!cnameSearch.isEmpty()) {
            sCtx.getAnswer().add(cnameSearch.get(0));
            sCtx.setsName(((CNameRData) cnameSearch.get(0).getTData()).getDomainName());
            self.setState(new TryToAnswerWithLocalInformation());
            self.run();
            return;
        }

        self.setState(new FindBestServerToAsk());
        self.run();
    }

    @Override
    public RequestStateName getName() {
        return RequestStateName.TRY_TO_ANSWER_WITH_LOCAL_INFORMATION;
    }
}
