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
import fim.unipassau.de.scratchLog.persistence.entity.ResourceEvent;
import fim.unipassau.de.scratchLog.persistence.entity.User;
import fim.unipassau.de.scratchLog.persistence.projection.EventProjection;
import fim.unipassau.de.scratchLog.util.enums.ResourceEventSpecific;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * A repository providing functionality for retrieving the resource event data.
 */
public interface ResourceEventRepository extends JpaRepository<ResourceEvent, Integer> {

    /**
     * Returns all {@link ResourceEvent}s that occurred during the given experiment.
     *
     * @param experiment The experiment to search for.
     * @return A {@link List} of all resource events.
     */
    List<ResourceEvent> findAllByExperiment(Experiment experiment);

    /**
     * Returns all {@link ResourceEvent}s for the given user and experiment with the given event.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @param event The event to search for.
     * @return A list of all click events of the given event.
     */
    List<EventProjection> findAllByUserAndExperimentAndEvent(User user, Experiment experiment,
                                                             ResourceEventSpecific event);

}
