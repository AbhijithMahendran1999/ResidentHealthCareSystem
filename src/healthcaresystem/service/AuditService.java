package healthcaresystem.service;

import java.time.LocalDateTime;

import healthcaresystem.model.audit.AuditEntry;
import healthcaresystem.model.facility.CareHome;
import healthcaresystem.model.people.Staff;

public class AuditService {
    private final CareHome careHome;

    public AuditService(CareHome careHome) {
        this.careHome = careHome;
    }

    public void log(Staff actor, String action, String details, boolean success) {
        String staffId = (actor == null) ? "ANONYMOUS" : actor.getId();
        careHome.getAuditLog().add(new AuditEntry(LocalDateTime.now(), staffId, action, details, success));
    }

    // Overload to log with custom timestamp if ever needed
    public void log(Staff actor, String action, String details, boolean success, LocalDateTime when) {
        String staffId = (actor == null) ? "ANONYMOUS" : actor.getId();
        careHome.getAuditLog().add(new AuditEntry(when, staffId, action, details, success));
    }
}
