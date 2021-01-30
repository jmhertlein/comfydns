package cafe.josh.comfydns.rfc1035.message.field.rr;

import cafe.josh.comfydns.butil.PrettyByte;
import cafe.josh.comfydns.rfc1035.message.RDataConstructionFunction;
import cafe.josh.comfydns.rfc1035.message.field.rr.rdata.BlobRData;

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
    public String toString() {
        return PrettyByte.binString(value[0]) + " " + PrettyByte.binString(value[1]);
    }
}
