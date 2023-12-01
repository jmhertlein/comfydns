package com.comfydns.resolver;

import com.comfydns.resolver.resolve.rfc1035.message.field.header.RCode;
import com.comfydns.resolver.resolve.rfc1035.message.field.rr.KnownRRClass;
import com.comfydns.resolver.resolve.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolve.rfc1035.message.field.rr.rdata.ARData;
import com.comfydns.resolver.resolve.rfc1035.message.field.rr.rdata.NSRData;
import com.comfydns.resolver.resolve.rfc1035.message.field.rr.rdata.TXTRData;
import com.comfydns.resolver.resolve.rfc1035.message.struct.Header;
import com.comfydns.resolver.resolve.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolve.rfc1035.message.struct.Question;
import com.comfydns.resolver.resolve.rfc1035.message.struct.RR;
import com.comfydns.resolver.resolve.trace.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class TraceEntryTest {
    @Test
    public void test() throws UnknownHostException {
        Gson gson = (new GsonBuilder())
                .setPrettyPrinting()
                .registerTypeAdapter(Throwable.class, new ThrowableSerializer())
                .registerTypeAdapter(Message.class, new MessageCodec())
                .registerTypeAdapter(RR.class, new RRCodec())
                .registerTypeAdapter(Header.class, new HeaderCodec())
                .create();

        TraceEntry.UpstreamQueryResultEntry entry = new TraceEntry.UpstreamQueryResultEntry(0,
                new IllegalStateException("Memes not dank enough"),
                "ns1.comfydns.com"
        );

        TraceEntry.UpstreamQueryResultEntry entry2 = new TraceEntry.UpstreamQueryResultEntry(0,
                m(), "ns1.comfydns.com"
        );
    }

    private static Message m() throws UnknownHostException {
        Message m = new Message();
        Header header = new Header();
        m.setHeader(header);
        header.setQDCount(1);
        header.setANCount(1);
        header.setNSCount(1);
        header.setARCount(1);
        //header.setId(5);
        header.setId(3);
        header.setQR(true);
        header.setRCode(RCode.NO_ERROR);
        header.setRA(true);
        header.setRD(true);

        m.getQuestions().add(new Question("comfydns.com", KnownRRType.A, KnownRRClass.IN));
        m.getAnswerRecords().add(new RR<>("comfydns.com", KnownRRType.A, KnownRRClass.IN, 60,
                new ARData((Inet4Address) Inet4Address.getByName("192.168.1.2"))));
        m.getAuthorityRecords().add(new RR<>("comfydns.com", KnownRRType.NS, KnownRRClass.IN, 60,
                new NSRData("ns1.comfydns.com")));
        m.getAdditionalRecords().add(new RR<>("comfydns.com", KnownRRType.TXT, KnownRRClass.IN, 60,
                new TXTRData("yeet")));

        return m;
    }
}
