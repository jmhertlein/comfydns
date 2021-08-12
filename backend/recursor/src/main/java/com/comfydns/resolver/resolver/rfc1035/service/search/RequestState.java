package com.comfydns.resolver.resolver.rfc1035.service.search;

import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolver.rfc1035.service.RecursiveResolverTask;

import java.util.Optional;

public interface RequestState {
    public Optional<RequestState> run(ResolverContext rCtx, SearchContext sCtx) throws CacheAccessException, NameResolutionException, StateTransitionCountLimitExceededException, OptionalFeatureNotImplementedException;
    public RequestStateName getName();
}
