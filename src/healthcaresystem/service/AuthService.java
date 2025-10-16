package healthcaresystem.service;

import healthcaresystem.model.facility.CareHome;
import healthcaresystem.model.people.Staff;

public class AuthService {
    private final CareHome careHome;

    public AuthService(CareHome careHome) {
        this.careHome = careHome;
    }

    // login logic
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
