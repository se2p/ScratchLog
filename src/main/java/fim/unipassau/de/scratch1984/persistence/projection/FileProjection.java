package fim.unipassau.de.scratch1984.persistence.projection;

/**
 * Projection interface for the {@link fim.unipassau.de.scratch1984.persistence.entity.File} class to return only the
 * file id and name.
 */
public interface FileProjection {

    /**
     * Returns the unique id of the file.
     *
     * @return The file id.
     */
    Integer getId();

    /**
     * Returns the name of the file.
     *
     * @return The file name.
     */
    String getName();

}
