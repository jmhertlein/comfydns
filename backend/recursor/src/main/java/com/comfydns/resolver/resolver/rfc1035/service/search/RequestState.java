package com.comfydns.resolver.resolver.rfc1035.service.search;

import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolver.rfc1035.service.RecursiveResolverTask;

public interface RequestState {
    public void run(ResolverContext rCtx, SearchContext sCtx, RecursiveResolverTask self) throws CacheAccessException, NameResolutionException, StateTransitionCountLimitExceededException, OptionalFeatureNotImplementedException;
    public RequestStateName getName();
}
