package com.comfydns.resolver.resolve.rfc1035.service.search;

/**
 * Thrown for exceptions that happen in the process of trying to resolve a name, generally specifically for
 * reasons specific to resolving names, and not for more general issues like database errors. Examples are:
 * - We detected an nsdname loop and would go into infinite looping if we didn't fail this.
 * - We hit our limit of state transitions and are stopping because it's unlikely the query will succeed.
 */
public class NameResolutionException extends Exception {
    public NameResolutionException() {
    }

    public NameResolutionException(String message) {
        super(message);
    }

    public NameResolutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public NameResolutionException(Throwable cause) {
        super(cause);
    }
}
