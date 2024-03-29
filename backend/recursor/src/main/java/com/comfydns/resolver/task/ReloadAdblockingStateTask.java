package com.comfydns.resolver.task;

import com.comfydns.resolver.resolve.block.DBDomainBlocker;
import com.comfydns.resolver.resolve.block.NoOpDomainBlocker;
import com.comfydns.resolver.resolve.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolve.rfc1035.message.InvalidMessageException;
import com.comfydns.resolver.resolve.rfc1035.message.UnsupportedRRTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

public class ReloadAdblockingStateTask implements Task {
    private static final Logger log = LoggerFactory.getLogger(ReloadAdblockingStateTask.class);
    private final TaskDefinition def;

    public ReloadAdblockingStateTask(TaskDefinition d) {
        def = d;
    }

    @Override
    public void run(TaskContext ctx) throws UnsupportedRRTypeException, SQLException, InvalidMessageException, CacheAccessException, ExecutionException, InterruptedException {
        TaskContext context;
        if (ctx instanceof TaskContext) {
            context = (TaskContext) ctx;
        } else {
            throw new RuntimeException("ReloadAdblockingStateTask requires a ResolverContext, was passed a " + ctx.getClass().getName());
        }

        try(Connection c = ctx.getDbPool().getConnection()) {
            log.debug("Checking if adblocking is enabled.");
            if (DBDomainBlocker.isEnabled(c)) {
                log.info("Ad blocking enabled.");
                context.getResolver().setDomainBlocker(new DBDomainBlocker(context.getDbPool()));
            } else {
                context.getResolver().setDomainBlocker(new NoOpDomainBlocker());
            }
        }
    }

    @Override
    public TaskDefinition getDefinition() {
        return def;
    }
}
