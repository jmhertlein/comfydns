package com.comfydns.resolver.resolve.rfc1035.cache.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface StatementSetupFunction {
    public void setup(PreparedStatement ps) throws SQLException;
}