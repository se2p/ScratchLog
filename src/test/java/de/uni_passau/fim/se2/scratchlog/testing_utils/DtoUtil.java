package de.uni_passau.fim.se2.scratchlog.testing_utils;

import de.uni_passau.fim.se2.scratchlog.util.enums.Language;
import de.uni_passau.fim.se2.scratchlog.util.enums.Role;
import de.uni_passau.fim.se2.scratchlog.web.dto.ExperimentDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.UserDTO;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Static utility methods for generating DTOs to be used in testing.
 */
public final class DtoUtil {

    /**
     * Creates a {@link UserDTO} whose name and email end in the given {@code username}, prefixed by a unique id.
     *
     * @param username The username that should be prefixed with a unique id.
     * @return The generated user DTO.
     */
    public static UserDTO generateUserDTO(String username) {
        String prefixedName = namePrefix() + username;
        UserDTO userDTO = new UserDTO(
            prefixedName,
            prefixedName + "@example.com",
            Role.PARTICIPANT,
            Language.ENGLISH,
            "password1!",
            null
        );
        userDTO.setLastLogin(LocalDateTime.now());
        return userDTO;
    }

    /**
     * Creates a {@link ExperimentDTO} whose title ends in the given {@code title}, prefixed by a unique id.
     *
     * @param title The title that should be prefixed with a unique id.
     * @return The generated experiment DTO.
     */
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
