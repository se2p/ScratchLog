package fim.unipassau.de.scratch1984.persistence.repository;

import fim.unipassau.de.scratch1984.persistence.entity.BlockEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * A repository providing functionality for retrieving the block event data.
 */
public interface BlockEventRepository extends JpaRepository<BlockEvent, Integer> {

    /**
     * Returns all xml data saved for the given user in the given experiment, if any exist.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @return The xml data or {@code null}, if no entry could be found.
     */
    @Query(nativeQuery = true, value = "SELECT b.xml FROM block_event AS b WHERE b.user_id = :uId AND b.experiment_id "
            + "= :expId ORDER BY b.date ASC;")
    String[] getXMlForUserAndExperiment(@Param("uId") Integer user, @Param("expId") Integer experiment);

    /**
     * Returns all json data saved for the given user in the given experiment, if any exist.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @return The json data or {@code null}, if no entry could be found.
     */
    @Query(nativeQuery = true, value = "SELECT b.json FROM block_event AS b WHERE b.user_id = :uId AND b.experiment_id"
            + " = :expId ORDER BY b.date ASC;")
    String[] getJsonForUserAndExperiment(@Param("uId") Integer user, @Param("expId") Integer experiment);

}
