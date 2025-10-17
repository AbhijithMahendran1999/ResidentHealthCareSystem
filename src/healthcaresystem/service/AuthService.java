package healthcaresystem.service;

import healthcaresystem.model.facility.CareHome;
import healthcaresystem.model.people.Staff;

// Handles user authentication
public class AuthService {

    private final CareHome careHome;

    public AuthService(CareHome careHome) {
        this.careHome = careHome;
    }

    // Validates username and password
    public Staff login(String username, String password) {
        return careHome.getStaff().stream()
                .filter(s -> s.getUsername().equalsIgnoreCase(username))
                .filter(s -> {
                    String p = s.getPassword();
                    return p != null && p.equals(password);
                })
                .findFirst()
                .orElse(null);
    }
}
