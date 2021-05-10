package fim.unipassau.de.scratch1984.application.service;

import fim.unipassau.de.scratch1984.persistence.entity.BlockEvent;
import fim.unipassau.de.scratch1984.persistence.entity.EventCount;
import fim.unipassau.de.scratch1984.persistence.entity.ResourceEvent;
import fim.unipassau.de.scratch1984.persistence.repository.BlockEventRepository;
import fim.unipassau.de.scratch1984.persistence.repository.EventCountRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ResourceEventRepository;
import fim.unipassau.de.scratch1984.web.dto.BlockEventDTO;
import fim.unipassau.de.scratch1984.web.dto.EventCountDTO;
import fim.unipassau.de.scratch1984.web.dto.ExperimentDTO;
import fim.unipassau.de.scratch1984.web.dto.ResourceEventDTO;
import fim.unipassau.de.scratch1984.web.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * A service providing methods related to event logging and retrieving event count results.
 */
@Service
public class EventService {

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
     * Constructs an event service with the given dependencies.
     *
     * @param eventCountRepository The event count repository to use.
     * @param blockEventRepository The block event repository to use.
     * @param resourceEventRepository The resource event repository to use.
     */
    @Autowired
    public EventService(final EventCountRepository eventCountRepository,
                        final BlockEventRepository blockEventRepository,
                        final ResourceEventRepository resourceEventRepository) {
        this.eventCountRepository = eventCountRepository;
        this.blockEventRepository = blockEventRepository;
        this.resourceEventRepository = resourceEventRepository;
    }

    /**
     * Creates a new block event with the given parameters in the database.
     *
     * @param blockEventDTO The dto containing the event information to set.
     */
    public void saveBlockEvent(final BlockEventDTO blockEventDTO) {
    }

    /**
     * Creates a new resource event with the given parameters in the database.
     *
     * @param resourceEventDTO The dto containing the event information to set.
     */
    public void saveResourceEvent(final ResourceEventDTO resourceEventDTO) {
    }

    /**
     * Returns the block event counts for the given user during the given experiment.
     *
     * @param userDTO The user to search for.
     * @param experimentDTO The experiment to search for.
     * @return A list of event count DTOs with the block event counts.
     */
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
    public String[] getXMLForUser(final Integer userId, final Integer experimentId) {
        return null;
    }

    /**
     * Creates a {@link BlockEvent} with the given information of the {@link BlockEventDTO}.
     *
     * @param blockEventDTO The dto containing the information.
     * @return The new block event containing the information passed in the DTO.
     */
    private BlockEvent createBlockEvent(final BlockEventDTO blockEventDTO) {
        return null;
    }

    /**
     * Creates a {@link ResourceEvent} with the given information of the {@link ResourceEventDTO}.
     *
     * @param resourceEventDTO The dto containing the information.
     * @return The new block event containing the information passed in the DTO.
     */
    private ResourceEvent createResourceEvent(final ResourceEventDTO resourceEventDTO) {
        return null;
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
