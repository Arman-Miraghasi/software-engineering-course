import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class Person {
    private final String licenseNumber;
    private String name;
    private String surname;
    private String address;
    private final Set<Vehicle> vehicles = new LinkedHashSet<>();

    public Person(String licenseNumber, String name, String surname, String address) {
        this.licenseNumber = Validation.identifier(licenseNumber, "License number");
        updateDetails(name, surname, address);
    }

    void updateDetails(String name, String surname, String address) {
        String validName = Validation.text(name, "Name");
        String validSurname = Validation.text(surname, "Surname");
        String validAddress = Validation.text(address, "Address");
        this.name = validName;
        this.surname = validSurname;
        this.address = validAddress;
    }

    void addVehicle(Vehicle vehicle) { vehicles.add(vehicle); }
    void removeVehicle(Vehicle vehicle) { vehicles.remove(vehicle); }

    public String getLicenseNumber() { return licenseNumber; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getAddress() { return address; }
    public Set<Vehicle> getVehicles() { return Collections.unmodifiableSet(vehicles); }

    @Override
    public String toString() {
        return licenseNumber + " | " + name + " " + surname + " | " + address;
    }
}
