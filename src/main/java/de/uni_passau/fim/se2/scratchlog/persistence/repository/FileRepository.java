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

package de.uni_passau.fim.se2.scratchlog.persistence.repository;

import de.uni_passau.fim.se2.scratchlog.persistence.entity.Experiment;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.File;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.FileProjection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * A repository providing functionality for retrieving file data.
 */
public interface FileRepository extends JpaRepository<File, Integer> {

    /**
     * Returns the ids and names of all files uploaded by the given user during the given experiment, if any exist.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @return A list of files that is empty if no entry could be found.
     */
    List<FileProjection> findFilesByUserAndExperiment(User user, Experiment experiment);

    /**
     * Returns all files uploaded by the given user during the given experiment, if any exist.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @return A list of files that is empty if no entry could be found.
     */
    List<File> findAllByUserAndExperiment(User user, Experiment experiment);

    /**
     * Returns the file with the given id, if any such file exists.
     *
     * @param id The file id to search for.
     * @return An {@link Optional} file.
     */
    Optional<File> findById(int id);

}
