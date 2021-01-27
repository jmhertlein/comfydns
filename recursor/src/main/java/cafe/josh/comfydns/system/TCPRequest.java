package cafe.josh.comfydns.system;

import cafe.josh.comfydns.butil.PrettyByte;
import cafe.josh.comfydns.rfc1035.message.struct.Message;
import cafe.josh.comfydns.rfc1035.service.request.Request;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class TCPRequest extends Request {
    private final Message m;
    private final ChannelHandlerContext ctx;

    public TCPRequest(Message m, ChannelHandlerContext ctx) {
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
        byte[] len = new byte[2];
        PrettyByte.writeNBitUnsignedInt(payload.length, 16, len, 0, 0);
        out.writeBytes(len);
        out.writeBytes(payload);
        ctx.writeAndFlush(out);
        ctx.close();
    }
}
