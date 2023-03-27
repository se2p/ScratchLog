package fim.unipassau.de.scratch1984.spring.configuration;

import fim.unipassau.de.scratch1984.spring.authentication.CustomAuthenticationProvider;
import fim.unipassau.de.scratch1984.util.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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
                        "/participant/restart", "/participant/stop", "/store/*", "/token", "/error").permitAll()
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
        return source;
    }

}
