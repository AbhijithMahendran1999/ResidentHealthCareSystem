package healthcaresystem.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Scanner;
import java.util.List;

import healthcaresystem.exception.NotRosteredException;
import healthcaresystem.exception.UnauthorizedActionException;
import healthcaresystem.model.facility.Bed;
import healthcaresystem.model.facility.CareHome;
import healthcaresystem.model.facility.Room;
import healthcaresystem.model.facility.Ward;
import healthcaresystem.model.people.Doctor;
import healthcaresystem.model.people.Manager;
import healthcaresystem.model.people.Nurse;
import healthcaresystem.model.people.Resident;
import healthcaresystem.model.people.Staff;
import healthcaresystem.model.schedule.Shift;
import healthcaresystem.model.schedule.ShiftType;
import healthcaresystem.model.meds.Prescription;
import healthcaresystem.model.meds.PrescriptionItem;
import healthcaresystem.model.meds.AdministrationRecord;
import healthcaresystem.service.AuthService;
import healthcaresystem.service.CareHomeService;
import healthcaresystem.service.AuditService;

public class MenuController {

    private final CareHomeService service;
    private final AuthService auth;
    private final AuditService audit;
    private final Scanner in;

    private Staff currentUser; // session user

    public MenuController(CareHomeService service, Scanner in) {
        this.service = service;
        this.in = in;
        this.auth = new AuthService(service.getCareHome());
        this.audit = new AuditService(service.getCareHome());
    }

    // Session methods
    public void login() {
        System.out.print("Username: ");
        String u = in.nextLine().trim();
        System.out.print("Password: ");
        String p = in.nextLine().trim();

        Staff s = auth.login(u, p);
        if (s == null) {
            System.out.println("Login failed.");
            auditDenied("LOGIN", "user=" + u);
        } else {
            currentUser = s;
            System.out.println("Logged in as " + s.getClass().getSimpleName() + " " + s.getName());
            auditOk("LOGIN", "user=" + s.getUsername());
        }
    }

    public void logout() {
        if (currentUser == null) {
            System.out.println("Not logged in.");
        } else {
        	auditOk("LOGOUT", "user=" + currentUser.getUsername());
            System.out.println("Logged out: @" + currentUser.getUsername());
            currentUser = null;
        }
    }

    public void printCurrentUser() {
        if (currentUser == null) {
            System.out.println("Not logged in.");
        } else {
            System.out.println("Current user: " + currentUser.getClass().getSimpleName()
                    + " " + currentUser.getName() + " @" + currentUser.getUsername());
        }
    }

    // Auth methods
    private void requireLoggedIn() {
        if (currentUser == null) throw new UnauthorizedActionException("Login required.");
    }

    @SafeVarargs
    private final void requireRole(Class<? extends Staff>... allowed) {
        for (Class<? extends Staff> c : allowed) {
            if (c.isInstance(currentUser)) return;
        }
        throw new UnauthorizedActionException("Action not permitted for role: " +
                (currentUser == null ? "none" : currentUser.getClass().getSimpleName()));
    }

    private void requireRosteredNow() {
        var roster = service.getCareHome().getRoster();
        var now = LocalDateTime.now();
        if (!roster.isRostered(currentUser, now)) {
            throw new NotRosteredException("Not rostered at " + now);
        }
    }

    // Helpers to find
    private Optional<Staff> findStaff(String key) {
        String k = key.trim();
        return service.getCareHome()
                      .getStaff()
                      .stream()
                      .filter(s -> s.getId().equalsIgnoreCase(k)
                                || s.getUsername().equalsIgnoreCase(k))
                      .findFirst();
    }

    private Optional<Resident> findResident(String key) {
        return service.getCareHome().getResidents().stream()
                .filter(r -> r.getId().equalsIgnoreCase(key) || r.getName().equalsIgnoreCase(key))
                .findFirst();
    }

    private Optional<Bed> findBed(String bedId) {
        return service.getCareHome().getWards().stream()
                .flatMap(w -> w.getRooms().stream())
                .flatMap(rm -> rm.getBeds().stream())
                .filter(b -> b.getId().equalsIgnoreCase(bedId))
                .findFirst();
    }

    
    //Audit methods for logging
    private void auditOk(String action, String details) {
        audit.log(currentUser, action, details, true);
    }
    private void auditDenied(String action, String details) {
        audit.log(currentUser, action, details, false);
    }


