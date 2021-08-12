package com.comfydns.resolver.resolver.rfc1035.service.search.state;

import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolver.rfc1035.service.search.*;
import io.prometheus.client.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Optional;

public class SNameCheckingState implements RequestState {
    private static final Logger log = LoggerFactory.getLogger(SNameCheckingState.class);
    private static final Counter blockedDomainsUsed =
            Counter.build().name("blocked_domains_used")
                    .help("How many responses were a result of a blocked domain.")
                    .register();

    @Override
    public Optional<RequestState> run(ResolverContext rCtx, SearchContext sCtx) throws CacheAccessException, NameResolutionException, StateTransitionCountLimitExceededException, OptionalFeatureNotImplementedException {
        Optional<InetAddress> remoteAddress = sCtx.getRequest().getRemoteAddress();
        if(remoteAddress.isPresent() && rCtx.getDomainBlocker().blockForClient(remoteAddress.get())) {
            if(rCtx.getDomainBlocker().isBlocked(sCtx.getSName())) {
                log.debug("{}Blocked question for {} from {}", sCtx.getRequestLogPrefix(), sCtx.getCurrentQuestion(), sCtx.getRequest().getRemoteAddress());
                blockedDomainsUsed.inc();
                DoubleCheckResultState.doubleCheckResults.labels("skipped").inc();
                return Optional.of(new SendResponseState(sCtx.buildNameErrorResponse()));
            }
        }

        return Optional.of(new TryToAnswerWithLocalInformation());
    }

    @Override
    public RequestStateName getName() {
        return RequestStateName.SNAME_CHECKING_STATE;
    }
}
