package healthcaresystem.view.fx;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import healthcaresystem.model.facility.Bed;
import healthcaresystem.model.people.Resident;

public final class FxBits {
    private FxBits() {}

    // Simple header label for sections like “Ward W1”
    public static Label h2(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        return l;
    }

    // Creates a clean FlowPane for arranging bed buttons per room
    public static FlowPane roomPane(String title) {
        FlowPane fp = new FlowPane();
        fp.setHgap(8);
        fp.setVgap(8);
        fp.setPadding(new Insets(6));
        fp.setPrefWrapLength(260);
        fp.setStyle(
            "-fx-background-color: #f7f9fb;" +
            "-fx-border-color: #d8e0ea;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;"
        );
        return fp;
    }

    // Builds a rectangular bed button: blue/red if occupied, gray if vacant
    public static Button bedButton(Bed bed, FxContext ctx) {
        Resident occ = bed.getOccupiedBy();
        Button b = new Button(bed.getId());
        b.setMinSize(96, 48);
        b.setMaxSize(120, 60);
        b.setWrapText(true);

        String base = "-fx-background-radius: 8; -fx-border-radius: 8; " +
                      "-fx-border-color: #c8cfd9; -fx-border-width: 1;";

        if (occ == null) {
            b.setStyle(base + " -fx-background-color: #eeeeee;");
        } else {
            char g = Character.toUpperCase(occ.getGender());
            if (g == 'M') {
                b.setStyle(base + " -fx-background-color: #b9ddff;"); // blue
            } else if (g == 'F') {
                b.setStyle(base + " -fx-background-color: #ffb9c7;"); // pink
            } else {
                b.setStyle(base + " -fx-background-color: #ddd;");
            }
        }
        return b;
    }
}
