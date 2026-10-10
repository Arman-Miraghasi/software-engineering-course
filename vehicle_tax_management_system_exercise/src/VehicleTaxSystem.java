import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class VehicleTaxSystem {
    private final Map<String, Person> persons = new LinkedHashMap<>();
    private final Map<String, Vehicle> vehicles = new LinkedHashMap<>();

    public Person addPerson(String licenseNumber, String name, String surname, String address) {
        Person person = new Person(licenseNumber, name, surname, address);
        if (persons.containsKey(person.getLicenseNumber())) {
            throw new IllegalArgumentException("License number is already registered.");
        }
        persons.put(person.getLicenseNumber(), person);
        return person;
    }

    public void editPerson(String licenseNumber, String name, String surname, String address) {
        requirePerson(licenseNumber).updateDetails(name, surname, address);
    }

    public void deletePerson(String licenseNumber) {
        Person person = requirePerson(licenseNumber);
        for (Vehicle vehicle : List.copyOf(person.getVehicles())) {
            person.removeVehicle(vehicle);
            vehicle.setOwner(null);
        }
        persons.remove(person.getLicenseNumber());
    }

    public Vehicle addVehicle(Vehicle vehicle) {
        if (vehicle == null) throw new IllegalArgumentException("Vehicle is required.");
        if (vehicle.getOwner() != null) {
            throw new IllegalArgumentException("Register an unowned vehicle, then assign it.");
        }
        if (vehicles.containsKey(vehicle.getPlateNumber())) {
            throw new IllegalArgumentException("Plate number is already registered.");
        }
        vehicles.put(vehicle.getPlateNumber(), vehicle);
        return vehicle;
    }

    public void editCar(String plateNumber, String maker, String model,
                        BigDecimal co2Emissions, FuelType fuelType) {
        Vehicle vehicle = requireVehicle(plateNumber);
        if (!(vehicle instanceof Car car)) throw new IllegalArgumentException("Vehicle is not a car.");
        car.updateDetails(maker, model, co2Emissions, fuelType);
    }

    public void editMotorcycle(String plateNumber, String maker, String model, BigDecimal displacement) {
        Vehicle vehicle = requireVehicle(plateNumber);
        if (!(vehicle instanceof Motorcycle motorcycle)) {
            throw new IllegalArgumentException("Vehicle is not a motorcycle.");
        }
        motorcycle.updateDetails(maker, model, displacement);
    }

    public void deleteVehicle(String plateNumber) {
        Vehicle vehicle = requireVehicle(plateNumber);
        if (vehicle.getOwner() != null) {
            vehicle.getOwner().removeVehicle(vehicle);
            vehicle.setOwner(null);
        }
        vehicles.remove(vehicle.getPlateNumber());
    }

    public Person findPerson(String licenseNumber) {
        return persons.get(Validation.identifier(licenseNumber, "License number"));
    }

    public Vehicle findVehicle(String plateNumber) {
        return vehicles.get(Validation.identifier(plateNumber, "Plate number"));
    }

    public List<Person> getAllPersons() { return List.copyOf(persons.values()); }
    public List<Vehicle> getAllVehicles() { return List.copyOf(vehicles.values()); }

    public List<Person> searchPersons(String query) {
        String term = searchTerm(query);
        return persons.values().stream().filter(person -> contains(term, person.getLicenseNumber(),
                person.getName(), person.getSurname(), person.getAddress())).toList();
    }

    public List<Vehicle> searchVehicles(String query) {
        String term = searchTerm(query);
        return vehicles.values().stream().filter(vehicle -> contains(term, vehicle.getPlateNumber(),
                vehicle.getMaker(), vehicle.getModel(), vehicle.getType())).toList();
    }

    public OwnershipResult assignVehicle(String plateNumber, String licenseNumber) {
        Vehicle vehicle = findVehicle(plateNumber);
        Person person = findPerson(licenseNumber);
        if (vehicle == null) return failure(OwnershipResult.Status.VEHICLE_NOT_FOUND);
        if (person == null) return failure(OwnershipResult.Status.PERSON_NOT_FOUND);
        if (vehicle.getOwner() != null) return failure(OwnershipResult.Status.ALREADY_OWNED);
        return changeOwner(vehicle, person);
    }

    public OwnershipResult transferVehicle(String plateNumber, String newLicenseNumber) {
        Vehicle vehicle = findVehicle(plateNumber);
        Person newOwner = findPerson(newLicenseNumber);
        if (vehicle == null) return failure(OwnershipResult.Status.VEHICLE_NOT_FOUND);
        if (newOwner == null) return failure(OwnershipResult.Status.PERSON_NOT_FOUND);
        Person oldOwner = vehicle.getOwner();
        if (oldOwner == newOwner) return failure(OwnershipResult.Status.ALREADY_OWNER);
        return changeOwner(vehicle, newOwner);
    }

    private OwnershipResult changeOwner(Vehicle vehicle, Person newOwner) {
        Person oldOwner = vehicle.getOwner();
        if (oldOwner != null) oldOwner.removeVehicle(vehicle);
        vehicle.setOwner(newOwner);
        newOwner.addVehicle(vehicle);
        return new OwnershipResult(OwnershipResult.Status.SUCCESS, vehicle.calculateTax());
    }

    public BigDecimal calculateVehicleTax(String plateNumber) {
        return requireVehicle(plateNumber).calculateTax();
    }

    public AnnualTaxReport generateAnnualTaxReport() {
        List<AnnualTaxReport.OwnerTax> report = new ArrayList<>();
        for (Person person : getAllPersons()) {
            List<AnnualTaxReport.VehicleTax> vehicleTaxes = new ArrayList<>();
            BigDecimal totalTax = new BigDecimal("0.00");
            for (Vehicle vehicle : person.getVehicles()) {
                BigDecimal vehicleTax = vehicle.calculateTax();
                vehicleTaxes.add(new AnnualTaxReport.VehicleTax(vehicle.getPlateNumber(),
                        vehicle.getMaker(), vehicle.getModel(), vehicle.getType(), vehicleTax));
                totalTax = totalTax.add(vehicleTax);
            }
            report.add(new AnnualTaxReport.OwnerTax(person.getLicenseNumber(), person.getName(),
                    person.getSurname(), person.getAddress(), vehicleTaxes, totalTax));
        }
        return new AnnualTaxReport(LocalDate.now(), report);
    }

    private Person requirePerson(String licenseNumber) {
        Person person = findPerson(licenseNumber);
        if (person == null) throw new IllegalArgumentException("Person not found.");
        return person;
    }

    private Vehicle requireVehicle(String plateNumber) {
        Vehicle vehicle = findVehicle(plateNumber);
        if (vehicle == null) throw new IllegalArgumentException("Vehicle not found.");
        return vehicle;
    }

    private static OwnershipResult failure(OwnershipResult.Status status) {
        return new OwnershipResult(status, null);
    }

    private static String searchTerm(String query) {
        if (query == null) throw new IllegalArgumentException("Search query is required.");
        return query.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean contains(String term, String... fields) {
        for (String field : fields) {
            if (field.toLowerCase(Locale.ROOT).contains(term)) return true;
        }
        return false;
    }
}
