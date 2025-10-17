package healthcaresystem.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import healthcaresystem.exception.NotRosteredException;
import healthcaresystem.exception.UnauthorizedActionException;
import healthcaresystem.model.facility.*;
import healthcaresystem.model.meds.AdministrationRecord;
import healthcaresystem.model.meds.Prescription;
import healthcaresystem.model.meds.PrescriptionItem;
import healthcaresystem.model.people.*;
import healthcaresystem.model.schedule.Roster;
import healthcaresystem.model.schedule.*;
import healthcaresystem.model.schedule.ShiftType;
import healthcaresystem.model.audit.AuditEntry;

// Application service: central place for domain actions
public class CareHomeService {
    private final CareHome careHome;
    private final AuditService audit;

    public CareHomeService() {
        this.careHome = new CareHome();
        this.audit = new AuditService(this.careHome);
    }

    public CareHomeService(CareHome ch) {
        this.careHome = (ch == null) ? new CareHome() : ch;
        this.audit = new AuditService(this.careHome);
    }

    public CareHome getCareHome() { return careHome; }

    // ---------- Auth/rule helpers ----------
    private void requireLoggedIn(Staff actor) {
        if (actor == null) throw new UnauthorizedActionException("Login required.");
    }

    @SafeVarargs
    private final void requireRole(Staff actor, Class<? extends Staff>... roles) {
        requireLoggedIn(actor);
        for (Class<? extends Staff> c : roles) if (c.isInstance(actor)) return;
        throw new UnauthorizedActionException("Not allowed for role: " +
                (actor == null ? "none" : actor.getClass().getSimpleName()));
    }

    private void requireRosteredNow(Staff actor) {
        requireLoggedIn(actor);
        Roster r = careHome.getRoster();
        LocalDateTime now = LocalDateTime.now();
        if (!r.isRostered(actor, now)) throw new NotRosteredException("Not rostered at " + now);
    }

    // ---------- Finders ----------
    private Optional<Staff> findStaff(String key) {
        String k = key.trim();
        return careHome.getStaff().stream()
            .filter(s -> s.getUsername().equalsIgnoreCase(k) || s.getId().equalsIgnoreCase(k))
            .findFirst();
    }

    private Optional<Resident> findResidentByIdOrName(String key) {
        String k = key.trim();
        return careHome.getResidents().stream()
            .filter(r -> r.getId().equalsIgnoreCase(k) || r.getName().equalsIgnoreCase(k))
            .findFirst();
    }

    private Optional<Bed> findBed(String bedId) {
        String k = bedId.trim();
        return careHome.getWards().stream()
            .flatMap(w -> w.getRooms().stream())
            .flatMap(r -> r.getBeds().stream())
            .filter(b -> b.getId().equalsIgnoreCase(k))
            .findFirst();
    }

