package cafe.josh.comfydns.rfc1035.cache;

import cafe.josh.comfydns.rfc1035.message.field.query.QClass;
import cafe.josh.comfydns.rfc1035.message.field.query.QType;
import cafe.josh.comfydns.rfc1035.message.field.rr.RRClass;
import cafe.josh.comfydns.rfc1035.message.field.rr.RRType;
import cafe.josh.comfydns.rfc1035.message.struct.RR;
import io.prometheus.client.Counter;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class InMemoryDNSCache implements RRCache {
    private static final Counter cachedRecordsTotal = Counter.build()
            .name("cached_records_total")
            .help("Total number of records ever put into the cache.")
            .labelNames("rrtype")
            .register();
    private static final Counter cachedRecordsPrunedTotal = Counter.build()
            .name("cached_records_pruned_total")
            .help("Total number of records ever removed from the cache.")
            .register();
    private final Map<String, Map<RR2Tuple, List<CachedRR<?>>>> cache;
    private final ReentrantReadWriteLock lock;

    public InMemoryDNSCache() {
        this.cache = new HashMap<>();
        lock = new ReentrantReadWriteLock(true);
    }

    @Override
    public List<RR<?>> search(String name, QType qType, QClass qClass, OffsetDateTime now) throws CacheAccessException {
        List<RR<?>> ret = new ArrayList<>();

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
        }

        return ret;
    }

    @Override
    public void cache(RR<?> record, OffsetDateTime now) throws CacheAccessException {
        this.lock.writeLock().lock();
        try {
            Map<RR2Tuple, List<CachedRR<?>>> records = cache.computeIfAbsent(record.getName(), k -> new HashMap<>());
            List<CachedRR<?>> cachedRRS = records.computeIfAbsent(record.getClassAndType(),
                    k -> new ArrayList<>());
            cachedRRS.add(new CachedRR<>(record, now));
            cachedRecordsTotal.labels(
                    record.getRrType().isWellKnown()
                    ? record.getRrType().getType().toLowerCase()
                    : "not_well_known"
            ).inc();
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
