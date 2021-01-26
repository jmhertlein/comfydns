package cafe.josh.comfydns.system;

import cafe.josh.comfydns.butil.PrettyByte;
import cafe.josh.comfydns.rfc1035.message.struct.Message;
import cafe.josh.comfydns.rfc1035.service.request.Request;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;

public class UDPRequest implements Request {
    private final Message m;
    private final ChannelHandlerContext ctx;

    public UDPRequest(Message m, ChannelHandlerContext ctx) {
        this.m = m;
        this.ctx = ctx;
    }

    @Override
    public Message getMessage() {
        return m;
    }

    @Override
    public void answer(Message m) {
        ByteBuf out = Unpooled.buffer();
        byte[] payload = m.write();
        out.writeBytes(payload);
        DatagramPacket packet = new DatagramPacket(out, (InetSocketAddress) ctx.channel().remoteAddress());
        ctx.writeAndFlush(packet);
        ctx.close();
    }
}
