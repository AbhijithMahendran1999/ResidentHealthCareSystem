package healthcaresystem.service;

import healthcaresystem.model.facility.Bed;
import healthcaresystem.model.people.Resident;

// Handles assigning residents to beds
public class AllocationService {

    // Places a resident into the given bed if it's vacant
    public boolean placeResident(Resident resident, Bed bed) {
        if (bed.getOccupiedBy() != null) return false; // bed already occupied
        bed.setOccupiedBy(resident);
        return true;
    }
}