    private List<Prescription> prescriptionsForResident(Resident r) {
        return careHome.getPrescriptions().stream()
            .filter(p -> p.getPatient().getId().equalsIgnoreCase(r.getId()))
            .sorted((a,b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .collect(Collectors.toList());
    }

    // ---------- Seeding (used by CLI & FX) ----------
    public void ensureDefaultAdmin() {
        if (!careHome.getStaff().isEmpty()) return;
        Manager admin = new Manager("M0", "AdminManager", "admin");
        admin.setPassword("admin123");
        addStaff(admin);
        audit.log(null, "SEED_MANAGER", "admin/admin123", true);
    }

    public void ensureDefaultFacility() {
        if (!careHome.getWards().isEmpty()) return;
        addWardWithRoomsAndBeds("W1", new int[]{1,2,4,4,4,4});
        addWardWithRoomsAndBeds("W2", new int[]{1,2,4,4,4,4});
        audit.log(null, "SEED_FACILITY", "2 wards × 6 rooms", true);
    }

    private void addWardWithRoomsAndBeds(String wardId, int[] bedsPerRoom) {
        Ward ward = new Ward(wardId);
        for (int i = 0; i < bedsPerRoom.length; i++) {
            Room room = new Room("R" + (i + 1));
            for (int b = 1; b <= bedsPerRoom[i]; b++) {
                room.getBeds().add(new Bed(wardId + "-R" + (i + 1) + "-B" + b));
            }
            ward.getRooms().add(room);
        }
        careHome.getWards().add(ward);
    }

    // ---------- Simple CRUD ----------
    public void addResident(Resident r) { careHome.getResidents().add(r); }
    public void addStaff(Staff s) { careHome.getStaff().add(s); }

    // Places resident in bed if vacant
    public boolean admitResidentToBed(Resident resident, Bed bed) {
        if (bed.getOccupiedBy() != null) return false;
        bed.setOccupiedBy(resident);
        return true;
    }

    // ---------- Use-cases ----------
    // Manager: create resident and place into a specific bed
    public void addResidentToBed(String resId, String name, char gender, String bedId, Staff actor) {
        requireRole(actor, Manager.class);
        Bed bed = findBed(bedId).orElseThrow(() -> new IllegalArgumentException("Bed not found: " + bedId));
        if (bed.getOccupiedBy() != null) throw new IllegalStateException("Bed already occupied: " + bedId);

        Resident r = new Resident(resId, name, gender);
        addResident(r);
        boolean ok = admitResidentToBed(r, bed);
        if (!ok) throw new IllegalStateException("Placement failed (occupied).");

        audit.log(actor, "ADD_RESIDENT_TO_BED", "resident=" + r.getId() + " bed=" + bed.getId(), true);
    }

    // Manager: add staff with credentials (Doctor/Nurse)
    public void addStaffWithCredentials(String role, String id, String name, String username, String password, Staff actor) {
        requireRole(actor, Manager.class);
        Staff s;
        if ("DOCTOR".equalsIgnoreCase(role))      s = new Doctor(id, name, username);
        else if ("NURSE".equalsIgnoreCase(role))  s = new Nurse(id, name, username);
        else throw new IllegalArgumentException("Role must be Doctor or Nurse.");
        s.setPassword(password);
        addStaff(s);
        audit.log(actor, "ADD_STAFF", "role=" + role + " id=" + id, true);
    }

    // Manager: modify a staff member’s password
    public void modifyStaffPassword(String key, String newPassword, Staff actor) {
        requireRole(actor, Manager.class);
        Staff target = findStaff(key).orElseThrow(() -> new IllegalArgumentException("No staff found: " + key));
        target.setPassword(newPassword);
        audit.log(actor, "MODIFY_STAFF_PASSWORD", "staff=" + target.getId(), true);
    }

    // Manager: assign (or replace) a shift, keeping nurses ≤ 8h/day
    public void assignOrReplaceShift(String staffKey, LocalDate day, ShiftType type, boolean replace, Staff actor) {
        requireRole(actor, Manager.class);
        Staff staff = findStaff(staffKey).orElseThrow(() -> new IllegalArgumentException("No staff: " + staffKey));
        var roster = careHome.getRoster();

        if (replace) {
            roster.getShifts().removeIf(s -> s.getStaff().equals(staff) && s.getDay().equals(day));
            roster.addShift(new healthcaresystem.model.schedule.Shift(staff, day, type));
            audit.log(actor, "ASSIGN_SHIFT", "REPLACE " + staff.getId() + " " + day + " " + type, true);
            return;
        }

        if (staff instanceof Nurse) {
            boolean hasShiftToday = roster.getShifts().stream()
                    .anyMatch(s -> s.getStaff().equals(staff) && s.getDay().equals(day));
            if (hasShiftToday)
                throw new IllegalStateException("Nurse already has a shift on " + day + " (max 8h/day). Use replace.");
        }

        roster.addShift(new healthcaresystem.model.schedule.Shift(staff, day, type));
        audit.log(actor, "ASSIGN_SHIFT", staff.getId() + " " + day + " " + type, true);
    }

    // Nurse/Doctor: view the resident in a bed (requires rostered now)
    public Optional<Resident> viewResidentInBed(String bedId, Staff actor) {
        requireRole(actor, Nurse.class, Doctor.class);
        requireRosteredNow(actor);
        Bed b = findBed(bedId).orElseThrow(() -> new IllegalArgumentException("Bed not found: " + bedId));
        Resident r = b.getOccupiedBy();
        audit.log(actor, "VIEW_RESIDENT_IN_BED", "bed=" + bedId + " " + (r == null ? "VACANT" : "resident=" + r.getId()), true);
        return Optional.ofNullable(r);
    }

    // Doctor: create a prescription for the patient in a bed
    public Prescription attachPrescriptionToBed(String bedId, List<PrescriptionItem> items, Staff actor) {
        requireRole(actor, Doctor.class);
        requireRosteredNow(actor);

        Bed bed = findBed(bedId).orElseThrow(() -> new IllegalArgumentException("Bed not found: " + bedId));
        Resident patient = Optional.ofNullable(bed.getOccupiedBy())
                .orElseThrow(() -> new IllegalStateException("No resident in bed."));

        String pid = "RX-" + System.currentTimeMillis();
        Prescription p = new Prescription(pid, patient, (Doctor) actor);
        if (items != null) items.forEach(p::addItem);

        careHome.getPrescriptions().add(p);
        audit.log(actor, "ATTACH_PRESCRIPTION", "rx=" + p.getId() + " patient=" + patient.getId() + " items=" + p.getItems().size(), true);
        return p;
    }

    // Nurse: move a resident from one bed to another
    public void moveResident(String fromBedId, String toBedId, Staff actor) {
        requireRole(actor, Nurse.class);
        requireRosteredNow(actor);

        Bed from = findBed(fromBedId).orElseThrow(() -> new IllegalArgumentException("From-bed not found."));
        Bed to   = findBed(toBedId).orElseThrow(() -> new IllegalArgumentException("To-bed not found."));

        Resident r = Optional.ofNullable(from.getOccupiedBy())
                .orElseThrow(() -> new IllegalStateException("Source bed is empty."));
        if (to.getOccupiedBy() != null) throw new IllegalStateException("Destination bed is occupied.");

        from.setOccupiedBy(null);
        to.setOccupiedBy(r);

        audit.log(actor, "MOVE_RESIDENT_BED", "resident=" + r.getId() + " from=" + from.getId() + " to=" + to.getId(), true);
    }

    // Doctor: add/edit/remove prescription items
    public void addPrescriptionItem(String rxId, PrescriptionItem item, Staff actor) {
        requireRole(actor, Doctor.class);
        requireRosteredNow(actor);
        Prescription p = findRx(rxId);
        p.addItem(item);
        audit.log(actor, "RX_ADD_ITEM", "rx=" + rxId + " med=" + item.getMedicine(), true);
    }

    public void editPrescriptionItem(String rxId, int index, PrescriptionItem newItem, Staff actor) {
        requireRole(actor, Doctor.class);
        requireRosteredNow(actor);
        Prescription p = findRx(rxId);
        if (index < 0 || index >= p.getItems().size()) throw new IllegalArgumentException("Invalid item index.");
        p.getItems().set(index, newItem);
        audit.log(actor, "RX_EDIT_ITEM", "rx=" + rxId + " idx=" + index, true);
    }

    public void removePrescriptionItem(String rxId, int index, Staff actor) {
        requireRole(actor, Doctor.class);
        requireRosteredNow(actor);
        Prescription p = findRx(rxId);
        if (index < 0 || index >= p.getItems().size()) throw new IllegalArgumentException("Invalid item index.");
        PrescriptionItem removed = p.getItems().remove(index);
        audit.log(actor, "RX_REMOVE_ITEM", "rx=" + rxId + " med=" + removed.getMedicine(), true);
    }

    // Nurse: record a medication administration
    public AdministrationRecord administerDose(String bedId, String rxId, int itemIndex, String doseOverride, Staff actor) {
        requireRole(actor, Nurse.class);
        requireRosteredNow(actor);

        Bed bed = findBed(bedId).orElseThrow(() -> new IllegalArgumentException("Bed not found."));
        Resident patient = Optional.ofNullable(bed.getOccupiedBy())
                .orElseThrow(() -> new IllegalStateException("No patient in this bed."));

        Prescription rx = findRx(rxId);
        if (!rx.getPatient().getId().equalsIgnoreCase(patient.getId()))
            throw new IllegalArgumentException("Prescription does not belong to the bed’s patient.");
        if (itemIndex < 0 || itemIndex >= rx.getItems().size())
            throw new IllegalArgumentException("Invalid item index.");

        PrescriptionItem it = rx.getItems().get(itemIndex);
        String dose = (doseOverride == null || doseOverride.isBlank()) ? it.getDose() : doseOverride;

        AdministrationRecord rec = new AdministrationRecord(rx.getId(), it.getMedicine(), dose, actor);
        careHome.getAdministrations().add(rec);

        audit.log(actor, "ADMINISTER_MEDICATION", "rx=" + rxId + " med=" + it.getMedicine() + " dose=" + dose + " patient=" + patient.getId(), true);
        return rec;
    }

    // Nurse/Doctor: get administration log for the patient in a bed
    public List<AdministrationRecord> administrationLogForBed(String bedId, Staff actor) {
        requireRole(actor, Nurse.class, Doctor.class);
        requireRosteredNow(actor);

        Bed bed = findBed(bedId).orElseThrow(() -> new IllegalArgumentException("Bed not found."));
        Resident patient = Optional.ofNullable(bed.getOccupiedBy())
                .orElseThrow(() -> new IllegalStateException("No resident in this bed."));

        Set<String> rxIds = careHome.getPrescriptions().stream()
                .filter(p -> p.getPatient().getId().equalsIgnoreCase(patient.getId()))
                .map(Prescription::getId)
                .collect(Collectors.toSet());

        return careHome.getAdministrations().stream()
                .filter(a -> rxIds.contains(a.getPrescriptionId()))
                .sorted(Comparator.comparing(AdministrationRecord::getTime))
                .collect(Collectors.toList());
    }

    // Manager: list roster filtered by role (sorted by day/type/name)
    public List<Shift> listRosterForRole(Staff actor, Class<? extends Staff> role) {
        requireRole(actor, Manager.class);
        return careHome.getRoster().getShifts().stream()
                .filter(s -> role.isInstance(s.getStaff()))
                .sorted(java.util.Comparator
                        .comparing(healthcaresystem.model.schedule.Shift::getDay)
                        .thenComparing(healthcaresystem.model.schedule.Shift::getType)
                        .thenComparing(s -> s.getStaff().getName(), String.CASE_INSENSITIVE_ORDER))
                .collect(java.util.stream.Collectors.toList());
    }

    // Manager: list staff filtered by role
    public List<Staff> listStaffByRole(Staff actor, Class<? extends Staff> role) {
        requireRole(actor, Manager.class);
        return careHome.getStaff().stream()
                .filter(role::isInstance)
                .sorted(java.util.Comparator.comparing(Staff::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(java.util.stream.Collectors.toList());
    }

    // Anyone: run weekly compliance checks
    public void checkCompliance() {
        careHome.checkCompliance();
        audit.log(null, "CHECK_COMPLIANCE", "run", true);
    }

    // Returns a copy of the audit log
    public List<AuditEntry> auditLog() {
        return new ArrayList<>(careHome.getAuditLog());
    }

    // ---------- helpers ----------
    private Prescription findRx(String rxId) {
        return careHome.getPrescriptions().stream()
                .filter(p -> p.getId().equals(rxId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Prescription not found: " + rxId));
    }
}
