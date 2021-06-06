package fim.unipassau.de.scratch1984.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.sql.Timestamp;

/**
 * An entity representing a participation in an experiment.
 */
@Entity
@IdClass(ParticipantId.class)
public class Participant {

    /**
     * The {@link User} representing the participant.
     */
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * The {@link Experiment} in which the user participated.
     */
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id")
    private Experiment experiment;

    /**
     * The timestamp at which the user started the experiment.
     */
    @Column(name = "start")
    private Timestamp start;

    /**
     * The timestamp at which the user finished the experiment.
     */
    @Column(name = "finish")
    private Timestamp end;

    /**
     * Default constructor for the participant entity.
     */
    public Participant() {
    }

    /**
     * Constructs a new participant with the given attributes.
     *
     * @param user The participating user.
     * @param experiment The experiment in which the user is participating.
     * @param start The timestamp at which the user started the experiment.
     * @param end The timestamp at which the user finished the experiment.
     */
    public Participant(final User user, final Experiment experiment, final Timestamp start, final Timestamp end) {
        this.user = user;
        this.experiment = experiment;
        this.start = start;
        this.end = end;
    }

    /**
     * Returns the participating user.
     *
     * @return The respective user.
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the participating user.
     *
     * @param user The user to be set.
     */
    public void setUser(final User user) {
        this.user = user;
    }

    /**
     * Returns the experiment in which the user participated.
     *
     * @return The respective experiment.
     */
    public Experiment getExperiment() {
        return experiment;
    }

    /**
     * Sets the experiment in which the user participated.
     *
     * @param experiment The experiment to be set.
     */
    public void setExperiment(final Experiment experiment) {
        this.experiment = experiment;
    }

    /**
     * Returns the timestamp at which the user started the experiment.
     *
     * @return The starting time.
     */
    public Timestamp getStart() {
        return start;
    }

    /**
     * Sets the time at which the user started the experiment.
     *
     * @param start The starting time to be set.
     */
    public void setStart(final Timestamp start) {
        this.start = start;
    }

    /**
     * Returns the timestamp at which the user finished the experiment.
     *
     * @return The finishing time.
     */
    public Timestamp getEnd() {
        return end;
    }

    /**
     * Sets the time at which the user finished the experiment.
     *
     * @param end The finishing time to be set.
     */
    public void setEnd(final Timestamp end) {
        this.end = end;
    }

}
