package cafe.josh.comfydns.rfc1035;

import cafe.josh.comfydns.net.DNSRootZone;
import cafe.josh.comfydns.rfc1035.message.InvalidMessageException;
import cafe.josh.comfydns.rfc1035.message.LabelCache;
import cafe.josh.comfydns.rfc1035.message.UnsupportedRRTypeException;
import cafe.josh.comfydns.rfc1035.message.field.header.OpCode;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRClass;
import cafe.josh.comfydns.rfc1035.message.field.rr.KnownRRType;
import cafe.josh.comfydns.rfc1035.message.field.rr.RRType;
import cafe.josh.comfydns.rfc1035.message.struct.Header;
import cafe.josh.comfydns.rfc1035.message.struct.Message;
import cafe.josh.comfydns.rfc1035.message.struct.Question;
import io.netty.channel.unix.DatagramSocketAddress;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Objects;

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
}
