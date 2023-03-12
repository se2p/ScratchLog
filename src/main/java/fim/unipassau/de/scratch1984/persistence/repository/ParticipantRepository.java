package fim.unipassau.de.scratch1984.persistence.repository;

import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.Participant;
import fim.unipassau.de.scratch1984.persistence.entity.ParticipantId;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * A repository providing functionality for retrieving participant information for an experiment.
 */
public interface ParticipantRepository extends JpaRepository<Participant, ParticipantId> {

    /**
     * Checks, whether a given user participated in the given experiment.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @return {@code true} iff the user participated in the experiment.
     */
    boolean existsByUserAndExperiment(User user, Experiment experiment);

    /**
     * Returns the participation data for the given user in the given experiment, if any exist.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @return The participation data or {@code null}, if no entry could be found.
     */
    Optional<Participant> findByUserAndExperiment(User user, Experiment experiment);

    /**
     * Returns a page of participants for the given experiment, if any entries exist.
     *
     * @param experiment The experiment to search for.
     * @param pageable The pageable to use.
     * @return The participant page.
     */
    Page<Participant> findAllByExperiment(Experiment experiment, Pageable pageable);

    /**
     * Returns a list of all participants for the given experiment, if any entries exist.
     *
     * @param experiment The experiment to search for.
     * @return The participant list.
     */
    List<Participant> findAllByExperiment(Experiment experiment);

    /**
     * Returns a list of all participants for the given experiment with the given end timestamp, if any entries exist.
     *
     * @param experiment The experiment to search for.
     * @param end The ending timestamp.
     * @return The participant list.
     */
    List<Participant> findAllByExperimentAndEnd(Experiment experiment, Timestamp end);

    /**
     * Returns a list of all participant relations for the given user, if any entries exist.
     *
     * @param user The user to search for.
     * @return The participation list.
     */
    List<Participant> findAllByUser(User user);

    /**
     * Returns a list of all participant relations for the given user where the experiment has not been finished, if any
     * entries exist.
     *
     * @param user The user to search for.
     * @return The participation list.
     */
    List<Participant> findAllByEndIsNullAndUser(User user);

}
