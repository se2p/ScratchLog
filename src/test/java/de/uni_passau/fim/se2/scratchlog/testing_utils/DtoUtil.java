package de.uni_passau.fim.se2.scratchlog.testing_utils;

import de.uni_passau.fim.se2.scratchlog.web.dto.ExperimentDTO;

import java.util.UUID;

public final class DtoUtil {

    public static ExperimentDTO generateExperimentDTO(String title) {
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

    private static String namePrefix() {
        return UUID.randomUUID() + "_";
    }
}
