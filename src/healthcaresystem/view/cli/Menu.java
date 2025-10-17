package healthcaresystem.view.cli;

import java.util.Scanner;
import healthcaresystem.controller.MenuController;
import healthcaresystem.service.CareHomeService;

public class Menu {
    private final MenuController controller;
    private final Scanner in = new Scanner(System.in);

    // attach controller to the service and scanner
    public Menu(CareHomeService service) {
        this.controller = new MenuController(service, in);
    }

    // main loop for the simple text menu
    public void start() {

        controller.ensureDefaultAdmin();     // seed a default manager if none exists
        controller.ensureDefaultFacility();  // seed two wards/rooms/beds if empty

        while (true) {
            System.out.println("\n--- Resident HealthCare System ---");
            System.out.println("L) Login");
            System.out.println("O) Logout");
            System.out.println("U) Who am I?");
            System.out.println("1) Add new resident to a vacant bed");
            System.out.println("2) Add staff (doctor/nurse)");
            System.out.println("3) Modify staff password (MANAGER only)");
            System.out.println("4) Allocate/Modify shift of a staff member");
            System.out.println("5) Check resident details in a bed");
            System.out.println("6) Attach prescription to resident in bed (Doctor only)");
            System.out.println("7) Move resident to a different bed (Nurse only)");
            System.out.println("8) Update administered prescription details");
            System.out.println("9) Administer prescription");
            System.out.println("10) View Medicine Administration Log by BedID");
            System.out.println("C) Check compliance");
            System.out.println("A) Show logs");
            System.out.println("0) Exit");
            System.out.print("Choice: ");

            String c = in.nextLine().trim();

            switch (c) {
                case "L": case "l": controller.login(); break;                    // login
                case "O": case "o": controller.logout(); break;                   // logout
                case "U": case "u": controller.printCurrentUser(); break;         // show session user

                case "1": controller.addNewResidentToVacantBedFlow(); break;      // manager: admit new resident
                case "2": controller.addStaffFlow(); break;                       // manager: add staff
                case "3": controller.modifyStaffPasswordFlow(); break;            // manager: change password
                case "4": controller.allocateOrModifyShiftFlow(); break;          // manager: assign/replace shift
                case "5": controller.viewResidentInBedFlow(); break;              // nurse/doctor: view bed details
                case "6": controller.attachPrescriptionFlow(); break;             // doctor: add prescription
                case "7": controller.moveResidentToDifferentBedFlow(); break;     // nurse: move resident
                case "8": controller.updatePrescriptionDetailsFlow(); break;      // doctor: update prescription
                case "9": controller.administerPrescriptionFlow(); break;         // nurse: administer dose
                case "10": controller.viewAdministrationLogForBedFlow(); break;   // nurse/doctor: view medicine admininstrated log

                case "C": case "c": controller.checkCompliance(); break;          // run weekly compliance check
                case "A": case "a": controller.showAuditLogFlow(); break;         // print audit log

                case "0": return;                                                 // exit
                default: System.out.println("Invalid choice.");
            }
        }
    }
}
