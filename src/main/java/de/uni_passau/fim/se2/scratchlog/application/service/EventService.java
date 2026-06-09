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

package de.uni_passau.fim.se2.scratchlog.application.service;

import de.uni_passau.fim.se2.scratchlog.persistence.entity.BlockEvent;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.ClickEvent;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.CodesData;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.DebuggerEvent;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.DebuggerQuestionEvent;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Event;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.EventCount;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Experiment;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.JsonEvent;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Participant;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.ResourceEvent;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.BlockEventRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ClickEventRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.CodesDataRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.DebuggerEventRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.DebuggerQuestionEventRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.EventCountRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ExperimentRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.JsonEventRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ParticipantRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ResourceEventRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.UserRepository;
import de.uni_passau.fim.se2.scratchlog.util.enums.LibraryResource;
import de.uni_passau.fim.se2.scratchlog.web.dto.BlockEventDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.ClickEventDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.CodesDataDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.DebuggerEventDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.DebuggerQuestionEventDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.EventCountDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.EventDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.JsonEventDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.ResourceEventDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * A service providing methods related to event logging and retrieving event count results.
 */
@Service
public class EventService {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private final JsonMapper jsonMapper;

    /**
     * The event count repository to use for event count queries.
     */
    private final EventCountRepository eventCountRepository;

    /**
     * The codes data repository to use for codes count queries.
     */
    private final CodesDataRepository codesDataRepository;

    /**
     * The block event repository to use for block event queries.
     */
    private final BlockEventRepository blockEventRepository;

    /**
     * The click event repository to use for click event queries.
     */
    private final ClickEventRepository clickEventRepository;

    /**
     * The debugger event repository to use for debugger event queries.
     */
    private final DebuggerEventRepository debuggerEventRepository;

    /**
     * The question event repository to use for debugger event queries.
     */
    private final DebuggerQuestionEventRepository debuggerQuestionEventRepository;

    /**
     * The JSON event repository to use for JSON event queries.
     */
    private final JsonEventRepository jsonEventRepository;

    /**
     * The resource event repository to use for resource event queries.
     */
    private final ResourceEventRepository resourceEventRepository;

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

    @Autowired
    public EventService(final JsonMapper jsonMapper,
                        final EventCountRepository eventCountRepository,
                        final CodesDataRepository codesDataRepository,
                        final BlockEventRepository blockEventRepository,
                        final ClickEventRepository clickEventRepository,
                        final DebuggerEventRepository debuggerEventRepository,
                        final DebuggerQuestionEventRepository debuggerQuestionEventRepository,
                        final JsonEventRepository jsonEventRepository,
                        final ResourceEventRepository resourceEventRepository,
                        final ParticipantRepository participantRepository,
                        final UserRepository userRepository,
                        final ExperimentRepository experimentRepository) {
        this.jsonMapper = jsonMapper;
        this.eventCountRepository = eventCountRepository;
        this.codesDataRepository = codesDataRepository;
        this.blockEventRepository = blockEventRepository;
        this.clickEventRepository = clickEventRepository;
        this.debuggerEventRepository = debuggerEventRepository;
        this.debuggerQuestionEventRepository = debuggerQuestionEventRepository;
        this.jsonEventRepository = jsonEventRepository;
        this.resourceEventRepository = resourceEventRepository;
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
        this.experimentRepository = experimentRepository;
    }

    /**
     * Creates a new block event with the given parameters in the database.
     *
     * @param blockEventDTO The dto containing the event information to set.
     */
    @Transactional
    public void saveBlockEvent(final BlockEventDTO blockEventDTO) {
        User user = userRepository.getReferenceById(blockEventDTO.getUser());
        Experiment experiment = experimentRepository.getReferenceById(blockEventDTO.getExperiment());

        try {
            if (isParticipant(user, experiment, blockEventDTO.getUser(), blockEventDTO.getExperiment())
                    && isValidEvent(user, experiment, blockEventDTO.getDate())) {
                BlockEvent blockEvent = createBlockEvent(blockEventDTO, user, experiment);
                blockEventRepository.save(blockEvent);
            }
        } catch (ConstraintViolationException e) {
            log.error(
                    "Could not store the block event data for user with id {} for experiment with id {} "
                            + "since the block event violates the block event table constraints!",
                    blockEventDTO.getUser(), blockEventDTO.getExperiment(), e
            );
        }
    }

