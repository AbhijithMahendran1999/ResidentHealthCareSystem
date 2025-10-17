package healthcaresystem.service;

import java.time.LocalDateTime;
import healthcaresystem.model.audit.AuditEntry;
import healthcaresystem.model.facility.CareHome;
import healthcaresystem.model.people.Staff;

// Handles creating and storing audit log entries
public class AuditService {

    private final CareHome careHome;

    public AuditService(CareHome careHome) {
        this.careHome = careHome;
    }

    // Records a new audit entry (who, what, details, success)
    public void log(Staff actor, String action, String details, boolean success) {
        careHome.getAuditLog().add(new AuditEntry(
            LocalDateTime.now(), actor, action, details, success
        ));
    }
}
