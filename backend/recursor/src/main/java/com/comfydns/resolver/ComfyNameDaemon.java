package com.comfydns.resolver;

import com.comfydns.resolver.resolve.block.DBDomainBlocker;
import com.comfydns.resolver.resolve.block.NoOpDomainBlocker;
import com.comfydns.resolver.resolve.rfc1035.cache.AuthorityRRSource;
import com.comfydns.resolver.resolve.rfc1035.cache.NegativeCache;
import com.comfydns.resolver.resolve.rfc1035.cache.RRCache;
import com.comfydns.resolver.resolve.rfc1035.cache.impl.DBAuthorityRRSource;
import com.comfydns.resolver.resolve.rfc1035.cache.impl.DBDNSCache;
import com.comfydns.resolver.resolve.rfc1035.cache.impl.DBNegativeCache;
import com.comfydns.resolver.resolve.rfc1035.service.RecursiveResolver;
import com.comfydns.resolver.resolve.rfc1035.service.transport.TCPSyncTransport;
import com.comfydns.resolver.resolve.rfc1035.service.transport.UDPSyncTransport;
import com.comfydns.resolver.resolve.system.JavaNetTCPServer;
import com.comfydns.resolver.resolve.system.JavaNetUDPServer;
import com.comfydns.resolver.task.ScheduledRefreshRunnable;
import com.comfydns.resolver.task.TaskDispatcher;
import com.comfydns.resolver.task.UsageReportTask;
import com.comfydns.util.config.EnvConfig;
import com.comfydns.util.config.IdFile;
import com.comfydns.util.db.CommonDatabaseUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.GarbageCollectorExports;
import io.prometheus.client.hotspot.MemoryPoolsExports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

public class ComfyNameDaemon {
    final static Logger log = LoggerFactory.getLogger(ComfyNameDaemon.class);

