package cafe.josh.comfydns.rfc1035.service.search;

import cafe.josh.comfydns.rfc1035.cache.*;
import cafe.josh.comfydns.rfc1035.service.RecursiveResolver;
import cafe.josh.comfydns.rfc1035.service.transport.NonTruncatingTransport;
import cafe.josh.comfydns.rfc1035.service.transport.TruncatingTransport;

import java.util.concurrent.ExecutorService;

public class ResolverContext {
    private final RecursiveResolver recursiveResolver;
    private final RRContainer globalCache;
    private final ExecutorService pool;
    private final TruncatingTransport primary;
    private final NonTruncatingTransport fallback;
    private final AuthoritativeRecordsContainer authorityZones;
    private final NegativeCache negativeCache;

    public ResolverContext(RecursiveResolver recursiveResolver,
                           RRContainer globalCache,
                           ExecutorService pool,
                           TruncatingTransport primary,
                           NonTruncatingTransport fallback,
                           AuthoritativeRecordsContainer authorityZones, NegativeCache negativeCache) {
        this.recursiveResolver = recursiveResolver;
        this.globalCache = globalCache;
        this.pool = pool;
        this.primary = primary;
        this.fallback = fallback;
        this.authorityZones = authorityZones;
        this.negativeCache = negativeCache;
    }

    public RRContainer getGlobalCache() {
        return globalCache;
    }

    public AuthoritativeRecordsContainer getAuthorityZones() {
        return authorityZones;
    }

    public ExecutorService getPool() {
        return pool;
    }

    public TruncatingTransport getPrimary() {
        return primary;
    }

    public NonTruncatingTransport getFallback() {
        return fallback;
    }

    public RecursiveResolver getRecursiveResolver() {
        return recursiveResolver;
    }

    public NegativeCache getNegativeCache() {
        return negativeCache;
    }
}
