package com.comfydns.resolver.util;


import com.comfydns.resolver.resolver.rfc1035.cache.AuthorityRRSource;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.SOARData;
import com.comfydns.resolver.resolver.rfc1035.message.struct.RR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.stream.IntStream;

public class DatabaseUtils {
    private static final Logger log = LoggerFactory.getLogger(DatabaseUtils.class);

    private DatabaseUtils() {}

    public static void updateServerDBRecord(Connection c, UUID serverId) throws SQLException {
        boolean exists = false;
        try(PreparedStatement ps = c.prepareStatement("select id from server where id=?")) {
            ps.setObject(1, serverId);
            try(ResultSet rs = ps.executeQuery()) {
                exists = rs.next();
            }
        }
        if(exists) {
            return;
        }

        try(PreparedStatement ps = c.prepareStatement("insert into server (id, created_at, updated_at) values (?, now(), now())")) {
            ps.setObject(1, serverId);
            ps.executeUpdate();
        }
    }

}
