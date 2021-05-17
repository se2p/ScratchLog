package fim.unipassau.de.scratch1984.spring.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Custom security configuration for integration testing.
 */
@EnableWebSecurity
@Configuration
@Profile("test")
public class SecurityTestConfig {

    /**
     * Disables the standard security settings for testing purposes.
     *
     * @return The new web security configurer adapter.
     */
    @Bean
    public WebSecurityConfigurerAdapter securityDisabled() {
        return new WebSecurityConfigurerAdapter() {

            /**
             * Authorizes any requests for all mappings.
             *
             * @param http The http security.
             * @throws Exception Throws an exception of the authorization requirements are not met.
             */
            @Override
            protected void configure(final HttpSecurity http) throws Exception {
                http.cors().and().authorizeRequests().anyRequest().permitAll();
            }
        };
    }

}
