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

package de.uni_passau.fim.se2.scratchlog.persistence.repository;

import de.uni_passau.fim.se2.scratchlog.persistence.entity.Experiment;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.JsonEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.stream.Stream;

/**
 * A repository providing functionality for retrieving the JSON event data.
 */
public interface JsonEventRepository extends JpaRepository<JsonEvent, Integer> {

    /**
     * Returns all {@link JsonEvent}s that occurred during the given experiment.
     *
     * @param experiment The experiment to search for.
     * @return A {@link List} of all JSON events.
     */
    Stream<JsonEvent> findAllByExperiment(Experiment experiment);

}
