package com.comfydns.resolver.resolver.rfc1035.cache;

import com.comfydns.resolver.resolver.butil.PrettyByte;
import com.comfydns.resolver.resolver.rfc1035.cache.impl.DBAuthorityRRSource;
import com.comfydns.resolver.resolver.rfc1035.cache.impl.DBDNSCache;
import com.comfydns.resolver.resolver.rfc1035.message.field.query.QOnlyType;
import com.comfydns.resolver.resolver.rfc1035.message.field.query.QType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRClass;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.UnknownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.ARData;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.BlobRData;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.SOARData;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.WKSRData;
import com.comfydns.resolver.resolver.rfc1035.message.struct.RR;
import com.comfydns.util.db.SimpleConnectionPool;
import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import org.postgresql.ds.PGConnectionPoolDataSource;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;

public class DBIntegrationTest {
    private static volatile SimpleConnectionPool pool;
    private static final ReentrantLock lock = new ReentrantLock();

    @BeforeAll
    public static void filter() throws ClassNotFoundException, SQLException {
        Assumptions.assumeTrue(Objects.equals(System.getenv("COMFYDNS_INTEGRATION"), "1"));
        lock.lock();
        try {
            if(pool == null) {
                Class.forName("org.postgresql.Driver");
                PGConnectionPoolDataSource pgPool = new PGConnectionPoolDataSource();
                pgPool.setURL("jdbc:postgresql://" + "localhost" + "/");
                pgPool.setApplicationName("comfydns-recursor-integration-test");

                pgPool.setDatabaseName("comfydns_dev");
                pgPool.setUser("comfydns");
                pool = new SimpleConnectionPool(pgPool);
            }
        } finally {
            lock.unlock();
        }
    }

    @Test
    public void testCache() throws UnknownHostException, CacheAccessException {
        DBDNSCache c = new DBDNSCache(pool);
        OffsetDateTime now = OffsetDateTime.now();
        c.cache(new RR<>("josh.cafe", KnownRRType.A, KnownRRClass.IN, 60, new ARData((Inet4Address) Inet4Address.getByName("192.168.1.2"))),
                now);
    }

    @Test
    public void testCacheQuery() throws CacheAccessException, UnknownHostException {
        DBDNSCache c = new DBDNSCache(pool);
        OffsetDateTime now = OffsetDateTime.now();
        c.cache(new RR<>("josh.cafe", KnownRRType.A, KnownRRClass.IN, 60, new ARData((Inet4Address) Inet4Address.getByName("192.168.1.2"))),
                now);
        now = now.plusSeconds(1);
        List<RR<?>> result = c.search("josh.cafe", KnownRRType.A, KnownRRClass.IN, now);
        Assertions.assertEquals(1, result.size());

        result = c.search("josh.cafe", QOnlyType.STAR, KnownRRClass.IN, now);
        Assertions.assertEquals(1, result.size());

        c.cache(new RR<>("josh.cafe", KnownRRType.WKS, KnownRRClass.IN, 60, new WKSRData(
                (Inet4Address) Inet4Address.getByName("192.168.1.2"), 1, new byte[]{0, 53})),
                now);

        result = c.search("josh.cafe", QOnlyType.STAR, KnownRRClass.IN, now);
        Assertions.assertEquals(2, result.size());
    }

    @Test
    public void testBlobCacheQuery() throws CacheAccessException, UnknownHostException {
        UnknownRRType httpsType = new UnknownRRType(PrettyByte.b(0, 65));
        DBDNSCache c = new DBDNSCache(pool);
        OffsetDateTime now = OffsetDateTime.now();
        c.cache(new RR<>("josh.cafe",
                httpsType,
                KnownRRClass.IN, 60, new BlobRData(PrettyByte.b(1, 3, 3, 7))), now);
        now = now.plusSeconds(1);

        List<RR<?>> result = c.search("josh.cafe", httpsType, KnownRRClass.IN, now);
        Assertions.assertEquals(1, result.size());
    }

