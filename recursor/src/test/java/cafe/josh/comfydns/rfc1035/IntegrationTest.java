package cafe.josh.comfydns.rfc1035;

import cafe.josh.comfydns.internet.DNSRootZone;
import cafe.josh.comfydns.rfc1035.cache.InMemoryDNSCache;
import cafe.josh.comfydns.rfc1035.cache.NegativeCache;
import cafe.josh.comfydns.rfc1035.message.InvalidMessageException;
import cafe.josh.comfydns.rfc1035.message.UnsupportedRRTypeException;
import cafe.josh.comfydns.rfc1035.message.field.header.OpCode;
import cafe.josh.comfydns.rfc1035.message.field.header.RCode;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRClass;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRType;
import cafe.josh.comfydns.rfc1035.message.struct.Header;
import cafe.josh.comfydns.rfc1035.message.struct.Message;
import cafe.josh.comfydns.rfc1035.message.struct.Question;
import cafe.josh.comfydns.rfc1035.service.RecursiveResolver;
import cafe.josh.comfydns.rfc1035.service.request.Request;
import cafe.josh.comfydns.rfc1035.service.transport.AsyncNonTruncatingTransport;
import cafe.josh.comfydns.rfc1035.service.transport.AsyncTruncatingTransport;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.*;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class IntegrationTest {
    @BeforeAll
    public static void filter() {
        Assumptions.assumeTrue(Objects.equals(System.getenv("COMFYDNS_BENCH"), "1"));
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
        assertHasAnswer(testQuery(new Question("ripe-hackathon6-ns.nlnetlabs.nl", KnownRRType.A, KnownRRClass.IN)));
    }

    /*
    java.lang.ArrayIndexOutOfBoundsException: arraycopy: last source index 1025 out of bounds for byte[1024]
	at java.base/java.lang.System.arraycopy(Native Method)
	at cafe.josh.comfydns.rfc1035.message.field.rr.rdata.ARData.read(ARData.java:35)

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
        };

        RecursiveResolver r = new RecursiveResolver(
                new InMemoryDNSCache(),
                new NegativeCache(), new AsyncTruncatingTransport(),
                new AsyncNonTruncatingTransport()
        );
        try {
            r.resolve(req);
            Message message = fM.get();
            System.out.println(message);
            return message;
        } catch (InterruptedException | ExecutionException e) {
            throw e;
        } finally {
            r.shutdown();
        }
    }

    @Test
    @Disabled // this doesnt repro the issue
    public void testRecursiveInternalQueryWeirdness() throws ExecutionException, InterruptedException {
        // ns3.zdns.google
        // dns.google
        assertNotServerFailure(testQuery(new Question("ns3.zdns.google", KnownRRType.A, KnownRRClass.IN)));
    }
}
