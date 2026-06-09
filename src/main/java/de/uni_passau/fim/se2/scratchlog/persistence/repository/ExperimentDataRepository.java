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

import de.uni_passau.fim.se2.scratchlog.persistence.entity.ExperimentData;
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
