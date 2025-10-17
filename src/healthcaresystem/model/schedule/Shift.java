package healthcaresystem.model.schedule;

import java.io.Serializable;
import java.time.LocalDate;
import healthcaresystem.model.people.Staff;

// Represents a single work shift for a staff member
public class Shift implements Serializable {
    private static final long serialVersionUID = 1L;

    private Staff staff;      // who is assigned
    private LocalDate day;    // which day
    private ShiftType type;   // shift type (DAY / EVE)

    public Shift(Staff staff, LocalDate day, ShiftType type) {
        this.staff = staff;
        this.day = day;
        this.type = type;
    }

    // Getters
    public Staff getStaff() { return staff; }
    public LocalDate getDay() { return day; }
    public ShiftType getType() { return type; }

    // Setters
    public void setStaff(Staff staff) { this.staff = staff; }
    public void setDay(LocalDate day) { this.day = day; }
    public void setType(ShiftType type) { this.type = type; }
}
