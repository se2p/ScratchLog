package fim.unipassau.de.scratch1984.application.service;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.persistence.entity.BlockEvent;
import fim.unipassau.de.scratch1984.persistence.entity.ClickEvent;
import fim.unipassau.de.scratch1984.persistence.entity.CodesData;
import fim.unipassau.de.scratch1984.persistence.entity.DebuggerEvent;
import fim.unipassau.de.scratch1984.persistence.entity.Event;
import fim.unipassau.de.scratch1984.persistence.entity.EventCount;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.Participant;
import fim.unipassau.de.scratch1984.persistence.entity.QuestionEvent;
import fim.unipassau.de.scratch1984.persistence.entity.ResourceEvent;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.projection.BlockEventJSONProjection;
import fim.unipassau.de.scratch1984.persistence.projection.BlockEventProjection;
import fim.unipassau.de.scratch1984.persistence.projection.BlockEventXMLProjection;
import fim.unipassau.de.scratch1984.persistence.repository.BlockEventRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ClickEventRepository;
import fim.unipassau.de.scratch1984.persistence.repository.CodesDataRepository;
import fim.unipassau.de.scratch1984.persistence.repository.DebuggerEventRepository;
import fim.unipassau.de.scratch1984.persistence.repository.EventCountRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ParticipantRepository;
import fim.unipassau.de.scratch1984.persistence.repository.QuestionEventRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ResourceEventRepository;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.web.dto.BlockEventDTO;
import fim.unipassau.de.scratch1984.web.dto.ClickEventDTO;
import fim.unipassau.de.scratch1984.web.dto.CodesDataDTO;
import fim.unipassau.de.scratch1984.web.dto.DebuggerEventDTO;
import fim.unipassau.de.scratch1984.web.dto.EventCountDTO;
import fim.unipassau.de.scratch1984.web.dto.EventDTO;
import fim.unipassau.de.scratch1984.web.dto.QuestionEventDTO;
import fim.unipassau.de.scratch1984.web.dto.ResourceEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import java.sql.Timestamp;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(EventService.class);

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
    private final QuestionEventRepository questionEventRepository;

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

    /**
     * Constructs an event service with the given dependencies.
     *
     * @param eventCountRepository The {@link EventCountRepository} to use.
     * @param codesDataRepository The {@link CodesDataRepository} to use.
     * @param blockEventRepository The {@link BlockEventRepository} to use.
     * @param clickEventRepository The {@link ClickEventRepository} to use.
     * @param debuggerEventRepository The {@link DebuggerEventRepository} to use.
     * @param questionEventRepository The {@link QuestionEventRepository} to use.
     * @param resourceEventRepository The {@link ResourceEventRepository} to use.
     * @param participantRepository The {@link ParticipantRepository} to use.
     * @param userRepository The {@link UserRepository} to use.
     * @param experimentRepository The {@link ExperimentRepository} to use.
     */
    @Autowired
    public EventService(final EventCountRepository eventCountRepository,
                        final CodesDataRepository codesDataRepository,
                        final BlockEventRepository blockEventRepository,
                        final ClickEventRepository clickEventRepository,
                        final DebuggerEventRepository debuggerEventRepository,
                        final QuestionEventRepository questionEventRepository,
                        final ResourceEventRepository resourceEventRepository,
                        final ParticipantRepository participantRepository,
                        final UserRepository userRepository,
                        final ExperimentRepository experimentRepository) {
        this.eventCountRepository = eventCountRepository;
        this.codesDataRepository = codesDataRepository;
        this.blockEventRepository = blockEventRepository;
        this.clickEventRepository = clickEventRepository;
        this.debuggerEventRepository = debuggerEventRepository;
        this.questionEventRepository = questionEventRepository;
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
        User user = userRepository.getOne(blockEventDTO.getUser());
        Experiment experiment = experimentRepository.getOne(blockEventDTO.getExperiment());

        try {
            if (isParticipant(user, experiment, blockEventDTO.getUser(), blockEventDTO.getExperiment())
                    && isValidEvent(user, experiment, blockEventDTO.getDate())) {
                BlockEvent blockEvent = createBlockEvent(blockEventDTO, user, experiment);
                blockEventRepository.save(blockEvent);
            }
        } catch (ConstraintViolationException e) {
            LOGGER.error("Could not store the block event data for user with id " + blockEventDTO.getUser()
                    + " for experiment with id " + blockEventDTO.getExperiment() + " since the block event violates the"
                    + " block event table constraints!", e);
        }
    }

    /**
     * Creates a new click event with the given parameters in the database.
     *
     * @param clickEventDTO The dto containing the event information to set.
     */
    @Transactional
    public void saveClickEvent(final ClickEventDTO clickEventDTO) {
        User user = userRepository.getOne(clickEventDTO.getUser());
        Experiment experiment = experimentRepository.getOne(clickEventDTO.getExperiment());

        try {
            if (isParticipant(user, experiment, clickEventDTO.getUser(), clickEventDTO.getExperiment())
                    && isValidEvent(user, experiment, clickEventDTO.getDate())) {
                ClickEvent clickEvent = createClickEvent(clickEventDTO, user, experiment);
                clickEventRepository.save(clickEvent);
            }
        } catch (ConstraintViolationException e) {
            LOGGER.error("Could not store the click event data for user with id " + clickEventDTO.getUser()
                    + " for experiment with id " + clickEventDTO.getExperiment() + " since the click event violates the"
                    + " click event table constraints!", e);
        }
    }

    /**
     * Creates a new debugger event with the given parameters in the database.
     *
     * @param debuggerEventDTO The dto containing the event information to set.
     */
    @Transactional
    public void saveDebuggerEvent(final DebuggerEventDTO debuggerEventDTO) {
        User user = userRepository.getOne(debuggerEventDTO.getUser());
        Experiment experiment = experimentRepository.getOne(debuggerEventDTO.getExperiment());

        try {
            if (isParticipant(user, experiment, debuggerEventDTO.getUser(), debuggerEventDTO.getExperiment())
                    && isValidEvent(user, experiment, debuggerEventDTO.getDate())) {
                DebuggerEvent debuggerEvent = createDebuggerEvent(debuggerEventDTO, user, experiment);
                debuggerEventRepository.save(debuggerEvent);
            }
        } catch (ConstraintViolationException e) {
            LOGGER.error("Could not store the debugger event data for user with id " + debuggerEventDTO.getUser()
                    + " for experiment with id " + debuggerEventDTO.getExperiment() + " since the debugger event "
                    + "violates the debugger event table constraints!", e);
        }
    }

    /**
     * Creates a new question event with the given parameters in the database.
     *
     * @param questionEventDTO The dto containing the event information to set.
     */
    @Transactional
    public void saveQuestionEvent(final QuestionEventDTO questionEventDTO) {
        User user = userRepository.getOne(questionEventDTO.getUser());
        Experiment experiment = experimentRepository.getOne(questionEventDTO.getExperiment());

        try {
            if (isParticipant(user, experiment, questionEventDTO.getUser(), questionEventDTO.getExperiment())
                    && isValidEvent(user, experiment, questionEventDTO.getDate())) {
                QuestionEvent questionEvent = createQuestionEvent(questionEventDTO, user, experiment);
                questionEventRepository.save(questionEvent);
            }
        } catch (ConstraintViolationException e) {
            LOGGER.error("Could not store the question event data for user with id " + questionEventDTO.getUser()
                    + " for experiment with id " + questionEventDTO.getExperiment() + " since the question event "
                    + "violates the question event table constraints!", e);
        }
    }

    /**
     * Creates a new resource event with the given parameters in the database.
     *
     * @param resourceEventDTO The dto containing the event information to set.
     */
    @Transactional
    public void saveResourceEvent(final ResourceEventDTO resourceEventDTO) {
        User user = userRepository.getOne(resourceEventDTO.getUser());
        Experiment experiment = experimentRepository.getOne(resourceEventDTO.getExperiment());

        try {
            if (isParticipant(user, experiment, resourceEventDTO.getUser(), resourceEventDTO.getExperiment())
                    && isValidEvent(user, experiment, resourceEventDTO.getDate())) {
                ResourceEvent resourceEvent = createResourceEvent(resourceEventDTO, user, experiment);
                resourceEventRepository.save(resourceEvent);
            }
        } catch (ConstraintViolationException e) {
            LOGGER.error("Could not store the resource event data for user with id " + resourceEventDTO.getUser()
                    + " for experiment with id " + resourceEventDTO.getExperiment() + " since the resource event "
                    + "violates the resource event table constraints!", e);
        }
    }

    /**
     * Returns the json code of the block event with the given id.
     *
     * @param id The block event id to search for.
     * @return The json string.
     * @throws IllegalArgumentException if the passed id is invalid or the event does not have any JSON code.
     * @throws NotFoundException if no corresponding block event could be found.
     */
    @Transactional
    public String findJsonById(final int id) {
        if (id < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot find block event with invalid id " + id + "!");
        }

        Optional<BlockEvent> projection = blockEventRepository.findById(id);

        if (projection.isEmpty()) {
            LOGGER.error("Could not find block event with id " + id + "!");
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
    @Transactional
    public String findFirstJSON(final int userId, final int experimentId) {
        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot retrieve the last saved json code for user with invalid id "
                    + userId + " or experiment with invalid id " + experimentId + "!");
        }

        User user = userRepository.getOne(userId);
        Experiment experiment = experimentRepository.getOne(experimentId);

        try {
            BlockEventJSONProjection projection =
                    blockEventRepository.findFirstByUserAndExperimentAndCodeIsNotNullOrderByDateDesc(user, experiment);
            Participant participant = participantRepository.findByUserAndExperiment(user, experiment);

            if (!checkReturnFirstJson(participant, projection, user, experiment)) {
                return null;
            }

            return projection.getCode();
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find user with id " + userId + " or experiment with id " + experimentId
                    + " when trying to retrieve the last json file!", e);
            throw new NotFoundException("Could not find user with id " + userId + " or experiment with id "
                    + experimentId + " when trying to retrieve the last json file!", e);
        }
    }

    /**
     * Returns the block event counts for the user with the given id during the experiment with the given id.
     *
     * @param user The user id to search for.
     * @param experiment The experiment id to search for.
     * @return A list of event count DTOs with the block event counts.
     * @throws IllegalArgumentException if the passed user or experiment ids are invalid.
     */
    @Transactional
    public List<EventCountDTO> getBlockEventCounts(final int user, final int experiment) {
        if (user < Constants.MIN_ID || experiment < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot retrieve block event count for user with invalid id " + user
                    + " or experiment with invalid id " + experiment + "!");
        }

        List<EventCount> blockEvents = eventCountRepository.findAllBlockEventsByUserAndExperiment(user, experiment);
        return createEventCountDTOList(blockEvents);
    }

    /**
     * Returns the click event counts for the user with the given id during the experiment with the given id.
     *
     * @param user The user id to search for.
     * @param experiment The experiment id to search for.
     * @return A list of event count DTOs with the click event counts.
     * @throws IllegalArgumentException if the user or experiment ids are invalid.
     */
    @Transactional
    public List<EventCountDTO> getClickEventCounts(final int user, final int experiment) {
        if (user < Constants.MIN_ID || experiment < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot retrieve click event count for user with invalid id " + user
                    + " or experiment with invalid id " + experiment + "!");
        }

        List<EventCount> clickEvents = eventCountRepository.findAllClickEventsByUserAndExperiment(user, experiment);
        return createEventCountDTOList(clickEvents);
    }

    /**
     * Returns the resource event counts for the user with the given id during the experiment with the given id.
     *
     * @param user The user id to search for.
     * @param experiment The experiment id to search for.
     * @return A list of event count DTOs with the resource event counts.
     * @throws IllegalArgumentException if the user or experiment ids are invalid.
     */
    @Transactional
    public List<EventCountDTO> getResourceEventCounts(final int user, final int experiment) {
        if (user < Constants.MIN_ID || experiment < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot retrieve resource event count for user with invalid id " + user
                    + " or experiment with invalid id " + experiment + "!");
        }

        List<EventCount> resourceEvents = eventCountRepository.findAllResourceEventsByUserIdAndExperimentId(user,
                experiment);
        return createEventCountDTOList(resourceEvents);
    }

    /**
     * Retrieves all JSON data and corresponding block event ids saved for the user with the given ID during the
     * experiment with the given ID.
     *
     * @param userId The user ID.
     * @param experimentId The experiment ID.
     * @return The list holding the data.
     * @throws IllegalArgumentException if the user or experiment ids are invalid.
     * @throws NotFoundException if no JSON data could be found or no corresponding user or experiment could be found.
     */
    @Transactional
    public List<BlockEventJSONProjection> getJsonForUser(final int userId, final int experimentId) {
        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot retrieve json data for user with invalid id " + userId
                    + " or experiment with invalid id " + experimentId + "!");
        }

        User user = userRepository.getOne(userId);
        Experiment experiment = experimentRepository.getOne(experimentId);

        try {
            List<BlockEventJSONProjection> json =
                    blockEventRepository.findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(user, experiment);

            if (json.isEmpty()) {
                LOGGER.error("Could not find any json data for user with id " + user + " for experiment with id "
                        + experimentId + "!");
                throw new NotFoundException("Could not find any json data for user with id " + user + " for experiment "
                        + "with id " + experimentId + "!");
            }

            return json;
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find user with id " + userId + " or experiment with id " + experimentId
                    + " when trying to download the json files!", e);
            throw new NotFoundException("Could not find user with id " + userId + " or experiment with id "
                    + experimentId + " when trying to download the json files!", e);
        }
    }

    /**
     * Retrieves all xml data and corresponding block event ids saved for the user with the given ID during the
     * experiment with the given ID.
     *
     * @param userId The user ID.
     * @param experimentId The experiment ID.
     * @return The list holding the data.
     * @throws IllegalArgumentException if the user or experiment ids are invalid.
     * @throws NotFoundException if no xml data could be found or no corresponding user or experiment could be found.
     */
    @Transactional
    public List<BlockEventXMLProjection> getXMLForUser(final int userId, final int experimentId) {
        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot retrieve xml data for user with invalid id " + userId
                    + " or experiment with invalid id " + experimentId + "!");
        }

        User user = userRepository.getOne(userId);
        Experiment experiment = experimentRepository.getOne(experimentId);

        try {
            List<BlockEventXMLProjection> xml = blockEventRepository.findAllByXmlIsNotNullAndUserAndExperiment(user,
                    experiment);

            if (xml.isEmpty()) {
                LOGGER.error("Could not find any xml data for user with id " + user + " for experiment with id "
                        + experimentId + "!");
                throw new NotFoundException("Could not find any xml data for user with id " + user + " for experiment "
                        + "with id " + experimentId + "!");
            }

            return xml;
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find user with id " + userId + " or experiment with id " + experimentId
                    + " when trying to download the xml files!", e);
            throw new NotFoundException("Could not find user with id " + userId + " or experiment with id "
                    + experimentId + " when trying to download the xml files!", e);
        }
    }

    /**
     * Retrieves a page of {@link BlockEventProjection}s for the user with the given ID during the experiment with the
     * given ID.
     *
     * @param userId The user ID.
     * @param experimentId The experiment ID.
     * @param pageable The pageable containing the page size and page number.
     * @return The page of block event projections.
     * @throws IllegalArgumentException if the user or experiment ids are invalid or the page size is invalid.
     * @throws NotFoundException if no corresponding user or experiment could be found.
     */
    @Transactional
    public Page<BlockEventProjection> getCodesForUser(final int userId, final int experimentId,
                                                      final Pageable pageable) {
        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot retrieve codes data for user with invalid id " + userId
                    + " or experiment with invalid id " + experimentId + "!");
        }

        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();

        if (pageSize != Constants.PAGE_SIZE) {
            throw new IllegalArgumentException("Cannot return block event projection page with invalid page size of "
                    + pageSize + "!");
        }

        User user = userRepository.getOne(userId);
        Experiment experiment = experimentRepository.getOne(experimentId);

        try {
            return blockEventRepository.findAllByUserAndExperimentAndXmlIsNotNull(user, experiment,
                    PageRequest.of(currentPage, pageSize, Sort.by("date").ascending()));
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find block event projections for user with id " + userId + " or experiment with id "
                    + experimentId + "!", e);
            throw new NotFoundException("Could not find block event projections for user with id " + userId
                    + " or experiment with id " + experimentId + "!", e);
        }
    }

    /**
     * Retrieves the codes data for the user with the given ID during the experiment with the given ID.
     *
     * @param user The user ID.
     * @param experiment The experiment ID.
     * @return The {@link CodesData}, or {@code null}, if no corresponding data could be found.
     * @throws IllegalArgumentException if the user or experiment ids are invalid.
     */
    @Transactional
    public CodesDataDTO getCodesData(final int user, final int experiment) {
        if (user < Constants.MIN_ID || experiment < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot retrieve codes data for user with invalid id " + user
                    + " or experiment with invalid id " + experiment + "!");
        }

        CodesData codesData = codesDataRepository.findByUserAndExperiment(user, experiment);

        if (codesData == null) {
            return new CodesDataDTO();
        }

        return createCodesDataDTO(codesData);
    }

    /**
     * Retrieves all block event data for the experiment with the given ID as a list of string arrays.
     *
     * @param id The experiment ID.
     * @return The list of string arrays.
     * @throws IllegalArgumentException if the passed id is invalid.
     * @throws NotFoundException if no corresponding experiment entry could be found.
     */
    @Transactional
    public List<String[]> getBlockEventData(final int id) {
        if (id < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot retrieve block event data for experiment with invalid id " + id
                    + "!");
        }

        Experiment experiment = experimentRepository.getOne(id);

        try {
            List<BlockEvent> blockEvents = blockEventRepository.findAllByExperiment(experiment);
            return createBlockEventList(blockEvents);
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find experiment with id " + id + " in the database!", e);
            throw new NotFoundException("Could not find experiment with id " + id + " in the database!", e);
        }
    }

    /**
     * Retrieves all click event data for the experiment with the given ID as a list of string arrays.
     *
     * @param id The experiment ID.
     * @return The list of string arrays.
     * @throws IllegalArgumentException if the passed id is invalid.
     * @throws NotFoundException if no corresponding experiment entry could be found.
     */
    @Transactional
    public List<String[]> getClickEventData(final int id) {
        if (id < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot retrieve click event data for experiment with invalid id " + id
                    + "!");
        }

        Experiment experiment = experimentRepository.getOne(id);

        try {
            List<ClickEvent> clickEvents = clickEventRepository.findAllByExperiment(experiment);
            return createClickEventList(clickEvents);
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find experiment with id " + id + " in the database!", e);
            throw new NotFoundException("Could not find experiment with id " + id + " in the database!", e);
        }
    }

    /**
     * Retrieves all resource event data for the experiment with the given ID as a list of string arrays.
     *
     * @param id The experiment ID.
     * @return The list of string arrays.
     * @throws IllegalArgumentException if the passed id is invalid.
     * @throws NotFoundException if no corresponding experiment entry could be found.
     */
    @Transactional
    public List<String[]> getResourceEventData(final int id) {
        if (id < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot retrieve resource event data for experiment with invalid id "
                    + id + "!");
        }

        Experiment experiment = experimentRepository.getOne(id);

        try {
            List<ResourceEvent> resourceEvents = resourceEventRepository.findAllByExperiment(experiment);
            return createResourceEventList(resourceEvents);
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find experiment with id " + id + " in the database!", e);
            throw new NotFoundException("Could not find experiment with id " + id + " in the database!", e);
        }
    }

    /**
     * Retrieves all block event counts for the experiment with the given ID as a list of string arrays.
     *
     * @param id The experiment ID.
     * @return The list of string arrays.
     * @throws IllegalArgumentException if the passed id is invalid.
     */
    @Transactional
    public List<String[]> getBlockEventCount(final int id) {
        if (id < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot retrieve block event count data for experiment with invalid id "
                    + id + "!");
        }

        List<EventCount> eventCounts = eventCountRepository.findAllBlockEventsByExperiment(id);
        return createEventCountList(eventCounts);
    }

    /**
     * Retrieves all click event counts for the experiment with the given ID as a list of string arrays.
     *
     * @param id The experiment ID.
     * @return The list of string arrays.
     * @throws IllegalArgumentException if the passed id is invalid.
     */
    @Transactional
    public List<String[]> getClickEventCount(final int id) {
        if (id < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot retrieve click event count data for experiment with invalid id "
                    + id + "!");
        }

        List<EventCount> eventCounts = eventCountRepository.findAllClickEventsByExperiment(id);
        return createEventCountList(eventCounts);
    }

    /**
     * Retrieves all resource event counts for the experiment with the given ID as a list of string arrays.
     *
     * @param id The experiment ID.
     * @return The list of string arrays.
     * @throws IllegalArgumentException if the passed id is invalid.
     */
    @Transactional
    public List<String[]> getResourceEventCount(final int id) {
        if (id < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot retrieve resource event count data for experiment with invalid "
                    + "id " + id + "!");
        }

        List<EventCount> eventCounts = eventCountRepository.findAllResourceEventsByExperiment(id);
        return createEventCountList(eventCounts);
    }

    /**
     * Retrieves all codes data for the experiment with the given ID as a list of string arrays.
     *
     * @param id The experiment ID.
     * @return The list of string arrays.
     * @throws IllegalArgumentException if the passed id is invalid.
     */
    @Transactional
    public List<String[]> getCodesDataForExperiment(final int id) {
        if (id < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot retrieve codes data for experiment with invalid id " + id + "!");
        }

        List<CodesData> codesData = codesDataRepository.findAllByExperiment(id);
        return createCodesDataList(codesData);
    }

    /**
     * Checks whether any participant entry exists for the user and experiment with the given id. If no user or
     * experiment with the given id exist, or the user has already finished the experiment, {@code false} is returned.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @param userId The user id.
     * @param experimentId The experiment id.
     * @return {@code true} if a valid participant entry could be found, or {@code false} otherwise.
     */
    private boolean isParticipant(final User user, final Experiment experiment, final int userId,
                                  final int experimentId) {
        try {
            Participant participant = participantRepository.findByUserAndExperiment(user, experiment);

            if (participant == null) {
                LOGGER.error("No corresponding participant entry could be found for user with id " + userId
                        + " and experiment " + experimentId + " when trying to save an event!");
                return false;
            } else if (participant.getEnd() != null) {
                LOGGER.error("Tried to insert an event for participant " + userId + " during experiment "
                        + experimentId + " who has already finished!");
                return false;
            }

            return true;
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find user with id " + userId + " or experiment with id " + experimentId
                    + " when trying to save an event!", e);
            return false;
        }
    }

    /**
     * Checks whether the user, experiment and date instances required for saving any type of event are present.
     *
     * @param user The {@link User} who caused the event.
     * @param experiment The {@link Experiment} during which the event occurred.
     * @param date The time at which the event occurred.
     * @return {@code true} if the given attributes are non-null values, or {@code false} otherwise.
     */
    private boolean isValidEvent(final User user, final Experiment experiment, final LocalDateTime date) {
        if (user == null || experiment == null || date == null) {
            LOGGER.error("Cannot save event to database with user, experiment or timestamp null!");
            return false;
        } else if (!user.isActive() || !experiment.isActive()) {
            LOGGER.error("Cannot save event to database with user or experiment inactive!");
            return false;
        } else {
            return true;
        }
    }

    /**
     * Checks, whether the latest JSON code should be retrieved for the given user and experiment. This is not the case
     * if no corresponding participant could be found, no JSON code could be retrieved or the user or experiment are
     * inactive.
     *
     * @param participant The {@link Participant} to check.
     * @param projection The {@link BlockEventJSONProjection} to check.
     * @param user The {@link User} for whom the code should be retrieved.
     * @param experiment The {@link Experiment} during which the code was generated.
     * @return {@code true} if the code should be returned, or {@code false} otherwise.
     */
    private boolean checkReturnFirstJson(final Participant participant, final BlockEventJSONProjection projection,
                                         final User user, final Experiment experiment) {
        if (participant == null) {
            LOGGER.error("No corresponding participant entry could be found for user with id " + user.getId()
                    + " and experiment with id " + experiment.getId() + " when trying to load the last json code!");
            return false;
        } else if (projection == null) {
            LOGGER.info("No json code saved for user with id " + user.getId() + " for experiment with id "
                    + experiment.getId() + ".");
            return false;
        } else if (!user.isActive() || !experiment.isActive()) {
            LOGGER.error("Tried to load json code for user with id " + user.getId() + " and experiment with id "
                    + experiment.getId() + " with inactive user or experiment!");
            return false;
        } else {
            return true;
        }
    }

    /**
     * Sets the properties for every {@link Event} entity using the values from the given attributes.
     *
     * @param event The event for which the properties are to be set.
     * @param user The {@link User} who caused the event.
     * @param experiment The {@link Experiment} during which the even occurred.
     * @param eventType The event type to be set.
     * @param eventName The concrete event to be set.
     * @param eventDTO The {@link EventDTO} containing additional information.
     */
    private void setEventData(final Event event, final User user, final Experiment experiment, final String eventType,
                              final String eventName, final EventDTO eventDTO) {
        if (eventDTO.getId() != null) {
            event.setId(eventDTO.getId());
        }

        event.setUser(user);
        event.setExperiment(experiment);
        event.setDate(Timestamp.valueOf(eventDTO.getDate()));
        event.setEventType(eventType);
        event.setEvent(eventName);
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
     * @param user The user who caused the event.
     * @param experiment The experiment during which the event occurred.
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

        setEventData(blockEvent, user, experiment, blockEventDTO.getEventType().toString(),
                blockEventDTO.getEvent().toString(), blockEventDTO);
        return blockEvent;
    }

    /**
     * Creates a {@link ClickEvent} with the given information of the {@link ClickEventDTO}, the {@link User}, and the
     * {@link Experiment}.
     *
     * @param clickEventDTO The dto containing the information.
     * @param user The user who caused the event.
     * @param experiment The experiment during which the event occurred.
     * @return The new click event containing the information passed in the DTO.
     */
    private ClickEvent createClickEvent(final ClickEventDTO clickEventDTO, final User user,
                                        final Experiment experiment) {
        ClickEvent clickEvent = new ClickEvent();

        if (clickEventDTO.getMetadata() != null) {
            clickEvent.setMetadata(clickEventDTO.getMetadata());
        }

        setEventData(clickEvent, user, experiment, clickEventDTO.getEventType().toString(),
                clickEventDTO.getEvent().toString(), clickEventDTO);
        return clickEvent;
    }

    /**
     * Creates a {@link DebuggerEvent} with the given information of the {@link DebuggerEventDTO}, the {@link User},
     * and the {@link Experiment}.
     *
     * @param debuggerEventDTO The dto containing the information.
     * @param user The user who caused the event.
     * @param experiment The experiment during which the event occurred.
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

        setEventData(debuggerEvent, user, experiment, debuggerEventDTO.getEventType().toString(),
                debuggerEventDTO.getEvent().toString(), debuggerEventDTO);
        return debuggerEvent;
    }

    /**
     * Creates a {@link QuestionEvent} with the given information of the {@link QuestionEventDTO}, the {@link User},
     * and the {@link Experiment}.
     *
     * @param questionEventDTO The dto containing the information.
     * @param user The user who caused the event.
     * @param experiment The experiment during which the event occurred.
     * @return The new question event containing the information passed in the DTO.
     */
    private QuestionEvent createQuestionEvent(final QuestionEventDTO questionEventDTO, final User user,
                                              final Experiment experiment) {
        QuestionEvent questionEvent = new QuestionEvent();

        if (questionEventDTO.getFeedback() != null) {
            questionEvent.setFeedback(questionEventDTO.getFeedback());
        }
        if (questionEventDTO.getType() != null) {
            questionEvent.setType(questionEventDTO.getType());
        }
        if (questionEventDTO.getValues() != null) {
            questionEvent.setValues(Arrays.toString(questionEventDTO.getValues()).replaceAll("[\\[\\]]", ""));
        }
        if (questionEventDTO.getCategory() != null) {
            questionEvent.setCategory(questionEventDTO.getCategory());
        }
        if (questionEventDTO.getForm() != null) {
            questionEvent.setForm(questionEventDTO.getForm());
        }
        if (questionEventDTO.getBlockID() != null) {
            questionEvent.setBlockID(questionEventDTO.getBlockID());
        }
        if (questionEventDTO.getOpcode() != null) {
            questionEvent.setOpcode(questionEventDTO.getOpcode());
        }

        setEventData(questionEvent, user, experiment, questionEventDTO.getEventType().toString(),
                questionEventDTO.getEvent().toString(), questionEventDTO);
        return questionEvent;
    }

    /**
     * Creates a {@link ResourceEvent} with the given information of the {@link ResourceEventDTO}, the {@link User},
     * and the {@link Experiment}.
     *
     * @param resourceEventDTO The dto containing the information.
     * @param user The user who caused the event.
     * @param experiment The experiment during which the event occurred.
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
        if (resourceEventDTO.getLibraryResource().equals(ResourceEventDTO.LibraryResource.TRUE)) {
            resourceEvent.setLibraryResource(1);
        } else if (resourceEventDTO.getLibraryResource().equals(ResourceEventDTO.LibraryResource.FALSE)) {
            resourceEvent.setLibraryResource(0);
        }

        setEventData(resourceEvent, user, experiment, resourceEventDTO.getEventType().toString(),
                resourceEventDTO.getEvent().toString(), resourceEventDTO);
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

    /**
     * Creates a list of String arrays holding the information passed in the {@link BlockEvent} list.
     *
     * @param blockEvents The block events.
     * @return The new list containing the information passed in the block event objects.
     */
    private List<String[]> createBlockEventList(final List<BlockEvent> blockEvents) {
        List<String[]> events = new ArrayList<>();
        String[] header = {"id", "user", "experiment", "date", "eventType", "event", "spritename", "metadata", "xml",
                "json"};
        events.add(header);

        for (BlockEvent blockEvent : blockEvents) {
            String[] data = {blockEvent.getId().toString(), blockEvent.getUser().getId().toString(),
                    blockEvent.getExperiment().getId().toString(), blockEvent.getDate().toString(),
                    blockEvent.getEventType(), blockEvent.getEvent(), blockEvent.getSprite(), blockEvent.getMetadata(),
                    blockEvent.getXml(), blockEvent.getCode()};
            events.add(data);
        }

        return events;
    }

    /**
     * Creates a list of String arrays holding the information passed in the {@link ClickEvent} list.
     *
     * @param clickEvents The click events.
     * @return The new list containing the information passed in the click event objects.
     */
    private List<String[]> createClickEventList(final List<ClickEvent> clickEvents) {
        List<String[]> events = new ArrayList<>();
        String[] header = {"id", "user", "experiment", "date", "eventType", "event", "metadata"};
        events.add(header);

        for (ClickEvent clickEvent : clickEvents) {
            String[] data = {clickEvent.getId().toString(), clickEvent.getUser().getId().toString(),
                    clickEvent.getExperiment().getId().toString(), clickEvent.getDate().toString(),
                    clickEvent.getEventType(), clickEvent.getEvent(), clickEvent.getMetadata()};
            events.add(data);
        }

        return events;
    }

    /**
     * Creates a list of String arrays holding the information passed in the {@link ResourceEvent} list.
     *
     * @param resourceEvents The resource events.
     * @return The new list containing the information passed in the resource event objects.
     */
    private List<String[]> createResourceEventList(final List<ResourceEvent> resourceEvents) {
        List<String[]> events = new ArrayList<>();
        String[] header = {"id", "user", "experiment", "date", "eventType", "event", "name", "md5", "filetype",
                "library"};
        events.add(header);

        for (ResourceEvent resourceEvent : resourceEvents) {
            String[] data = {resourceEvent.getId().toString(), resourceEvent.getUser().getId().toString(),
                    resourceEvent.getExperiment().getId().toString(), resourceEvent.getDate().toString(),
                    resourceEvent.getEventType(), resourceEvent.getEvent(), resourceEvent.getResourceName(),
                    resourceEvent.getHash(), resourceEvent.getResourceType(), resourceEvent.getLibraryResource() == null
                    ? "null" : resourceEvent.getLibraryResource().toString()};
            events.add(data);
        }

        return events;
    }

    /**
     * Creates a list of String arrays holding the information passed in the {@link EventCount} list.
     *
     * @param eventCounts The event counts.
     * @return The new list containing the information passed in the event count objects.
     */
    private List<String[]> createEventCountList(final List<EventCount> eventCounts) {
        List<String[]> events = new ArrayList<>();
        String[] header = {"user", "experiment", "count", "event"};
        events.add(header);

        for (EventCount eventCount : eventCounts) {
            String[] data = {eventCount.getUser().toString(), eventCount.getExperiment().toString(),
                    String.valueOf(eventCount.getCount()), eventCount.getEvent()};
            events.add(data);
        }

        return events;
    }

    /**
     * Creates a list of String arrays holding the information passed in the {@link CodesData} list.
     *
     * @param codesData The codes data.
     * @return The new list containing the information passed in the codes data objects.
     */
    private List<String[]> createCodesDataList(final List<CodesData> codesData) {
        List<String[]> events = new ArrayList<>();
        String[] header = {"user", "experiment", "count"};
        events.add(header);

        for (CodesData codes : codesData) {
            String[] data = {codes.getUser().toString(), codes.getExperiment().toString(),
                    String.valueOf(codes.getCount())};
            events.add(data);
        }

        return events;
    }

}
