package cafe.josh.comfydns.rfc1035.message.field.query;

import cafe.josh.comfydns.rfc1035.message.field.rr.RRClass;
import cafe.josh.comfydns.rfc1035.message.field.rr.RRType;

import java.util.Arrays;

public interface QClass {
    public String getType();
    public byte[] getValue();
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
}
