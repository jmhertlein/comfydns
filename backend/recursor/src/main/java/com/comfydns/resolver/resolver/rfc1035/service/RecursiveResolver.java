package com.comfydns.resolver.resolver.rfc1035.service;

import com.comfydns.resolver.resolver.block.DomainBlocker;
import com.comfydns.resolver.resolver.block.NoOpDomainBlocker;
import com.comfydns.resolver.resolver.rfc1035.cache.AuthorityRRSource;
import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolver.rfc1035.cache.NegativeCache;
import com.comfydns.resolver.resolver.rfc1035.cache.RRCache;
import com.comfydns.resolver.resolver.rfc1035.cache.impl.InMemoryAuthorityRRSource;
import com.comfydns.resolver.resolver.rfc1035.cache.impl.InMemoryDNSCache;
import com.comfydns.resolver.resolver.rfc1035.cache.impl.InMemoryNegativeCache;
import com.comfydns.resolver.resolver.rfc1035.message.InvalidMessageException;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.message.struct.MessageReadingException;
import com.comfydns.resolver.resolver.rfc1035.service.request.LiveRequest;
import com.comfydns.resolver.resolver.rfc1035.service.request.Request;
import com.comfydns.resolver.resolver.rfc1035.service.search.*;
import com.comfydns.resolver.resolver.rfc1035.service.search.state.InitialCheckingState;
import com.comfydns.resolver.resolver.rfc1035.service.transport.NonTruncatingSyncTransport;
import com.comfydns.resolver.resolver.rfc1035.service.transport.TruncatingSyncTransport;
import com.comfydns.resolver.resolver.rfc1035.service.transport.async.NonTruncatingTransport;
import com.comfydns.resolver.resolver.rfc1035.service.transport.async.TruncatingTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class RecursiveResolver {
    private static final Logger log = LoggerFactory.getLogger(RecursiveResolver.class);
    private final RRCache cache;
    private final NegativeCache negativeCache;
    private final TruncatingSyncTransport primary;
    private final NonTruncatingSyncTransport fallback;
    private final AuthorityRRSource authorityZones;
    private volatile DomainBlocker domainBlocker;
    private final Set<InetAddress> allowZoneTransferToAddresses;

    public RecursiveResolver(
            TruncatingSyncTransport primary,
            NonTruncatingSyncTransport fallback,
            Set<InetAddress> allowZoneTransferToAddresses
    ) {
        this.cache = new InMemoryDNSCache();
        this.authorityZones = new InMemoryAuthorityRRSource();
        this.negativeCache = new InMemoryNegativeCache();
        this.primary = primary;
        this.fallback = fallback;
        this.domainBlocker = new NoOpDomainBlocker();
        this.allowZoneTransferToAddresses = allowZoneTransferToAddresses;
    }

    public RecursiveResolver(
            RRCache cache,
            AuthorityRRSource authorityRecords,
            NegativeCache negativeCache,
            TruncatingSyncTransport primary,
            NonTruncatingSyncTransport fallback,
            Set<InetAddress> allowZoneTransferToAddresses
    ) {
        this.cache = cache;
        this.negativeCache = negativeCache;
        this.primary = primary;
        this.fallback = fallback;
        this.allowZoneTransferToAddresses = allowZoneTransferToAddresses;
        this.authorityZones = authorityRecords;
        this.domainBlocker = new NoOpDomainBlocker();
    }

    public Set<InetAddress> getAllowZoneTransferToAddresses() {
        return allowZoneTransferToAddresses;
    }

    public void setDomainBlocker(DomainBlocker domainBlocker) {
        this.domainBlocker = domainBlocker;
    }

    /**
     * Performs DNS resolution for the given request.
     * @param r
     * @return
     */
    public Message resolve(Request r) {
        LiveRequest req;
        try {
            req = r.begin();
        } catch (MessageReadingException e) {
            return e.buildResponse();
        } catch (InvalidMessageException e) {
            return Message.invalidMessageResponse();
        }

        SearchContext sCtx = new SearchContext(req, cache, req.getParentQSet());
        ResolverContext rCtx = new ResolverContext(this, cache, null, primary, fallback, authorityZones, negativeCache, domainBlocker);

        RequestState cur = new InitialCheckingState();

        Message response;
        try {
            while(!cur.isTerminal()) {
                RequestState next = cur.run(rCtx, sCtx);
                log.debug("[{}]: STATE {} -> {}",
                        sCtx.getRequest().getId(),
                        cur.getName(),
                        next.getName()
                );
                sCtx.incrementStateTransitionCount();
            }

            response = cur.getResult().get();
        } catch(OptionalFeatureNotImplementedException e) {
            sCtx.forEachListener(l -> l.onException(e));
            response = sCtx.prepareNotImplemented();
        } catch (CacheAccessException | NameResolutionException | StateTransitionCountLimitExceededException e) {
            sCtx.forEachListener(l -> l.onException(e));
            log.warn("[" + sCtx.getRequest().getId() + "]: Returning SERVER_FAILURE to client for request: " + sCtx.getRequest().getMessage(), e);
            response = sCtx.prepareOops("Sorry, something went wrong.");
        } catch(Throwable t) {
            sCtx.forEachListener(l -> l.onException(t));
            log.warn(String.format("[%s]: Unhandled exception for request.", sCtx.getRequest().getId()), t);
            response = sCtx.prepareOops("Sorry, something went wrong.");
        }

        return response;
    }
}
