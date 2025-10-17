package healthcaresystem.view.fx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import healthcaresystem.model.people.Manager;
import healthcaresystem.model.people.Staff;

public class DashboardView {

    private final FxContext ctx;
    private final Stage stage;

    // holds the right-side bed board so we can refresh it after changes
    private VBox boardHolder;

    public DashboardView(FxContext ctx, Stage stage) {
        this.ctx = ctx;
        this.stage = stage;
    }

    // build the main dashboard scene
    public Scene createScene() {
        Staff user = ctx.getCurrentUser();

        Label title = new Label("Resident HealthCare System");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);

        Label userInfo = new Label(
            user.getClass().getSimpleName() + " | " + user.getName() +
            " (" + user.getUsername() + ")"
        );
        userInfo.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");

        Button logout = new Button("Logout");
        logout.setOnAction(e -> {
            ctx.setCurrentUser(null);
            new LoginView(ctx).show(stage);
        });

        HBox rightInfo = new HBox(10, userInfo, logout);
        rightInfo.setAlignment(Pos.CENTER_RIGHT);

        HBox centerBox = new HBox(title);
        centerBox.setAlignment(Pos.CENTER);

        HBox header = new HBox(16, leftSpacer, centerBox, rightInfo);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(10, 20, 12, 20));
        header.setStyle("-fx-background-color: #f2f2f2; -fx-border-color: #d0d0d0; -fx-border-width: 0 0 1 0;");

        // ----- role aware left sidebar (manager only actions here) -----
        boolean isManager = user instanceof Manager;

        Button viewStaff       = new Button("List Staff");
        Button viewRoster      = new Button("View Roster");
        Button addStaff        = new Button("Add Staff (Doctor/Nurse)");
        Button modifyPass      = new Button("Modify Staff Password");
        Button assignShift     = new Button("Allocate/Modify Shift");
        Button checkCompliance = new Button("Check Compliance");
        Button showLogs        = new Button("Show Audit Logs");

        // wire manager actions to dialogs
        viewStaff.setOnAction(e -> new ViewStaffDialog(ctx).show(stage));
        viewRoster.setOnAction(e -> new ViewRosterDialog(ctx).show(stage));
        addStaff.setOnAction(e -> new AddStaffDialog(ctx, null).show(stage));
        modifyPass.setOnAction(e -> new ModifyStaffPasswordDialog(ctx).show(stage));
        assignShift.setOnAction(e -> new AssignShiftDialog(ctx, null).show(stage));
        checkCompliance.setOnAction(e -> new CheckComplianceDialog(ctx).show(stage));
        showLogs.setOnAction(e -> new ShowAuditLogDialog(ctx).show(stage));

        VBox actions = new VBox(10);
        actions.setPrefWidth(300);
        actions.setPadding(new Insets(14));
        actions.setStyle("-fx-background-color: #fafafa; -fx-border-color: #d0d0d0; -fx-border-width: 0 1 0 0;");

        if (isManager) {
            actions.getChildren().addAll(
                    viewStaff, viewRoster, addStaff, modifyPass, assignShift, checkCompliance, showLogs
            );
        } else {
            // nurses/doctors act from bed tiles and no global buttons needed
            actions.getChildren().add(new Label("Select a bed to act."));
        }

        // ----- right: two ward bed board (clickable beds) -----
        boardHolder = new VBox();
        boardHolder.getChildren().setAll(new BedBoardView(ctx, this::refreshBoard).build());
        boardHolder.setPadding(new Insets(10));

        HBox content = new HBox(24, actions, boardHolder);
        content.setPadding(new Insets(15));
        content.setAlignment(Pos.TOP_LEFT);

        VBox root = new VBox(header, content);
        return new Scene(root, 1250, 740);
    }

    // show the dashboard
    public void show() {
        stage.setScene(createScene());
        stage.setTitle("Resident HealthCare System - Dashboard");
        stage.show();
    }

    // rebuild the bed board after occupancy changes
    public void refreshBoard() {
        if (boardHolder != null) {
            boardHolder.getChildren().setAll(new BedBoardView(ctx, this::refreshBoard).build());
        }
    }
}
