package healthcaresystem.view.cli;

import java.util.Scanner;
import healthcaresystem.service.CareHomeService;

public class Menu {
    private final CareHomeService service;
    private final Scanner in = new Scanner(System.in);

    public Menu(CareHomeService service) {
        this.service = service;
    }

    public void start() {
        while (true) {
            System.out.println("\n--- Resident HealthCare System ---");
            System.out.println("1) Add resident");
            System.out.println("2) Add staff");
            System.out.println("3) Assign shift");
            System.out.println("4) Admit to bed");
            System.out.println("0) Exit");
            System.out.print("Choice: ");
            String c = in.nextLine().trim();

            switch (c) {
                case "1": break;
                case "2": break;
                case "3": break;
                case "4": break;
                case "0": return;
                default: System.out.println("Invalid choice.");
            }
        }
    }
}
