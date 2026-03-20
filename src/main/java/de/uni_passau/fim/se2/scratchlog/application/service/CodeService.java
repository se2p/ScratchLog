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

import de.uni_passau.fim.se2.scratchlog.application.exception.NotFoundException;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.BlockEvent;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Experiment;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Participant;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.BlockEventJSONProjection;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.BlockEventXMLProjection;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.BlockEventRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ExperimentRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ParticipantRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.UserRepository;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import de.uni_passau.fim.se2.scratchlog.web.dto.Sb3ZipDTO;
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
 * A service providing methods related to retrieving information about Scratch code generated during experiments.
 */
@Service
public class CodeService {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger log = LoggerFactory.getLogger(CodeService.class);

    /**
     * The block event repository to use for block event queries.
     */
    private final BlockEventRepository blockEventRepository;

    /**
     * The participant repository to use for participation queries.
     */
    private final ParticipantRepository participantRepository;

    /**
     * The user repository to use for user queries.
     */
    private final UserRepository userRepository;

    /**
     * The experiment repository to use for experiment queries.
     */
    private final ExperimentRepository experimentRepository;

    /**
     * Constructs a code service with the given dependencies.
     *
     * @param blockEventRepository The {@link BlockEventRepository} to use.
     * @param participantRepository The {@link ParticipantRepository} to use.
     * @param userRepository The {@link UserRepository} to use.
     * @param experimentRepository The {@link ExperimentRepository} to use.
     */
    @Autowired
    public CodeService(final BlockEventRepository blockEventRepository,
                       final ParticipantRepository participantRepository,
                       final UserRepository userRepository,
                       final ExperimentRepository experimentRepository) {
        this.blockEventRepository = blockEventRepository;
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
        this.experimentRepository = experimentRepository;
    }

    /**
     * Returns the json code of the block event with the given id.
     *
     * @param id The block event id to search for.
     * @return The json string.
     * @throws IllegalArgumentException if the passed id is invalid or the event does not have any JSON code.
     * @throws NotFoundException if no corresponding block event could be found.
     */
    public String findJsonById(final int id) {
        Optional<BlockEvent> projection = blockEventRepository.findById(id);

        if (projection.isEmpty()) {
            log.error("Could not find block event with id {}!", id);
            throw new NotFoundException("Could not find block event with id " + id + "!");
        } else if (projection.get().getCode() == null) {
            throw new IllegalArgumentException("No json string could be found for the block event with id " + id + "!");
        }

        return projection.get().getCode();
    }

    /**
     * Returns the latest saved json code for the user with the given id during the experiment with the given id, if it
     * exists and a participant entry could be found for the user.
     *
     * @param userId The user id to search for.
     * @param experimentId The experiment id to search for.
     * @return The json code, or {@code null}.
     * @throws IllegalArgumentException if the passed user or experiment ids are invalid.
     * @throws NotFoundException if no corresponding user or experiment entry could be found.
     */
    public String findFirstJSON(final int userId, final int experimentId) {
        User user = userRepository.getReferenceById(userId);
        Experiment experiment = experimentRepository.getReferenceById(experimentId);

        try {
            BlockEventJSONProjection projection =
                    blockEventRepository.findFirstByUserAndExperimentAndCodeIsNotNullOrderByDateDesc(user, experiment);
            Optional<Participant> participant = participantRepository.findByUserAndExperiment(user, experiment);

            if (!checkReturnFirstJson(participant, projection, user, experiment)) {
                return null;
            }

            return projection.getCode();
        } catch (EntityNotFoundException e) {
            log.error(
                "Could not find user with id {} or experiment with id {} when trying to retrieve the last json file!",
                userId, experimentId, e
            );
            throw new NotFoundException("Could not find user with id " + userId + " or experiment with id "
                    + experimentId + " when trying to retrieve the last json file!", e);
        }
    }

    /**
     * Retrieves all JSON data and corresponding block event ids saved for the user with the given ID during the
     * experiment with the given ID. The returned list is sorted ascendingly by date.
     *
     * @param userId The user ID.
     * @param experimentId The experiment ID.
     * @return The list holding the data.
     * @throws IllegalArgumentException if the user or experiment ids are invalid.
     * @throws NotFoundException if no JSON data could be found or no corresponding user or experiment could be found.
     */
    public List<BlockEventJSONProjection> getJsonForUser(final int userId, final int experimentId) {
        User user = userRepository.getReferenceById(userId);
        Experiment experiment = experimentRepository.getReferenceById(experimentId);

        try {
            List<BlockEventJSONProjection> json =
                    blockEventRepository.findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(user, experiment);

            if (json.isEmpty()) {
                log.error(
                    "Could not find any json data for user with id {} for experiment with id {}!",
                    user, experimentId
                );
                throw new NotFoundException("Could not find any json data for user with id " + user + " for experiment "
                        + "with id " + experimentId + "!");
            }

            return json;
        } catch (EntityNotFoundException e) {
            log.error(
                "Could not find user with id {} or experiment with id {} when trying to download the json files!",
                userId, experimentId, e
            );
            throw new NotFoundException("Could not find user with id " + userId + " or experiment with id "
                    + experimentId + " when trying to download the json files!", e);
        }
    }

