package cafe.josh.comfydns.rfc1035.cache;

import cafe.josh.comfydns.rfc1035.message.field.query.QClass;
import cafe.josh.comfydns.rfc1035.message.field.query.QType;
import cafe.josh.comfydns.rfc1035.message.struct.RR;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OverlayCache implements RRSource {
    private final RRSource[] caches;

    public OverlayCache(RRSource... caches) {
        this.caches = caches;
    }

    @Override
    public List<RR<?>> search(String name, QType qType, QClass qClass, OffsetDateTime now) throws CacheAccessException {
        Set<RR<?>> ret = new HashSet<>();
        for (RRSource c : caches) {
            List<RR<?>> search = c.search(name, qType, qClass, now);
            ret.addAll(search);
        }

        return new ArrayList<>(ret);
    }
}
