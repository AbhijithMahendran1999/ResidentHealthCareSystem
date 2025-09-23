package healthcaresystem.model.facility;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import healthcaresystem.model.people.Resident;
import healthcaresystem.model.people.Staff;
import healthcaresystem.model.schedule.Roster;

public class CareHome implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Ward> wards = new ArrayList<>();
    private List<Staff> staff = new ArrayList<>();
    private List<Resident> residents = new ArrayList<>();
    private Roster roster = new Roster();

    // Getters
    public List<Ward> getWards() { return wards; }
    public List<Staff> getStaff() { return staff; }
    public List<Resident> getResidents() { return residents; }
    public Roster getRoster() { return roster; }

    // Setters
    public void setWards(List<Ward> wards) { this.wards = wards; }
    public void setStaff(List<Staff> staff) { this.staff = staff; }
    public void setResidents(List<Resident> residents) { this.residents = residents; }
    public void setRoster(Roster roster) { this.roster = roster; }
}
