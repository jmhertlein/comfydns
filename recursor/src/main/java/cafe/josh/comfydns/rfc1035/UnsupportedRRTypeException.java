package cafe.josh.comfydns.rfc1035;

public class UnsupportedRRTypeException extends Exception {
    public UnsupportedRRTypeException() {
    }

    public UnsupportedRRTypeException(String message) {
        super(message);
    }

    public UnsupportedRRTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedRRTypeException(Throwable cause) {
        super(cause);
    }
}
