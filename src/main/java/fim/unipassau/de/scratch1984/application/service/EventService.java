package fim.unipassau.de.scratch1984.application.service;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.persistence.entity.BlockEvent;
import fim.unipassau.de.scratch1984.persistence.entity.CodesData;
import fim.unipassau.de.scratch1984.persistence.entity.EventCount;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.Participant;
import fim.unipassau.de.scratch1984.persistence.entity.ResourceEvent;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.projection.BlockEventJSONProjection;
import fim.unipassau.de.scratch1984.persistence.projection.BlockEventProjection;
import fim.unipassau.de.scratch1984.persistence.projection.BlockEventXMLProjection;
import fim.unipassau.de.scratch1984.persistence.repository.BlockEventRepository;
import fim.unipassau.de.scratch1984.persistence.repository.CodesDataRepository;
import fim.unipassau.de.scratch1984.persistence.repository.EventCountRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ParticipantRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ResourceEventRepository;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.web.dto.BlockEventDTO;
import fim.unipassau.de.scratch1984.web.dto.CodesDataDTO;
import fim.unipassau.de.scratch1984.web.dto.EventCountDTO;
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
import java.util.ArrayList;
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
    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

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
     * @param resourceEventRepository The {@link ResourceEventRepository} to use.
     * @param participantRepository The {@link ParticipantRepository} to use.
     * @param userRepository The {@link UserRepository} to use.
     * @param experimentRepository The {@link ExperimentRepository} to use.
     */
    @Autowired
    public EventService(final EventCountRepository eventCountRepository,
                        final CodesDataRepository codesDataRepository,
                        final BlockEventRepository blockEventRepository,
                        final ResourceEventRepository resourceEventRepository,
                        final ParticipantRepository participantRepository,
                        final UserRepository userRepository,
                        final ExperimentRepository experimentRepository) {
        this.eventCountRepository = eventCountRepository;
        this.codesDataRepository = codesDataRepository;
        this.blockEventRepository = blockEventRepository;
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
            Participant participant = participantRepository.findByUserAndExperiment(user, experiment);

            if (participant == null) {
                logger.error("No corresponding participant entry could be found for user with id "
                        + blockEventDTO.getUser() + " and experiment " + blockEventDTO.getExperiment()
                        + " when trying to save a block event!");
                return;
            }

            BlockEvent blockEvent = createBlockEvent(blockEventDTO, user, experiment);
            blockEventRepository.save(blockEvent);
        } catch (EntityNotFoundException e) {
            logger.error("Could not find user with id " + blockEventDTO.getUser() + " or experiment with id "
                    + blockEventDTO.getExperiment() + " when trying to save a block event!", e);
        } catch (ConstraintViolationException e) {
            logger.error("Could not store the block event data for user with id " + blockEventDTO.getUser()
                    + " for experiment with id " + blockEventDTO.getExperiment() + " since the block event violates the"
                    + " block event table constraints!", e);
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
            Participant participant = participantRepository.findByUserAndExperiment(user, experiment);

            if (participant == null) {
                logger.error("No corresponding participant entry could be found for user with id "
                        + resourceEventDTO.getUser() + " and experiment " + resourceEventDTO.getExperiment()
                        + " when trying to save a resource event!");
                return;
            }

            ResourceEvent resourceEvent = createResourceEvent(resourceEventDTO, user, experiment);
            resourceEventRepository.save(resourceEvent);
        } catch (EntityNotFoundException e) {
            logger.error("Could not find user with id " + resourceEventDTO.getUser() + " or experiment with id "
                    + resourceEventDTO.getExperiment() + " when trying to save a resource event!", e);
        } catch (ConstraintViolationException e) {
            logger.error("Could not store the resource event data for user with id " + resourceEventDTO.getUser()
                    + " for experiment with id " + resourceEventDTO.getExperiment() + " since the resource event "
                    + "violates the resource event table constraints!", e);
        }
    }

    /**
     * Returns the json code of the block event with the given id. If no corresponding block event can be found, a
     * {@link NotFoundException} is thrown instead.
     *
     * @param id The block event id to search for.
     * @return The json string.
     */
    @Transactional
    public String findJsonById(final int id) {
        if (id < Constants.MIN_ID) {
            logger.error("Cannot find block event with invalid id " + id + "!");
            throw new IllegalArgumentException("Cannot find block event with invalid id " + id + "!");
        }

        Optional<BlockEvent> projection = blockEventRepository.findById(id);

        if (projection.isEmpty()) {
            logger.error("Could not find block event with id " + id + "!");
            throw new NotFoundException("Could not find block event with id " + id + "!");
        } else if (projection.get().getCode() == null) {
            logger.error("No json string could be found for the block event with id " + id + "!");
            throw new IllegalArgumentException("No json string could be found for the block event with id " + id + "!");
        }

        return projection.get().getCode();
    }

    /**
     * Returns the latest saved json code for the user with the given id during the experiment with the given id, if it
     * exists and a participant entry could be found for the user. If no corresponding user or experiment could be
     * found, a {@link NotFoundException} is thrown instead.
     *
     * @param userId The user id to search for.
     * @param experimentId The experiment id to search for.
     * @return The json code, or {@code null}.
     */
    @Transactional
    public String findFirstJSON(final int userId, final int experimentId) {
        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            logger.error("Cannot retrieve the last saved json code for user with invalid id " + userId
                    + " or experiment with invalid id " + experimentId + "!");
            throw new IllegalArgumentException("Cannot retrieve the last saved json code for user with invalid id "
                    + userId + " or experiment with invalid id " + experimentId + "!");
        }

        User user = userRepository.getOne(userId);
        Experiment experiment = experimentRepository.getOne(experimentId);

        try {
            BlockEventJSONProjection projection =
                    blockEventRepository.findFirstByUserAndExperimentAndCodeIsNotNullOrderByDateDesc(user, experiment);
            Participant participant = participantRepository.findByUserAndExperiment(user, experiment);

            if (participant == null) {
                logger.error("No corresponding participant entry could be found for user with id " + userId
                        + " and experiment with id " + experimentId + " when trying to load the last json code!");
                return null;
            } else if (projection == null) {
                logger.info("No json code saved for user with id " + userId + " for experiment with id "
                        + experimentId + ".");
                return null;
            }

            return projection.getCode();
        } catch (EntityNotFoundException e) {
            logger.error("Could not find user with id " + userId + " or experiment with id " + experimentId
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
     */
    @Transactional
    public List<EventCountDTO> getBlockEventCounts(final int user, final int experiment) {
        if (user < Constants.MIN_ID || experiment < Constants.MIN_ID) {
            logger.error("Cannot retrieve block event count for user with invalid id " + user + " or experiment with "
                    + "invalid id " + experiment + "!");
            throw new IllegalArgumentException("Cannot retrieve block event count for user with invalid id " + user
                    + " or experiment with invalid id " + experiment + "!");
        }

        List<EventCount> blockEvents = eventCountRepository.findAllBlockEventsByUserAndExperiment(user, experiment);
        return createEventCountDTOList(blockEvents);
    }

    /**
     * Returns the resource event counts for the user with the given id during the experiment with the given id.
     *
     * @param user The user id to search for.
     * @param experiment The experiment id to search for.
     * @return A list of event count DTOs with the resource event counts.
     */
    @Transactional
    public List<EventCountDTO> getResourceEventCounts(final int user, final int experiment) {
        if (user < Constants.MIN_ID || experiment < Constants.MIN_ID) {
            logger.error("Cannot retrieve resource event count for user with invalid id " + user + " or experiment with"
                    + " invalid id " + experiment + "!");
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
     */
    @Transactional
    public List<BlockEventJSONProjection> getJsonForUser(final int userId, final int experimentId) {
        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            logger.error("Cannot retrieve json data for user with invalid id " + userId + " or experiment with invalid "
                    + "id " + experimentId + "!");
            throw new IllegalArgumentException("Cannot retrieve json data for user with invalid id " + userId
                    + " or experiment with invalid id " + experimentId + "!");
        }

        User user = userRepository.getOne(userId);
        Experiment experiment = experimentRepository.getOne(experimentId);

        try {
            List<BlockEventJSONProjection> json =
                    blockEventRepository.findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(user, experiment);

            if (json.isEmpty()) {
                logger.error("Could not find any json data for user with id " + user + " for experiment with id "
                        + experimentId + "!");
                throw new NotFoundException("Could not find any json data for user with id " + user + " for experiment "
                        + "with id " + experimentId + "!");
            }

            return json;
        } catch (EntityNotFoundException e) {
            logger.error("Could not find user with id " + userId + " or experiment with id " + experimentId
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
     */
    @Transactional
    public List<BlockEventXMLProjection> getXMLForUser(final int userId, final int experimentId) {
        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            logger.error("Cannot retrieve xml data for user with invalid id " + userId + " or experiment with invalid "
                    + "id " + experimentId + "!");
            throw new IllegalArgumentException("Cannot retrieve xml data for user with invalid id " + userId
                    + " or experiment with invalid id " + experimentId + "!");
        }

        User user = userRepository.getOne(userId);
        Experiment experiment = experimentRepository.getOne(experimentId);

        try {
            List<BlockEventXMLProjection> xml = blockEventRepository.findAllByXmlIsNotNullAndUserAndExperiment(user,
                    experiment);

            if (xml.isEmpty()) {
                logger.error("Could not find any xml data for user with id " + user + " for experiment with id "
                        + experimentId + "!");
                throw new NotFoundException("Could not find any xml data for user with id " + user + " for experiment "
                        + "with id " + experimentId + "!");
            }

            return xml;
        } catch (EntityNotFoundException e) {
            logger.error("Could not find user with id " + userId + " or experiment with id " + experimentId
                    + " when trying to download the xml files!", e);
            throw new NotFoundException("Could not find user with id " + userId + " or experiment with id "
                    + experimentId + " when trying to download the xml files!", e);
        }
    }

    /**
     * Retrieves a page of {@link BlockEventProjection}s for the user with the given ID during the experiment with the
     * given ID. If the given parameters are invalid, an {@link IllegalArgumentException} is thrown instead. If no
     * corresponding user or experiment could be found, a {@link NotFoundException} is thrown.
     *
     * @param userId The user ID.
     * @param experimentId The experiment ID.
     * @param pageable The pageable containing the page size and page number.
     * @return The page of block event projections.
     */
    @Transactional
    public Page<BlockEventProjection> getCodesForUser(final int userId, final int experimentId,
                                                      final Pageable pageable) {
        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            logger.error("Cannot retrieve codes data for user with invalid id " + userId + " or experiment with invalid"
                    + " id " + experimentId + "!");
            throw new IllegalArgumentException("Cannot retrieve codes data for user with invalid id " + userId
                    + " or experiment with invalid id " + experimentId + "!");
        }

        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();

        if (pageSize != Constants.PAGE_SIZE) {
            logger.error("Cannot return block event projection page with invalid page size of " + pageSize + "!");
            throw new IllegalArgumentException("Cannot return block event projection page with invalid page size of "
                    + pageSize + "!");
        }

        User user = userRepository.getOne(userId);
        Experiment experiment = experimentRepository.getOne(experimentId);

        try {
            return blockEventRepository.findAllByUserAndExperimentAndXmlIsNotNull(user, experiment,
                    PageRequest.of(currentPage, pageSize, Sort.by("date").ascending()));
        } catch (EntityNotFoundException e) {
            logger.error("Could not find block event projections for user with id " + userId + " or experiment with id "
                    + experimentId + "!", e);
            throw new NotFoundException("Could not find block event projections for user with id " + userId
                    + " or experiment with id " + experimentId + "!", e);
        }
    }

    /**
     * Retrieves the codes data for the user with the given ID during the experiment with the given ID. If the ids are
     * invalid, an {@link IllegalArgumentException} is thrown instead.
     *
     * @param user The user ID.
     * @param experiment The experiment ID.
     * @return The {@link CodesData}, or {@code null}, if no corresponding data could be found.
     */
    @Transactional
    public CodesDataDTO getCodesData(final int user, final int experiment) {
        if (user < Constants.MIN_ID || experiment < Constants.MIN_ID) {
            logger.error("Cannot retrieve codes data for user with invalid id " + user + " or experiment with invalid "
                    + "id " + experiment + "!");
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
     * Retrieves all block event data for the experiment with the given ID as a list of string arrays. If the id is
     * invalid, an {@link IllegalArgumentException} is thrown instead. If no corresponding experiment could be found, a
     * {@link NotFoundException} is thrown.
     *
     * @param id The experiment ID.
     * @return The list of string arrays.
     */
    @Transactional
    public List<String[]> getBlockEventData(final int id) {
        if (id < Constants.MIN_ID) {
            logger.error("Cannot retrieve block event data for experiment with invalid id " + id + "!");
            throw new IllegalArgumentException("Cannot retrieve block event data for experiment with invalid id " + id
                    + "!");
        }

        Experiment experiment = experimentRepository.getOne(id);

        try {
            List<BlockEvent> blockEvents = blockEventRepository.findAllByExperiment(experiment);
            return createBlockEventList(blockEvents);
        } catch (EntityNotFoundException e) {
            logger.error("Could not find experiment with id " + id + " in the database!", e);
            throw new NotFoundException("Could not find experiment with id " + id + " in the database!", e);
        }
    }

    /**
     * Retrieves all resource event data for the experiment with the given ID as a list of string arrays. If the id is
     * invalid, an {@link IllegalArgumentException} is thrown instead. If no corresponding experiment could be found, a
     * {@link NotFoundException} is thrown.
     *
     * @param id The experiment ID.
     * @return The list of string arrays.
     */
    @Transactional
    public List<String[]> getResourceEventData(final int id) {
        if (id < Constants.MIN_ID) {
            logger.error("Cannot retrieve resource event data for experiment with invalid id " + id + "!");
            throw new IllegalArgumentException("Cannot retrieve resource event data for experiment with invalid id "
                    + id + "!");
        }

        Experiment experiment = experimentRepository.getOne(id);

        try {
            List<ResourceEvent> resourceEvents = resourceEventRepository.findAllByExperiment(experiment);
            return createResourceEventList(resourceEvents);
        } catch (EntityNotFoundException e) {
            logger.error("Could not find experiment with id " + id + " in the database!", e);
            throw new NotFoundException("Could not find experiment with id " + id + " in the database!", e);
        }
    }

    /**
     * Retrieves all block event counts for the experiment with the given ID as a list of string arrays. If the id is
     * invalid, an {@link IllegalArgumentException} is thrown instead.
     *
     * @param id The experiment ID.
     * @return The list of string arrays.
     */
    @Transactional
    public List<String[]> getBlockEventCount(final int id) {
        if (id < Constants.MIN_ID) {
            logger.error("Cannot retrieve block event count data for experiment with invalid id " + id + "!");
            throw new IllegalArgumentException("Cannot retrieve block event count data for experiment with invalid id "
                    + id + "!");
        }

        List<EventCount> eventCounts = eventCountRepository.findAllBlockEventsByExperiment(id);
        return createEventCountList(eventCounts);
    }

    /**
     * Retrieves all resource event counts for the experiment with the given ID as a list of string arrays. If the id is
     * invalid, an {@link IllegalArgumentException} is thrown instead.
     *
     * @param id The experiment ID.
     * @return The list of string arrays.
     */
    @Transactional
    public List<String[]> getResourceEventCount(final int id) {
        if (id < Constants.MIN_ID) {
            logger.error("Cannot retrieve resource event count data for experiment with invalid id " + id + "!");
            throw new IllegalArgumentException("Cannot retrieve resource event count data for experiment with invalid "
                    + "id " + id + "!");
        }

        List<EventCount> eventCounts = eventCountRepository.findAllResourceEventsByExperiment(id);
        return createEventCountList(eventCounts);
    }

    /**
     * Retrieves all codes data for the experiment with the given ID as a list of string arrays. If the id is invalid,
     * an {@link IllegalArgumentException} is thrown instead.
     *
     * @param id The experiment ID.
     * @return The list of string arrays.
     */
    @Transactional
    public List<String[]> getCodesDataForExperiment(final int id) {
        if (id < Constants.MIN_ID) {
            logger.error("Cannot retrieve codes data for experiment with invalid id " + id + "!");
            throw new IllegalArgumentException("Cannot retrieve codes data for experiment with invalid id " + id + "!");
        }

        List<CodesData> codesData = codesDataRepository.findAllByExperiment(id);
        return createCodesDataList(codesData);
    }

    /**
     * Creates a {@link CodesDataDTO} with the given information of the {@link CodesData}.
     *
     * @param codesData The entity containing the information.
     * @return The new codes data dto containing the information passed in the entity.
     */
    private CodesDataDTO createCodesDataDTO(final CodesData codesData) {
        CodesDataDTO codesDataDTO = new CodesDataDTO();

        if (codesData.getUser() != null) {
            codesDataDTO.setUser(codesData.getUser());
        }
        if (codesData.getExperiment() != null) {
            codesDataDTO.setExperiment(codesData.getExperiment());
        }
        codesDataDTO.setCount(codesData.getCount());

        return codesDataDTO;
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

        if (user != null) {
            blockEvent.setUser(user);
        }
        if (experiment != null) {
            blockEvent.setExperiment(experiment);
        }
        if (blockEventDTO.getId() != null) {
            blockEvent.setId(blockEventDTO.getId());
        }
        if (blockEventDTO.getDate() != null) {
            blockEvent.setDate(Timestamp.valueOf(blockEventDTO.getDate()));
        }
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

        blockEvent.setEventType(blockEventDTO.getEventType().toString());
        blockEvent.setEvent(blockEventDTO.getEvent().toString());
        return blockEvent;
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

        if (user != null) {
            resourceEvent.setUser(user);
        }
        if (experiment != null) {
            resourceEvent.setExperiment(experiment);
        }
        if (resourceEventDTO.getId() != null) {
            resourceEvent.setId(resourceEventDTO.getId());
        }
        if (resourceEventDTO.getDate() != null) {
            resourceEvent.setDate(Timestamp.valueOf(resourceEventDTO.getDate()));
        }
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

        resourceEvent.setEventType(resourceEventDTO.getEventType().toString());
        resourceEvent.setEvent(resourceEventDTO.getEvent().toString());
        return resourceEvent;
    }

    /**
     * Creates an {@link EventCountDTO} with the given information of the {@link EventCount}.
     *
     * @param eventCount The entity containing the information.
     * @return The new event count DTO containing the information passed in the entity.
     */
    private EventCountDTO createEventCountDTO(final EventCount eventCount) {
        EventCountDTO eventCountDTO = new EventCountDTO();

        if (eventCount.getUser() != null) {
            eventCountDTO.setUser(eventCount.getUser());
        }
        if (eventCount.getExperiment() != null) {
            eventCountDTO.setExperiment(eventCount.getExperiment());
        }
        if (eventCount.getEvent() != null) {
            eventCountDTO.setEvent(eventCount.getEvent());
        }

        eventCountDTO.setCount(eventCount.getCount());
        return eventCountDTO;
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
