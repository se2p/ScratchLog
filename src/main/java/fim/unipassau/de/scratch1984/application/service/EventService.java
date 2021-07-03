package fim.unipassau.de.scratch1984.application.service;

import fim.unipassau.de.scratch1984.persistence.entity.BlockEvent;
import fim.unipassau.de.scratch1984.persistence.entity.EventCount;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.Participant;
import fim.unipassau.de.scratch1984.persistence.entity.ResourceEvent;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.repository.BlockEventRepository;
import fim.unipassau.de.scratch1984.persistence.repository.EventCountRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ParticipantRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ResourceEventRepository;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
import fim.unipassau.de.scratch1984.web.dto.BlockEventDTO;
import fim.unipassau.de.scratch1984.web.dto.EventCountDTO;
import fim.unipassau.de.scratch1984.web.dto.ExperimentDTO;
import fim.unipassau.de.scratch1984.web.dto.ResourceEventDTO;
import fim.unipassau.de.scratch1984.web.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import java.sql.Timestamp;
import java.util.List;

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
     * @param eventCountRepository The event count repository to use.
     * @param blockEventRepository The block event repository to use.
     * @param resourceEventRepository The resource event repository to use.
     * @param participantRepository The participant repository to use.
     * @param userRepository The user repository to use.
     * @param experimentRepository THe experiment repository to use.
     */
    @Autowired
    public EventService(final EventCountRepository eventCountRepository,
                        final BlockEventRepository blockEventRepository,
                        final ResourceEventRepository resourceEventRepository,
                        final ParticipantRepository participantRepository,
                        final UserRepository userRepository,
                        final ExperimentRepository experimentRepository) {
        this.eventCountRepository = eventCountRepository;
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
     * Returns the block event counts for the given user during the given experiment.
     *
     * @param userDTO The user to search for.
     * @param experimentDTO The experiment to search for.
     * @return A list of event count DTOs with the block event counts.
     */
    @Transactional
    public List<EventCountDTO> getBlockEventCounts(final UserDTO userDTO, final ExperimentDTO experimentDTO) {
        return null;
    }

    /**
     * Returns the click event counts for the given user during the given experiment.
     *
     * @param userDTO The user to search for.
     * @param experimentDTO The experiment to search for.
     * @return A list of event count DTOs with the click event counts.
     */
    @Transactional
    public List<EventCountDTO> getClickEventCounts(final UserDTO userDTO, final ExperimentDTO experimentDTO) {
        return null;
    }

    /**
     * Returns the resource event counts for the given user during the given experiment.
     *
     * @param userDTO The user to search for.
     * @param experimentDTO The experiment to search for.
     * @return A list of event count DTOs with the resource event counts.
     */
    @Transactional
    public List<EventCountDTO> getResourceEventCounts(final UserDTO userDTO, final ExperimentDTO experimentDTO) {
        return null;
    }

    /**
     * Retrieves all JSON data saved for the user with the given ID during the experiment with the given ID.
     *
     * @param userId The user ID.
     * @param experimentId The experiment ID.
     * @return The String array holding the data.
     */
    @Transactional
    public String[] getJsonForUser(final Integer userId, final Integer experimentId) {
        return null;
    }

    /**
     * Retrieves all xml data saved for the user with the given ID during the experiment with the given ID.
     *
     * @param userId The user ID.
     * @param experimentId The experiment ID.
     * @return The String array holding the data.
     */
    @Transactional
    public String[] getXMLForUser(final Integer userId, final Integer experimentId) {
        return null;
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
        return null;
    }

    /**
     * Creates a list of {@link EventCountDTO}s with the given information of the {@link EventCount} list.
     *
     * @param eventCounts The list containing the individual event counts.
     * @return The new list containing the information passed in the event count objects.
     */
    private List<EventCountDTO> createEventCountDTOList(final List<EventCount> eventCounts) {
        return null;
    }

}
