package cafe.josh.comfydns.system;

import cafe.josh.comfydns.rfc1035.message.struct.Message;
import cafe.josh.comfydns.rfc1035.service.request.Request;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;

public class UDPRequest extends Request {
    private final InetSocketAddress replyTo;
    private final Message m;
    private final ChannelHandlerContext ctx;

    public UDPRequest(InetSocketAddress replyTo, Message m, ChannelHandlerContext ctx) {
        this.replyTo = replyTo;
        this.m = m;
        this.ctx = ctx;
        requestsIn.labels("udp").inc();
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
        DatagramPacket packet = new DatagramPacket(out, replyTo);
        ctx.writeAndFlush(packet);
        this.recordAnswer(m, "udp");
        //ctx.close(); // DO NOT REMOVE. CLOSING THIS CLOSES THE ENTIRE DANG UDP SOCKET.
    }
}
