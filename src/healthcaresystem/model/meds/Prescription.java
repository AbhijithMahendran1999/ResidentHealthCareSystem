package healthcaresystem.model.meds;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import healthcaresystem.model.people.Doctor;
import healthcaresystem.model.people.Resident;

public class Prescription implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private Resident patient;
    private Doctor doctor;
    private LocalDateTime createdAt;
    private final List<PrescriptionItem> items = new ArrayList<>();

    public Prescription(String id, Resident patient, Doctor doctor) {
        this.id = id;
        this.patient = patient;
        this.doctor = doctor;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public Resident getPatient() { return patient; }
    public Doctor getDoctor() { return doctor; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<PrescriptionItem> getItems() { return items; }

    public void addItem(PrescriptionItem item) { items.add(item); }

    @Override
    public String toString() {
        return "Prescription " + id + " for " + patient.getName() + " by Dr. " + doctor.getName()
                + " (" + items.size() + " items)";
    }
}
