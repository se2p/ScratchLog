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

package de.uni_passau.fim.se2.scratchlog.application.service;

import de.uni_passau.fim.se2.scratchlog.persistence.entity.EventCount;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Experiment;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.ExperimentData;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.EventProjection;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.BlockEventRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ClickEventRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.EventCountRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ExperimentDataRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ExperimentRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ParticipantRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ResourceEventRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.UserRepository;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import de.uni_passau.fim.se2.scratchlog.util.enums.BlockEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.ClickEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.ResourceEventSpecific;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A service providing methods related to the experiment dashboard.
 */
@Service
public class DashboardService {

    /**
     * The experiment repository to use for database queries related to experiment data.
     */
    private final ExperimentRepository experimentRepository;

    /**
     * The experiment data repository to use for database queries related to participant numbers.
     */
    private final ExperimentDataRepository experimentDataRepository;

    /**
     * The participant repository to use for database queries participation data.
     */
    private final ParticipantRepository participantRepository;

    /**
     * The user repository to use for user queries.
     */
    private final UserRepository userRepository;

    /**
     * The block event repository to use for block event queries.
     */
    private final BlockEventRepository blockEventRepository;

    /**
     * The click event repository to use for click event queries.
     */
    private final ClickEventRepository clickEventRepository;

    /**
     * The resource event repository to use for resource event queries.
     */
    private final ResourceEventRepository resourceEventRepository;

    /**
     * The event count repository to use for event count queries.
     */
    private final EventCountRepository eventCountRepository;

    /**
     * Constructs an experiment service with the given dependencies.
     *
     * @param experimentRepository The experiment repository to use.
     * @param experimentDataRepository The experiment data repository to use.
     * @param participantRepository The participant repository to use.
     * @param userRepository The user repository to use.
     * @param blockEventRepository The block event repository to use.
     * @param clickEventRepository The click event repository to use.
     * @param resourceEventRepository The resource event repository to use.
     * @param eventCountRepository The event count repository to use.
     */
    @Autowired
    public DashboardService(final ExperimentRepository experimentRepository,
                            final ExperimentDataRepository experimentDataRepository,
                            final ParticipantRepository participantRepository, final UserRepository userRepository,
                            final BlockEventRepository blockEventRepository,
                            final ClickEventRepository clickEventRepository,
                            final ResourceEventRepository resourceEventRepository,
                            final EventCountRepository eventCountRepository) {
        this.experimentRepository = experimentRepository;
        this.experimentDataRepository = experimentDataRepository;
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
        this.blockEventRepository = blockEventRepository;
        this.clickEventRepository = clickEventRepository;
        this.resourceEventRepository = resourceEventRepository;
        this.eventCountRepository = eventCountRepository;
    }

    /**
     * Checks whether an experiment with the given id exists in the database.
     *
     * @param id The id to search for.
     * @return {@code true} if a corresponding experiment exists or {@code false} otherwise.
     * @throws IllegalArgumentException if the passed id is invalid.
     */
    public boolean existsExperiment(final int id) {
        return experimentRepository.existsById(id);
    }

    /**
     * Checks whether the experiment with the given id has participants.
     *
     * @param id The id of the experiment.
     * @return {@code true} if the experiment has participants, or {@code false} otherwise.
     */
    public boolean existsParticipants(final int id) {
        Experiment experiment = experimentRepository.getReferenceById(id);
        return participantRepository.existsByExperiment(experiment);
    }

    /**
     * Retrieves the experiment data, i.e. how many participants the experiment has and how many of them started and
     * finished it, from the database.
     *
     * @param id The id of the experiment.
     * @return The corresponding experiment data.
     * @throws IllegalArgumentException if the passed id is invalid.
     */
    public ExperimentDataDto getExperimentData(final int id) {
        ExperimentData experimentData = experimentDataRepository.getReferenceById(id);

        return new ExperimentDataDto(
            experimentData.getParticipants(),
            experimentData.getStarted(),
            experimentData.getFinished()
        );
    }

    public record ExperimentDataDto(int participants, int started, int finished) {
    }

