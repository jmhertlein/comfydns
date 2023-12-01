package com.comfydns.resolver.task;

import com.comfydns.resolver.resolve.rfc1035.service.RecursiveResolver;
import com.zaxxer.hikari.HikariDataSource;

public class TaskContext {
    private final RecursiveResolver resolver;
    private final HikariDataSource dbPool;

    public TaskContext(RecursiveResolver resolver, HikariDataSource dbPool) {
        this.resolver = resolver;
        this.dbPool = dbPool;
    }

    public RecursiveResolver getResolver() {
        return resolver;
    }

    public HikariDataSource getDbPool() {
        return dbPool;
    }

}
