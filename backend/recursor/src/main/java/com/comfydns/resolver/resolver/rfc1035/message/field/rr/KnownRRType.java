package com.comfydns.resolver.resolver.rfc1035.message.field.rr;

import com.comfydns.resolver.resolver.rfc1035.message.RDataConstructionFunction;
import com.comfydns.resolver.resolver.rfc1035.message.RDataFromJsonFunction;
import com.comfydns.resolver.resolver.rfc1035.message.field.query.QType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata.*;

public enum KnownRRType implements QType,RRType {
    A("A", (byte) 1, "a host address", ARData::read, ARData::read, ARData.class),
    NS("NS", (byte) 2, "an authoritative name server", NSRData::read, NSRData::new, NSRData.class),
    MD("MD", (byte) 3, "a mail destination (Obsolete - use MX)"),
    MF("MF", (byte) 4, "a mail forwarder (Obsolete - use MX)"),
    CNAME("CNAME", (byte) 5, "the canonical name for an alias", CNameRData::read, CNameRData::read, CNameRData.class),
    SOA("SOA", (byte) 6, "marks the start of a zone of authority", SOARData::read, SOARData::new, SOARData.class),
    MB("MB", (byte) 7, "a mailbox domain name (EXPERIMENTAL)"),
    MG("MG", (byte) 8, "a mail group member (EXPERIMENTAL)"),
    MR("MR", (byte) 9, "a mail rename domain name (EXPERIMENTAL)"),
    NULL("NULL", (byte) 10, "a null RR (EXPERIMENTAL)"),
    WKS("WKS", (byte) 11, "a well known service description", WKSRData::read, WKSRData::new, WKSRData.class),
    PTR("PTR", (byte) 12, "a domain name pointer", PTRRData::read, PTRRData::read, PTRRData.class),
    HINFO("HINFO", (byte) 13, "host information"),
    MINFO("MINFO", (byte) 14, "mailbox or mail list information (EXPERIMENTAL"),
    MX("MX", (byte) 15, "mail exchange", MXRData::read, MXRData::new, MXRData.class),
    TXT("TXT", (byte) 16, "text strings", TXTRData::read, TXTRData::new, TXTRData.class),
    AAAA("AAAA", (byte) 28, "a host address (ipv6)", AAAARData::read, AAAARData::read, AAAARData.class),
    ;

    private final String type;
    private final byte[] value;
    private final String meaning;
    private final RDataConstructionFunction ctor;
    private final RDataFromJsonFunction jsonCtor;
    private final Class<? extends RData> rDataClass;

    KnownRRType(String type, byte value, String meaning,
                RDataConstructionFunction ctor, RDataFromJsonFunction jsonCtor, Class<? extends RData> rDataClass) {
        this.type = type;
        this.value = new byte[]{0, value};
        this.meaning = meaning;
        this.ctor = ctor;
        this.jsonCtor = jsonCtor;
        this.rDataClass = rDataClass;
    }

    KnownRRType(String type, byte value, String meaning) {
        this.type = type;
        this.value = new byte[]{0, value};
        this.meaning = meaning;
        this.ctor = null;
        this.jsonCtor = null;
        this.rDataClass = null;
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

    @Override
    public RDataConstructionFunction getCtor() {
        return ctor;
    }

    public RDataFromJsonFunction getJsonCtor() {
        return jsonCtor;
    }

    @Override
    public Class<? extends RData> getRDataClass() {
        return rDataClass;
    }
}
