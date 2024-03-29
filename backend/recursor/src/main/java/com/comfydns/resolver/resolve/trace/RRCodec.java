package com.comfydns.resolver.resolve.trace;

import com.comfydns.resolver.resolve.rfc1035.message.struct.RR;
import com.google.gson.*;

import java.lang.reflect.Type;

public class RRCodec implements JsonSerializer<RR<?>> {
    @Override
    public JsonElement serialize(RR<?> src, Type typeOfSrc, JsonSerializationContext context) {
        return serialize(src);
    }

    public static JsonObject serialize(RR<?> src) {
        JsonObject ret = new JsonObject();
        ret.addProperty("name", src.getName());
        ret.addProperty("rrtype", src.getRrType().getIntValue());
        ret.addProperty("rrclass", src.getRrClass().getIntValue());
        ret.addProperty("ttl", src.getTtl());
        ret.add("rdata", src.getRData().writeJson());
        return ret;
    }
}
