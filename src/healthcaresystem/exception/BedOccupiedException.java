package healthcaresystem.exception;

public class BedOccupiedException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public BedOccupiedException(String message) { super(message); }
}
