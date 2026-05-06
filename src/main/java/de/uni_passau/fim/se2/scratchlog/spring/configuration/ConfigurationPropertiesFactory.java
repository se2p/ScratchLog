package de.uni_passau.fim.se2.scratchlog.spring.configuration;

import de.uni_passau.fim.se2.scratchlog.util.Constants;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class ConfigurationPropertiesFactory {

    /**
     * Automatically provided code embedding model configuration.
     * @return The configuration
     */
    @Bean
    @Profile(Constants.PROFILE_CODE_EMBEDDINGS)
    @ConfigurationProperties(prefix = "code-embeddings")
    public CodeEmbeddingConfiguration codeEmbeddingConfiguration() {
        return new CodeEmbeddingConfiguration();
    }

    /**
     * Automatically provided Whisker configuration.
     * @return The configuration
     */
    @Bean
    @Profile(Constants.PROFILE_WHISKER)
    @ConfigurationProperties(prefix = "whisker")
    public WhiskerConfiguration whiskerConfiguration() {
        return new WhiskerConfiguration();
    }

}
