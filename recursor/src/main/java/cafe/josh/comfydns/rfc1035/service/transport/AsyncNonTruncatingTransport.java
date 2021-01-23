package cafe.josh.comfydns.rfc1035.service.transport;

import cafe.josh.comfydns.butil.PrettyByte;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AsyncNonTruncatingTransport implements NonTruncatingTransport {
    private static final int DNS_TCP_PORT = 53;
    private final EventLoopGroup group;

    public AsyncNonTruncatingTransport() throws InterruptedException {
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
        clientBootstrap.channel(NioSocketChannel.class);
        clientBootstrap.remoteAddress(dest, DNS_TCP_PORT);
        clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new ChannelHandler(payload, cb, onError));
            }
        });
        clientBootstrap.connect();
    }

    private static class ChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {
        private final byte[] payload;
        private final Consumer<byte[]> cb;
        private final Consumer<Throwable> onError;
        private final List<byte[]> in;
        private int msgLen;

        private ChannelHandler(byte[] payload, Consumer<byte[]> cb, Consumer<Throwable> onError) {
            this.payload = payload;
            this.cb = cb;
            this.onError = onError;
            in = new ArrayList<>();
            msgLen = -1;
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
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
            in.add(msg.array());
            if(msgLen == -1 && bytesRead() >= 2) {
                byte[] len = new byte[2];
                int pos = 0;
                collectTwoOctets:
                for (byte[] bytes : in) {
                    for(byte b : bytes) {
                        if(pos < 2) {
                            len[pos] = b;
                            pos++;
                        } else {
                            break collectTwoOctets;
                        }
                    }
                }

                msgLen = (int) PrettyByte.readNBitUnsignedInt(16, len, 0, 0);
            }

            if(msgLen != -1 && bytesRead() >= msgLen) {
                ctx.close();
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            int len = bytesRead();
            byte[] ret = new byte[len];
            int pos = 0;
            for (byte[] bytes : in) {
                System.arraycopy(bytes, 0, ret, pos, bytes.length);
                pos += bytes.length;
            }

            cb.accept(ret);
            ctx.close();
        }

        private int bytesRead() {
            return in.stream().mapToInt(l -> l.length).sum();
        }
    }
}
