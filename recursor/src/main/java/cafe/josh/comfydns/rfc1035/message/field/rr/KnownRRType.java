package cafe.josh.comfydns.rfc1035.message.field.rr;

import cafe.josh.comfydns.rfc1035.message.RDataConstructionFunction;
import cafe.josh.comfydns.rfc1035.message.field.query.QType;
import cafe.josh.comfydns.rfc1035.message.field.rr.rdata.*;

public enum KnownRRType implements QType,RRType {
    A("A", (byte) 1, "a host address", ARData::read),
    NS("NS", (byte) 2, "an authoritative name server", NSRData::read),
    MD("MD", (byte) 3, "a mail destination (Obsolete - use MX)"),
    MF("MF", (byte) 4, "a mail forwarder (Obsolete - use MX)"),
    CNAME("CNAME", (byte) 5, "the canonical name for an alias", CNameRData::read),
    SOA("SOA", (byte) 6, "marks the start of a zone of authority", SOARData::read),
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
    AAAA("AAAA", (byte) 28, "a host address (ipv6)", AAAARData::read),
    ;

    private final String type;
    private final byte[] value;
    private final String meaning;
    private final RDataConstructionFunction ctor;

    KnownRRType(String type, byte value, String meaning, RDataConstructionFunction ctor) {
        this.type = type;
        this.value = new byte[]{0, value};
        this.meaning = meaning;
        this.ctor = ctor;
    }

    KnownRRType(String type, byte value, String meaning) {
        this.type = type;
        this.value = new byte[]{0, value};
        this.meaning = meaning;
        this.ctor = null;
    }

    public String getType() {
        return type;
    }

    public byte[] getValue() {
        return this.value;
    }

    public String getMeaning() {
        return meaning;
    }

    @Override
    public boolean isWellKnown() {
        return true;
    }

    @Override
    public boolean isSupported() {
        return ctor != null;
    }

    public RDataConstructionFunction getCtor() {
        return ctor;
    }


}
