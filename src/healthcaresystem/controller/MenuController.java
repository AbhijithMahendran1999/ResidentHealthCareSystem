package healthcaresystem.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

import healthcaresystem.model.meds.AdministrationRecord;
import healthcaresystem.model.meds.PrescriptionItem;
import healthcaresystem.model.people.Resident;
import healthcaresystem.model.people.Staff;
import healthcaresystem.model.schedule.ShiftType;
import healthcaresystem.service.AuthService;
import healthcaresystem.service.CareHomeService;

public class MenuController {

    private final CareHomeService service;
    private final AuthService auth;
    private final Scanner in;

    private Staff currentUser; // session user

    public MenuController(CareHomeService service, Scanner in) {
        this.service = service;
        this.in = in;
        this.auth = new AuthService(service.getCareHome());
    }

    // --- session handling ---
    public void login() {
        System.out.print("Username: ");
        String u = in.nextLine().trim();

        System.out.print("Password: ");
        String p = in.nextLine().trim();

        Staff s = auth.login(u, p);
        if (s == null) System.out.println("Login failed.");
        else {
            currentUser = s;
            System.out.println("Logged in as " + s.getClass().getSimpleName() + " " + s.getName());
        }
    }

    public void logout() {
        if (currentUser == null) System.out.println("Not logged in.");
        else {
            System.out.println("Logged out @" + currentUser.getUsername());
            currentUser = null;
        }
    }

    public void printCurrentUser() {
        if (currentUser == null) System.out.println("Not logged in.");
        else System.out.println("Current: " + currentUser.getClass().getSimpleName()
                + " " + currentUser.getName() + " @" + currentUser.getUsername());
    }

    // --- startup seeding ---
    public void ensureDefaultAdmin() { service.ensureDefaultAdmin(); }
    public void ensureDefaultFacility() { service.ensureDefaultFacility(); }

