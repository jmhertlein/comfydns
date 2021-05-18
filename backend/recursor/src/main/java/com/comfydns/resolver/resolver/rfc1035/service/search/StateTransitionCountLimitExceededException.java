package com.comfydns.resolver.resolver.rfc1035.service.search;

public class StateTransitionCountLimitExceededException extends Exception {
    public StateTransitionCountLimitExceededException() {
    }

    public StateTransitionCountLimitExceededException(String message) {
        super(message);
    }

    public StateTransitionCountLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }

    public StateTransitionCountLimitExceededException(Throwable cause) {
        super(cause);
    }
}
