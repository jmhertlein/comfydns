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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class AsyncTruncatingTransport implements TruncatingTransport {
    private static final Logger log = LoggerFactory.getLogger(AsyncTruncatingTransport.class);
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
        clientBootstrap.handler(new ChannelInitializer<NioDatagramChannel>() {
            protected void initChannel(NioDatagramChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new ChannelHandler(payload, cb, onError));
            }
        });
        clientBootstrap.connect();
    }

    private static class ChannelHandler extends SimpleChannelInboundHandler<DatagramPacket> {
        private final byte[] payload;
        private final Consumer<byte[]> cb;
        private final Consumer<Throwable> onError;

        private boolean done;

        private ChannelHandler(byte[] payload, Consumer<byte[]> cb, Consumer<Throwable> onError) {
            this.payload = payload;
            this.cb = cb;
            this.onError = onError;
            done = false;
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            onError.accept(cause);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ctx.writeAndFlush(Unpooled.wrappedBuffer(payload));
            ctx.channel().eventLoop().schedule(() -> {
                if(!done) {
                    log.warn("Timed out after 10 seconds waiting for UDP reply from " + ctx.channel().remoteAddress());
                    ctx.close();
                }
            }, 10, TimeUnit.SECONDS);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
            ByteBuf content = msg.content();
            byte[] dest = new byte[content.capacity()];
            content.getBytes(0, dest);
            cb.accept(dest);
            ctx.close();
            done = true;
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            if(!done) {
                onError.accept(new TimeoutException("Timed out waiting for response."));
            }
        }
    }
}
