package com.comfydns.resolver.resolver.rfc1035.cache;

import com.comfydns.resolver.resolver.butil.PrettyByte;
import com.comfydns.resolver.resolver.rfc1035.cache.impl.DBDNSCache;
import com.comfydns.resolver.resolver.rfc1035.message.field.query.QOnlyType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRClass;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.UnknownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.ARData;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.BlobRData;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.WKSRData;
import com.comfydns.resolver.resolver.rfc1035.message.struct.RR;
import com.comfydns.util.db.SimpleConnectionPool;
import org.junit.jupiter.api.*;
import org.postgresql.ds.PGConnectionPoolDataSource;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;

public class DBDNSCacheIntegrationTest {
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

    @AfterEach
    public void cleanup() throws SQLException, ExecutionException, InterruptedException {
        try(Connection c = pool.getConnection().get(); Statement s = c.createStatement()) {
            s.execute("delete from cached_rr");
        }
    }
}
