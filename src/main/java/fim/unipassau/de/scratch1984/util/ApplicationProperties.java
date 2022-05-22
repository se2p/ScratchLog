package fim.unipassau.de.scratch1984.util;

import java.util.ResourceBundle;

/**
 * Utility class providing access to the values specified in the application.properties file needed by the application.
 */
public final class ApplicationProperties {

    /**
     * The name of the application to be displayed.
     */
    public static final String APPLICATION_NAME;

    /**
     * The base URL under which the application is deployed.
     */
    public static final String BASE_URL;

    /**
     * The context path of the application, if applicable.
     */
    public static final String CONTEXT_PATH;

    /**
     * The base URL of the Scratch GUI instance.
     */
    public static final String GUI_BASE_URL;

    /**
     * The full path to the Scratch GUI instance.
     */
    public static final String GUI_URL;

    /**
     * The boolean indicating whether the project uses a mail server or not.
     */
    public static final Boolean MAIL_SERVER;

    static {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("application-local");
        APPLICATION_NAME = resourceBundle.getString("app.name");
        BASE_URL = resourceBundle.getString("app.url");
        CONTEXT_PATH = resourceBundle.getString("server.servlet.context-path");
        GUI_BASE_URL = resourceBundle.getString("app.gui.base");
        GUI_URL = resourceBundle.getString("app.gui");
        MAIL_SERVER = resourceBundle.getString("app.mail").equals("true");
    }

}
