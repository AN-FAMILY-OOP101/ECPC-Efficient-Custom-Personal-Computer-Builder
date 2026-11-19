/** Thrown when the current build costs more than the allowed maximum budget. */
public class BudgetExceededException extends ECPCException {
    public BudgetExceededException(String message) {
        super(message);
    }
}
