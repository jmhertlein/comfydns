package cafe.josh.comfydns.rfc1035.field.query;

import cafe.josh.comfydns.rfc1035.field.rr.RRClass;

public interface QClass {
    public String getType();
    public byte[] getValue();
    public String getMeaning();
    public default boolean isSupported() {
        return true;
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