    /**
     * Creates a new click event with the given parameters in the database.
     *
     * @param clickEventDTO The dto containing the event information to set.
     */
    @Transactional
    public void saveClickEvent(final ClickEventDTO clickEventDTO) {
        User user = userRepository.getReferenceById(clickEventDTO.getUser());
        Experiment experiment = experimentRepository.getReferenceById(clickEventDTO.getExperiment());

        try {
            if (isParticipant(user, experiment, clickEventDTO.getUser(), clickEventDTO.getExperiment())
                    && isValidEvent(user, experiment, clickEventDTO.getDate())) {
                ClickEvent clickEvent = createClickEvent(clickEventDTO, user, experiment);
                clickEventRepository.save(clickEvent);
            }
        } catch (ConstraintViolationException e) {
            log.error(
                    "Could not store the click event data for user with id {} for experiment with id {} "
                            + "since the click event violates the click event table constraints!",
                    clickEventDTO.getUser(), clickEventDTO.getExperiment(), e
            );
        }
    }

    /**
     * Creates a new debugger event with the given parameters in the database.
     *
     * @param debuggerEventDTO The dto containing the event information to set.
     */
    @Transactional
    public void saveDebuggerEvent(final DebuggerEventDTO debuggerEventDTO) {
        User user = userRepository.getReferenceById(debuggerEventDTO.getUser());
        Experiment experiment = experimentRepository.getReferenceById(debuggerEventDTO.getExperiment());

        try {
            if (isParticipant(user, experiment, debuggerEventDTO.getUser(), debuggerEventDTO.getExperiment())
                    && isValidEvent(user, experiment, debuggerEventDTO.getDate())) {
                DebuggerEvent debuggerEvent = createDebuggerEvent(debuggerEventDTO, user, experiment);
                debuggerEventRepository.save(debuggerEvent);
            }
        } catch (ConstraintViolationException e) {
            log.error(
                    "Could not store the debugger event data for user with id {} for experiment with id {} "
                            + "since the debugger event violates the debugger event table constraints!",
                    debuggerEventDTO.getUser(), debuggerEventDTO.getExperiment(), e
            );
        }
    }

    /**
     * Creates a new question event with the given parameters in the database.
     *
     * @param debuggerQuestionEventDTO The dto containing the event information to set.
     */
    @Transactional
    public void saveQuestionEvent(final DebuggerQuestionEventDTO debuggerQuestionEventDTO) {
        User user = userRepository.getReferenceById(debuggerQuestionEventDTO.getUser());
        Experiment experiment = experimentRepository.getReferenceById(debuggerQuestionEventDTO.getExperiment());

        try {
            if (isParticipant(user, experiment, debuggerQuestionEventDTO.getUser(),
                    debuggerQuestionEventDTO.getExperiment())
                    && isValidEvent(user, experiment, debuggerQuestionEventDTO.getDate())) {
                DebuggerQuestionEvent debuggerQuestionEvent =
                        createQuestionEvent(debuggerQuestionEventDTO, user, experiment);
                debuggerQuestionEventRepository.save(debuggerQuestionEvent);
            }
        } catch (ConstraintViolationException e) {
            log.error(
                    "Could not store the question event data for user with id {} for experiment with id {} "
                            + "since the question event violates the question event table constraints!",
                    debuggerQuestionEventDTO.getUser(), debuggerQuestionEventDTO.getExperiment(), e
            );
        }
    }

    /**
     * Creates a new JSON event with the given parameters in the database.
     *
     * @param jsonEventDTO The dto containing the event information to set.
     */
    @Transactional
    public void saveJsonEvent(final JsonEventDTO jsonEventDTO) {
        User user = userRepository.getReferenceById(jsonEventDTO.getUser());
        Experiment experiment = experimentRepository.getReferenceById(jsonEventDTO.getExperiment());

        try {
            if (isParticipant(user, experiment, jsonEventDTO.getUser(), jsonEventDTO.getExperiment())
                    && isValidEvent(user, experiment, jsonEventDTO.getDate())) {
                JsonEvent jsonEvent = createJsonEvent(jsonEventDTO, user, experiment);
                jsonEventRepository.save(jsonEvent);
            }
        } catch (ConstraintViolationException e) {
            log.error(
                    "Could not store the JSON event data for user with id {} for experiment with id {} "
                            + "since the JSON event violates the JSON event table constraints!",
                    jsonEventDTO.getUser(), jsonEventDTO.getExperiment(), e
            );
        }
    }

