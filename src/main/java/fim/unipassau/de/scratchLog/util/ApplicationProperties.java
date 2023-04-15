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
