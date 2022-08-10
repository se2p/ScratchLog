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
    public static final String[] GUI_BASE_URL;

    /**
     * The full path to the Scratch GUI instance.
     */
    public static final String[] GUI_URL;

    /**
     * The base URL of the SAML2 identity provider.
     */
    public static final String SAML2_BASE_URL;

    /**
     * The boolean indicating whether the project uses a mail server or not.
     */
    public static final boolean MAIL_SERVER;

    /**
     * The boolean indicating whether the project supports authentication using SAML2 or not.
     */
    public static final boolean SAML_AUTHENTICATION;

    static {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("application");
        APPLICATION_NAME = resourceBundle.getString("app.name");
        BASE_URL = resourceBundle.getString("app.url");
        CONTEXT_PATH = resourceBundle.getString("server.servlet.context-path");
        GUI_BASE_URL = resourceBundle.getString("app.gui.base").split(",");
        GUI_URL = resourceBundle.getString("app.gui").split(",");
        SAML2_BASE_URL = resourceBundle.getString("app.saml.base");
        MAIL_SERVER = resourceBundle.getString("app.mail").equals("true");
        SAML_AUTHENTICATION = resourceBundle.getString("spring.profiles.active").contains("saml2");
    }

}
