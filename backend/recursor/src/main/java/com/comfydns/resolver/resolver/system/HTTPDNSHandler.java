package com.comfydns.resolver.resolver.system;

import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.service.RecursiveResolver;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class HTTPDNSHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger log = LoggerFactory.getLogger(HTTPDNSHandler.class);
    private final RecursiveResolver resolver;

    public HTTPDNSHandler(RecursiveResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Connection from {}", ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.debug("Exception caught for client " + ctx.channel().remoteAddress(), cause);
        ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR));
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // TODO maybe cancel the request if the transport disconnects?
        log.debug("Connection close: {}", ctx.channel().remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        Message m;
        if(msg.method() == HttpMethod.POST) {
            String content = msg.content().toString(StandardCharsets.UTF_8);
            byte[] decoded = Base64.getUrlDecoder().decode(content);
            m = Message.read(decoded);
        } else if(msg.method() == HttpMethod.GET) {
            QueryStringDecoder decoder = new QueryStringDecoder(msg.uri());
            Map<String, List<String>> params = decoder.parameters();
            if(!params.containsKey("dns")) {
                log.debug("No parameter key: dns");
                ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
                ctx.close();
                return;
            }

            List<String> dns = params.get("dns");
            if(dns.size() != 1) {
                log.debug("Too many dns parameter keys");
                ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
                ctx.close();
                return;
            }

            String payload = dns.get(0);
            byte[] decoded = Base64.getUrlDecoder().decode(payload);
            m = Message.read(decoded);
        } else {
            log.debug("unsupported http method: {}", msg.method());
            ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            ctx.close();
            return;
        }
        resolver.resolve(new HTTPRequest(m, ctx));
    }
}
