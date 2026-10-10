import java.math.BigDecimal;

public record OwnershipResult(Status status, BigDecimal annualTax) {
    public enum Status {
        SUCCESS, VEHICLE_NOT_FOUND, PERSON_NOT_FOUND, ALREADY_OWNER, ALREADY_OWNED
    }

    public boolean isSuccessful() { return status == Status.SUCCESS; }

    @Override
    public String toString() {
        return switch (status) {
            case SUCCESS -> "Ownership updated. Annual tax: EUR " + annualTax;
            case VEHICLE_NOT_FOUND -> "Operation failed: vehicle not found.";
            case PERSON_NOT_FOUND -> "Operation failed: person not found.";
            case ALREADY_OWNER -> "Transfer not required: this person is already the owner.";
            case ALREADY_OWNED -> "Assignment failed: vehicle already has an owner. Use transfer.";
        };
    }
}
