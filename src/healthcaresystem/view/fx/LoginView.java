package healthcaresystem.view.fx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import healthcaresystem.model.people.Staff;

public class LoginView {
    private final FxContext ctx;

    public LoginView(FxContext ctx) {
        this.ctx = ctx;
    }

    // show login window on the given stage
    public void show(Stage stage) {
        stage.setTitle("Resident HealthCare System - Login");
        stage.setScene(createScene(stage));
        stage.show();
    }

    // build and return the login scene
    public Scene createScene(Stage stage) {
        Label title = new Label("Resident HealthCare System");
        title.setStyle("-fx-font-size:18px; -fx-font-weight:bold;");

        // input fields
        TextField username = new TextField();
        username.setPromptText("Username");

        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        Button loginBtn = new Button("Login");
        Label status = new Label();

        // handle login button click
        loginBtn.setOnAction(e -> {
            String u = username.getText().trim();
            String p = password.getText().trim();

            Staff s = ctx.auth().login(u, p);
            if (s != null) {
                // success : store user and open dashboard
                ctx.setCurrentUser(s);
                status.setText("Welcome, " + s.getName() + "!");
                status.setStyle("-fx-text-fill: green;");
                new DashboardView(ctx, stage).show();
            } else {
                // failure : show error
                status.setText("Invalid username or password.");
                status.setStyle("-fx-text-fill: red;");
            }
        });

        // layout configuration
        VBox root = new VBox(10, title, username, password, loginBtn, status);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setPrefSize(420, 260);

        return new Scene(root);
    }
}
