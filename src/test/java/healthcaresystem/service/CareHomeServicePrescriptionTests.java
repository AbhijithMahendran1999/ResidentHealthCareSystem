package healthcaresystem.service;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import healthcaresystem.model.facility.*;
import healthcaresystem.model.meds.*;
import healthcaresystem.model.people.*;
import healthcaresystem.model.schedule.ShiftType;
import healthcaresystem.exception.UnauthorizedActionException;

public class CareHomeServicePrescriptionTests {

    private CareHomeService service;
    private Manager manager;
    private Doctor doctor;
    private Nurse nurse;
    private String bedId;

    @Before
    public void setup() {
        service = new CareHomeService(new CareHome());
        service.ensureDefaultFacility();

        manager = new Manager("M1", "Mgr", "mgr"); manager.setPassword("pw"); service.addStaff(manager);
        doctor  = new Doctor ("D1", "Doc", "doc");  doctor.setPassword("pw");  service.addStaff(doctor);
        nurse   = new Nurse  ("N1", "Nur", "nur");  nurse.setPassword("pw");   service.addStaff(nurse);

        // resident + bed
        bedId = service.getCareHome().getWards().get(0).getRooms().get(0).getBeds().get(0).getId();
        service.addResidentToBed("R1", "Alice", 'F', bedId, manager);

        // roster today so doctor/nurse are "rostered now"
        var day = LocalDate.now();
        service.assignOrReplaceShift("doc", day, ShiftType.DAY, true, manager);
        service.assignOrReplaceShift("nur", day, ShiftType.DAY, true, manager);
    }

    @Test
    public void doctor_canAttachPrescriptionToBed() {
        var p = service.attachPrescriptionToBed(bedId,
                List.of(new PrescriptionItem("Amox", "500mg", "8-hourly", "")),
                doctor);
        assertNotNull(p);
        assertEquals("R1", p.getPatient().getId());
        assertEquals(1, p.getItems().size());
    }

    @Test(expected = UnauthorizedActionException.class)
    public void nurse_cannotAttachPrescription() {
        service.attachPrescriptionToBed(bedId,
                List.of(new PrescriptionItem("Ibuprofen", "200mg", "PRN", "")),
                nurse);
    }

    @Test
    public void doctor_addEditRemoveItem() {
        var p = service.attachPrescriptionToBed(bedId,
                List.of(new PrescriptionItem("Amox", "500mg", "8-hourly", "")),
                doctor);

        service.addPrescriptionItem(p.getId(),
                new PrescriptionItem("Panadol", "1g", "12-hourly", "night"),
                doctor);
        assertEquals(2, p.getItems().size());

        service.editPrescriptionItem(p.getId(), 0,
                new PrescriptionItem("Amox", "250mg", "8-hourly", ""), doctor);
        assertEquals("250mg", p.getItems().get(0).getDose());

        service.removePrescriptionItem(p.getId(), 1, doctor);
        assertEquals(1, p.getItems().size());
    }
}
