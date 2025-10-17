package healthcaresystem.exception;

public class PersistenceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    // thrown when saving or loading application data fails
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    // basic constructor for persistence-related errors
    public PersistenceException(String message) {
        super(message);
    }
}
