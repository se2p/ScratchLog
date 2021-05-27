package fim.unipassau.de.scratch1984.util.validation;

import fim.unipassau.de.scratch1984.util.Constants;

import java.util.regex.Pattern;

/**
 * Validator for email inputs.
 */
public final class EmailValidator {

    /**
     * The RegEx to use when validating the email.
     */
    private static final Pattern REGEX = Pattern.compile("^(.+)@(.+)$");

    /**
     * Validates the given {@code email} and returns the appropriate error message string if the input does not meet the
     * requirements, or {@code null} otherwise.
     *
     * @param email The email to validate.
     * @return An error message string, or null.
     */
    public static String validate(final String email) {
        if (email == null || email.trim().isBlank()) {
            return "empty_string";
        } else if (email.length() > Constants.LARGE_FIELD) {
            return "long_string";
        } else if (!REGEX.matcher(email).matches()) {
            return "email_validator_regex";
        }

        return null;
    }

}
