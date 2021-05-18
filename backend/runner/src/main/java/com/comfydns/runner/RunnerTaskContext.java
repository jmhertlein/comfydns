package com.comfydns.runner;

import com.comfydns.util.task.TaskContext;

import java.sql.Connection;


public class RunnerTaskContext implements TaskContext {
    private final Connection c;

    public RunnerTaskContext(Connection c) {
        this.c = c;
    }

    @Override
    public Connection getConnection() {
        return c;
    }
}
