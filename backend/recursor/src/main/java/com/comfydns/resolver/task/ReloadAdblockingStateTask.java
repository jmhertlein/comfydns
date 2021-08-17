package com.comfydns.resolver.task;

import com.comfydns.resolver.resolver.block.DBDomainBlocker;
import com.comfydns.resolver.resolver.block.NoOpDomainBlocker;
import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolver.rfc1035.message.InvalidMessageException;
import com.comfydns.resolver.resolver.rfc1035.message.UnsupportedRRTypeException;
import com.comfydns.util.task.Task;
import com.comfydns.util.task.TaskContext;
import com.comfydns.util.task.TaskDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

public class ReloadAdblockingStateTask implements Task {
    private static final Logger log = LoggerFactory.getLogger(ReloadZonesTask.class);
    private final TaskDefinition def;

    public ReloadAdblockingStateTask(TaskDefinition d) {
        def = d;
    }

    @Override
    public void run(TaskContext ctx) throws UnsupportedRRTypeException, SQLException, InvalidMessageException, CacheAccessException, ExecutionException, InterruptedException {
        Connection c = ctx.getConnection();
        ResolverTaskContext context;
        if(ctx instanceof ResolverTaskContext) {
            context = (ResolverTaskContext) ctx;
        } else {
            throw new RuntimeException("ReloadAdblockingStateTask requires a ResolverContext, was passed a " + ctx.getClass().getName());
        }

        log.debug("Checking if adblocking is enabled.");
        if(DBDomainBlocker.isEnabled(ctx.getConnection())) {
            log.info("Ad blocking enabled.");
            context.getResolver().setDomainBlocker(new DBDomainBlocker(context.getDbPool()));
        } else {
            context.getResolver().setDomainBlocker(new NoOpDomainBlocker());
        }
    }

    @Override
    public TaskDefinition getDefinition() {
        return def;
    }
}
