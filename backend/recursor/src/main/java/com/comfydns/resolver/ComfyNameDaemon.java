package com.comfydns.resolver;

import com.comfydns.resolver.resolver.ComfyResolverThread;
import com.comfydns.resolver.task.ScheduledRefreshRunnable;
import com.comfydns.resolver.task.UsageReportTask;
import com.comfydns.util.config.EnvConfig;
import com.comfydns.util.config.IdFile;
import com.comfydns.util.db.CommonDatabaseUtils;
import com.comfydns.util.db.SimpleConnectionPool;
import com.comfydns.resolver.task.TaskDispatcher;
import io.netty.channel.nio.NioEventLoopGroup;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.HTTPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.*;

public class ComfyNameDaemon {
    final static Logger log = LoggerFactory.getLogger(ComfyNameDaemon.class);

    public static void main(String... args) throws IOException, InterruptedException, SQLException, ExecutionException {
        IdFile installIdFile = new IdFile(EnvConfig.getPersistentRootPath().resolve("install_id.txt"));
        UUID installId = installIdFile.readOrGenerateAndRead();

        NioEventLoopGroup bossGroup, workerGroup;
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        ExecutorService stateMachinePool = getInstrumentedDaemonPool("state_machine");
        ExecutorService taskPool = getInstrumentedDaemonPool("bgtask");

        ScheduledExecutorService cron = Executors.newScheduledThreadPool(2);

        SimpleConnectionPool dbPool;
        try {
            dbPool = CommonDatabaseUtils.setupPool(EnvConfig.buildDBConfig(), cron);
        } catch (ClassNotFoundException | SQLException | IOException e) {
            log.error("Fatal database startup error.", e);
            System.exit(1);
            return;
        }

        ExecutorService apps = Executors.newCachedThreadPool();

        ComfyResolverThread d = new ComfyResolverThread(workerGroup, bossGroup, cron, stateMachinePool, dbPool);
        apps.submit(d);

        Thread.sleep(50);

        while (!d.isReady()) {
            log.info("[Startup] Waiting for resolver to be ready...");
            Thread.sleep(1000);
        }

        //TODO: this has gotten out of hand, now there are two of them. (move this into a subclass... ResolverTaskDispatcher?
        TaskDispatcher taskDispatcher = new TaskDispatcher(dbPool,
                taskPool,
                d.getResolver());
        cron.scheduleWithFixedDelay(taskDispatcher, 10, 1, TimeUnit.SECONDS);

        cron.scheduleWithFixedDelay(
                new ScheduledRefreshRunnable(dbPool),
                10, 60, TimeUnit.SECONDS);

        if(!EnvConfig.isUsageReportingDisabled()) {
            cron.scheduleWithFixedDelay(
                    new UsageReportTask(
                            EnvConfig.getUsageReportingProto(),
                            EnvConfig.getUsageReportingDomain(),
                            installId
                    ),
                    1, 60, TimeUnit.MINUTES);
        }

        HTTPServer server = new HTTPServer(EnvConfig.getMetricsServerPort()); // this is the prometheus /metrics server... it takes care of itself

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            apps.shutdownNow();
            cron.shutdownNow();
            stateMachinePool.shutdownNow();
        }));
        log.info("[Startup] Startup complete!");
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
}