package com.comfydns.resolver.util.http;

import com.google.gson.Gson;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.nio.charset.StandardCharsets;

public class Responses {
    private Responses() {}

    private static FullHttpResponse forStatus(HttpResponseStatus status, String body) {
        FullHttpResponse r = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
                Unpooled.copiedBuffer(body, StandardCharsets.UTF_8));
        r.headers().set("Content-Type", "text/plain; charset=UTF-8");
        return r;
    }

    public static FullHttpResponse notFound(String body) {
        return forStatus(HttpResponseStatus.NOT_FOUND, body);
    }

    public static FullHttpResponse forbidden(String body) {
        return forStatus(HttpResponseStatus.FORBIDDEN, body);
    }

    public static FullHttpResponse oops() {
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

    public static FullHttpResponse ok() {
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
    }

    public static FullHttpResponse ok(Object body) {
        Gson gson = new Gson();
        return forStatus(HttpResponseStatus.OK, gson.toJson(body));
    }

    public static FullHttpResponse tooBig(String body) {
        return forStatus(HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE, body);
    }

    public static FullHttpResponse bad(String body) {
        return forStatus(HttpResponseStatus.BAD_REQUEST, body);
    }
}
