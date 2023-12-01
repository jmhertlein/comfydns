package com.comfydns.resolver.resolve.rfc1035.cache;

import com.comfydns.resolver.resolve.rfc1035.message.field.query.QClass;
import com.comfydns.resolver.resolve.rfc1035.message.field.query.QType;
import com.comfydns.resolver.resolve.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolve.rfc1035.message.struct.RR;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface RRSource {
    List<RR<?>> search(String name, QType qType, QClass qClass, OffsetDateTime now) throws CacheAccessException;
    default List<RR<?>> searchAAAAA(String name, QClass qClass, OffsetDateTime now) throws CacheAccessException {
        return Stream.concat(
                search(name, KnownRRType.A, qClass, now).stream(),
                search(name, KnownRRType.AAAA, qClass, now).stream()
        ).collect(Collectors.toList());
    }
    default boolean isAuthoritative() {
        return false;
    }
}
