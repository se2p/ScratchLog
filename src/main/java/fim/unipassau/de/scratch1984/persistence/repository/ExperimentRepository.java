package fim.unipassau.de.scratch1984.persistence.repository;

import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * A repository providing functionality for retrieving experiment data.
 */
public interface ExperimentRepository extends JpaRepository<Experiment, Integer> {

    /**
     * Checks, whether an experiment with the given name already exists in the database.
     *
     * @param name The name to search for.
     * @return {@code true} iff the name already exists.
     */
    boolean existsByName(String name);

    /**
     * Returns the experiment identified by the given name, if one exists.
     *
     * @param name The name to search for.
     * @return The experiment data or {@code null}, if no entry could be found.
     */
    Experiment findByName(String name);

}
