package com.comfydns.resolver.resolver.block;

import com.comfydns.resolver.resolver.rfc1035.cache.CacheAccessException;

import java.net.Inet4Address;
import java.net.InetAddress;

public interface DomainBlocker {
    public boolean isBlocked(String name) throws CacheAccessException;
    public boolean blockForClient(InetAddress addr) throws CacheAccessException;
}
