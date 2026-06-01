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

package de.uni_passau.fim.se2.scratchlog.util.validation.annotation;

import de.uni_passau.fim.se2.scratchlog.util.Constants;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A Jakarta Validation constraint annotation for passwords. Requirements are given by the other validation annotations.
 */
@Constraint(validatedBy = {})
@Size(min = Constants.PASSWORD_MIN, max = Constants.SMALL_FIELD)
@Pattern(
    regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])([!?+\\-*.:,;@#$%_äöüÄÖÜßẞ]*).*",
    message = "{error_password_pattern}"
)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Password {

    /**
     * The error message in case validation fails and the validator specifies no other error message.
     * Defaults to the `error_password` resource bundle message.
     *
     * @return The error message in case validation fails.
     */
    String message() default "{error_password}";

    /**
     * The validation groups parameter required by Jakarta Validation.
     *
     * @return The validation groups.
     */
    Class<?>[] groups() default {};

    /**
     * The payload parameter required by Jakarta Validation.
     *
     * @return The payload metadata.
     */
    Class<? extends Payload>[] payload() default {};

}