    /**
     * Creates a new resource event with the given parameters in the database.
     *
     * @param resourceEventDTO The dto containing the event information to set.
     */
    @Transactional
    public void saveResourceEvent(final ResourceEventDTO resourceEventDTO) {
        User user = userRepository.getReferenceById(resourceEventDTO.getUser());
        Experiment experiment = experimentRepository.getReferenceById(resourceEventDTO.getExperiment());

        try {
            if (isParticipant(user, experiment, resourceEventDTO.getUser(), resourceEventDTO.getExperiment())
                    && isValidEvent(user, experiment, resourceEventDTO.getDate())) {
                ResourceEvent resourceEvent = createResourceEvent(resourceEventDTO, user, experiment);
                resourceEventRepository.save(resourceEvent);
            }
        } catch (ConstraintViolationException e) {
            log.error(
                    "Could not store the resource event data for user with id {} for experiment with id {} "
                            + "since the resource event violates the resource event table constraints!",
                    resourceEventDTO.getUser(), resourceEventDTO.getExperiment(), e
            );
        }
    }

    /**
     * Returns the block event counts for the user with the given id during the experiment with the given id.
     *
     * @param user       The user id to search for.
     * @param experiment The experiment id to search for.
     * @return A list of event count DTOs with the block event counts.
     * @throws IllegalArgumentException if the passed user or experiment ids are invalid.
     */
    public List<EventCountDTO> getBlockEventCounts(final int user, final int experiment) {
        List<EventCount> blockEvents = eventCountRepository.findAllBlockEventsByUserAndExperiment(user, experiment);
        return createEventCountDTOList(blockEvents);
    }

    /**
     * Returns the click event counts for the user with the given id during the experiment with the given id.
     *
     * @param user       The user id to search for.
     * @param experiment The experiment id to search for.
     * @return A list of event count DTOs with the click event counts.
     * @throws IllegalArgumentException if the user or experiment ids are invalid.
     */
    public List<EventCountDTO> getClickEventCounts(final int user, final int experiment) {
        List<EventCount> clickEvents = eventCountRepository.findAllClickEventsByUserAndExperiment(user, experiment);
        return createEventCountDTOList(clickEvents);
    }

    /**
     * Returns the JSON event counts for the user with the given id during the experiment with the given id.
     *
     * @param user       The user id to search for.
     * @param experiment The experiment id to search for.
     * @return A list of event count DTOs with the JSON event counts.
     * @throws IllegalArgumentException if the user or experiment ids are invalid.
     */
    public List<EventCountDTO> getJsonEventCounts(final int user, final int experiment) {
        List<EventCount> fileEvents = eventCountRepository.findAllJsonEventsByUserIdAndExperimentId(user,
                experiment);
        return createEventCountDTOList(fileEvents);
    }

    /**
     * Returns the resource event counts for the user with the given id during the experiment with the given id.
     *
     * @param user       The user id to search for.
     * @param experiment The experiment id to search for.
     * @return A list of event count DTOs with the resource event counts.
     * @throws IllegalArgumentException if the user or experiment ids are invalid.
     */
    public List<EventCountDTO> getResourceEventCounts(final int user, final int experiment) {
        List<EventCount> resourceEvents = eventCountRepository.findAllResourceEventsByUserIdAndExperimentId(user,
                experiment);
        return createEventCountDTOList(resourceEvents);
    }

    /**
     * Retrieves the codes data for the user with the given ID during the experiment with the given ID.
     *
     * @param user       The user ID.
     * @param experiment The experiment ID.
     * @return The {@link CodesData}, or {@code null}, if no corresponding data could be found.
     * @throws IllegalArgumentException if the user or experiment ids are invalid.
     */
    public CodesDataDTO getCodesData(final int user, final int experiment) {
        Optional<CodesData> codesData = codesDataRepository.findByUserAndExperiment(user, experiment);

        if (codesData.isEmpty()) {
            return new CodesDataDTO();
        }

        return createCodesDataDTO(codesData.get());
    }

