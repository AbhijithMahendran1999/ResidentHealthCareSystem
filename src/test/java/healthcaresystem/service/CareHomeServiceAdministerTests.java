package healthcaresystem.service;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import healthcaresystem.model.facility.CareHome;
import healthcaresystem.model.meds.PrescriptionItem;
import healthcaresystem.model.people.*;
import healthcaresystem.model.schedule.ShiftType;

public class CareHomeServiceAdministerTests {

    private CareHomeService service;
    private Manager manager;
    private Doctor doctor;
    private Nurse nurse;
    private String bedId;
    private String rxId;

    @Before
    public void setup() {
        service = new CareHomeService(new CareHome());
        service.ensureDefaultFacility();

        manager = new Manager("M1", "Mgr", "mgr"); manager.setPassword("pw"); service.addStaff(manager);
        doctor  = new Doctor ("D1", "Doc", "doc"); doctor.setPassword("pw");  service.addStaff(doctor);
        nurse   = new Nurse  ("N1", "Nur", "nur"); nurse.setPassword("pw");   service.addStaff(nurse);

        bedId = service.getCareHome().getWards().get(0).getRooms().get(0).getBeds().get(0).getId();
        service.addResidentToBed("R1", "Alice", 'F', bedId, manager);

        var today = LocalDate.now();
        service.assignOrReplaceShift("doc", today, ShiftType.DAY, true, manager);
        service.assignOrReplaceShift("nur", today, ShiftType.DAY, true, manager);

        rxId = service.attachPrescriptionToBed(bedId,
                List.of(new PrescriptionItem("Amox", "500mg", "8-hourly", "")),
                doctor).getId();
    }

}
