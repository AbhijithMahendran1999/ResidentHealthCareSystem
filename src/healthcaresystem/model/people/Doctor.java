package healthcaresystem.model.people;

// Represents a doctor who can create and update prescriptions
public class Doctor extends Staff {
    private static final long serialVersionUID = 1L;

    public Doctor(String id, String name, String username) {
        super(id, name, username);
    }
}
