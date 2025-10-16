package healthcaresystem.exception;

public class NotRosteredException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public NotRosteredException(String message) { super(message); }
}
