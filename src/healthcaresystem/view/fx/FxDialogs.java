package healthcaresystem.view.fx;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public final class FxDialogs {
    private FxDialogs() {}  // utility class, no instantiation

    // show a simple information alert with an OK button
    public static void info(String title, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, content, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
