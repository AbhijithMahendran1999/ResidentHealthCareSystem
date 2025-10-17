package healthcaresystem.exception;

public class ValidationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    // thrown when user input or data validation fails
    public ValidationException(String message) {
        super(message);
    }
}
