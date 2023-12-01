package com.comfydns.util.db;

import com.comfydns.util.config.DBConfig;
import com.zaxxer.hikari.HikariConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

public class CommonDatabaseUtils {
    private static final Logger log = LoggerFactory.getLogger(CommonDatabaseUtils.class);

    public static HikariConfig mapConfig(DBConfig inputConfig) throws ClassNotFoundException, SQLException, IOException {
        Class.forName("org.postgresql.Driver");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://" + inputConfig.getDbHost() + ":5432/" + inputConfig.getDbName() + "??ApplicationName=comfydns-recursor");
        config.setUsername("comfydns");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        return config;
    }
}
