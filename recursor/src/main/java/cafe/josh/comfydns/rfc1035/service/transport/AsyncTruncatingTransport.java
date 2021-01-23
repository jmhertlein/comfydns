package cafe.josh.comfydns.rfc1035.service.transport;

import cafe.josh.comfydns.butil.PrettyByte;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AsyncTruncatingTransport implements NonTruncatingTransport {
    private static final int DNS_UDP_PORT = 53;
    private final EventLoopGroup group;

    public AsyncTruncatingTransport() throws InterruptedException {
        group = new NioEventLoopGroup();
    }


    @Override
    public void send(byte[] payload, InetAddress dest, Consumer<byte[]> cb, Consumer<Throwable> onError) {
        setupSend(payload, dest, cb, onError);
    }

    public void shutdown() throws InterruptedException {
        group.shutdownGracefully().sync();
    }

    private void setupSend(byte[] payload, InetAddress dest, Consumer<byte[]> cb, Consumer<Throwable> onError) {
        Bootstrap clientBootstrap = new Bootstrap();

        clientBootstrap.group(group);
        clientBootstrap.channel(NioDatagramChannel.class);
        clientBootstrap.remoteAddress(dest, DNS_UDP_PORT);
        clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new ChannelHandler(payload, cb, onError));
            }
        });
        clientBootstrap.connect();
    }

    private static class ChannelHandler extends SimpleChannelInboundHandler<DatagramPacket> {
        private final byte[] payload;
        private final Consumer<byte[]> cb;
        private final Consumer<Throwable> onError;

        private ChannelHandler(byte[] payload, Consumer<byte[]> cb, Consumer<Throwable> onError) {
            this.payload = payload;
            this.cb = cb;
            this.onError = onError;
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            onError.accept(cause);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ctx.writeAndFlush(Unpooled.wrappedBuffer(payload));
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
            cb.accept(msg.content().array());
            ctx.close();
        }
    }
}
