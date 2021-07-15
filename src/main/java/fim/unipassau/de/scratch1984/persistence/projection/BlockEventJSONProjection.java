package fim.unipassau.de.scratch1984.persistence.projection;

/**
 * Projection interface for the {@link fim.unipassau.de.scratch1984.persistence.entity.BlockEvent} class to return only
 * the block event id and the json code.
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

}
