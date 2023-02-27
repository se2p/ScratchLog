package fim.unipassau.de.scratch1984.persistence.projection;

/**
 * Projection interface for the {@link fim.unipassau.de.scratch1984.persistence.entity.Experiment} class to return only
 * the id and the sb3 project.
 */
public interface ExperimentProjection {

    /**
     * Returns the unique id of the experiment.
     *
     * @return The experiment id.
     */
    Integer getId();

    /**
     * Returns whether the experiment is currently running.
     *
     * @return The experiment status.
     */
    boolean isActive();

    /**
     * Returns the current sb3 project.
     *
     * @return The sb3 project.
     */
    byte[] getProject();

}
