package com.comfydns.resolver;

import com.comfydns.util.db.SimpleConnectionPool;
import org.junit.jupiter.api.*;
import org.postgresql.ds.PGConnectionPoolDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

public class DBPoolIntegrationTest {
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
                pgPool.setApplicationName("comfydns-recursor-integration-test");

                pgPool.setDatabaseName("comfydns_dev");
                pgPool.setUser("comfydns");
                pool = new SimpleConnectionPool(pgPool, 1);
            }
        } finally {
            lock.unlock();
        }
    }

    @Test
    public void testWaiting() throws SQLException, ExecutionException, InterruptedException {
        Future<Connection> c1 = pool.getConnection();
        Future<Connection> c2 = pool.getConnection();
        Assertions.assertTrue(c1.isDone());
        Assertions.assertFalse(c2.isDone());
        c1.get().close();
        Assertions.assertTrue(c2.isDone());
        c2.get().close();
    }


}
