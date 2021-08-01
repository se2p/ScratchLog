package fim.unipassau.de.scratch1984.spring.configuration;

import fim.unipassau.de.scratch1984.spring.authentication.CustomAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Custom security configuration for accessing application content.
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
@Profile("!test")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

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
     * @throws Exception Throws an exception if a user with insufficient privileges tries to access a restricted page.
     */
    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.cors()
                .and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/login", "/finish", "/token/password", "/reset",
                        "/users/reset").not().authenticated()
                .antMatchers("/experiment/*", "/users/add", "/users/delete", "/users/forgot", "/users/add",
                        "/result").hasRole("ADMIN")
                .antMatchers("/experiment", "/users/profile", "/users/logout", "/users/edit",
                        "/users/update").hasRole("PARTICIPANT")
                .and().formLogin().loginPage("/login").usernameParameter("username")
                .defaultSuccessUrl("/index", true)
                .and().headers().frameOptions().sameOrigin();
    }

}
