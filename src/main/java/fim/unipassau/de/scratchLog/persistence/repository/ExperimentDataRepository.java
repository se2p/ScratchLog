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

import fim.unipassau.de.scratchLog.persistence.entity.ExperimentData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * A repository providing functionality for retrieving experiment data for an experiment.
 */
public interface ExperimentDataRepository extends JpaRepository<ExperimentData, Integer> {

    /**
     * Returns the experiment data for the experiment with the given id, if one exists.
     *
     * @param experiment The id to search for.
     * @return The experiment data or {@code null}, if no entry could be found.
     */
    Optional<ExperimentData> findByExperiment(int experiment);

}
