package healthcaresystem.model.schedule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Roster implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Shift> shifts = new ArrayList<>();

    // Getters/Setters
    public List<Shift> getShifts() { return shifts; }
    public void setShifts(List<Shift> shifts) { this.shifts = shifts; }

    //Other operations
    public void addShift(Shift shift) { shifts.add(shift); }
    public boolean removeShift(Shift shift) { return shifts.remove(shift); }
}
