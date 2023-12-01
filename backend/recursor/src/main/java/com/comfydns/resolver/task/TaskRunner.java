package com.comfydns.resolver.task;

import com.comfydns.resolver.resolve.rfc1035.service.RecursiveResolver;
import com.zaxxer.hikari.HikariDataSource;
import io.prometheus.client.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

public class TaskRunner implements Runnable {
    private static final Counter taskErrors, taskDBErrors;
    static {
        taskErrors = Counter.build().name("task_errors")
                .help("Background server tasks that did not succeed.").create();
        taskDBErrors = Counter.build().name("task_db_errors")
                .help("Background server tasks that couldn't properly update db state due to SQLExceptions.").create();
    }
    private static final Logger log = LoggerFactory.getLogger(TaskRunner.class);
    private final Task t;
    private final HikariDataSource dbPool;
    private final RecursiveResolver resolver;

    public TaskRunner(Task t, HikariDataSource dbPool, RecursiveResolver resolver) {
        this.t = t;
        this.dbPool = dbPool;
        this.resolver = resolver;
    }

    @Override
    public void run() {
        Throwable error = null;
        try {
            log.info("Started running task {}", t.getDefinition().getId());
            t.run(new TaskContext(resolver, dbPool));
            log.info("Finished running task {}", t.getDefinition().getId());
        } catch (Throwable t) {
            error = t;
        }

        try(Connection c = dbPool.getConnection()) {
            if(error == null) {
                updateTask(c, true);
            } else {
                taskErrors.inc();
                log.error("Error in task " + this.t.getDefinition().getId(), error);
                updateTask(c, false);
            }
        } catch (SQLException throwables) {
            taskDBErrors.inc();
            log.error("Database error in task " + this.t.getDefinition().getId(), throwables);
        }
    }

    private void updateTask(Connection c, boolean success) throws SQLException {
        c.setAutoCommit(true);
        try(PreparedStatement ps = c.prepareStatement(
                "update task set done=true, failed=?, updated_at=now() where id=?")) {
            ps.setBoolean(1, !success);
            ps.setObject(2, t.getDefinition().getId());
            ps.executeUpdate();
        }
    }
}
