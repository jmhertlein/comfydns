package cafe.josh.comfydns.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

public class DNSHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private final SimpleConnectionPool pool;

    public DNSHandler(SimpleConnectionPool pool) {
        this.pool = pool;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {

    }
}
