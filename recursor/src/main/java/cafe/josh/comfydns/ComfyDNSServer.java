package cafe.josh.comfydns;

import cafe.josh.comfydns.rfc1035.cache.DNSCache;
import cafe.josh.comfydns.rfc1035.cache.InMemoryDNSCache;
import cafe.josh.comfydns.rfc1035.service.RecursiveResolver;
import cafe.josh.comfydns.rfc1035.service.transport.AsyncNonTruncatingTransport;
import cafe.josh.comfydns.rfc1035.service.transport.AsyncTruncatingTransport;
import cafe.josh.comfydns.system.Metrics;
import cafe.josh.comfydns.system.SimpleConnectionPool;
import cafe.josh.comfydns.system.TCPServer;
import cafe.josh.comfydns.system.UDPServer;
import cafe.josh.comfydns.system.http.HttpServer;
import cafe.josh.comfydns.system.http.Responses;
import cafe.josh.comfydns.system.http.router.HttpRouter;
import com.google.gson.Gson;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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

        HttpRouter router = new HttpRouter.Builder()
                .get("/metrics", r -> Responses.ok(Metrics.getInstance().toJson())).build();

        DNSCache cache = new InMemoryDNSCache();

        EventLoopGroup bossGroup, workerGroup;
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        RecursiveResolver resolver = new RecursiveResolver(cache, new AsyncTruncatingTransport(workerGroup),
                new AsyncNonTruncatingTransport(workerGroup));
        try {
            TCPServer tcp = new TCPServer(resolver, bossGroup, workerGroup);
            UDPServer udp = new UDPServer(resolver, bossGroup);
            HttpServer metrics = new HttpServer(router, bossGroup, workerGroup);
            metrics.waitFor();
            tcp.waitFor();
            udp.waitFor();
        } catch (InterruptedException e) {
            log.error("Interrupted.", e);
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
