package cafe.josh.comfydns.rfc1035.cache;

import cafe.josh.comfydns.rfc1035.message.field.rr.RData;
import cafe.josh.comfydns.rfc1035.message.struct.RR;

import java.time.OffsetDateTime;

public class CachedRR<T extends RData> {
    private final RR<T> rr;
    private final OffsetDateTime cacheTime;

    public CachedRR(RR<T> rr, OffsetDateTime cacheTime) {
        this.rr = rr;
        this.cacheTime = cacheTime;
    }

    public RR<T> getRr() {
        return rr;
    }

    public OffsetDateTime getCacheTime() {
        return cacheTime;
    }
}
