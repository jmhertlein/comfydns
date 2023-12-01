package com.comfydns.resolver.resolve.rfc1035.cache;

import com.comfydns.resolver.resolve.rfc1035.cache.impl.InMemoryDNSCache;
import com.comfydns.resolver.resolve.rfc1035.message.field.rr.KnownRRClass;
import com.comfydns.resolver.resolve.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolve.rfc1035.message.field.rr.rdata.ARData;
import com.comfydns.resolver.resolve.rfc1035.message.struct.RR;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.time.OffsetDateTime;
import java.util.List;

public class InMemoryCacheTest {
    private static RR<ARData> A;
    @BeforeAll
    public static void setup() throws UnknownHostException {
        A = new RR<>("stripe.com", KnownRRType.A, KnownRRClass.IN, 60 * 60,
                new ARData((Inet4Address) Inet4Address.getByName("192.168.10.10")));
    }

    @Test
    public void test() throws CacheAccessException {
        InMemoryDNSCache c = new InMemoryDNSCache();
        Assertions.assertTrue(c.search("stripe.com", KnownRRType.A, KnownRRClass.IN, OffsetDateTime.now()).isEmpty());
        c.cache(A, OffsetDateTime.now());
        List<RR<?>> search = c.search("stripe.com", KnownRRType.A, KnownRRClass.IN, OffsetDateTime.now());
        Assertions.assertFalse(search.isEmpty());
        Assertions.assertEquals("192.168.10.10", ((ARData) search.get(0).getRData()).getAddress().getHostAddress());
    }

    @Test
    public void testExpiration() throws CacheAccessException {
        OffsetDateTime now = OffsetDateTime.now();
        InMemoryDNSCache c = new InMemoryDNSCache();
        c.cache(A, now);
        List<RR<?>> search = c.search(A.getName(), A.getRrType(), A.getRrClass(), now);
        Assertions.assertEquals(1, search.size());

        c.prune(now.plusSeconds(A.getTtl()));
        search = c.search(A.getName(), A.getRrType(), A.getRrClass(), now.plusSeconds(A.getTtl()));
        Assertions.assertEquals(0, search.size());
    }
}
