package com.comfydns.resolver.resolver.trace;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Type;

public class ThrowableSerializer implements JsonSerializer<Throwable> {
    @Override
    public JsonElement serialize(Throwable src, Type typeOfSrc, JsonSerializationContext context) {
        return serialize(src);
    }

    public static JsonObject serialize(Throwable t) {
        JsonObject ret = new JsonObject();
        ret.addProperty("exc_type", t.getClass().getName());
        ret.addProperty("message", t.getMessage());
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        t.printStackTrace(pw);
        pw.flush();
        out.flush();
        String stack = out.toString();
        ret.addProperty("stack", stack);
        return ret;
    }
}
