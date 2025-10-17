package healthcaresystem.view.fx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MoveResidentDialog {
    private final FxContext ctx;
    private final String fromBedId;
    private final Runnable onMoved; // optional callback after move

    public MoveResidentDialog(FxContext ctx, String fromBedId, Runnable onMoved) {
        this.ctx = ctx;
        this.fromBedId = fromBedId;
        this.onMoved = onMoved;
    }

    // show dialog for moving a resident to another bed
    public void show(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Move Resident from " + fromBedId);

        // input and labels
        TextField toBedId = new TextField();
        toBedId.setPromptText("Destination Bed ID (e.g., W2-R3-B2)");

        Label status = new Label();
        Button move = new Button("Move");
        Button close = new Button("Close");

        // move action
        move.setOnAction(e -> {
            try {
                String dest = toBedId.getText().trim();
                if (dest.isEmpty()) {
                    status.setText("Enter destination bed id.");
                    return;
                }

                // perform move through service
                ctx.service().moveResident(fromBedId, dest, ctx.getCurrentUser());
                status.setStyle("-fx-text-fill: green;");
                status.setText("Moved to " + dest);

                if (onMoved != null) onMoved.run();
                dialog.close();
            } catch (Exception ex) {
                status.setStyle("-fx-text-fill: red;");
                status.setText("Error: " + ex.getMessage());
            }
        });

        close.setOnAction(e -> dialog.close());

        // layout
        HBox buttons = new HBox(10, move, close);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(12,
                new Label("From Bed: " + fromBedId),
                new Label("To Bed:"),
                toBedId,
                status,
                buttons);
        root.setPadding(new Insets(14));
        root.setPrefWidth(460);

        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }
}
