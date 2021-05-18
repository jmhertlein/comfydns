package com.comfydns.resolver.resolver.rfc1035.message.field.rr;

import com.comfydns.resolver.resolver.butil.PrettyByte;

import java.util.Arrays;

public class UnknownRRClass implements RRClass {
    private final byte[] value;

    public UnknownRRClass(byte[] value) {
        this.value = value;
    }

    @Override
    public String getType() {
        return PrettyByte.binString(value[0]) + " " + PrettyByte.binString(value[1]);
    }

    @Override
    public byte[] getValue() {
        return value;
    }

    @Override
    public String getMeaning() {
        return "Unknown type.";
    }

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public boolean isWellKnown() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnknownRRClass that = (UnknownRRClass) o;
        return Arrays.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }
}
