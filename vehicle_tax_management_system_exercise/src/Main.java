import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

/** Console boundary for the City Administrator actor. */
public final class Main {
    private final VehicleTaxSystem system;
    private final Scanner input;

    private Main(VehicleTaxSystem system, Scanner input) {
        this.system = system;
        this.input = input;
    }

    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("--demo")) {
            runDemo();
            return;
        }
        if (args.length > 0) {
            System.out.println("Usage: java -cp out Main [--demo]");
            return;
        }
        new Main(new VehicleTaxSystem(), new Scanner(System.in)).run();
    }

    private void run() {
        System.out.println("Vehicle Tax Management System");
        System.out.println("Data is kept in memory and lasts until you exit.");
        try {
            while (true) {
                System.out.println("""

                        1. Add person          2. Edit person
                        3. Delete person       4. Search people
                        5. Add vehicle         6. Edit vehicle
                        7. Delete vehicle      8. Search vehicles
                        9. Assign vehicle     10. Transfer vehicle
                       11. Calculate tax      12. Annual tax report
                        0. Exit
                       """);
                try {
                    int choice = Integer.parseInt(read("Choice: "));
                    switch (choice) {
                        case 0 -> { System.out.println("Goodbye."); return; }
                        case 1 -> addPerson();
                        case 2 -> editPerson();
                        case 3 -> deletePerson();
                        case 4 -> showMatches(system.searchPersons(read("License/name/surname/address (blank = all): ")));
                        case 5 -> addVehicle();
                        case 6 -> editVehicle();
                        case 7 -> {
                            system.deleteVehicle(read("Plate number: "));
                            System.out.println("Vehicle deleted and ownership removed.");
                        }
                        case 8 -> showMatches(system.searchVehicles(read("Plate/maker/model/type (blank = all): ")));
                        case 9 -> System.out.println(system.assignVehicle(read("Plate number: "), read("Owner license: ")));
                        case 10 -> System.out.println(system.transferVehicle(read("Plate number: "), read("New owner license: ")));
                        case 11 -> System.out.println("Annual tax: EUR " + system.calculateVehicleTax(read("Plate number: ")));
                        case 12 -> System.out.print(system.generateAnnualTaxReport());
                        default -> System.out.println("Choose a number from 0 to 12.");
                    }
                } catch (NumberFormatException exception) {
                    System.out.println("Invalid number. Use a decimal point for measurements (for example, 120.5).");
                } catch (IllegalArgumentException exception) {
                    System.out.println(exception.getMessage());
                }
            }
        } catch (NoSuchElementException exception) {
            System.out.println("\nInput ended. Goodbye.");
        }
    }

    private void addPerson() {
        Person person = system.addPerson(read("License number: "), read("Name: "),
                read("Surname: "), read("Address: "));
        System.out.println("Person added: " + person);
    }

    private void editPerson() {
        String license = read("License number: ");
        Person person = system.findPerson(license);
        if (person == null) throw new IllegalArgumentException("Person not found.");
        System.out.println("Current details: " + person);
        system.editPerson(license, read("New name: "), read("New surname: "), read("New address: "));
        System.out.println("Person updated.");
    }

    private void deletePerson() {
        String license = read("License number: ");
        Person person = system.findPerson(license);
        if (person == null) throw new IllegalArgumentException("Person not found.");
        int count = person.getVehicles().size();
        system.deletePerson(license);
        System.out.println("Person deleted. " + count + " vehicle(s) are now unowned.");
    }

    private void addVehicle() {
        String type = read("Vehicle type (CAR/MOTORCYCLE): ").toUpperCase(Locale.ROOT);
        if (!type.equals("CAR") && !type.equals("MOTORCYCLE")) {
            throw new IllegalArgumentException("Choose CAR or MOTORCYCLE.");
        }
        String plate = read("Plate number: ");
        String maker = read("Maker: ");
        String model = read("Model: ");
        Vehicle vehicle;
        if (type.equals("CAR")) {
            vehicle = new Car(plate, maker, model, decimal("CO2 emissions (grams): "), readFuelType());
        } else {
            vehicle = new Motorcycle(plate, maker, model, decimal("Engine displacement (cc): "));
        }
        system.addVehicle(vehicle);
        System.out.println("Vehicle added: " + vehicle);
    }

    private void editVehicle() {
        String plate = read("Plate number: ");
        Vehicle vehicle = system.findVehicle(plate);
        if (vehicle == null) throw new IllegalArgumentException("Vehicle not found.");
        System.out.println("Current details: " + vehicle);
        String maker = read("New maker: ");
        String model = read("New model: ");
        if (vehicle instanceof Car) {
            system.editCar(plate, maker, model, decimal("New CO2 emissions (grams): "), readFuelType());
        } else {
            system.editMotorcycle(plate, maker, model, decimal("New engine displacement (cc): "));
        }
        System.out.println("Vehicle updated: " + vehicle);
    }

    private FuelType readFuelType() {
        try {
            return FuelType.valueOf(read("Fuel type (PETROL/DIESEL/HYBRID): ").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Choose PETROL, DIESEL, or HYBRID.");
        }
    }

    private String read(String prompt) {
        System.out.print(prompt);
        return input.nextLine().trim();
    }

    private BigDecimal decimal(String prompt) { return new BigDecimal(read(prompt)); }

    private static void showMatches(List<?> matches) {
        if (matches.isEmpty()) System.out.println("No matching records.");
        else matches.forEach(System.out::println);
    }

    private static void runDemo() {
        VehicleTaxSystem system = new VehicleTaxSystem();
        system.addPerson("L001", "Ana", "Garcia", "1 Main Street");
        system.addPerson("L002", "Luis", "Martin", "2 Main Street");
        system.addPerson("L003", "Sara", "Lopez", "3 Main Street");
        system.addVehicle(new Car("P001", "Toyota", "Corolla", new BigDecimal("120"), FuelType.PETROL));
        system.addVehicle(new Car("D001", "Volkswagen", "Golf", new BigDecimal("140"), FuelType.DIESEL));
        system.addVehicle(new Car("H001", "Toyota", "Prius", new BigDecimal("90"), FuelType.HYBRID));
        system.addVehicle(new Motorcycle("M001", "Honda", "CB500", new BigDecimal("500")));
        system.addVehicle(new Car("UNOWNED", "Ford", "Focus", new BigDecimal("110"), FuelType.PETROL));
        System.out.println("Assigning vehicles:");
        System.out.println(system.assignVehicle("P001", "L001"));
        System.out.println(system.assignVehicle("D001", "L001"));
        System.out.println(system.assignVehicle("H001", "L002"));
        System.out.println(system.assignVehicle("M001", "L002"));
        System.out.println("\nTransferring P001 from L001 to L002:");
        System.out.println(system.transferVehicle("P001", "L002"));
        System.out.println("\nRepeating the transfer:");
        System.out.println(system.transferVehicle("P001", "L002"));
        System.out.println();
        System.out.print(system.generateAnnualTaxReport());
    }
}
