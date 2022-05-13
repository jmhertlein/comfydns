package com.comfydns.resolver.resolver.rfc1035.service;

import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Question;
import com.comfydns.resolver.resolver.rfc1035.service.search.*;
import com.comfydns.resolver.resolver.rfc1035.service.search.state.ImmediateDeathState;
import com.comfydns.resolver.resolver.rfc1035.service.search.state.InitialCheckingState;
import com.comfydns.resolver.resolver.rfc1035.service.search.state.SendResponseState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class RecursiveResolverTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(RecursiveResolverTask.class);

    private final SearchContext sCtx;
    private final ResolverContext rCtx;
    private RequestState state;

    public RecursiveResolverTask(SearchContext sCtx, ResolverContext rCtx) {
        this.sCtx = sCtx;
        this.rCtx = rCtx;
        this.state = new InitialCheckingState();
    }

    public RecursiveResolverTask(SearchContext sCtx, ResolverContext rCtx, RequestState cur) throws StateTransitionCountLimitExceededException {
        this.sCtx = sCtx;
        this.rCtx = rCtx;
        setState(cur);
    }

    @Override
    public void run() {
        try {
            Optional<RequestState> next = this.state.run(rCtx, sCtx);
            if(next.isPresent()) {
                setState(next.get());
                run();
            }
        } catch(OptionalFeatureNotImplementedException e) {
            sCtx.forEachListener(l -> l.onException(e));
            sCtx.sendNotImplemented();
        } catch (CacheAccessException | NameResolutionException | StateTransitionCountLimitExceededException e) {
            sCtx.forEachListener(l -> l.onException(e));
            log.warn("[" + sCtx.getRequest().getId() + "]: Returning SERVER_FAILURE to client for request: " + sCtx.getRequest().getMessage(), e);
            sCtx.sendOops("Sorry, something went wrong.");
        } catch(Throwable t) {
            sCtx.forEachListener(l -> l.onException(t));
            log.warn(String.format("[%s]: Unhandled exception for request.", sCtx.getRequest().getId()), t);
            sCtx.sendOops("Sorry, something went wrong.");
        }

    }

    public RequestState getState() {
        return state;
    }

    private void setState(RequestState state) throws StateTransitionCountLimitExceededException {
        log.debug("[{}]: STATE {} -> {}",
                sCtx.getRequest().getId(),
                this.state == null ? "<callback>" : this.state.getName(),
                state.getName()
        );
        sCtx.incrementStateTransitionCount();
        this.state = state;
    }

    public void setImmediateDeathState() {
        this.state = new ImmediateDeathState();
    }

    public int getStateTransitionCount() {
        return sCtx.getStateTransitionCount();
    }
}
