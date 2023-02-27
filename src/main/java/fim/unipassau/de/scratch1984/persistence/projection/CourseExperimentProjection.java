package fim.unipassau.de.scratch1984.persistence.projection;

/**
 * Projection interface for the {@link fim.unipassau.de.scratch1984.persistence.entity.CourseExperiment} class to return
 * only the experiment id, title, description, and status.
 */
public interface CourseExperimentProjection {

    /**
     * Returns an {@link ExperimentTableProjection} of the experiment.
     *
     * @return The projection.
     */
    ExperimentTableProjection getExperiment();

}
