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
import fim.unipassau.de.scratchLog.persistence.entity.Participant;
import fim.unipassau.de.scratchLog.persistence.entity.ParticipantId;
import fim.unipassau.de.scratchLog.persistence.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * A repository providing functionality for retrieving participant information for an experiment.
 */
public interface ParticipantRepository extends JpaRepository<Participant, ParticipantId> {

    /**
     * Checks, whether a given user participated in the given experiment.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @return {@code true} iff the user participated in the experiment.
     */
    boolean existsByUserAndExperiment(User user, Experiment experiment);

    /**
     * Returns the participation data for the given user in the given experiment, if any exist.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @return The participation data or {@code null}, if no entry could be found.
     */
    Optional<Participant> findByUserAndExperiment(User user, Experiment experiment);

    /**
     * Returns a page of participants for the given experiment, if any entries exist.
     *
     * @param experiment The experiment to search for.
     * @param pageable The pageable to use.
     * @return The participant page.
     */
    Page<Participant> findAllByExperiment(Experiment experiment, Pageable pageable);

    /**
     * Returns a list of all participants for the given experiment, if any entries exist.
     *
     * @param experiment The experiment to search for.
     * @return The participant list.
     */
    List<Participant> findAllByExperiment(Experiment experiment);

    /**
     * Returns a list of all participants for the given experiment with the given end datetime, if any entries exist.
     *
     * @param experiment The experiment to search for.
     * @param end The ending datetime.
     * @return The participant list.
     */
    List<Participant> findAllByExperimentAndEnd(Experiment experiment, LocalDateTime end);

    /**
     * Returns a list of all participant relations for the given user, if any entries exist.
     *
     * @param user The user to search for.
     * @return The participation list.
     */
    List<Participant> findAllByUser(User user);

    /**
     * Returns a list of all participant relations for the given user where the experiment has not been finished, if any
     * entries exist.
     *
     * @param user The user to search for.
     * @return The participation list.
     */
    List<Participant> findAllByEndIsNullAndUser(User user);

}
