package cafe.josh.comfydns.rfc1035.field.query;

public interface QType {
    public String getType();
    public byte getValue();
    public String getMeaning();
    public default boolean isSupported() {
        return true;
    }
}