    /**
     * Filters the json code saved for the given user during the given experiment according to the specified parameters.
     * If the code is to be filtered in minute intervals, the jsons are filtered according to their generation time. If
     * the code within a certain range is to be returned, the jsons are filtered according to the specified start and
     * end positions.
     *
     * @param steps The step interval in minutes.
     * @param startPosition The start of the interval in which all json files should be downloaded.
     * @param endPosition The end of the interval in which all json files should be downloaded.
     * @param userId The id of the user.
     * @param experimentId The id of the experiment.
     * @param finalProject The final project saved for the user, if any.
     * @return The filtered code list.
     * @throws IllegalArgumentException if the given end position is bigger than the number of codes or if it is smaller
     * than the start position.
     */
    public List<BlockEventJSONProjection> getFilteredJsons(final int userId, final int experimentId, final int steps,
                                                           final int startPosition, final int endPosition,
                                                           final Optional<Sb3ZipDTO> finalProject) {
        try {
            List<BlockEventJSONProjection> jsons = getJsonForUser(userId, experimentId);

            if (steps > 0) {
                LocalDateTime lastDateTime = finalProject.isPresent() ? finalProject.get().getDate()
                        : jsons.getLast().getDate();
                return filterProjectionsByStep(jsons, steps, lastDateTime);
            } else if (startPosition > 0) {
                if (endPosition > jsons.size()) {
                    throw new IllegalArgumentException("Cannot generate zip file with invalid end position "
                            + endPosition + " bigger than the amount of saved json strings " + jsons.size() + "!");
                } else if (startPosition > endPosition) {
                    throw new IllegalArgumentException("Cannot filter json codes with start position " + startPosition
                            + " greater than end position " + endPosition + "!");
                }

                return jsons.subList(startPosition - 1, endPosition);
            }

            return jsons;
        } catch (NotFoundException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves all xml data and corresponding block event ids saved for the user with the given ID during the
     * experiment with the given ID. The returned list is sorted ascendingly by date.
     *
     * @param userId The user ID.
     * @param experimentId The experiment ID.
     * @return The list holding the data.
     * @throws IllegalArgumentException if the user or experiment ids are invalid.
     * @throws NotFoundException if no xml data could be found or no corresponding user or experiment could be found.
     */
    public List<BlockEventXMLProjection> getXMLForUser(final int userId, final int experimentId) {
        User user = userRepository.getReferenceById(userId);
        Experiment experiment = experimentRepository.getReferenceById(experimentId);

        try {
            List<BlockEventXMLProjection> xml
                = blockEventRepository.findAllByXmlIsNotNullAndUserAndExperimentOrderByDateAsc(user, experiment);

            if (xml.isEmpty()) {
                log.error(
                    "Could not find any xml data for user with id {} for experiment with id {}!", user, experimentId
                );
                throw new NotFoundException("Could not find any xml data for user with id " + user + " for experiment "
                        + "with id " + experimentId + "!");
            }

            return xml;
        } catch (EntityNotFoundException e) {
            log.error(
                "Could not find user with id {} or experiment with id {} when trying to download the xml files!",
                userId, experimentId, e
            );
            throw new NotFoundException("Could not find user with id " + userId + " or experiment with id "
                    + experimentId + " when trying to download the xml files!", e);
        }
    }

    /**
     * Checks, whether the latest JSON code should be retrieved for the given user and experiment. This is not the case
     * if no corresponding participant could be found or no JSON code could be retrieved.
     *
     * @param participant The {@link Participant} to check.
     * @param projection The {@link BlockEventJSONProjection} to check.
     * @param user The {@link User} for whom the code should be retrieved.
     * @param experiment The {@link Experiment} during which the code was generated.
     * @return {@code true} if the code should be returned, or {@code false} otherwise.
     */
    private boolean checkReturnFirstJson(final Optional<Participant> participant,
                                         final BlockEventJSONProjection projection,
                                         final User user, final Experiment experiment) {
        if (participant.isEmpty()) {
            log.error(
                "No corresponding participant entry could be found for user with id {} and experiment with id {} "
                    + "when trying to load the last json code!", user.getId(), experiment.getId()
            );
            return false;
        } else if (projection == null) {
            log.info(
                "No json code saved for user with id {} for experiment with id {}.", user.getId(), experiment.getId()
            );
            return false;
        } else {
            return true;
        }
    }

    /**
     * Filters the passed {@link BlockEventJSONProjection}s according to the passed steps in minutes. Starting with the
     * first json, steps minutes are added to its datetime. The remaining json files are traversed until one with a
     * timestamp after the calculated one is found. Its predecessor is added to filtered list and the calculated time
     * is increased by one more step. The same json file might be added multiple times if the next calculated timestamp
     * is more than one time step apart from the timestamp of the next json file. To avoid adding the same file too many
     * times, the process skips time breaks longer than a certain threshold.
     *
     * @param projections A list of {@link BlockEventJSONProjection} containing the relevant block event data.
     * @param step The time steps the files should be apart in minutes.
     * @param lastDateTime The datetime of the last file the final project state saved.
     * @return The filtered {@link BlockEventJSONProjection}s.
     */
    private List<BlockEventJSONProjection> filterProjectionsByStep(final List<BlockEventJSONProjection> projections,
                                                                   final int step, final LocalDateTime lastDateTime) {
        List<BlockEventJSONProjection> filteredProjections = new ArrayList<>();
        filteredProjections.add(projections.getFirst());

        if (projections.size() > 1) {
            filteredProjections.addAll(addProjections(projections, step, lastDateTime));
        }

        return filteredProjections;
    }

    /**
     * Adds the passed {@link BlockEventJSONProjection}s to a list depending on their datetime. If the datetime of the
     * current file is after that of the current time, it is added to the list (possibly more than once), unless the
     * datetime is after the maximum allowed time break. In that case, the project is only added once. Finally, the
     * last project file is added and the list returned.
     *
     * @param projections A list of {@link BlockEventJSONProjection} containing the relevant block event data.
     * @param steps The regular desired time break between two projections.
     * @param lastDateTime The {@link LocalDateTime} of the last project.
     * @return The list of filtered projections.
     */
    private List<BlockEventJSONProjection> addProjections(final List<BlockEventJSONProjection> projections,
                                                          final int steps, final LocalDateTime lastDateTime) {
        List<BlockEventJSONProjection> filteredProjections = new ArrayList<>();
        LocalDateTime currentTime = projections.getFirst().getDate().plusMinutes(1);
        LocalDateTime maxTime = currentTime.plusMinutes((long) Constants.MAX_ALLOWED_BREAK_FACTOR * steps);

        for (int i = 1; i < projections.size(); i++) {
            BlockEventJSONProjection projection = projections.get(i);
            LocalDateTime projectionTime = projection.getDate();

            if (projectionTime.isBefore(maxTime)) {
                while (projectionTime.isAfter(currentTime)) {
                    filteredProjections.add(projections.get(i - 1));
                    currentTime = currentTime.plusMinutes(steps);
                    maxTime = maxTime.plusMinutes(steps);
                }
            } else {
                if (!filteredProjections.contains(projections.get(i - 1)) && i > 1) {
                    filteredProjections.add(projections.get(i - 1));
                }

                currentTime = projectionTime;
                maxTime = currentTime.plusMinutes((long) Constants.MAX_ALLOWED_BREAK_FACTOR * steps);
            }
        }

        addLastProjection(filteredProjections, projections.getLast(), lastDateTime, currentTime, maxTime, steps);
        return filteredProjections;
    }

    /**
     * Adds the participant's final sb3 project file to the given {@link BlockEventJSONProjection} list.
     *
     * @param filteredProjections The list of filtered projections.
     * @param lastProjection The last projection to be added.
     * @param lastProjectTime The {@link LocalDateTime} of the last saved project change.
     * @param currentTime The current time to look at.
     * @param maxTime The maximum allowed break time signifying that the participant has been inactive.
     * @param steps The desired step size in minutes.
     */
    private void addLastProjection(final List<BlockEventJSONProjection> filteredProjections,
                                   final BlockEventJSONProjection lastProjection, final LocalDateTime lastProjectTime,
                                   final LocalDateTime currentTime, final LocalDateTime maxTime, final int steps) {
        int compare = lastProjectTime.compareTo(lastProjection.getDate());

        if (compare <= 0) {
            filteredProjections.add(lastProjection);
        }

        LocalDateTime projectTime = lastProjectTime;

        if (projectTime.isBefore(maxTime)) {
            while (projectTime.isAfter(currentTime)) {
                filteredProjections.add(lastProjection);
                projectTime = projectTime.minusMinutes(steps);
            }
        } else {
            filteredProjections.add(lastProjection);
        }
    }

}
