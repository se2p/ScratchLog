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

import de.uni_passau.fim.se2.scratchlog.persistence.entity.EventCount;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.EventCountId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

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
     * Returns the total number of times the given user executed the given block event during the given experiment, if
     * it was executed at all.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @param event The event to search for.
     * @return The number of times the event was executed or an empty {@link Optional} if the event was never executed.
     */
    @Query(nativeQuery = true, value = "SELECT * FROM user_num_block_events AS n WHERE n.`user` = :uId AND "
            + "n.experiment = :expId AND n.event = :event")
    Optional<EventCount> findBlockEventCountByUserAndExperiment(@Param("uId") Integer user,
                                                                @Param("expId") Integer experiment,
                                                                @Param("event") String event);

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
     * Returns the total number of times the given user executed the given click event during the given experiment, if
     * it was executed at all.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @param event The event to search for.
     * @return The number of times the event was executed or an empty {@link Optional} if the event was never executed.
     */
    @Query(nativeQuery = true, value = "SELECT * FROM user_num_click_events AS n WHERE n.`user` = :uId AND "
            + "n.experiment = :expId AND n.event = :event")
    Optional<EventCount> findClickEventCountByUserAndExperiment(@Param("uId") Integer user,
                                                                @Param("expId") Integer experiment,
                                                                @Param("event") String event);

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
     * Returns all file count data for the given user during the given experiment, if any exist.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @return A list of event counts that is empty if no entry could be found.
     */
    @Query(nativeQuery = true, value = "SELECT * FROM user_num_json_events AS n WHERE n.`user` = :uId AND "
        + "n.experiment = :expId")
    List<EventCount> findAllJsonEventsByUserIdAndExperimentId(@Param("uId") Integer user,
                                                                  @Param("expId") Integer experiment);

}
