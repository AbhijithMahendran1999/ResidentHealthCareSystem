package healthcaresystem.view.fx;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import healthcaresystem.model.people.Doctor;
import healthcaresystem.model.people.Nurse;
import healthcaresystem.model.people.Staff;
import healthcaresystem.model.schedule.Shift;
import healthcaresystem.model.schedule.ShiftType;

public class ViewRosterDialog {

    private final FxContext ctx; // shared app context

    public ViewRosterDialog(FxContext ctx) {
        this.ctx = ctx;
    }

    // shows the grouped roster in tabs (Nurses / Doctors)
    public void show(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Roster (Grouped & Graphical)");

        TabPane tabs = new TabPane();
        tabs.getTabs().add(makeRoleRosterTab("Nurses", Nurse.class));
        tabs.getTabs().add(makeRoleRosterTab("Doctors", Doctor.class));
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        VBox root = new VBox(10, tabs);
        root.setPadding(new Insets(12));
        root.setPrefSize(900, 520);

        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }

    // builds a single tab for the given role with a grid view
    private Tab makeRoleRosterTab(String title, Class<? extends Staff> role) {
        List<Shift> shifts;
        List<Staff> staffOfRole;

        // load filtered shifts and staff via service (manager-only)
        try {
            shifts = ctx.service().listRosterForRole(ctx.getCurrentUser(), role);
            staffOfRole = ctx.service().listStaffByRole(ctx.getCurrentUser(), role);
        } catch (Exception ex) {
            return new Tab(title, new Label("Error: " + ex.getMessage()));
        }

        // collect distinct days shown as columns
        List<LocalDate> days = shifts.stream()
                .map(Shift::getDay)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // grid scaffolding
        GridPane grid = new GridPane();
        grid.setHgap(6);
        grid.setVgap(6);
        grid.setPadding(new Insets(10));

        ColumnConstraints firstCol = new ColumnConstraints();
        firstCol.setMinWidth(180);
        firstCol.setPrefWidth(220);
        grid.getColumnConstraints().add(firstCol);

        for (int i = 0; i < days.size(); i++) {
            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth((100.0 - 22) / Math.max(days.size(), 1)); // rough layout balance
            grid.getColumnConstraints().add(c);
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE dd/MM");

        // header row (Staff \ Date)
        Label topLeft = makeHeader("Staff \\ Date");
        grid.add(topLeft, 0, 0);

        for (int c = 0; c < days.size(); c++) {
            Label d = makeHeader(days.get(c).format(fmt));
            GridPane.setHalignment(d, HPos.CENTER);
            grid.add(d, c + 1, 0);
        }

        // index shifts as staffId -> day -> set of shift types
        Map<String, Map<LocalDate, EnumSet<ShiftType>>> map = new HashMap<>();
        for (Shift s : shifts) {
            String sid = s.getStaff().getId();
            map.computeIfAbsent(sid, k -> new HashMap<>());
            map.get(sid).computeIfAbsent(s.getDay(), k -> EnumSet.noneOf(ShiftType.class)).add(s.getType());
        }

        // one row per staff, one cell per day
        int row = 1;
        for (Staff st : staffOfRole) {
            Label name = new Label(st.getName() + "  @" + st.getUsername());
            name.setStyle("-fx-font-weight: bold;");
            name.setMaxWidth(Double.MAX_VALUE);
            grid.add(name, 0, row);

            for (int c = 0; c < days.size(); c++) {
                LocalDate day = days.get(c);

                String cellText = "";
                String cellStyle = "-fx-background-color: #f4f4f4; -fx-border-color: #ddd; -fx-padding: 6;";

                EnumSet<ShiftType> set = Optional.ofNullable(map.get(st.getId()))
                        .map(m -> m.get(day))
                        .orElse(null);

                if (role == Nurse.class) {
                    if (set != null && !set.isEmpty()) {
                        boolean dayShift = set.contains(ShiftType.DAY);
                        boolean eveShift = set.contains(ShiftType.EVE);

                        if (dayShift && eveShift) {
                            cellText = "DAY+EVE";
                            cellStyle = "-fx-background-color: #ffd8a8; -fx-border-color: #ddd; -fx-padding: 6;";
                        } else if (dayShift) {
                            cellText = "DAY";
                            cellStyle = "-fx-background-color: #c0f0ff; -fx-border-color: #ddd; -fx-padding: 6;";
                        } else if (eveShift) {
                            cellText = "EVE";
                            cellStyle = "-fx-background-color: #d9c0ff; -fx-border-color: #ddd; -fx-padding: 6;";
                        }
                    }
                } else {
                    // doctors just need any coverage that day
                    if (set != null && !set.isEmpty()) {
                        cellText = "✔ 1h";
                        cellStyle = "-fx-background-color: #c7f6c7; -fx-border-color: #ddd; -fx-padding: 6;";
                    }
                }

                Label cell = new Label(cellText);
                cell.setStyle(cellStyle + " -fx-alignment: center;");
                cell.setMaxWidth(Double.MAX_VALUE);
                cell.setMinWidth(80);
                cell.setAlignment(Pos.CENTER);

                grid.add(cell, c + 1, row);
            }

            row++;
        }

        // allow scroll if many days/users
        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);

        return new Tab(title, scroll);
    }

    // makes a simple header label cell
    private Label makeHeader(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold; -fx-background-color: #eef2f7; -fx-border-color: #ccd5e0; -fx-padding: 6;");
        l.setMaxWidth(Double.MAX_VALUE);
        l.setAlignment(Pos.CENTER);
        return l;
    }
}
