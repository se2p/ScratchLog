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

package fim.unipassau.de.scratchLog.spring.configuration;

import fim.unipassau.de.scratchLog.spring.authentication.CustomAuthenticationProvider;
import fim.unipassau.de.scratchLog.util.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
     * Registers the custom authentication provider with spring security.
     *
     * @param authenticationProvider The custom authentication provider.
     */
    @Autowired
    public SecurityConfig(final CustomAuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
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
        http.cors(withDefaults())
                .csrf().disable()
                .authorizeHttpRequests()
                .requestMatchers("/login", "/finish", "/token/password", "/reset",
                        "/users/reset", "/users/login", "/users/authenticate").anonymous()
                .requestMatchers("/experiment/*", "/users/add", "/users/delete", "/users/forgot", "/users/add",
                        "/users/bulk", "/result", "/search", "/secret", "/search/*").hasRole("ADMIN")
                .requestMatchers("/experiment", "/users/profile", "/users/logout", "/users/edit",
                        "/users/update", "/course").hasRole("PARTICIPANT")
                .requestMatchers("/design/*", "/js/*", "/webfonts/*", "/", "/finish",
                        "/participant/restart", "/participant/stop", "/store/*", "/token", "/error", "/login/saml2",
                        "/saml2/**").permitAll()
                .anyRequest().authenticated()
                .and().formLogin().loginPage("/login")
                .defaultSuccessUrl("/", true)
                .and().headers().frameOptions().sameOrigin();

        return http.build();
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
        configuration.setAllowedOrigins(List.of(ApplicationProperties.GUI_BASE_URL));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/store/*", configuration);
        CorsConfiguration saml2Config = new CorsConfiguration();
        saml2Config.setAllowedOrigins(List.of(ApplicationProperties.SAML2_BASE_URL));
        saml2Config.setAllowedMethods(Arrays.asList("GET", "POST"));
        source.registerCorsConfiguration("/login/saml2", saml2Config);
        source.registerCorsConfiguration("/saml2/**", saml2Config);
        return source;
    }

}
