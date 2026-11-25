package ecpc.exceptions;

/** Thrown when an invalid value or unsupported operation is supplied to ECPC. */
public class InvalidInputException extends ECPCException {
    public InvalidInputException(String message) {
        super(message);
    }
}