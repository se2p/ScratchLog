/*
 * This file is part of ScratchLog.
 * Licenced under the GPL v3.0 or later.
 *
 * SPDX-FileCopyrightText: 2021-2026 Scratchlog contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * ScratchLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ScratchLog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.uni_passau.fim.se2.scratchlog.util.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import java.util.Map;

/**
 * A collection of general utility methods for validation purposes.
 */
public final class ValidationUtils {

    private ValidationUtils() {
        // intentionally empty, utility class
    }

    /**
     * Rejects a value in validation by building a constraint validation for the given {@code context}, as well as
     * attaching an error message.
     *
     * @param context The {@link ConstraintValidatorContext} of the current validation.
     * @param messageKey The resource bundle key that contains the error message.
     * @return {@code false}, always.
     */
    public static boolean reject(final ConstraintValidatorContext context, final String messageKey) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("{" + messageKey + "}").addConstraintViolation();
        return false;
    }

    /**
     * Same as {@link #reject(ConstraintValidatorContext, String)}, but allows replacing message template parameters
     * via the given map.
     *
     * @param context The {@link ConstraintValidatorContext} of the current validation.
     * @param messageKey The resource bundle key that contains the error message.
     * @param messageParameters A string map in which the keys represent the resource bundle message placeholders,
     *                          and values represent the corresponding text to insert.
     * @return {@code false}, always.
     */
    public static boolean reject(final ConstraintValidatorContext context, final String messageKey,
                           final Map<String, String> messageParameters) {
        HibernateConstraintValidatorContext hibernateContext
            = context.unwrap(HibernateConstraintValidatorContext.class);
        for (Map.Entry<String, String> entry : messageParameters.entrySet()) {
            hibernateContext.addMessageParameter(entry.getKey(), entry.getValue());
        }
        return reject(hibernateContext, messageKey);
    }
}
