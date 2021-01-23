package cafe.josh.comfydns.rfc1035.message.field.query;

import cafe.josh.comfydns.rfc1035.message.field.rr.RRType;

import java.util.Arrays;

public interface QType {
    public String getType();
    public byte[] getValue();
    public String getMeaning();
    public default boolean isSupported() {
        return true;
    }

    public default boolean queryMatches(byte[] t) {
        return Arrays.equals(t, getValue());
    }

    public static QType match(byte[] content, int pos) {
        for (QOnlyType value : QOnlyType.values()) {
            byte[] v = value.getValue();
            if(v[0] == content[pos] && v[1] == content[pos+1]) {
                return value;
            }
        }

        return RRType.match(content, pos);
    }
}
