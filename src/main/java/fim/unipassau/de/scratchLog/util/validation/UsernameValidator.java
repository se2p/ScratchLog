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

import fim.unipassau.de.scratchLog.util.Constants;

import java.util.regex.Pattern;

/**
 * Validator for username inputs.
 */
public final class UsernameValidator {

    /**
     * The RegEx to use when validating a username.
     */
    private static final Pattern REGEX = Pattern.compile("^([a-zA-Z0-9_]+)[a-zA-Z]([a-zA-Z0-9_]+)$");

    /**
     * Validates the given {@code username} and returns the appropriate error message string if the input does not meet
     * the requirements, or {@code null} otherwise.
     *
     * @param username The username to validate.
     * @return An error message string, or null.
     */
    public static String validate(final String username) {
        if (username == null || username.trim().isBlank()) {
            return "empty_string";
        } else if (username.length() < Constants.USERNAME_MIN) {
            return "short_string";
        } else if (username.length() > Constants.SMALL_FIELD) {
            return "long_string";
        } else if (!REGEX.matcher(username).matches()) {
            return "username_validator_regex";
        }

        return null;
    }

}
