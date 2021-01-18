package cafe.josh.comfydns.rfc1035.field.rr.query;

public interface QClass {
    public String getType();
    public byte getValue();
    public String getMeaning();
    public default boolean isSupported() {
        return true;
    }
}
