package healthcaresystem.service;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import healthcaresystem.model.facility.CareHome;
import healthcaresystem.model.people.Manager;

public class AuthServiceTests {

    private CareHomeService service;
    private AuthService auth;

    @Before
    public void setup() {
        service = new CareHomeService(new CareHome());
        Manager m = new Manager("M1", "Mgr", "mgr");
        m.setPassword("pw");
        service.addStaff(m);
        auth = new AuthService(service.getCareHome());
    }

    @Test
    public void login_success() {
        assertNotNull(auth.login("mgr", "pw"));
    }

    @Test
    public void login_wrongPassword_returnsNull() {
        assertNull(auth.login("mgr", "x"));
    }
}
