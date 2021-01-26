package cafe.josh.comfydns.system;

import cafe.josh.comfydns.rfc1035.service.RecursiveResolver;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class TCPServer {
    private final Channel channel;
    public TCPServer(RecursiveResolver resolver, EventLoopGroup bossGroup, EventLoopGroup workerGroup) throws InterruptedException {
        ServerBootstrap b = new ServerBootstrap();
        String portString = System.getenv("COMFYDNS_PORT");
        int port = portString == null ? 53 : Integer.parseInt(portString);
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new TCPDNSHandler(resolver));
                    }
                });

        channel = b.bind(port).sync().channel();
    }

    public void stop() throws InterruptedException {
        channel.close().sync();
    }

    public void waitFor() throws InterruptedException {
        channel.closeFuture().sync();
    }
}
