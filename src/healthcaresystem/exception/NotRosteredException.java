package healthcaresystem.exception;

public class NotRosteredException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    // thrown when a staff member performs an action while not rostered
    public NotRosteredException(String message) {
        super(message);
    }
}
