package healthcaresystem.app;

import javafx.application.Application;
import javafx.stage.Stage;
import healthcaresystem.view.fx.FxContext;
import healthcaresystem.view.fx.LoginView;

public class FxMain extends Application {
    private FxContext ctx;

    @Override
    public void start(Stage stage) {
        // set up app context and show login screen
        ctx = new FxContext();
        LoginView login = new LoginView(ctx);

        stage.setTitle("Resident HealthCare System - Login");
        stage.setScene(login.createScene(stage));
        stage.show();
    }

    @Override
    public void stop() {
        // save everything before exit
        if (ctx != null) ctx.save();
    }

    public static void main(String[] args) {
        // entry point
        launch(args);
    }
}
