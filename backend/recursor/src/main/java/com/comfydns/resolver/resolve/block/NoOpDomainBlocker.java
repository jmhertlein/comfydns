package com.comfydns.resolver.resolve.block;

import com.comfydns.resolver.resolve.rfc1035.cache.CacheAccessException;

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
