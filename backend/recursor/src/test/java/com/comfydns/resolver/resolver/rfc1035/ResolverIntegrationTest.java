package com.comfydns.resolver.resolver.rfc1035;

import com.comfydns.resolver.resolver.butil.PrettyByte;
import com.comfydns.resolver.resolver.internet.DNSRootZone;
import com.comfydns.resolver.resolver.rfc1035.cache.impl.InMemoryNegativeCache;
import com.comfydns.resolver.resolver.rfc1035.cache.impl.InMemoryDNSCache;
import com.comfydns.resolver.resolver.rfc1035.message.InvalidMessageException;
import com.comfydns.resolver.resolver.rfc1035.message.UnsupportedRRTypeException;
import com.comfydns.resolver.resolver.rfc1035.message.field.header.OpCode;
import com.comfydns.resolver.resolver.rfc1035.message.field.header.RCode;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRClass;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.UnknownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Header;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Question;
import com.comfydns.resolver.resolver.rfc1035.message.struct.RR;
import com.comfydns.resolver.resolver.rfc1035.service.RecursiveResolver;
import com.comfydns.resolver.resolver.rfc1035.service.request.Request;
import com.comfydns.resolver.resolver.rfc1035.service.transport.AsyncNonTruncatingTransport;
import com.comfydns.resolver.resolver.rfc1035.service.transport.AsyncTruncatingTransport;
import com.comfydns.resolver.resolver.trace.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    public void simpleRootServerHit() throws IOException, UnsupportedRRTypeException, InvalidMessageException {
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
    public void testResolver() throws UnknownHostException, InterruptedException, ExecutionException {
        assertHasAnswer(testQuery(new Question("status.stripe.com", KnownRRType.A, KnownRRClass.IN)));
    }

    @Test
    public void testTCP() throws InterruptedException, ExecutionException, UnsupportedRRTypeException, InvalidMessageException {
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
        CompletableFuture<byte[]> f = new CompletableFuture<>();
        t.send(sendData, s.getAddress(), f::complete,
                f::completeExceptionally);

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
        assertNameError(testQuery(new Question("github.com", KnownRRType.AAAA, KnownRRClass.IN)));
    }

    @Test
    public void testBingA() throws ExecutionException, InterruptedException {
        assertNameError(testQuery(new Question("bing", KnownRRType.A, KnownRRClass.IN)));
    }

    @Test
    public void testPTRSearch() throws ExecutionException, InterruptedException {
        assertHasAnswer(testQuery(new Question("129.238.22.165.in-addr.arpa", KnownRRType.PTR, KnownRRClass.IN)));
    }

    @Test
    public void testArchNTPSearch() throws ExecutionException, InterruptedException {
        assertNameError(testQuery(new Question("0.arch.pool.ntp.org", KnownRRType.AAAA, KnownRRClass.IN)));
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
        assertNameError(testQuery(new Question("ripe-hackathon6-ns.nlnetlabs.nl", KnownRRType.A, KnownRRClass.IN)));
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

    private static void assertNameError(Message m) {
        Assertions.assertEquals(RCode.NAME_ERROR, m.getHeader().getRCode());
    }

    private static void assertNotServerFailure(Message m) {
        Assertions.assertNotEquals(RCode.SERVER_FAILURE, m.getHeader().getRCode());
    }

    private static Message testQuery(Question test) throws ExecutionException, InterruptedException {
        ExecutorService stateMachinePool = Executors.newCachedThreadPool();
        try {
            CompletableFuture<Message> fM = new CompletableFuture<>();
            Request req = new Request() {
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
                public void answer(Message m) {
                    fM.complete(m);
                }

                @Override
                public boolean transportIsTruncating() {
                    return false;
                }
            };

            RecursiveResolver r = new RecursiveResolver(
                    stateMachinePool, new InMemoryDNSCache(),
                    new InMemoryNegativeCache(), new AsyncTruncatingTransport(),
                    new AsyncNonTruncatingTransport(),
                    new HashSet<>());
            r.resolve(req);
            Message message = fM.get();
            // call write just to exercise that code path
            message.write();
            System.out.println(message);
            return message;
        } finally {
            stateMachinePool.shutdown();
        }
    }

    @Test
    public void testTraceQuery() throws ExecutionException, InterruptedException {
        ExecutorService stateMachinePool = Executors.newCachedThreadPool();
        CompletableFuture<Message> fM = new CompletableFuture<>();
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
        TracingInternalRequest req = new TracingInternalRequest(m, fM::complete);
        try {
            RecursiveResolver r = new RecursiveResolver(
                    stateMachinePool, new InMemoryDNSCache(),
                    new InMemoryNegativeCache(), new AsyncTruncatingTransport(),
                    new AsyncNonTruncatingTransport(),
                    new HashSet<>());
            r.resolve(req);
            Message message = fM.get();
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

}
