package fim.unipassau.de.scratch1984.persistence.repository;

import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.ResourceEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * A repository providing functionality for retrieving the resource event data.
 */
public interface ResourceEventRepository extends JpaRepository<ResourceEvent, Integer> {

    /**
     * Returns all {@link ResourceEvent}s that occurred during the given experiment.
     *
     * @param experiment The experiment to search for.
     * @return A {@link List} of all resource events.
     */
    List<ResourceEvent> findAllByExperiment(Experiment experiment);

}