    public static void main(String... args) throws IOException, InterruptedException, SQLException, ExecutionException {
        // for singletons that must run
        ExecutorService apps = Executors.newCachedThreadPool();
        // virtual threads (for serving resolver requests)
        ExecutorService workerPool = Executors.newVirtualThreadPerTaskExecutor();
        // pool for the task dispatcher to run tasks in
        ExecutorService taskPool = getInstrumentedDaemonPool("bgtask");
        // scheduled background tasks (like the task dispatcher)
        ScheduledExecutorService cron = Executors.newScheduledThreadPool(2);

        HikariConfig cfg;
        HikariDataSource ds;
        try {
            cfg = CommonDatabaseUtils.mapConfig(EnvConfig.buildDBConfig());
            ds = new HikariDataSource(cfg);
        } catch (ClassNotFoundException | SQLException | IOException e) {
            log.error("Fatal database startup error.", e);
            System.exit(1);
            return;
        }

        new MemoryPoolsExports().register();
        new GarbageCollectorExports().register();

        RecursiveResolver resolver = initializeResolver(cron, ds);
        Cancel cancelResolverListening = startListening(resolver, apps, workerPool);

        TaskDispatcher taskDispatcher = new TaskDispatcher(ds,
                taskPool,
                resolver);
        cron.scheduleWithFixedDelay(taskDispatcher, 10, 1, TimeUnit.SECONDS);

        cron.scheduleWithFixedDelay(
                new ScheduledRefreshRunnable(ds),
                10, 60, TimeUnit.SECONDS);

        if(!EnvConfig.isUsageReportingDisabled()) {
            startUsageReporting(cron);
        }

        HTTPServer server = new HTTPServer(EnvConfig.getMetricsServerPort()); // this is the prometheus /metrics server... it takes care of itself

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                // stop listening and close down apps thread pool
                log.info("Stopping listening...");
                cancelResolverListening.cancel();
                apps.shutdown();
                apps.awaitTermination(1, TimeUnit.SECONDS);
                apps.shutdownNow();
                log.info("Stopped listening.");


                // wait for requests to drain
                log.info("Waiting for requests to drain...");
                workerPool.shutdown();
                workerPool.awaitTermination(10, TimeUnit.SECONDS);
                workerPool.shutdownNow();
                log.info("Done waiting for requests to drain...");

                // stop scheduled tasks
                log.info("Waiting for scheduled tasks to drain...");
                cron.shutdown();
                cron.awaitTermination(1, TimeUnit.SECONDS);
                cron.shutdownNow();
                taskPool.shutdown();
                taskPool.awaitTermination(1, TimeUnit.SECONDS);
                taskPool.shutdownNow();
                log.info("Done waiting on scheduled tasks.");

                log.info("Closing db pool...");
                ds.close();
                log.info("Closed db pool.");
                log.info("Shutdown complete.");
            } catch (InterruptedException ignore) {}
        }));
        log.info("[Startup] Startup complete!");
    }

    private static void startUsageReporting(ScheduledExecutorService cron) throws IOException {
        IdFile installIdFile = new IdFile(EnvConfig.getPersistentRootPath().resolve("install_id.txt"));
        UUID installId = installIdFile.readOrGenerateAndRead();
        cron.scheduleWithFixedDelay(
                new UsageReportTask(
                        EnvConfig.getUsageReportingProto(),
                        EnvConfig.getUsageReportingDomain(),
                        installId
                ),
                1, 60, TimeUnit.MINUTES);
    }

    public static ExecutorService getInstrumentedDaemonPool(String name) {
        ExecutorService pool = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true);
                return t;
            }
        });
        Gauge tasksPending = Gauge.build().name("pool_" + name + "_tasks_pending")
                .help("Tasks pending on the state machine thread pool")
                .register();
        tasksPending.setChild(new Gauge.Child() {
            @Override
            public double get() {
                ThreadPoolExecutor tpe = (ThreadPoolExecutor) pool;
                return tpe.getTaskCount() - tpe.getCompletedTaskCount() - tpe.getActiveCount();
            }
        });

        Gauge tasksActive = Gauge.build().name("pool_" + name + "_tasks_running")
                .help("Tasks running on the state machine thread pool")
                .register();
        tasksActive.setChild(new Gauge.Child() {
            @Override
            public double get() {
                ThreadPoolExecutor tpe = (ThreadPoolExecutor) pool;
                return tpe.getActiveCount();
            }
        });

        return pool;
    }

    private static RecursiveResolver initializeResolver(
            ScheduledExecutorService cron,
            HikariDataSource dbPool
    ) throws SQLException, ExecutionException, InterruptedException {
        AuthorityRRSource authorityRecords = new DBAuthorityRRSource(dbPool);

        RRCache cache = new DBDNSCache(dbPool);
        cron.scheduleAtFixedRate(() -> {
            try {
                cache.prune(OffsetDateTime.now());
            } catch(Throwable t) {
                log.warn("DNS cache pruner error", t);
            }
        }, 30, 30, TimeUnit.SECONDS);

        NegativeCache negativeCache = new DBNegativeCache(dbPool);
        cron.scheduleAtFixedRate(() -> {
            try {
                negativeCache.prune(OffsetDateTime.now());
            } catch(Throwable t) {
                log.warn("DNS negative cache pruner error", t);
            }
        }, 30, 30, TimeUnit.SECONDS);

        Set<InetAddress> allowZoneTransferToAddresses = null;
        try {
            allowZoneTransferToAddresses = new HashSet<>(
                    EnvConfig.getAllowZoneTransferTo()
            );
        } catch (UnknownHostException e) {
            throw new RuntimeException("Error loading list of IPs to allow zone transfers to.", e);
        }

        RecursiveResolver resolver = new RecursiveResolver(
                cache,
                authorityRecords,
                negativeCache,
                new UDPSyncTransport(),
                new TCPSyncTransport(),
                allowZoneTransferToAddresses);


        try(Connection c = dbPool.getConnection()) {
            if(DBDomainBlocker.isEnabled(c)) {
                resolver.setDomainBlocker(new DBDomainBlocker(dbPool));
            } else {
                resolver.setDomainBlocker(new NoOpDomainBlocker());
            }
        }
        return resolver;
    }

    @FunctionalInterface
    private interface Cancel {
        void cancel();
    }

    private static Cancel startListening(RecursiveResolver resolver,
        ExecutorService appsPool, ExecutorService workerPool
    ) {
        JavaNetUDPServer udp = new JavaNetUDPServer(resolver, workerPool);
        JavaNetTCPServer tcp = new JavaNetTCPServer(resolver, workerPool);
        Future<?> udpFuture = appsPool.submit(udp);
        Future<?> tcpFuture = appsPool.submit(tcp);
        log.info("Resolver started.");
        return () -> {
            log.info("Cancel callback called.");
            udp.setShutdown();
            tcp.setShutdown();
            udpFuture.cancel(true);
            tcpFuture.cancel(true);
            log.info("Cancel callback completed.");
        };
    }
}