    /**
     * Checks whether any participant entry exists for the user and experiment with the given id. If no user or
     * experiment with the given id exist, or the user has already finished the experiment, {@code false} is returned.
     *
     * @param user         The user to search for.
     * @param experiment   The experiment to search for.
     * @param userId       The user id.
     * @param experimentId The experiment id.
     * @return {@code true} if a valid participant entry could be found, or {@code false} otherwise.
     */
    private boolean isParticipant(final User user, final Experiment experiment, final int userId,
                                  final int experimentId) {
        try {
            Optional<Participant> participant = participantRepository.findByUserAndExperiment(user, experiment);

            if (participant.isEmpty()) {
                log.error(
                        "No participant entry could be found for user {} and "
                                + "experiment {} when trying to save an event!",
                        userId, experimentId
                );
                return false;
            } else if (participant.get().getEnd() != null) {
                log.error(
                        "Tried to insert an event for participant {} during experiment {} who has already finished!",
                        userId, experimentId
                );
                return false;
            }

            return true;
        } catch (EntityNotFoundException e) {
            log.error(
                    "Could not find user with id {} or experiment with id {} when trying to save an event!",
                    userId, experimentId, e
            );
            return false;
        }
    }

    /**
     * Checks whether the user, experiment and date instances required for saving any type of event are present.
     *
     * @param user       The {@link User} who caused the event.
     * @param experiment The {@link Experiment} during which the event occurred.
     * @param date       The time at which the event occurred.
     * @return {@code true} if the given attributes are non-null values, or {@code false} otherwise.
     */
    private boolean isValidEvent(final User user, final Experiment experiment, final LocalDateTime date) {
        if (user == null || experiment == null || date == null) {
            log.error("Cannot save event to database with user, experiment or timestamp null!");
            return false;
        } else if (!user.isActive() || !experiment.isActive()) {
            log.error("Cannot save event to database with user or experiment inactive!");
            return false;
        } else {
            return true;
        }
    }

    /**
     * Sets the properties for every {@link Event} entity using the values from the given attributes.
     *
     * @param event      The event for which the properties are to be set.
     * @param user       The {@link User} who caused the event.
     * @param experiment The {@link Experiment} during which the even occurred.
     * @param eventDTO   The {@link EventDTO} containing additional information.
     */
    private void setEventData(final Event event, final User user, final Experiment experiment,
                              final EventDTO eventDTO) {
        if (eventDTO.getId() != null) {
            event.setId(eventDTO.getId());
        }

        event.setUser(user);
        event.setExperiment(experiment);
        event.setDate(eventDTO.getDate());
    }

    /**
     * Creates a {@link CodesDataDTO} with the given information of the {@link CodesData}.
     *
     * @param codesData The entity containing the information.
     * @return The new codes data dto containing the information passed in the entity.
     */
    private CodesDataDTO createCodesDataDTO(final CodesData codesData) {
        return CodesDataDTO.builder()
                .user(codesData.getUser())
                .experiment(codesData.getExperiment())
                .count(codesData.getCount())
                .build();
    }

    /**
     * Creates a {@link BlockEvent} with the given information of the {@link BlockEventDTO}, the {@link User}, and the
     * {@link Experiment}.
     *
     * @param blockEventDTO The dto containing the information.
     * @param user          The user who caused the event.
     * @param experiment    The experiment during which the event occurred.
     * @return The new block event containing the information passed in the DTO.
     */
    private BlockEvent createBlockEvent(final BlockEventDTO blockEventDTO, final User user,
                                        final Experiment experiment) {
        BlockEvent blockEvent = new BlockEvent();

        if (blockEventDTO.getSprite() != null) {
            blockEvent.setSprite(blockEventDTO.getSprite());
        }
        if (blockEventDTO.getMetadata() != null) {
            blockEvent.setMetadata(blockEventDTO.getMetadata());
        }
        if (blockEventDTO.getXml() != null) {
            blockEvent.setXml(blockEventDTO.getXml());
        }
        if (blockEventDTO.getCode() != null) {
            blockEvent.setCode(blockEventDTO.getCode());
        }

        blockEvent.setEventType(blockEventDTO.getEventType());
        blockEvent.setEvent(blockEventDTO.getEvent());
        setEventData(blockEvent, user, experiment, blockEventDTO);
        return blockEvent;
    }

