package de.uni_passau.fim.se2.scratchlog.spring.configuration;

import de.uni_passau.fim.se2.scratchlog.util.Constants;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.net.URI;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "whisker")
@Profile(Constants.PROFILE_WHISKER)
public class WhiskerConfiguration {

    private URI baseUrl;

}
