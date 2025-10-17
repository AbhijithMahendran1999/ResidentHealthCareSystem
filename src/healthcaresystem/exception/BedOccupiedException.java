package healthcaresystem.exception;

public class BedOccupiedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    // thrown when trying to assign a resident to an already occupied bed
    public BedOccupiedException(String message) {
        super(message);
    }
}
