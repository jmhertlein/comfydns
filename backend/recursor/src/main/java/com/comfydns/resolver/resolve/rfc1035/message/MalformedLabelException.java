package com.comfydns.resolver.resolve.rfc1035.message;

public class MalformedLabelException extends InvalidMessageException {
    public MalformedLabelException() {
    }

    public MalformedLabelException(String message) {
        super(message);
    }

    public MalformedLabelException(String message, Throwable cause) {
        super(message, cause);
    }

    public MalformedLabelException(Throwable cause) {
        super(cause);
    }

    public MalformedLabelException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
