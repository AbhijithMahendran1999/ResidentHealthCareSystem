package healthcaresystem.model.people;

// Represents a nurse who can move residents and administer medications
public class Nurse extends Staff {
    private static final long serialVersionUID = 1L;

    public Nurse(String id, String name, String username) {
        super(id, name, username);
    }
}
