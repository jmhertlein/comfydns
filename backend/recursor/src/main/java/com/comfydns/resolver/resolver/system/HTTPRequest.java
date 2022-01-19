package com.comfydns.resolver.resolver.system;

import com.comfydns.resolver.resolver.butil.PrettyByte;
import com.comfydns.resolver.resolver.rfc1035.message.field.header.RCode;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.service.request.Request;
import com.comfydns.resolver.util.http.Responses;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

public class HTTPRequest extends Request {
    private static final Logger log = LoggerFactory.getLogger(HTTPRequest.class);
    private final Message m;
    private final ChannelHandlerContext ctx;

    public HTTPRequest(Message m, ChannelHandlerContext ctx) {
        this.m = m;
        this.ctx = ctx;
        requestsIn.labels("http").inc();
    }

    @Override
    public Message getMessage() {
        return m;
    }

    @Override
    public void answer(Message m) {
        FullHttpResponse r = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.copiedBuffer(Base64.getUrlEncoder().encode(m.write()))
        );
        r.headers().set("Content-Type", "application/dns-message; charset=UTF-8");
        ctx.writeAndFlush(r);
        ctx.close();
        this.recordAnswer(m, "http");
        if(m.getHeader().getRCode() == RCode.SERVER_FAILURE) {
            log.info("[R] [{}]: {} | {}", getRemoteAddress(), m.getHeader().getRCode(), id);
        }
    }

    @Override
    public Optional<InetAddress> getRemoteAddress() {
        return Optional.of(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress());
    }

    @Override
    public boolean transportIsTruncating() {
        return false;
    }
}
