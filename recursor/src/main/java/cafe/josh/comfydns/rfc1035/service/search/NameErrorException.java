package cafe.josh.comfydns.rfc1035.service.search;

public class NameErrorException extends Exception {
    public NameErrorException() {
    }

    public NameErrorException(String message) {
        super(message);
    }

    public NameErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public NameErrorException(Throwable cause) {
        super(cause);
    }
}
