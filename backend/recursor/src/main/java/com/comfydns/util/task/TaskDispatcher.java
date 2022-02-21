package com.comfydns.util.task;

import com.comfydns.resolver.task.ResolverTaskContext;
import com.comfydns.util.db.SimpleConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TaskDispatcher implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(TaskDispatcher.class);

    private final SimpleConnectionPool dbPool;
    private final ExecutorService taskPool;
    private final Function<Connection, ResolverTaskContext> contextCreator;
    private final TaskLoader taskLoader;
    private final TaskCreator taskCreator;

    public TaskDispatcher(SimpleConnectionPool dbPool,
                          ExecutorService taskPool,
                          Function<Connection, ResolverTaskContext> contextCreator, TaskLoader taskLoader, TaskCreator taskCreator) {
        this.dbPool = dbPool;
        this.taskPool = taskPool;
        this.contextCreator = contextCreator;
        this.taskLoader = taskLoader;
        this.taskCreator = taskCreator;
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
        try(Connection c = dbPool.getConnection().get()) {
            c.setAutoCommit(false);
            tasks = taskLoader.load(c).stream()
                    .map(taskCreator::create)
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
            taskPool.submit(new TaskRunner(task, contextCreator, dbPool));
            log.info("Dispatched task {}", task.getDefinition().getId());
        }
    }
}
