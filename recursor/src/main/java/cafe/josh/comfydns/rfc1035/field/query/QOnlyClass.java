package cafe.josh.comfydns.rfc1035.field.query;

public enum QOnlyClass {
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

    public byte getValue() {
        return value;
    }

    public String getMeaning() {
        return meaning;
    }
}
