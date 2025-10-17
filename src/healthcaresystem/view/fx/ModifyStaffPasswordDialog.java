package healthcaresystem.view.fx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ModifyStaffPasswordDialog {

    private final FxContext ctx;

    public ModifyStaffPasswordDialog(FxContext ctx) {
        this.ctx = ctx;
    }

    // show password modification dialog for manager
    public void show(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Modify Staff Password");

        // form fields
        TextField keyField = new TextField();
        keyField.setPromptText("Username or Staff ID");

        PasswordField newPass = new PasswordField();
        newPass.setPromptText("New password");

        PasswordField confirm = new PasswordField();
        confirm.setPromptText("Confirm password");

        Label status = new Label();

        Button save = new Button("Save");
        Button cancel = new Button("Cancel");

        // handle save button click
        save.setOnAction(e -> {
            String key = keyField.getText().trim();
            String p1 = newPass.getText();
            String p2 = confirm.getText();

            // validate input
            if (key.isEmpty() || p1.isEmpty() || p2.isEmpty()) {
                setError(status, "All fields are required.");
                return;
            }
            if (!p1.equals(p2)) {
                setError(status, "Passwords do not match.");
                return;
            }
            if (p1.length() < 4) {
                setError(status, "Password must be at least 4 characters.");
                return;
            }

            // update password using service
            try {
                ctx.service().modifyStaffPassword(key, p1, ctx.getCurrentUser());
                setOk(status, "Password updated.");
                dialog.close();
            } catch (Exception ex) {
                setError(status, ex.getMessage());
            }
        });

        cancel.setOnAction(e -> dialog.close());

        // layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));
        grid.add(new Label("User / Staff ID:"), 0, 0);
        grid.add(keyField, 1, 0);
        grid.add(new Label("New password:"), 0, 1);
        grid.add(newPass, 1, 1);
        grid.add(new Label("Confirm:"), 0, 2);
        grid.add(confirm, 1, 2);

        HBox buttons = new HBox(10, save, cancel);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        GridPane root = new GridPane();
        root.setVgap(10);
        root.setPadding(new Insets(15));
        root.add(grid, 0, 0);
        root.add(status, 0, 1);
        root.add(buttons, 0, 2);

        dialog.setScene(new Scene(root, 420, 240));
        dialog.showAndWait();
    }

    // helper for error message
    private void setError(Label l, String msg) {
        l.setText(msg);
        l.setStyle("-fx-text-fill: red;");
    }

    // helper for success message
    private void setOk(Label l, String msg) {
        l.setText(msg);
        l.setStyle("-fx-text-fill: green;");
    }
}
