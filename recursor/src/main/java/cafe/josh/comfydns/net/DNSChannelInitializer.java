package cafe.josh.comfydns.net;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DNSChannelInitializer extends ChannelInitializer<SocketChannel> {
    final Logger log = LoggerFactory.getLogger(this.getClass());
    private final SimpleConnectionPool pool;

    public DNSChannelInitializer(SimpleConnectionPool pool) {
        this.pool = pool;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        log.debug("Initializing channel: {}", socketChannel.remoteAddress());
        ChannelPipeline p = socketChannel.pipeline();
        p.addLast(new UDPDNSHandler(this.pool));
    }
}
