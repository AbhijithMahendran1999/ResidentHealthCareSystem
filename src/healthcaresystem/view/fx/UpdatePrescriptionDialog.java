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
import healthcaresystem.model.meds.PrescriptionItem;

import java.util.List;
import java.util.stream.Collectors;

public class UpdatePrescriptionDialog {
    private final FxContext ctx;
    private final String bedId;

    // constructor for dashboard button (no preselected bed)
    public UpdatePrescriptionDialog(FxContext ctx) {
        this(ctx, null);
    }

    // constructor for bed-aware launch (preselected bed)
    public UpdatePrescriptionDialog(FxContext ctx, String bedId) {
        this.ctx = ctx;
        this.bedId = bedId;
    }

    // show dialog and wire actions
    public void show(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Update Prescription" + (bedId != null ? " (Bed " + bedId + ")" : ""));

        Label status = new Label();

        // prescription picker (filtered by resident in bed)
        ComboBox<Prescription> rxCombo = new ComboBox<>();
        rxCombo.setPrefWidth(420);

        // action type (add/edit/remove)
        ComboBox<String> actionCombo = new ComboBox<>(
                FXCollections.observableArrayList("ADD", "EDIT", "REMOVE")
        );
        actionCombo.getSelectionModel().selectFirst();

        // item index for EDIT/REMOVE
        ComboBox<Integer> itemIndexCombo = new ComboBox<>();

        // editable fields for ADD/EDIT
        TextField med   = new TextField(); med.setPromptText("Medicine");
        TextField dose  = new TextField(); dose.setPromptText("Dose (e.g., 500mg)");
        TextField freq  = new TextField(); freq.setPromptText("Frequency (e.g., 8-hourly)");
        TextField notes = new TextField(); notes.setPromptText("Notes (optional)");

        // simple rows/groups
        HBox actionRow = new HBox(8, new Label("Action:"), actionCombo);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        HBox itemRow = new HBox(8, new Label("Item #:"), itemIndexCombo);
        itemRow.setAlignment(Pos.CENTER_LEFT);

        GridPane fields = new GridPane();
        fields.setHgap(10); fields.setVgap(10);
        fields.add(new Label("Medicine:"), 0, 0); fields.add(med,   1, 0);
        fields.add(new Label("Dose:"),     0, 1); fields.add(dose,  1, 1);
        fields.add(new Label("Frequency:"),0, 2); fields.add(freq,  1, 2);
        fields.add(new Label("Notes:"),    0, 3); fields.add(notes, 1, 3);

        // buttons
        Button apply = new Button("Apply");
        Button close = new Button("Close");
        HBox buttons = new HBox(10, apply, close);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        // load prescriptions for the resident in bed
        try {
            if (bedId == null) {
                setError(status, "No bed context. Open from a bed.");
            } else {
                var patientOpt = ctx.service().viewResidentInBed(bedId, ctx.getCurrentUser());
                if (patientOpt.isEmpty()) {
                    setError(status, "No resident in this bed or not authorized.");
                } else {
                    var patient = patientOpt.get();
                    List<Prescription> rx = ctx.service().getCareHome().getPrescriptions().stream()
                            .filter(p -> p.getPatient().getId().equalsIgnoreCase(patient.getId()))
                            .sorted((a,b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                            .collect(Collectors.toList());

                    rxCombo.setItems(FXCollections.observableArrayList(rx));
                    rxCombo.setCellFactory(cb -> new ListCell<>() {
                        @Override protected void updateItem(Prescription p, boolean empty) {
                            super.updateItem(p, empty);
                            setText(empty || p == null ? "" : p.getId() + " | Dr. " + p.getDoctor().getName());
                        }
                    });
                    rxCombo.setButtonCell(new ListCell<>() {
                        @Override protected void updateItem(Prescription p, boolean empty) {
                            super.updateItem(p, empty);
                            setText(empty || p == null ? "" : p.getId() + " | Dr. " + p.getDoctor().getName());
                        }
                    });
                    if (!rx.isEmpty()) rxCombo.getSelectionModel().selectFirst();
                }
            }
        } catch (Exception ex) {
            setError(status, ex.getMessage());
        }

        // refresh item index list when prescription changes
        rxCombo.valueProperty().addListener((obs, old, val) -> {
            itemIndexCombo.getItems().clear();
            if (val != null && !val.getItems().isEmpty()) {
                var idxs = java.util.stream.IntStream.range(0, val.getItems().size())
                        .map(i -> i + 1).boxed().collect(Collectors.toList());
                itemIndexCombo.setItems(FXCollections.observableArrayList(idxs));
                itemIndexCombo.getSelectionModel().selectFirst();
            }
        });

        // toggle visible controls by action
        Runnable toggle = () -> {
            String act = actionCombo.getValue();
            boolean isAdd  = "ADD".equals(act);
            boolean isEdit = "EDIT".equals(act);
            boolean isRem  = "REMOVE".equals(act);

            itemRow.setVisible(isEdit || isRem);
            itemRow.setManaged(isEdit || isRem);

            fields.setVisible(isAdd || isEdit);
            fields.setManaged(isAdd || isEdit);
        };
        actionCombo.valueProperty().addListener((o, a, b) -> toggle.run());
        toggle.run();

        // apply requested change
        apply.setOnAction(e -> {
            try {
                Prescription sel = rxCombo.getValue();
                if (sel == null) { setError(status, "Select a prescription."); return; }
                String act = actionCombo.getValue();

                switch (act) {
                    case "ADD": {
                        String m = med.getText().trim();
                        if (m.isEmpty()) { setError(status, "Medicine is required."); return; }
                        PrescriptionItem item = new PrescriptionItem(
                                m, dose.getText().trim(),
                                freq.getText().trim(),
                                notes.getText().trim());
                        ctx.service().addPrescriptionItem(sel.getId(), item, ctx.getCurrentUser());
                        setOk(status, "Item added.");
                        break;
                    }
                    case "EDIT": {
                        Integer idx1 = itemIndexCombo.getValue();
                        if (idx1 == null) { setError(status, "Choose an item #."); return; }
                        int itemIndex = idx1 - 1;

                        // keep old values if left blank
                        PrescriptionItem cur = sel.getItems().get(itemIndex);
                        String newMed  = med.getText().trim().isEmpty()  ? cur.getMedicine()  : med.getText().trim();
                        String newDose = dose.getText().trim().isEmpty() ? cur.getDose()      : dose.getText().trim();
                        String newFreq = freq.getText().trim().isEmpty() ? cur.getFrequency() : freq.getText().trim();
                        String newNote = notes.getText().trim().isEmpty()? cur.getNotes()     : notes.getText().trim();

                        ctx.service().editPrescriptionItem(sel.getId(), itemIndex,
                                new PrescriptionItem(newMed, newDose, newFreq, newNote),
                                ctx.getCurrentUser());
                        setOk(status, "Item edited.");
                        break;
                    }
                    case "REMOVE": {
                        Integer idx2 = itemIndexCombo.getValue();
                        if (idx2 == null) { setError(status, "Choose an item #."); return; }
                        int itemIndex = idx2 - 1;
                        ctx.service().removePrescriptionItem(sel.getId(), itemIndex, ctx.getCurrentUser());
                        setOk(status, "Item removed.");
                        break;
                    }
                }

                // refresh indices after mutating the prescription
                var now = ctx.service().getCareHome().getPrescriptions().stream()
                        .filter(p -> p.getId().equals(sel.getId()))
                        .findFirst().orElse(sel);
                rxCombo.getSelectionModel().select(now);
                itemIndexCombo.getItems().clear();
                if (!now.getItems().isEmpty()) {
                    var idxs = java.util.stream.IntStream.range(0, now.getItems().size())
                            .map(i -> i + 1).boxed().collect(Collectors.toList());
                    itemIndexCombo.setItems(FXCollections.observableArrayList(idxs));
                    itemIndexCombo.getSelectionModel().selectFirst();
                }
            } catch (Exception ex) {
                setError(status, ex.getMessage());
            }
        });

        // close dialog
        close.setOnAction(e -> dialog.close());

        // layout
        GridPane top = new GridPane();
        top.setHgap(10); top.setVgap(10); top.setPadding(new Insets(12));
        int r = 0;
        top.add(new Label("Prescription:"), 0, r); top.add(rxCombo, 1, r++);
        top.add(actionRow, 1, r++);
        top.add(itemRow,   1, r++);
        top.add(fields,    1, r++);

        VBox root = new VBox(10, top, status, buttons);
        root.setPadding(new Insets(14));
        root.setPrefWidth(620);

        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }

    // set red status
    private void setError(Label l, String msg){ l.setText(msg); l.setStyle("-fx-text-fill: red;"); }

    // set green status
    private void setOk   (Label l, String msg){ l.setText(msg); l.setStyle("-fx-text-fill: green;"); }
}
