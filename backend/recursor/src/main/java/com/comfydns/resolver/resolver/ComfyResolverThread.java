package com.comfydns.resolver.resolver;

import com.comfydns.resolver.resolver.block.DBDomainBlocker;
import com.comfydns.resolver.resolver.block.DomainBlocker;
import com.comfydns.resolver.resolver.block.NoOpDomainBlocker;
import com.comfydns.resolver.resolver.rfc1035.cache.AuthorityRRSource;
import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolver.rfc1035.cache.impl.DBNegativeCache;
import com.comfydns.resolver.resolver.rfc1035.cache.impl.AuthoritativeRecordsContainer;
import com.comfydns.resolver.resolver.rfc1035.cache.impl.DBDNSCache;
import com.comfydns.resolver.resolver.rfc1035.cache.NegativeCache;
import com.comfydns.resolver.resolver.rfc1035.cache.RRCache;
import com.comfydns.resolver.resolver.rfc1035.message.InvalidMessageException;
import com.comfydns.resolver.resolver.rfc1035.message.UnsupportedRRTypeException;
import com.comfydns.resolver.resolver.rfc1035.service.RecursiveResolver;
import com.comfydns.resolver.resolver.rfc1035.service.transport.AsyncNonTruncatingTransport;
import com.comfydns.resolver.resolver.rfc1035.service.transport.AsyncTruncatingTransport;
import com.comfydns.resolver.util.DatabaseUtils;
import com.comfydns.resolver.resolver.system.TCPServer;
import com.comfydns.resolver.resolver.system.UDPServer;
import com.comfydns.util.config.EnvConfig;
import com.comfydns.util.db.Flag;
import com.comfydns.util.db.SimpleConnectionPool;
import io.netty.channel.nio.NioEventLoopGroup;
import io.prometheus.client.hotspot.GarbageCollectorExports;
import io.prometheus.client.hotspot.MemoryPoolsExports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public class ComfyResolverThread implements Runnable {
    final Logger log = LoggerFactory.getLogger(this.getClass());
    private final NioEventLoopGroup bossGroup;
    private final NioEventLoopGroup workerGroup;

    private final UUID serverId;
    private final ScheduledExecutorService cron;
    private final SimpleConnectionPool dbPool;
    private final AtomicBoolean ready;
    private final ExecutorService stateMachinePool;
    private final RecursiveResolver resolver;

    public ComfyResolverThread(
            UUID serverId, NioEventLoopGroup workerGroup, NioEventLoopGroup bossGroup,
            ScheduledExecutorService cron,
            ExecutorService stateMachinePool,
            SimpleConnectionPool dbPool) throws SQLException, ExecutionException, InterruptedException {
        this.serverId = serverId;
        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
        this.cron = cron;
        this.dbPool = dbPool;
        this.ready = new AtomicBoolean(false);
        this.stateMachinePool = stateMachinePool;

        new MemoryPoolsExports().register();
        new GarbageCollectorExports().register();

        RRCache cache = new DBDNSCache(dbPool);
        cron.scheduleAtFixedRate(() -> {
            try {
                cache.prune(OffsetDateTime.now());
            } catch(Throwable t) {
                log.warn("DNS cache pruner error", t);
            }
        }, 30, 30, TimeUnit.SECONDS);

        NegativeCache negativeCache = new DBNegativeCache(dbPool);
        cron.scheduleAtFixedRate(() -> {
            try {
                negativeCache.prune(OffsetDateTime.now());
            } catch(Throwable t) {
                log.warn("DNS negative cache pruner error", t);
            }
        }, 30, 30, TimeUnit.SECONDS);

        DomainBlocker domainBlocker;

        try(Connection c = dbPool.getConnection().get()) {
            if(Flag.enabled("adblock", c)) {
                log.info("Adblocking enabled.");
                try {
                    domainBlocker = new DBDomainBlocker(dbPool);
                } catch (SQLException | ExecutionException | InterruptedException throwables) {
                    throw new RuntimeException("Error initializing DBDomainBlocker", throwables);
                }
            } else {
                domainBlocker = new NoOpDomainBlocker();
            }
        }

        Set<InetAddress> allowZoneTransferToAddresses = null;
        try {
            allowZoneTransferToAddresses = new HashSet<>(
                    EnvConfig.getAllowZoneTransferTo()
            );
        } catch (UnknownHostException e) {
            throw new RuntimeException("Error loading list of IPs to allow zone transfers to.", e);
        }

        resolver = new RecursiveResolver(stateMachinePool, cache, negativeCache, new AsyncTruncatingTransport(workerGroup),
                new AsyncNonTruncatingTransport(workerGroup), allowZoneTransferToAddresses);

        resolver.setDomainBlocker(domainBlocker);
    }

    public RecursiveResolver getResolver() {
        return resolver;
    }

    @Override
    public void run() {
        try {
            doRun();
        } catch(Throwable t) {
            log.error("Error in run.", t);
            System.exit(1);
        }
    }

    public void doRun() throws SQLException, UnsupportedRRTypeException, InvalidMessageException, ExecutionException, InterruptedException, CacheAccessException {
        resolver.getAuthorityZonesLock().lock();
        try(Connection c = dbPool.getConnection().get()) {
            c.setAutoCommit(false);
            AuthorityRRSource container = AuthoritativeRecordsContainer.load(c);
            DatabaseUtils.updateServerAuthoritativeZoneState(c, container, serverId);
            c.commit();
            resolver.setAuthorityZones(container);
        } finally {
            resolver.getAuthorityZonesLock().unlock();
        }

        try(Connection c = dbPool.getConnection().get()) {
            if(DBDomainBlocker.isEnabled(c)) {
                resolver.setDomainBlocker(new DBDomainBlocker(dbPool));
            } else {
                resolver.setDomainBlocker(new NoOpDomainBlocker());
            }
        }

        try {
            TCPServer tcp = new TCPServer(resolver, bossGroup, workerGroup);
            UDPServer udp = new UDPServer(resolver, bossGroup);
            ready.set(true);
            log.info("Resolver ready.");
            tcp.waitFor();
            udp.waitFor();
        } catch (InterruptedException e) {
            log.error("Interrupted.", e);
        } finally {
            try { bossGroup.shutdownGracefully().sync(); } catch (InterruptedException ignore) {}
            try { workerGroup.shutdownGracefully().sync(); } catch (InterruptedException ignore) {}
            this.cron.shutdown();
        }
    }


    public void stop() {
        log.warn("stop() called. This probably shouldn't happen in prod.");


        this.cron.shutdown();
    }

    public boolean isReady() {
        return ready.get();
    }
}
