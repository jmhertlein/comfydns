package com.comfydns.resolver.resolver.system;

import com.comfydns.resolver.resolver.butil.PrettyByte;
import com.comfydns.resolver.resolver.rfc1035.message.struct.Message;
import com.comfydns.resolver.resolver.rfc1035.service.RecursiveResolver;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TCPDNSHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static final Logger log = LoggerFactory.getLogger(TCPDNSHandler.class);
    private final ByteBuf acculmulated;
    private final RecursiveResolver resolver;
    private Integer msgLen;

    public TCPDNSHandler(RecursiveResolver resolver) {
        this.resolver = resolver;
        acculmulated = Unpooled.buffer();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        acculmulated.writeBytes(msg);
        if(acculmulated.writerIndex() > 1 && msgLen == null) {
            byte[] len = new byte[2];
            acculmulated.readBytes(len);
            msgLen = (int) PrettyByte.readNBitUnsignedInt(16, len, 0, 0);
        }

        if(msgLen != null) {
            if(acculmulated.writerIndex() >= msgLen) {
                byte[] content = new byte[msgLen];
                acculmulated.readBytes(content, 0, msgLen);
                Message m = Message.read(content);
                resolver.resolve(new TCPRequest(m, ctx));
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Connection from {}", ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.debug("Exception caught for client " + ctx.channel().remoteAddress(), cause);
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // TODO maybe cancel the request if the transport disconnects?
        log.debug("Connection close: {}", ctx.channel().remoteAddress());
    }
}
