package com.comfydns.resolver.task;

import com.comfydns.resolver.resolver.rfc1035.service.RecursiveResolver;
import com.comfydns.util.db.SimpleConnectionPool;
import com.comfydns.util.task.TaskContext;

import java.sql.Connection;
import java.util.UUID;

public class ResolverTaskContext implements TaskContext {
    private final UUID serverId;
    private final RecursiveResolver resolver;
    private final Connection c;
    private final SimpleConnectionPool dbPool;

    public ResolverTaskContext(UUID serverId, RecursiveResolver resolver, SimpleConnectionPool dbPool, Connection c) {
        this.serverId = serverId;
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

    @Override
    public Connection getConnection() {
        return c;
    }

    public UUID getServerId() {
        return serverId;
    }
}
