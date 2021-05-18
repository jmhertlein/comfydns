package com.comfydns.resolver.util;


import com.comfydns.resolver.resolver.rfc1035.cache.impl.AuthoritativeRecordsContainer;
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

    public static void updateServerAuthoritativeZoneState(Connection c, AuthoritativeRecordsContainer container, UUID serverId) throws SQLException {
        // delete what's there
        try(PreparedStatement ps = c.prepareStatement("delete from server_authority_state where server_id=?")) {
            ps.setObject(1, serverId);
            int rows = ps.executeUpdate();
            log.debug("Deleted {} rows from server_authority_state", rows);
        }

        // fill out with proper stuff
        try(PreparedStatement ps = c.prepareStatement("insert into server_authority_state (id, soa_name, soa_serial, server_id, created_at, updated_at) values (DEFAULT, ?, ?, ?, now(), now())")) {
            for (RR<SOARData> soa : container.getSOAs()) {
                ps.setString(1, soa.getName());
                ps.setLong(2, soa.getRData().getSerial());
                ps.setObject(3, serverId);
                ps.addBatch();
            }
            int[] rowsChanged = ps.executeBatch();

            log.debug("Inserted {} rows into server_authority_state", IntStream.of(rowsChanged).sum());
        }
    }
}
