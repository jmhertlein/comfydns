package cafe.josh.comfydns.system;

import cafe.josh.comfydns.rfc1035.service.RecursiveResolver;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class UDPServer {
    private final Channel channel;
    private final RecursiveResolver resolver;
    public UDPServer(RecursiveResolver resolver, EventLoopGroup bossGroup) {
        this.resolver = resolver;
        String portString = System.getenv("COMFYDNS_PORT");
        int port = portString == null ? 53 : Integer.parseInt(portString);
        Bootstrap b = new Bootstrap();
        b.group(bossGroup)
                .channel(NioDatagramChannel.class)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    protected void initChannel(NioDatagramChannel ch) throws Exception {
                        ch.pipeline().addLast(new UDPDNSHandler(UDPServer.this.resolver));
                    }
                });
        channel = b.bind(port).channel();
    }

    public void stop() throws InterruptedException {
        channel.close().sync();
    }

    public void waitFor() throws InterruptedException {
        channel.closeFuture().sync();
    }
}
