package cafe.josh.comfydns.rfc1035.field.header;

public enum OpCode {
    QUERY(0, "a standard query (QUERY)"),
    IQUERY(1, "an inverse query (IQUERY)"),
    STATUS(2, "a server status request (STATUS)"),
    ;

    private final int code;
    private final String explanation;

    OpCode(int code, String explanation) {
        this.code = code;
        this.explanation = explanation;
    }

    public int getCode() {
        return code;
    }

    public String getExplanation() {
        return explanation;
    }
}
