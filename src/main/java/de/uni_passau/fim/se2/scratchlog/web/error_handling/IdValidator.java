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

/**
 * Utility class for validating ID parameters.
 */
public final class IdValidator {

    private IdValidator() {
        // intentionally empty, utility class
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
