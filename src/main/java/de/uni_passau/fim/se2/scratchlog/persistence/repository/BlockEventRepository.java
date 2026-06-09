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

import de.uni_passau.fim.se2.scratchlog.persistence.entity.BlockEvent;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Experiment;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.BlockEventJSONProjection;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.BlockEventProjection;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.EventProjection;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.BlockEventXMLProjection;
import de.uni_passau.fim.se2.scratchlog.util.enums.BlockEventSpecific;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.stream.Stream;

/**
 * A repository providing functionality for retrieving the block event data.
 */
public interface BlockEventRepository extends JpaRepository<BlockEvent, Integer> {

    /**
     * Returns all xml data with the corresponding id of the block event saved for the given user in the given
     * experiment, if any exist. The returned list is sorted ascendingly by date.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @return The xml data and corresponding ids or an empty list, if no entry could be found.
     */
    List<BlockEventXMLProjection> findAllByXmlIsNotNullAndUserAndExperimentOrderByDateAsc(User user,
                                                                                          Experiment experiment);

    /**
     * Returns all json data with the corresponding id of the block event saved for the given user in the given
     * experiment, if any exist. The returned list is sorted ascendingly by date.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @return The json data and corresponding ids or an empty list, if no entry could be found.
     */
    List<BlockEventJSONProjection> findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(User user,
                                                                                            Experiment experiment);

    /**
     * Returns a page of {@link BlockEventProjection}s for the given user and experiment corresponding to the parameters
     * set in the pageable.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @param pageable The pageable to use.
     * @return A block event projection page.
     */
    Page<BlockEventProjection> findAllByUserAndExperimentAndXmlIsNotNull(User user, Experiment experiment,
                                                                         Pageable pageable);

    /**
     * Returns all {@link BlockEvent}s that occurred during the given experiment.
     *
     * @param experiment The experiment to search for.
     * @return A {@link List} of all block events.
     */
    Stream<BlockEvent> findAllByExperiment(Experiment experiment);

    /**
     * Returns all {@link EventProjection}s for the given user and experiment with the given event.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @param event The event to search for.
     * @return A list of all block events of the given event.
     */
    List<EventProjection> findAllByUserAndExperimentAndEvent(User user, Experiment experiment,
                                                             BlockEventSpecific event);

    /**
     * Returns a {@link BlockEventJSONProjection} containing the last non-null JSON code that was saved for the given
     * user during the given experiment.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @return The {@link BlockEventJSONProjection}.
     */
    BlockEventJSONProjection findFirstByUserAndExperimentAndCodeIsNotNullOrderByDateDesc(User user,
                                                                                         Experiment experiment);

}
