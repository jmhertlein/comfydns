package cafe.josh.comfydns.rfc1035.field.rr;

import cafe.josh.comfydns.rfc1035.field.rr.query.QType;

public enum RRType implements QType {
    A("A", (byte) 1, "a host address"),
    NS("NS", (byte) 2, "an authoritative name server"),
    MD("MD", (byte) 3, "a mail destination (Obsolete - use MX)", false),
    MF("MF", (byte) 4, "a mail forwarder (Obsolete - use MX)", false),
    CNAME("CNAME", (byte) 5, "the canonical name for an alias"),
    SOA("SOA", (byte) 6, "marks the start of a zone of authority"),
    MB("MB", (byte) 7, "a mailbox domain name (EXPERIMENTAL)"),
    MG("MG", (byte) 8, "a mail group member (EXPERIMENTAL)"),
    MR("MR", (byte) 9, "a mail rename domain name (EXPERIMENTAL)"),
    NULL("NULL", (byte) 10, "a null RR (EXPERIMENTAL)"),
    WKS("WKS", (byte) 11, "a well known service description"),
    PTR("PTR", (byte) 12, "a domain name pointer"),
    HINFO("HINFO", (byte) 13, "host information"),
    MINFO("MINFO", (byte) 14, "mailbox or mail list information"),
    MX("MX", (byte) 15, "mail exchange"),
    TXT("TXT", (byte) 16, "text strings"),
    ;

    private final String type;
    private final byte value;
    private final String meaning;
    private final boolean supported;

    RRType(String type, byte value, String meaning) {
        this(type, value, meaning, true);
    }

    RRType(String type, byte value, String meaning, boolean supported) {
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
        return supported;
    }
}
