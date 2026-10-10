import java.math.BigDecimal;

public final class Motorcycle extends Vehicle {
    private BigDecimal engineDisplacement;

    public Motorcycle(String plateNumber, String maker, String model, BigDecimal engineDisplacement) {
        super(plateNumber, maker, model);
        updateDetails(maker, model, engineDisplacement);
    }

    void updateDetails(String maker, String model, BigDecimal engineDisplacement) {
        BigDecimal validDisplacement = Validation.measurement(
                engineDisplacement, "Engine displacement", false);
        updateIdentityDetails(maker, model);
        this.engineDisplacement = validDisplacement;
    }

    public BigDecimal getEngineDisplacement() { return engineDisplacement; }
    @Override public String getType() { return "Motorcycle"; }
    @Override protected BigDecimal calculateUnroundedTax() {
        return engineDisplacement.multiply(new BigDecimal("0.10"));
    }
    @Override public String toString() {
        return super.toString() + " | displacement: " + engineDisplacement + " cc";
    }
}
