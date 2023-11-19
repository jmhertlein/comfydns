package com.comfydns.resolver.resolver.rfc1035.message;

/**
 * Indicates a message was so malformed that no useful information
 * could be extracted from it.
 */
public class InvalidMessageException extends Exception {
    public InvalidMessageException() {
    }

    public InvalidMessageException(String message) {
        super(message);
    }

    public InvalidMessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidMessageException(Throwable cause) {
        super(cause);
    }

    public InvalidMessageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
