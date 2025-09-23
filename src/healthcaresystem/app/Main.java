package healthcaresystem.app;

import healthcaresystem.model.facility.CareHome;
import healthcaresystem.repo.SerializationRepository;
import healthcaresystem.service.CareHomeService;
import healthcaresystem.view.cli.Menu;

public class Main {
    public static void main(String[] args) {
        final String FILE = "carehome.dat";
        SerializationRepository repo = new SerializationRepository();
        CareHome careHome = repo.loadState(FILE);
        CareHomeService service = new CareHomeService(careHome);

        new Menu(service).start();

        repo.saveState(careHome, FILE);
    }
}
