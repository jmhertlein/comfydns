package com.comfydns.resolver.resolver.rfc1035.cache.impl;

import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolver.rfc1035.cache.CachedRR;
import com.comfydns.resolver.resolver.rfc1035.cache.RR2Tuple;
import com.comfydns.resolver.resolver.rfc1035.cache.RRCache;
import com.comfydns.resolver.resolver.rfc1035.message.field.query.QClass;
import com.comfydns.resolver.resolver.rfc1035.message.field.query.QType;
import com.comfydns.resolver.resolver.rfc1035.message.struct.RR;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import static com.comfydns.resolver.resolver.rfc1035.cache.impl.CacheMetrics.cachedRecordsPrunedTotal;

/**
 * An entirely in-memory RRCache that only removes records by TTL-based expiration.
 */
public class InMemoryDNSCache implements RRCache {
    private final Map<String, Map<RR2Tuple, List<CachedRR<?>>>> cache;
    private final ReentrantReadWriteLock lock;

    public InMemoryDNSCache() {
        this.cache = new HashMap<>();
        lock = new ReentrantReadWriteLock(true);

        CacheMetrics.currentCachedRecords.setChild(new Gauge.Child() {
            @Override
            public double get() {
                lock.readLock().lock();
                try {
                    return cache.values().stream()
                            .flatMap(m -> m.values().stream())
                            .mapToLong(Collection::size)
                            .sum();
                } finally {
                    lock.readLock().unlock();
                }
            }
        });
    }

    @Override
    public List<RR<?>> search(String name, QType qType, QClass qClass, OffsetDateTime now) throws CacheAccessException {
        List<RR<?>> ret = new ArrayList<>();
        Histogram.Timer t = CacheMetrics.cacheReadTimeSeconds.startTimer();
        lock.readLock().lock();
        try {
            Map<RR2Tuple, List<CachedRR<?>>> rr2TupleRRMap = cache.get(name);
            if (rr2TupleRRMap == null) {
                return List.of();
            }

            for (Map.Entry<RR2Tuple, List<CachedRR<?>>> e : rr2TupleRRMap.entrySet()) {
                if (qType.queryMatches(e.getKey().rrType) && qClass.queryMatches(e.getKey().rrClass)) {
                    ret.addAll(e.getValue().stream()
                            .map(rr -> rr.getRr().adjustTTL(rr.getCacheTime(), now))
                            .filter(rr -> rr.getTtl() > 0)
                            .collect(Collectors.toList())
                    );
                }
            }
        } finally {
            lock.readLock().unlock();
            t.observeDuration();
        }

        return ret;
    }

    @Override
    public void cache(RR<?> record, OffsetDateTime now) throws CacheAccessException {
        Histogram.Timer t = CacheMetrics.cacheWriteTimeSeconds.startTimer();
        this.lock.writeLock().lock();
        try {
            Map<RR2Tuple, List<CachedRR<?>>> records = cache.computeIfAbsent(record.getName(), k -> new HashMap<>());
            List<CachedRR<?>> cachedRRS = records.computeIfAbsent(record.getClassAndType(),
                    k -> new ArrayList<>());
            cachedRRS.add(new CachedRR<>(record, now));
            CacheMetrics.recordCache(record);
        } finally {
            this.lock.writeLock().unlock();
            t.observeDuration();
        }
    }

    @Override
    public void prune(OffsetDateTime now) {
        try {
            this.lock.writeLock().lock();
            List<? extends RR<?>> prune = cache.values().stream()
                    .flatMap(caches -> caches.values().stream())
                    .flatMap(Collection::stream)
                    .filter(crr -> isRRExpired(crr, now))
                    .map(CachedRR::getRr)
                    .collect(Collectors.toList());

            cachedRecordsPrunedTotal.inc(prune.size());
            removeRRs(prune);

        } finally {
            this.lock.writeLock().unlock();
        }
    }

    private void removeRRs(List<? extends RR<?>> prune) {
        prune.forEach(rr -> {
            Map<RR2Tuple, List<CachedRR<?>>> cacheForDomain = cache.get(rr.getName());
            List<CachedRR<?>> typeClassMatch = cacheForDomain.get(rr.getClassAndType());
            typeClassMatch.removeIf(crr -> crr.getRr() == rr);
            if (typeClassMatch.isEmpty()) {
                cacheForDomain.remove(rr.getClassAndType());
            }
            if (cacheForDomain.isEmpty()) {
                cache.remove(rr.getName());
            }

        });
    }

    @Override
    public void expunge(List<RR<?>> records) {
        this.lock.writeLock().lock();
        try {
            removeRRs(records);
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    private static boolean isRRExpired(CachedRR<?> crr, OffsetDateTime now) {
        return crr.getRr().getTtl() - (crr.getCacheTime().until(now, ChronoUnit.SECONDS)) <= 0;
    }

}
