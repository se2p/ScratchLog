package fim.unipassau.de.scratch1984.spring.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Provides the bean configuration for the password encoder.
 */
@Configuration
public class EncoderConfiguration {

    /**
     * Configures the password encoder to use for hashing user passwords.
     *
     * @return The new password encoder to use.
     */
    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

}
