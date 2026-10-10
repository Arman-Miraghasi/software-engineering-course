import java.math.BigDecimal;

public final class Car extends Vehicle {
    private BigDecimal co2Emissions;
    private FuelType fuelType;

    public Car(String plateNumber, String maker, String model,
               BigDecimal co2Emissions, FuelType fuelType) {
        super(plateNumber, maker, model);
        updateDetails(maker, model, co2Emissions, fuelType);
    }

    void updateDetails(String maker, String model, BigDecimal co2Emissions, FuelType fuelType) {
        BigDecimal validEmissions = Validation.measurement(co2Emissions, "CO2 emissions", true);
        if (fuelType == null) {
            throw new IllegalArgumentException("Fuel type is required.");
        }
        updateIdentityDetails(maker, model);
        this.co2Emissions = validEmissions;
        this.fuelType = fuelType;
    }

    public BigDecimal getCo2Emissions() { return co2Emissions; }
    public FuelType getFuelType() { return fuelType; }
    @Override public String getType() { return fuelType + " car"; }
    @Override protected BigDecimal calculateUnroundedTax() {
        return co2Emissions.multiply(fuelType.getRatePerGram());
    }
    @Override public String toString() {
        return super.toString() + " | CO2: " + co2Emissions + " g";
    }
}
