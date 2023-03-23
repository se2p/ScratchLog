package fim.unipassau.de.scratch1984.spring.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Custom security configuration for integration testing.
 */
@EnableWebSecurity
@Configuration
@Profile("test")
public class SecurityTestConfig {

    /**
     * Authorizes any requests for all mappings for the test profile.
     *
     * @param http The http security.
     * @return The security filter chain.
     * @throws Exception Throws an exception if a user with insufficient privileges tries to access a restricted page.
     */
    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        http.cors()
                .and()
                .csrf().disable()
                .authorizeHttpRequests()
                .anyRequest().permitAll();

        return http.build();
    }

}
