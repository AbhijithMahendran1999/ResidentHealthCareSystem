package healthcaresystem.model.meds;

import java.io.Serializable;
import java.time.LocalDateTime;
import healthcaresystem.model.people.Staff;

public class AdministrationRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private String prescriptionId;
    private String medicine;
    private String dose;
    private LocalDateTime time;
    private Staff administeredBy;

    public AdministrationRecord(String prescriptionId, String medicine, String dose, Staff by) {
        this.prescriptionId = prescriptionId;
        this.medicine = medicine;
        this.dose = dose;
        this.time = LocalDateTime.now();
        this.administeredBy = by;
    }

    public String getPrescriptionId() { return prescriptionId; }
    public String getMedicine() { return medicine; }
    public String getDose() { return dose; }
    public LocalDateTime getTime() { return time; }
    public Staff getAdministeredBy() { return administeredBy; }

    @Override
    public String toString() {
        return "[" + time + "] " + medicine + " " + dose + " by " + administeredBy.getName();
    }
}
