package com.comfydns.resolver.resolver.rfc1035.cache;

import com.comfydns.resolver.resolver.rfc1035.message.struct.RR;

import java.time.OffsetDateTime;
import java.util.List;

public interface RRCache extends RRContainer {
    public void prune(OffsetDateTime now) throws CacheAccessException;
    public void expunge(List<RR<?>> records) throws CacheAccessException;
}
