package cafe.josh.comfydns.system;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class UDPServer {
    private final Channel channel;
    public UDPServer(EventLoopGroup bossGroup, SimpleConnectionPool dbPool) {
        String portString = System.getenv("COMFYDNS_PORT");
        int port = portString == null ? 53 : Integer.parseInt(portString);
        Bootstrap b = new Bootstrap();
        b.group(bossGroup)
                .channel(NioDatagramChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new UDPDNSHandler());
                    }
                });
        channel = b.bind(port).channel();
    }

    public void stop() throws InterruptedException {
        channel.close().sync();
    }
}
