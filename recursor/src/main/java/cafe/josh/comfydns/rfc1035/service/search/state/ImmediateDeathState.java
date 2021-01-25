package cafe.josh.comfydns.rfc1035.service.search.state;

import cafe.josh.comfydns.rfc1035.cache.CacheAccessException;
import cafe.josh.comfydns.rfc1035.service.RecursiveResolverTask;
import cafe.josh.comfydns.rfc1035.service.search.*;

public class ImmediateDeathState implements RequestState {
    @Override
    public void run(ResolverContext rCtx, SearchContext sCtx, RecursiveResolverTask self) throws CacheAccessException, NameResolutionException, NameErrorException, StateTransitionCountLimitExceededException {
        throw new StateTransitionCountLimitExceededException("Limit: " + RecursiveResolverTask.STATE_TRANSITION_COUNT_LIMIT);
    }

    @Override
    public RequestStateName getName() {
        return RequestStateName.IMMEDIATE_DEATH;
    }
}
