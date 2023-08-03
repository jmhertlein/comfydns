package com.comfydns.resolver.resolver.rfc1035.message.field.rr.rdata;

import com.comfydns.resolver.resolver.butil.PrettyByte;
import com.comfydns.resolver.resolver.rfc1035.message.InvalidMessageException;
import com.comfydns.resolver.resolver.rfc1035.message.LabelCache;
import com.comfydns.resolver.resolver.rfc1035.message.UnsupportedRRTypeException;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.RData;
import com.comfydns.resolver.resolver.rfc1035.message.field.rr.KnownRRType;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Objects;

public class TXTRData implements RData {
    private final String text;

    public TXTRData(String text) {
        this.text = text;
        for(char c : text.toCharArray()) {
            if(c > 255) {
                throw new IllegalArgumentException("Non-ascii character is illegal: " + c);
            }
        }
    }

    public TXTRData(JsonObject o) {
        text = o.get("txt-data").getAsString();
    }

    public String getText() {
        return text;
    }

    @Override
    public KnownRRType getRRType() {
        return KnownRRType.TXT;
    }

    @Override
    public JsonObject writeJson() {
        JsonObject o = new JsonObject();
        o.add("txt-data", new JsonPrimitive(text));
        return o;
    }

    @Override
    public byte[] write(LabelCache ignore, int index) {
        int charStrings = text.length()/255;
        if(text.length() % 255 > 0) {
            charStrings++;
        }

        int outputLen = text.length() + charStrings;
        byte[] ret = new byte[outputLen];

        int pos = 0;
        String remaining = text;
        while(pos < ret.length) {
            String curSlice;
            if(remaining.length() > 255) {
                curSlice = remaining.substring(0, 255);
                remaining = curSlice.substring(255);
            } else {
                curSlice = remaining;
            }

            ret[pos] = (byte) curSlice.length();
            pos++;
            for(char c : curSlice.toCharArray()) {
                ret[pos] = (byte) c;
                pos++;
            }
        }

        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TXTRData txtrData = (TXTRData) o;
        return Objects.equals(text, txtrData.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }

    @Override
    public String toString() {
        return text;
    }

    public static TXTRData read(byte[] content, final int pos, int rdlength) throws InvalidMessageException, UnsupportedRRTypeException {
        StringBuilder b = new StringBuilder();
        int cur = pos;
        while(cur < (pos+rdlength)) {
            int len = Byte.toUnsignedInt(content[cur]);
            cur++;
            for(int i = cur; i < cur + len && i < pos+rdlength; i++) {
                b.append((char) content[i]);
            }
            cur += len;
        }

        return new TXTRData(b.toString());
    }
}
