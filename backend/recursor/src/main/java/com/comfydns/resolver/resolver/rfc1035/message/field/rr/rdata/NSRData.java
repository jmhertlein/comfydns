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

public class NSRData implements RData {
    private final String nsDName;

    public NSRData(String nsDName) {
        this.nsDName = nsDName;
    }

    public NSRData(JsonObject o) {
        nsDName = o.get("nsdname").getAsString();
    }

    public String getNsDName() {
        return nsDName;
    }

    @Override
    public KnownRRType getRRType() {
        return KnownRRType.NS;
    }

    @Override
    public JsonObject writeJson() {
        JsonObject o = new JsonObject();
        o.add("nsdname", new JsonPrimitive(nsDName));
        return o;
    }

    @Override
    public byte[] write(LabelCache c, int index) {
        byte[] ret = LabelMaker.makeLabels(nsDName, c);
        c.addSuffixes(nsDName, index);
        return ret;
    }

    public static RData read(byte[] content, int pos, int rdlength) throws InvalidMessageException, UnsupportedRRTypeException {
        LabelMaker.ReadLabels readLabels = LabelMaker.readLabels(content, pos);
        return new NSRData(readLabels.name);
    }

    @Override
    public String toString() {
        return "NSDNAME: " + nsDName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NSRData nsrData = (NSRData) o;
        return nsDName.equals(nsrData.nsDName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nsDName);
    }
}
