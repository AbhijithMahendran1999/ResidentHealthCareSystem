package healthcaresystem.view.fx;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import healthcaresystem.model.meds.PrescriptionItem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Dialog for doctors to attach a new prescription to a bed's resident
public class AttachPrescriptionDialog {
    private final FxContext ctx;
    private final String bedId;

    public AttachPrescriptionDialog(FxContext ctx) {
        this(ctx, null);
    }

    public AttachPrescriptionDialog(FxContext ctx, String bedId) {
        this.ctx = ctx;
        this.bedId = bedId;
    }

    // Build and show the modal dialog
    public void show(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Attach Prescription" + (bedId != null ? " (Bed " + bedId + ")" : ""));

        // Form fields for prescription items
        TextField med = new TextField();  med.setPromptText("Medicine");
        TextField dose = new TextField(); dose.setPromptText("Dose (e.g., 500mg)");
        TextField freq = new TextField(); freq.setPromptText("Frequency (e.g., 8-hourly)");
        TextField notes = new TextField(); notes.setPromptText("Notes (optional)");

        Button addItem = new Button("Add Item");
        ListView<String> itemsView = new ListView<>();
        List<PrescriptionItem> items = new ArrayList<>();

        Label patientLabel = new Label();
        Label status = new Label();

        // Prevent attaching if no bed is selected
        if (bedId == null) {
            status.setStyle("-fx-text-fill: red;");
            status.setText("No bed context. Open from a bed.");
        }

        // Add item to the list
        addItem.setOnAction(e -> {
            String m = med.getText().trim();
            if (m.isEmpty()) {
                setError(status, "Medicine is required.");
                return;
            }
            PrescriptionItem it = new PrescriptionItem(
                    m,
                    dose.getText().trim(),
                    freq.getText().trim(),
                    notes.getText().trim()
            );
            items.add(it);
            itemsView.setItems(FXCollections.observableArrayList(
                    items.stream().map(this::fmt).collect(Collectors.toList())
            ));
            med.clear(); dose.clear(); freq.clear(); notes.clear();
            status.setText("");
        });

        // Submit prescription
        Button submit = new Button("Attach");
        Button close = new Button("Close");

        submit.setOnAction(e -> {
            try {
                if (bedId == null) {
                    setError(status, "Missing bed context.");
                    return;
                }
                if (items.isEmpty()) {
                    setError(status, "Add at least one item.");
                    return;
                }
                ctx.service().attachPrescriptionToBed(bedId, items, ctx.getCurrentUser());
                setOk(status, "Prescription attached.");
                dialog.close();
            } catch (Exception ex) {
                setError(status, ex.getMessage());
            }
        });

        close.setOnAction(e -> dialog.close());

        // Layout setup
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(12));

        int r = 0;
        form.add(new Label("Medicine:"),  0, r); form.add(med,  1, r++);
        form.add(new Label("Dose:"),      0, r); form.add(dose, 1, r++);
        form.add(new Label("Frequency:"), 0, r); form.add(freq, 1, r++);
        form.add(new Label("Notes:"),     0, r); form.add(notes,1, r++);
        form.add(addItem,                 1, r++);

        HBox actions = new HBox(10, submit, close);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(10,
                new Label(bedId == null ? "No bed selected." : "Bed: " + bedId),
                patientLabel,
                form,
                new Label("Items:"),
                itemsView,
                status,
                actions
        );

        root.setPadding(new Insets(14));
        root.setPrefWidth(560);

        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }

    // Format prescription item display text
    private String fmt(PrescriptionItem it) {
        StringBuilder sb = new StringBuilder();
        sb.append(it.getMedicine());
        if (!it.getDose().isBlank()) sb.append(" | ").append(it.getDose());
        if (!it.getFrequency().isBlank()) sb.append(" | ").append(it.getFrequency());
        if (!it.getNotes().isBlank()) sb.append(" | [").append(it.getNotes()).append("]");
        return sb.toString();
    }

    private void setError(Label l, String msg) {
        l.setText(msg);
        l.setStyle("-fx-text-fill: red;");
    }

    private void setOk(Label l, String msg) {
        l.setText(msg);
        l.setStyle("-fx-text-fill: green;");
    }
}
