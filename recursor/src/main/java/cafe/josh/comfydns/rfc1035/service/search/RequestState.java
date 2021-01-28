package cafe.josh.comfydns.rfc1035.service.search;

import cafe.josh.comfydns.rfc1035.cache.CacheAccessException;
import cafe.josh.comfydns.rfc1035.service.RecursiveResolverTask;

public interface RequestState {
    public void run(ResolverContext rCtx, SearchContext sCtx, RecursiveResolverTask self) throws CacheAccessException, NameResolutionException, NameErrorException, StateTransitionCountLimitExceededException, OptionalFeatureNotImplementedException;
    public RequestStateName getName();
}
