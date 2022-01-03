package fim.unipassau.de.scratch1984.util;

/**
 * Collection of application-wide constants.
 */
public final class Constants {

    /**
     * Prevents instantiation of this utility class.
     */
    private Constants() {
        throw new UnsupportedOperationException();
    }

    /**
     * The project name to be displayed in the header and the index page.
     */
    public static final String PROJECT_NAME = "Scratch1984";

    /**
     * The base URL under which the application is deployed.
     */
    public static final String BASE_URL = "http://localhost:8090";

    /**
     * The base URL of the Scratch GUI instance.
     */
    public static final String GUI_URL = "http://localhost:8601";

    /**
     * The context path of the application, if applicable.
     */
    public static final String CONTEXT_PATH = "";

    /**
     * String corresponding to the administrator role used by Spring Security for granting role-based access.
     */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    /**
     * String corresponding to the participant role used by Spring Security for granting role-based access.
     */
    public static final String ROLE_PARTICIPANT = "ROLE_PARTICIPANT";

    /**
     * String corresponding to redirecting to the error page.
     */
    public static final String ERROR = "redirect:/error";

    /**
     * The filetype of a sb3 zip.
     */
    public static final String SB3 = "sb3";

    /**
     * The minimum length of usernames.
     */
    public static final int USERNAME_MIN = 4;

    /**
     * The minimum length of passwords.
     */
    public static final int PASSWORD_MIN = 8;

    /**
     * The maximum length of inputs in small or normal text boxes.
     */
    public static final int SMALL_FIELD = 50;

    /**
     * The maximum length of inputs in large text boxes.
     */
    public static final int LARGE_FIELD = 100;

    /**
     * The maximum length of inputs in small or normal text areas.
     */
    public static final int SMALL_AREA = 1000;

    /**
     * The maximum length of inputs in large text areas.
     */
    public static final int LARGE_AREA = 50_000;

    /**
     * The page size for a pageable.
     */
    public static final int PAGE_SIZE = 10;

    /**
     * The maximum number of attempts to send an email.
     */
    public static final int MAX_EMAIL_TRIES = 3;

    /**
     * The maximum number of login attempts before the user account is temporarily deactivated.
     */
    public static final int MAX_LOGIN_ATTEMPTS = 3;

    /**
     * The minimal id for a user or experiment entity.
     */
    public static final int MIN_ID = 1;

    /**
     * The number of bytes that are generated for a user secret.
     */
    public static final int SECRET_LENGTH = 32;

    /**
     * The number a minute value needs to be multiplied with to convert it to milliseconds.
     */
    public static final int MINUTES_TO_MILLIS = 60 * 1000;

}
