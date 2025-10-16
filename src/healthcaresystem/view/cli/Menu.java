package healthcaresystem.view.cli;

import java.util.Scanner;
import healthcaresystem.controller.MenuController;
import healthcaresystem.service.CareHomeService;

public class Menu {
    private final MenuController controller;
    private final Scanner in = new Scanner(System.in);

    public Menu(CareHomeService service) {
        this.controller = new MenuController(service, in);
    }

    public void start() {
    	
        controller.ensureDefaultAdmin();     // creates admin login if no login exists
        controller.ensureDefaultFacility();  // creates two wards if none exist
       
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
            System.out.println("C) Check compliance ");
            System.out.println("A) Show logs ");
            System.out.println("0) Exit");
            System.out.print("Choice: ");

            String c = in.nextLine().trim();

            switch (c) {
                case "L": case "l": controller.login(); break;
                case "O": case "o": controller.logout(); break;
                case "U": case "u": controller.printCurrentUser(); break;

                case "1": controller.addNewResidentToVacantBedFlow(); break;
                case "2": controller.addStaffFlow(); break;
                case "3": controller.modifyStaffPasswordFlow(); break;
                case "4": controller.allocateOrModifyShiftFlow(); break;
                case "5": controller.viewResidentInBedFlow(); break;
                case "6": controller.attachPrescriptionFlow(); break;
                case "7": controller.moveResidentToDifferentBedFlow(); break;
                case "8": controller.updatePrescriptionDetailsFlow(); break;
                case "9": controller.administerPrescriptionFlow(); break;
                case "10": controller.viewAdministrationLogForBedFlow(); break;

                case "C": case "c": controller.checkCompliance(); break;
                case "A": case "a": controller.showAuditLogFlow(); break;


                case "0": return;
                default: System.out.println("Invalid choice.");
            }
        }
    }
}
