package fim.unipassau.de.scratch1984.util.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UsernameValidatorTest {

    private static final String EMPTY = "empty_string";
    private static final String SHORT = "short_string";
    private static final String LONG = "long_string";
    private static final String REGEX = "username_validator_regex";
    private static final String BLANK = "   ";
    private static final String USERNAME = "admin";
    private static final String MIXED = "user_123";
    private static final String INVALID1 = "    user   ";
    private static final String INVALID2 = "!?user?!";
    private static final String SHORT_USERNAME = "min";
    private static final String LONG_USERNAME = "uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuser";

    @Test
    public void testValidateNull() {
        assertEquals(EMPTY, UsernameValidator.validate(null));
    }

    @Test
    public void testValidateBlank() {
        assertEquals(EMPTY, UsernameValidator.validate(BLANK));
    }

    @Test
    public void testValidateShort() {
        assertEquals(SHORT, UsernameValidator.validate(SHORT_USERNAME));
    }

    @Test
    public void testValidateLong() {
        assertEquals(LONG, UsernameValidator.validate(LONG_USERNAME));
    }

    @Test
    public void testValidateInvalidUsernames() {
        assertAll(
                () -> assertEquals(REGEX, UsernameValidator.validate(INVALID1)),
                () -> assertEquals(REGEX, UsernameValidator.validate(INVALID2))
        );
    }

    @Test
    public void testValidateValid() {
        assertAll(
                () -> assertNull(UsernameValidator.validate(USERNAME)),
                () -> assertNull(UsernameValidator.validate(MIXED))
        );
    }
}
