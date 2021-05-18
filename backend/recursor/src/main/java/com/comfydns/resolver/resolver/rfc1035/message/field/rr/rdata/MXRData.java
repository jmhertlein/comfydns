package com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata;

import com.comfydns.resolver.resolver.butil.PrettyByte;
import com.comfydns.resolver.resolver.rfc1035.message.InvalidMessageException;
import com.comfydns.resolver.resolver.rfc1035.message.LabelCache;
import com.comfydns.resolver.resolver.rfc1035.message.LabelMaker;
import com.comfydns.resolver.resolver.rfc1035.message.UnsupportedRRTypeException;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRType;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.RData;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Objects;

public class MXRData implements RData {
    private final int preference;
    private final String exchange;

    public MXRData(int preference, String exchange) {
        this.preference = preference;
        this.exchange = exchange;
    }

    public MXRData(JsonObject o) {
        this.preference = o.get("preference").getAsInt();
        this.exchange = o.get("exchange").getAsString();
    }

    public int getPreference() {
        return preference;
    }

    public String getExchange() {
        return exchange;
    }

    @Override
    public KnownRRType getRRType() {
        return KnownRRType.MX;
    }

    @Override
    public JsonObject writeJson() {
        JsonObject o = new JsonObject();
        o.add("preference", new JsonPrimitive(preference));
        o.add("exchange", new JsonPrimitive(exchange));
        return o;
    }

    @Override
    public byte[] write(LabelCache c, int index) {
        byte[] exchange = LabelMaker.makeLabels(this.exchange, c);
        c.addSuffixes(this.exchange, index+2);

        byte[] ret = new byte[2 + exchange.length];
        System.arraycopy(exchange, 0, ret, 2, exchange.length);
        PrettyByte.writeNBitUnsignedInt(preference, 16, ret, 0, 0);

        return ret;
    }

    public static RData read(byte[] content, int pos, int rdlength) throws InvalidMessageException, UnsupportedRRTypeException {
        int preference = (int) PrettyByte.readNBitUnsignedInt(16, content, pos, 0);
        LabelMaker.ReadLabels readLabels = LabelMaker.readLabels(content, pos + 2);

        return new MXRData(preference, readLabels.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MXRData mxrData = (MXRData) o;
        return preference == mxrData.preference && exchange.equals(mxrData.exchange);
    }

    @Override
    public int hashCode() {
        return Objects.hash(preference, exchange);
    }
}
