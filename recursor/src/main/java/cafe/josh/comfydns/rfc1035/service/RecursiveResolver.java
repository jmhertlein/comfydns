package cafe.josh.comfydns.rfc1035.service;

import cafe.josh.comfydns.rfc1035.cache.DNSCache;
import cafe.josh.comfydns.rfc1035.cache.TemporaryDNSCache;
import cafe.josh.comfydns.rfc1035.service.request.Request;
import cafe.josh.comfydns.rfc1035.service.search.ResolverContext;
import cafe.josh.comfydns.rfc1035.service.search.SearchContext;
import cafe.josh.comfydns.rfc1035.service.transport.NonTruncatingTransport;
import cafe.josh.comfydns.rfc1035.service.transport.TruncatingTransport;
import cafe.josh.comfydns.system.Metrics;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecursiveResolver {
    private final ExecutorService pool;
    private final DNSCache cache;
    private final TruncatingTransport primary;
    private final NonTruncatingTransport fallback;

    public RecursiveResolver(DNSCache cache, TruncatingTransport primary, NonTruncatingTransport fallback) {
        this.cache = cache;
        this.primary = primary;
        this.fallback = fallback;

        this.pool = Executors.newCachedThreadPool();
    }

    public ExecutorService getPool() {
        return pool;
    }

    public void resolve(Request r) {
        RecursiveResolverTask t = new RecursiveResolverTask(
                new SearchContext(r),
                new ResolverContext(this, cache, new TemporaryDNSCache(), pool, primary, fallback)
        );
        Metrics.getInstance().getRequestsReceived().incrementAndGet();
        pool.submit(t);
    }

    public void shutdown() {
        pool.shutdown();
    }
}
