package fim.unipassau.de.scratch1984.util.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class EmailValidatorTest {

    private static final String EMPTY = "empty_string";
    private static final String LONG = "long_string";
    private static final String REGEX = "email_validator_regex";
    private static final String EMAIL = "admin@admin.com";
    private static final String INVALID1 = "@admin.com";
    private static final String INVALID2 = "admin@";
    private static final String INVALID3 = "@";
    private static final String BLANK = "   ";
    private static final String LONG_EMAIL = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaadmin@"
            + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaadmin.com";

    @Test
    public void testValidateNull() {
        assertEquals(EMPTY, EmailValidator.validate(null));
    }

    @Test
    public void testValidateBlank() {
        assertEquals(EMPTY, EmailValidator.validate(BLANK));
    }

    @Test
    public void testValidateLongEmail() {
        assertEquals(LONG, EmailValidator.validate(LONG_EMAIL));
    }

    @Test
    public void testValidateInvalidFormats() {
        assertAll(
                () -> assertEquals(REGEX, EmailValidator.validate(INVALID1)),
                () -> assertEquals(REGEX, EmailValidator.validate(INVALID2)),
                () -> assertEquals(REGEX, EmailValidator.validate(INVALID3))
        );
    }

    @Test
    public void testValidateValid() {
        assertNull(EmailValidator.validate(EMAIL));
    }
}
