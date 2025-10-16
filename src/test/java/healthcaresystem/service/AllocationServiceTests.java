package healthcaresystem.service;

import static org.junit.Assert.*;

import org.junit.Test;

import healthcaresystem.model.facility.Bed;
import healthcaresystem.model.people.Resident;

public class AllocationServiceTests {

    @Test
    public void placeResident_intoVacantBed_returnsTrue_andOccupies() {
        AllocationService svc = new AllocationService();
        Bed bed = new Bed("B1");
        Resident r = new Resident("RES-1", "John", 'M');

        boolean placed = svc.placeResident(r, bed);

        assertTrue(placed);
        assertEquals(r, bed.getOccupiedBy());
    }

    @Test
    public void placeResident_intoOccupiedBed_returnsFalse_andKeepsOriginal() {
        AllocationService svc = new AllocationService();
        Bed bed = new Bed("B1");
        Resident r1 = new Resident("RES-1", "John", 'M');
        Resident r2 = new Resident("RES-2", "Mike", 'M');

        assertTrue(svc.placeResident(r1, bed));     // first time is going to be ok
        boolean placedAgain = svc.placeResident(r2, bed);

        assertFalse(placedAgain);
        assertEquals(r1, bed.getOccupiedBy());      // unchanged first allocation
    }
}
