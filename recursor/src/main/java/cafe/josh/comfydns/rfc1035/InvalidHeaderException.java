package cafe.josh.comfydns.rfc1035;

public class InvalidHeaderException extends InvalidMessageException {
    public InvalidHeaderException() {
    }

    public InvalidHeaderException(String message) {
        super(message);
    }

    public InvalidHeaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidHeaderException(Throwable cause) {
        super(cause);
    }

    public InvalidHeaderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
