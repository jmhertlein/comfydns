package cafe.josh.comfydns.system;

import cafe.josh.comfydns.rfc1035.message.struct.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

public class UDPDNSHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        byte[] content = msg.content().array();
        Message read = Message.read(content);

    }
}
