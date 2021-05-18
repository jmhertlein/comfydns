package com.comfydns.resolver.resolver.block;

import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolver.rfc1035.message.LabelCache;
import com.comfydns.resolver.resolver.rfc1035.message.LabelMaker;
import com.comfydns.util.db.Flag;
import com.comfydns.util.db.SimpleConnectionPool;
import io.prometheus.client.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DBDomainBlocker implements DomainBlocker {
    private static final Logger log = LoggerFactory.getLogger(DBDomainBlocker.class);

    private static final Histogram blockedNameReadTimeSeconds = Histogram.build()
            .name("blocked_name_read_time")
            .help("How long it takes to determine if a name is blocked")
            .buckets(0.001, 0.005, 0.01, 0.05, 0.1, 0.2, 0.3, 0.5, 1.0, 2, 5, 10)
            .register();

    private static final Histogram clientConfigReadTimeSeconds = Histogram.build()
            .name("adblock_client_config_read_time")
            .help("How long it takes to query the db for the configuration for an adblock client")
            .buckets(0.001, 0.005, 0.01, 0.05, 0.1, 0.2, 0.3, 0.5, 1.0, 2, 5, 10)
            .register();

    private final SimpleConnectionPool dbPool;

    private final boolean blockByDefault;

    public DBDomainBlocker(SimpleConnectionPool dbPool) throws SQLException, ExecutionException, InterruptedException {
        this.dbPool = dbPool;

        try(Connection c = dbPool.getConnection().get()) {
            blockByDefault = Flag.enabled("adblock_client_default_on", c);
        }
    }

    @Override
    public boolean isBlocked(String name) throws CacheAccessException {
        Histogram.Timer timer = blockedNameReadTimeSeconds.startTimer();
        log.debug("Checking if {} is blocked", name);
        try(Connection c = dbPool.getConnection().get();
            PreparedStatement ps = c.prepareStatement("select distinct name from blocked_name where name = any(?)")
        ) {
            Array arg = c.createArrayOf("varchar", LabelCache.genSuffixes(name).toArray());
            ps.setArray(1, arg);
            List<String> blockedNames = new ArrayList<>();
            try(ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    blockedNames.add(rs.getString("name"));
                }
            }
            if(!blockedNames.isEmpty()) {
                log.debug("Blocked sname {} due to finding blocked names {}", name, blockedNames);
            }

            return !blockedNames.isEmpty();

        } catch (InterruptedException | SQLException | ExecutionException e) {
            String msg = String.format("Error checking if domain %s is blocked", name);
            log.error(msg, e);
            throw new CacheAccessException(msg, e);
        } finally {
            timer.observeDuration();
        }
    }

    @Override
    public boolean blockForClient(InetAddress addr) throws CacheAccessException {
        Histogram.Timer timer = clientConfigReadTimeSeconds.startTimer();
        log.debug("Checking blocking for {}", addr);
        try(Connection c = dbPool.getConnection().get();
        PreparedStatement ps = c.prepareStatement(
                "select ip, block_ads from ad_block_client_config " +
                        "where ?::inet <<= ip order by masklen(ip) desc limit 1")) {
            ps.setString(1, addr.getHostAddress());
            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    boolean ret = rs.getBoolean("block_ads");
                    log.debug("Blocking for {} is configured as {}", addr, ret);
                    return ret;
                } else {
                    log.debug("Blocking for {} is unconfigured, default is {}", addr, blockByDefault);
                    return blockByDefault;
                }
            }
        } catch (InterruptedException | ExecutionException | SQLException e) {
            String msg = String.format("Error checking if client %s has ad blocking enabled.", addr);
            log.error(msg, e);
            throw new CacheAccessException(msg, e);
        } finally {
            timer.observeDuration();
        }
    }

    public static boolean isEnabled(Connection c) throws SQLException {
        return Flag.enabled("adblock", c);
    }
}
