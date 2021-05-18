package com.comfydns.util.db;

import com.comfydns.util.config.DBConfig;
import org.postgresql.ds.PGConnectionPoolDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ScheduledExecutorService;

public class CommonDatabaseUtils {
    private static final Logger log = LoggerFactory.getLogger(CommonDatabaseUtils.class);

    public static SimpleConnectionPool setupPool(DBConfig config, ScheduledExecutorService cron) throws ClassNotFoundException, SQLException, IOException {
        Class.forName("org.postgresql.Driver");

        log.debug("Opening connection pool to db...");
        PGConnectionPoolDataSource pgPool = new PGConnectionPoolDataSource();
        pgPool.setURL("jdbc:postgresql://" + config.getDbHost() + "/");
        pgPool.setApplicationName("comfydns-recursor");

        pgPool.setDatabaseName(config.getDbName());
        pgPool.setUser("comfydns");
        if(config.getPassword() != null) {
            pgPool.setPassword(config.getPassword());
        }
        log.debug(pgPool.getURL());
        SimpleConnectionPool ret;
        ret = new SimpleConnectionPool(pgPool);
        ret.startPruning(cron);
        return ret;
    }
}
