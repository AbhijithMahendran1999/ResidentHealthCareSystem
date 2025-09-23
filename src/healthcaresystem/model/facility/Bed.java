package healthcaresystem.model.facility;

import java.io.Serializable;
import healthcaresystem.model.people.Resident;

public class Bed implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private char gender;            // 'M' or 'F'
    private Resident occupiedBy;    // null if vacant

    public Bed(String id, char gender) {
        this.id = id;
        this.gender = gender;
    }

    // Getters
    public String getId() { return id; }
    public char getGender() { return gender; }
    public Resident getOccupiedBy() { return occupiedBy; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setGender(char gender) { this.gender = gender; }
    public void setOccupiedBy(Resident occupiedBy) { this.occupiedBy = occupiedBy; }
}
