package healthcaresystem.model.meds;

import java.io.Serializable;

public class PrescriptionItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String medicine;
    private String dose;
    private String frequency; // e.g. "Once daily", "8AM and 8PM"
    private String notes;

    public PrescriptionItem(String medicine, String dose, String frequency, String notes) {
        this.medicine = medicine;
        this.dose = dose;
        this.frequency = frequency;
        this.notes = notes;
    }

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
