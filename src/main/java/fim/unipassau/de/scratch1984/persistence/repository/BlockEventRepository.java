package fim.unipassau.de.scratch1984.persistence.repository;

import fim.unipassau.de.scratch1984.persistence.entity.BlockEvent;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.projection.BlockEventJSONProjection;
import fim.unipassau.de.scratch1984.persistence.projection.BlockEventXMLProjection;
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
    List<BlockEventJSONProjection> findAllByCodeIsNotNullAndUserAndExperiment(User user, Experiment experiment);

}