    /**
     * Creates a {@link ClickEvent} with the given information of the {@link ClickEventDTO}, the {@link User}, and the
     * {@link Experiment}.
     *
     * @param clickEventDTO The dto containing the information.
     * @param user          The user who caused the event.
     * @param experiment    The experiment during which the event occurred.
     * @return The new click event containing the information passed in the DTO.
     */
    private ClickEvent createClickEvent(final ClickEventDTO clickEventDTO, final User user,
                                        final Experiment experiment) {
        ClickEvent clickEvent = new ClickEvent();

        if (clickEventDTO.getMetadata() != null) {
            clickEvent.setMetadata(clickEventDTO.getMetadata());
        }

        clickEvent.setEventType(clickEventDTO.getEventType());
        clickEvent.setEvent(clickEventDTO.getEvent());
        setEventData(clickEvent, user, experiment, clickEventDTO);
        return clickEvent;
    }

    /**
     * Creates a {@link DebuggerEvent} with the given information of the {@link DebuggerEventDTO}, the {@link User},
     * and the {@link Experiment}.
     *
     * @param debuggerEventDTO The dto containing the information.
     * @param user             The user who caused the event.
     * @param experiment       The experiment during which the event occurred.
     * @return The new debugger event containing the information passed in the DTO.
     */
    private DebuggerEvent createDebuggerEvent(final DebuggerEventDTO debuggerEventDTO, final User user,
                                              final Experiment experiment) {
        DebuggerEvent debuggerEvent = new DebuggerEvent();

        if (debuggerEventDTO.getBlockOrTargetID() != null) {
            debuggerEvent.setBlockOrTargetID(debuggerEventDTO.getBlockOrTargetID());
        }
        if (debuggerEventDTO.getNameOrOpcode() != null) {
            debuggerEvent.setNameOrOpcode(debuggerEventDTO.getNameOrOpcode());
        }
        if (debuggerEventDTO.getOriginal() != null) {
            debuggerEvent.setOriginal(debuggerEventDTO.getOriginal());
        }
        if (debuggerEventDTO.getExecution() != null) {
            debuggerEvent.setExecution(debuggerEventDTO.getExecution());
        }

        debuggerEvent.setEventType(debuggerEventDTO.getEventType());
        debuggerEvent.setEvent(debuggerEventDTO.getEvent());
        setEventData(debuggerEvent, user, experiment, debuggerEventDTO);
        return debuggerEvent;
    }

    /**
     * Creates a {@link DebuggerQuestionEvent} with the given information of the {@link DebuggerQuestionEventDTO},
     * the {@link User}, and the {@link Experiment}.
     *
     * @param debuggerQuestionEventDTO The dto containing the information.
     * @param user                     The user who caused the event.
     * @param experiment               The experiment during which the event occurred.
     * @return The new question event containing the information passed in the DTO.
     */
    private DebuggerQuestionEvent createQuestionEvent(final DebuggerQuestionEventDTO debuggerQuestionEventDTO,
                                                      final User user, final Experiment experiment) {
        DebuggerQuestionEvent debuggerQuestionEvent = new DebuggerQuestionEvent();

        if (debuggerQuestionEventDTO.getFeedback() != null) {
            debuggerQuestionEvent.setFeedback(debuggerQuestionEventDTO.getFeedback());
        }
        if (debuggerQuestionEventDTO.getType() != null) {
            debuggerQuestionEvent.setType(debuggerQuestionEventDTO.getType());
        }
        if (debuggerQuestionEventDTO.getValues() != null) {
            debuggerQuestionEvent.setValues(Arrays.toString(debuggerQuestionEventDTO.getValues())
                    .replaceAll("[\\[\\]]", ""));
        }
        if (debuggerQuestionEventDTO.getCategory() != null) {
            debuggerQuestionEvent.setCategory(debuggerQuestionEventDTO.getCategory());
        }
        if (debuggerQuestionEventDTO.getForm() != null) {
            debuggerQuestionEvent.setForm(debuggerQuestionEventDTO.getForm());
        }
        if (debuggerQuestionEventDTO.getBlockID() != null) {
            debuggerQuestionEvent.setBlockID(debuggerQuestionEventDTO.getBlockID());
        }
        if (debuggerQuestionEventDTO.getOpcode() != null) {
            debuggerQuestionEvent.setOpcode(debuggerQuestionEventDTO.getOpcode());
        }

        debuggerQuestionEvent.setEventType(debuggerQuestionEventDTO.getEventType());
        debuggerQuestionEvent.setEvent(debuggerQuestionEventDTO.getEvent());
        setEventData(debuggerQuestionEvent, user, experiment, debuggerQuestionEventDTO);
        return debuggerQuestionEvent;
    }

