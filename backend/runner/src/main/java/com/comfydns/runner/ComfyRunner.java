package com.comfydns.runner;

import com.comfydns.resolver.ComfyNameDaemon;
import com.comfydns.util.config.EnvConfig;
import com.comfydns.util.config.IdFile;
import com.comfydns.util.db.CommonDatabaseUtils;
import com.comfydns.util.task.TaskDefinition;
import com.comfydns.util.task.TaskDispatcher;
import com.comfydns.util.db.SimpleConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ComfyRunner {
    private static final Logger log = LoggerFactory.getLogger(ComfyRunner.class);

    public static void main(String ... args) throws IOException {
        IdFile installIdFile = new IdFile(EnvConfig.getPersistentRootPath().resolve("install_id.txt"));
        UUID installId = installIdFile.readOrGenerateAndRead();

        final AtomicBoolean stop = new AtomicBoolean(false);
        ScheduledExecutorService cron = Executors.newScheduledThreadPool(2);
        ExecutorService taskPool = ComfyNameDaemon.getInstrumentedDaemonPool("runnertask");
        SimpleConnectionPool dbPool;
        try {
            dbPool = CommonDatabaseUtils.setupPool(EnvConfig.buildDBConfig(), cron);
        } catch (ClassNotFoundException | SQLException | IOException e) {
            log.error("Fatal database startup error.", e);
            System.exit(1);
            return;
        }

        TaskDispatcher taskDispatcher = new TaskDispatcher(dbPool,
                taskPool,
                RunnerTaskContext::new,
                c -> {
                    List<TaskDefinition> ret = new ArrayList<>();
                    try (PreparedStatement ps = c.prepareStatement("select * from task where " +
                            "server_id is null and not done and not started")) {
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                ret.add(new TaskDefinition(rs));
                            }
                        }
                    }
                    return ret;
                }, new RunnerTaskCreator());

        cron.scheduleWithFixedDelay(
                taskDispatcher,
                10, 1, TimeUnit.SECONDS);

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

        while(!stop.get()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                log.info("Interrupted while sleeping in main thread?", e);
            }
        }

    }
}
