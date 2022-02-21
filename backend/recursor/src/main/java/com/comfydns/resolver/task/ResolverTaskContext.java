package com.comfydns.resolver.task;

import com.comfydns.resolver.resolver.rfc1035.service.RecursiveResolver;
import com.comfydns.util.db.SimpleConnectionPool;

import java.sql.Connection;

public class ResolverTaskContext {
    private final RecursiveResolver resolver;
    private final Connection c;
    private final SimpleConnectionPool dbPool;

    public ResolverTaskContext(RecursiveResolver resolver, SimpleConnectionPool dbPool, Connection c) {
        this.resolver = resolver;
        this.dbPool = dbPool;
        this.c = c;
    }

    public RecursiveResolver getResolver() {
        return resolver;
    }

    public SimpleConnectionPool getDbPool() {
        return dbPool;
    }

    public Connection getConnection() {
        return c;
    }
}
