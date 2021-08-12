package com.comfydns.resolver.resolver.rfc1035.service.search.state;

import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolver.rfc1035.service.RecursiveResolverTask;
import com.comfydns.resolver.resolver.rfc1035.service.search.*;

import java.util.Optional;

public class ImmediateDeathState implements RequestState {
    @Override
    public Optional<RequestState> run(ResolverContext rCtx, SearchContext sCtx) throws CacheAccessException, NameResolutionException, StateTransitionCountLimitExceededException {
        throw new StateTransitionCountLimitExceededException("Limit: " + SearchContext.STATE_TRANSITION_COUNT_LIMIT);
    }

    @Override
    public RequestStateName getName() {
        return RequestStateName.IMMEDIATE_DEATH;
    }
}
