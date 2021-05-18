package com.comfydns.runner;

import com.comfydns.util.db.SimpleConnectionPool;
import com.comfydns.util.task.TaskDefinition;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGConnectionPoolDataSource;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;

public class UsageReportIntegrationTest {
    private static volatile SimpleConnectionPool pool;
    private static final ReentrantLock lock = new ReentrantLock();

    @BeforeAll
    public static void filter() {
        Assumptions.assumeTrue(Objects.equals(System.getenv("COMFYDNS_INTEGRATION"), "1"));
    }

    @Test
    public void test() {
        UsageReportTask t = new UsageReportTask("http", "localhost:3000", UUID.randomUUID());
        t.run();
    }
}
