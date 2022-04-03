package com.comfydns.resolver.resolver.system;

import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.service.RecursiveResolver;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.prometheus.client.Counter;

public class UDPDNSHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    protected static final Counter earlyNettyErrors = Counter.build()
            .name("early_netty_errors")
            .help("Total number of errors that happened so early in Netty handlers that we can't send a proper DNS error response.")
            .labelNames("protocol")
            .register();
    private final RecursiveResolver resolver;

    public UDPDNSHandler(RecursiveResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        byte[] content = new byte[msg.content().writerIndex() - msg.content().readerIndex()];
        msg.content().readBytes(content, 0, content.length);
        Message read = Message.read(content);
        UDPRequest req = new UDPRequest(msg.sender(), read, ctx);
        resolver.resolve(req);

        earlyNettyErrors.labels("udp").inc();
    }
}
