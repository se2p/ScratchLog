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
