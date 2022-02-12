package com.comfydns.util.task;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@FunctionalInterface
public interface TaskLoader {
    public List<TaskDefinition> load(Connection c) throws SQLException;
}
