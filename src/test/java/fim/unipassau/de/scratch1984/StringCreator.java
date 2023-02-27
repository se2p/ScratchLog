package fim.unipassau.de.scratch1984;

/**
 * Utility class for creating long strings.
 */
public final class StringCreator {

    public static String createLongString(int length) {
        StringBuilder longString = new StringBuilder();
        longString.append("a".repeat(Math.max(0, length)));
        return longString.toString();
    }

}
