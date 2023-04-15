/*
 * Copyright (C) 2023 ScratchLog contributors
 *
 * This file is part of ScratchLog.
 *
 * ScratchLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * ScratchLog is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ScratchLog. If not, see <http://www.gnu.org/licenses/>.
 */

package fim.unipassau.de.scratchLog.persistence.repository;

import fim.unipassau.de.scratchLog.persistence.entity.Experiment;
import fim.unipassau.de.scratchLog.persistence.entity.Sb3Zip;
import fim.unipassau.de.scratchLog.persistence.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * A repository providing functionality for sb3 zip files.
 */
public interface Sb3ZipRepository extends JpaRepository<Sb3Zip, Integer> {

    /**
     * Returns the zip file with the given id, if any such file exists.
     *
     * @param id The file id to search for.
     * @return An {@link Optional} zip file.
     */
    Optional<Sb3Zip> findById(int id);

    /**
     * Returns the final project file for the given user and experiment, if any such file exists.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @return An {@link Optional} zip file.
     */
    Optional<Sb3Zip> findFirstByUserAndExperimentOrderByIdDesc(User user, Experiment experiment);

    /**
     * Returns a list of ids of all zip files for the given user created during the given experiment, if any exist.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @return A list of zip file ids.
     */
    @Query("SELECT z.id FROM Sb3Zip z WHERE z.user = :user AND z.experiment = :experiment")
    List<Integer> findAllIdsByUserAndExperiment(@Param("user") User user, @Param("experiment") Experiment experiment);

    /**
     * Returns all zip files for the given user created during the given experiment, if any exist.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @return A list of {@link Sb3Zip} files.
     */
    List<Sb3Zip> findAllByUserAndExperiment(User user, Experiment experiment);

}
