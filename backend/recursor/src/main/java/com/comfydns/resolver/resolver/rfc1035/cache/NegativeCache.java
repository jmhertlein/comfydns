package com.comfydns.resolver.resolver.rfc1035.cache;

import com.comfydns.resolver.resolver.rfc1035.message.field.query.QClass;
import com.comfydns.resolver.resolver.rfc1035.message.field.query.QType;

import java.time.OffsetDateTime;
import java.util.List;

public interface NegativeCache {
    boolean cachedNegative(String qName, QType qType, QClass qClass, OffsetDateTime now) throws CacheAccessException;

    void cacheNegative(String qName, QType qType, QClass qClass, long ttl, OffsetDateTime now) throws CacheAccessException;

    void bustCacheFor(List<String> qNames) throws CacheAccessException;

    void prune(OffsetDateTime now) throws CacheAccessException;
}
