package healthcaresystem.view.fx;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import healthcaresystem.model.meds.AdministrationRecord;

public class ViewAdministrationLogDialog {

    private final FxContext ctx;

    // connects service context into the dialog
    public ViewAdministrationLogDialog(FxContext ctx) {
        this.ctx = ctx;
    }

    // shows a modal table of administration records filtered by bed id
    public void show(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Administration Log by Bed");

        TextField bedIdField = new TextField();
        bedIdField.setPromptText("Bed ID (e.g., W1-R2-B1)");

        Button load = new Button("Load");
        Label status = new Label();

        TableView<AdministrationRecord> table = new TableView<>();
        table.setPlaceholder(new Label("No records."));

        // columns
        TableColumn<AdministrationRecord, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        timeCol.setPrefWidth(160);

        TableColumn<AdministrationRecord, String> medCol = new TableColumn<>("Medicine");
        medCol.setCellValueFactory(new PropertyValueFactory<>("medicine"));
        medCol.setPrefWidth(180);

        TableColumn<AdministrationRecord, String> doseCol = new TableColumn<>("Dose");
        doseCol.setCellValueFactory(new PropertyValueFactory<>("dose"));
        doseCol.setPrefWidth(120);

        TableColumn<AdministrationRecord, String> byCol = new TableColumn<>("By");
        byCol.setCellValueFactory(c -> {
            var staff = c.getValue().getAdministeredBy();
            String who = (staff == null) ? "-" : ("@" + staff.getUsername());
            return new javafx.beans.property.SimpleStringProperty(who);
        });
        byCol.setPrefWidth(120);

        table.getColumns().addAll(timeCol, medCol, doseCol, byCol);

        // fetch and display rows for the provided bed id
        load.setOnAction(e -> {
            String bedId = bedIdField.getText().trim();
            if (bedId.isEmpty()) {
                status.setText("Enter a bed ID.");
                status.setStyle("-fx-text-fill: red;");
                return;
            }
            try {
                var rows = ctx.service().administrationLogForBed(bedId, ctx.getCurrentUser());
                table.setItems(FXCollections.observableArrayList(rows));
                status.setText("Loaded " + rows.size() + " record(s).");
                status.setStyle("-fx-text-fill: green;");
            } catch (Exception ex) {
                status.setText("Error: " + ex.getMessage());
                status.setStyle("-fx-text-fill: red;");
                table.getItems().clear();
            }
        });

        HBox top = new HBox(8, new Label("Bed ID:"), bedIdField, load);
        top.setPadding(new Insets(10));

        VBox root = new VBox(8, top, table, status);
        root.setPadding(new Insets(10));
        root.setPrefSize(650, 400);

        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }
}
