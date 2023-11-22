package com.comfydns.resolver.task;

import com.comfydns.resolver.resolver.rfc1035.service.RecursiveResolver;
import com.comfydns.util.db.SimpleConnectionPool;

import java.sql.Connection;

public class TaskContext {
    private final RecursiveResolver resolver;
    private final SimpleConnectionPool dbPool;

    public TaskContext(RecursiveResolver resolver, SimpleConnectionPool dbPool) {
        this.resolver = resolver;
        this.dbPool = dbPool;
    }

    public RecursiveResolver getResolver() {
        return resolver;
    }

    public SimpleConnectionPool getDbPool() {
        return dbPool;
    }

}
