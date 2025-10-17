package healthcaresystem.model.facility;

import java.io.Serializable;
import healthcaresystem.model.people.Resident;

public class Bed implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;                // unique bed ID (e.g., W1-R2-B3)
    private Resident occupiedBy;      // null means the bed is vacant

    public Bed(String id) {
        this.id = id;
    }

    // basic getters and setters
    public String getId() { return id; }
    public Resident getOccupiedBy() { return occupiedBy; }
    public void setOccupiedBy(Resident occupiedBy) { this.occupiedBy = occupiedBy; }
}
