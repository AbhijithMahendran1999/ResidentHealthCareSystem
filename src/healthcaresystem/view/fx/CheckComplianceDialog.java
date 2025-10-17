package healthcaresystem.view.fx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CheckComplianceDialog {

    private final FxContext ctx;

    public CheckComplianceDialog(FxContext ctx) {
        this.ctx = ctx;
    }

    // Show a tiny modal to run the weekly compliance check
    public void show(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Check Compliance");

        Label status = new Label("Press 'Check' to validate scheduling rules for the next 7 days.");

        Button checkBtn = new Button("Check");
        Button closeBtn = new Button("Close");

        // Run the check and show a simple OK/FAIL message
        checkBtn.setOnAction(e -> {
            try {
                ctx.service().checkCompliance();
                status.setText("Compliance OK for the next 7 days from first scheduled day.");
                status.setStyle("-fx-text-fill: green;");
            } catch (Exception ex) {
                status.setText("❌ Compliance FAILED: " + ex.getMessage());
                status.setStyle("-fx-text-fill: red;");
            }
        });

        // Close the dialog
        closeBtn.setOnAction(e -> dialog.close());

        HBox buttons = new HBox(10, checkBtn, closeBtn);
        buttons.setAlignment(Pos.CENTER);

        VBox root = new VBox(15, status, buttons);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(25));

        dialog.setScene(new Scene(root, 500, 180));
        dialog.showAndWait();
    }
}
