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

import fim.unipassau.de.scratchLog.persistence.entity.BlockEvent;
import fim.unipassau.de.scratchLog.persistence.entity.Experiment;
import fim.unipassau.de.scratchLog.persistence.entity.User;
import fim.unipassau.de.scratchLog.persistence.projection.BlockEventJSONProjection;
import fim.unipassau.de.scratchLog.persistence.projection.BlockEventProjection;
import fim.unipassau.de.scratchLog.persistence.projection.BlockEventXMLProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * A repository providing functionality for retrieving the block event data.
 */
public interface BlockEventRepository extends JpaRepository<BlockEvent, Integer> {

    /**
     * Returns all xml data with the corresponding id of the block event saved for the given user in the given
     * experiment, if any exist.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @return The xml data and corresponding ids or an empty list, if no entry could be found.
     */
    List<BlockEventXMLProjection> findAllByXmlIsNotNullAndUserAndExperiment(User user, Experiment experiment);

    /**
     * Returns all json data with the corresponding id of the block event saved for the given user in the given
     * experiment, if any exist.
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
     * @return An block event projection page.
     */
    Page<BlockEventProjection> findAllByUserAndExperimentAndXmlIsNotNull(User user, Experiment experiment,
                                                                         Pageable pageable);

    /**
     * Returns all {@link BlockEvent}s that occurred during the given experiment.
     *
     * @param experiment The experiment to search for.
     * @return A {@link List} of all block events.
     */
    List<BlockEvent> findAllByExperiment(Experiment experiment);

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
