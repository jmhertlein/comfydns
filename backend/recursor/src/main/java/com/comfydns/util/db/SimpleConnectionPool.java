package com.comfydns.util.db;

import io.prometheus.client.Gauge;
import org.postgresql.ds.PGConnectionPoolDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

public class SimpleConnectionPool implements ConnectionEventListener {
    private static final int MAX_DB_CONNECTIONS = 8;
    final Logger log = LoggerFactory.getLogger(this.getClass());
    private static final Gauge activeDBConnections = Gauge.build()
            .name("active_db_connections")
            .help("Number of open db connections")
            .register();

    private static final Gauge usedDBConnections = Gauge.build()
            .name("used_db_connections")
            .help("Number of open db connections")
            .register();
    private static final Gauge waitingDBConnections = Gauge.build()
            .name("waiting_db_connections")
            .help("Number of requestors waiting for a db connection.")
            .register();

    private static final int IDLE_CXN_THRESHOLD_MINS = 5;

    private final PGConnectionPoolDataSource ds;
    private final Deque<PooledConnectionHolder> available;
    private final Deque<CompletableFuture<Connection>> requests;
    private int connectionCount;
    private final int maxConnections;


    public SimpleConnectionPool(PGConnectionPoolDataSource ds) throws SQLException {
        this(ds, MAX_DB_CONNECTIONS);
    }

    public SimpleConnectionPool(PGConnectionPoolDataSource ds, int maxConnections) throws SQLException {
        this.ds = ds;
        this.available = new ArrayDeque<>();
        this.requests = new ArrayDeque<>();
        connectionCount = 0;
        this.maxConnections = maxConnections;

        preloadConnection();
    }

    private synchronized void preloadConnection() throws SQLException {
        PooledConnection first = ds.getPooledConnection();
        first.addConnectionEventListener(this);
        this.available.push(new PooledConnectionHolder(first));
        connectionCount++;
        activeDBConnections.inc();
    }

    public synchronized Future<Connection> getConnection() throws SQLException {
        final PooledConnectionHolder cxn;
        if(available.isEmpty()) {
            if(connectionCount >= maxConnections) {
                CompletableFuture<Connection> ret = new CompletableFuture<>();
                requests.add(ret);
                waitingDBConnections.inc();
                return ret;
            } else {
                cxn = new PooledConnectionHolder(ds.getPooledConnection());
                connectionCount++;
                activeDBConnections.inc();
                cxn.pcxn.addConnectionEventListener(this);
                log.debug("Created new physical db connection.");
            }
        } else {
            log.debug("Re-using connection.");
            cxn = available.pop();
        }

        Connection ret = cxn.pcxn.getConnection();
        usedDBConnections.inc();
        CompletableFuture<Connection> retFuture = new CompletableFuture<>();
        retFuture.complete(ret);
        return retFuture;
    }

    public synchronized void prune() {
        log.debug("Running prune!");
        if(this.available.size() == 0) {
            log.debug("No idle connections, not pruning.");
            return;
        }

        if(this.available.size() == 1) {
            log.debug("Not pruning last free connection.");
            return;
        }

        List<PooledConnectionHolder> removed = new ArrayList<>();

        for(PooledConnectionHolder cxn : available) {
            if(removed.size() == available.size()-1) {
                log.debug("Down to one connection, not pruning.");
                break;
            }

            Instant lastUse = cxn.lastUse;
            if(lastUse.isBefore(Instant.now().minus(IDLE_CXN_THRESHOLD_MINS, ChronoUnit.MINUTES))) {
                log.debug("DB connection last use ts is {}, which is old. Closing it.", lastUse);
                removed.add(cxn);
            }
        }

        for(PooledConnectionHolder removeMe : removed) {
            try {
                activeDBConnections.dec();
                connectionCount--;
                removeMe.pcxn.close();
            } catch(SQLException e) {
                log.error("Error closing physical db cxn after no-use timeout.", e);
            }
        }

        this.available.removeAll(removed);
        log.debug("Pruned {} connections, leaving {} in pool.", removed.size(), this.available.size());
    }

    protected synchronized void recycle(PooledConnection cxn) {
        if(requests.isEmpty()) {
            available.push(new PooledConnectionHolder(cxn));
            usedDBConnections.dec();
            log.debug("Recycled connection.");
        } else {
            CompletableFuture<Connection> first = requests.removeFirst();
            try {
                Connection r = cxn.getConnection();
                first.complete(r);
            } catch (SQLException throwables) {
                first.completeExceptionally(throwables);
            } finally {
                waitingDBConnections.dec();
            }
            log.debug("Recycled connection but immediately gave it to a pending request.");
        }
    }

    private void wrapPrune() {
        try {
            this.prune();
        } catch(Throwable t) {
            log.error("DB cxn pruning thread died lol fml.", t);
            throw t;
        }
    }

    @Override
    public void connectionClosed(ConnectionEvent connectionEvent) {
        PooledConnection cxn = (PooledConnection) connectionEvent.getSource();
        recycle(cxn);
        log.debug("Connection returned to pool.");
    }

    @Override
    public void connectionErrorOccurred(ConnectionEvent connectionEvent) {
        log.debug("Something happened to a connection.", connectionEvent.getSQLException());
        try {
            ((PooledConnection) connectionEvent.getSource()).close();
        } catch(Throwable t) {
            log.debug("Error while closing errored connection.", t);
        }
        usedDBConnections.dec();
        activeDBConnections.dec();
    }

    public void startPruning(ScheduledExecutorService svc) {
        svc.scheduleWithFixedDelay(this::wrapPrune, 10, 10, TimeUnit.SECONDS);
    }

    private static class PooledConnectionHolder {
        public final UUID id;
        public final Instant lastUse;
        public final PooledConnection pcxn;

        public PooledConnectionHolder(PooledConnection pcxn) {
            this.pcxn = pcxn;
            lastUse = Instant.now();
            id = UUID.randomUUID();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PooledConnectionHolder that = (PooledConnectionHolder) o;
            return id.equals(that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
}
