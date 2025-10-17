package healthcaresystem.service;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import healthcaresystem.model.facility.*;
import healthcaresystem.model.people.*;
import healthcaresystem.exception.UnauthorizedActionException;

public class CareHomeServiceResidentBedTests {

    private CareHomeService service;
    private Manager manager;
    private Nurse nurse;

    @Before
    public void init() {
        service = new CareHomeService(new CareHome());
        service.ensureDefaultFacility();

        manager = new Manager("M1", "Mgr", "mgr");
        manager.setPassword("pw");
        service.addStaff(manager);

        nurse = new Nurse("N1", "Nurse", "nurse");
        nurse.setPassword("pw");
        service.addStaff(nurse);
    }

    private String anyBed() {
        return service.getCareHome().getWards().get(0).getRooms().get(0).getBeds().get(0).getId();
    }

    @Test
    public void manager_canAddResidentToVacantBed() {
        String bedId = anyBed();
        service.addResidentToBed("R1", "Alice", 'F', bedId, manager);
        Bed b = findBed(bedId);
        assertNotNull(b.getOccupiedBy());
        assertEquals("R1", b.getOccupiedBy().getId());
    }

    @Test(expected = IllegalStateException.class)
    public void addingToOccupiedBed_throws() {
        String bedId = anyBed();
        service.addResidentToBed("R1", "Alice", 'F', bedId, manager);
        service.addResidentToBed("R2", "Bob", 'M', bedId, manager);
    }

    @Test(expected = UnauthorizedActionException.class)
    public void nurse_cannotAddResidentToBed() {
        service.addResidentToBed("R3", "Cara", 'F', anyBed(), nurse);
    }

    private Bed findBed(String id) {
        for (Ward w : service.getCareHome().getWards())
            for (Room r : w.getRooms())
                for (Bed b : r.getBeds())
                    if (b.getId().equals(id)) return b;
        throw new AssertionError("bed not found in fixture");
    }
}
