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

/**
 * Validator for string inputs.
 */
public final class StringValidator {

    /**
     * Checks, whether the given input string matches the general requirements and returns a custom error message
     * string if it does not, or {@code null} if everything is fine.
     *
     * @param input The input string to check.
     * @param maxLength The maximum string length allowed for the field.
     * @return The custom error message string or {@code null}.
     */
    public static String validate(final String input, final int maxLength) {
        if (input == null || input.trim().isBlank()) {
            return "empty_string";
        }

        if (input.length() > maxLength) {
            return "long_string";
        }

        return null;
    }

}
