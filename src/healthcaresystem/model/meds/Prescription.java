package healthcaresystem.model.meds;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import healthcaresystem.model.people.Doctor;
import healthcaresystem.model.people.Resident;

public class Prescription implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;                        // unique prescription ID
    private Resident patient;                 // resident this prescription belongs to
    private Doctor doctor;                    // doctor who created the prescription
    private LocalDateTime createdAt;          // creation timestamp
    private final List<PrescriptionItem> items = new ArrayList<>();  // list of prescribed medicines

    public Prescription(String id, Resident patient, Doctor doctor) {
        this.id = id;
        this.patient = patient;
        this.doctor = doctor;
        this.createdAt = LocalDateTime.now();
    }

    // ---------- Getters ----------
    public String getId() { return id; }
    public Resident getPatient() { return patient; }
    public Doctor getDoctor() { return doctor; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<PrescriptionItem> getItems() { return items; }

    // ---------- Methods ----------
    public void addItem(PrescriptionItem item) { items.add(item); }

    @Override
    public String toString() {
        return "Prescription " + id + " for " + patient.getName() + " by Dr. " + doctor.getName()
                + " (" + items.size() + " items)";
    }
}
