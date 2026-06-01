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

package de.uni_passau.fim.se2.scratchlog.spring.configuration;

import de.uni_passau.fim.se2.scratchlog.spring.authentication.CustomAuthenticationProvider;
import de.uni_passau.fim.se2.scratchlog.util.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Custom security configuration for accessing application content.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("!test")
public class SecurityConfig {

    /**
     * The custom authentication provider to user for user authentication.
     */
    private final CustomAuthenticationProvider authenticationProvider;

    /**
     * The global application config.
     */
    private final ApplicationProperties applicationProperties;

    /**
     * Registers the custom authentication provider with spring security.
     *
     * @param authenticationProvider The custom authentication provider.
     * @param applicationProperties  The application properties.
     */
    @Autowired
    public SecurityConfig(
        final CustomAuthenticationProvider authenticationProvider, final ApplicationProperties applicationProperties) {
        this.authenticationProvider = authenticationProvider;
        this.applicationProperties = applicationProperties;
    }

    /**
     * Configures the URL patterns that are restricted and only accessible for users with certain privileges.
     *
     * @param http The http security.
     * @return The security filter chain.
     * @throws Exception Throws an exception if a user with insufficient privileges tries to access a restricted page.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        http
            .cors(withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(this::authorizeHttpRequestsConfig)
            .formLogin(config -> config.loginPage("/login"))
            .headers(config -> config.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));

        return http.build();
    }

    private void authorizeHttpRequestsConfig(
        final AuthorizeHttpRequestsConfigurer<?>.AuthorizationManagerRequestMatcherRegistry authorize) {

        authorize

            .requestMatchers(
                "/login",
                "/finish",
                "/token/password",
                "/reset",
                "/users/reset",
                "/users/login"
            ).anonymous()

            .requestMatchers(
                "/users/authenticate"
            ).hasAnyRole("PARTICIPANT", "ANONYMOUS")

            .requestMatchers(
                "/experiment/*",
                "/users/add",
                "/users/delete",
                "/users/forgot",
                "/users/add",
                "/users/bulk",
                "/result",
                "/search",
                "/secret",
                "/search/*"
            ).hasRole("ADMIN")

            .requestMatchers(
                "/experiment",
                "/users/profile",
                "/users/logout",
                "/users/edit",
                "/users/update",
                "/course"
            ).hasRole("PARTICIPANT")

            .requestMatchers(
                "/css/*",
                "/js/*",
                "/lib/**",
                "/",
                "/finish",
                "/participant/restart",
                "/participant/stop",
                "/store/*",
                "/token",
                "/error",
                "/login/saml2",
                "/saml2/**",
                "/webjars/**"
            ).permitAll()

            .anyRequest().authenticated();
    }

    /**
     * Configures the allowed sources of cross-origin requests, namely the instrumented Scratch GUI and the SSO
     * authentication provider.
     *
     * @return The CORS configuration.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(applicationProperties.getScratchGuiBaseUrls()));
        configuration.setAllowedHeaders(List.of("content-type"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/store/*", configuration);
        CorsConfiguration saml2Config = new CorsConfiguration();
        saml2Config.setAllowedOrigins(List.of(applicationProperties.getSamlUrl()));
        saml2Config.setAllowedMethods(Arrays.asList("GET", "POST"));
        source.registerCorsConfiguration("/login/saml2", saml2Config);
        source.registerCorsConfiguration("/saml2/**", saml2Config);
        return source;
    }

}
