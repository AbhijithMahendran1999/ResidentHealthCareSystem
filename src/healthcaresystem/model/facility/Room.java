package healthcaresystem.model.facility;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;              // unique room ID within a ward
    private List<Bed> beds = new ArrayList<>();  // list of beds in this room

    public Room(String id) {
        this.id = id;
    }

    // ---------- Getters ----------
    public String getId() { return id; }
    public List<Bed> getBeds() { return beds; }

    // ---------- Setters ----------
    public void setId(String id) { this.id = id; }
    public void setBeds(List<Bed> beds) { this.beds = beds; }
}
