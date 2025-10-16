package healthcaresystem.model.audit;

import java.io.Serializable;
import java.time.LocalDateTime;

public class AuditEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private final LocalDateTime when;
    private final String staffId;   // who did it (or attempted it)
    private final String action;    // e.g. "ATTACH_PRESCRIPTION", "ADD_STAFF"
    private final String details;   // free text details
    private final boolean success;  // true if completed, false if denied/failed

    public AuditEntry(LocalDateTime when, String staffId, String action, String details, boolean success) {
        this.when = when;
        this.staffId = staffId;
        this.action = action;
        this.details = details;
        this.success = success;
    }

    public LocalDateTime getWhen()   { return when; }
    public String getStaffId()       { return staffId; }
    public String getAction()        { return action; }
    public String getDetails()       { return details; }
    public boolean isSuccess()       { return success; }

    @Override public String toString() {
        return "[" + when + "] " + action + " by " + staffId + " : "
                + (success ? "SUCCESS " : "DENIED ") + details;
    }
}
