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

package fim.unipassau.de.scratchLog.application.service;

import fim.unipassau.de.scratchLog.application.exception.NotFoundException;
import fim.unipassau.de.scratchLog.persistence.entity.EventCount;
import fim.unipassau.de.scratchLog.persistence.entity.Experiment;
import fim.unipassau.de.scratchLog.persistence.entity.ExperimentData;
import fim.unipassau.de.scratchLog.persistence.entity.Participant;
import fim.unipassau.de.scratchLog.persistence.entity.User;
import fim.unipassau.de.scratchLog.persistence.projection.EventProjection;
import fim.unipassau.de.scratchLog.persistence.repository.BlockEventRepository;
import fim.unipassau.de.scratchLog.persistence.repository.ClickEventRepository;
import fim.unipassau.de.scratchLog.persistence.repository.EventCountRepository;
import fim.unipassau.de.scratchLog.persistence.repository.ExperimentDataRepository;
import fim.unipassau.de.scratchLog.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratchLog.persistence.repository.ParticipantRepository;
import fim.unipassau.de.scratchLog.persistence.repository.UserRepository;
import fim.unipassau.de.scratchLog.util.Constants;
import fim.unipassau.de.scratchLog.util.enums.BlockEventSpecific;
import fim.unipassau.de.scratchLog.util.enums.ClickEventSpecific;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardService.class);

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
     * The event count repository to use for event count queries.
     */
    private final EventCountRepository eventCountRepository;

    /**
     * The maximum allowed gap in minutes between two events when calculating event counts.
     */
    private static final int MAX_GAP = 10;

    /**
     * Constructs an experiment service with the given dependencies.
     *
     * @param experimentRepository The experiment repository to use.
     * @param experimentDataRepository The experiment data repository to use.
     * @param participantRepository The participant repository to use.
     * @param userRepository The user repository to use.
     * @param blockEventRepository The block event repository to use.
     * @param clickEventRepository The click event repository to use.
     * @param eventCountRepository The event count repository to use.
     */
    @Autowired
    public DashboardService(final ExperimentRepository experimentRepository,
                            final ExperimentDataRepository experimentDataRepository,
                            final ParticipantRepository participantRepository, final UserRepository userRepository,
                            final BlockEventRepository blockEventRepository,
                            final ClickEventRepository clickEventRepository,
                            final EventCountRepository eventCountRepository) {
        this.experimentRepository = experimentRepository;
        this.experimentDataRepository = experimentDataRepository;
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
        this.blockEventRepository = blockEventRepository;
        this.clickEventRepository = clickEventRepository;
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
        if (id < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot check if experiment exists with invalid id " + id + "!");
        }

        return experimentRepository.existsById(id);
    }

    /**
     * Checks whether the experiment with the given id has participants.
     *
     * @param id The id of the experiment.
     * @return {@code true} if the experiment has participants, or {@code false} otherwise.
     * @throws IllegalArgumentException if the passed id is invalid.
     */
    public boolean existsParticipants(final int id) {
        if (id < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot check if participants exist for experiment with invalid id "
                    + id + "!");
        }

        Experiment experiment = experimentRepository.getReferenceById(id);

        try {
            return participantRepository.existsByExperiment(experiment);
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find experiment with id " + id + " in the database!", e);
            return false;
        }
    }

    /**
     * Retrieves the experiment data, i.e. how many participants the experiment has and how many of them started and
     * finished it, from the database.
     *
     * @param id The id of the experiment.
     * @return The corresponding experiment data.
     * @throws IllegalArgumentException if the passed id is invalid.
     * @throws NotFoundException if no corresponding experiment data could be found.
     */
    public String[] getExperimentData(final int id) {
        if (id < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot fetch experiment data for experiment with invalid id " + id
                    + "!");
        }

        Optional<ExperimentData> experimentData = experimentDataRepository.findByExperiment(id);

        if (experimentData.isEmpty()) {
            LOGGER.error("Could not find experiment data for experiment with id " + id + "!");
            throw new NotFoundException("Could not find experiment data for experiment with id " + id + "!");
        }

        return new String[]{String.valueOf(experimentData.get().getParticipants()),
                String.valueOf(experimentData.get().getStarted()), String.valueOf(experimentData.get().getFinished())};
    }

    /**
     * Retrieves the ids and usernames of all participants of the experiment with the given id.
     *
     * @param id The experiment id.
     * @return A list of participant ids and usernames.
     * @throws IllegalArgumentException if the passed id is invalid.
     * @throws IllegalStateException if the experiment has not participants.
     * @throws NotFoundException if no corresponding experiment could be found.
     */
    public List<String[]> getParticipants(final int id) {
        if (id < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot retrieve participant data for experiment with invalid id " + id
                    + "!");
        }

        Experiment experiment = experimentRepository.getReferenceById(id);

        try {
            List<Participant> participants = participantRepository.findAllByExperiment(experiment);

            if (participants.isEmpty()) {
                throw new IllegalStateException("Could not find any participants for experiment with id " + id + "!");
            }

            List<String[]> userInfo = new ArrayList<>();
            participants.forEach(participant -> userInfo.add(new String[]{String.valueOf(participant.getUser().getId()),
                    participant.getUser().getUsername()}));
            return userInfo;
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find experiment with id " + id + " in the database!", e);
            throw new NotFoundException("Could not find experiment with id " + id + " in the database!", e);
        }
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
        Experiment experiment = checkInputsAndGetExperiment(userIds, experimentId);
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
        Experiment experiment = checkInputsAndGetExperiment(userIds, experimentId);
        List<Integer[]> eventNumbers = new ArrayList<>();
        userIds.forEach(id -> eventNumbers.add(getClickEventCounts(id, experiment, event)));
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
        checkInputsAndGetExperiment(userIds, experimentId);
        List<Integer[]> eventNumbers = new ArrayList<>();
        userIds.forEach(id -> eventNumbers.add(getEventCounts(id, experimentId)));
        return eventNumbers;
    }

    /**
     * Checks, whether the passed list of user ids and the experiment id are valid and returns the corresponding
     * experiment.
     *
     * @param userIds A list of user ids to check.
     * @param experimentId The experiment id to check.
     * @return The corresponding experiment, if all ids are valid.
     * @throws IllegalArgumentException if any of the passed ids are invalid.
     */
    private Experiment checkInputsAndGetExperiment(final List<Integer> userIds, final int experimentId) {
        if (experimentId < Constants.MIN_ID || userIds.stream().anyMatch(id -> id < Constants.MIN_ID)) {
            throw new IllegalArgumentException("Cannot retrieve event data for experiment with invalid experiment or "
                    + "user ids!");
        }

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
     * @throws NotFoundException if the given user or experiment could not be found.
     */
    private Integer[] getBlockEventCounts(final int userId, final Experiment experiment,
                                          final BlockEventSpecific event) {
        User user = userRepository.getReferenceById(userId);

        try {
            return getSampledEventCounts(blockEventRepository.findAllByUserAndExperimentAndEvent(user, experiment,
                    event));
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find user or experiment when trying to retrieve block event data!", e);
            throw new NotFoundException("Could not find user or experiment when trying to retrieve block event data!",
                    e);
        }
    }

    /**
     * For a given user, experiment and click event, the number of times the event is executed per minute is calculated
     * and returned.
     *
     * @param userId The id of the user.
     * @param experiment The experiment in which the events occurred.
     * @param event The concrete event of interest.
     * @return The number of executions per minute.
     * @throws NotFoundException if the given user or experiment could not be found.
     */
    private Integer[] getClickEventCounts(final int userId, final Experiment experiment,
                                          final ClickEventSpecific event) {
        User user = userRepository.getReferenceById(userId);

        try {
            return getSampledEventCounts(clickEventRepository.findAllByUserAndExperimentAndEvent(user, experiment,
                    event));
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find user or experiment when trying to retrieve click event data!", e);
            throw new NotFoundException("Could not find user or experiment when trying to retrieve click event data!",
                    e);
        }
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
        LocalDateTime startTime = projections.get(0).getDate();
        int count = 0;
        int i = 1;

        while (i < projections.size()) {
            if (projections.get(i).getDate().isBefore(startTime.plusMinutes(1))) {
                i++;

                if (i == projections.size()) {
                    counts.add(i - count);
                }
            } else {
                counts.add(i - count);
                count = i;
                LocalDateTime nextTime = projections.get(i).getDate();
                startTime = nextTime.isAfter(startTime.plusMinutes(MAX_GAP)) ? nextTime : startTime.plusMinutes(1);
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
        return count.isEmpty() ? 0 : count.get().getCount();
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
        return count.isEmpty() ? 0 : count.get().getCount();
    }

}
