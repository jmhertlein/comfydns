package cafe.josh.comfydns;

import cafe.josh.comfydns.rfc1035.cache.InMemoryDNSCache;
import cafe.josh.comfydns.rfc1035.cache.RRCache;
import cafe.josh.comfydns.rfc1035.service.RecursiveResolver;
import cafe.josh.comfydns.rfc1035.service.transport.AsyncNonTruncatingTransport;
import cafe.josh.comfydns.rfc1035.service.transport.AsyncTruncatingTransport;
import cafe.josh.comfydns.system.SimpleConnectionPool;
import cafe.josh.comfydns.system.TCPServer;
import cafe.josh.comfydns.system.UDPServer;
import cafe.josh.comfydns.system.http.HttpServer;
import cafe.josh.comfydns.system.http.router.HttpRouter;
import io.netty.channel.nio.NioEventLoopGroup;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.GarbageCollectorExports;
import io.prometheus.client.hotspot.MemoryPoolsExports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public class ComfyDNSServer implements Runnable {
    final Logger log = LoggerFactory.getLogger(this.getClass());

    private SimpleConnectionPool pool;
    private final ScheduledExecutorService cron;
    private final AtomicBoolean ready;

    public ComfyDNSServer() {
        this.cron = Executors.newScheduledThreadPool(1);
        this.ready = new AtomicBoolean(false);
    }

    public void run() {
//        try {
//            pool = DatabaseUtils.setupPool(cron);
//        } catch (ClassNotFoundException | SQLException | IOException e) {
//            log.error("Fatal database startup error.", e);
//            return;
//        }

        new MemoryPoolsExports().register();
        new GarbageCollectorExports().register();

        RRCache cache = new InMemoryDNSCache();
        cron.scheduleAtFixedRate(() -> {
            try {
                cache.prune(OffsetDateTime.now());
            } catch(Throwable t) {
                log.warn("DNS cache pruner error", t);
            }
        }, 30, 30, TimeUnit.SECONDS);

        NioEventLoopGroup bossGroup, workerGroup;
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        RecursiveResolver resolver = new RecursiveResolver(cache, new AsyncTruncatingTransport(workerGroup),
                new AsyncNonTruncatingTransport(workerGroup));

        HttpRouter router = new HttpRouter.Builder()
        .build();

        try {
            TCPServer tcp = new TCPServer(resolver, bossGroup, workerGroup);
            UDPServer udp = new UDPServer(resolver, bossGroup);
            HttpServer api = new HttpServer(router, bossGroup, workerGroup);
            HTTPServer server = new HTTPServer(33200);
            api.waitFor();
            tcp.waitFor();
            udp.waitFor();
        } catch (InterruptedException e) {
            log.error("Interrupted.", e);
        } catch (IOException e) {
            log.error("", e);
        } finally {
            try { bossGroup.shutdownGracefully().sync(); } catch (InterruptedException ignore) {}
            try { workerGroup.shutdownGracefully().sync(); } catch (InterruptedException ignore) {}
            this.cron.shutdown();
            resolver.shutdown();
        }
        this.ready.set(true);
    }


    public void stop() {
        log.warn("stop() called. This probably shouldn't happen in prod.");


        this.cron.shutdown();
    }

    public boolean isReady() {
        return ready.get();
    }
}
