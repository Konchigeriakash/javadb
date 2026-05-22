import javax.swing.JTextField;
import java.math.BigDecimal;

public class InputValidator {
    private InputValidator() {
    }

    public static String requiredText(JTextField field, String label) {
        String value = field.getText().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        return value;
    }

    public static BigDecimal nonNegativeDecimal(JTextField field, String label) {
        String value = requiredText(field, label);
        try {
            BigDecimal number = new BigDecimal(value);
            if (number.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(label + " cannot be negative.");
            }
            return number;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " must be a valid number, for example 899.00.");
        }
    }

    public static int nonNegativeInt(JTextField field, String label) {
        int value = intValue(field, label);
        if (value < 0) {
            throw new IllegalArgumentException(label + " cannot be negative.");
        }
        return value;
    }

    public static int positiveInt(JTextField field, String label) {
        int value = intValue(field, label);
        if (value <= 0) {
            throw new IllegalArgumentException(label + " must be greater than zero.");
        }
        return value;
    }

    public static int nonZeroInt(JTextField field, String label) {
        int value = intValue(field, label);
        if (value == 0) {
            throw new IllegalArgumentException(label + " cannot be zero.");
        }
        return value;
    }

    private static int intValue(JTextField field, String label) {
        String value = requiredText(field, label);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " must be a whole number.");
        }
    }
}
