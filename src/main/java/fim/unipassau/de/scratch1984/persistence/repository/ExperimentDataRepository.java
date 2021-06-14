package fim.unipassau.de.scratch1984.persistence.repository;

import fim.unipassau.de.scratch1984.persistence.entity.ExperimentData;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * A repository providing functionality for retrieving experiment data for an experiment.
 */
public interface ExperimentDataRepository extends JpaRepository<ExperimentData, Integer> {

    /**
     * Returns the experiment data for the experiment with the given id, if one exists.
     *
     * @param experiment The id to search for.
     * @return The experiment data or {@code null}, if no entry could be found.
     */
    ExperimentData findByExperiment(int experiment);

}
