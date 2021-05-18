package com.comfydns.resolver.util.http.router;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;

import java.util.Objects;
import java.util.function.Function;

public class Route {
    private final String path;
    private final HttpMethod method;
    private final Function<RoutedRequest, FullHttpResponse> consumer;

    public Route(String path, HttpMethod method, Function<RoutedRequest, FullHttpResponse> consumer) {
        path = path.strip();
        while(path.startsWith("/")) {
            path = path.substring(1);
        }
        while(path.endsWith("/")) {
            path = path.substring(0, path.length()-1);
        }

        this.path = path;
        this.method = method;
        this.consumer = consumer;
    }

    public String getPath() {
        return path;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public Function<RoutedRequest, FullHttpResponse> getFunction() {
        return consumer;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return path.equals(route.path) &&
                method.equals(route.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, method);
    }


    @Override
    public String toString() {
        return this.method.name() + " " + this.path;
    }
}
