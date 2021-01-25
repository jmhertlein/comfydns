package cafe.josh.comfydns.rfc1035.service.search;

import cafe.josh.comfydns.rfc1035.cache.DNSCache;
import cafe.josh.comfydns.rfc1035.cache.OverlayCache;
import cafe.josh.comfydns.rfc1035.service.RecursiveResolver;
import cafe.josh.comfydns.rfc1035.service.transport.NonTruncatingTransport;
import cafe.josh.comfydns.rfc1035.service.transport.TruncatingTransport;

import java.util.concurrent.ExecutorService;

public class ResolverContext {
    private final RecursiveResolver recursiveResolver;
    private final DNSCache globalCache;
    private final DNSCache requestCache;
    private final DNSCache overlay;
    private final ExecutorService pool;
    private final TruncatingTransport primary;
    private final NonTruncatingTransport fallback;

    public ResolverContext(RecursiveResolver recursiveResolver, DNSCache globalCache, DNSCache requestCache, ExecutorService pool, TruncatingTransport primary, NonTruncatingTransport fallback) {
        this.recursiveResolver = recursiveResolver;
        this.globalCache = globalCache;
        this.requestCache = requestCache;
        this.pool = pool;
        this.primary = primary;
        this.fallback = fallback;
        this.overlay = new OverlayCache(requestCache, globalCache);
    }

    public DNSCache getGlobalCache() {
        return globalCache;
    }

    public DNSCache getRequestCache() {
        return requestCache;
    }

    public DNSCache getOverlay() {
        return overlay;
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
