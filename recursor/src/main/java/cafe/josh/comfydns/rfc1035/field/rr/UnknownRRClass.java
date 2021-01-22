package cafe.josh.comfydns.rfc1035.field.rr;

import cafe.josh.comfydns.PrettyByte;

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
}
