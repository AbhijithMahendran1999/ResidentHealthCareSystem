Resident HealthCare System – CLI and GUI Implementation

1. Overview

This project implements a Resident HealthCare Management System that runs in two modes:

A Command-Line Interface (CLI) version.

A Graphical User Interface (GUI) version built with JavaFX.

Both interfaces share the same backend logic and data model, following a clean MVC (Model–View–Controller) design. The system manages staff, residents, wards, rooms, and beds, supporting authentication, authorization, scheduling, prescriptions, and audit logging.

2. Architecture and Design
   
2.1 MVC Architecture

Model (healthcaresystem.model.*): Domain entities like Resident, abstract Staff (with Doctor, Nurse, Manager), Ward, Room, Bed, Prescription, Shift, etc. Staff is abstract to capture shared identity/credentials and specialize behavior by role.

View (CLI) (healthcaresystem.view.cli): Text-based UI via Menu, orchestrated by MenuController.

View (GUI) (healthcaresystem.view.fx): JavaFX UI with LoginView, DashboardView, and focused dialogs.

Controller (healthcaresystem.controller): Connects views to services (e.g., MenuController).

Service (healthcaresystem.service): Core business logic and rule enforcement (CareHomeService, AuthService, AuditService, AllocationService).

3. Authentication and Authorization
   
3.1 Authentication

AuthService validates credentials against stored Staff records.

3.2 Authorization

CareHomeService enforces role and roster checks:

Manager: add staff, assign shifts, modify passwords, admit residents, check compliance, view logs.

Doctor: attach/update prescriptions.

Nurse: administer medication and move residents.

Rules are enforced in the backend (service checks) and in the GUI (role-aware buttons/menus).

4. Data Initialization and Persistence
   
4.1 Seeding and Initialization

On first run the system seeds:

Default Manager admin (admin / admin123).

Facility with two wards, six rooms each, and multiple beds per room.

Called via:

service.ensureDefaultAdmin();
service.ensureDefaultFacility();

4.2 Data Persistence

State is serialized to carehome.dat via SerializationRepository. It is loaded on startup and saved on exit.

5. Audit Logging

AuditService records an AuditEntry for each action (timestamp, actor, action, details, success). The log is viewable in CLI and GUI.

6. Exception Handling

Custom exceptions:

UnauthorizedActionException – insufficient permission

NotRosteredException – acting outside rostered time

PersistenceException – IO/serialization issues

ComplianceException – roster compliance failures

All surfaced with clear, user-facing messages.

7. Testing

Extensive JUnit tests cover:

Login/auth

Shift assignment & compliance

Bed allocation & movement

Prescriptions & administering doses

Persistence integrity

Authorization failures (negative tests)

8. Running the Application

8.1 CLI Entry Point

Main class: healthcaresystem.app.Main

How: Run Main to start the text-based menu.

8.2 GUI Entry Point (JavaFX)

Main class: healthcaresystem.app.FxMain

How: Run FxMain to start the JavaFX GUI (this is the GUI entry main file).

9. Shared Business Logic

Both CLI and GUI call the same CareHomeService methods, ensuring consistent behavior and rules across interfaces.

10. GUI Design and Features
    
10.1 Dashboard

Centered title, user info + logout on the right.

Manager sidebar (staff/roster/compliance/logs).

Bed Board shows wards/rooms/beds; bed color indicates occupancy & gender.

10.2 Dialogs

AddResidentDialog: manager adds resident to bed.

AddStaffDialog: manager creates doctor/nurse.

ModifyStaffPasswordDialog: manager updates staff passwords.

AssignShiftDialog: manager assigns/replaces shifts.

AttachPrescriptionDialog: doctor adds prescription for bed’s resident.

UpdatePrescriptionDialog: doctor add/edit/remove prescription items.

AdministerMedicationDialog: nurse administers dose (logs record).

MoveResidentDialog: nurse moves resident to another bed.

ViewPrescriptionsDialog: view prescriptions for a bed’s resident.

ViewAdministrationLogDialog: see med administration history by bed.

CheckComplianceDialog: 7-day roster validation.

ShowAuditLogDialog: scrollable, most-recent-first audit table.

ViewRosterDialog: grouped graphical roster (nurses vs doctors).

ViewStaffDialog: grouped staff directory.

Frontend only shows actions allowed for the user’s role; backend still validates on every request.

11. Data Flow Summary

Load carehome.dat (seed data if missing).

Login as Manager/Doctor/Nurse.

Perform actions allowed by role.

Every action is audited.

On exit, state is saved for the next run.

12. How to Test the GUI

Run the GUI: execute healthcaresystem.app.FxMain.

Login as Manager: admin / admin123.

Add staff: create at least one nurse and one doctor.

Assign shifts: allocate DAY/EVE shifts across a week.

Check compliance: should pass if coverage rules are met.

Attach prescriptions (Doctor): log in as doctor, add/edit items.

Administer (Nurse): log in as nurse, administer doses, move residents.

Review audit (Manager): log back in as manager and open audit logs.

13. Repository

GitHub:
https://github.com/COSC1295-advanced-programming-2025-s2/cosc1295-assignment-2-semester-2-2025-AbhijithMahendran1999.git

Note: If you see a carehome.dat binary file in the repo, please delete it before running. That file contains local test data accidentally pushed; removing it lets the app seed a clean state on first launch.

14. References

[1] GeeksforGeeks, “JavaFX Tutorial.” Accessed: Oct. 17, 2025. [Online]. Available: https://www.geeksforgeeks.org/java/javafx-tutorial/

[2] TutorialsPoint, “JavaFX Tutorial.” Accessed: Oct. 17, 2025. [Online]. Available: https://www.tutorialspoint.com/javafx/index.htm

[3] Java Code Geeks, “JavaFX Tutorials.” Accessed: Oct. 17, 2025. [Online]. Available: https://www.javacodegeeks.com/javafx-tutorials

[4] GeeksforGeeks, “JavaFX Button with Examples.” Accessed: Oct. 17, 2025. [Online]. Available: https://www.geeksforgeeks.org/java/javafx-button-with-examples/

[5] GeeksforGeeks, “JavaFX FlowPane Class.” Accessed: Oct. 17, 2025. [Online]. Available: https://www.geeksforgeeks.org/java/javafx-flowpane-class/

[6] GeeksforGeeks, “JavaFX VBox Class.” Accessed: Oct. 17, 2025. [Online]. Available: https://www.geeksforgeeks.org/java/javafx-vbox-class/

[7] Baeldung, “Authentication vs Authorization.” Accessed: Oct. 17, 2025. [Online]. Available: https://www.baeldung.com/cs/authentication-vs-authorization

[8] Wikipedia, “JavaFX.” Accessed: Oct. 17, 2025. [Online]. Available: https://en.wikipedia.org/wiki/JavaFX
