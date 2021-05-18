package com.comfydns.resolver.resolver.rfc1035.cache;

import com.comfydns.resolver.resolver.rfc1035.message.struct.RR;

import java.time.OffsetDateTime;

public interface RRContainer extends RRSource {
    void cache(RR<?> record, OffsetDateTime now) throws CacheAccessException;
}
