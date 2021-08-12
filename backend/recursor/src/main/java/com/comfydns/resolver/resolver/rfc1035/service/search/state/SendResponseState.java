package com.comfydns.resolver.resolver.rfc1035.service.search.state;

import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.service.search.*;

import java.util.Optional;

public class SendResponseState implements RequestState {
    private final Message response;

    public SendResponseState(Message response) {
        this.response = response;
    }
    @Override
    public Optional<RequestState> run(ResolverContext rCtx, SearchContext sCtx) throws CacheAccessException, NameResolutionException, StateTransitionCountLimitExceededException, OptionalFeatureNotImplementedException {
        sCtx.getRequest().answer(response);
        return Optional.empty();
    }

    @Override
    public RequestStateName getName() {
        return RequestStateName.SEND_RESPONSE_STATE;
    }
}
