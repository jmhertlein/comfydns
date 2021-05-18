package com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata;

import com.comfydns.resolver.resolver.rfc1035.message.InvalidMessageException;
import com.comfydns.resolver.resolver.rfc1035.message.LabelCache;
import com.comfydns.resolver.resolver.rfc1035.message.LabelMaker;
import com.comfydns.resolver.resolver.rfc1035.message.UnsupportedRRTypeException;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.RData;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PTRRData implements RData {
    private final String ptrDName;

    public PTRRData(String ptrDName) {
        this.ptrDName = ptrDName;
    }

    public PTRRData(JsonObject o) {
        this.ptrDName = o.get("ptrdname").getAsString();
    }

    @Override
    public KnownRRType getRRType() {
        return KnownRRType.PTR;
    }

    @Override
    public JsonObject writeJson() {
        JsonObject o = new JsonObject();
        o.add("ptrdname", new JsonPrimitive(ptrDName));
        return o;
    }

    @Override
    public byte[] write(LabelCache c, int index) {
        byte[] ret = LabelMaker.makeLabels(ptrDName, c);
        c.addSuffixes(ptrDName, index);
        return ret;
    }


    public static RData read(byte[] content, int pos, int rdlength) throws InvalidMessageException, UnsupportedRRTypeException {
        LabelMaker.ReadLabels readLabels = LabelMaker.readLabels(content, pos);
        return new PTRRData(readLabels.name);
    }

    public static RData read(JsonObject o) {
        return new PTRRData(o.get("ptrdname").getAsString());
    }

    @Override
    public String toString() {
        return "PTRDNAME: " + ptrDName;
    }

    public static String ipToInAddrArpa(String ipString) {
        List<String> parts = Arrays.asList(ipString.split("\\."));
        Collections.reverse(parts);
        return String.join(".", parts) + ".in-addr.arpa";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PTRRData ptrrData = (PTRRData) o;
        return ptrDName.equals(ptrrData.ptrDName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ptrDName);
    }
}


