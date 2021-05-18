package com.comfydns.resolver.resolver.rfc1035.cache.impl;

import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolver.rfc1035.cache.CachedRR;
import com.comfydns.resolver.resolver.rfc1035.cache.RR2Tuple;
import com.comfydns.resolver.resolver.rfc1035.cache.RRContainer;
import com.comfydns.resolver.resolver.rfc1035.message.field.query.QClass;
import com.comfydns.resolver.resolver.rfc1035.message.field.query.QType;
import com.comfydns.resolver.resolver.rfc1035.message.struct.RR;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class TemporaryDNSCache implements RRContainer {
    private final Map<String, Map<RR2Tuple, List<CachedRR<?>>>> cache;

    public TemporaryDNSCache() {
        this.cache = new HashMap<>();
    }

    @Override
    public List<RR<?>> search(String name, QType qType, QClass qClass, OffsetDateTime now) throws CacheAccessException {
        List<RR<?>> ret = new ArrayList<>();
        Map<RR2Tuple, List<CachedRR<?>>> rr2TupleRRMap = cache.get(name);
        if(rr2TupleRRMap == null) {
            return List.of();
        }

        for (Map.Entry<RR2Tuple, List<CachedRR<?>>> e : rr2TupleRRMap.entrySet()) {
            if(qType.queryMatches(e.getKey().rrType) && qClass.queryMatches(e.getKey().rrClass)) {
                ret.addAll(e.getValue().stream().map(CachedRR::getRr).collect(Collectors.toList()));
            }
        }

        return ret;
    }

    @Override
    public void cache(RR<?> record, OffsetDateTime now) throws CacheAccessException {
        Map<RR2Tuple, List<CachedRR<?>>> records = cache.computeIfAbsent(record.getName(), k -> new HashMap<>());
        List<CachedRR<?>> cachedRRS = records.computeIfAbsent(record.getClassAndType(),
                k -> new ArrayList<>());
        cachedRRS.add(new CachedRR<>(record, now));
    }
}
