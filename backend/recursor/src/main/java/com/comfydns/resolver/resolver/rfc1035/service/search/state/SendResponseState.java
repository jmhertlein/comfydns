package com.comfydns.resolver.resolver.rfc1035.service.search.state;

import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.service.RecursiveResolverTask;
import com.comfydns.resolver.resolver.rfc1035.service.search.*;

public class SendResponseState implements RequestState {
    private final Message response;

    public SendResponseState(Message response) {
        this.response = response;
    }
    @Override
    public void run(ResolverContext rCtx, SearchContext sCtx, RecursiveResolverTask self) throws CacheAccessException, NameResolutionException, StateTransitionCountLimitExceededException, OptionalFeatureNotImplementedException {
        sCtx.getRequest().answer(response);
    }

    @Override
    public RequestStateName getName() {
        return RequestStateName.SEND_RESPONSE_STATE;
    }
}
