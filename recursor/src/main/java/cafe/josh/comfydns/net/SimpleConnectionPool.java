package cafe.josh.comfydns.net;

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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SimpleConnectionPool implements ConnectionEventListener {
    final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final int IDLE_CXN_THRESHOLD_MINS = 2;

    private final PGConnectionPoolDataSource ds;
    private final Queue<PooledConnection> available, used;
    private final IdentityHashMap<PooledConnection, Instant> lastUse;

    public SimpleConnectionPool(PGConnectionPoolDataSource ds) throws SQLException {
        this.ds = ds;

        this.available = new LinkedList<>();
        this.used = new LinkedList<>();
        this.lastUse = new IdentityHashMap<>();

        preloadConnection();
    }

    private synchronized void preloadConnection() throws SQLException {
        PooledConnection first = ds.getPooledConnection();
        this.available.add(first);
        this.lastUse.put(first, Instant.now());
    }

    public synchronized Connection getConnection() throws SQLException {
        PooledConnection poll = available.poll();
        if(poll == null) {
            poll = ds.getPooledConnection();
            poll.addConnectionEventListener(this);
            log.debug("Created new physical db connection.");
        } else {
            log.debug("Re-using connection.");
        }

        used.add(poll);

        return poll.getConnection();
    }

    public synchronized void prune() {
        log.trace("Running prune!");
        if(this.available.size() == 0) {
            log.trace("No idle connections, not pruning.");
            return;
        }

        if(this.available.size() == 1) {
            log.trace("Not pruning last free connection.");
            return;
        }

        List<PooledConnection> removed = new ArrayList<>();

        for(PooledConnection cxn : available) {
            if(removed.size() == available.size()-1) {
                log.trace("Down to one connection, not pruning.");
                break;
            }

            if(lastUse.containsKey(cxn)) {
                Instant lastUse = this.lastUse.get(cxn);
                if(lastUse.isBefore(Instant.now().minus(IDLE_CXN_THRESHOLD_MINS, ChronoUnit.MINUTES))) {
                    log.debug("DB connection last use ts is {}, which is old. Closing it.", lastUse);
                    removed.add(cxn);
                }
            } else {
                log.warn("Weird: a pooled connection is in the available queue but without a last-use time. Closing it.");
                removed.add(cxn);
            }
        }

        for(PooledConnection removeMe : removed) {
            this.lastUse.remove(removeMe);
            try {
                removeMe.close();
            } catch(SQLException e) {
                log.error("Error closing physical db cxn after no-use timeout.", e);
            }
        }

        this.available.removeAll(removed);
        log.debug("Pruned {} connections, leaving {} in pool.", removed.size(), this.available.size());
    }

    protected synchronized void recycle(PooledConnection cxn) {
        this.used.remove(cxn);
        try {
            cxn.getConnection().setAutoCommit(true);
        } catch(SQLException e) {
            log.warn("Error recycling connection.", e);
            return;
        }
        this.available.add(cxn);
        this.lastUse.put(cxn, Instant.now());
    }

    public List<PooledConnection> getAvailable() {
        return new ArrayList<>(available);
    }

    public List<PooledConnection> getUsed() {
        return new ArrayList<>(used);
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
    }

    public void startPruning(ScheduledExecutorService svc) {
        svc.scheduleWithFixedDelay(this::wrapPrune, 10, 10, TimeUnit.SECONDS);
    }
}
