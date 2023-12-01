package com.comfydns.resolver.task;

import com.comfydns.resolver.resolve.rfc1035.service.RecursiveResolver;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TaskDispatcher implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(TaskDispatcher.class);

    private final HikariDataSource dbPool;
    private final ExecutorService taskPool;
    private final RecursiveResolver resolver;

    public TaskDispatcher(HikariDataSource dbPool,
                          ExecutorService taskPool,
                          RecursiveResolver resolver) {
        this.dbPool = dbPool;
        this.taskPool = taskPool;
        this.resolver = resolver;
    }

    @Override
    public void run() {
        try {
            doRun();
        } catch (Throwable t) {
            log.error("Error in task dispatcher", t);
        }
    }

    public void doRun() throws SQLException, ExecutionException, InterruptedException {
        List<Task> tasks;
        try(Connection c = dbPool.getConnection()) {
            c.setAutoCommit(false);
            tasks = loadTasks(c).stream()
                    .map(this::createTask)
                    .collect(Collectors.toList());;


            try(PreparedStatement ps = c.prepareStatement("update task set started=true, updated_at=now() where id=?")) {
                for (Task t : tasks) {
                    ps.setObject(1, t.getDefinition().getId());
                    ps.addBatch();
                }
                int[] started = ps.executeBatch();
                int startedTasks = IntStream.of(started).sum();
                if(startedTasks > 0) {
                    log.debug("Started {} tasks", startedTasks);
                } else {
                    log.debug("No tasks to start");
                }
            }
            c.commit();
        }

        for (Task task : tasks) {
            taskPool.submit(new TaskRunner(task, dbPool, resolver));
            log.info("Dispatched task {}", task.getDefinition().getId());
        }
    }

    private Task createTask(TaskDefinition d) {
        switch(d.getAction()) {
            case "RELOAD_ADBLOCK_CONFIG":
                return new ReloadAdblockingStateTask(d);
            case "TRACE_QUERY":
                return new TraceQueryTask(d);
            case "REFRESH_BLOCK_LIST":
                return new RefreshBlockListsTask(d);
            default:
                throw new IllegalArgumentException("unsupported task type " + d.getAction());
        }
    }

    private List<TaskDefinition> loadTasks(Connection c) throws SQLException {
        List<TaskDefinition> ret = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement("select * from task where " +
                "not done and not started")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ret.add(new TaskDefinition(rs));
                }
            }
        }
        return ret;
    }
}
