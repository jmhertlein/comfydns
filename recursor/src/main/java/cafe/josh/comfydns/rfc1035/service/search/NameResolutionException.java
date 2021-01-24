package cafe.josh.comfydns.rfc1035.service.search;

public class NameResolutionException extends Exception {
    public NameResolutionException() {
    }

    public NameResolutionException(String message) {
        super(message);
    }

    public NameResolutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public NameResolutionException(Throwable cause) {
        super(cause);
    }
}
