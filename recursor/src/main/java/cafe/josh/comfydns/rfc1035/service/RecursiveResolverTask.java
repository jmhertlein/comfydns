package cafe.josh.comfydns.rfc1035.service;

import cafe.josh.comfydns.rfc1035.cache.CacheAccessException;
import cafe.josh.comfydns.rfc1035.service.search.*;
import cafe.josh.comfydns.rfc1035.service.search.state.ImmediateDeathState;
import cafe.josh.comfydns.rfc1035.service.search.state.TryToAnswerWithLocalInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecursiveResolverTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(RecursiveResolverTask.class);

    public static final int STATE_TRANSITION_COUNT_LIMIT = 128;

    private final SearchContext sCtx;
    private final ResolverContext rCtx;
    private RequestState state;
    private int stateTransitionCount;

    public RecursiveResolverTask(SearchContext sCtx, ResolverContext rCtx) {
        this.sCtx = sCtx;
        this.rCtx = rCtx;
        this.state = new TryToAnswerWithLocalInformation();
        this.stateTransitionCount = 0;
    }

    @Override
    public void run() {
        try {
            this.state.run(rCtx, sCtx, this);
        } catch (CacheAccessException | NameResolutionException | StateTransitionCountLimitExceededException e) {
            log.warn("Returning SERVER_FAILURE to client.", e);
            sCtx.sendOops(e.getClass().getSimpleName() + ": " + e.getMessage());
        } catch (NameErrorException e) {
            sCtx.sendNameError();
        }
    }

    public RequestState getState() {
        return state;
    }

    public void setState(RequestState state) throws StateTransitionCountLimitExceededException {
        if(stateTransitionCount > STATE_TRANSITION_COUNT_LIMIT) {
            throw new StateTransitionCountLimitExceededException("Limit: " + STATE_TRANSITION_COUNT_LIMIT);
        }
        stateTransitionCount++;
        this.state = state;
    }

    public void setImmediateDeathState() {
        this.state = new ImmediateDeathState();
    }
}