    /**
     * Retrieves information about the number of times the given block event was executed per minute during the
     * experiment with the given id for the users with the give id.
     *
     * @param userIds The ids of the users for whom the event count should be calculated.
     * @param experimentId The id of the experiment.
     * @param event The event of interest.
     * @return A list of arrays with the numbers of executions per minute for every user.
     */
    public List<Integer[]> getBlockEventCountData(final List<Integer> userIds, final int experimentId,
                                                  final BlockEventSpecific event) {
        Experiment experiment = getExperiment(experimentId);
        List<Integer[]> eventNumbers = new ArrayList<>();
        userIds.forEach(id -> eventNumbers.add(getBlockEventCounts(id, experiment, event)));
        return eventNumbers;
    }

    /**
     * Retrieves information about the number of times the given click event was executed per minute during the
     * experiment with the given id for the users with the give id.
     *
     * @param userIds The ids of the users for whom the event count should be calculated.
     * @param experimentId The id of the experiment.
     * @param event The event of interest.
     * @return A list of arrays with the numbers of executions per minute for every user.
     */
    public List<Integer[]> getClickEventCountData(final List<Integer> userIds, final int experimentId,
                                                  final ClickEventSpecific event) {
        Experiment experiment = getExperiment(experimentId);
        List<Integer[]> eventNumbers = new ArrayList<>();
        userIds.forEach(id -> eventNumbers.add(getClickEventCounts(id, experiment, event)));
        return eventNumbers;
    }

    /**
     * Retrieves information about the number of times the given resource event was executed per minute during the
     * experiment with the given id for the users with the give id.
     *
     * @param userIds The ids of the users for whom the event count should be calculated.
     * @param experimentId The id of the experiment.
     * @param event The event of interest.
     * @return A list of arrays with the numbers of executions per minute for every user.
     */
    public List<Integer[]> getResourceEventCountData(final List<Integer> userIds, final int experimentId,
                                                     final ResourceEventSpecific event) {
        Experiment experiment = getExperiment(experimentId);
        List<Integer[]> eventNumbers = new ArrayList<>();
        userIds.forEach(id -> eventNumbers.add(getResourceEventCounts(id, experiment, event)));
        return eventNumbers;
    }

    /**
     * Retrieves information about the number of times specific click and block events were executed during the
     * experiment with the given id for the users with the give id.
     *
     * @param userIds The ids of the users for whom the event count should be calculated.
     * @param experimentId The id of the experiment.
     * @return A list of arrays with the numbers of executions for every user.
     */
    public List<Integer[]> getEventCountData(final List<Integer> userIds, final int experimentId) {
        List<Integer[]> eventNumbers = new ArrayList<>();
        userIds.forEach(id -> eventNumbers.add(getEventCounts(id, experimentId)));
        return eventNumbers;
    }

    /**
     * Returns the corresponding experiment.
     *
     * @param experimentId The experiment id to check.
     * @return The corresponding experiment, if all ids are valid.
     * @throws IllegalArgumentException if any of the passed ids are invalid.
     */
    private Experiment getExperiment(final int experimentId) {
        return experimentRepository.getReferenceById(experimentId);
    }

    /**
     * For a given user, experiment and block event, the number of times the event is executed per minute is calculated
     * and returned.
     *
     * @param userId The id of the user.
     * @param experiment The experiment in which the events occurred.
     * @param event The concrete event of interest.
     * @return The number of executions per minute.
     */
    private Integer[] getBlockEventCounts(final int userId, final Experiment experiment,
                                          final BlockEventSpecific event) {
        User user = userRepository.getReferenceById(userId);

        return getSampledEventCounts(blockEventRepository.findAllByUserAndExperimentAndEvent(user, experiment, event));
    }

    /**
     * For a given user, experiment and click event, the number of times the event is executed per minute is calculated
     * and returned.
     *
     * @param userId The id of the user.
     * @param experiment The experiment in which the events occurred.
     * @param event The concrete event of interest.
     * @return The number of executions per minute.
     */
    private Integer[] getClickEventCounts(final int userId, final Experiment experiment,
                                          final ClickEventSpecific event) {
        User user = userRepository.getReferenceById(userId);
        return getSampledEventCounts(clickEventRepository.findAllByUserAndExperimentAndEvent(user, experiment, event));
    }

