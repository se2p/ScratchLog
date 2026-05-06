package de.uni_passau.fim.se2.scratchlog.spring.configuration;

import lombok.Getter;
import lombok.Setter;

import java.net.URI;

@Getter
@Setter
public class CodeEmbeddingConfiguration {

    private String model;

    private URI embeddingConnectorUrl;

}
