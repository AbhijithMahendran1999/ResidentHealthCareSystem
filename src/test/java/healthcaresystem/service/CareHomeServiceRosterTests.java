package healthcaresystem.service;

import static org.junit.Assert.*;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;

import healthcaresystem.model.facility.CareHome;
import healthcaresystem.model.people.*;
import healthcaresystem.model.schedule.ShiftType;

public class CareHomeServiceRosterTests {

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
    public void manager_canAssignShift() {
        service.assignOrReplaceShift("nurse", LocalDate.now(), ShiftType.DAY, false, manager);
        long countToday = service.getCareHome().getRoster().getShifts().stream()
                .filter(s -> s.getStaff().equals(nurse) && s.getDay().equals(LocalDate.now()))
                .count();
        assertEquals(1, countToday);
    }

    @Test(expected = IllegalStateException.class)
    public void nurse_cannotExceed8hInDay_usingAddMode() {
        service.assignOrReplaceShift("nurse", LocalDate.now(), ShiftType.DAY, false, manager);
        // adding a second shift same day (EVE) without replace => should throw
        service.assignOrReplaceShift("nurse", LocalDate.now(), ShiftType.EVE, false, manager);
    }

    @Test
    public void replaceMode_allowsChangingShiftSameDay() {
        service.assignOrReplaceShift("nurse", LocalDate.now(), ShiftType.DAY, false, manager);
        service.assignOrReplaceShift("nurse", LocalDate.now(), ShiftType.EVE, true, manager);
        var shifts = service.getCareHome().getRoster().getShifts();
        long eve = shifts.stream().filter(s -> s.getStaff().equals(nurse)
                && s.getDay().equals(LocalDate.now())
                && s.getType()==ShiftType.EVE).count();
        assertEquals(1, eve);
    }
}
