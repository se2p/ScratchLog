package fim.unipassau.de.scratch1984.persistence.projection;

/**
 * Projection interface for the {@link fim.unipassau.de.scratch1984.persistence.entity.BlockEvent} class to return only
 * the block event id, the xml and the json code.
 */
public interface BlockEventProjection {

    /**
     * Returns the unique id of the block event.
     *
     * @return The block event id.
     */
    Integer getId();

    /**
     * Returns the xml of the block event.
     *
     * @return The xml.
     */
    String getXml();

    /**
     * Returns the json code of the block event.
     *
     * @return The json code.
     */
    String getCode();

}
