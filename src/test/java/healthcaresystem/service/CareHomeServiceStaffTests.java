package healthcaresystem.service;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import healthcaresystem.model.facility.CareHome;
import healthcaresystem.model.people.*;
import healthcaresystem.exception.UnauthorizedActionException;

public class CareHomeServiceStaffTests {

    private CareHomeService service;
    private Manager manager;
    private Nurse nurse;

    @Before
    public void setUp() {
        service = new CareHomeService(new CareHome());
        service.ensureDefaultFacility();

        manager = new Manager("M1", "Mgr", "mgr");
        manager.setPassword("pw");
        service.addStaff(manager);

        nurse = new Nurse("N1", "Nurse", "nurse");
        nurse.setPassword("pw");
        service.addStaff(nurse);
    }

    @Test
    public void manager_canAddDoctor() {
        service.addStaffWithCredentials("DOCTOR", "D1", "Doc", "doc1", "p", manager);
        assertTrue(service.getCareHome().getStaff().stream().anyMatch(s -> "D1".equals(s.getId())));
    }

    @Test(expected = UnauthorizedActionException.class)
    public void nurse_cannotAddStaff() {
        service.addStaffWithCredentials("DOCTOR", "D2", "Doc2", "doc2", "p", nurse);
    }

    @Test
    public void manager_canModifyPassword() {
        service.modifyStaffPassword("nurse", "new", manager);
        Staff s = service.getCareHome().getStaff().stream()
                .filter(x -> x.getUsername().equals("nurse")).findFirst().get();
        assertEquals("new", s.getPassword());
    }

    @Test(expected = UnauthorizedActionException.class)
    public void nonManager_cannotModifyPassword() {
        service.modifyStaffPassword("mgr", "x", nurse);
    }
}
