package com.comfydns.resolver;

import com.comfydns.resolver.resolver.rfc1035.message.field.header.RCode;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRClass;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.ARData;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.NSRData;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.TXTRData;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Header;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Question;
import com.comfydns.resolver.resolver.rfc1035.message.struct.RR;
import com.comfydns.resolver.resolver.trace.MessageCodec;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class MessageCodecTest {
    @Test
    public void test() throws UnknownHostException {
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

        JsonObject json = MessageCodec.serialize(m);
        Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
        String rendered = gson.toJson(json);

        String expected = "{\n" +
                "  \"header\": {\n" +
                "    \"id\": 3,\n" +
                "    \"qr\": true,\n" +
                "    \"opcode\": 0,\n" +
                "    \"aa\": false,\n" +
                "    \"tc\": false,\n" +
                "    \"rd\": true,\n" +
                "    \"ra\": true,\n" +
                "    \"rcode\": 0,\n" +
                "    \"qdcount\": 1,\n" +
                "    \"ancount\": 1,\n" +
                "    \"nscount\": 1,\n" +
                "    \"arcount\": 1\n" +
                "  },\n" +
                "  \"question\": [\n" +
                "    {\n" +
                "      \"qname\": \"comfydns.com\",\n" +
                "      \"qtype\": 1,\n" +
                "      \"qclass\": 1\n" +
                "    }\n" +
                "  ],\n" +
                "  \"answer\": [\n" +
                "    {\n" +
                "      \"name\": \"comfydns.com\",\n" +
                "      \"rrtype\": 1,\n" +
                "      \"rrclass\": 1,\n" +
                "      \"ttl\": 60,\n" +
                "      \"tdata\": {\n" +
                "        \"address\": \"192.168.1.2\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"authority\": [\n" +
                "    {\n" +
                "      \"name\": \"comfydns.com\",\n" +
                "      \"rrtype\": 2,\n" +
                "      \"rrclass\": 1,\n" +
                "      \"ttl\": 60,\n" +
                "      \"tdata\": {\n" +
                "        \"nsdname\": \"ns1.comfydns.com\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"additional\": [\n" +
                "    {\n" +
                "      \"name\": \"comfydns.com\",\n" +
                "      \"rrtype\": 16,\n" +
                "      \"rrclass\": 1,\n" +
                "      \"ttl\": 60,\n" +
                "      \"tdata\": {\n" +
                "        \"txt-data\": \"yeet\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        Assertions.assertEquals(expected, rendered);
    }
}
