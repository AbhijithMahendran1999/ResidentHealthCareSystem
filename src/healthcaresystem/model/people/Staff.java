package healthcaresystem.model.people;

import java.io.Serializable;

public abstract class Staff implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String username;

    public Staff(String id, String name, String username) {
        this.id = id;
        this.name = name;
        this.username = username;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getUsername() { return username; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setUsername(String username) { this.username = username; }
}
