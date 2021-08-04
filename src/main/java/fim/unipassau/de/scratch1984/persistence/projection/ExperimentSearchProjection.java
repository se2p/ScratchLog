package fim.unipassau.de.scratch1984.persistence.projection;

/**
 * Projection interface for the {@link fim.unipassau.de.scratch1984.persistence.entity.Experiment} class to return only
 * the id, title, and description.
 */
public interface ExperimentSearchProjection {

    /**
     * Returns the unique id of the experiment.
     *
     * @return The experiment id.
     */
    Integer getId();

    /**
     * Returns the unique title of the experiment.
     *
     * @return The title.
     */
    String getTitle();

    /**
     * Returns the description of the experiment.
     *
     * @return The description.
     */
    String getDescription();

}
