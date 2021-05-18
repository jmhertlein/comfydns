package com.comfydns.resolver.resolver.rfc1035.cache.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface StatementSetupFunction {
    public void setup(PreparedStatement ps) throws SQLException;
}