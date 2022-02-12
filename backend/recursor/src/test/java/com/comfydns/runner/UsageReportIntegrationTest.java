package com.comfydns.runner;

import com.comfydns.util.db.SimpleConnectionPool;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.UUID;
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
