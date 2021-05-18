package com.comfydns.resolver.resolver.rfc1035.service.search.state;

import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolver.rfc1035.service.RecursiveResolverTask;
import com.comfydns.resolver.resolver.rfc1035.service.search.*;

public class ImmediateDeathState implements RequestState {
    @Override
    public void run(ResolverContext rCtx, SearchContext sCtx, RecursiveResolverTask self) throws CacheAccessException, NameResolutionException, StateTransitionCountLimitExceededException {
        throw new StateTransitionCountLimitExceededException("Limit: " + RecursiveResolverTask.STATE_TRANSITION_COUNT_LIMIT);
    }

    @Override
    public RequestStateName getName() {
        return RequestStateName.IMMEDIATE_DEATH;
    }
}