    @Test
    public void testExpunge() throws UnknownHostException, CacheAccessException {
        DBDNSCache c = new DBDNSCache(pool);
        OffsetDateTime now = OffsetDateTime.now();
        c.cache(new RR<>("josh.cafe", KnownRRType.A, KnownRRClass.IN, 60, new ARData((Inet4Address) Inet4Address.getByName("192.168.1.2"))),
                now);
        now = now.plusSeconds(1);
        List<RR<?>> result = c.search("josh.cafe", KnownRRType.A, KnownRRClass.IN, now);
        Assertions.assertEquals(1, result.size());

        c.expunge(result);
        result = c.search("josh.cafe", KnownRRType.A, KnownRRClass.IN, now);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    public void testExpiration() throws CacheAccessException, UnknownHostException {
        DBDNSCache c = new DBDNSCache(pool);
        OffsetDateTime now = OffsetDateTime.now();
        c.cache(new RR<>("josh.cafe", KnownRRType.A, KnownRRClass.IN, 60, new ARData((Inet4Address) Inet4Address.getByName("192.168.1.2"))),
                now);
        now = now.plusSeconds(1);
        List<RR<?>> result = c.search("josh.cafe", KnownRRType.A, KnownRRClass.IN, now);
        Assertions.assertEquals(1, result.size());

        now = now.plusSeconds(60);
        result = c.search("josh.cafe", KnownRRType.A, KnownRRClass.IN, now);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    public void testPrune() throws CacheAccessException, UnknownHostException, SQLException, ExecutionException, InterruptedException {
        DBDNSCache c = new DBDNSCache(pool);
        OffsetDateTime now = OffsetDateTime.now();
        c.cache(new RR<>("josh.cafe", KnownRRType.A, KnownRRClass.IN, 60, new ARData((Inet4Address) Inet4Address.getByName("192.168.1.2"))),
                now);
        now = now.plusSeconds(1);
        List<RR<?>> result = c.search("josh.cafe", KnownRRType.A, KnownRRClass.IN, now);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(59, result.get(0).getTtl());
        try(Connection cxn = pool.getConnection().get(); Statement s = cxn.createStatement()) {
            try(ResultSet rs = s.executeQuery("select count(*) as row_ct from cached_rr where name='josh.cafe' and rrtype=1 and" +
                    " rrclass=1")) {
                if(rs.next()) {
                    Assertions.assertEquals(1, rs.getInt("row_ct"));
                } else {
                    Assertions.fail("No rows found?");
                }
            }
        }

        now = now.plusSeconds(60);
        result = c.search("josh.cafe", KnownRRType.A, KnownRRClass.IN, now);
        Assertions.assertEquals(0, result.size());

        c.prune(now);

        try(Connection cxn = pool.getConnection().get(); Statement s = cxn.createStatement()) {
            try(ResultSet rs = s.executeQuery("select count(*) as row_ct from cached_rr where name='josh.cafe' and rrtype=1 and" +
                    " rrclass=1")) {
                if(rs.next()) {
                    Assertions.assertEquals(0, rs.getInt("row_ct"));
                } else {
                    Assertions.fail("No rows found?");
                }
            }
        }
    }

    @Test
    public void testCacheDupeQuery() throws CacheAccessException, UnknownHostException {
        DBDNSCache c = new DBDNSCache(pool);
        OffsetDateTime now = OffsetDateTime.now();
        c.cache(new RR<>("josh.cafe", KnownRRType.A, KnownRRClass.IN, 60, new ARData((Inet4Address) Inet4Address.getByName("192.168.1.2"))),
                now);
        now = now.plusSeconds(2);
        List<RR<?>> result = c.search("josh.cafe", KnownRRType.A, KnownRRClass.IN, now);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(58, result.get(0).getTtl());

        c.cache(new RR<>("josh.cafe", KnownRRType.A, KnownRRClass.IN, 60, new ARData((Inet4Address) Inet4Address.getByName("192.168.1.2"))),
                now);

        result = c.search("josh.cafe", KnownRRType.A, KnownRRClass.IN, now);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(60, result.get(0).getTtl());
    }

    @Test
    public void testAuthorityQuery() throws Exception {
        DBAuthorityRRSource s = new DBAuthorityRRSource(pool);
        OffsetDateTime now = OffsetDateTime.now();

        List<RR<?>> records = new ArrayList<>();
        records.add(new RR<>("josh.cafe", KnownRRType.SOA, KnownRRClass.IN, 600,
                new SOARData("ns1.josh.cafe",
                        "jmhertlein@gmail.com",
                        1, 60, 60, 60, 60)));
        records.add(new RR<>("host1.josh.cafe", KnownRRType.A, KnownRRClass.IN, 60,
                new ARData((Inet4Address) Inet4Address.getByName("192.168.1.2"))));

        try(Connection c = pool.getConnection().get();
        PreparedStatement ps = c.prepareStatement("insert into zone " +
                "(id, name, gen_ptrs, created_at, updated_at) " +
                "values (DEFAULT, ?, true, now(), now())")) {
            ps.setString(1, "josh.cafe");
            ps.executeUpdate();
        }


        try(Connection c = pool.getConnection().get();
                PreparedStatement ps = c.prepareStatement("insert into rr" +
                        "(id, name, rrtype, rrclass, ttl, rdata, zone_id, created_at, updated_at) " +
                        "values (DEFAULT, ?, ?, ?, ?, ?::jsonb, (select id from zone), now(), now()) ")) {
            for (RR<?> rr : records) {
                ps.setString(1, rr.getName());
                ps.setInt(2, rr.getRrType().getIntValue());
                ps.setInt(3, rr.getRrClass().getIntValue());
                ps.setInt(4, rr.getTtl());
                ps.setString(5, new Gson().toJson(rr.getRData().writeJson()));
                ps.addBatch();
            }
            ps.executeBatch();
        }

        try(Connection c = pool.getConnection().get();
            PreparedStatement ps = c.prepareStatement(
                    "update zone set soa_rr_id=(select id from rr where rrtype=?)")) {
            ps.setInt(1, KnownRRType.SOA.getIntValue());
            ps.executeUpdate();
        }

        List<RR<?>> search = s.search("host1.josh.cafe", KnownRRType.A, KnownRRClass.IN, now);
        Assertions.assertEquals(1, search.size());

        Assertions.assertTrue(s.getAuthoritativeForDomains().contains("josh.cafe"));
        Assertions.assertTrue(s.getNames().contains("josh.cafe"));
        Assertions.assertEquals(1, s.getSOAs().size());
        Assertions.assertTrue(s.isAuthoritativeFor("josh.cafe"));
        Assertions.assertFalse(s.isAuthoritativeFor("notjosh.cafe"));

        List<RR<?>> search1 = s.search("josh.cafe", QOnlyType.STAR, KnownRRClass.IN, now);
        Assertions.assertEquals(1, search1.size());
    }

    @AfterEach
    public void cleanup() throws SQLException, ExecutionException, InterruptedException {
        try(Connection c = pool.getConnection().get(); Statement s = c.createStatement()) {
            s.execute("delete from cached_rr");
            s.execute("delete from zone");
            s.execute("delete from rr");
        }
    }
}