    // 1) Add new resident to a vacant bed  (Rostered nurse only)
    public void addNewResidentToVacantBedFlow() {
    	final String ACTION = "ADD_RESIDENT_TO_BED";
        try {
            requireLoggedIn();
            requireRole(Manager.class);
//            requireRosteredNow();

            System.out.print("New resident ID: ");
            String id = in.nextLine().trim();
            System.out.print("Name: ");
            String name = in.nextLine().trim();
            System.out.print("Gender (M/F): ");
            String g = in.nextLine().trim().toUpperCase();
            char gender = g.isEmpty() ? 'M' : g.charAt(0);

            System.out.print("Vacant bed ID: ");
            String bedId = in.nextLine().trim();

            var bedOpt = findBed(bedId);
            if (bedOpt.isEmpty()) { System.out.println("Bed not found."); return; }
            Bed bed = bedOpt.get();
            if (bed.getOccupiedBy() != null) { System.out.println("Bed is occupied."); return; }

            Resident r = new Resident(id, name, gender);
            service.addResident(r);
            boolean placed = service.admitResidentToBed(r, bed);

            if (placed) {
                System.out.println("Added resident and placed into bed " + bed.getId());
                auditOk(ACTION, "resident=" + id + " bed=" + bed.getId());
            } else {
                System.out.println("Could not place resident (bed now occupied).");
                auditDenied(ACTION, "resident=" + id + " bed=" + bed.getId() + " FAILED");
            }
        } catch (Exception e) {
            auditDenied(ACTION, "error=" + e.getMessage());
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 2) Add staff (Doctor/Nurse)  (Manager only)
    public void addStaffFlow() {
    	final String ACTION = "ADD_STAFF";
        try {
            requireLoggedIn();
            requireRole(Manager.class);

            System.out.print("Role (N/D/M): ");
            String roleIn = in.nextLine().trim().toUpperCase();
            System.out.print("Staff ID: ");
            String id = in.nextLine().trim();
            System.out.print("Name: ");
            String name = in.nextLine().trim();
            System.out.print("Username: ");
            String username = in.nextLine().trim();

            Staff staff;
            switch (roleIn) {
                case "N": staff = new Nurse(id, name, username); break;
                case "D": staff = new Doctor(id, name, username); break;
                case "M": staff = new Manager(id, name, username); break;
                default:  System.out.println("Invalid role."); return;
            }

            System.out.print("Set initial password (optional, Enter to skip): ");
            String pw = in.nextLine();
            if (!pw.isEmpty()) staff.setPassword(pw);

            service.addStaff(staff);
            System.out.println("Added " + staff.getClass().getSimpleName() + " " + staff.getName());
            auditOk(ACTION, "staffId=" + id + " role=" + staff.getClass().getSimpleName());
        } catch (Exception e) {
            auditDenied(ACTION, "error=" + e.getMessage());
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 3) Modify staff password  (Manager only)
    public void modifyStaffPasswordFlow() {
    	final String ACTION = "MODIFY_STAFF_PASSWORD";
        try {
            requireLoggedIn();
            requireRole(Manager.class);

            System.out.print("Target staff ID or username: ");
            String key = in.nextLine().trim();
            var sOpt = findStaff(key);
            if (sOpt.isEmpty()) { System.out.println("Staff not found."); return; }
            Staff target = sOpt.get();

            System.out.print("New password: ");
            String pw = in.nextLine();
            target.setPassword(pw);
            System.out.println("Password updated for @" + target.getUsername());
            auditOk(ACTION, "staff=" + target.getId());
        } catch (Exception e) {
        	auditDenied(ACTION, "error=" + e.getMessage());
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 4) Allocate/Modify shift of a staff member (Manager only, rollback on breach)
    public void allocateOrModifyShiftFlow() {
    	final String ACTION = "ASSIGN_SHIFT";
        try {
            requireLoggedIn();
            requireRole(Manager.class);

            System.out.print("Staff ID or username: ");
            String key = in.nextLine().trim();
            var sOpt = findStaff(key);
            if (sOpt.isEmpty()) { System.out.println("Staff not found."); return; }
            Staff staff = sOpt.get();

            System.out.print("Date (YYYY-MM-DD): ");
            LocalDate day = LocalDate.parse(in.nextLine().trim());

            System.out.print("Shift type (DAY/EVE): ");
            ShiftType type = ShiftType.valueOf(in.nextLine().trim().toUpperCase());

            Shift proposed = new Shift(staff, day, type);
            var roster = service.getCareHome().getRoster();
            roster.addShift(proposed);

            try {
                // Only ensure the nurse doesn't exceed 8h on that day.
                roster.ensureNurseDailyHoursOk(staff, day);
                System.out.println("Shift assigned.");
                auditOk(ACTION, "staff=" + staff.getId() + " day=" + day + " type=" + type);
            } catch (Exception ce) {
                roster.removeShift(proposed);  // rollback
                System.out.println("Denied: " + ce.getMessage());
                auditDenied(ACTION, "staff=" + staff.getId() + " day=" + day + " type=" + type + " reason=" + ce.getMessage());
            }
        } catch (Exception e) {
        	auditDenied(ACTION, "error=" + e.getMessage());
            System.out.println("Error: " + e.getMessage());
        }
    }


    // 5) Check resident details in a bed (Doctor/Nurse, rostered)
    public void viewResidentInBedFlow() {
    	final String ACTION = "VIEW_RESIDENT_IN_BED";
        try {
            requireLoggedIn();
            requireRole(Nurse.class, Doctor.class);
            requireRosteredNow();

            System.out.print("Bed ID: ");
            String bedId = in.nextLine().trim();
            var bedOpt = findBed(bedId);
            if (bedOpt.isEmpty()) { System.out.println("Bed not found."); return; }
            Bed b = bedOpt.get();

            Resident r = b.getOccupiedBy();
            if (r == null) {
                System.out.println("Bed " + b.getId() + " is VACANT.");
                auditOk(ACTION, "bed=" + b.getId() + " vacant");
            } else {
                System.out.println("Bed " + b.getId() + " → Resident: " + r.getId() + " " + r.getName() + " (" + r.getGender() + ")");
                auditOk(ACTION, "bed=" + b.getId() + " resident=" + r.getId());
            }
        } catch (Exception e) {
        	auditDenied(ACTION, "error=" + e.getMessage());
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 6) Attach prescription to resident in bed  (Doctor/Nurse, rostered) TODO
    public void attachPrescriptionFlow() {
    	final String ACTION = "ATTACH_PRESCRIPTION";
        try {
            requireLoggedIn();
            requireRole(Doctor.class);
            requireRosteredNow();

            System.out.print("Bed ID: ");
            String bedId = in.nextLine().trim();
            var bedOpt = findBed(bedId);
            if (bedOpt.isEmpty()) { System.out.println("Bed not found."); return; }
            var bed = bedOpt.get();

            var patient = bed.getOccupiedBy();
            if (patient == null) { System.out.println("No patient in this bed."); return; }

            String pid = "RX-" + System.currentTimeMillis();
            Prescription p = new Prescription(pid, patient, (Doctor) currentUser);

            System.out.println("Enter medicines (blank name to finish):");
            while (true) {
                System.out.print("Medicine name: ");
                String med = in.nextLine().trim();
                if (med.isEmpty()) break;

                System.out.print("Dose: ");
                String dose = in.nextLine().trim();
                System.out.print("Frequency: ");
                String freq = in.nextLine().trim();
                System.out.print("Notes (optional): ");
                String notes = in.nextLine().trim();

                p.addItem(new PrescriptionItem(med, dose, freq, notes));
            }

            service.getCareHome().getPrescriptions().add(p);
            System.out.println("Prescription added: " + p);
            auditOk(ACTION, "rx=" + p.getId() + " patient=" + patient.getId() + " items=" + p.getItems().size());
        } catch (Exception e) {
        	auditDenied(ACTION, "error=" + e.getMessage());
            System.out.println("Error: " + e.getMessage());
        }
    }


    // 7) Move resident to a different bed (Nurse only, rostered)
    public void moveResidentToDifferentBedFlow() {
    	final String ACTION = "MOVE_RESIDENT_BED";
        try {
            requireLoggedIn();
            requireRole(Nurse.class);
            requireRosteredNow();

            System.out.print("FROM bed ID: ");
            String fromId = in.nextLine().trim();
            System.out.print("TO bed ID: ");
            String toId = in.nextLine().trim();

            var fromOpt = findBed(fromId);
            var toOpt = findBed(toId);
            if (fromOpt.isEmpty() || toOpt.isEmpty()) { System.out.println("Bed not found."); return; }
            Bed from = fromOpt.get();
            Bed to = toOpt.get();

            Resident r = from.getOccupiedBy();
            if (r == null) { System.out.println("Source bed is empty."); return; }
            if (to.getOccupiedBy() != null) { System.out.println("Destination bed is occupied."); return; }

            // move
            from.setOccupiedBy(null);
            to.setOccupiedBy(r);
            System.out.println("Moved " + r.getName() + " from " + from.getId() + " to " + to.getId());
            auditOk(ACTION, "resident=" + r.getId() + " from=" + from.getId() + " to=" + to.getId());
        } catch (Exception e) {
            auditDenied(ACTION, "error=" + e.getMessage());
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 8) Update administered prescription details
    public void updatePrescriptionDetailsFlow() {
        final String ACTION = "UPDATE_PRESCRIPTION";
        try {
            requireLoggedIn(); requireRole(Doctor.class); requireRosteredNow();

            System.out.print("Bed ID: "); String bedId = in.nextLine().trim();
            var bedOpt = findBed(bedId);
            if (bedOpt.isEmpty()) { System.out.println("Bed not found."); auditDenied(ACTION, "bed=" + bedId + " NOT_FOUND"); return; }
            Bed bed = bedOpt.get();

            Resident patient = bed.getOccupiedBy();
            if (patient == null) { System.out.println("No resident in this bed."); auditDenied(ACTION, "bed=" + bedId + " NO_PATIENT"); return; }

            var prescriptions = service.getCareHome().getPrescriptions().stream()
                    .filter(p -> p.getPatient().getId().equalsIgnoreCase(patient.getId()))
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .collect(java.util.stream.Collectors.toList());

            if (prescriptions.isEmpty()) { System.out.println("No prescriptions found."); auditDenied(ACTION, "patient=" + patient.getId() + " NO_RX"); return; }

            System.out.println("Prescriptions:");
            for (int i = 0; i < prescriptions.size(); i++) System.out.println((i + 1) + ") " + prescriptions.get(i));
            System.out.print("Select prescription #: ");
            int pIndex = Integer.parseInt(in.nextLine().trim()) - 1;
            if (pIndex < 0 || pIndex >= prescriptions.size()) { System.out.println("Invalid number."); auditDenied(ACTION, "bad index"); return; }

            Prescription selected = prescriptions.get(pIndex);
            var items = selected.getItems();

            System.out.println("Existing medicines:");
            for (int i = 0; i < items.size(); i++) System.out.println((i + 1) + ") " + items.get(i));

            System.out.println("Choose action: A) Add  E) Edit  R) Remove");
            String choice = in.nextLine().trim().toUpperCase();

            switch (choice) {
                case "A": {
                    System.out.print("Medicine: ");  String med = in.nextLine().trim();
                    System.out.print("Dose: ");      String dose = in.nextLine().trim();
                    System.out.print("Frequency: "); String freq = in.nextLine().trim();
                    System.out.print("Notes: ");     String notes = in.nextLine().trim();
                    selected.addItem(new PrescriptionItem(med, dose, freq, notes));
                    System.out.println("Added new medicine: " + med);
                    auditOk(ACTION, "rx=" + selected.getId() + " ADD " + med);
                    break;
                }
                case "E": {
                    if (items.isEmpty()) { System.out.println("No items to edit."); auditDenied(ACTION, "rx=" + selected.getId() + " EDIT_EMPTY"); return; }
                    System.out.print("Select item #: ");
                    int ei = Integer.parseInt(in.nextLine().trim()) - 1;
                    if (ei < 0 || ei >= items.size()) { System.out.println("Invalid item."); auditDenied(ACTION, "rx=" + selected.getId() + " EDIT_BAD_INDEX"); return; }

                    PrescriptionItem cur = items.get(ei);
                    System.out.println("Editing " + cur.getMedicine());
                    System.out.print("New dose (Enter to keep '" + cur.getDose() + "'): ");
                    String newDose = in.nextLine().trim();
                    System.out.print("New frequency (Enter to keep '" + cur.getFrequency() + "'): ");
                    String newFreq = in.nextLine().trim();
                    System.out.print("New notes (Enter to keep '" + cur.getNotes() + "'): ");
                    String newNotes = in.nextLine().trim();

                    PrescriptionItem upd = new PrescriptionItem(
                            cur.getMedicine(),
                            newDose.isEmpty() ? cur.getDose() : newDose,
                            newFreq.isEmpty() ? cur.getFrequency() : newFreq,
                            newNotes.isEmpty() ? cur.getNotes() : newNotes);

                    items.set(ei, upd);
                    System.out.println("Updated medicine entry.");
                    auditOk(ACTION, "rx=" + selected.getId() + " EDIT " + cur.getMedicine());
                    break;
                }
                case "R": {
                    if (items.isEmpty()) { System.out.println("No items to remove."); auditDenied(ACTION, "rx=" + selected.getId() + " REMOVE_EMPTY"); return; }
                    System.out.print("Select item #: ");
                    int ri = Integer.parseInt(in.nextLine().trim()) - 1;
                    if (ri < 0 || ri >= items.size()) { System.out.println("Invalid item."); auditDenied(ACTION, "rx=" + selected.getId() + " REMOVE_BAD_INDEX"); return; }
                    PrescriptionItem removed = items.remove(ri);
                    System.out.println("Removed " + removed.getMedicine());
                    auditOk(ACTION, "rx=" + selected.getId() + " REMOVE " + removed.getMedicine());
                    break;
                }
                default:
                    System.out.println("Invalid choice.");
                    auditDenied(ACTION, "invalid choice=" + choice);
            }
        } catch (Exception e) {
            auditDenied(ACTION, "error=" + e.getMessage());
            System.out.println("Error: " + e.getMessage());
        }
    }


    // 9) Administer prescription (log patient, med, time, dose, staff)
    public void administerPrescriptionFlow() {
    	final String ACTION = "ADMINISTER_MEDICATION";
        try {
            requireLoggedIn();
            requireRole(Nurse.class);
            requireRosteredNow();

            System.out.print("Bed ID: ");
            String bedId = in.nextLine().trim();
            var bedOpt = findBed(bedId);
            if (bedOpt.isEmpty()) { System.out.println("Bed not found."); return; }
            var bed = bedOpt.get();

            var patient = bed.getOccupiedBy();
            if (patient == null) { System.out.println("No patient in this bed."); return; }

            // Find prescriptions for this patient (latest first)
            CareHome ch = service.getCareHome();
            List<Prescription> rxList = ch.getPrescriptions().stream()
                    .filter(rx -> rx.getPatient().getId().equalsIgnoreCase(patient.getId()))
                    .sorted((a,b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .toList();

            if (rxList.isEmpty()) {
                System.out.println("No prescriptions found for " + patient.getName() + ".");
                return;
            }

            // List prescriptions
            System.out.println("Prescriptions for " + patient.getName() + ":");
            for (int i = 0; i < rxList.size(); i++) {
                Prescription rx = rxList.get(i);
                System.out.println((i+1) + ") " + rx.getId() + " by Dr. " + rx.getDoctor().getName()
                        + " (" + rx.getItems().size() + " items)");
            }

            System.out.print("Select prescription #: ");
            String rxi = in.nextLine().trim();
            int rxIndex;
            try { rxIndex = Integer.parseInt(rxi) - 1; } catch (NumberFormatException nfe) { System.out.println("Invalid number."); return; }
            if (rxIndex < 0 || rxIndex >= rxList.size()) { System.out.println("Out of range."); return; }

            Prescription selected = rxList.get(rxIndex);
            if (selected.getItems().isEmpty()) {
                System.out.println("This prescription has no items.");
                return;
            }

            // List items
            System.out.println("Items:");
            for (int i = 0; i < selected.getItems().size(); i++) {
                PrescriptionItem it = selected.getItems().get(i);
                System.out.println((i+1) + ") " + it.toString());
            }

            System.out.print("Select item #: ");
            String iti = in.nextLine().trim();
            int itemIndex;
            try { itemIndex = Integer.parseInt(iti) - 1; } catch (NumberFormatException nfe) { System.out.println("Invalid number."); return; }
            if (itemIndex < 0 || itemIndex >= selected.getItems().size()) { System.out.println("Out of range."); return; }

            PrescriptionItem item = selected.getItems().get(itemIndex);

            // Dose: default to prescription dose, allow override quickly
            System.out.print("Dose to administer (Enter for '" + item.getDose() + "'): ");
            String doseIn = in.nextLine().trim();
            String dose = doseIn.isEmpty() ? item.getDose() : doseIn;

            // Record administration (time = now)
            AdministrationRecord record = new AdministrationRecord(
                    selected.getId(), item.getMedicine(), dose, currentUser);

            ch.getAdministrations().add(record);

            System.out.println("Administered: " + item.getMedicine() + " " + dose +
                    " to " + patient.getName() + " at " + record.getTime() +
                    " by @" + currentUser.getUsername());
            auditOk(ACTION, "rx=" + selected.getId() + " med=" + item.getMedicine() + " dose=" + dose + " patient=" + patient.getId());
        } catch (Exception e) {
        	auditDenied("ADMINISTER_MEDICATION", "error=" + e.getMessage());
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    // 10) View Medicine Administration Log
    public void viewAdministrationLogForBedFlow() {
        try {
            requireLoggedIn();
            requireRole(Nurse.class, Doctor.class);
            requireRosteredNow();

            System.out.print("Bed ID: ");
            String bedId = in.nextLine().trim();
            var bedOpt = findBed(bedId);
            if (bedOpt.isEmpty()) { System.out.println("Bed not found."); return; }
            var bed = bedOpt.get();

            var patient = bed.getOccupiedBy();
            if (patient == null) { System.out.println("No patient in this bed."); return; }

            var logs = service.getCareHome().getAdministrations().stream()
                    .filter(a -> service.getCareHome().getPrescriptions().stream()
                            .filter(p -> p.getId().equals(a.getPrescriptionId()))
                            .anyMatch(p -> p.getPatient().getId().equals(patient.getId())))
                    .sorted((a,b) -> a.getTime().compareTo(b.getTime()))
                    .toList();

            if (logs.isEmpty()) {
                System.out.println("No administration records for " + patient.getName() + ".");
                return;
            }

            System.out.println("Administration log for " + patient.getName() + ":");
            logs.forEach(l ->
                System.out.println(" - [" + l.getTime() + "] " + l.getMedicine() + " " + l.getDose()
                                   + " by @" + l.getAdministeredBy().getUsername())
            );
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    } 
    
    
    // To check compliance
    public void checkCompliance() {
    	final String ACTION = "CHECK_COMPLIANCE";
        try {
            service.getCareHome().checkCompliance();
            System.out.println("Compliance OK for the next 7 days from first scheduled day.");
            auditOk(ACTION, "ok");
        } catch (Exception e) {
            System.out.println("Compliance FAILED: " + e.getMessage());
            auditDenied(ACTION, "fail=" + e.getMessage());
        }
    }
    
    // Calling during startup
    
    public void ensureDefaultAdmin() {
    	CareHome ch = service.getCareHome();
        boolean hasAnyStaff = !ch.getStaff().isEmpty();
        if (hasAnyStaff) return;

        // Create a default manager login
        Manager admin = new Manager("M0", "Default Manager", "admin");
        admin.setPassword("admin123"); // dummy password
        service.addStaff(admin);

        System.out.println("Initialized default manager login -> username: 'admin', password: 'admin123'");
        System.out.println("Use 'L' to log in, then option 3 to change the password.");
    }
    

    public void ensureDefaultFacility() {
        CareHome ch = service.getCareHome();
        if (!ch.getWards().isEmpty()) return; // already has data

        // Ward 1: rooms with 1,2,4,4,4,4 beds
        addWardWithRoomsAndBeds("W1", new int[]{1,2,4,4,4,4});

        // Ward 2: rooms with 1,2,4,4,4,4 beds
        addWardWithRoomsAndBeds("W2", new int[]{1,2,4,4,4,4});

        System.out.println("Initialized default facility: 2 wards, 6 rooms each, beds 1–4 per room.");
    }

    private void addWardWithRoomsAndBeds(String wardId, int[] bedsPerRoom) {
        Ward ward = new Ward(wardId);
        for (int i = 0; i < bedsPerRoom.length; i++) {
            String roomId = "R" + (i + 1);
            Room room = new Room(roomId);

            int bedCount = bedsPerRoom[i];
            for (int b = 1; b <= bedCount; b++) {
                // ID pattern: W1-R3-B2
                String bedId = wardId + "-" + roomId + "-B" + b;

                room.getBeds().add(new Bed(bedId));
            }
            ward.getRooms().add(room);
        }
        service.getCareHome().getWards().add(ward);
    }
    
    public void showAuditLogFlow() {
        var log = service.getCareHome().getAuditLog();
        if (log.isEmpty()) { System.out.println("Audit log is empty."); return; }
        log.forEach(e -> System.out.println(e.toString()));
    }

}
