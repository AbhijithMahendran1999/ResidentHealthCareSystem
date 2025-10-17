package healthcaresystem.view.fx;

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import healthcaresystem.model.facility.Bed;
import healthcaresystem.model.people.Resident;

// Utility class for creating styled bed buttons in the bed board
final class BedCells {
    private BedCells() {}

    // Creates a styled button representing a bed (color-coded by occupant)
    static Button bedButton(Bed bed) {
        Resident r = bed.getOccupiedBy();

        String label = bed.getId();
        String bg; // background color
        String fg = "#ffffff"; // text color

        // Color logic: gray for vacant, red for female, blue for male
        if (r == null) {
            bg = "#8a8a8a"; // vacant
        } else if (Character.toUpperCase(r.getGender()) == 'F') {
            bg = "#d64545"; // female
        } else {
            bg = "#2f6fd6"; // male
        }

        // Button styling
        Button b = new Button(label);
        b.setMinSize(88, 32);
        b.setPrefSize(100, 34);
        b.setMaxWidth(Region.USE_PREF_SIZE);
        b.setStyle(
            "-fx-background-color:" + bg + ";" +
            "-fx-text-fill:" + fg + ";" +
            "-fx-font-weight:bold;" +
            "-fx-background-radius:6;" +
            "-fx-border-radius:6;" +
            "-fx-border-color: rgba(0,0,0,0.15);" +
            "-fx-border-width:1;"
        );

        // Tooltip showing resident details or VACANT
        String tip = (r == null)
                ? "VACANT"
                : r.getName() + " (" + r.getId() + ", " + r.getGender() + ")";
        b.setTooltip(new Tooltip(tip));

        return b;
    }
}
