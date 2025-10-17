package healthcaresystem.view.fx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;

import healthcaresystem.model.schedule.ShiftType;

// Dialog to assign or replace a staff shift
public class AssignShiftDialog {

    private final FxContext ctx;
    private final Runnable onSuccess;

    public AssignShiftDialog(FxContext ctx, Runnable onSuccess) {
        this.ctx = ctx;
        this.onSuccess = onSuccess;
    }

    // Build and show the modal
    public void show(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Allocate / Modify Shift");

        // Inputs
        TextField staffKey = new TextField();
        staffKey.setPromptText("Staff username or ID");

        DatePicker datePicker = new DatePicker(LocalDate.now());

        ComboBox<ShiftType> typeBox = new ComboBox<>();
        typeBox.getItems().addAll(ShiftType.DAY, ShiftType.EVE);
        typeBox.setValue(ShiftType.DAY);

        CheckBox replace = new CheckBox("Replace existing shift on this date");

        // Status + actions
        Label status = new Label();
        Button save = new Button("Save");
        Button cancel = new Button("Cancel");

        // Save shift (assign or replace)
        save.setOnAction(e -> {
            String key = staffKey.getText().trim();
            LocalDate day = datePicker.getValue();
            ShiftType t = typeBox.getValue();

            if (key.isEmpty() || day == null || t == null) {
                setError(status, "Please fill all fields.");
                return;
            }

            try {
                ctx.service().assignOrReplaceShift(key, day, t, replace.isSelected(), ctx.getCurrentUser());
                setOk(status, (replace.isSelected() ? "Replaced " : "Assigned ") + t + " on " + day + ".");
                if (onSuccess != null) onSuccess.run();
                dialog.close();
            } catch (Exception ex) {
                setError(status, ex.getMessage());
            }
        });

        // Close dialog
        cancel.setOnAction(e -> dialog.close());

        // Layout
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(15));

        form.add(new Label("Staff (username/ID):"), 0, 0);
        form.add(staffKey,                          1, 0);
        form.add(new Label("Date:"),                0, 1);
        form.add(datePicker,                        1, 1);
        form.add(new Label("Shift type:"),          0, 2);
        form.add(typeBox,                           1, 2);
        form.add(replace,                           1, 3);

        HBox buttons = new HBox(10, save, cancel);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(12, form, status, buttons);
        root.setPadding(new Insets(14));

        dialog.setScene(new Scene(root, 440, 260));
        dialog.showAndWait();
    }

    // Set error text styling
    private void setError(Label l, String msg) {
        l.setText(msg);
        l.setStyle("-fx-text-fill: red;");
    }

    // Set success text styling
    private void setOk(Label l, String msg) {
        l.setText(msg);
        l.setStyle("-fx-text-fill: green;");
    }
}
