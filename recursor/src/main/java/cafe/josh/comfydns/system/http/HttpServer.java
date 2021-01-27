package cafe.josh.comfydns.system.http;

import cafe.josh.comfydns.system.http.router.HttpRouter;
import cafe.josh.comfydns.system.http.router.Routed;
import cafe.josh.comfydns.system.http.router.RoutedRequest;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class HttpServer {
    private static final Logger log = LoggerFactory.getLogger(HttpServer.class);
    private final Channel channel;
    public HttpServer(HttpRouter router, EventLoopGroup bossGroup, EventLoopGroup workerGroup) throws InterruptedException {
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new HttpRequestDecoder());
                        p.addLast(new HttpObjectAggregator(1048576));
                        p.addLast(new HttpResponseEncoder());
                        p.addLast(new HttpContentCompressor());
                        p.addLast(new SimpleChannelInboundHandler<FullHttpRequest>() {
                            @Override
                            public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                                try {
                                    ctx.flush();
                                } finally {
                                    ctx.close();
                                }
                            }

                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
                                Optional<Routed> optionalRouted = router.route(msg);
                                if(optionalRouted.isEmpty()) {
                                    ctx.writeAndFlush(Responses.notFound(""));
                                    log.info("Failed to route for URI: {}", msg.uri());
                                    ctx.write(Responses.notFound(""));
                                    return;
                                }

                                Routed routed = optionalRouted.get();
                                RoutedRequest rr = new RoutedRequest(routed, msg);
                                final FullHttpResponse resp;
                                try {
                                    resp = routed.getRoute().getFunction().apply(rr);
                                } catch(Throwable t) {
                                    log.error("Error while handling request in route " + routed.getRoute().toString(), t);
                                    ctx.write(Responses.oops());
                                    return;
                                }

                                ctx.write(resp);

                                log.debug("200 OK: {}", msg.uri());
                            }
                        });
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        channel = b.bind(8080).sync().channel();
    }

    public void waitFor() throws InterruptedException {
        channel.closeFuture().sync();
    }
}
