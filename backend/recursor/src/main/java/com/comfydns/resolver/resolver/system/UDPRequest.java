package com.comfydns.resolver.resolver.system;

import com.comfydns.resolver.resolver.rfc1035.message.field.header.RCode;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Header;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.service.request.Request;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Optional;

public class UDPRequest extends Request {
    private static final Logger log = LoggerFactory.getLogger(UDPRequest.class);
    private final InetSocketAddress replyTo;
    private final Message m;
    private final ChannelHandlerContext ctx;

    public UDPRequest(InetSocketAddress replyTo, Message m, ChannelHandlerContext ctx) {
        this.replyTo = replyTo;
        this.m = m;
        this.ctx = ctx;
    }

    @Override
    public Message getMessage() {
        return m;
    }

    @Override
    protected void writeToTransport(Message m) {
        ByteBuf out = Unpooled.buffer();
        byte[] payload = m.write();
        if(payload.length > ((1 << 16) - 1)) {
            Header.setTruncated(payload);
            out.writeBytes(payload, 0, (1 << 16) - 1);
        } else {
            out.writeBytes(payload);
        }

        DatagramPacket packet = new DatagramPacket(out, replyTo);
        ctx.writeAndFlush(packet);
    }

    @Override
    public Optional<InetAddress> getRemoteAddress() {
        return Optional.of(replyTo.getAddress());
    }

    @Override
    protected String getRequestProtocolMetricsTag() {
        return "udp";
    }

    @Override
    public boolean transportIsTruncating() {
        return true;
    }
}
