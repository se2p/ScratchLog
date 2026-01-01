package de.uni_passau.fim.se2.scratchlog.testing_utils;

import de.uni_passau.fim.se2.scratchlog.persistence.entity.BlockEvent;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.ClickEvent;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Experiment;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.ResourceEvent;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.BlockEventRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ClickEventRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ResourceEventRepository;
import de.uni_passau.fim.se2.scratchlog.util.enums.BlockEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.BlockEventType;
import de.uni_passau.fim.se2.scratchlog.util.enums.ClickEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.ClickEventType;
import de.uni_passau.fim.se2.scratchlog.util.enums.ResourceEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.ResourceEventType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Utility methods for event domain entity creations for tests.
 *
 * <p>Implementation note: The public methods should save the created entities to the database already and return the
 * persisted objects.
 */
@Service
public class EventUtilService {

    private final BlockEventRepository blockEventRepository;

    private final ClickEventRepository clickEventRepository;

    private final ResourceEventRepository resourceEventRepository;

    public EventUtilService(
        final BlockEventRepository blockEventRepository,
        final ClickEventRepository clickEventRepository,
        final ResourceEventRepository resourceEventRepository
    ) {
        this.blockEventRepository = blockEventRepository;
        this.clickEventRepository = clickEventRepository;
        this.resourceEventRepository = resourceEventRepository;
    }

    /**
     * Generates a new block event.
     *
     * @param user The user that initiated the event. Must already exist in the database.
     * @param experiment The experiment the event was created in. Must already exist in the database.
     * @param type The event type.
     * @param specificType The specific event type.
     * @return The event as stored to the database.
     */
    public BlockEvent generateBlockEvent(
        final User user, final Experiment experiment, final BlockEventType type, final BlockEventSpecific specificType
    ) {
        final BlockEvent event = new BlockEvent(
            user, experiment, LocalDateTime.now(), type, specificType, "figure 1", "meta", "xml", null
        );
        return blockEventRepository.save(event);
    }

    /**
     * Generates a new click event.
     *
     * @param user The user that initiated the event. Must already exist in the database.
     * @param experiment The experiment the event was created in. Must already exist in the database.
     * @param type The event type.
     * @param specificType The specific event type.
     * @return The event as stored to the database.
     */
    public ClickEvent generateClickEvent(
        final User user, final Experiment experiment, final ClickEventType type, final ClickEventSpecific specificType
    ) {
        final ClickEvent event = new ClickEvent(
            user, experiment, LocalDateTime.now(), type, specificType, "meta"
        );
        return clickEventRepository.save(event);
    }

    /**
     * Generates a new resource event.
     *
     * @param user The user that initiated the event. Must already exist in the database.
     * @param experiment The experiment the event was created in. Must already exist in the database.
     * @param type The event type.
     * @param specificType The specific event type.
     * @return The event as stored to the database.
     */
    public ResourceEvent generateResourceEvent(
        final User user, final Experiment experiment, final ResourceEventType type, final ResourceEventSpecific specificType
    ) {
        final ResourceEvent event = new ResourceEvent(
            user, experiment, LocalDateTime.now(), type, specificType, "name", "hash", "type", 0
        );
        return resourceEventRepository.save(event);
    }
}
