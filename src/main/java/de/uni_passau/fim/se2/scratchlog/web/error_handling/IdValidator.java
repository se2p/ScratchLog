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

package de.uni_passau.fim.se2.scratchlog.web.error_handling;

import de.uni_passau.fim.se2.scratchlog.util.Constants;

/**
 * Utility class for validating ID parameters.
 */
public final class IdValidator {

    /**
     * Checks if the given id is within the valid range.
     *
     * @param id An id.
     * @throws InvalidIdException Thrown in case the id is invalid.
     */
    public static void validateUserIdElseThrow(final int id) throws InvalidIdException {
        validateIdElseThrow("user", id);
    }

    /**
     * Checks if the given id is within the valid range.
     *
     * @param id An id.
     * @throws InvalidIdException Thrown in case the id is invalid.
     */
    public static void validateExperimentIdElseThrow(final int id) throws InvalidIdException {
        validateIdElseThrow("experiment", id);
    }

    /**
     * Checks if the given id is within the valid range.
     *
     * @param id An id.
     * @throws InvalidIdException Thrown in case the id is invalid.
     */
    public static void validateCourseIdElseThrow(final int id) throws InvalidIdException {
        validateIdElseThrow("course", id);
    }

    /**
     * Checks if the given id is within the valid range.
     *
     * @param id An id.
     * @throws InvalidIdException Thrown in case the id is invalid.
     */
    public static void validateFileIdElseThrow(final int id) throws InvalidIdException {
        validateIdElseThrow("file", id);
    }

    /**
     * Checks if the given id is within the valid range.
     *
     * <p>Prefer the more specific methods if possible (e.g. {@link #validateCourseIdElseThrow(int)}).
     *
     * @param entity The name of the entity for which an ID should be checked. E.g. "user", "course".
     * @param id An id.
     * @throws InvalidIdException Thrown in case the id is invalid.
     */
    public static void validateIdElseThrow(final String entity, final int id) {
        if (id < Constants.MIN_ID) {
            throw new InvalidIdException(entity, id);
        }
    }

    /**
     * Checks whether the given page is within the tolerated boundaries. The lower boundary for any page number is
     * zero.
     *
     * @param page The page number to check.
     * @throws InvalidIdException Thrown in case the ID is outside the valid range.
     */
    public static void validatePageNumberElseThrow(final int page) throws InvalidIdException {
        if (page < 0) {
            throw new InvalidIdException("page", page);
        }
    }

    /**
     * Checks whether the given page is within the tolerated boundaries. The lower boundary for any page number is
     * zero, while the upper boundary depends on the given last page number.
     *
     * @param page The page number to check.
     * @param lastPage The number of the last page.
     * @throws InvalidIdException Thrown in case the ID is outside the valid range.
     */
    public static void validatePageNumberElseThrow(final int page, final int lastPage) throws InvalidIdException {
        if (page < 0 || page >= lastPage) {
            throw new InvalidIdException("page", page);
        }
    }

}
