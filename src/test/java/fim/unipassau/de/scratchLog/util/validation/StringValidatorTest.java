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
