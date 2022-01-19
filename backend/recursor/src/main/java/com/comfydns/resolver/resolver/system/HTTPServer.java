package com.comfydns.resolver.resolver.system;

import com.comfydns.resolver.resolver.rfc1035.service.RecursiveResolver;
import com.comfydns.util.config.EnvConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslProvider;

import javax.net.ssl.SSLException;
import java.io.File;

public class HTTPServer {
    private final Channel channel;
    public HTTPServer(RecursiveResolver resolver, EventLoopGroup bossGroup, EventLoopGroup workerGroup) throws InterruptedException {
        ServerBootstrap b = new ServerBootstrap();
        int port = EnvConfig.getDOHServerPort();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws SSLException {
                        ChannelPipeline p = ch.pipeline();
                        if(EnvConfig.getDOHUsesTLS()) {
                            p.addLast("ssl", SslContextBuilder
                                    .forServer(
                                            new File(EnvConfig.getDOHServerCertificateFile()),
                                            new File(EnvConfig.getDOHServerKeyFile())
                                    ).build()
                                    .newHandler(ch.alloc())
                            );
                        }
                        p.addLast(new HttpRequestDecoder());
                        p.addLast(new HttpObjectAggregator(1048576));
                        p.addLast(new HttpResponseEncoder());
                        p.addLast(new HttpContentCompressor());
                        p.addLast(new HTTPDNSHandler(resolver));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);;

        channel = b.bind(port).sync().channel();
    }

    public void stop() throws InterruptedException {
        channel.close().sync();
    }

    public void waitFor() throws InterruptedException {
        channel.closeFuture().sync();
    }
}