    /**
     * For a given user, experiment and resource event, the number of times the event is executed per minute is
     * calculated and returned.
     *
     * @param userId The id of the user.
     * @param experiment The experiment in which the events occurred.
     * @param event The concrete event of interest.
     * @return The number of executions per minute.
     */
    private Integer[] getResourceEventCounts(final int userId, final Experiment experiment,
                                             final ResourceEventSpecific event) {
        User user = userRepository.getReferenceById(userId);

        return getSampledEventCounts(
            resourceEventRepository.findAllByUserAndExperimentAndEvent(user, experiment, event)
        );
    }

    /**
     * Retrieves the number of times an event was executed per minute given the list of event projections.
     *
     * @param projections The projections containing information on when an event was executed.
     * @return The number of executions per minute.
     */
    private Integer[] getSampledEventCounts(final List<EventProjection> projections) {
        if (projections.isEmpty()) {
            return new Integer[]{};
        }

        return sampleEventCountPerMinute(projections).toArray(Integer[]::new);
    }

    /**
     * Given a list of {@link EventProjection}s of a specific event or event type, the number of times this
     * event (type) was executed per minute is calculated.
     *
     * @param projections The projections for which the number of executions per minute should be calculated.
     * @return A list of event counts per minute.
     */
    private List<Integer> sampleEventCountPerMinute(final List<EventProjection> projections) {
        List<Integer> counts = new ArrayList<>();
        LocalDateTime startTime = projections.getFirst().getDate();
        int count = 0;
        int i = 1;

        while (i < projections.size() && counts.size() <= Constants.MAX_DATA_POINTS) {
            if (projections.get(i).getDate().isBefore(startTime.plusMinutes(1))) {
                i++;

                if (i == projections.size()) {
                    counts.add(i - count);
                }
            } else {
                counts.add(i - count);
                count = i;
                startTime = startTime.plusMinutes(1);
            }
        }

        return counts;
    }

    /**
     * Retrieves event counts for specific click and block events executed by the user with the given id during the
     * experiment with the given id from the database.
     *
     * @param userId The id of the user.
     * @param experimentId The id of the experiment.
     * @return An array containing the retrieved event counts for the user.
     */
    private Integer[] getEventCounts(final int userId, final int experimentId) {
        List<Integer> counts = new ArrayList<>();
        counts.add(getBlockEventCount(userId, experimentId, BlockEventSpecific.CREATE));
        counts.add(getBlockEventCount(userId, experimentId, BlockEventSpecific.MOVE));
        counts.add(getBlockEventCount(userId, experimentId, BlockEventSpecific.DELETE));
        counts.add(getClickEventCount(userId, experimentId, ClickEventSpecific.GREENFLAG));
        counts.add(getClickEventCount(userId, experimentId, ClickEventSpecific.STOPALL));
        counts.add(getClickEventCount(userId, experimentId, ClickEventSpecific.STACKCLICK));
        return counts.toArray(Integer[]::new);
    }

    /**
     * Returns the number of times the user with the given id executed the given block event during the experiment with
     * the specified id. If no matching event count could be retrieved from the database, zero is returned instead.
     *
     * @param userId The id of the user.
     * @param experimentId The id of the experiment.
     * @param blockEvent The event of interest.
     * @return The number of times the event was executed.
     */
    private int getBlockEventCount(final int userId, final int experimentId, final BlockEventSpecific blockEvent) {
        Optional<EventCount> count = eventCountRepository.findBlockEventCountByUserAndExperiment(userId, experimentId,
                blockEvent.toString());
        return count.map(EventCount::getCount).orElse(0);
    }

    /**
     * Returns the number of times the user with the given id executed the given click event during the experiment with
     * the specified id. If no matching event count could be retrieved from the database, zero is returned instead.
     *
     * @param userId The id of the user.
     * @param experimentId The id of the experiment.
     * @param clickEvent The event of interest.
     * @return The number of times the event was executed.
     */
    private int getClickEventCount(final int userId, final int experimentId, final ClickEventSpecific clickEvent) {
        Optional<EventCount> count = eventCountRepository.findClickEventCountByUserAndExperiment(userId, experimentId,
                clickEvent.toString());
        return count.map(EventCount::getCount).orElse(0);
    }

}
