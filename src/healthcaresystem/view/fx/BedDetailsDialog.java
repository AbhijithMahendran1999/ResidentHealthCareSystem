package healthcaresystem.view.fx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import healthcaresystem.model.people.Doctor;
import healthcaresystem.model.people.Manager;
import healthcaresystem.model.people.Nurse;
import healthcaresystem.model.people.Resident;
import healthcaresystem.model.people.Staff;

public class BedDetailsDialog {

    private final FxContext ctx;
    private final String bedId;
    private final Runnable onRefresh;

    public BedDetailsDialog(FxContext ctx, String bedId, Runnable onRefresh) {
        this.ctx = ctx;
        this.bedId = bedId;
        this.onRefresh = onRefresh;
    }

    // Shows a small dialog for one bed with role-based actions
    public void show(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Bed: " + bedId);

        Staff user = ctx.getCurrentUser();
        boolean isManager = user instanceof Manager;
        boolean isNurse   = user instanceof Nurse;
        boolean isDoctor  = user instanceof Doctor;

        Label bedLbl = new Label("Bed: " + bedId);
        bedLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label residentLbl = new Label();
        residentLbl.setWrapText(true);

        // Look up bed directly from the model
        var bed = ctx.service().getCareHome().getWards().stream()
                .flatMap(w -> w.getRooms().stream())
                .flatMap(rm -> rm.getBeds().stream())
                .filter(b -> b.getId().equalsIgnoreCase(bedId))
                .findFirst()
                .orElse(null);

        Resident r = (bed == null) ? null : bed.getOccupiedBy();

        if (bed == null) {
            residentLbl.setText("Error: bed not found.");
        } else if (r == null) {
            residentLbl.setText("Status: VACANT");
        } else {
            residentLbl.setText("Resident: " + r.getName() + " (" + r.getId() + "), Gender: " + r.getGender());
        }

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        // Manager: add resident when vacant
        if (isManager && bed != null && r == null) {
            Button addResident = new Button("Add Resident to this Bed");
            addResident.setOnAction(e -> {
                new AddResidentDialog(ctx, () -> { if (onRefresh != null) onRefresh.run(); })
                        .show(owner, bedId); // prefill/lock bed
                dialog.close();
            });
            actions.getChildren().add(addResident);
        }

        // Doctor: view/attach/update prescriptions (only if occupied)
        if (isDoctor && r != null) {
            Button viewRx   = new Button("View Prescriptions");
            Button attachRx = new Button("Attach Prescription");
            Button updateRx = new Button("Update Prescription");

            viewRx.setOnAction(e -> new ViewPrescriptionsDialog(ctx, bedId).show(owner));
            attachRx.setOnAction(e -> new AttachPrescriptionDialog(ctx, bedId).show(owner));
            updateRx.setOnAction(e -> new UpdatePrescriptionDialog(ctx, bedId).show(owner));

            actions.getChildren().addAll(viewRx, attachRx, updateRx);
        }

        // Nurse: view/administer/move (only if occupied)
        if (isNurse && r != null) {
            Button viewRx     = new Button("View Prescriptions");
            Button administer = new Button("Administer Medication");
            Button move       = new Button("Move Resident");

            viewRx.setOnAction(e -> new ViewPrescriptionsDialog(ctx, bedId).show(owner));
            administer.setOnAction(e -> new AdministerMedicationDialog(ctx, bedId).show(owner));
            move.setOnAction(e -> new MoveResidentDialog(ctx, bedId, () -> {
                if (onRefresh != null) onRefresh.run();
            }).show(owner));

            actions.getChildren().addAll(viewRx, administer, move);
        }

        Button close = new Button("Close");
        close.setOnAction(e -> dialog.close());
        actions.getChildren().add(close);

        VBox root = new VBox(12, bedLbl, residentLbl, actions);
        root.setPadding(new Insets(14));
        root.setPrefWidth(520);

        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }
}
