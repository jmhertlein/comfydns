package com.comfydns.resolver.resolver.block;

import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;

import java.net.Inet4Address;
import java.net.InetAddress;

public class NoOpDomainBlocker implements DomainBlocker {
    @Override
    public boolean isBlocked(String name) throws CacheAccessException {
        return false;
    }

    @Override
    public boolean blockForClient(InetAddress addr) {
        return false;
    }
}
