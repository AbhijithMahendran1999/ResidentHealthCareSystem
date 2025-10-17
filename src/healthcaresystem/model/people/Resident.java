package healthcaresystem.model.people;

import java.io.Serializable;

// Represents a resident living in the care home
public class Resident implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private char gender; // 'M' or 'F'

    public Resident(String id, String name, char gender) {
        this.id = id;
        this.name = name;
        this.gender = gender;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public char getGender() { return gender; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setGender(char gender) { this.gender = gender; }
}
