package de.uni_passau.fim.se2.scratchlog.spring.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "code-embeddings")
public class CodeEmbeddingConfiguration {

    private String model;

    private URI embeddingConnectorUrl;

}
