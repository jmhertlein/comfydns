package com.comfydns.resolver.resolver.system;

import com.comfydns.resolver.resolver.butil.PrettyByte;
import com.comfydns.resolver.resolver.rfc1035.message.field.header.RCode;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.service.request.Request;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Optional;

public class TCPRequest extends Request {
    private static final Logger log = LoggerFactory.getLogger(TCPRequest.class);
    private final Message m;
    private final ChannelHandlerContext ctx;

    public TCPRequest(Message m, ChannelHandlerContext ctx) {
        this.m = m;
        this.ctx = ctx;
        requestsIn.labels("tcp").inc();
    }

    @Override
    public Message getMessage() {
        return m;
    }

    @Override
    public void answer(Message m) {
        ByteBuf out = Unpooled.buffer();
        byte[] payload = m.write();
        byte[] len = new byte[2];
        PrettyByte.writeNBitUnsignedInt(payload.length, 16, len, 0, 0);
        out.writeBytes(len);
        out.writeBytes(payload);
        ctx.writeAndFlush(out);
        ctx.close();
        this.recordAnswer(m, "tcp");
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
