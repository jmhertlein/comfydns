package com.comfydns.resolver.resolver.rfc1035.message.field.rr;

import com.comfydns.resolver.resolver.butil.PrettyByte;
import com.comfydns.resolver.resolver.rfc1035.message.RDataConstructionFunction;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.BlobRData;

import java.util.Arrays;

public class UnknownRRType implements RRType {
    private final byte[] value;

    public UnknownRRType(byte[] value) {
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
    public boolean isWellKnown() {
        return false;
    }

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public RDataConstructionFunction getCtor() {
        return BlobRData::read;
    }

    @Override
    public Class<? extends RData> getRDataClass() {
        return BlobRData.class;
    }

    @Override
    public String toString() {
        return PrettyByte.binString(value[0]) + " " + PrettyByte.binString(value[1]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnknownRRType that = (UnknownRRType) o;
        return Arrays.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }
}
