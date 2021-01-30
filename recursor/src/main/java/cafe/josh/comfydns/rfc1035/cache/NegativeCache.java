package cafe.josh.comfydns.rfc1035.cache;

import cafe.josh.comfydns.rfc1035.message.field.query.QClass;
import cafe.josh.comfydns.rfc1035.message.field.query.QType;
import io.prometheus.client.Counter;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class NegativeCache {
    private static final Counter cachedNegativeRecordsTotal = Counter.build()
            .name("cached_negative_records_total")
            .help("Total number of records ever put into the cache.")
            .register();
    private static final Counter cachedNegativeRecordsPrunedTotal = Counter.build()
            .name("cached_negative_records_pruned_total")
            .help("Total number of records ever removed from the cache.")
            .register();

    private final Map<String, Map<RR2Tuple, NegativeRecord>> cache;
    private final ReentrantReadWriteLock lock;

    public NegativeCache() {
        cache = new HashMap<>();
        lock = new ReentrantReadWriteLock(true);
    }

    public boolean cachedNegative(String qName, QType qType, QClass qClass, OffsetDateTime now) {
        lock.readLock().lock();
        try {
            Map<RR2Tuple, NegativeRecord> rr2TupleNegativeRecordMap = cache.get(qName);
            if(rr2TupleNegativeRecordMap == null) {
                return false;
            }
            RR2Tuple key = new RR2Tuple(qClass.getValue(), qType.getValue());
            NegativeRecord negativeRecord = rr2TupleNegativeRecordMap.get(key);
            if(negativeRecord == null) {
                return false;
            }

            return !negativeRecord.isExpired(now);

        } finally {
            lock.readLock().unlock();
        }
    }

    public void cacheNegative(String qName, QType qType, QClass qClass, long ttl, OffsetDateTime now) {
        lock.writeLock().lock();
        try {
            Map<RR2Tuple, NegativeRecord> rr2TupleNegativeRecordMap = cache.computeIfAbsent(qName, k -> new HashMap<>());
            RR2Tuple key = new RR2Tuple(qClass.getValue(), qType.getValue());
            rr2TupleNegativeRecordMap.put(key, new NegativeRecord(qName, key, ttl, now));
            cachedNegativeRecordsTotal.inc();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private static class NegativeRecord {
        private final String qName;
        private final RR2Tuple classType;
        public final long ttl;
        public final OffsetDateTime cachedAt;

        private NegativeRecord(String qName, RR2Tuple classType, long ttl, OffsetDateTime cachedAt) {
            this.qName = qName;
            this.classType = classType;
            this.ttl = ttl;
            this.cachedAt = cachedAt;
        }

        public boolean isExpired(OffsetDateTime now) {
            return cachedAt.until(now, ChronoUnit.SECONDS) >= ttl;
        }
    }

    public void prune(OffsetDateTime now) {
        lock.writeLock().lock();
        try {
            List<NegativeRecord> pending = cache.values().stream()
                    .flatMap(m -> m.values().stream())
                    .filter(r -> r.isExpired(now))
                    .collect(Collectors.toList());

            cachedNegativeRecordsPrunedTotal.inc(pending.size());

            pending.forEach(nr -> {
                Map<RR2Tuple, NegativeRecord> map = cache.get(nr.qName);
                map.remove(nr.classType);
                if(map.isEmpty()) {
                    cache.remove(nr.qName);
                }
            });

        } finally {
            lock.writeLock().unlock();
        }
    }
}
