package com.comfydns.resolver.resolver.system;

import com.comfydns.resolver.resolver.rfc1035.service.RecursiveResolver;
import com.comfydns.util.config.EnvConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class UDPServer {
    private final Channel channel;
    private final RecursiveResolver resolver;
    public UDPServer(RecursiveResolver resolver, EventLoopGroup bossGroup) {
        this.resolver = resolver;
        int port = EnvConfig.getDnsServerPort();
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
