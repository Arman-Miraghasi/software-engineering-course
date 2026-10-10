import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AnnualTaxReport(LocalDate generatedOn, List<OwnerTax> owners) {
    public AnnualTaxReport {
        owners = List.copyOf(owners);
    }

    public record VehicleTax(String plateNumber, String maker, String model,
                             String type, BigDecimal annualTax) { }

    public record OwnerTax(String licenseNumber, String name, String surname, String address,
                           List<VehicleTax> vehicles, BigDecimal totalTax) {
        public OwnerTax { vehicles = List.copyOf(vehicles); }
    }

    public BigDecimal getTotalTax() {
        return owners.stream().map(OwnerTax::totalTax).reduce(new BigDecimal("0.00"), BigDecimal::add);
    }

    @Override
    public String toString() {
        StringBuilder report = new StringBuilder("Annual Vehicle Tax Report - ")
                .append(generatedOn).append('\n');
        if (owners.isEmpty()) report.append("No registered people.\n");
        for (OwnerTax owner : owners) {
            report.append('\n').append(owner.licenseNumber()).append(" | ")
                    .append(owner.name()).append(' ').append(owner.surname())
                    .append(" | ").append(owner.address()).append('\n');
            if (owner.vehicles().isEmpty()) report.append("  No vehicles.\n");
            for (VehicleTax vehicle : owner.vehicles()) {
                report.append("  ").append(vehicle.plateNumber()).append(" | ")
                        .append(vehicle.maker()).append(' ').append(vehicle.model())
                        .append(" | ").append(vehicle.type()).append(" | EUR ")
                        .append(vehicle.annualTax()).append('\n');
            }
            report.append("  Owner total: EUR ").append(owner.totalTax()).append('\n');
        }
        return report.append("\nOverall total: EUR ").append(getTotalTax()).append('\n').toString();
    }
}
