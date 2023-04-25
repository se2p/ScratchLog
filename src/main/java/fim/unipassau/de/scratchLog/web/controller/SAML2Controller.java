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

package fim.unipassau.de.scratchLog.web.controller;

import fim.unipassau.de.scratchLog.application.service.SAML2Service;
import fim.unipassau.de.scratchLog.spring.authentication.CustomAuthenticationProvider;
import fim.unipassau.de.scratchLog.util.ApplicationProperties;
import fim.unipassau.de.scratchLog.util.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

/**
 * The controller for handling SAML2 authenticated users. This class has been adapted from the Artemis project.
 */
@Controller
@RequestMapping("/saml2")
public class SAML2Controller {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SAML2Controller.class);

    /**
     * The SAML2 service, if SAML2 authentication has been configured, or empty.
     */
    private final Optional<SAML2Service> saml2Service;

    /**
     * The authentication provider for user authentication.
     */
    private final CustomAuthenticationProvider authenticationProvider;

    /**
     * Constructs a new SAML2 controller with the given dependencies.
     *
     * @param saml2Service The {@link SAML2Service}, if the saml2 profile is being used, or an empty {@link Optional}.
     * @param authenticationProvider The {@link CustomAuthenticationProvider} to use.
     */
    @Autowired
    public SAML2Controller(final Optional<SAML2Service> saml2Service,
                           final CustomAuthenticationProvider authenticationProvider) {
        this.saml2Service = saml2Service;
        this.authenticationProvider = authenticationProvider;
    }

    /**
     * Properly authenticates the SAML2 authenticated user with the service by retrieving information about an existing
     * user from the database or creating a new user. The user is then authenticated by the service's
     * {@link CustomAuthenticationProvider} and the security context is updated accordingly before the user is
     * redirected to the index page.
     *
     * @param httpServletRequest The {@link HttpServletRequest} request containing the current user session.
     * @return The index page on success or the error page otherwise.
     */
    @GetMapping("/login")
    public String authorizeSAML2(final HttpServletRequest httpServletRequest) {
        if (saml2Service.isEmpty() || !ApplicationProperties.SAML_AUTHENTICATION) {
            LOGGER.error("Cannot authenticate SAML2 users when SAML2 is disabled!");
            clearSecurityContext(httpServletRequest);
            return Constants.ERROR;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Saml2AuthenticatedPrincipal)) {
            LOGGER.error("Cannot authenticate SAML2 users with illegal authentication!");
            clearSecurityContext(httpServletRequest);
            return Constants.ERROR;
        }

        try {
            Authentication authReq = saml2Service.get().handleAuthentication(
                    (Saml2AuthenticatedPrincipal) authentication.getPrincipal());
            clearSecurityContext(httpServletRequest);
            authReq = authenticationProvider.authenticate(authReq);
            updateSecurityContext(httpServletRequest, authReq);
            return "redirect:/";
        } catch (Exception e) {
            clearSecurityContext(httpServletRequest);
            return Constants.ERROR;
        }
    }

    /**
     * Clears the current security context and invalidates the {@link HttpSession} after the user has been authenticated
     * by the {@link SAML2Service}.
     *
     * @param httpServletRequest The {@link HttpServletRequest} request containing the current user session.
     */
    private void clearSecurityContext(final HttpServletRequest httpServletRequest) {
        SecurityContextHolder.clearContext();
        HttpSession session = httpServletRequest.getSession(false);

        if (session != null) {
            session.invalidate();
        }
    }

    /**
     * Updates the spring security context with the given authentication.
     *
     * @param auth The {@link Authentication} containing the user information.
     * @param httpServletRequest The {@link HttpServletRequest} containing the current session.
     */
    private void updateSecurityContext(final HttpServletRequest httpServletRequest, final Authentication auth) {
        SecurityContextHolder.getContext().setAuthentication(auth);
        HttpSession session = httpServletRequest.getSession(true);
        session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
    }

}
