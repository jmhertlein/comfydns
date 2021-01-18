package cafe.josh.comfydns.rfc1035.field.query;

public enum QOnlyType implements QType {
    AXFR("AXFR", (byte) 252, "A request for a transfer of an entire zone"),
    MAILB("MAILB", (byte) 253, "A request for mailbox-related records (MB, MG or MR)"),
    MAILA("MAILA", (byte) 254, "A request for mail agent RRs (Obsolete - see MX)", false),
    STAR("*", (byte) 255, "A request for all records"),
    ;

    private final String type;
    private final byte value;
    private final String meaning;
    private final boolean supported;

    QOnlyType(String type, byte value, String meaning) {
        this(type, value, meaning, true);
    }

    QOnlyType(String type, byte value, String meaning, boolean supported) {
        this.type = type;
        this.value = value;
        this.meaning = meaning;
        this.supported = supported;
    }

    public String getType() {
        return type;
    }

    public byte getValue() {
        return value;
    }

    public String getMeaning() {
        return meaning;
    }

    @Override
    public boolean isSupported() {
        return this.supported;
    }


}
