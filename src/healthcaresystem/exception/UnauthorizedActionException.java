package healthcaresystem.exception;

public class UnauthorizedActionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    // thrown when a user performs an action without proper authorization
    public UnauthorizedActionException(String message) {
        super(message);
    }
}
