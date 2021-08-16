package com.comfydns.resolver.resolver.rfc1035.service;

import com.comfydns.resolver.resolver.block.DomainBlocker;
import com.comfydns.resolver.resolver.block.NoOpDomainBlocker;
import com.comfydns.resolver.resolver.rfc1035.cache.AuthorityRRSource;
import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolver.rfc1035.cache.impl.AuthoritativeRecordsContainer;
import com.comfydns.resolver.resolver.rfc1035.cache.NegativeCache;
import com.comfydns.resolver.resolver.rfc1035.cache.RRCache;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Question;
import com.comfydns.resolver.resolver.rfc1035.service.request.Request;
import com.comfydns.resolver.resolver.rfc1035.service.search.ResolverContext;
import com.comfydns.resolver.resolver.rfc1035.service.search.SearchContext;
import com.comfydns.resolver.resolver.rfc1035.service.transport.NonTruncatingTransport;
import com.comfydns.resolver.resolver.rfc1035.service.transport.TruncatingTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;

public class RecursiveResolver {
    private static final Logger log = LoggerFactory.getLogger(RecursiveResolver.class);
    private final ExecutorService stateMachinePool;
    private final RRCache cache;
    private final NegativeCache negativeCache;
    private final TruncatingTransport primary;
    private final NonTruncatingTransport fallback;
    private final ReentrantLock authorityZonesLock;
    private volatile AuthorityRRSource authorityZones;
    private volatile DomainBlocker domainBlocker;
    private final Set<InetAddress> allowZoneTransferToAddresses;

    public RecursiveResolver(ExecutorService stateMachinePool,
                             RRCache cache,
                             NegativeCache negativeCache,
                             TruncatingTransport primary,
                             NonTruncatingTransport fallback, Set<InetAddress> allowZoneTransferToAddresses) {
        this.stateMachinePool = stateMachinePool;
        this.cache = cache;
        this.negativeCache = negativeCache;
        this.primary = primary;
        this.fallback = fallback;
        this.allowZoneTransferToAddresses = allowZoneTransferToAddresses;
        this.authorityZones = new AuthoritativeRecordsContainer();
        this.authorityZonesLock = new ReentrantLock();
        this.domainBlocker = new NoOpDomainBlocker();
    }

    public void setAuthorityZones(AuthorityRRSource authorityZones) throws CacheAccessException {
        if(!authorityZonesLock.isHeldByCurrentThread()) {
            throw new IllegalStateException("Lock not held by current thread!");
        }
        this.authorityZones = authorityZones;
        negativeCache.bustCacheFor(authorityZones.getNames());
    }

    public Set<InetAddress> getAllowZoneTransferToAddresses() {
        return allowZoneTransferToAddresses;
    }

    public void setDomainBlocker(DomainBlocker domainBlocker) {
        this.domainBlocker = domainBlocker;
    }

    public ReentrantLock getAuthorityZonesLock() {
        return authorityZonesLock;
    }

    public AuthorityRRSource getAuthorityZones() {
        return authorityZones;
    }

    public void resolve(Request r) {
        if(!r.isInternal()) {
            Optional<InetAddress> rAddr = r.getRemoteAddress();
            for (Question q : r.getMessage().getQuestions()) {
                log.info("[Q] [{}]: {} | {}", rAddr.map(InetAddress::getHostAddress).orElse("N/A"), q, r.getId());
            }
        }
        RecursiveResolverTask t = new RecursiveResolverTask(
                new SearchContext(r, cache, r.getParentQSet()),
                new ResolverContext(this, cache, stateMachinePool, primary, fallback, authorityZones, negativeCache, domainBlocker)
        );
        stateMachinePool.submit(t);
    }
}
