package cafe.josh.comfydns.rfc1035.message.field.query;

import cafe.josh.comfydns.rfc1035.message.field.rr.RRClass;

public enum QOnlyClass implements QClass {
    STAR("*", (byte) 255, "any class");

    private final String type;
    private final byte value;
    private final String meaning;

    QOnlyClass(String type, byte value, String meaning) {
        this.type = type;
        this.value = value;
        this.meaning = meaning;
    }

    public String getType() {
        return type;
    }

    public byte[] getValue() {
        return new byte[]{0, value};
    }

    public String getMeaning() {
        return meaning;
    }

    @Override
    public boolean queryMatches(byte[] c) {
        if(this == STAR) {
            return true;
        } else {
            return QClass.super.queryMatches(c);
        }
    }


}
