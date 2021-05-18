package com.comfydns.resolver.task;

import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolver.rfc1035.cache.impl.AuthoritativeRecordsContainer;
import com.comfydns.resolver.resolver.rfc1035.message.InvalidMessageException;
import com.comfydns.resolver.resolver.rfc1035.message.UnsupportedRRTypeException;
import com.comfydns.resolver.util.DatabaseUtils;
import com.comfydns.util.task.Task;
import com.comfydns.util.task.TaskContext;
import com.comfydns.util.task.TaskDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class ReloadZonesTask implements Task {
    private final Logger log = LoggerFactory.getLogger(ReloadZonesTask.class);
    private final TaskDefinition def;

    public ReloadZonesTask(TaskDefinition d) {
        def = d;
    }

    @Override
    public void run(TaskContext ctx) throws UnsupportedRRTypeException, SQLException, InvalidMessageException, CacheAccessException {
        Connection c = ctx.getConnection();
        ResolverTaskContext context;
        if(ctx instanceof ResolverTaskContext) {
            context = (ResolverTaskContext) ctx;
        } else {
            throw new RuntimeException("ReloadZonesTask requires a ResolverContext, was passed a " + ctx.getClass().getName());
        }

        log.debug("Starting zone reload.");
        context.getResolver().getAuthorityZonesLock().lock();
        try {
            AuthoritativeRecordsContainer newZones = AuthoritativeRecordsContainer.load(c);
            DatabaseUtils.updateServerAuthoritativeZoneState(c, newZones, context.getServerId());
            context.getResolver().setAuthorityZones(newZones);
            log.info("Finished zone reload.");
        } finally {
            context.getResolver().getAuthorityZonesLock().unlock();
        }
    }

    @Override
    public TaskDefinition getDefinition() {
        return def;
    }
}
