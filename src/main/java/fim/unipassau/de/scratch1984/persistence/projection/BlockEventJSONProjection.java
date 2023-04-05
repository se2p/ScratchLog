package fim.unipassau.de.scratch1984.persistence.projection;

import java.time.LocalDateTime;

/**
 * Projection interface for the {@link fim.unipassau.de.scratch1984.persistence.entity.BlockEvent} class to return only
 * the block event id, timestamp and the json code.
 */
public interface BlockEventJSONProjection {

    /**
     * Returns the unique id of the block event.
     *
     * @return The block event id.
     */
    Integer getId();

    /**
     * Returns the json code of the block event.
     *
     * @return The json code.
     */
    String getCode();

    /**
     * Returns the timestamp of the block event.
     *
     * @return The timestamp.
     */
    LocalDateTime getDate();

    /**
     * Returns the concrete event that occurred.
     *
     * @return The respective event.
     */
    String getEvent();

}
