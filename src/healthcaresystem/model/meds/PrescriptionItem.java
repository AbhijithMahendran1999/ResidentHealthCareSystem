package healthcaresystem.model.meds;

import java.io.Serializable;

public class PrescriptionItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String medicine;     // medicine name
    private String dose;         // prescribed dose (e.g., "500mg")
    private String frequency;    // timing/frequency (e.g., "Once daily", "8AM and 8PM")
    private String notes;        // optional notes from the doctor

    public PrescriptionItem(String medicine, String dose, String frequency, String notes) {
        this.medicine = medicine;
        this.dose = dose;
        this.frequency = frequency;
        this.notes = notes;
    }

    // ---------- Getters ----------
    public String getMedicine() { return medicine; }
    public String getDose() { return dose; }
    public String getFrequency() { return frequency; }
    public String getNotes() { return notes; }

    @Override
    public String toString() {
        return medicine + " " + dose + " (" + frequency + ")" +
               (notes == null || notes.isEmpty() ? "" : " - " + notes);
    }
}
