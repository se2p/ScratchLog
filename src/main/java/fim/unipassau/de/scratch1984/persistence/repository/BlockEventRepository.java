package fim.unipassau.de.scratch1984.persistence.repository;

import fim.unipassau.de.scratch1984.persistence.entity.BlockEvent;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.projection.BlockEventJSONProjection;
import fim.unipassau.de.scratch1984.persistence.projection.BlockEventProjection;
import fim.unipassau.de.scratch1984.persistence.projection.BlockEventXMLProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * A repository providing functionality for retrieving the block event data.
 */
public interface BlockEventRepository extends JpaRepository<BlockEvent, Integer> {

    /**
     * Returns all xml data with the corresponding id of the block event saved for the given user in the given
     * experiment, if any exist.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @return The xml data and corresponding ids or an empty list, if no entry could be found.
     */
    List<BlockEventXMLProjection> findAllByXmlIsNotNullAndUserAndExperiment(User user, Experiment experiment);

    /**
     * Returns all json data with the corresponding id of the block event saved for the given user in the given
     * experiment, if any exist.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @return The json data and corresponding ids or an empty list, if no entry could be found.
     */
    List<BlockEventJSONProjection> findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(User user,
                                                                                            Experiment experiment);

    /**
     * Returns a page of {@link BlockEventProjection}s for the given user and experiment corresponding to the parameters
     * set in the pageable.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @param pageable The pageable to use.
     * @return An block event projection page.
     */
    Page<BlockEventProjection> findAllByUserAndExperimentAndXmlIsNotNull(User user, Experiment experiment,
                                                                         Pageable pageable);

    /**
     * Returns all {@link BlockEvent}s that occurred during the given experiment.
     *
     * @param experiment The experiment to search for.
     * @return A {@link List} of all block events.
     */
    List<BlockEvent> findAllByExperiment(Experiment experiment);

}
