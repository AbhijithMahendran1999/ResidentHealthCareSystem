package healthcaresystem.app;

import healthcaresystem.model.facility.CareHome;
import healthcaresystem.repo.SerializationRepository;
import healthcaresystem.service.CareHomeService;
import healthcaresystem.view.cli.Menu;

public class Main {

    public static void main(String[] args) {

        final String FILE = "carehome.dat";

        // load saved data (if any)
        SerializationRepository repo = new SerializationRepository();
        CareHome careHome = repo.loadState(FILE);

        // create main service using loaded data
        CareHomeService service = new CareHomeService(careHome);

        // run the CLI menu version of the app
        new Menu(service).start();

        // save state again when exiting
        repo.saveState(careHome, FILE);
    }
}
