package fim.unipassau.de.scratch1984.persistence.repository;

import fim.unipassau.de.scratch1984.persistence.entity.CodesData;
import fim.unipassau.de.scratch1984.persistence.entity.CodesDataId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * A repository providing functionality for retrieving the codes count data.
 */
public interface CodesDataRepository extends JpaRepository<CodesData, CodesDataId> {

    /**
     * Returns the {@link CodesData} for the given user during the given experiment, if any exist.
     *
     * @param user The user id to search for.
     * @param experiment The experiment id to search for.
     * @return The corresponding codes data, or {@code null}.
     */
    CodesData findByUserAndExperiment(Integer user, Integer experiment);

    /**
     * Returns a list of all {@link CodesData} for the given experiment, if any exist.
     *
     * @param experiment The experiment id to search for.
     * @return A list containing the data.
     */
    List<CodesData> findAllByExperiment(Integer experiment);

}
