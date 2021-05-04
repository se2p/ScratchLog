package fim.unipassau.de.scratch1984.persistence.repository;

import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.Participant;
import fim.unipassau.de.scratch1984.persistence.entity.ParticipantId;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

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
    Participant findByUserAndExperiment(User user, Experiment experiment);

}
