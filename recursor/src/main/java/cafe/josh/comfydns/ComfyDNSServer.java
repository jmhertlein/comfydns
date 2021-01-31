package cafe.josh.comfydns;

import cafe.josh.comfydns.rfc1035.cache.AuthoritativeRecordsContainer;
import cafe.josh.comfydns.rfc1035.cache.InMemoryDNSCache;
import cafe.josh.comfydns.rfc1035.cache.NegativeCache;
import cafe.josh.comfydns.rfc1035.cache.RRCache;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRClass;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRType;
import cafe.josh.comfydns.rfc1035.message.field.rr.rdata.ARData;
import cafe.josh.comfydns.rfc1035.message.field.rr.rdata.NSRData;
import cafe.josh.comfydns.rfc1035.message.field.rr.rdata.SOARData;
import cafe.josh.comfydns.rfc1035.message.struct.RR;
import cafe.josh.comfydns.rfc1035.service.RecursiveResolver;
import cafe.josh.comfydns.rfc1035.service.transport.AsyncNonTruncatingTransport;
import cafe.josh.comfydns.rfc1035.service.transport.AsyncTruncatingTransport;
import cafe.josh.comfydns.system.SimpleConnectionPool;
import cafe.josh.comfydns.system.TCPServer;
import cafe.josh.comfydns.system.UDPServer;
import cafe.josh.comfydns.system.http.HttpServer;
import cafe.josh.comfydns.system.http.router.HttpRouter;
import cafe.josh.comfydns.util.Resources;
import io.netty.channel.nio.NioEventLoopGroup;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.GarbageCollectorExports;
import io.prometheus.client.hotspot.MemoryPoolsExports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.time.OffsetDateTime;
import java.util.*;
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

        NegativeCache negativeCache = new NegativeCache();
        cron.scheduleAtFixedRate(() -> {
            try {
                negativeCache.prune(OffsetDateTime.now());
            } catch(Throwable t) {
                log.warn("DNS negative cache pruner error", t);
            }
        }, 30, 30, TimeUnit.SECONDS);

        NioEventLoopGroup bossGroup, workerGroup;
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        RecursiveResolver resolver = new RecursiveResolver(cache, negativeCache, new AsyncTruncatingTransport(workerGroup),
                new AsyncNonTruncatingTransport(workerGroup));

        {
            List<RR<?>> records = new ArrayList<>();
            records.add(new RR<>("hert", KnownRRType.SOA, KnownRRClass.IN, 60 * 60, new SOARData(
                "ns1.hert",
                "jmhertlein@gmail.com",
                1,
                60*60, 60, 60*60*2,
                60
            )));
            records.add(new RR<>("hert", KnownRRType.NS, KnownRRClass.IN, 60*60, new NSRData("ns1.hert")));
            Resources.readLines(getClass().getResourceAsStream("/hert.db"))
                    .map(s -> s.trim().split("\\s+"))
                    .map(arr -> {
                        try {
                            return new RR<>(arr[0].substring(0, arr[0].length()-1), KnownRRType.A, KnownRRClass.IN, 60, new ARData((Inet4Address) Inet4Address.getByName(arr[3])));
                        } catch (UnknownHostException e) {
                            log.error("", e);
                            return null;
                        }
                    }).filter(Objects::nonNull)
                    .forEach(records::add);

            resolver.setAuthorityZones(new AuthoritativeRecordsContainer(records));
        }

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
