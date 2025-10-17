package healthcaresystem.view.fx;

import healthcaresystem.model.facility.CareHome;
import healthcaresystem.model.people.Staff;
import healthcaresystem.repo.SerializationRepository;
import healthcaresystem.service.AuthService;
import healthcaresystem.service.CareHomeService;

public class FxContext {
    private static final String FILE = "carehome.dat";

    private final SerializationRepository repo = new SerializationRepository();
    private final CareHomeService service;
    private final AuthService auth;
    private Staff currentUser;

    public FxContext() {
        // load saved state from file (creates new CareHome if file missing)
        CareHome ch = repo.loadState(FILE);

        // use same CareHome instance for both services
        service = new CareHomeService(ch);
        auth = new AuthService(service.getCareHome());

        // seed default admin and facility on first launch
        service.ensureDefaultAdmin();
        service.ensureDefaultFacility();

        // save seeded data so it's available next time
        save();
    }

    // accessors for main service and auth
    public CareHomeService service() { return service; }
    public AuthService auth() { return auth; }

    // track currently logged-in staff
    public Staff getCurrentUser() { return currentUser; }
    public void setCurrentUser(Staff s) { this.currentUser = s; }

    // persist current CareHome state to disk
    public void save() {
        repo.saveState(service.getCareHome(), FILE);
    }
}
