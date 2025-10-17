package healthcaresystem.view.fx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

// Dialog for manager to add a new staff member
public class AddStaffDialog {

    private final FxContext ctx;
    private final Runnable onSuccess; // optional refresh callback

    public AddStaffDialog(FxContext ctx, Runnable onSuccess) {
        this.ctx = ctx;
        this.onSuccess = onSuccess;
    }

    // Show the add staff dialog
    public void show(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add Staff Member");

        // Form fields
        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("Doctor", "Nurse");
        roleBox.setValue("Nurse");

        TextField idField = new TextField();       idField.setPromptText("e.g., S-001");
        TextField nameField = new TextField();     nameField.setPromptText("Full name");
        TextField usernameField = new TextField(); usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField(); passwordField.setPromptText("Password");

        Label status = new Label();

        Button save = new Button("Save");
        Button cancel = new Button("Cancel");

        // Save button action
        save.setOnAction(e -> {
            String role = roleBox.getValue();
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (id.isEmpty() || name.isEmpty() || username.isEmpty() || password.isEmpty()) {
                setError(status, "All fields are required.");
                return;
            }

            try {
                ctx.service().addStaffWithCredentials(role, id, name, username, password, ctx.getCurrentUser());
                setOk(status, "Added " + role + " successfully!");
                if (onSuccess != null) onSuccess.run();
                dialog.close();
            } catch (Exception ex) {
                setError(status, ex.getMessage());
            }
        });

        cancel.setOnAction(e -> dialog.close());

        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        grid.add(new Label("Role:"),      0, 0); grid.add(roleBox,       1, 0);
        grid.add(new Label("Staff ID:"),  0, 1); grid.add(idField,       1, 1);
        grid.add(new Label("Name:"),      0, 2); grid.add(nameField,     1, 2);
        grid.add(new Label("Username:"),  0, 3); grid.add(usernameField, 1, 3);
        grid.add(new Label("Password:"),  0, 4); grid.add(passwordField, 1, 4);

        HBox buttons = new HBox(10, save, cancel);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(15, grid, status, buttons);
        root.setPadding(new Insets(20));

        dialog.setScene(new Scene(root, 420, 320));
        dialog.showAndWait();
    }

    // Helper to show red error messages
    private void setError(Label l, String msg) {
        l.setText(msg);
        l.setStyle("-fx-text-fill: red;");
    }

    // Helper to show green success messages
    private void setOk(Label l, String msg) {
        l.setText(msg);
        l.setStyle("-fx-text-fill: green;");
    }
}
