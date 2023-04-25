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

/**
 * Utility class for parsing a string value to an integer.
 */
public final class NumberParser {

    /**
     * Returns the corresponding int value of the given string, or -1, if the string is not a number.
     *
     * @param value The number in its string representation.
     * @return The corresponding int value, or -1.
     */
    public static int parseNumber(final String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Returns the corresponding int value of the given string, or -1, if the string is not a number or null.
     *
     * @param id The number in its string representation.
     * @return The corresponding int value, or -1.
     */
    public static int parseId(final String id) {
        if (id == null) {
            return -1;
        }

        return NumberParser.parseNumber(id);
    }

}
