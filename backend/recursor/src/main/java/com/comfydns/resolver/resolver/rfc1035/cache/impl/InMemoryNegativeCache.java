package com.comfydns.resolver.resolver.rfc1035.cache.impl;

import com.comfydns.resolver.resolver.rfc1035.cache.NegativeCache;
import com.comfydns.resolver.resolver.rfc1035.cache.RR2Tuple;
import com.comfydns.resolver.resolver.rfc1035.message.LabelCache;
import com.comfydns.resolver.resolver.rfc1035.message.field.query.QClass;
import com.comfydns.resolver.resolver.rfc1035.message.field.query.QType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import static com.comfydns.resolver.resolver.rfc1035.cache.impl.CacheMetrics.cachedNegativeRecordsPrunedTotal;
import static com.comfydns.resolver.resolver.rfc1035.cache.impl.CacheMetrics.cachedNegativeRecordsTotal;

public class InMemoryNegativeCache implements NegativeCache {
    private static final Logger log = LoggerFactory.getLogger(InMemoryNegativeCache.class);


    private final Map<String, Map<RR2Tuple, NegativeRecord>> cache;
    private final ReentrantReadWriteLock lock;

    public InMemoryNegativeCache() {
        cache = new HashMap<>();
        lock = new ReentrantReadWriteLock(true);
    }

    @Override
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

    @Override
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

    @Override
    public void bustCacheFor(List<String> qNames) {
        Set<String> names = qNames.stream().flatMap(qn -> LabelCache.genSuffixes(qn).stream())
                .collect(Collectors.toSet());

        lock.writeLock().lock();
        try {
            for (String n : names) {
                if(cache.containsKey(n)) {
                    cache.remove(n);
                    log.debug("Purged cached negatives for " + n);
                }
            }
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

    @Override
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
