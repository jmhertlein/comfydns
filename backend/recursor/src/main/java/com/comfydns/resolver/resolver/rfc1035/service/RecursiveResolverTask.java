package com.comfydns.resolver.resolver.rfc1035.service;

import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Question;
import com.comfydns.resolver.resolver.rfc1035.service.search.*;
import com.comfydns.resolver.resolver.rfc1035.service.search.state.ImmediateDeathState;
import com.comfydns.resolver.resolver.rfc1035.service.search.state.InitialCheckingState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RecursiveResolverTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(RecursiveResolverTask.class);

    public static final int STATE_TRANSITION_COUNT_LIMIT = 128;

    private final SearchContext sCtx;
    private final ResolverContext rCtx;
    private RequestState state;
    private int stateTransitionCount;

    public RecursiveResolverTask(SearchContext sCtx, ResolverContext rCtx) {
        this.sCtx = sCtx;
        this.rCtx = rCtx;
        this.state = new InitialCheckingState();
        this.stateTransitionCount = 0;
    }

    @Override
    public void run() {
        try {
            this.state.run(rCtx, sCtx, this);
        } catch(OptionalFeatureNotImplementedException e) {
            sCtx.sendNotImplemented();
        } catch (CacheAccessException | NameResolutionException | StateTransitionCountLimitExceededException e) {
            log.debug("[" + sCtx.getRequest().getId() + "]: Returning SERVER_FAILURE to client for request: " + sCtx.getRequest().getMessage(), e);
            sCtx.sendOops("Sorry, something went wrong.");
        } catch(Throwable t) {
            int idx = sCtx.getQuestionIndex().get();
            if(idx < sCtx.getRequest().getMessage().getQuestions().size()) {
                log.debug(String.format("[%s]: Unhandled exception for question: %s", sCtx.getRequest().getId(),
                        sCtx.getCurrentQuestion()), t);
            } else {
                List<Question> questions = sCtx.getRequest().getMessage().getQuestions();
                log.debug(String.format("[%s]: Unhandled exception for question: %s", sCtx.getRequest().getId(),
                        questions.get(questions.size()-1)), t);
            }
            sCtx.sendOops("Sorry, something went wrong.");
        }

    }

    public RequestState getState() {
        return state;
    }

    public void setState(RequestState state) throws StateTransitionCountLimitExceededException {
        log.debug("[{}]: STATE {} -> {}", sCtx.getRequest().getId(), this.state.getName(), state.getName());
        if(stateTransitionCount > STATE_TRANSITION_COUNT_LIMIT) {
            throw new StateTransitionCountLimitExceededException("Limit: " + STATE_TRANSITION_COUNT_LIMIT);
        }
        stateTransitionCount++;
        this.state = state;
    }

    public void setImmediateDeathState() {
        this.state = new ImmediateDeathState();
    }

    public int getStateTransitionCount() {
        return stateTransitionCount;
    }
}
