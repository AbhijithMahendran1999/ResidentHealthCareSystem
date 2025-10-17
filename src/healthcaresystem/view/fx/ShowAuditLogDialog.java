package healthcaresystem.view.fx;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import healthcaresystem.model.audit.AuditEntry;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class ShowAuditLogDialog {

    private final FxContext ctx;

    public ShowAuditLogDialog(FxContext ctx) {
        this.ctx = ctx;
    }

    // display a read only table of audit logs
    public void show(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("System Audit Log");

        // header label
        Label header = new Label("System Audit Log (Most Recent First)");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // main table setup
        TableView<AuditEntry> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPlaceholder(new Label("No audit log entries."));

        // timestamp column
        TableColumn<AuditEntry, String> timeCol = new TableColumn<>("Timestamp");
        timeCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                ));
        timeCol.setMinWidth(160);

        // user column
        TableColumn<AuditEntry, String> userCol = new TableColumn<>("User");
        userCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getActor() == null
                                ? "(system)"
                                : c.getValue().getActor().getUsername()
                ));
        userCol.setMinWidth(100);

        // action column
        TableColumn<AuditEntry, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getAction()));
        actionCol.setMinWidth(140);

        // details column
        TableColumn<AuditEntry, String> detailsCol = new TableColumn<>("Details");
        detailsCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getDetails()));
        detailsCol.setMinWidth(280);

        // result column (OK / Denied)
        TableColumn<AuditEntry, String> resultCol = new TableColumn<>("Result");
        resultCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().isSuccess() ? "OK" : "Denied"
                ));
        resultCol.setMinWidth(100);

        table.getColumns().addAll(timeCol, userCol, actionCol, detailsCol, resultCol);

        // load and sort audit entries by time (newest first)
        List<AuditEntry> entries = ctx.service()
                .getCareHome()
                .getAuditLog()
                .stream()
                .sorted(Comparator.comparing(AuditEntry::getTime).reversed())
                .toList();
        table.getItems().addAll(entries);

        // make table scrollable
        ScrollPane scrollPane = new ScrollPane(table);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        // root layout
        VBox root = new VBox(15, header, scrollPane);
        root.setPadding(new Insets(15));
        root.setPrefSize(900, 500);

        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }
}
