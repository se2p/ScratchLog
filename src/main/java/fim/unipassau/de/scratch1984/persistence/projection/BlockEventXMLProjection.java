package fim.unipassau.de.scratch1984.persistence.projection;

/**
 * Projection interface for the {@link fim.unipassau.de.scratch1984.persistence.entity.BlockEvent} class to return only
 * the block event id and the xml code.
 */
public interface BlockEventXMLProjection {

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

}
