package fim.unipassau.de.scratch1984.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * An entity representing the number users participating in an experiment, those who started and those who finished the
 * experiment.
 */
@Entity
@Table(name = "experiment_data")
public class ExperimentData {

    /**
     * The ID of the experiment.
     */
    @Id
    @Column(name = "experiment")
    private Integer experiment;

    /**
     * The number of participants for the experiment.
     */
    @Column(name = "participants")
    private int participants;

    /**
     * The number of participants who started the experiment.
     */
    @Column(name = "started")
    private int started;

    /**
     * The number of participants who finished the experiment.
     */
    @Column(name = "finished")
    private int finished;

    /**
     * Default constructor for the experiment data entity.
     */
    public ExperimentData() {
    }

    /**
     * Constructs a new experiment data with the given attributes.
     *
     * @param experiment The experiment id.
     * @param participants The number of participants in the experiment.
     * @param started The number of participants who have already started the experiment.
     * @param finished The number of participants who have already finished the experiment.
     */
    public ExperimentData(final Integer experiment, final int participants, final int started, final int finished) {
        this.experiment = experiment;
        this.participants = participants;
        this.started = started;
        this.finished = finished;
    }

    /**
     * Returns the ID of the experiment to which this data belongs.
     *
     * @return The experiment ID.
     */
    public Integer getExperiment() {
        return experiment;
    }

    /**
     * Sets the experiment ID.
     *
     * @param experiment The experiment ID to be set.
     */
    public void setExperiment(final Integer experiment) {
        this.experiment = experiment;
    }

    /**
     * Returns the number of participants for the experiment.
     *
     * @return The number of participants.
     */
    public int getParticipants() {
        return participants;
    }

    /**
     * Sets the number of participants for the experiment.
     *
     * @param participants The respective number.
     */
    public void setParticipants(final int participants) {
        this.participants = participants;
    }

    /**
     * Returns the number of participants who started the experiment.
     *
     * @return The respective number.
     */
    public int getStarted() {
        return started;
    }

    /**
     * Sets the number of participants who started the experiment.
     *
     * @param started The respective number.
     */
    public void setStarted(final int started) {
        this.started = started;
    }

    /**
     * Returns the number of participants who finished the experiment.
     *
     * @return The respective number.
     */
    public int getFinished() {
        return finished;
    }

    /**
     * Sets the number of participants who finished the experiment.
     *
     * @param finished The respective number.
     */
    public void setFinished(final int finished) {
        this.finished = finished;
    }

}
