import java.math.BigDecimal;
import java.util.Locale;

final class Validation {
    private Validation() { }

    static String text(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank.");
        }
        return value.trim();
    }

    static String identifier(String value, String field) {
        return text(value, field).toUpperCase(Locale.ROOT);
    }

    static BigDecimal measurement(BigDecimal value, String field, boolean allowZero) {
        if (value == null || value.signum() < 0 || (!allowZero && value.signum() == 0)) {
            throw new IllegalArgumentException(field + (allowZero
                    ? " must be zero or greater." : " must be greater than zero."));
        }
        return value;
    }
}
