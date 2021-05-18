package com.comfydns.resolver.util.http.router;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class HttpRouter {
    private final RouteTree routeTree;

    public HttpRouter(RouteTree routeTree) {
        this.routeTree = routeTree;
    }

    public Optional<Routed> route(FullHttpRequest r) {
        URI uri = URI.create(r.uri());
        return this.routeTree.route(r.method(), uri.getPath());
    }

    public static class Builder {
        private final List<Route> routes;
        public Builder() {
            this.routes = new ArrayList<>();
        }

        public Builder get(String path, Function<RoutedRequest, FullHttpResponse> c) {
            this.routes.add(new Route(path, HttpMethod.GET, c));
            return this;
        }

        public Builder post(String path, Function<RoutedRequest, FullHttpResponse> c) {
            this.routes.add(new Route(path, HttpMethod.POST, c));
            return this;
        }

        public Builder put(String path, Function<RoutedRequest, FullHttpResponse> c) {
            this.routes.add(new Route(path, HttpMethod.PUT, c));
            return this;
        }

        public Builder delete(String path, Function<RoutedRequest, FullHttpResponse> c) {
            this.routes.add(new Route(path, HttpMethod.DELETE, c));
            return this;
        }

        public Builder patch(String path, Function<RoutedRequest, FullHttpResponse> c) {
            this.routes.add(new Route(path, HttpMethod.PATCH, c));
            return this;
        }

        public Builder connect(String path, Function<RoutedRequest, FullHttpResponse> c) {
            this.routes.add(new Route(path, HttpMethod.CONNECT, c));
            return this;
        }

        public Builder head(String path, Function<RoutedRequest, FullHttpResponse> c) {
            this.routes.add(new Route(path, HttpMethod.HEAD, c));
            return this;
        }

        public Builder options(String path, Function<RoutedRequest, FullHttpResponse> c) {
            this.routes.add(new Route(path, HttpMethod.OPTIONS, c));
            return this;
        }

        public Builder trace(String path, Function<RoutedRequest, FullHttpResponse> c) {
            this.routes.add(new Route(path, HttpMethod.TRACE, c));
            return this;
        }

        public HttpRouter build() {
            return new HttpRouter(new RouteTree(this.routes));
        }
    }
}
