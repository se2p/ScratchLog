package fim.unipassau.de.scratch1984.util.validation;

/**
 * Validator for string inputs.
 */
public final class StringValidator {

    /**
     * Checks, whether the given input string matches the general requirements and returns a custom error message
     * string if it does not, or {@code null} if everything is fine.
     *
     * @param input The input string to check.
     * @param maxLength The maximum string length allowed for the field.
     * @return The custom error message string or {@code null}.
     */
    public static String validate(final String input, final int maxLength) {
        if (input == null || input.trim().isBlank()) {
            return "empty_string";
        }

        if (input.length() > maxLength) {
            return "long_string";
        }

        return null;
    }

}
