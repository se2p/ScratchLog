package fim.unipassau.de.scratch1984.util;

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
     * Checks, whether the passed id and page strings are valid numbers.
     *
     * @param id The id to check.
     * @param currentPage The number of the current page to check.
     * @param lastPage The number of the last page to check.
     * @return {@code true} if any of the strings is an invalid number, or {@code false} otherwise.
     */
    public static boolean isInvalidParams(final String id, final String currentPage, final String lastPage) {
        if (!isInvalidParams(id, currentPage)) {
            if (lastPage == null) {
                return true;
            } else {
                return NumberParser.parseNumber(lastPage) <= -1;
            }
        } else {
            return true;
        }
    }

    /**
     * Checks, whether the given current page is within the tolerated upper and lower boundaries.
     *
     * @param current The current page number to be checked.
     * @param currentBoundary The allowed lower boundary.
     * @param lastPage The allowed upper boundary.
     * @param isNext Whether the operation is to retrieve the next or previous page.
     * @return {@code true} if the current page is invalid, or {@code false} otherwise.
     */
    public static boolean isInvalidCurrentPage(final int current, final int currentBoundary, final int lastPage,
                                               final boolean isNext) {
        if (current <= currentBoundary) {
            return true;
        } else {
            return isNext ? current >= lastPage : current > lastPage;
        }
    }

}
