package healthcaresystem.model.audit;

import java.io.Serializable;
import java.time.LocalDateTime;

import healthcaresystem.model.people.Staff;

public class AuditEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private final LocalDateTime time;   // timestamp of the action
    private final Staff actor;          // staff member who performed it (null for system)
    private final String action;        // action code (e.g., LOGIN, ADD_RESIDENT)
    private final String details;       // descriptive info about the action
    private final boolean success;      // true if successful, false if denied

    public AuditEntry(LocalDateTime time, Staff actor, String action, String details, boolean success) {
        this.time = time;
        this.actor = actor;
        this.action = action;
        this.details = details;
        this.success = success;
    }

    // getters for all fields
    public LocalDateTime getTime() { return time; }
    public Staff getActor() { return actor; }
    public String getAction() { return action; }
    public String getDetails() { return details; }
    public boolean isSuccess() { return success; }

    // formatted summary for logs and console
    @Override 
    public String toString() {
        return "[" + time + "] " +
               (actor == null ? "(system)" : actor.getUsername()) + " " +
               action + " " + details + " " +
               (success ? "OK" : "DENIED");
    }
}
