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

import de.uni_passau.fim.se2.scratchlog.persistence.entity.ClickEvent;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Experiment;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.EventProjection;
import de.uni_passau.fim.se2.scratchlog.util.enums.ClickEventSpecific;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.stream.Stream;

/**
 * A repository providing functionality for retrieving the click event data.
 */
public interface ClickEventRepository extends JpaRepository<ClickEvent, Integer> {

    /**
     * Returns all {@link ClickEvent}s that occurred during the given experiment.
     *
     * @param experiment The experiment to search for.
     * @return A {@link List} of all click events.
     */
    Stream<ClickEvent> findAllByExperiment(Experiment experiment);

    /**
     * Returns all {@link ClickEvent}s for the given user and experiment with the given event.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @param event The event to search for.
     * @return A list of all click events of the given event.
     */
    List<EventProjection> findAllByUserAndExperimentAndEvent(User user, Experiment experiment,
                                                             ClickEventSpecific event);

}
