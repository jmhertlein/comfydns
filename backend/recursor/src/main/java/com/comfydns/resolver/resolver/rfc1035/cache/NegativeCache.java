package com.comfydns.resolver.resolver.rfc1035.cache;

import com.comfydns.resolver.resolver.rfc1035.message.field.query.QClass;
import com.comfydns.resolver.resolver.rfc1035.message.field.query.QType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.SOARData;
import com.comfydns.resolver.resolver.rfc1035.message.struct.RR;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface NegativeCache {
    Optional<RR<SOARData>> cachedNegative(String qName, QType qType, QClass qClass, OffsetDateTime now) throws CacheAccessException;

    void cacheNegative(String qName, QType qType, QClass qClass, RR<SOARData> soaRR, OffsetDateTime now) throws CacheAccessException;

    void bustCacheFor(List<String> qNames) throws CacheAccessException;

    void prune(OffsetDateTime now) throws CacheAccessException;
}
