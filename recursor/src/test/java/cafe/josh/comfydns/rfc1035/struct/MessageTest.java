package cafe.josh.comfydns.rfc1035.struct;

import cafe.josh.comfydns.PrettyByte;
import cafe.josh.comfydns.rfc1035.LabelCache;
import cafe.josh.comfydns.rfc1035.field.header.OpCode;
import cafe.josh.comfydns.rfc1035.field.header.RCode;
import cafe.josh.comfydns.rfc1035.field.query.QType;
import cafe.josh.comfydns.rfc1035.field.rr.RRClass;
import cafe.josh.comfydns.rfc1035.field.rr.RRType;
import cafe.josh.comfydns.rfc1035.field.rr.rdata.ARData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Arrays;

public class MessageTest {
    @Test
    public void testSimple() throws UnknownHostException {
        byte[] expected = PrettyByte.b(
        0x42, 0x5c, 0x81, 0x80, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x04, 0x61, 0x6a, 0x61,
                0x78, 0x0a, 0x67, 0x6f, 0x6f, 0x67, 0x6c, 0x65, 0x61, 0x70, 0x69, 0x73, 0x03, 0x63, 0x6f, 0x6d,
                0x00, 0x00, 0x01, 0x00, 0x01, 0xc0, 0x0c, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0x00, 0x3d, 0x00,
                0x04, 0xac, 0xd9, 0x04, 0x6a);

        Header h = new Header();
        h.setId(0x425c);
        h.setQR(true);
        h.setOpCode(OpCode.QUERY);
        h.setRD(true);
        h.setRA(true);
        h.setRCode(RCode.NO_ERROR);

        h.setQDCount(1);
        h.setANCount(1);

        Question q = new Question("ajax.googleapis.com", RRType.A, RRClass.IN);
        ARData rd = new ARData((Inet4Address) Inet4Address.getByAddress(new byte[]{(byte) 172, (byte) 217, (byte) 4, (byte) 106}));
        RR<ARData> answer = new RR<>("ajax.googleapis.com", RRType.A, RRClass.IN, 61, rd);
        Message m = new Message();
        m.setHeader(h);
        m.getQuestions().add(q);
        m.getAnswerRecords().add(answer);

        byte[] found = m.write(new LabelCache());

        if(!Arrays.equals(expected, found)) {
            System.out.println(PrettyByte.toComparison(expected, found));
        }


        Assertions.assertArrayEquals(expected, found);

    }
}
