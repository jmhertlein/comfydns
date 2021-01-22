package cafe.josh.comfydns.rfc1035.message.field.rr;

import cafe.josh.comfydns.rfc1035.message.field.query.QClass;

public enum KnownRRClass implements QClass, RRClass {
    IN("IN", (byte) 1, "the Internet"),
    CS("CS", (byte) 2, "the CSNET class (Obsolete - used only for examples in some obsolete RFCs)", false),
    CH("CH", (byte) 3, "the CHAOS class"),
    HS("HS", (byte) 4, "Hesiod [Dyer 87]")
    ;

    private final String type;
    private final byte[] value;
    private final String meaning;
    private final boolean supported;

    KnownRRClass(String type, byte value, String meaning) {
        this(type, value, meaning, true);
    }

    KnownRRClass(String type, byte value, String meaning, boolean supported) {
        this.type = type;
        this.value = new byte[]{0, value};
        this.meaning = meaning;
        this.supported = supported;
    }

    public String getType() {
        return type;
    }

    public byte[] getValue() {
        return value;
    }

    @Override
    public boolean isSupported() {
        return supported;
    }

    @Override
    public boolean isWellKnown() {
        return true;
    }

    public String getMeaning() {
        return meaning;
    }
}
