package healthcaresystem.view.fx;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import healthcaresystem.model.meds.Prescription;
import healthcaresystem.model.people.Staff;

import java.util.List;
import java.util.stream.Collectors;

// Dialog for a nurse to administer a medicine for the patient in a given bed
public class AdministerMedicationDialog {
    private final FxContext ctx;
    private final String bedId;

    public AdministerMedicationDialog(FxContext ctx, String bedId) {
        this.ctx = ctx;
        this.bedId = bedId;
    }

    // Build and show the modal dialog
    public void show(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Administer Medication (Bed " + bedId + ")");

        Label info = new Label("Select a prescription and item to administer.");

        ComboBox<Prescription> rxCombo = new ComboBox<>();
        ComboBox<Integer> itemCombo = new ComboBox<>();

        TextField doseOverride = new TextField();
        doseOverride.setPromptText("Dose (optional override)");

        Label status = new Label();
        Button give = new Button("Administer");
        Button close = new Button("Close");

        // Load prescriptions for current bed's resident
        try {
            var patientOpt = ctx.service().viewResidentInBed(bedId, ctx.getCurrentUser());
            if (patientOpt.isEmpty()) {
                status.setText("No resident in this bed or not authorized.");
            } else {
                var patient = patientOpt.get();
                List<Prescription> list = ctx.service().getCareHome().getPrescriptions().stream()
                        .filter(p -> p.getPatient().getId().equalsIgnoreCase(patient.getId()))
                        .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                        .collect(Collectors.toList());

                rxCombo.setItems(FXCollections.observableArrayList(list));

                rxCombo.setCellFactory(cb -> new ListCell<>() {
                    @Override
                    protected void updateItem(Prescription p, boolean empty) {
                        super.updateItem(p, empty);
                        setText(empty || p == null ? "" : p.getId() + " | Dr. " + p.getDoctor().getName());
                    }
                });

                rxCombo.setButtonCell(new ListCell<>() {
                    @Override
                    protected void updateItem(Prescription p, boolean empty) {
                        super.updateItem(p, empty);
                        setText(empty || p == null ? "" : p.getId() + " | Dr. " + p.getDoctor().getName());
                    }
                });
            }
        } catch (Exception ex) {
            status.setText("Error: " + ex.getMessage());
        }

        // Populate item numbers (1..n) when a prescription is picked
        rxCombo.valueProperty().addListener((obs, old, val) -> {
            itemCombo.getItems().clear();
            if (val != null && !val.getItems().isEmpty()) {
                var idxs = java.util.stream.IntStream.range(0, val.getItems().size())
                        .map(i -> i + 1)
                        .boxed()
                        .collect(Collectors.toList());
                itemCombo.setItems(FXCollections.observableArrayList(idxs));
                itemCombo.getSelectionModel().selectFirst();
            }
        });

        // Attempt to administer the selected dose
        give.setOnAction(e -> {
            try {
                Prescription rx = rxCombo.getValue();
                Integer itemNum = itemCombo.getValue();

                if (rx == null || itemNum == null) {
                    status.setText("Choose a prescription and an item.");
                    return;
                }

                int itemIndex = itemNum - 1;
                String dose = doseOverride.getText().trim();
                Staff actor = ctx.getCurrentUser();

                ctx.service().administerDose(bedId, rx.getId(), itemIndex, dose, actor);
                status.setStyle("-fx-text-fill: green;");
                status.setText("Dose administered.");
            } catch (Exception ex) {
                status.setStyle("-fx-text-fill: red;");
                status.setText("Error: " + ex.getMessage());
            }
        });

        // Close and return to dashboard
        close.setOnAction(e -> dialog.close());

        // Form layout
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(12));

        int r = 0;
        form.add(new Label("Prescription:"), 0, r); form.add(rxCombo,      1, r++);
        form.add(new Label("Item #:"),       0, r); form.add(itemCombo,    1, r++);
        form.add(new Label("Dose override:"),0, r); form.add(doseOverride, 1, r++);

        HBox buttons = new HBox(10, give, close);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(10, info, form, status, buttons);
        root.setPadding(new Insets(14));
        root.setPrefWidth(520);

        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }
}
