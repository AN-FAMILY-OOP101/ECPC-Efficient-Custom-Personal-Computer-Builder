/** Thrown when an operation requires a complete build but one or more parts are missing. */
public class IncompleteBuildException extends ECPCException {
    public IncompleteBuildException(String message) {
        super(message);
    }
}
