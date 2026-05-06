package de.uni_passau.fim.se2.scratchlog.spring.configuration;

import lombok.Getter;
import lombok.Setter;

import java.net.URI;

@Getter
@Setter
public class WhiskerConfiguration {

    private URI baseUrl;

    private int maxParallel = 1;

}
