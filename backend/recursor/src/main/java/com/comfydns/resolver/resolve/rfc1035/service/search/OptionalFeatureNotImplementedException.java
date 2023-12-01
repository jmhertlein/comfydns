package com.comfydns.resolver.resolve.rfc1035.service.search;

public class OptionalFeatureNotImplementedException extends Exception {
    public OptionalFeatureNotImplementedException() {
        super();
    }

    public OptionalFeatureNotImplementedException(String message) {
        super(message);
    }

    public OptionalFeatureNotImplementedException(String message, Throwable cause) {
        super(message, cause);
    }

    public OptionalFeatureNotImplementedException(Throwable cause) {
        super(cause);
    }
}
