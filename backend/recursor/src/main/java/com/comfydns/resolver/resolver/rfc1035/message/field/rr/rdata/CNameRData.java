package com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata;

import com.comfydns.resolver.resolver.rfc1035.message.InvalidMessageException;
import com.comfydns.resolver.resolver.rfc1035.message.LabelCache;
import com.comfydns.resolver.resolver.rfc1035.message.LabelMaker;
import com.comfydns.resolver.resolver.rfc1035.message.UnsupportedRRTypeException;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.RData;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Objects;

public class CNameRData implements RData {
    private final String domainName;

    public CNameRData(String domainName) {
        this.domainName = domainName;
    }

    @Override
    public KnownRRType getRRType() {
        return KnownRRType.CNAME;
    }

    public String getDomainName() {
        return domainName;
    }

    @Override
    public byte[] write(LabelCache c, int index) {
        byte[] ret = LabelMaker.makeLabels(domainName, c);
        c.addSuffixes(domainName, index);
        return ret;
    }

    public static RData read(byte[] content, int pos, int rdlength) throws InvalidMessageException, UnsupportedRRTypeException {
        LabelMaker.ReadLabels readLabels = LabelMaker.readLabels(content, pos);
        return new CNameRData(readLabels.name);
    }

    public static RData read(JsonObject o) {
        return new CNameRData(o.get("cname").getAsString());
    }

    @Override
    public JsonObject writeJson() {
        JsonObject ret = new JsonObject();
        ret.add("cname", new JsonPrimitive(domainName));
        return ret;
    }

    @Override
    public String toString() {
        return "CNAME='" + domainName + "'";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CNameRData that = (CNameRData) o;
        return domainName.equals(that.domainName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domainName);
    }
}
