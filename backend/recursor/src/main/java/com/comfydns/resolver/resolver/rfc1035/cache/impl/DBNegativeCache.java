package com.comfydns.resolver.resolver.rfc1035.cache.impl;

import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolver.rfc1035.cache.NegativeCache;
import com.comfydns.resolver.resolver.rfc1035.message.LabelCache;
import com.comfydns.resolver.resolver.rfc1035.message.field.query.QClass;
import com.comfydns.resolver.resolver.rfc1035.message.field.query.QType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.RRClass;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.SOARData;
import com.comfydns.resolver.resolver.rfc1035.message.struct.RR;
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
import java.util.Optional;
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
    public Optional<RR<SOARData>> cachedNegative(String qName, QType qType, QClass qClass, OffsetDateTime now) throws CacheAccessException {
        try (Histogram.Timer t = CacheMetrics.negativeCacheReadTimeSeconds.startTimer();
             Connection cxn = pool.getConnection().get();
             PreparedStatement ps = cxn.prepareStatement(
                     "select r_name, r_class, r_ttl - floor(extract(epoch from (?-created_at))) as r_ttl, " +
                             "r_mname, r_rname, r_serial, r_refresh, r_retry, r_expire, r_minimum " +
                             "from cached_negative " +
                             "where ? < expires_at and qname=? and qtype=? and qclass=?")) {
            ps.setObject(1, now);
            ps.setObject(2, now);
            ps.setString(3, qName);
            ps.setInt(4, qType.getIntValue());
            ps.setInt(5, qClass.getIntValue());
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    return Optional.of(readFromRS(rs));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException | InterruptedException | ExecutionException throwables) {
            throw new CacheAccessException(throwables);
        }
    }

    @Override
    public void cacheNegative(String qName, QType qType, QClass qClass, RR<SOARData> soaRR, OffsetDateTime now) throws CacheAccessException {
        try(Histogram.Timer t = CacheMetrics.negativeCacheWriteTimeSeconds.startTimer();
            Connection cxn = pool.getConnection().get();
            PreparedStatement ps = cxn.prepareStatement("insert into cached_negative " +
                    "(id, " +
                    "qname, qtype, qclass," +
                    "r_name, r_class, r_ttl, r_mname, r_rname, r_serial, r_refresh, r_retry, r_expire, r_minimum," +
                    "created_at, expires_at) " +
                    "values (DEFAULT, " +
                    "?, ?, ?," +
                    "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
                    "?, ?)")) {
            ps.setString(1, qName);
            ps.setInt(2, qType.getIntValue());
            ps.setInt(3, qClass.getIntValue());
            writeIntoPS(soaRR, ps);
            ps.setObject(14, now);
            ps.setObject(15, now.plusSeconds(soaRR.getRData().getMinimum()));
            ps.executeUpdate();
            cachedNegativeRecordsTotal.inc();
        } catch (SQLException | InterruptedException | ExecutionException throwables) {
            throw new CacheAccessException(throwables);
        }
    }

    @Override
    public void bustCacheFor(List<String> qNames) throws CacheAccessException {
        Set<String> names = qNames.stream().flatMap(qn -> LabelCache.genSuffixes(qn).stream())
                .collect(Collectors.toSet());
        try(Connection c = pool.getConnection().get();
            PreparedStatement ps = c.prepareStatement("delete from cached_negative where qname=?")) {
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

    private static RR<SOARData> readFromRS(ResultSet rs) throws SQLException {
        return new RR<>(
                rs.getString("r_name"),
                KnownRRType.SOA,
                RRClass.match(rs.getInt("r_class")),
                rs.getInt("r_ttl"),
                new SOARData(
                        rs.getString("r_mname"),
                        rs.getString("r_rname"),
                        rs.getLong("r_serial"),
                        rs.getLong("r_refresh"),
                        rs.getLong("r_retry"),
                        rs.getLong("r_expire"),
                        rs.getLong("r_minimum")
                )
        );
    }

    private static void writeIntoPS(RR<SOARData> rr, PreparedStatement ps) throws SQLException {
        int n = 3;
        ps.setString(n+1, rr.getName());
        ps.setInt(n+2, rr.getRrClass().getIntValue());
        ps.setInt(n+3, rr.getTtl());
        ps.setString(n+4, rr.getRData().getMName());
        ps.setString(n+5, rr.getRData().getRName());
        ps.setLong(n+6, rr.getRData().getSerial());
        ps.setLong(n+7, rr.getRData().getRefresh());
        ps.setLong(n+8, rr.getRData().getRetry());
        ps.setLong(n+9, rr.getRData().getExpire());
        ps.setLong(n+10, rr.getRData().getMinimum());
    }
}
