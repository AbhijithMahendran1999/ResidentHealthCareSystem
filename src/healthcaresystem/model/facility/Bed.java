package healthcaresystem.model.facility;

import java.io.Serializable;
import healthcaresystem.model.people.Resident;

public class Bed implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private Resident occupiedBy; // null = vacant

    public Bed(String id) {
        this.id = id;
    }

    public String getId() { return id; }
    public Resident getOccupiedBy() { return occupiedBy; }
    public void setOccupiedBy(Resident occupiedBy) { this.occupiedBy = occupiedBy; }
}
