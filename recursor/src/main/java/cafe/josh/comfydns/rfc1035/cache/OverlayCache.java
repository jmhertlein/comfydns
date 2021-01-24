package cafe.josh.comfydns.rfc1035.cache;

import cafe.josh.comfydns.rfc1035.message.field.query.QClass;
import cafe.josh.comfydns.rfc1035.message.field.query.QType;
import cafe.josh.comfydns.rfc1035.message.struct.RR;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class OverlayCache implements DNSCache {
    private final DNSCache[] caches;

    public OverlayCache(DNSCache ... caches) {
        this.caches = caches;
    }

    @Override
    public List<RR<?>> search(String name, QType qType, QClass qClass, OffsetDateTime now) throws CacheAccessException {
        Set<RR<?>> ret = new HashSet<>();
        for (DNSCache c : caches) {
            List<RR<?>> search = c.search(name, qType, qClass, now);
            ret.addAll(search);
        }

        return new ArrayList<>(ret);
    }

    @Override
    public void cache(RR<?> record, OffsetDateTime now) throws CacheAccessException {
        throw new UnsupportedOperationException("Add to the underlying caches, not the overlay cache.");
    }

    @Override
    public void prune(OffsetDateTime now) {
        throw new UnsupportedOperationException("Overlay caches can't be pruned.");
    }
}
