package com.comfydns.resolver.resolve.rfc1035.cache.impl;

import com.comfydns.resolver.resolve.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolve.rfc1035.cache.RRCache;
import com.comfydns.resolver.resolve.rfc1035.message.InvalidMessageException;
import com.comfydns.resolver.resolve.rfc1035.message.UnsupportedRRTypeException;
import com.comfydns.resolver.resolve.rfc1035.message.field.query.QClass;
import com.comfydns.resolver.resolve.rfc1035.message.field.query.QOnlyClass;
import com.comfydns.resolver.resolve.rfc1035.message.field.query.QOnlyType;
import com.comfydns.resolver.resolve.rfc1035.message.field.query.QType;
import com.comfydns.resolver.resolve.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolve.rfc1035.message.struct.RR;
import com.comfydns.resolver.resolve.rfc1035.service.search.SearchContext;
import com.google.gson.Gson;
import com.zaxxer.hikari.HikariDataSource;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DBDNSCache implements RRCache {
    private static final Logger log = LoggerFactory.getLogger(DBDNSCache.class);
    public static final String MB_TYPES = Stream.of(KnownRRType.MB, KnownRRType.MG, KnownRRType.MR)
            .map(t -> Integer.toString(t.getIntValue()))
            .collect(Collectors.joining(","));

    private final HikariDataSource pool;
    private final ThreadLocal<Gson> gson;

    public DBDNSCache(HikariDataSource pool) {
        this.pool = pool;
        this.gson = ThreadLocal.withInitial(Gson::new);

        CacheMetrics.currentCachedRecords.setChild(new Gauge.Child() {
            @Override
            public double get() {
                try(Connection c = pool.getConnection();
                PreparedStatement ps = c.prepareStatement("select count(*) as ct from cached_rr")) {
                    try(ResultSet rs = ps.executeQuery()) {
                        if(rs.next()) {
                            return rs.getInt("ct");
                        } else {
                            log.warn("Error counting cached_rr rows for metrics: no rows returned from query.");
                            return -1;
                        }
                    }
                } catch (SQLException throwables) {
                    log.warn("Error counting cached_rr rows for metrics: exception", throwables);
                    return -1;
                }
            }
        });
    }

    @Override
    public void prune(OffsetDateTime now) throws CacheAccessException {
        try(Connection c = pool.getConnection();
            PreparedStatement ps = c.prepareStatement("delete from cached_rr where expires_at <= ?")) {
            ps.setObject(1, now);
            int rows = ps.executeUpdate();
            CacheMetrics.cachedRecordsPrunedTotal.inc(rows);
            log.debug("Deleted {} cached rr's.", rows);
        } catch (SQLException throwables) {
            throw new CacheAccessException(throwables);
        }
    }

    @Override
    public void expunge(List<RR<?>> records) throws CacheAccessException {
        try(Connection c = pool.getConnection();
            PreparedStatement ps = c.prepareStatement(
                    "delete from cached_rr where name=? and rrtype=? and rrclass=? and rdata=?::jsonb")) {
            for (RR<?> r : records) {
                ps.setString(1, r.getName());
                ps.setInt(2, r.getRrType().getIntValue());
                ps.setInt(3, r.getRrClass().getIntValue());
                ps.setString(4, gson.get().toJson(r.getRData().writeJson()));
                ps.addBatch();
            }
            int rows = IntStream.of(ps.executeBatch()).sum();
            CacheMetrics.cachedRecordsPrunedTotal.inc(rows);
            log.debug("Expunged {} cached rr's.", rows);
        } catch (SQLException throwables) {
            throw new CacheAccessException(throwables);
        }
    }

    @Override
    public void cache(RR<?> record, OffsetDateTime now, SearchContext sCtx) throws CacheAccessException {
        try(
            Histogram.Timer ignored = CacheMetrics.cacheWriteTimeSeconds.startTimer();
            Connection c = pool.getConnection();
            PreparedStatement ps = c.prepareStatement("insert into cached_rr (id, name, rrtype, rrclass, ttl, rdata, created_at, expires_at, original_qname, original_query_id) values (DEFAULT, ?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?) on conflict on constraint cached_rr_name_rrtype_rrclass_rdata_key do update set ttl=excluded.ttl, created_at=excluded.created_at, expires_at=excluded.expires_at, original_qname=excluded.original_qname, original_query_id=excluded.original_query_id")
        ) {
            ps.setString(1, record.getName());
            ps.setInt(2, record.getRrType().getIntValue());
            ps.setInt(3, record.getRrClass().getIntValue());
            ps.setInt(4, record.getTtl());
            ps.setString(5, gson.get().toJson(record.getRData().writeJson()));
            ps.setObject(6, now);
            ps.setObject(7, now.plusSeconds(record.getTtl()));
            ps.setString(8, sCtx == null ? null : sCtx.getCurrentQuestion().getQName());
            ps.setObject(9, sCtx == null ? null : sCtx.getRequest().getId());
            int rows = ps.executeUpdate();
            log.debug("Inserted {} rows", rows);
            CacheMetrics.recordCache(record);
        } catch (SQLException throwables) {
            throw new CacheAccessException(throwables);
        }
    }

    @Override
    public List<RR<?>> search(String name, QType qType, QClass qClass, OffsetDateTime now) throws CacheAccessException {
        Histogram.Timer t = CacheMetrics.cacheReadTimeSeconds.startTimer();
        try {
            final String whereClause;
            final StatementSetupFunction setup;

            if (qType == QOnlyType.STAR) {
                if (qClass == QOnlyClass.STAR) {
                    whereClause = "";
                    setup = ps -> {
                    };
                } else {
                    whereClause = " and rrclass=?";
                    setup = ps -> ps.setInt(4, qClass.getIntValue());
                }
            } else if (qType == QOnlyType.MAILB) {
                if (qClass == QOnlyClass.STAR) {
                    whereClause = " and rrtype in (" + MB_TYPES + ")";
                    setup = ps -> {
                    };
                } else {
                    whereClause = " and rrtype in (" + MB_TYPES + ") and rrclass=?";
                    setup = ps -> ps.setInt(4, qClass.getIntValue());
                }
            } else {
                if (qClass == QOnlyClass.STAR) {
                    whereClause = " and rrtype=?";
                    setup = ps -> ps.setInt(4, qType.getIntValue());
                } else {
                    whereClause = " and rrtype=? and rrclass=?";
                    setup = ps -> {
                        ps.setInt(4, qType.getIntValue());
                        ps.setInt(5, qClass.getIntValue());
                    };
                }
            }

            final String sqlText = "select name, rrtype, rrclass, " +
                    "ttl - floor(extract(epoch from (?-created_at))) as ttl, " +
                    "rdata from cached_rr where name=? and ? < expires_at" + whereClause;
            List<RR<?>> ret = new ArrayList<>();
            try (Connection c = pool.getConnection();
                 PreparedStatement ps = c.prepareStatement(sqlText)) {
                ps.setObject(1, now);
                ps.setString(2, name);
                ps.setObject(3, now);
                setup.setup(ps);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ret.add(RR.read(rs));
                    }
                }
            } catch (SQLException | UnsupportedRRTypeException | InvalidMessageException throwables) {
                throw new CacheAccessException(throwables);
            }

            return ret;
        } finally {
            t.observeDuration();
        }
    }

    @Override
    public List<RR<?>> searchAAAAA(String name, QClass qClass, OffsetDateTime now) throws CacheAccessException {
        Histogram.Timer t = CacheMetrics.cacheReadTimeSeconds.startTimer();
        try {
            final String sqlText = String.format("select name, rrtype, rrclass, " +
                    "ttl - floor(extract(epoch from (?-created_at))) as ttl, " +
                    "rdata from cached_rr where name=? and ? < expires_at and rrtype in (%s, %s)",
                    KnownRRType.A.getIntValue(), KnownRRType.AAAA.getIntValue());
            List<RR<?>> ret = new ArrayList<>();
            try (Connection c = pool.getConnection();
                 PreparedStatement ps = c.prepareStatement(sqlText)) {
                ps.setObject(1, now);
                ps.setString(2, name);
                ps.setObject(3, now);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ret.add(RR.read(rs));
                    }
                }
            } catch (SQLException | UnsupportedRRTypeException | InvalidMessageException throwables) {
                throw new CacheAccessException(throwables);
            }

            return ret;
        } finally {
            t.observeDuration();
        }
    }
}
