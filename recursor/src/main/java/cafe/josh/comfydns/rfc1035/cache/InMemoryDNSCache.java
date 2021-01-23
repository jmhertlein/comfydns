package cafe.josh.comfydns.rfc1035.cache;

import cafe.josh.comfydns.rfc1035.message.field.query.QClass;
import cafe.josh.comfydns.rfc1035.message.field.query.QType;
import cafe.josh.comfydns.rfc1035.message.struct.RR;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class InMemoryDNSCache implements DNSCache {
    private final Map<String, Map<RR2Tuple, List<CachedRR<?>>>> cache;
    private final ReentrantReadWriteLock lock;

    public InMemoryDNSCache() {
        this.cache = new HashMap<>();
        lock = new ReentrantReadWriteLock();
    }

    @Override
    public List<RR<?>> search(String name, QType qType, QClass qClass) throws CacheAccessException {
        List<RR<?>> ret = new ArrayList<>();
        try {
            lock.readLock().lock();
            Map<RR2Tuple, List<CachedRR<?>>> rr2TupleRRMap = cache.get(name);
            if(rr2TupleRRMap == null) {
                return List.of();
            }

            for (Map.Entry<RR2Tuple, List<CachedRR<?>>> e : rr2TupleRRMap.entrySet()) {
                if(qType.queryMatches(e.getKey().rrType) && qClass.queryMatches(e.getKey().rrClass)) {
                    ret.addAll(e.getValue().stream()
                            .map(rr -> rr.getRr().adjustTTL(rr.getCacheTime()))
                            .collect(Collectors.toList())
                    );
                }
            }
        } finally {
            lock.readLock().unlock();
        }

        return ret;
    }

    @Override
    public void cache(RR<?> record, OffsetDateTime now) throws CacheAccessException {
        try {
            this.lock.writeLock().lock();
            Map<RR2Tuple, List<CachedRR<?>>> records = cache.computeIfAbsent(record.getName(), k -> new HashMap<>());
            List<CachedRR<?>> cachedRRS = records.computeIfAbsent(record.getClassAndType(),
                    k -> new ArrayList<>());
            cachedRRS.add(new CachedRR<>(record, now));
        } finally {
            this.lock.writeLock().unlock();
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
                    prune.forEach(rr -> {
                        Map<RR2Tuple, List<CachedRR<?>>> cacheForDomain = cache.get(rr.getName());
                        List<CachedRR<?>> typeClassMatch = cacheForDomain.get(rr.getClassAndType());
                        typeClassMatch.removeIf(crr -> crr.getRr() == rr);
                        if(typeClassMatch.isEmpty()) {
                            cacheForDomain.remove(rr.getClassAndType());
                        }
                        if(cacheForDomain.isEmpty()) {
                            cache.remove(rr.getName());
                        }

                    });

        } finally {
            this.lock.writeLock().unlock();
        }
    }

    private boolean isRRExpired(CachedRR<?> crr, OffsetDateTime now) {
        return crr.getRr().getTtl() - (crr.getCacheTime().until(now, ChronoUnit.SECONDS)) <= 0;
    }

    private static class PendingPrune {
        public final String domainName;
        public final RR2Tuple key;
        public final RR<?> record;

        private PendingPrune(String domainName, RR2Tuple key, RR<?> record) {
            this.domainName = domainName;
            this.key = key;
            this.record = record;
        }
    }


}