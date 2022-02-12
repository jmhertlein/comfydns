package com.comfydns.runner;

import com.comfydns.resolver.task.RefreshBlockListsTask;
import com.comfydns.util.db.SimpleConnectionPool;
import com.comfydns.util.task.TaskContext;
import com.comfydns.util.task.TaskDefinition;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.*;
import org.postgresql.ds.PGConnectionPoolDataSource;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;

public class BlockListUpdateIntegrationTest {
    private static volatile SimpleConnectionPool pool;
    private static final ReentrantLock lock = new ReentrantLock();

    @BeforeAll
    public static void filter() throws ClassNotFoundException, SQLException {
        Assumptions.assumeTrue(Objects.equals(System.getenv("COMFYDNS_INTEGRATION"), "1"));
        lock.lock();
        try {
            if(pool == null) {
                Class.forName("org.postgresql.Driver");
                PGConnectionPoolDataSource pgPool = new PGConnectionPoolDataSource();
                pgPool.setURL("jdbc:postgresql://" + "localhost" + "/");
                pgPool.setApplicationName("comfydns-runner-integration-test");

                pgPool.setDatabaseName("comfydns_dev");
                pgPool.setUser("comfydns");
                pool = new SimpleConnectionPool(pgPool);
            }
        } finally {
            lock.unlock();
        }
    }

    @Test
    public void test() throws SQLException, ExecutionException, InterruptedException, MalformedURLException {
        RefreshBlockListsTask t = new RefreshBlockListsTask(new TaskDefinition(UUID.randomUUID(), "refresh_block_lists", null,
                true, false, false, new JsonObject()));

        try(Connection c = pool.getConnection().get(); Statement s = c.createStatement()) {
            s.execute("insert into block_list (id, name, url, auto_update, update_frequency, created_at, updated_at) values " +
                    "(DEFAULT, 'easylist', 'https://raw.githubusercontent.com/justdomains/blocklists/master/lists/easylist-justdomains.txt', true, 'PT15M', now(), now());");
        }

        try(Connection c = pool.getConnection().get()) {
            TaskContext ctx = () -> c;
            t.run(ctx);
        }
    }

    @AfterEach
    public void cleanup() throws SQLException, ExecutionException, InterruptedException {
        try(Connection c = pool.getConnection().get(); Statement s = c.createStatement()) {
            s.execute("delete from blocked_name");
            s.execute("delete from block_list_snapshot");
            s.execute("delete from block_list");
        }
    }
}
