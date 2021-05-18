package com.comfydns.resolver.util.http.router;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.netty.handler.codec.http.FullHttpRequest;

import java.nio.charset.StandardCharsets;

public class RoutedRequest {
    private final Routed routed;
    private final FullHttpRequest request;

    public RoutedRequest(Routed routed, FullHttpRequest request) {
        this.routed = routed;
        this.request = request;
    }

    public Routed getRouted() {
        return routed;
    }

    public FullHttpRequest getRequest() {
        return request;
    }

    public JsonObject body() {
        return body(JsonObject.class);
    }

    public boolean sizeUnder(int bytes) {
        return this.request.content().readableBytes() < bytes;
    }

    public <T> T body(Class<T> type) {
        return new Gson().fromJson(
                rawBody(),
                type);
    }

    public String rawBody() {
        return this.request.content().toString(StandardCharsets.UTF_8);
    }

    public String getPathVar(String name) {
        return getRouted().getVariables().get(name);
    }
}
