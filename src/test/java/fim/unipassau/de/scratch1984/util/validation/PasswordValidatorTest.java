package fim.unipassau.de.scratch1984.util.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PasswordValidatorTest {

    private static final String EMPTY = "empty_string";
    private static final String SHORT = "short_string";
    private static final String LONG = "long_string";
    private static final String REGEX = "password_validator_regex";
    private static final String MATCH = "passwords_not_matching";
    private static final String BLANK = "   ";
    private static final String PASSWORD = "!V4l1d_P4ssw0rd!";
    private static final String INVALID1 = "1nv4l1dP4ssw0rd";
    private static final String INVALID2 = "!Invalid_Password?";
    private static final String INVALID3 = "!1nv4l1d_p4ssw0rd!";
    private static final String INVALID4 = "!1NV4L1D_P4SSW0RD!";
    private static final String SHORT_PASSWORD = "!B4d_PW";
    private static final String LONG_PASSWORD = "!L000000000000000000000000000000000000000000000000000000000000ng_PW!";

    @Test
    public void testValidateNull() {
        assertAll(
                () -> assertEquals(EMPTY, PasswordValidator.validate(null, PASSWORD)),
                () -> assertEquals(EMPTY, PasswordValidator.validate(PASSWORD, null))
        );
    }

    @Test
    public void testValidateBlank() {
        assertAll(
                () -> assertEquals(EMPTY, PasswordValidator.validate(BLANK, PASSWORD)),
                () -> assertEquals(EMPTY, PasswordValidator.validate(PASSWORD, BLANK))
        );
    }

    @Test
    public void testValidateShort() {
        assertEquals(SHORT, PasswordValidator.validate(SHORT_PASSWORD, PASSWORD));
    }

    @Test
    public void testValidateLong() {
        assertEquals(LONG, PasswordValidator.validate(LONG_PASSWORD, PASSWORD));
    }

    @Test
    public void testValidateInvalidPasswords() {
        assertAll(
                () -> assertEquals(REGEX, PasswordValidator.validate(INVALID1, PASSWORD)),
                () -> assertEquals(REGEX, PasswordValidator.validate(INVALID2, PASSWORD)),
                () -> assertEquals(REGEX, PasswordValidator.validate(INVALID3, PASSWORD)),
                () -> assertEquals(REGEX, PasswordValidator.validate(INVALID4, PASSWORD))
        );
    }

    @Test
    public void testValidateNotMatching() {
        assertEquals(MATCH, PasswordValidator.validate(PASSWORD, INVALID1));
    }

    @Test
    public void testValidateValid() {
        assertNull(PasswordValidator.validate(PASSWORD, PASSWORD));
    }
}
