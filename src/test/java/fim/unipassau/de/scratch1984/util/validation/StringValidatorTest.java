package fim.unipassau.de.scratch1984.util.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class StringValidatorTest {

    private static final String EMPTY = "empty_string";
    private static final String LONG = "long_string";
    private static final String BLANK = "   ";
    private static final String LONG_INPUT = "too long";
    private static final String INPUT = "hi";

    private static final int maxLength = 5;

    @Test
    public void testValidateNull() {
        assertEquals(EMPTY, StringValidator.validate(null, maxLength));
    }

    @Test
    public void testValidateBlank() {
        assertEquals(EMPTY, StringValidator.validate(BLANK, maxLength));
    }

    @Test
    public void testValidateMaxLength() {
        assertEquals(LONG, StringValidator.validate(LONG_INPUT, maxLength));
    }

    @Test
    public void testValidateValid() {
        assertNull(StringValidator.validate(INPUT, maxLength));
    }

}
