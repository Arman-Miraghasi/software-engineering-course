import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract sealed class Vehicle permits Car, Motorcycle {
    private final String plateNumber;
    private String maker;
    private String model;
    private Person owner;

    protected Vehicle(String plateNumber, String maker, String model) {
        this.plateNumber = Validation.identifier(plateNumber, "Plate number");
        updateIdentityDetails(maker, model);
    }

    final void updateIdentityDetails(String maker, String model) {
        String validMaker = Validation.text(maker, "Maker");
        String validModel = Validation.text(model, "Model");
        this.maker = validMaker;
        this.model = validModel;
    }

    final void setOwner(Person owner) { this.owner = owner; }
    public final String getPlateNumber() { return plateNumber; }
    public final String getMaker() { return maker; }
    public final String getModel() { return model; }
    public final Person getOwner() { return owner; }
    public abstract String getType();
    protected abstract BigDecimal calculateUnroundedTax();

    public final BigDecimal calculateTax() {
        return calculateUnroundedTax().setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return plateNumber + " | " + maker + " " + model + " | " + getType()
                + " | owner: " + (owner == null ? "Unowned" : owner.getLicenseNumber())
                + " | annual tax: EUR " + calculateTax();
    }
}
