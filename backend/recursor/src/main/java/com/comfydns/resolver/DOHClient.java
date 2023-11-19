package com.comfydns.resolver;

import com.comfydns.resolver.resolver.rfc1035.message.InvalidMessageException;
import com.comfydns.resolver.resolver.rfc1035.message.UnsupportedRRTypeException;
import com.comfydns.resolver.resolver.rfc1035.message.field.header.OpCode;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRClass;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Header;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.message.struct.MessageReadingException;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Question;
import okhttp3.*;

import java.io.IOException;
import java.util.Base64;

public class DOHClient {
    public static void main(String ... args) throws IOException, InvalidMessageException, UnsupportedRRTypeException, MessageReadingException {
        OkHttpClient client = new OkHttpClient();
        Message m = new Message();
        m.setHeader(new Header());
        m.getHeader().setQDCount(1);
        m.getQuestions().add(new Question(args[0], KnownRRType.A, KnownRRClass.IN));
        m.getHeader().setId(0);
        m.getHeader().setRD(true);
        m.getHeader().setOpCode(OpCode.QUERY);
        RequestBody body = RequestBody.create(Base64.getUrlEncoder().encodeToString(m.write()),
                MediaType.get("application/dns-message"));
        Request req = new Request.Builder()
                .url("http://localhost:8080/")
                .post(body)
                .build();

        Response resp = client.newCall(req).execute();
        Message dns = Message.read(Base64.getUrlDecoder().decode(resp.body().string()));

        System.out.println(dns);

    }
}
