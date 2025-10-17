package healthcaresystem.model.facility;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Ward implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;                 // ward identifier (e.g., W1, W2)
    private List<Room> rooms = new ArrayList<>(); // list of rooms in this ward

    public Ward(String id) {
        this.id = id;
    }

    // ---------- Getters ----------
    public String getId() { return id; }
    public List<Room> getRooms() { return rooms; }

    // ---------- Setters ----------
    public void setId(String id) { this.id = id; }
    public void setRooms(List<Room> rooms) { this.rooms = rooms; }
}
