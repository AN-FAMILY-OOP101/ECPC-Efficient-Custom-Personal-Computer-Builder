/** Base class for application-specific ECPC exceptions. */
public class ECPCException extends RuntimeException {
    public ECPCException(String message) {
        super(message);
    }

    public ECPCException(String message, Throwable cause) {
        super(message, cause);
    }
}
