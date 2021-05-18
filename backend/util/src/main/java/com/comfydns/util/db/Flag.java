package com.comfydns.util.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Flag {
    public static boolean enabled(String name, Connection c) throws SQLException {
        try(PreparedStatement ps = c.prepareStatement("select value from flag where name=?")) {
            ps.setString(1, name);
            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    return rs.getBoolean("value");
                } else {
                    return false;
                }
            }
        }
    }
}
