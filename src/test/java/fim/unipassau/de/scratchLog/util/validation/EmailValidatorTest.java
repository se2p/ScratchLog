/*
 * Copyright (C) 2023 ScratchLog contributors
 *
 * This file is part of ScratchLog.
 *
 * ScratchLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * ScratchLog is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ScratchLog. If not, see <http://www.gnu.org/licenses/>.
 */
package fim.unipassau.de.scratchLog.util.validation;

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
