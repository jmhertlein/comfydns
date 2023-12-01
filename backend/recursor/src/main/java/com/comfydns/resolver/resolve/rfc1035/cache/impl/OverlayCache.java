package com.comfydns.resolver.resolve.rfc1035.cache.impl;

import com.comfydns.resolver.resolve.rfc1035.cache.CacheAccessException;
import com.comfydns.resolver.resolve.rfc1035.cache.RRSource;
import com.comfydns.resolver.resolve.rfc1035.message.field.query.QClass;
import com.comfydns.resolver.resolve.rfc1035.message.field.query.QType;
import com.comfydns.resolver.resolve.rfc1035.message.struct.RR;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * An RRSource that just concatenates the results of queries to subordinate RRSources
 */
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

    @Override
    public List<RR<?>> searchAAAAA(String name, QClass qClass, OffsetDateTime now) throws CacheAccessException {
        List<RR<?>> ret = new ArrayList<>();
        for (RRSource c : caches) {
            ret.addAll(c.searchAAAAA(name, qClass, now));
        }

        return ret;
    }
}