    // --- 1) Manager: add new resident to a bed ---
    public void addNewResidentToVacantBedFlow() {
        try {
            System.out.print("Resident ID: ");
            String id = in.nextLine().trim();

            System.out.print("Name: ");
            String name = in.nextLine().trim();

            System.out.print("Gender (M/F): ");
            String g = in.nextLine().trim().toUpperCase();
            char gender = g.isEmpty() ? 'M' : g.charAt(0);

            System.out.print("Bed ID: ");
            String bedId = in.nextLine().trim();

            service.addResidentToBed(id, name, gender, bedId, currentUser);
            System.out.println("Added resident " + name + " to " + bedId);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // --- 2) Manager: add staff ---
    public void addStaffFlow() {
        try {
            System.out.print("Role (Doctor/Nurse): ");
            String role = in.nextLine().trim();

            System.out.print("Staff ID: ");
            String id = in.nextLine().trim();

            System.out.print("Name: ");
            String name = in.nextLine().trim();

            System.out.print("Username: ");
            String username = in.nextLine().trim();

            System.out.print("Password: ");
            String pw = in.nextLine();

            service.addStaffWithCredentials(role, id, name, username, pw, currentUser);
            System.out.println("Added " + role + " " + name);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // --- 3) Manager: modify staff password ---
    public void modifyStaffPasswordFlow() {
        try {
            System.out.print("Username or staff ID: ");
            String key = in.nextLine().trim();

            System.out.print("New password: ");
            String pw = in.nextLine();

            service.modifyStaffPassword(key, pw, currentUser);
            System.out.println("Password updated.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // --- 4) Manager: allocate or modify shift ---
    public void allocateOrModifyShiftFlow() {
        try {
            System.out.print("Staff (username/ID): ");
            String key = in.nextLine().trim();

            System.out.print("Date (YYYY-MM-DD): ");
            LocalDate day = LocalDate.parse(in.nextLine().trim());

            System.out.print("Shift (DAY/EVE): ");
            ShiftType t = ShiftType.valueOf(in.nextLine().trim().toUpperCase());

            System.out.print("Replace existing on same day? (Y/N): ");
            boolean replace = in.nextLine().trim().equalsIgnoreCase("Y");

            service.assignOrReplaceShift(key, day, t, replace, currentUser);
            System.out.println((replace ? "Replaced" : "Assigned") + " " + t + " on " + day + ".");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // --- 5) Nurse/Doctor: view resident in a bed ---
    public void viewResidentInBedFlow() {
        try {
            System.out.print("Bed ID: ");
            String bedId = in.nextLine().trim();

            var opt = service.viewResidentInBed(bedId, currentUser);
            if (opt.isEmpty()) System.out.println("VACANT");
            else {
                Resident r = opt.get();
                System.out.println("Resident: " + r.getId() + " " + r.getName() + " (" + r.getGender() + ")");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // --- 6) Doctor: attach prescription ---
    public void attachPrescriptionFlow() {
        try {
            System.out.print("Bed ID: ");
            String bedId = in.nextLine().trim();

            System.out.println("Enter medicines (blank name to finish)");
            List<PrescriptionItem> items = new java.util.ArrayList<>();

            while (true) {
                System.out.print("Medicine: ");
                String med = in.nextLine().trim();
                if (med.isEmpty()) break;

                System.out.print("Dose: ");
                String dose = in.nextLine().trim();

                System.out.print("Frequency: ");
                String freq = in.nextLine().trim();

                System.out.print("Notes: ");
                String notes = in.nextLine().trim();

                items.add(new PrescriptionItem(med, dose, freq, notes));
            }

            var p = service.attachPrescriptionToBed(bedId, items, currentUser);
            System.out.println("Prescription " + p.getId() + " added (" + p.getItems().size() + " items).");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // --- 7) Nurse: move resident to a different bed ---
    public void moveResidentToDifferentBedFlow() {
        try {
            System.out.print("FROM bed: ");
            String fromId = in.nextLine().trim();

            System.out.print("TO bed: ");
            String toId = in.nextLine().trim();

            service.moveResident(fromId, toId, currentUser);
            System.out.println("Moved successfully.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // --- 8) Doctor: update prescription (add/edit/remove) ---
    public void updatePrescriptionDetailsFlow() {
        try {
            System.out.print("Prescription ID: ");
            String rxId = in.nextLine().trim();

            System.out.print("Action (A=Add, E=Edit, R=Remove): ");
            String act = in.nextLine().trim().toUpperCase();

            switch (act) {
                case "A" -> {
                    System.out.print("Medicine: ");
                    String med = in.nextLine().trim();

                    System.out.print("Dose: ");
                    String dose = in.nextLine().trim();

                    System.out.print("Frequency: ");
                    String freq = in.nextLine().trim();

                    System.out.print("Notes: ");
                    String notes = in.nextLine().trim();

                    service.addPrescriptionItem(rxId, new PrescriptionItem(med, dose, freq, notes), currentUser);
                    System.out.println("Added.");
                }
                case "E" -> {
                    System.out.print("Item index (1..n): ");
                    int idx = Integer.parseInt(in.nextLine().trim()) - 1;

                    System.out.print("Medicine: ");
                    String med = in.nextLine().trim();

                    System.out.print("Dose: ");
                    String dose = in.nextLine().trim();

                    System.out.print("Frequency: ");
                    String freq = in.nextLine().trim();

                    System.out.print("Notes: ");
                    String notes = in.nextLine().trim();

                    service.editPrescriptionItem(rxId, idx, new PrescriptionItem(med, dose, freq, notes), currentUser);
                    System.out.println("Updated.");
                }
                case "R" -> {
                    System.out.print("Item index (1..n): ");
                    int idx = Integer.parseInt(in.nextLine().trim()) - 1;

                    service.removePrescriptionItem(rxId, idx, currentUser);
                    System.out.println("Removed.");
                }
                default -> System.out.println("Invalid choice.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // --- 9) Nurse: administer medication ---
    public void administerPrescriptionFlow() {
        try {
            System.out.print("Bed ID: ");
            String bedId = in.nextLine().trim();

            System.out.print("Prescription ID: ");
            String rxId = in.nextLine().trim();

            System.out.print("Item index (1..n): ");
            int idx = Integer.parseInt(in.nextLine().trim()) - 1;

            System.out.print("Dose override (Enter to use prescribed dose): ");
            String dose = in.nextLine().trim();

            AdministrationRecord rec = service.administerDose(bedId, rxId, idx, dose, currentUser);
            System.out.println("Admin OK at " + rec.getTime() + " for " + rec.getMedicine() + " " + rec.getDose());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // --- 10) Nurse/Doctor: view medication log for a bed ---
    public void viewAdministrationLogForBedFlow() {
        try {
            System.out.print("Bed ID: ");
            String bedId = in.nextLine().trim();

            List<AdministrationRecord> rows = service.administrationLogForBed(bedId, currentUser);
            if (rows.isEmpty()) {
                System.out.println("No records.");
                return;
            }

            rows.forEach(l -> System.out.println(
                "[" + l.getTime() + "] " + l.getMedicine() + " " + l.getDose() +
                " by @" + l.getAdministeredBy().getUsername()
            ));
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // --- compliance check ---
    public void checkCompliance() {
        try {
            service.checkCompliance();
            System.out.println("Compliance OK.");
        } catch (Exception e) {
            System.out.println("Compliance FAILED: " + e.getMessage());
        }
    }

    // --- show audit log ---
    public void showAuditLogFlow() {
        var log = service.auditLog();
        if (log.isEmpty()) System.out.println("Audit log is empty.");
        else log.forEach(e -> System.out.println(e.toString()));
    }
}
