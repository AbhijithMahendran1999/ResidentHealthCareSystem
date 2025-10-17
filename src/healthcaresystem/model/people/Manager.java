package healthcaresystem.model.people;

// Represents a manager who oversees staff and residents
public class Manager extends Staff {
    private static final long serialVersionUID = 1L;

    public Manager(String id, String name, String username) {
        super(id, name, username);
    }
}
