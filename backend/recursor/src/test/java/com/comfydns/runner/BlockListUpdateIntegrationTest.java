package com.comfydns.runner;

import com.comfydns.resolver.task.RefreshBlockListsTask;
import com.comfydns.resolver.task.TaskContext;
import com.comfydns.resolver.task.TaskDefinition;
import com.google.gson.JsonObject;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;

public class BlockListUpdateIntegrationTest {
    private static volatile HikariDataSource pool;
    private static final ReentrantLock lock = new ReentrantLock();

    @BeforeAll
    public static void filter() throws ClassNotFoundException, SQLException {
        Assumptions.assumeTrue(Objects.equals(System.getenv("COMFYDNS_INTEGRATION"), "1"));
        lock.lock();
        try {
            if(pool == null) {
                HikariConfig cfg = new HikariConfig();
                cfg.setJdbcUrl("jsbc:postgresql://localhost/comfydns_dev?ApplicationName=comfydns-runner-integration-test");
                Class.forName("org.postgresql.Driver");
                cfg.setUsername("comfydns");
                pool = new HikariDataSource(cfg);
            }
        } finally {
            lock.unlock();
        }
    }

    @Test
    public void test() throws SQLException, ExecutionException, InterruptedException, MalformedURLException {
        RefreshBlockListsTask t = new RefreshBlockListsTask(new TaskDefinition(UUID.randomUUID(), "refresh_block_lists", null,
                true, false, false, new JsonObject()));

        try(Connection c = pool.getConnection(); Statement s = c.createStatement()) {
            s.execute("insert into block_list (id, name, url, auto_update, update_frequency, created_at, updated_at) values " +
                    "(DEFAULT, 'easylist', 'https://raw.githubusercontent.com/justdomains/blocklists/master/lists/easylist-justdomains.txt', true, 'PT15M', now(), now());");
        }

        try(Connection c = pool.getConnection()) {
            TaskContext ctx = new TaskContext(null, pool);
            t.run(ctx);
        }
    }

    @AfterEach
    public void cleanup() throws SQLException, ExecutionException, InterruptedException {
        try(Connection c = pool.getConnection(); Statement s = c.createStatement()) {
            s.execute("delete from blocked_name");
            s.execute("delete from block_list_snapshot");
            s.execute("delete from block_list");
        }
    }
}
