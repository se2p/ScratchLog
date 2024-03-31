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

package fim.unipassau.de.scratchLog.util;

import fim.unipassau.de.scratchLog.web.error_handling.InvalidIdException;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class CommonAssertions {

    public static void assertInvalidIdException(final Executable executable) {
        assertThrows(InvalidIdException.class, executable);
    }

}
