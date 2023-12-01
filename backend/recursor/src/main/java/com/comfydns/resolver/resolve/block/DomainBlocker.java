package com.comfydns.resolver.resolve.block;

import com.comfydns.resolver.resolve.rfc1035.cache.CacheAccessException;

import java.net.InetAddress;

public interface DomainBlocker {
    public boolean isBlocked(String name) throws CacheAccessException;
    public boolean blockForClient(InetAddress addr) throws CacheAccessException;
}
