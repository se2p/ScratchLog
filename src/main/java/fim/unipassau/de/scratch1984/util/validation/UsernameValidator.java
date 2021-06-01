package fim.unipassau.de.scratch1984.util.validation;

import fim.unipassau.de.scratch1984.util.Constants;

import java.util.regex.Pattern;

/**
 * Validator for username inputs.
 */
public final class UsernameValidator {

    /**
     * The RegEx to use when validating a username.
     */
    private static final Pattern REGEX = Pattern.compile("^(.+[a-zA-Z0-9_])$");

    /**
     * Validates the given {@code username} and returns the appropriate error message string if the input does not meet
     * the requirements, or {@code null} otherwise.
     *
     * @param username The username to validate.
     * @return An error message string, or null.
     */
    public static String validate(final String username) {
        if (username == null || username.trim().isBlank()) {
            return "empty_string";
        } else if (username.length() < Constants.USERNAME_MIN) {
            return "short_string";
        } else if (username.length() > Constants.SMALL_FIELD) {
            return "long_string";
        } else if (!REGEX.matcher(username).matches()) {
            return "username_validator_regex";
        }

        return null;
    }

}
