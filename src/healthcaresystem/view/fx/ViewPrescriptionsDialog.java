package healthcaresystem.view.fx;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import healthcaresystem.model.meds.Prescription;
import healthcaresystem.model.meds.PrescriptionItem;

import java.util.List;
import java.util.stream.Collectors;

public class ViewPrescriptionsDialog {

    private final FxContext ctx;
    private final String bedId;

    // remembers context and which bed we’re showing for
    public ViewPrescriptionsDialog(FxContext ctx, String bedId) {
        this.ctx = ctx;
        this.bedId = bedId;
    }

    // shows a simple, read only list of prescriptions for the bed’s current resident
    public void show(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Prescriptions for Bed " + bedId);

        ListView<String> list = new ListView<>();
        Label status = new Label();

        try {
            // resolve resident via service rules (auth/roster enforced there)
            var patientOpt = ctx.service().viewResidentInBed(bedId, ctx.getCurrentUser());
            if (patientOpt.isEmpty()) {
                list.setPlaceholder(new Label("No resident in this bed or not authorized."));
            } else {
                var patient = patientOpt.get();

                // latest-first prescriptions for this patient
                List<Prescription> rx = ctx.service().getCareHome().getPrescriptions().stream()
                        .filter(p -> p.getPatient().getId().equalsIgnoreCase(patient.getId()))
                        .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                        .collect(Collectors.toList());

                if (rx.isEmpty()) {
                    list.setPlaceholder(new Label("No prescriptions found for " + patient.getName() + "."));
                } else {
                    list.setItems(FXCollections.observableArrayList(
                            rx.stream().map(this::formatRx).collect(Collectors.toList())
                    ));
                }

                status.setText("Patient: " + patient.getName() + " (" + patient.getId() + ")");
            }
        } catch (Exception ex) {
            list.setPlaceholder(new Label("Error: " + ex.getMessage()));
        }

        VBox root = new VBox(10, status, list);
        root.setPadding(new Insets(12));
        root.setPrefSize(560, 420);

        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }

    // renders a prescription summary and its items as multi line text
    private String formatRx(Prescription p) {
        String header = p.getId() + "  |  Dr. " + p.getDoctor().getName() +
                "  |  " + p.getCreatedAt();
        if (p.getItems().isEmpty()) return header + "\n  (no items)";

        String items = p.getItems().stream()
                .map(this::formatItem)
                .collect(Collectors.joining("\n"));

        return header + "\n" + items;
    }

    // renders a single prescription item line
    private String formatItem(PrescriptionItem it) {
        StringBuilder sb = new StringBuilder("  - ");
        sb.append(it.getMedicine());
        if (it.getDose() != null && !it.getDose().isBlank()) sb.append("  ").append(it.getDose());
        if (it.getFrequency() != null && !it.getFrequency().isBlank()) sb.append("  ").append(it.getFrequency());
        if (it.getNotes() != null && !it.getNotes().isBlank()) sb.append("  [").append(it.getNotes()).append("]");
        return sb.toString();
    }
}
