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

import fim.unipassau.de.scratchLog.persistence.entity.EventCount;
import fim.unipassau.de.scratchLog.persistence.entity.EventCountId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * A repository providing functionality for retrieving the different event count values.
 */
public interface EventCountRepository extends JpaRepository<EventCount, EventCountId> {

    /**
     * Returns all block count data for the given user during the given experiment, if any exist.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @return A list of event counts that is empty if no entry could be found.
     */
    @Query(nativeQuery = true, value = "SELECT * FROM user_num_block_events AS n WHERE n.`user` = :uId AND "
            + "n.experiment = :expId")
    List<EventCount> findAllBlockEventsByUserAndExperiment(@Param("uId") Integer user,
                                                           @Param("expId") Integer experiment);

    /**
     * Returns all block count data for the given experiment, if any exist.
     *
     * @param experiment The experiment to search for.
     * @return A list of event counts that is empty if no entry could be found.
     */
    @Query(nativeQuery = true, value = "SELECT * FROM user_num_block_events AS n WHERE n.experiment = :expId")
    List<EventCount> findAllBlockEventsByExperiment(@Param("expId") Integer experiment);

    /**
     * Returns all click count data for the given user during the given experiment, if any exist.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @return A list of event counts that is empty if no entry could be found.
     */
    @Query(nativeQuery = true, value = "SELECT * FROM user_num_click_events AS n WHERE n.`user` = :uId AND "
            + "n.experiment = :expId")
    List<EventCount> findAllClickEventsByUserAndExperiment(@Param("uId") Integer user,
                                                           @Param("expId") Integer experiment);

    /**
     * Returns all click count data for the given experiment, if any exist.
     *
     * @param experiment The experiment to search for.
     * @return A list of event counts that is empty if no entry could be found.
     */
    @Query(nativeQuery = true, value = "SELECT * FROM user_num_click_events AS n WHERE n.experiment = :expId")
    List<EventCount> findAllClickEventsByExperiment(@Param("expId") Integer experiment);

    /**
     * Returns all resource count data for the given user during the given experiment, if any exist.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @return A list of event counts that is empty if no entry could be found.
     */
    @Query(nativeQuery = true, value = "SELECT * FROM user_num_resource_events AS n WHERE n.`user` = :uId AND "
            + "n.experiment = :expId")
    List<EventCount> findAllResourceEventsByUserIdAndExperimentId(@Param("uId") Integer user,
                                                                  @Param("expId") Integer experiment);

    /**
     * Returns all resource count data for the given the given experiment, if any exist.
     *
     * @param experiment The experiment to search for.
     * @return A list of event counts that is empty if no entry could be found.
     */
    @Query(nativeQuery = true, value = "SELECT * FROM user_num_resource_events AS n WHERE n.experiment = :expId")
    List<EventCount> findAllResourceEventsByExperiment(@Param("expId") Integer experiment);

}
