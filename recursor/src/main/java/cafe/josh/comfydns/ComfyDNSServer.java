package cafe.josh.comfydns;

import cafe.josh.comfydns.net.DNSChannelInitializer;
import cafe.josh.comfydns.net.SimpleConnectionPool;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.postgresql.ds.PGConnectionPoolDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;


public class ComfyDNSServer implements Runnable {
    final Logger log = LoggerFactory.getLogger(this.getClass());

    private Channel channel;
    private SimpleConnectionPool pool;
    private final ScheduledExecutorService cron;
    private final AtomicBoolean ready;

    public ComfyDNSServer() {
        this.cron = Executors.newScheduledThreadPool(1);
        this.ready = new AtomicBoolean(false);
    }

    public void run() {
        log.info("Starting ComfyDNS server.");
        try {
            Class.forName("org.postgresql.Driver");
        } catch(ClassNotFoundException e) {
            log.error("Error poking PG driver.", e);
            return;
        }

        log.info("Opening connection pool to db...");
        PGConnectionPoolDataSource pgPool = new PGConnectionPoolDataSource();
        pgPool.setApplicationName("comfydns-recursor");

        pgPool.setDatabaseName("comfydns");
        pgPool.setPortNumbers(new int[]{54321});
        pgPool.setUser("comfydns");
        String passwordFileName = System.getProperty("COMFYDNS_DB_PASSWORD_FILE", "");
        String password;
        if(passwordFileName.isBlank()) {
            password = "";
        } else {
            try {
                password = Files.readString(Path.of(passwordFileName)).strip();
            } catch (IOException e) {
                log.error("Fatal startup error.", e);
                return;
            }
        }
        pgPool.setPassword(password);
        pgPool.setURL("jdbc:postgresql://" + System.getenv("COMFYDNS_DB_HOST") + "/");
        try {
            this.pool = new SimpleConnectionPool(pgPool);
        } catch(SQLException e) {
            log.error("Error starting db connection pool", e);
            return;
        }
        this.pool.startPruning(this.cron);

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        try {
            int port = Integer.parseInt(System.getProperty("CONFYDNS_PORT", "53"));
            Bootstrap b = bootstrapUDP(bossGroup, pool);
            log.info("Binding to port {}", port);
            channel = b.bind(port).channel();
            log.info("Bound to port {}", port);
            Runtime.getRuntime().gc();
            this.ready.set(true);
            channel.closeFuture().sync();
            log.info("Port {} unbound.", port);
        } catch(InterruptedException e) {
            log.warn("Interrupted while waiting for web server to finish.", e);
        } finally {
            bossGroup.shutdownGracefully();
        }
    }

    private Bootstrap bootstrapUDP(EventLoopGroup bossGroup, SimpleConnectionPool pool) {
        Bootstrap b = new Bootstrap();
        b.group(bossGroup)
                .channel(NioDatagramChannel.class)
                .handler(new DNSChannelInitializer(pool));

        return b;
    }

    public SimpleConnectionPool getPool() {
        return pool;
    }

    public void stop() {
        log.warn("stop() called. This probably shouldn't happen in prod.");
        if(channel != null) {
            channel.close();
            channel = null;
        }

        this.cron.shutdown();
    }

    public boolean isReady() {
        return ready.get();
    }
}
