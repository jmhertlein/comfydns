package com.comfydns.resolver.resolver.rfc1035.cache.impl;

import com.comfydns.resolver.resolver.rfc1035.cache.AuthorityRRSource;
import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolver.rfc1035.message.InvalidMessageException;
import com.comfydns.resolver.resolver.rfc1035.message.UnsupportedRRTypeException;
import com.comfydns.resolver.resolver.rfc1035.message.field.query.QClass;
import com.comfydns.resolver.resolver.rfc1035.message.field.query.QOnlyClass;
import com.comfydns.resolver.resolver.rfc1035.message.field.query.QOnlyType;
import com.comfydns.resolver.resolver.rfc1035.message.field.query.QType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.SOARData;
import com.comfydns.resolver.resolver.rfc1035.message.struct.RR;
import com.comfydns.util.db.SimpleConnectionPool;
import io.prometheus.client.Histogram;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static com.comfydns.resolver.resolver.rfc1035.cache.impl.DBDNSCache.MB_TYPES;

public class DBAuthorityRRSource implements AuthorityRRSource {
    private final SimpleConnectionPool pool;

    public DBAuthorityRRSource(SimpleConnectionPool pool) {
        this.pool = pool;
    }

    @Override
    public boolean isAuthoritativeFor(String domain) throws CacheAccessException {
        try (Connection c = pool.getConnection().get();
                PreparedStatement ps = c.prepareStatement(
                "select name from zone where name=?")){
            ps.setString(1, domain);
            try(ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException | InterruptedException | ExecutionException e) {
            throw new CacheAccessException(e);
        }
    }

    @Override
    public Set<String> getAuthoritativeForDomains() throws CacheAccessException {
        Set<String> domains = new HashSet<>();
        try (Connection cxn = pool.getConnection().get();
                PreparedStatement ps = cxn.prepareStatement(
                "select name from zone"); ResultSet rs = ps.executeQuery()){
            while(rs.next()) {
                domains.add(rs.getString("name"));
            }
        } catch (SQLException | InterruptedException | ExecutionException e) {
            throw new CacheAccessException(e);
        }

        return domains;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<RR<SOARData>> getSOAs() throws CacheAccessException {
        List<RR<SOARData>> rrs = new ArrayList<>();
        try (Connection cxn = pool.getConnection().get();
                PreparedStatement ps = cxn.prepareStatement(
                "select * from rr where rrtype=?")){
            ps.setInt(1, KnownRRType.SOA.getIntValue());
            try(ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RR<?> rr = RR.read(rs);
                    if (rr.getRData() instanceof SOARData) {
                        rrs.add((RR<SOARData>) rr);
                    } else {
                        throw new RuntimeException("Despite querying for SOAs, we almost poisoned our heap because we got back something that didn't RR.read as an SOARData");
                    }
                }
            }
        } catch (SQLException | InterruptedException | ExecutionException | UnsupportedRRTypeException | InvalidMessageException e) {
            throw new CacheAccessException(e);
        }

        return rrs;
    }

    @Override
    public List<RR<?>> getZoneTransferPayload(String zoneName) throws CacheAccessException {
        List<RR<?>> ret = new ArrayList<>();
        try (Connection c = pool.getConnection().get();
             PreparedStatement ps = c.prepareStatement(
                     "select name, rrtype, rrclass, ttl, " +
                             "rdata from rr where zone_id=(select id from zone where name=?) " +
                             "and rrtype != ?")) {
            ps.setString(1, zoneName);
            ps.setInt(2, KnownRRType.SOA.getIntValue());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ret.add(RR.read(rs));
                }
            }
        } catch (SQLException | UnsupportedRRTypeException | InvalidMessageException | InterruptedException | ExecutionException throwables) {
            throw new CacheAccessException(throwables);
        }
        return ret;
    }

    @Override
    public List<String> getNames() throws CacheAccessException {
        return new ArrayList<>(getAuthoritativeForDomains());
    }



    @Override
    public List<RR<?>> search(String name, QType qType, QClass qClass, OffsetDateTime now) throws CacheAccessException {
        /*
         * I've copied this implementation from DBDNSCache because I'm not convinced
         * they'll always share an implementation, and I'm worried that whatever I might refactor out
         * right now will just be super tightly coupled to the other implementation.
         *
         * These tables are also going to be very different sizes, so
         * I might end up neeing to optimize the queries differently anyway.
         */
        Histogram.Timer t = CacheMetrics.authorityRRReadTimeSeconds.startTimer();
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
                    setup = ps -> ps.setInt(2, qClass.getIntValue());
                }
            } else if (qType == QOnlyType.MAILB) {
                if (qClass == QOnlyClass.STAR) {
                    whereClause = " and rrtype in (" + MB_TYPES + ")";
                    setup = ps -> {
                    };
                } else {
                    whereClause = " and rrtype in (" + MB_TYPES + ") and rrclass=?";
                    setup = ps -> ps.setInt(2, qClass.getIntValue());
                }
            } else {
                if (qClass == QOnlyClass.STAR) {
                    whereClause = " and rrtype=?";
                    setup = ps -> ps.setInt(2, qType.getIntValue());
                } else {
                    whereClause = " and rrtype=? and rrclass=?";
                    setup = ps -> {
                        ps.setInt(2, qType.getIntValue());
                        ps.setInt(3, qClass.getIntValue());
                    };
                }
            }

            final String sqlText = "select name, rrtype, rrclass, " +
                    "ttl, " +
                    "rdata from rr where name=?" + whereClause;
            List<RR<?>> ret = new ArrayList<>();
            try (Connection c = pool.getConnection().get();
                 PreparedStatement ps = c.prepareStatement(sqlText)) {
                ps.setString(1, name);
                setup.setup(ps);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ret.add(RR.read(rs));
                    }
                }
            } catch (SQLException | UnsupportedRRTypeException | InvalidMessageException | InterruptedException | ExecutionException throwables) {
                throw new CacheAccessException(throwables);
            }

            return ret;
        } finally {
            t.observeDuration();
        }
    }
}
