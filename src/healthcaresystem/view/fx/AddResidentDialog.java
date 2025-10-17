package healthcaresystem.view.fx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

// Dialog for manager to add a new resident to a bed
public class AddResidentDialog {

    private final FxContext ctx;
    private final Runnable onAdded; // optional refresh callback

    public AddResidentDialog(FxContext ctx, Runnable onAdded) {
        this.ctx = ctx;
        this.onAdded = onAdded;
    }

    // Show generic dialog (bed not prefilled)
    public void show(Stage owner) {
        buildAndShow(owner, null, false);
    }

    // Overload: show with prefilled bed ID (used from specific bed)
    public void show(Stage owner, String prefillBedId) {
        buildAndShow(owner, prefillBedId, true);
    }

    // Builds and displays the dialog UI
    private void buildAndShow(Stage owner, String prefillBedId, boolean lockBedField) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add Resident to Vacant Bed");

        // Input fields
        TextField id = new TextField();        id.setPromptText("Resident ID");
        TextField name = new TextField();      name.setPromptText("Resident Name");
        TextField gender = new TextField();    gender.setPromptText("Gender (M/F)");
        TextField bed = new TextField();       bed.setPromptText("Bed ID (e.g., W1-R1-B1)");

        // Prefill and lock if needed
        if (prefillBedId != null) {
            bed.setText(prefillBedId);
            if (lockBedField) bed.setDisable(true);
        }

        Label status = new Label();

        Button add = new Button("Add");
        Button cancel = new Button("Cancel");

        // Add button action
        add.setOnAction(e -> {
            try {
                String rid = id.getText().trim();
                String rname = name.getText().trim();
                String gText = gender.getText().trim().toUpperCase();
                char g = gText.isEmpty() ? 'M' : gText.charAt(0);
                String bedId = bed.getText().trim();

                if (rid.isEmpty() || rname.isEmpty() || bedId.isEmpty()) {
                    setError(status, "ID, name, and bed are required.");
                    return;
                }

                ctx.service().addResidentToBed(rid, rname, g, bedId, ctx.getCurrentUser());
                setOk(status, "Resident added to " + bedId);
                if (onAdded != null) onAdded.run();
                dialog.close();

            } catch (Exception ex) {
                setError(status, ex.getMessage());
            }
        });

        cancel.setOnAction(e -> dialog.close());

        // Form layout
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(15));

        int r = 0;
        form.add(new Label("Resident ID:"), 0, r); form.add(id, 1, r++);
        form.add(new Label("Name:"),        0, r); form.add(name, 1, r++);
        form.add(new Label("Gender (M/F):"),0, r); form.add(gender, 1, r++);
        form.add(new Label("Bed ID:"),      0, r); form.add(bed, 1, r++);

        // Buttons
        HBox buttons = new HBox(10, add, cancel);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(12, form, status, buttons);
        root.setPadding(new Insets(14));

        dialog.setScene(new Scene(root, 480, 260));
        dialog.showAndWait();
    }

    // Helper for showing error message
    private void setError(Label l, String msg) {
        l.setText(msg);
        l.setStyle("-fx-text-fill: red;");
    }

    // Helper for success message
    private void setOk(Label l, String msg) {
        l.setText(msg);
        l.setStyle("-fx-text-fill: green;");
    }
}
