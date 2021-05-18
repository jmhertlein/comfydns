package com.comfydns.resolver.resolver.rfc1035.cache;

public class CacheAccessException extends Exception {
    public CacheAccessException() {
    }

    public CacheAccessException(String message) {
        super(message);
    }

    public CacheAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheAccessException(Throwable cause) {
        super(cause);
    }
}
