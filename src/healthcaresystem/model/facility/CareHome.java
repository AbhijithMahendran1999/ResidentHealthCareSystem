package healthcaresystem.model.facility;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import healthcaresystem.model.people.Resident;
import healthcaresystem.model.people.Staff;
import healthcaresystem.model.schedule.Roster;
import healthcaresystem.model.meds.Prescription;
import healthcaresystem.model.meds.AdministrationRecord;
import healthcaresystem.model.audit.AuditEntry;
import healthcaresystem.exception.ComplianceException;

public class CareHome implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Ward> wards = new ArrayList<>();
    private List<Staff> staff = new ArrayList<>();
    private List<Resident> residents = new ArrayList<>();
    private List<Prescription> prescriptions = new ArrayList<>();
    private List<AdministrationRecord> administrations = new ArrayList<>();
    private List<AuditEntry> auditLog = new ArrayList<>();
    private Roster roster = new Roster();

    // Getters
    public List<AuditEntry> getAuditLog() {
        if (auditLog == null) auditLog = new ArrayList<>();
        return auditLog;
    }
    public List<Prescription> getPrescriptions() {
        if (prescriptions == null) prescriptions = new ArrayList<>();
        return prescriptions;
    }
    public List<AdministrationRecord> getAdministrations() {
        if (administrations == null) administrations = new ArrayList<>();
        return administrations;
    }
    public List<Staff> getStaff() {
        if (staff == null) staff = new ArrayList<>();
        return staff;
    }
    public List<Resident> getResidents() {
        if (residents == null) residents = new ArrayList<>();
        return residents;
    }
    public List<Ward> getWards() {
        if (wards == null) wards = new ArrayList<>();
        return wards;
    }
    public Roster getRoster() {
        if (roster == null) roster = new Roster();
        return roster;
    }

    // Setters
    public void setWards(List<Ward> wards) { this.wards = wards; }
    public void setStaff(List<Staff> staff) { this.staff = staff; }
    public void setResidents(List<Resident> residents) { this.residents = residents; }
    public void setRoster(Roster roster) { this.roster = roster; }
    
    public void checkCompliance() throws ComplianceException {
        roster.ensureDailyDoctorHour();
        roster.ensureNurseShiftsAndHours();
    }
}
	