package healthcaresystem.model.schedule;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.LocalDate;

import org.junit.Test;

import healthcaresystem.exception.ComplianceException;
import healthcaresystem.model.facility.CareHome;
import healthcaresystem.model.people.Doctor;
import healthcaresystem.model.people.Nurse;

public class RosterTests {

    @Test
    public void weeklyRoster_meetsAllRules_noException() {
        CareHome ch = new CareHome();
        Nurse n1 = new Nurse("N1", "Nurse One", "n1");
        Nurse n2 = new Nurse("N2", "Nurse Two", "n2");
        Doctor d1 = new Doctor("D1", "Doc One", "d1");
        ch.getStaff().add(n1); 
        ch.getStaff().add(n2); 
        ch.getStaff().add(d1);

        LocalDate start = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate day = start.plusDays(i);
            ch.getRoster().addShift(new Shift(n1, day, ShiftType.DAY));  // nurse DAY
            ch.getRoster().addShift(new Shift(n2, day, ShiftType.EVE));  // nurse EVE
            ch.getRoster().addShift(new Shift(d1, day, ShiftType.DAY));  // doctor present
        }

        try {
            ch.checkCompliance();
        } catch (Exception e) {
            fail("Should not throw, but got: " + e.getMessage());
        }
    }

    @Test
    public void missingDoctorOnAnyDay_throwsComplianceException() {
        CareHome ch = new CareHome();
        Nurse n1 = new Nurse("N1", "Nurse One", "n1");
        Nurse n2 = new Nurse("N2", "Nurse Two", "n2");
        Doctor d1 = new Doctor("D1", "Doc One", "d1");
        ch.getStaff().add(n1); 
        ch.getStaff().add(n2); 
        ch.getStaff().add(d1);

        LocalDate start = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate day = start.plusDays(i);
            ch.getRoster().addShift(new Shift(n1, day, ShiftType.DAY));
            ch.getRoster().addShift(new Shift(n2, day, ShiftType.EVE));
            if (i != 3) { // skip doctor on day 3
                ch.getRoster().addShift(new Shift(d1, day, ShiftType.DAY));
            }
        }

        boolean threw = false;
        try {
            ch.checkCompliance();
        } catch (ComplianceException ce) {
            threw = true;
        }
        assertTrue("Expected ComplianceException for missing doctor", threw);
    }

    @Test
    public void sameNurseAssignedDayAndEve_sameDay_throwsComplianceException() {
        CareHome ch = new CareHome();
        Nurse n1 = new Nurse("N1", "Nurse One", "n1");
        Nurse n2 = new Nurse("N2", "Nurse Two", "n2");
        Doctor d1 = new Doctor("D1", "Doc One", "d1");
        ch.getStaff().add(n1); 
        ch.getStaff().add(n2); 
        ch.getStaff().add(d1);

        LocalDate start = LocalDate.now();
        // violation on day 0, n1 gets both shifts (16houts)
        ch.getRoster().addShift(new Shift(n1, start, ShiftType.DAY));
        ch.getRoster().addShift(new Shift(n1, start, ShiftType.EVE));
        ch.getRoster().addShift(new Shift(d1, start, ShiftType.DAY));

        // remaining days are compliant
        for (int i = 1; i < 7; i++) {
            LocalDate day = start.plusDays(i);
            ch.getRoster().addShift(new Shift(n1, day, ShiftType.DAY));
            ch.getRoster().addShift(new Shift(n2, day, ShiftType.EVE));
            ch.getRoster().addShift(new Shift(d1, day, ShiftType.DAY));
        }

        boolean threw = false;
        try {
            ch.checkCompliance();
        } catch (ComplianceException ce) {
            threw = true;
        }
        assertTrue("Expected ComplianceException for nurse over 8h/day", threw);
    }
}
