package com.comfydns.util.config;

public class DBConfig {
    private final String dbHost;
    private final String dbName;
    private final String password;

    public DBConfig(String dbHost, String dbName) {
        this.dbHost = dbHost;
        this.dbName = dbName;
        this.password = null;
    }

    public DBConfig(String dbHost, String dbName, String password) {
        this.dbHost = dbHost;
        this.dbName = dbName;
        this.password = password;
    }

    public String getDbHost() {
        return dbHost;
    }

    public String getDbName() {
        return dbName;
    }

    public String getPassword() {
        return password;
    }
}
