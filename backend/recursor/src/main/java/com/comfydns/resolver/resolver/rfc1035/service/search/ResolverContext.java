package com.comfydns.resolver.resolver.rfc1035.service.search;

import com.comfydns.resolver.resolver.block.DomainBlocker;
import com.comfydns.resolver.resolver.rfc1035.cache.AuthorityRRSource;
import com.comfydns.resolver.resolver.rfc1035.cache.NegativeCache;
import com.comfydns.resolver.resolver.rfc1035.service.RecursiveResolver;
import com.comfydns.resolver.resolver.rfc1035.service.transport.NonTruncatingSyncTransport;
import com.comfydns.resolver.resolver.rfc1035.service.transport.TruncatingSyncTransport;
import com.comfydns.resolver.resolver.rfc1035.service.transport.async.NonTruncatingTransport;
import com.comfydns.resolver.resolver.rfc1035.service.transport.async.TruncatingTransport;
import com.comfydns.resolver.resolver.rfc1035.cache.RRCache;

import java.util.concurrent.ExecutorService;

public class ResolverContext {
    private final RecursiveResolver recursiveResolver;
    private final RRCache globalCache;
    private final ExecutorService pool;
    private final TruncatingSyncTransport primary;
    private final NonTruncatingSyncTransport fallback;
    private final AuthorityRRSource authorityZones;
    private final NegativeCache negativeCache;
    private final DomainBlocker domainBlocker;

    public ResolverContext(RecursiveResolver recursiveResolver,
                           RRCache globalCache,
                           ExecutorService pool,
                           TruncatingSyncTransport primary,
                           NonTruncatingSyncTransport fallback,
                           AuthorityRRSource authorityZones, NegativeCache negativeCache,
                           DomainBlocker domainBlocker) {
        this.recursiveResolver = recursiveResolver;
        this.globalCache = globalCache;
        this.pool = pool;
        this.primary = primary;
        this.fallback = fallback;
        this.authorityZones = authorityZones;
        this.negativeCache = negativeCache;
        this.domainBlocker = domainBlocker;
    }

    public RRCache getGlobalCache() {
        return globalCache;
    }

    public AuthorityRRSource getAuthorityZones() {
        return authorityZones;
    }

    public ExecutorService getPool() {
        return pool;
    }

    public TruncatingSyncTransport getPrimary() {
        return primary;
    }

    public NonTruncatingSyncTransport getFallback() {
        return fallback;
    }

    public RecursiveResolver getRecursiveResolver() {
        return recursiveResolver;
    }

    public NegativeCache getNegativeCache() {
        return negativeCache;
    }

    public DomainBlocker getDomainBlocker() {
        return domainBlocker;
    }
}
