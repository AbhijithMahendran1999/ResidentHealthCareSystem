package healthcaresystem.model.people;

import java.io.Serializable;

// Base class for all staff members (Doctor, Nurse, Manager)
public abstract class Staff implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String username;
    private String password;

    public Staff(String id, String name, String username) {
        this.id = id;
        this.name = name;
        this.username = username;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
}
