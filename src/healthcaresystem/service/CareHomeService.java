package healthcaresystem.service;

import java.time.LocalDate;

import healthcaresystem.model.facility.Bed;
import healthcaresystem.model.facility.CareHome;
import healthcaresystem.model.people.Resident;
import healthcaresystem.model.people.Staff;
import healthcaresystem.model.schedule.Shift;
import healthcaresystem.model.schedule.ShiftType;

public class CareHomeService {

    private final CareHome careHome;
    private final AllocationService allocationService = new AllocationService();

    public CareHomeService(CareHome careHome) {
        this.careHome = careHome;
    }

    // adding staff and resident
    public void addStaff(Staff s) {
        careHome.getStaff().add(s);
    }

    public void addResident(Resident r) {
        careHome.getResidents().add(r);
    }

    // allocating resident to bed
    public boolean admitResidentToBed(Resident resident, Bed bed) {
        return allocationService.placeResident(resident, bed);
    }

    // assigning shifts
    public void assignShift(Staff staff, LocalDate day, ShiftType type) {
        careHome.getRoster().addShift(new Shift(staff, day, type));
    }

    public CareHome getCareHome() {
        return careHome;
    }
}
