package com.comfydns.resolver.resolve.rfc1035.service.search.state;

import com.comfydns.resolver.resolve.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolve.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolve.rfc1035.service.search.*;

import java.util.Optional;

public class ResponseReadyState implements RequestState {
    private final Message m;

    public ResponseReadyState(Message m) {
        this.m = m;
    }

    @Override
    public RequestState run(ResolverContext rCtx, SearchContext sCtx) throws CacheAccessException, NameResolutionException, StateTransitionCountLimitExceededException, OptionalFeatureNotImplementedException {
        return null;
    }

    @Override
    public RequestStateName getName() {
        return RequestStateName.RESPONSE_READY_STATE;
    }

    @Override
    public boolean isTerminal() {
        return true;
    }

    @Override
    public Optional<Message> getResult() {
        return Optional.of(m);
    }
}
