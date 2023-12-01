package com.comfydns.resolver.task;

import com.comfydns.resolver.resolve.rfc1035.service.RecursiveResolver;
import com.comfydns.util.db.SimpleConnectionPool;

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
