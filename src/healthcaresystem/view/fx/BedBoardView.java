package healthcaresystem.view.fx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import healthcaresystem.model.facility.Ward;
import healthcaresystem.model.facility.Room;
import healthcaresystem.model.facility.Bed;

// Builds the full visual layout of all wards, rooms, and beds
public class BedBoardView {

    private final FxContext ctx;
    private final Runnable onRefresh;

    public BedBoardView(FxContext ctx) {
        this(ctx, null);
    }

    public BedBoardView(FxContext ctx, Runnable onRefresh) {
        this.ctx = ctx;
        this.onRefresh = onRefresh;
    }

    // Builds a horizontally scrollable set of wards, each containing its rooms and beds
    public Node build() {

        HBox wards = new HBox(24);
        wards.setPadding(new Insets(10));
        wards.setAlignment(Pos.TOP_LEFT);

        // Loop through all wards in the care home
        for (Ward w : ctx.service().getCareHome().getWards()) {

            VBox wardBox = new VBox(10);
            wardBox.getChildren().add(FxBits.h2("Ward " + w.getId()));

            // Each ward contains multiple rooms
            for (Room r : w.getRooms()) {

                FlowPane roomPane = FxBits.roomPane("Room " + r.getId());

                // Each room contains multiple beds
                for (Bed b : r.getBeds()) {
                    Button bedBtn = FxBits.bedButton(b, ctx);

                    // Opens details dialog for selected bed
                    bedBtn.setOnAction(e -> 
                        new BedDetailsDialog(ctx, b.getId(), onRefresh)
                            .show((Stage) bedBtn.getScene().getWindow())
                    );

                    roomPane.getChildren().add(bedBtn);
                }

                wardBox.getChildren().add(roomPane);
            }

            wards.getChildren().add(wardBox);
        }

        return wards;
    }
}
