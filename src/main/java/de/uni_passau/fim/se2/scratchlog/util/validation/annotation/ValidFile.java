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

import de.uni_passau.fim.se2.scratchlog.util.validation.FileValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A Jakarta Validation annotation for files.
 *
 * @see FileValidator
 */
@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileValidator.class)
@Documented
public @interface ValidFile {

    /**
     * The error message in case validation fails and the validator specifies no other error message.
     * Defaults to the `file_empty` resource bundle message.
     *
     * @return The error message in case validation fails.
     */
    String message() default "{file_empty}";

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

    /**
     * The content types that should be allowed for this file.
     *
     * @return The allowed content types.
     */
    String[] contentTypes() default {};

    /**
     * The file endings that should be allowed for this file.
     *
     * @return The allowed file endings.
     */
    String[] fileEndings() default {};

}
