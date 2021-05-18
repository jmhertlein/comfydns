package com.comfydns.resolver.resolver.rfc1035.cache.impl;

import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolver.rfc1035.cache.NegativeCache;
import com.comfydns.resolver.resolver.rfc1035.message.LabelCache;
import com.comfydns.resolver.resolver.rfc1035.message.field.query.QClass;
import com.comfydns.resolver.resolver.rfc1035.message.field.query.QType;
import com.comfydns.util.db.SimpleConnectionPool;
import io.prometheus.client.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.comfydns.resolver.resolver.rfc1035.cache.impl.CacheMetrics.cachedNegativeRecordsTotal;

public class DBNegativeCache implements NegativeCache {
    private static final Logger log = LoggerFactory.getLogger(DBNegativeCache.class);
    private final SimpleConnectionPool pool;

    public DBNegativeCache(SimpleConnectionPool pool) {
        this.pool = pool;
    }

    @Override
    public boolean cachedNegative(String qName, QType qType, QClass qClass, OffsetDateTime now) throws CacheAccessException {
        Histogram.Timer t = CacheMetrics.negativeCacheReadTimeSeconds.startTimer();
        try (Connection cxn = pool.getConnection().get();
             PreparedStatement ps = cxn.prepareStatement(
                     "select name, qtype, qclass, ttl - floor(extract(epoch from (?-created_at))) as ttl, created_at, expires_at " +
                             "from cached_negative " +
                             "where ? < expires_at and name=? and qtype=? and qclass=?")) {
            ps.setObject(1, now);
            ps.setObject(2, now);
            ps.setString(3, qName);
            ps.setInt(4, qType.getIntValue());
            ps.setInt(5, qClass.getIntValue());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException | InterruptedException | ExecutionException throwables) {
            throw new CacheAccessException(throwables);
        } finally {
            t.observeDuration();
        }
    }

    @Override
    public void cacheNegative(String qName, QType qType, QClass qClass, long ttl, OffsetDateTime now) throws CacheAccessException {
        Histogram.Timer t = CacheMetrics.negativeCacheWriteTimeSeconds.startTimer();
        try(Connection cxn = pool.getConnection().get();
            PreparedStatement ps = cxn.prepareStatement("insert into cached_negative " +
                    "(id, name, qtype, qclass, ttl, created_at, expires_at) " +
                    "values (DEFAULT, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, qName);
            ps.setInt(2, qType.getIntValue());
            ps.setInt(3, qClass.getIntValue());
            ps.setLong(4, ttl);
            ps.setObject(5, now);
            ps.setObject(6, now.plusSeconds(ttl));
            ps.executeUpdate();
            cachedNegativeRecordsTotal.inc();
        } catch (SQLException | InterruptedException | ExecutionException throwables) {
            throw new CacheAccessException(throwables);
        } finally {
            t.observeDuration();
        }
    }

    @Override
    public void bustCacheFor(List<String> qNames) throws CacheAccessException {
        Set<String> names = qNames.stream().flatMap(qn -> LabelCache.genSuffixes(qn).stream())
                .collect(Collectors.toSet());
        try(Connection c = pool.getConnection().get();
            PreparedStatement ps = c.prepareStatement("delete from cached_negative where name=?")) {
            for(String s : names) {
                ps.setString(1, s);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException | InterruptedException | ExecutionException throwables) {
            throw new CacheAccessException(throwables);
        }
    }

    @Override
    public void prune(OffsetDateTime now) throws CacheAccessException {
        try(Connection c = pool.getConnection().get();
            PreparedStatement ps = c.prepareStatement("delete from cached_negative where expires_at <= ?")) {
            ps.setObject(1, now);
            int rows = ps.executeUpdate();
            CacheMetrics.cachedNegativeRecordsPrunedTotal.inc(rows);
            log.debug("Deleted {} cached negatives.", rows);
        } catch (SQLException | InterruptedException | ExecutionException throwables) {
            throw new CacheAccessException(throwables);
        }
    }
}
