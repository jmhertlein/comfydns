package cafe.josh.comfydns.rfc1035.write;

import cafe.josh.comfydns.rfc1035.struct.Message;
import cafe.josh.comfydns.rfc1035.struct.Question;

import java.util.ArrayList;

/**
 * Takes a well-formed Message and prepares it to write to the wire.
 * Notably, takes care of compression and other concerns that depend on the
 * final bytes of the message.
 */
public class DNSMessageWriter {
    private DNSMessageWriter() {}
    public static byte[] write(Message m) {
        ArrayList<Byte> ret = new ArrayList<>();
        for (byte b : m.getHeader().getNetworkForm()) {
            ret.add(b);
        }

        if(m.getHeader().getQDCount() != m.getQuestions().size()) {
            throw new IllegalArgumentException("Header QDCOUNT (" + m.getHeader().getQDCount() + ") =/= questions length (" + m.getQuestions().size() + ")");
        }

        for(Question q : m.getQuestions()) {

        }
        return null;
    }
}
