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
