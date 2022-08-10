package fim.unipassau.de.scratch1984.util.validation;

import fim.unipassau.de.scratch1984.util.Constants;

import java.util.regex.Pattern;

/**
 * Validator for password inputs.
 */
public final class PasswordValidator {

    /**
     * The RegEx to use when validating a password's strength.
     */
    private static final Pattern REGEX = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])"
            + "([!?+\\-*.:,;@#$%_äöüÄÖÜßẞ]*).{" + Constants.PASSWORD_MIN + "," + Constants.SMALL_FIELD + "}$");

    /**
     * Validates the given {@code password} and returns the appropriate error message string if the input does not meet
     * the requirements, or {@code null} otherwise.
     *
     * @param password The password to validate.
     * @param confirmPassword The repeated password input.
     * @return An error message string, or null.
     */
    public static String validate(final String password, final String confirmPassword) {
        if (password == null || password.trim().isBlank() || confirmPassword == null
                || confirmPassword.trim().isBlank()) {
            return "empty_string";
        } else if (password.length() < Constants.PASSWORD_MIN) {
            return "short_string";
        } else if (password.length() > Constants.SMALL_FIELD) {
            return "long_string";
        } else if (!REGEX.matcher(password).matches()) {
            return "password_validator_regex";
        } else if (!password.equals(confirmPassword)) {
            return "passwords_not_matching";
        }

        return null;
    }

}
