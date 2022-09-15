package fim.unipassau.de.scratch1984.util;

/**
 * Utility class for parsing a string value to an integer.
 */
public final class NumberParser {

    /**
     * Returns the corresponding int value of the given string, or -1, if the string is not a number.
     *
     * @param value The number in its string representation.
     * @return The corresponding int value, or -1.
     */
    public static int parseNumber(final String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Returns the corresponding int value of the given string, or -1, if the string is not a number or null.
     *
     * @param id The number in its string representation.
     * @return The corresponding int value, or -1.
     */
    public static int parseId(final String id) {
        if (id == null) {
            return -1;
        }

        return NumberParser.parseNumber(id);
    }

}