    /**
     * Creates a {@link JsonEvent} with the given information of the {@link JsonEventDTO}, the {@link User},
     * and the {@link Experiment}.
     *
     * @param jsonEventDTO The dto containing the information.
     * @param user         The user who caused the event.
     * @param experiment   The experiment during which the event occurred.
     * @return The new event containing the information passed in the DTO.
     */
    private JsonEvent createJsonEvent(final JsonEventDTO jsonEventDTO, final User user,
                                      final Experiment experiment) {
        JsonEvent jsonEvent = new JsonEvent();

        if (jsonEventDTO.getName() != null) {
            jsonEvent.setFileName(jsonEventDTO.getName());
        }
        if (jsonEventDTO.getContent() != null) {
            jsonEvent.setContent(jsonMapper.writeValueAsString(jsonEventDTO.getContent()));
        }

        jsonEvent.setEventType(jsonEventDTO.getEventType());
        jsonEvent.setEvent(jsonEventDTO.getEvent());
        setEventData(jsonEvent, user, experiment, jsonEventDTO);
        return jsonEvent;
    }


    /**
     * Creates a {@link ResourceEvent} with the given information of the {@link ResourceEventDTO}, the {@link User},
     * and the {@link Experiment}.
     *
     * @param resourceEventDTO The dto containing the information.
     * @param user             The user who caused the event.
     * @param experiment       The experiment during which the event occurred.
     * @return The new block event containing the information passed in the DTO.
     */
    private ResourceEvent createResourceEvent(final ResourceEventDTO resourceEventDTO, final User user,
                                              final Experiment experiment) {
        ResourceEvent resourceEvent = new ResourceEvent();

        if (resourceEventDTO.getName() != null) {
            resourceEvent.setResourceName(resourceEventDTO.getName());
        }
        if (resourceEventDTO.getMd5() != null) {
            resourceEvent.setHash(resourceEventDTO.getMd5());
        }
        if (resourceEventDTO.getFiletype() != null) {
            resourceEvent.setResourceType(resourceEventDTO.getFiletype());
        }
        if (resourceEventDTO.getLibraryResource().equals(LibraryResource.TRUE)) {
            resourceEvent.setLibraryResource(1);
        } else if (resourceEventDTO.getLibraryResource().equals(LibraryResource.FALSE)) {
            resourceEvent.setLibraryResource(0);
        }

        resourceEvent.setEventType(resourceEventDTO.getEventType());
        resourceEvent.setEvent(resourceEventDTO.getEvent());
        setEventData(resourceEvent, user, experiment, resourceEventDTO);
        return resourceEvent;
    }

    /**
     * Creates an {@link EventCountDTO} with the given information of the {@link EventCount}.
     *
     * @param eventCount The entity containing the information.
     * @return The new event count DTO containing the information passed in the entity.
     */
    private EventCountDTO createEventCountDTO(final EventCount eventCount) {
        return EventCountDTO.builder()
                .user(eventCount.getUser())
                .experiment(eventCount.getExperiment())
                .event(eventCount.getEvent())
                .count(eventCount.getCount())
                .build();
    }

    /**
     * Creates a list of {@link EventCountDTO}s with the given information of the {@link EventCount} list.
     *
     * @param eventCounts The list containing the individual event counts.
     * @return The new list containing the information passed in the event count objects.
     */
    private List<EventCountDTO> createEventCountDTOList(final List<EventCount> eventCounts) {
        List<EventCountDTO> eventCountDTOS = new ArrayList<>();

        for (EventCount eventCount : eventCounts) {
            EventCountDTO eventCountDTO = createEventCountDTO(eventCount);
            eventCountDTOS.add(eventCountDTO);
        }

        return eventCountDTOS;
    }

}
