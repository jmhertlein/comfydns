package com.comfydns.resolver.resolver.rfc1035;

import com.comfydns.resolver.resolver.butil.PrettyByte;
import com.comfydns.resolver.resolver.internet.DNSRootZone;
import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolver.rfc1035.cache.RRCache;
import com.comfydns.resolver.resolver.rfc1035.cache.impl.InMemoryAuthorityRRSource;
import com.comfydns.resolver.resolver.rfc1035.cache.impl.InMemoryNegativeCache;
import com.comfydns.resolver.resolver.rfc1035.cache.impl.InMemoryDNSCache;
import com.comfydns.resolver.resolver.rfc1035.message.InvalidMessageException;
import com.comfydns.resolver.resolver.rfc1035.message.UnsupportedRRTypeException;
import com.comfydns.resolver.resolver.rfc1035.message.field.header.OpCode;
import com.comfydns.resolver.resolver.rfc1035.message.field.header.RCode;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRClass;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.UnknownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.ARData;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.NSRData;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.TXTRData;
import com.comfydns.resolver.resolver.rfc1035.message.struct.*;
import com.comfydns.resolver.resolver.rfc1035.service.RecursiveResolver;
import com.comfydns.resolver.resolver.rfc1035.service.request.LiveRequest;
import com.comfydns.resolver.resolver.rfc1035.service.transport.TCPSyncTransport;
import com.comfydns.resolver.resolver.rfc1035.service.transport.UDPSyncTransport;
import com.comfydns.resolver.resolver.trace.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ResolverIntegrationTest {
    @BeforeAll
    public static void filter() {
        Assumptions.assumeTrue(Objects.equals(System.getenv("COMFYDNS_BENCH"), "1"));
    }

    @AfterEach
    public void hangOn() throws InterruptedException {
        Thread.sleep(1000);
    }

    @Test
    public void simpleRootServerHit() throws IOException, InvalidMessageException, MessageReadingException {
        DNSRootZone.Server s = DNSRootZone.getRandomRootServer();
        try (DatagramSocket socket = new DatagramSocket(45221)) {
            Message m = new Message();
            Header h = new Header();
            h.setQDCount(1);
            h.setId(55214);
            h.setOpCode(OpCode.QUERY);
            h.setRD(false);
            h.setQR(false);
            m.setHeader(h);

            m.getQuestions().add(new Question("com", KnownRRType.A, KnownRRClass.IN));

            byte[] sendData = m.write();
            DatagramPacket send = new DatagramPacket(sendData, 0, sendData.length, s.getAddress(), 53);
            socket.send(send);
            System.out.println("Sent!!");
            byte[] data = new byte[512];
            DatagramPacket p = new DatagramPacket(data, 512);
            socket.receive(p);

            Message rcv = Message.read(p.getData());
            System.out.println(rcv);
        }
    }

    @Test
    public void testResolver() throws InterruptedException, ExecutionException {
        assertHasAnswer(testQuery(new Question("status.stripe.com", KnownRRType.A, KnownRRClass.IN)));
    }

    @Test
    public void testTCP() throws InterruptedException, ExecutionException, UnsupportedRRTypeException, InvalidMessageException, MessageReadingException {
        DNSRootZone.Server s = DNSRootZone.ROOT_SERVERS.get(1);

        Message m = new Message();
        Header h = new Header();
        h.setQDCount(1);
        h.setId(55214);
        h.setOpCode(OpCode.QUERY);
        h.setRD(false);
        h.setQR(false);
        m.setHeader(h);

        m.getQuestions().add(new Question("com", KnownRRType.A, KnownRRClass.IN));

        byte[] sendData = m.write();
        AsyncNonTruncatingTransport t = new AsyncNonTruncatingTransport();
        t.send(sendData, s.getAddress());

        Message rcv = Message.read(f.get());
        System.out.println(rcv);
    }

    @Test
    public void testCName() throws InterruptedException, ExecutionException {
        assertHasAnswer(testQuery(new Question("testcname.josh.cafe", KnownRRType.A, KnownRRClass.IN)));
    }

    @Test
    public void testNameError() throws ExecutionException, InterruptedException {
        assertNameError(testQuery(new Question("wakaka.hert", KnownRRType.A, KnownRRClass.IN)));
    }

    @Test
    public void testEffDotOrg() throws ExecutionException, InterruptedException {
        assertHasAnswer(testQuery(new Question("www.eff.org", KnownRRType.A, KnownRRClass.IN)));
    }

    @Test
    public void testGithubDotComAAAA() throws ExecutionException, InterruptedException {
        assertNoData(testQuery(new Question("github.com", KnownRRType.AAAA, KnownRRClass.IN)));
    }

    @Test
    public void testBingA() throws ExecutionException, InterruptedException {
        assertNoData(testQuery(new Question("bing", KnownRRType.A, KnownRRClass.IN)));
    }

    @Test
    @Disabled
    public void testPTRSearch() throws ExecutionException, InterruptedException {
        assertHasAnswer(testQuery(new Question("129.238.22.165.in-addr.arpa", KnownRRType.PTR, KnownRRClass.IN)));
    }

    @Test
    public void testArchNTPSearch() throws ExecutionException, InterruptedException {
        assertNoData(testQuery(new Question("0.arch.pool.ntp.org", KnownRRType.AAAA, KnownRRClass.IN)));
    }

    @Test
    public void testCaseSensitivity() throws ExecutionException, InterruptedException {
        Message m = testQuery(new Question("EU.bAttLe.NET", KnownRRType.A, KnownRRClass.IN));
        assertHasAnswer(m);
        Assertions.assertEquals("EU.bAttLe.NET", m.getAnswerRecords().get(0).getName());
    }


    // TODO: test idk if this should be name_error or no_error
    @Test
    public void testRipeHackathon() throws ExecutionException, InterruptedException {
        assertNoData(testQuery(new Question("ripe-hackathon6-ns.nlnetlabs.nl", KnownRRType.A, KnownRRClass.IN)));
    }

    /*
    java.lang.ArrayIndexOutOfBoundsException: arraycopy: last source index 1025 out of bounds for byte[1024]
	at java.base/java.lang.System.arraycopy(Native Method)
	at comfydns.resolver.rfc1035.message.field.rr.rdata.ARData.read(ARData.java:35)

	ROFL this crashes 8.8.8.8 too:

	dig @8.8.8.8 0822b46b4771b7f5-1612023248-54251-rslv.1500-plus0.pmtu4.rootcanary.net

; <<>> DiG 9.16.11 <<>> @8.8.8.8 0822b46b4771b7f5-1612023248-54251-rslv.1500-plus0.pmtu4.rootcanary.net
; (1 server found)
;; global options: +cmd
;; Got answer:
;; ->>HEADER<<- opcode: QUERY, status: SERVFAIL, id: 17272
;; flags: qr rd ra; QUERY: 1, ANSWER: 0, AUTHORITY: 0, ADDITIONAL: 1

;; OPT PSEUDOSECTION:
; EDNS: version: 0, flags:; udp: 512
;; QUESTION SECTION:
;0822b46b4771b7f5-1612023248-54251-rslv.1500-plus0.pmtu4.rootcanary.net.	IN A

;; Query time: 416 msec
;; SERVER: 8.8.8.8#53(8.8.8.8)
;; WHEN: Sat Jan 30 10:30:29 CST 2021
;; MSG SIZE  rcvd: 99

     */
    @Test
    @Disabled
    public void testWeirdArrayCopyIssue() throws ExecutionException, InterruptedException {
        assertNotServerFailure(testQuery(new Question("0822b46b4771b7f5-1612023248-54251-rslv.1500-plus0.pmtu4.rootcanary.net",
                KnownRRType.A, KnownRRClass.IN)));

    }

    @Test
    public void testFastlyInsights() throws ExecutionException, InterruptedException {
        assertNotServerFailure(testQuery(new Question("347bb12d-aabe-4c0a-b7a8-1b828b822904.us.u.fastly-insights.com",
                new UnknownRRType(PrettyByte.b(0, 0b01000001)), KnownRRClass.IN)));
    }

    //usllpic0zl4fj43u7ara62nff25shhlanuzebc4s8eab2a24be2fa831sac.d.aa.online-metrix.net
    @Test
    public void testOnlineMetrix() throws ExecutionException, InterruptedException {
        assertNotServerFailure(testQuery(new Question("usllpic0zl4fj43u7ara62nff25shhlanuzebc4s8eab2a24be2fa831sac.d.aa.online-metrix.net",
                new UnknownRRType(PrettyByte.b(0, 0b01000001)), KnownRRClass.IN)));
    }

    private static void assertHasAnswer(Message m) {
        Assertions.assertEquals(RCode.NO_ERROR, m.getHeader().getRCode());
        Assertions.assertTrue(m.getAnswerRecords().size() > 0);
    }

    private static void assertHasFormatError(Message m) {
        Assertions.assertEquals(RCode.FORMAT_ERROR, m.getHeader().getRCode());
    }

    private static void assertNameError(Message m) {
        Assertions.assertEquals(RCode.NAME_ERROR, m.getHeader().getRCode());
    }

    private static void assertNoData(Message m) {
        Assertions.assertEquals(RCode.NO_ERROR, m.getHeader().getRCode());
        Assertions.assertEquals(0, m.getHeader().getANCount());
        Assertions.assertEquals(0, m.getAuthorityRecords().stream()
                .filter(rr -> rr.getRrType() != KnownRRType.SOA)
                .count()
        );
    }

    private static void assertNotServerFailure(Message m) {
        Assertions.assertNotEquals(RCode.SERVER_FAILURE, m.getHeader().getRCode());
    }

    private static Message testQuery(Question test) throws ExecutionException, InterruptedException {
        return testQueries(test).get(0);
    }

    private static Message testQuery(Question test, RRCache cache) throws ExecutionException, InterruptedException {
        return testQueries(cache, test).get(0);
    }

    private static List<Message> testQueries(Question ... tests) throws ExecutionException, InterruptedException {
        return testQueries(new InMemoryDNSCache(), tests);
    }

    private static List<Message> testQueries(RRCache cache, Question ... tests) throws ExecutionException, InterruptedException {
        RecursiveResolver r = new RecursiveResolver(
                cache,
                new InMemoryAuthorityRRSource(),
                new InMemoryNegativeCache(), new UDPSyncTransport(),
                new TCPSyncTransport(),
                new HashSet<>());

        List<Message> ret = new ArrayList<>();
        for(Question test : tests) {
            LiveRequest req = new LiveRequest() {
                @Override
                public Message getMessage() {
                    Message ret = new Message();
                    Header h = new Header();
                    h.setQDCount(1);
                    h.setRD(true);
                    ret.getQuestions().add(test);
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


            Message message = r.resolve(() -> req);
            // call write just to exercise that code path
            message.write();
            System.out.println(message);
            ret.add(message);
        }

        return ret;
    }

    @Test
    public void testTraceQuery() throws ExecutionException, InterruptedException {
        ExecutorService stateMachinePool = Executors.newCachedThreadPool();
        Message m = new Message();
        Header h = new Header();
        h.setQDCount(1);
        h.setRD(true);
        m.getQuestions().add(new Question(
                "comfydns.com",
                KnownRRType.A,
                KnownRRClass.IN
        ));
        m.setHeader(h);
        TracingInternalRequest req = new TracingInternalRequest(m);
        try {
            RecursiveResolver r = new RecursiveResolver(
                    new InMemoryDNSCache(),
                    new InMemoryAuthorityRRSource(),
                    new InMemoryNegativeCache(), new UDPSyncTransport(),
                    new TCPSyncTransport(),
                    new HashSet<>());
            Message message = r.resolve(() -> req);

            // call write just to exercise that code path
            message.write();
            System.out.println(message);
        } finally {
            stateMachinePool.shutdown();
        }

        Tracer tracer = req.getTracer();
        Gson gson = (new GsonBuilder())
                .setPrettyPrinting()
                .registerTypeAdapter(Throwable.class, new ThrowableSerializer())
                .registerTypeAdapter(Message.class, new MessageCodec())
                .registerTypeAdapter(RR.class, new RRCodec())
                .registerTypeAdapter(Header.class, new HeaderCodec())
                .create();
        System.out.println(gson.toJson(tracer.getEntries()));
    }

    @Test
    @Disabled // this doesnt repro the issue
    public void testRecursiveInternalQueryWeirdness() throws ExecutionException, InterruptedException {
        // ns3.zdns.google
        // dns.google
        assertNotServerFailure(testQuery(new Question("ns3.zdns.google", KnownRRType.A, KnownRRClass.IN)));
    }

    /*
    54251-db1017c7f0754466.gioia.essedarius.net A IN
    ns5.gioia.essedarius.net
    ns4.infinita.verfwinkel.net
     */

    @Test
    @Disabled // they fixed their DNS lol
    public void testInfiniteRecursionFromGermany() throws ExecutionException, InterruptedException {
        assertNameError(testQuery(new Question("54251-db1017c7f0754466.gioia.essedarius.net", KnownRRType.A, KnownRRClass.IN)));
    }

    @Test
    public void testInitItunes() throws ExecutionException, InterruptedException {
        assertNotServerFailure(testQuery(new Question("init.itunes.apple.com",
                new UnknownRRType(PrettyByte.b(0, 0b01000001)), KnownRRClass.IN)));
    }

    private void assertServerFailure(Message testQuery) {
        Assertions.assertEquals(RCode.SERVER_FAILURE, testQuery.getHeader().getRCode());
    }

    @Test
    public void testVillagerDB() throws ExecutionException, InterruptedException {
        // [Q] [192.168.1.168]: QNAME: villagerdb.com, QTYPE: 0b00000000 0b01000001, QCLASS: IN
        assertNotServerFailure(testQuery(new Question("cdn2.signal.org",
                new UnknownRRType(PrettyByte.b(0, 0b01000001)), KnownRRClass.IN)));
    }

    @Test
    public void testAWSConsole() throws ExecutionException, InterruptedException {
        /*
        2021-03-03T12:47:52.536-0600 DEBUG HandleResponseToZoneQuery - [8000f4fd-7fcd-4fd9-a796-c0a0c2fc8390]: Message received: >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
ID: 35676, QR: response, OPCODE: QUERY, AA: true, TC: false, RD: false, RA: false, RCODE: NAME_ERROR
QDCOUNT: 1, ANCOUNT: 1, NSCOUNT: 1, ARCOUNT: 0
===========================================================
QNAME: us-east-1.console.aws.amazon.com, QTYPE: A, QCLASS: IN
===========================================================
----------------------------------------------------------
NAME: us-east-1.console.aws.amazon.com, TYPE: CNAME, CLASS: IN, TTL: 60, RDATA:
 CNAME='gr.console-geo.us-east-1.amazonaws.com'
===========================================================
----------------------------------------------------------
NAME: us-east-1.amazonaws.com, TYPE: SOA, CLASS: IN, TTL: 60, RDATA:
 MNAME: ns-923.amazon.com, RNAME: root.amazon.com, SERIAL: 74608495,
REFRESH: 1958748768, RETRY: 1071239168, EXPIRE: 921600, MINIMUM: 230400
===========================================================
<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
         */
        assertHasAnswer(testQuery(new Question("console.aws.amazon.com", KnownRRType.A, KnownRRClass.IN)));
    }



    /*
    ocsp.pki.goog A IN
     */

    @Test
    public void testOCSPPKIGoog() throws ExecutionException, InterruptedException {
        assertHasAnswer(testQuery(new Question("ocsp.pki.goog", KnownRRType.A, KnownRRClass.IN)));
    }

    @Test
    public void testSOAjoshCafe() throws ExecutionException, InterruptedException {
        assertHasAnswer(testQuery(new Question("josh.cafe", KnownRRType.SOA, KnownRRClass.IN)));
    }

    @Test
    public void testThrivent() throws ExecutionException, InterruptedException {
        assertHasAnswer(testQuery(new Question("service.thrivent.com", KnownRRType.A, KnownRRClass.IN)));
    }

    @Test
    public void testSSLGA() throws ExecutionException, InterruptedException {
        assertHasAnswer(testQuery(new Question("ssl.google-analytics.com", KnownRRType.A, KnownRRClass.IN)));
    }

    @Test
    public void testPotteryBarnKids() throws ExecutionException, InterruptedException {
        assertHasAnswer(testQuery(new Question("www.potterybarnkids.com", KnownRRType.A, KnownRRClass.IN)));
    }

    @Test
    public void testEmptyQName() throws ExecutionException, InterruptedException {
        assertHasFormatError(testQuery(new Question("", KnownRRType.A, KnownRRClass.IN)));
    }

    @Test
    public void testAssetsJishoOrg() throws ExecutionException, InterruptedException, UnknownHostException {
        Message resp = testQuery(new Question("assets.jisho.org", KnownRRType.A, KnownRRClass.IN));

        Optional<RR<?>> first = resp.getAnswerRecords().stream().filter(rr -> rr.getRrType().equals(KnownRRType.A))
                .findFirst();
        Assertions.assertTrue(first.isPresent());
        RR<?> aRR = first.get();
        Assertions.assertNotEquals(aRR.cast(ARData.class).getRData().getAddress(),
                Inet4Address.getByName("192.64.147.142"));
    }

    @Test
    public void testENT() throws ExecutionException, InterruptedException {
        Question question = new Question("test.comfydns.com", KnownRRType.A, KnownRRClass.IN);
        Message resp1, resp2;
        List<Message> resps = testQueries(question, question);
        resp1 = resps.get(0);
        resp2 = resps.get(1);

        Assertions.assertEquals(RCode.NO_ERROR, resp1.getHeader().getRCode());
        Assertions.assertEquals(0, resp1.getHeader().getANCount());
        Assertions.assertEquals(RCode.NO_ERROR, resp2.getHeader().getRCode());
        Assertions.assertEquals(0, resp2.getHeader().getANCount());
    }

    @Test
    public void testMinecraft() throws ExecutionException, InterruptedException, CacheAccessException {

        RRCache cache = new InMemoryDNSCache();
        // example query
        // https://gist.github.com/jmhertlein/a92d2df5d5fccc8a6a07992455b32761

        // when you query for p.r.m.c, you get initially told to ask azure DNS servers for m.n, but then
        // the azure servers tell you to ask AWS for r.m.n, and then while asking them,
        // you get a CNAME *and* NS records, where the NS records are telling you to ask AWS dns for
        // m.n, which does not server A records for www.m.n and m.n. So it breaks your cache.
        assertHasAnswer(testQuery(
                new Question("pocket.realms.minecraft.net", KnownRRType.A, KnownRRClass.IN),
                cache
        ));

        List<RR<?>> results = cache.search("minecraft.net", KnownRRType.NS, KnownRRClass.IN, OffsetDateTime.now());
        Set<String> uniqueDNameDomains = results.stream().map(rr -> rr.cast(NSRData.class))
                .map(rr -> rr.getRData().getNsDName())
                .map(dname -> {
                    String[] split = dname.split("\\.");
                    return split[split.length-2];
                })
                .collect(Collectors.toSet());

        // alternate idea: only accept nsdname records for 1-dot names from the gtld servers

        Assertions.assertEquals(1, uniqueDNameDomains.size());
        Assertions.assertEquals("azure-dns", uniqueDNameDomains.stream().findFirst().get());
    }

    @Test
    public void testIcloudMailQueries() throws ExecutionException, InterruptedException {
        assertHasAnswer(testQuery(new Question("200608._domainkey.email.schwab.com", KnownRRType.TXT, KnownRRClass.IN)));
        // k=rsa; p=MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDGoQCNwAQdJBy23MrShs1EuHqK/dtDC33QrTqgWd9CJmtM3CK2ZiTYugkhcxnkEtGbzg+IJqcDRNkZHyoRezTf6QbinBB2dbyANEuwKI5DVRBFowQOj9zvM3IvxAEboMlb0szUjAoML94HOkKuGuCkdZ1gbVEi3GcVwrIQphal1QIDAQAB
    }

    @Test
    public void testTXT() throws ExecutionException, InterruptedException {
        Message result = testQuery(new Question("txt.test.comfydns.com", KnownRRType.TXT, KnownRRClass.IN));
        assertHasAnswer(result);
        Assertions.assertEquals("hello, world!_-", result.getAnswerRecords().get(0).cast(TXTRData.class).getRData().getText());
    }
}
