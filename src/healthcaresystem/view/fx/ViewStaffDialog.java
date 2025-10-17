package healthcaresystem.view.fx;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import healthcaresystem.model.people.Doctor;
import healthcaresystem.model.people.Nurse;
import healthcaresystem.model.people.Staff;

public class ViewStaffDialog {

    private final FxContext ctx; // shared app context

    // wire up context
    public ViewStaffDialog(FxContext ctx) {
        this.ctx = ctx;
    }

    // show the grouped staff directory (tabs for Nurses/Doctors)
    public void show(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Staff Directory");

        TabPane tabs = new TabPane();
        tabs.getTabs().add(makeRoleTab("Nurses", Nurse.class));
        tabs.getTabs().add(makeRoleTab("Doctors", Doctor.class));
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        VBox root = new VBox(10, tabs);
        root.setPadding(new Insets(12));
        root.setPrefSize(650, 420);

        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }

    // build a single role tab with a simple table
    private Tab makeRoleTab(String title, Class<? extends Staff> role) {
        TableView<Staff> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPlaceholder(new Label("No " + title.toLowerCase() + " found."));

        TableColumn<Staff, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMinWidth(80);

        TableColumn<Staff, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setMinWidth(160);

        TableColumn<Staff, String> userCol = new TableColumn<>("Username");
        userCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        userCol.setMinWidth(140);

        table.getColumns().addAll(idCol, nameCol, userCol);

        // load rows via service (manager-only)
        try {
            var rows = ctx.service().listStaffByRole(ctx.getCurrentUser(), role);
            table.setItems(FXCollections.observableArrayList(rows));
        } catch (Exception ex) {
            table.setPlaceholder(new Label("Error: " + ex.getMessage()));
        }

        return new Tab(title, table);
    }
}
