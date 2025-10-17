package healthcaresystem.model.schedule;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import healthcaresystem.exception.ComplianceException;
import healthcaresystem.model.people.*;

public class Roster implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Shift> shifts = new ArrayList<>();

    // Getters/Setters
    public List<Shift> getShifts() { return shifts; }
    public void setShifts(List<Shift> shifts) { this.shifts = shifts; }

    // Add or remove shifts
    public void addShift(Shift shift) { shifts.add(shift); }
    public boolean removeShift(Shift shift) { return shifts.remove(shift); }

    // Check if staff is rostered at a given time
    public boolean isRostered(Staff staff, LocalDateTime at) {
        LocalTime t = at.toLocalTime();
        return shifts.stream()
                .filter(s -> s.getDay().equals(at.toLocalDate())
                          && s.getStaff().getId().equalsIgnoreCase(staff.getId()))
                .anyMatch(s -> covers(s.getType(), t));
    }

    // Helper: defines what hours each shift covers
    private boolean covers(ShiftType type, LocalTime t) {
        switch (type) {
            case DAY: return !t.isBefore(LocalTime.of(8, 0)) && t.isBefore(LocalTime.of(16, 0)); // 08:00–16:00
            case EVE: return !t.isBefore(LocalTime.of(14, 0)) && t.isBefore(LocalTime.of(22, 0)); // 14:00–22:00
            default:  return false;
        }
    }

    // Ensure at least one doctor is assigned for each of the next 7 days
    public void ensureDailyDoctorHour() {
        if (shifts.isEmpty()) throw new ComplianceException("No shifts scheduled.");
        LocalDate start = shifts.stream()
                .map(Shift::getDay)
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new ComplianceException("No shift dates."));
        for (int i = 0; i < 7; i++) {
            LocalDate day = start.plusDays(i);
            boolean hasDoctor = shifts.stream()
                    .anyMatch(s -> s.getDay().equals(day) && (s.getStaff() instanceof Doctor));
            if (!hasDoctor) throw new ComplianceException("No doctor assigned on " + day + ".");
        }
    }

    // Ensure nurse coverage for both DAY and EVE shifts without exceeding 8h/day
    public void ensureNurseShiftsAndHours() {
        if (shifts.isEmpty()) throw new ComplianceException("No shifts scheduled.");
        LocalDate start = shifts.stream()
                .map(Shift::getDay)
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new ComplianceException("No shift dates."));
        for (int i = 0; i < 7; i++) {
            LocalDate day = start.plusDays(i);

            boolean dayCovered = shifts.stream().anyMatch(s ->
                    s.getDay().equals(day) &&
                    s.getStaff() instanceof Nurse &&
                    s.getType() == ShiftType.DAY);

            boolean eveCovered = shifts.stream().anyMatch(s ->
                    s.getDay().equals(day) &&
                    s.getStaff() instanceof Nurse &&
                    s.getType() == ShiftType.EVE);

            if (!dayCovered || !eveCovered)
                throw new ComplianceException("Nurse coverage missing on " + day + " (needs DAY and EVE).");

            // No nurse should work both DAY and EVE shifts on the same day
            shifts.stream()
                    .filter(s -> s.getDay().equals(day) && s.getStaff() instanceof Nurse)
                    .map(Shift::getStaff)
                    .distinct()
                    .forEach(nurse -> {
                        int hours = shifts.stream()
                                .filter(s -> s.getDay().equals(day) && s.getStaff().equals(nurse))
                                .mapToInt(s -> s.getType() == ShiftType.DAY || s.getType() == ShiftType.EVE ? 8 : 0)
                                .sum();
                        if (hours > 8)
                            throw new ComplianceException("Nurse over 8 hours on " + day + ".");
                    });
        }
    }

    // Ensures a nurse isn't double-booked for multiple 8h shifts on the same day
    public void ensureNurseDailyHoursOk(Staff staff, LocalDate day) {
        if (!(staff instanceof Nurse)) return;
        long count = shifts.stream()
                .filter(s -> s.getDay().equals(day) && s.getStaff().equals(staff))
                .count();
        if (count > 1)
            throw new ComplianceException("Nurse over 8 hours on " + day + " (already has a shift).");
    }
}
