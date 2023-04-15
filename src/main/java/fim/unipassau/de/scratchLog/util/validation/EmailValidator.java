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
 * Validator for email inputs.
 */
public final class EmailValidator {

    /**
     * The RegEx to use when validating the email.
     */
    private static final Pattern REGEX = Pattern.compile("^(.+)@(.+)$");

    /**
     * Validates the given {@code email} and returns the appropriate error message string if the input does not meet the
     * requirements, or {@code null} otherwise.
     *
     * @param email The email to validate.
     * @return An error message string, or null.
     */
    public static String validate(final String email) {
        if (email == null || email.trim().isBlank()) {
            return "empty_string";
        } else if (email.length() > Constants.LARGE_FIELD) {
            return "long_string";
        } else if (!REGEX.matcher(email).matches()) {
            return "email_validator_regex";
        }

        return null;
    }

}
