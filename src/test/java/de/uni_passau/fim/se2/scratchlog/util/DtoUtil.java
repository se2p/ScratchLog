package de.uni_passau.fim.se2.scratchlog.util;

import de.uni_passau.fim.se2.scratchlog.web.dto.ExperimentDTO;

import java.util.UUID;

public class DtoUtil {

    public ExperimentDTO generateExperimentDTO(String title) {
        return new ExperimentDTO(
            null,
            namePrefix() + title,
            "Description for " + title,
            "Information about " + title,
            "Postscript for " + title,
            false,
            false,
            "http://localhost:8601"
        );
    }

    private String namePrefix() {
        return UUID.randomUUID() + "_";
    }
}
