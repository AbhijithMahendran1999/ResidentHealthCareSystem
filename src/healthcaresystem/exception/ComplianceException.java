package healthcaresystem.exception;

public class ComplianceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    // thrown when a compliance check fails (e.g., scheduling or procedure rules)
    public ComplianceException(String message) {
        super(message);
    }
}
