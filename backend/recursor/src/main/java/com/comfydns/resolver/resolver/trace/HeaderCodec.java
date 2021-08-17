package com.comfydns.resolver.resolver.trace;

import com.comfydns.resolver.resolver.rfc1035.message.struct.Header;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class HeaderCodec implements JsonSerializer<Header> {
    @Override
    public JsonElement serialize(Header src, Type typeOfSrc, JsonSerializationContext context) {
        return serialize(src);
    }

    public static JsonObject serialize(Header src) {
        JsonObject ret = new JsonObject();
        ret.addProperty("id", src.getId());
        ret.addProperty("qr", src.getQR());
        ret.addProperty("opcode", src.getOpCode().getCode());
        ret.addProperty("aa", src.getAA());
        ret.addProperty("tc", src.getTC());
        ret.addProperty("rd", src.getRD());
        ret.addProperty("ra", src.getRA());
        ret.addProperty("rcode", src.getRCode().getCode());
        ret.addProperty("qdcount", src.getQDCount());
        ret.addProperty("ancount", src.getANCount());
        ret.addProperty("nscount", src.getNSCount());
        ret.addProperty("arcount", src.getARCount());
        return ret;
    }
}
