package cafe.josh.comfydns.rfc1035.service.search;

import cafe.josh.comfydns.rfc1035.cache.OverlayCache;
import cafe.josh.comfydns.rfc1035.cache.RRContainer;
import cafe.josh.comfydns.rfc1035.cache.RRSource;
import cafe.josh.comfydns.rfc1035.cache.TemporaryDNSCache;
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

    public ResolverContext(RecursiveResolver recursiveResolver, RRContainer globalCache, ExecutorService pool, TruncatingTransport primary, NonTruncatingTransport fallback) {
        this.recursiveResolver = recursiveResolver;
        this.globalCache = globalCache;
        this.pool = pool;
        this.primary = primary;
        this.fallback = fallback;
    }

    public RRContainer getGlobalCache() {
        return globalCache;
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
}
