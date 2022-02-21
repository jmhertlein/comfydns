package com.comfydns.util.task;

import com.comfydns.resolver.task.ResolverTaskContext;
import com.comfydns.util.db.SimpleConnectionPool;
import io.prometheus.client.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

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
    private final Function<Connection, ResolverTaskContext> contextCreator;
    private final SimpleConnectionPool dbPool;

    public TaskRunner(Task t, Function<Connection, ResolverTaskContext> contextCreator, SimpleConnectionPool dbPool) {
        this.t = t;
        this.contextCreator = contextCreator;
        this.dbPool = dbPool;
    }

    @Override
    public void run() {
        try(Connection c = dbPool.getConnection().get()) {
            try {
                log.info("Started running task {}", t.getDefinition().getId());
                t.run(contextCreator.apply(c));
                log.info("Finished running task {}", t.getDefinition().getId());
                updateTask(c, true);
            } catch (Throwable t) {
                taskErrors.inc();
                log.error("Error in task " + this.t.getDefinition().getId(), t);
                updateTask(c, false);
            }
        } catch (SQLException | InterruptedException | ExecutionException throwables) {
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
