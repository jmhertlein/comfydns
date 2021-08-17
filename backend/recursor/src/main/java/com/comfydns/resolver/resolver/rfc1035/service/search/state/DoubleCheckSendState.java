package com.comfydns.resolver.resolver.rfc1035.service.search.state;

import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.service.RecursiveResolverTask;
import com.comfydns.resolver.resolver.rfc1035.service.search.*;
import com.comfydns.util.config.EnvConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.function.Consumer;

public class DoubleCheckSendState implements RequestState {
    private static final Logger log = LoggerFactory.getLogger(DoubleCheckSendState.class);

    private final Message ourResponse;

    public DoubleCheckSendState(Message ourResponse) {
        this.ourResponse = ourResponse;
    }

    @Override
    public Optional<RequestState> run(ResolverContext rCtx, SearchContext sCtx) throws CacheAccessException, NameResolutionException, StateTransitionCountLimitExceededException, OptionalFeatureNotImplementedException {
        if(!EnvConfig.isDoubleCheckingEnabled()) {
            log.debug("Skipping double-check - no upstream configured.");
            DoubleCheckResultState.doubleCheckResults.labels("skipped").inc();
            return Optional.of(new SendResponseState(ourResponse));
        }

        if(sCtx.getRequest().isSubquery()) {
            log.debug("Skipping double-check - internal request.");
            DoubleCheckResultState.doubleCheckResults.labels("skipped").inc();
            return Optional.of(new SendResponseState(ourResponse));
        }

        for (String d : rCtx.getAuthorityZones().getAuthoritativeForDomains()) {
            if(sCtx.getRequest().getMessage().getQuestions().stream().anyMatch(q -> q.getQName().toLowerCase().endsWith(d.toLowerCase()))) {
                log.debug("Skipping double-check - we are authoritative for this domain.");
                DoubleCheckResultState.doubleCheckResults.labels("skipped").inc();
                return Optional.of(new SendResponseState(ourResponse));
            }
        }

        Consumer<byte[]> onSuccess = payload -> {
            RecursiveResolverTask t;
            try {
                t = new RecursiveResolverTask(sCtx, rCtx, new DoubleCheckResultState(ourResponse, payload));
            } catch (StateTransitionCountLimitExceededException e) {
                t = new RecursiveResolverTask(sCtx, rCtx);
                t.setImmediateDeathState();
            }
            rCtx.getPool().submit(t);
        };

        Consumer<Throwable> onError = e -> {
            RecursiveResolverTask t;
            try {
                log.warn("Error double-checking our response: {}", ourResponse);
                t = new RecursiveResolverTask(sCtx, rCtx, new SendResponseState(ourResponse));
                DoubleCheckResultState.doubleCheckResults.labels("error").inc();
            } catch (StateTransitionCountLimitExceededException e2) {
                t = new RecursiveResolverTask(sCtx, rCtx);
                t.setImmediateDeathState();
            }
            rCtx.getPool().submit(t);
        };

        try {
            rCtx.getPrimary().send(sCtx.getRequest().getMessage().write(), InetAddress.getByName(EnvConfig.getDoubleCheckServer()), onSuccess, onError);
        } catch (UnknownHostException e) {
            throw new NameResolutionException(e);
        }
        return Optional.empty();
    }

    @Override
    public RequestStateName getName() {
        return RequestStateName.DOUBLE_CHECK_SEND_STATE;
    }
}
