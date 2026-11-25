package ecpc.exceptions;

/** Thrown when the selected components fail the compatibility checks. */
public class IncompatibleComponentException extends ECPCException {
    public IncompatibleComponentException(String message) {
        super(message);
    }
}