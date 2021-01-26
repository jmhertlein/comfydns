package cafe.josh.comfydns.system;

import cafe.josh.comfydns.rfc1035.message.struct.Message;
import cafe.josh.comfydns.rfc1035.service.RecursiveResolver;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

public class UDPDNSHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private final RecursiveResolver resolver;

    public UDPDNSHandler(RecursiveResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        byte[] content = new byte[msg.content().writerIndex() - msg.content().readerIndex()];
        msg.content().readBytes(content, 0, content.length);
        Message read = Message.read(content);
        resolver.resolve(new UDPRequest(read, ctx));
    }
}
