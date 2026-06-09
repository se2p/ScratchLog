/*
 * This file is part of ScratchLog.
 * Licenced under the GPL v3.0 or later.
 *
 * SPDX-FileCopyrightText: 2021-2026 Scratchlog contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * ScratchLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ScratchLog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
