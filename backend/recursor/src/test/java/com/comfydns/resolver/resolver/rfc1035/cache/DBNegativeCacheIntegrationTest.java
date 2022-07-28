package com.comfydns.resolver.resolver.rfc1035.cache;

import com.comfydns.resolver.resolver.butil.PrettyByte;
import com.comfydns.resolver.resolver.rfc1035.cache.impl.DBNegativeCache;
import com.comfydns.resolver.resolver.rfc1035.message.field.header.RCode;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRClass;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.UnknownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.SOARData;
import com.comfydns.resolver.resolver.rfc1035.message.struct.RR;
import com.comfydns.util.db.SimpleConnectionPool;
import org.junit.jupiter.api.*;
import org.postgresql.ds.PGConnectionPoolDataSource;

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

public class DBNegativeCacheIntegrationTest {
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

    private static RR<SOARData> mkSOA(String nsdname) {
        return new RR<>(nsdname, KnownRRType.SOA, KnownRRClass.IN, 60,
                new SOARData("ns1." + nsdname, "admin@josh.cafe", 1, 60, 60, 60, 60));
    }

    @Test
    public void testCache() throws UnknownHostException, CacheAccessException {
        OffsetDateTime now = OffsetDateTime.now();
        DBNegativeCache c = new DBNegativeCache(pool);
        c.cacheNegative("sirnotappearinginthisfilm.josh.cafe", KnownRRType.A, KnownRRClass.IN,
                RCode.NAME_ERROR, mkSOA("josh.cafe"), now);
    }

    @Test
    public void testCacheQuery() throws CacheAccessException, UnknownHostException {
        DBNegativeCache c = new DBNegativeCache(pool);
        OffsetDateTime now = OffsetDateTime.now();
        c.cacheNegative("sirnotappearinginthisfilm.josh.cafe", KnownRRType.A, KnownRRClass.IN,
                RCode.NAME_ERROR, mkSOA("josh.cafe"), now);
        now = now.plusSeconds(1);
        Assertions.assertTrue(c.cachedNegative("sirnotappearinginthisfilm.josh.cafe", KnownRRType.A, KnownRRClass.IN, now).isPresent());
    }

    @Test
    public void testUnknownQTypeCacheQuery() throws CacheAccessException, UnknownHostException {
        UnknownRRType httpsType = new UnknownRRType(PrettyByte.b(0, 65));
        DBNegativeCache c = new DBNegativeCache(pool);
        OffsetDateTime now = OffsetDateTime.now();
        c.cacheNegative("sirnotappearinginthisfilm.josh.cafe", httpsType, KnownRRClass.IN,
                RCode.NAME_ERROR, mkSOA("josh.cafe"), now);
        now = now.plusSeconds(1);

        Assertions.assertTrue(c.cachedNegative("sirnotappearinginthisfilm.josh.cafe", httpsType, KnownRRClass.IN, now).isPresent());
    }

    @Test
    public void testExpunge() throws UnknownHostException, CacheAccessException {
        DBNegativeCache c = new DBNegativeCache(pool);
        OffsetDateTime now = OffsetDateTime.now();
        c.cacheNegative("sirnotappearinginthisfilm.josh.cafe", KnownRRType.A, KnownRRClass.IN,
                RCode.NAME_ERROR, mkSOA("josh.cafe"), now);
        now = now.plusSeconds(1);
        c.bustCacheFor(List.of("sirnotappearinginthisfilm.josh.cafe"));
        Assertions.assertFalse(c.cachedNegative("sirnotappearinginthisfilm.josh.cafe", KnownRRType.A, KnownRRClass.IN, now).isPresent());
    }

    @Test
    public void testExpiration() throws CacheAccessException {
        DBNegativeCache c = new DBNegativeCache(pool);
        OffsetDateTime now = OffsetDateTime.now();
        c.cacheNegative("sirnotappearinginthisfilm.josh.cafe", KnownRRType.A, KnownRRClass.IN, RCode.NAME_ERROR, mkSOA("josh.cafe"), now);
        now = now.plusSeconds(61);
        Assertions.assertFalse(c.cachedNegative("sirnotappearinginthisfilm.josh.cafe", KnownRRType.A, KnownRRClass.IN, now).isPresent());
    }

    @Test
    public void testPrune() throws CacheAccessException, UnknownHostException, SQLException, ExecutionException, InterruptedException {
        OffsetDateTime now = OffsetDateTime.now();
        DBNegativeCache c = new DBNegativeCache(pool);
        c.cacheNegative("josh.cafe", KnownRRType.A, KnownRRClass.IN, RCode.NAME_ERROR, mkSOA("josh.cafe"), now);
        now = now.plusSeconds(61);
        c.prune(now);

        try(Connection cxn = pool.getConnection().get(); Statement s = cxn.createStatement()) {
            try(ResultSet rs = s.executeQuery("select count(*) as row_ct from cached_negative where qname='josh.cafe' and qtype=1 and" +
                    " qclass=1")) {
                if(rs.next()) {
                    Assertions.assertEquals(0, rs.getInt("row_ct"));
                } else {
                    Assertions.fail("No rows found?");
                }
            }
        }

    }

    @AfterEach
    public void cleanup() throws SQLException, ExecutionException, InterruptedException {
        try(Connection c = pool.getConnection().get(); Statement s = c.createStatement()) {
            s.execute("delete from cached_negative");
        }
    }
}
