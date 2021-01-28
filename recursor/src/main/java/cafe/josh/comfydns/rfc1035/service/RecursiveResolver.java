package cafe.josh.comfydns.rfc1035.service;

import cafe.josh.comfydns.rfc1035.cache.DNSCache;
import cafe.josh.comfydns.rfc1035.cache.TemporaryDNSCache;
import cafe.josh.comfydns.rfc1035.service.request.Request;
import cafe.josh.comfydns.rfc1035.service.search.ResolverContext;
import cafe.josh.comfydns.rfc1035.service.search.SearchContext;
import cafe.josh.comfydns.rfc1035.service.transport.NonTruncatingTransport;
import cafe.josh.comfydns.rfc1035.service.transport.TruncatingTransport;
import io.prometheus.client.Gauge;
import io.prometheus.client.hotspot.DefaultExports;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class RecursiveResolver {
    private static final ExecutorService pool;
    static {
        pool = Executors.newCachedThreadPool();
        Gauge tasksPending = Gauge.build().name("tasks_pending")
                .help("Tasks pending on the state machine thread pool")
                .register();
        tasksPending.setChild(new Gauge.Child() {
            @Override
            public double get() {
                ThreadPoolExecutor tpe = (ThreadPoolExecutor) pool;
                return tpe.getTaskCount() - tpe.getCompletedTaskCount() - tpe.getActiveCount();
            }
        });

        Gauge tasksActive = Gauge.build().name("tasks_running")
                .help("Tasks running on the state machine thread pool")
                .register();
        tasksActive.setChild(new Gauge.Child() {
            @Override
            public double get() {
                ThreadPoolExecutor tpe = (ThreadPoolExecutor) pool;
                return tpe.getActiveCount();
            }
        });
    }
    private final DNSCache cache;
    private final TruncatingTransport primary;
    private final NonTruncatingTransport fallback;

    public RecursiveResolver(DNSCache cache, TruncatingTransport primary, NonTruncatingTransport fallback) {
        this.cache = cache;
        this.primary = primary;
        this.fallback = fallback;
    }

    public ExecutorService getPool() {
        return pool;
    }

    public void resolve(Request r) {
        RecursiveResolverTask t = new RecursiveResolverTask(
                new SearchContext(r),
                new ResolverContext(this, cache, new TemporaryDNSCache(), pool, primary, fallback)
        );
        pool.submit(t);
    }

    public void shutdown() {
        pool.shutdown();
    }
}
