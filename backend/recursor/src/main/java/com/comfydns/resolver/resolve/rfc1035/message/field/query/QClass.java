package com.comfydns.resolver.resolve.rfc1035.message.field.query;

import com.comfydns.resolver.resolve.butil.PrettyByte;
import com.comfydns.resolver.resolve.rfc1035.message.field.rr.RRClass;

import java.util.Arrays;

public interface QClass {
    public String getType();
    public byte[] getValue();
    public default int getIntValue() {
        return (int) PrettyByte.readNBitUnsignedInt(16, getValue(), 0, 0);
    }
    public String getMeaning();
    public default boolean isSupported() {
        return true;
    }

    public default boolean queryMatches(byte[] c) {
        return Arrays.equals(c, getValue());
    }

    public static QClass match(byte[] content, int pos) {
        for (QOnlyClass value : QOnlyClass.values()) {
            byte[] v = value.getValue();
            if(v[0] == content[pos] && v[1] == content[pos+1]) {
                return value;
            }
        }

        return RRClass.match(content, pos);
    }

    public static QClass match(int value) {
        byte[] tmp = new byte[2];
        PrettyByte.writeNBitUnsignedInt(value, 16, tmp, 0, 0);
        return match(tmp, 0);
    }
}
