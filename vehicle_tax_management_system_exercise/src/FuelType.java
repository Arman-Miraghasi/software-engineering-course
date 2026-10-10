import java.math.BigDecimal;

public enum FuelType {
    PETROL("1.40"), DIESEL("1.80"), HYBRID("1.20");

    private final BigDecimal ratePerGram;

    FuelType(String rate) { ratePerGram = new BigDecimal(rate); }
    public BigDecimal getRatePerGram() { return ratePerGram; }
}
