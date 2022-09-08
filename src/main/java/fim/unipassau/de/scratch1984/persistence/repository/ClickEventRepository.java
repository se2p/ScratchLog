package fim.unipassau.de.scratch1984.persistence.repository;

import fim.unipassau.de.scratch1984.persistence.entity.ClickEvent;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * A repository providing functionality for retrieving the click event data.
 */
public interface ClickEventRepository extends JpaRepository<ClickEvent, Integer> {

    /**
     * Returns all {@link ClickEvent}s that occurred during the given experiment.
     *
     * @param experiment The experiment to search for.
     * @return A {@link List} of all click events.
     */
    List<ClickEvent> findAllByExperiment(Experiment experiment);

}
