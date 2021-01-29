package cafe.josh.comfydns.rfc1035.cache;

import cafe.josh.comfydns.rfc1035.message.struct.RR;

import java.time.OffsetDateTime;

public interface RRContainer extends RRSource {
    void cache(RR<?> record, OffsetDateTime now) throws CacheAccessException;
}
