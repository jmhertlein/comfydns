package cafe.josh.comfydns.net;

import cafe.josh.comfydns.rfc1035.message.struct.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.util.concurrent.ExecutorService;

public class UDPDNSHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private final SimpleConnectionPool pool;


    public UDPDNSHandler(SimpleConnectionPool pool) {
        this.pool = pool;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        byte[] content = msg.content().array();
        Message read = Message.read(content);

    }
}
