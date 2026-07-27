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

package de.uni_passau.fim.se2.scratchlog.util;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class providing access to the values specified in the application.properties file needed by the application.
 */
@Component
public class ApplicationProperties {

    /**
     * The user-facing name of the application.
     */
    @Getter
    private final String applicationName;

    /**
     * The context path under which the application is deployed.
     */
    @Getter
    private final String contextPath;

    /**
     * The base URL where the application is deployed.
     */
    private final String baseUrl;

    /**
     * The URLs for Scratch UIs that can be configured by the user.
     */
    @Getter
    private final String[] scratchGuiUrls;

    /**
     * The corresponding base URLs for {@link #scratchGuiUrls}.
     */
    @Getter
    private final String[] scratchGuiBaseUrls;

    /**
     * The URL of the SAML authentication provider.
     */
    @Getter
    private final String samlUrl;

    /**
     * The Spring profiles configured for the application.
     */
    private final Set<String> springProfiles;

    /**
     * Autowiring constructor.
     *
     * @param applicationName The application name.
     * @param contextPath The context path.
     * @param baseUrl The base URL for the application.
     * @param scratchGuiUrls The Scratch UI URLs.
     * @param scratchGuiBaseUrls The corresponding Scratch UI base URLs.
     * @param samlBaseUrl The base URL of the SAML authentication provider.
     * @param springProfiles The active Spring profiles.
     */
    @Autowired
    public ApplicationProperties(
            @Value("${spring.application.name}") final String applicationName,
            @Value("${server.servlet.context-path}") final String contextPath,
            @Value("${server.url}") final String baseUrl,
            @Value("${app.gui}") final String[] scratchGuiUrls,
            @Value("${app.gui.base}") final String[] scratchGuiBaseUrls,
            @Value("${app.saml.base:null}") final String samlBaseUrl,
            @Value("${spring.profiles.active}") final String[] springProfiles
    ) {
        this.applicationName = applicationName;
        this.contextPath = contextPath;
        this.baseUrl = baseUrl;
        this.scratchGuiUrls = scratchGuiUrls;
        this.scratchGuiBaseUrls = scratchGuiBaseUrls;
        this.samlUrl = samlBaseUrl;
        this.springProfiles = Arrays.stream(springProfiles).collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Returns the full application URL.
     *
     * @return The URL including the context path.
     */
    public String getApplicationUrl() {
        return baseUrl + contextPath;
    }

    /**
     * Returns if mails should be sent.
     *
     * @return True, if mails should be sent.
     */
    public boolean useMail() {
        return springProfiles.contains("mail");
    }

    /**
     * Checks if SAML authentication is enabled.
     *
     * @return True, if SAML authentication is enabled.
     */
    public boolean useSamlAuthentication() {
        return springProfiles.contains("saml2");
    }

    /**
     * Checks if the code embeddings feature is enabled.
     *
     * @return True, if enabled.
     */
    public boolean codeEmbeddingsActive() {
        return springProfiles.contains(Constants.PROFILE_CODE_EMBEDDINGS);
    }

    /**
     * Checks if the Whisker feature is enabled.
     *
     * @return True, if enabled.
     */
    public boolean whiskerActive() {
        return springProfiles.contains(Constants.PROFILE_WHISKER);
    }

    @Override
    public final String toString() {
        return "ApplicationProperties{"
                + "applicationName='" + applicationName + '\''
                + ", contextPath='" + contextPath + '\''
                + ", baseUrl='" + baseUrl + '\''
                + ", scratchGuiUrls=" + Arrays.toString(scratchGuiUrls)
                + ", scratchGuiBaseUrls=" + Arrays.toString(scratchGuiBaseUrls)
                + ", samlUrl='" + samlUrl + '\''
                + ", springProfiles=" + springProfiles
                + '}';
    }

}
