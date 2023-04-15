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
 * Utility class for validating parameters passed for retrieving pages.
 */
public final class PageUtils {

    /**
     * Checks, whether the passed id and page string are valid numbers.
     *
     * @param id The id to check.
     * @param page The page number to check.
     * @return {@code true} if any string is an invalid number, or {@code false} otherwise.
     */
    public static boolean isInvalidParams(final String id, final String page) {
        if (page == null) {
            return true;
        }

        int current = NumberParser.parseNumber(page);
        int parsedId = NumberParser.parseId(id);

        return parsedId < Constants.MIN_ID || current <= -1;
    }

    /**
     * Checks, whether the given page is within the tolerated boundaries. The lower boundary for any page number is
     * zero, while the upper boundary depends on the given last page number.
     *
     * @param page The page number to check.
     * @param lastPage The number of the last page.
     * @return {@code true} if the page number is invalid, or {@code false} otherwise.
     */
    public static boolean isInvalidPageNumber(final int page, final int lastPage) {
        if (page < 0) {
            return true;
        } else {
            return page >= lastPage;
        }
    }

}
