package com.comfydns.resolver.resolver.rfc1035.service.search;

import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;

import java.util.Optional;

public interface RequestState {
    public RequestState run(ResolverContext rCtx, SearchContext sCtx) throws CacheAccessException, NameResolutionException, StateTransitionCountLimitExceededException, OptionalFeatureNotImplementedException;
    public RequestStateName getName();

    public default boolean isTerminal() {
        return false;
    }
    public default Optional<Message> getResult() {
        return Optional.empty();
    }
}
