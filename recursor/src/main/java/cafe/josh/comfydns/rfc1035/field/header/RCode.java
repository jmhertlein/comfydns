package cafe.josh.comfydns.rfc1035.field.header;

import java.util.Optional;

public enum RCode {
    NO_ERROR(0, "No error condition"),
    FORMAT_ERROR(1, "Format error - The name server wasunable to interpret the query."),
    SERVER_FAILURE(2, "Server failure - The name server was unable to process this query due to a problem with the name server."),
    NAME_ERROR(3, "Name Error - Meaningful only for responses from an authoritative name server, this code signifies that the domain name referenced in the query does not exist."),
    NOT_IMPLEMENTED(4, "Not Implemented - The name server does not support the requested kind of query."),
    REFUSED(5, "Refused - The name server refuses to perform the specified operation for policy reasons.  For example, a name server may not wish to provide the information to the particular requester, or a name server may not wish to perform a particular operation (e.g., zone transfer) for particular data."),
    ;
    private final int code;
    private final String explanation;

    RCode(int code, String explanation) {
        this.code = code;
        this.explanation = explanation;
    }

    public static Optional<RCode> match(int code) {
        for (RCode c : RCode.values()) {
            if(c.code == code) {
                return Optional.of(c);
            }
        }

        return Optional.empty();
    }

    public int getCode() {
        return code;
    }

    public String getExplanation() {
        return explanation;
    }
}
