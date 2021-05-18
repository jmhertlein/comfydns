package com.comfydns.resolver.resolver.rfc1035.message.field.header;

import java.util.Optional;

public enum OpCode {
    QUERY(0, "a standard query (QUERY)"),
    IQUERY(1, "an inverse query (IQUERY)"),
    STATUS(2, "a server status request (STATUS)"),
    ;

    private final int code;
    private final String explanation;

    OpCode(int code, String explanation) {
        this.code = code;
        this.explanation = explanation;
    }

    public static Optional<OpCode> match(int code) {
        for (OpCode o : OpCode.values()) {
            if(o.code == code) {
                return Optional.of(o);
            }
        }

        return Optional.empty();
    }

    public int getCode() {
        return code;
    }

    public String getExplanation() {
        return explanation;
    }
}
