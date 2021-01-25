package cafe.josh.comfydns.rfc1035;

import cafe.josh.comfydns.net.DNSRootZone;
import cafe.josh.comfydns.rfc1035.cache.InMemoryDNSCache;
import cafe.josh.comfydns.rfc1035.message.InvalidMessageException;
import cafe.josh.comfydns.rfc1035.message.LabelCache;
import cafe.josh.comfydns.rfc1035.message.UnsupportedRRTypeException;
import cafe.josh.comfydns.rfc1035.message.field.header.OpCode;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRClass;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRType;
import cafe.josh.comfydns.rfc1035.message.field.rr.RRType;
import cafe.josh.comfydns.rfc1035.message.field.rr.rdata.ARData;
import cafe.josh.comfydns.rfc1035.message.field.rr.rdata.NSRData;
import cafe.josh.comfydns.rfc1035.message.struct.Header;
import cafe.josh.comfydns.rfc1035.message.struct.Message;
import cafe.josh.comfydns.rfc1035.message.struct.Question;
import cafe.josh.comfydns.rfc1035.message.struct.RR;
import cafe.josh.comfydns.rfc1035.service.RecursiveResolver;
import cafe.josh.comfydns.rfc1035.service.Request;
import cafe.josh.comfydns.rfc1035.service.transport.AsyncNonTruncatingTransport;
import cafe.josh.comfydns.rfc1035.service.transport.AsyncTruncatingTransport;
import cafe.josh.comfydns.rfc1035.service.transport.TestTruncatingTransport;
import io.netty.channel.unix.DatagramSocketAddress;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
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
        CompletableFuture<Message> fM = new CompletableFuture<>();
        Request req = new Request() {
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
            public void answer(Message m) {
                fM.complete(m);
            }
        };

        RecursiveResolver r = new RecursiveResolver(
                new InMemoryDNSCache(),
                new AsyncTruncatingTransport(),
                new AsyncNonTruncatingTransport()
        );
        try {
            r.resolve(req);
            Message message = fM.get();
            System.out.println(message);
            Assertions.assertTrue(message.getHeader().getANCount() > 0);
        } finally {
            r.shutdown();
        }
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
}
