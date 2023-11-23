package com.comfydns.resolver.resolver.rfc1035.service;

import com.comfydns.resolver.resolver.internet.DNSRootZone;
import com.comfydns.resolver.resolver.rfc1035.cache.impl.InMemoryAuthorityRRSource;
import com.comfydns.resolver.resolver.rfc1035.cache.impl.InMemoryNegativeCache;
import com.comfydns.resolver.resolver.rfc1035.cache.impl.InMemoryDNSCache;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRClass;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.ARData;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.NSRData;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Header;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Question;
import com.comfydns.resolver.resolver.rfc1035.message.struct.RR;
import com.comfydns.resolver.resolver.rfc1035.service.request.LiveRequest;
import com.comfydns.resolver.resolver.rfc1035.service.transport.TestTruncatingTransport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecursiveResolverTest {
    @Test
    public void test() throws UnknownHostException, ExecutionException, InterruptedException {
        Map<InetAddress, Message> responses = new HashMap<>();
        Message rootResponse = new Message();
        Header h = new Header();
        h.setNSCount(1);
        h.setARCount(1);
        h.setQR(true);
        rootResponse.setHeader(h);
        rootResponse.getAuthorityRecords().add(
                new RR<>("com", KnownRRType.NS, KnownRRClass.IN, 60 * 60, new NSRData("a.gtld.net"))
        );
        rootResponse.getAdditionalRecords().add(
                new RR<>("a.gtld.net", KnownRRType.A, KnownRRClass.IN, 60 * 60, new ARData((Inet4Address) InetAddress.getByName("192.168.1.23")))
        );

        DNSRootZone.ROOT_SERVERS.forEach(s -> {
            responses.put(s.getAddress(), rootResponse);
        });

        Message aGtldNetResponse = new Message();
        Header h2 = new Header();
        h2.setQR(true);
        h2.setNSCount(1);
        h2.setARCount(1);
        aGtldNetResponse.setHeader(h2);
        aGtldNetResponse.getAuthorityRecords().add(
                new RR<>("stripe.com", KnownRRType.NS, KnownRRClass.IN, 60 * 60, new NSRData("ns1.google.domains"))
        );
        aGtldNetResponse.getAdditionalRecords().add(
                new RR<>("ns1.google.domains", KnownRRType.A, KnownRRClass.IN, 60 * 60, new ARData((Inet4Address) InetAddress.getByName("192.168.1.24")))
        );
        responses.put(InetAddress.getByName("192.168.1.23"), aGtldNetResponse);

        Message ns1GoogleDomainsResponse = new Message();
        Header h3 = new Header();
        ns1GoogleDomainsResponse.setHeader(h3);
        h3.setANCount(1);
        h3.setQR(true);
        ns1GoogleDomainsResponse.getAnswerRecords().add(
                new RR<>("status.stripe.com", KnownRRType.A, KnownRRClass.IN, 60 * 60,
                        new ARData((Inet4Address) InetAddress.getByName("192.168.1.100")))
        );
        responses.put(InetAddress.getByName("192.168.1.24"), ns1GoogleDomainsResponse);

        LiveRequest req = new LiveRequest() {
            @Override
            public Message getMessage() {
                Message ret = new Message();
                Header h = new Header();
                h.setQDCount(1);
                h.setRD(true);
                ret.getQuestions().add(new Question("status.stripe.com", KnownRRType.A, KnownRRClass.IN));
                ret.setHeader(h);
                return ret;
            }

            @Override
            protected String getRequestProtocolMetricsTag() {
                return "test";
            }

            @Override
            public boolean transportIsTruncating() {
                return false;
            }
        };


        RecursiveResolver r = new RecursiveResolver(
                new InMemoryDNSCache(),
                new InMemoryAuthorityRRSource(),
                new InMemoryNegativeCache(), new TestTruncatingTransport(responses),
                null,
                new HashSet<>());
        Message message = r.resolve(() -> req);
        Assertions.assertEquals(1, message.getHeader().getANCount());
        Assertions.assertEquals("192.168.1.100",
                ((ARData) message.getAnswerRecords().get(0).getRData())
                        .getAddress().getHostAddress());

    }

    @Test
    public void testServerIsItsOwnNameserver() throws UnknownHostException {
        Map<InetAddress, Message> responses = new HashMap<>();
        Message rootResponse = new Message();
        Header h = new Header();
        h.setNSCount(1);
        h.setARCount(1);
        h.setQR(true);
        rootResponse.setHeader(h);
        rootResponse.getAuthorityRecords().add(
                new RR<>("google", KnownRRType.NS, KnownRRClass.IN, 60 * 60, new NSRData("a.gtld.net"))
        );
        rootResponse.getAdditionalRecords().add(
                new RR<>("a.gtld.net", KnownRRType.A, KnownRRClass.IN, 60 * 60, new ARData((Inet4Address) InetAddress.getByName("192.168.1.23")))
        );

        DNSRootZone.ROOT_SERVERS.forEach(s -> {
            responses.put(s.getAddress(), rootResponse);
        });

        Message aGtldNetResponse = new Message();
        Header h2 = new Header();
        h2.setQR(true);
        h2.setNSCount(1);
        h2.setARCount(1);
        aGtldNetResponse.setHeader(h2);
        aGtldNetResponse.getAuthorityRecords().add(
                new RR<>("dns.google", KnownRRType.NS, KnownRRClass.IN, 60 * 60, new NSRData("ns3.zdns.google"))
        );
        responses.put(InetAddress.getByName("192.168.1.23"), aGtldNetResponse);

        Message ns1GoogleDomainsResponse = new Message();
        Header h3 = new Header();
        ns1GoogleDomainsResponse.setHeader(h3);
        h3.setANCount(1);
        h3.setQR(true);
        ns1GoogleDomainsResponse.getAnswerRecords().add(
                new RR<>("status.stripe.com", KnownRRType.A, KnownRRClass.IN, 60 * 60,
                        new ARData((Inet4Address) InetAddress.getByName("192.168.1.100")))
        );
        responses.put(InetAddress.getByName("192.168.1.24"), ns1GoogleDomainsResponse);
    }
}
