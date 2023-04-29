package com.comfydns.resolver.resolver.rfc1035.cache;

import com.comfydns.resolver.resolver.rfc1035.message.struct.RR;
import com.comfydns.resolver.resolver.rfc1035.service.search.SearchContext;

import java.time.OffsetDateTime;

public interface RRContainer extends RRSource {
    void cache(RR<?> record, OffsetDateTime now, SearchContext sCtx) throws CacheAccessException;
    default void cache(RR<?> record, OffsetDateTime now) throws CacheAccessException {
        this.cache(record, now, null);
    }
}
