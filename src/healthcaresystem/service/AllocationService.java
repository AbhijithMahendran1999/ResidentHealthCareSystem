package healthcaresystem.service;

import healthcaresystem.model.facility.Bed;
import healthcaresystem.model.people.Resident;

public class AllocationService {

	// Put resident in bed if free; return true if placed, false otherwise
    public boolean placeResident(Resident resident, Bed bed) {
        if (bed.getOccupiedBy() != null) {
            return false;
        }
        bed.setOccupiedBy(resident);
        return true;
    }
}
