package cafe.josh.comfydns.rfc1035.field.rr;

import cafe.josh.comfydns.PrettyByte;
import cafe.josh.comfydns.RDataConstructionFunction;
import cafe.josh.comfydns.rfc1035.field.rr.rdata.BlobRData;

import java.util.function.BiFunction;

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
}
