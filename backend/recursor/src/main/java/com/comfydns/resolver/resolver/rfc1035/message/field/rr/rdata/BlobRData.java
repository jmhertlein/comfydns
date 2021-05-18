package com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata;

import com.comfydns.resolver.resolver.rfc1035.message.LabelCache;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.RData;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Arrays;
import java.util.Base64;

public class BlobRData implements RData {
    private final byte[] data;

    public BlobRData(byte[] data) {
        this.data = data;
    }

    public BlobRData(JsonObject o) {
        data = Base64.getDecoder().decode(o.get("data").getAsString());
    }

    @Override
    public KnownRRType getRRType() {
        return null;
    }

    @Override
    public JsonObject writeJson() {
        JsonObject o = new JsonObject();
        o.add("data", new JsonPrimitive(Base64.getEncoder().encodeToString(data)));
        return o;
    }

    @Override
    public byte[] write(LabelCache c, int index) {
        return data;
    }

    public static BlobRData read(byte[] content, int pos, int rdlength) {
        byte[] d = new byte[rdlength];
        System.arraycopy(content, pos, d, 0, rdlength);
        return new BlobRData(d);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlobRData blobRData = (BlobRData) o;
        return Arrays.equals(data, blobRData.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }
}
