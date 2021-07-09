package fim.unipassau.de.scratch1984.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

/**
 * An entity representing the number of times a user executed a specific event during an experiment.
 */
@Entity
@IdClass(EventCountId.class)
public class EventCount {

    /**
     * The ID of the user to whom the data belongs.
     */
    @Id
    @Column(name = "user")
    private Integer user;

    /**
     * The ID of the experiment in which the counted events occurred.
     */
    @Id
    @Column(name = "experiment")
    private Integer experiment;

    /**
     * The number of times the event occurred.
     */
    @Column(name = "count")
    private int count;

    /**
     * The event for which its occurrences have been counted.
     */
    @Id
    @Column(name = "event")
    private String event;

    /**
     * Default constructor for the event count entity.
     */
    public EventCount() {
    }

    /**
     * Constructs a new event count entity with the given attributes.
     *
     * @param user The id of the user who caused the event.
     * @param experiment The id of the experiment during which the event occurred.
     * @param count The number of times the given event occurred.
     * @param event The specific event.
     */
    public EventCount(final Integer user, final Integer experiment, final int count, final String event) {
        this.user = user;
        this.experiment = experiment;
        this.count = count;
        this.event = event;
    }

    /**
     * Returns the ID of the user to whom this data belongs.
     *
     * @return The user ID.
     */
    public Integer getUser() {
        return user;
    }

    /**
     * Sets the user ID.
     *
     * @param user The user ID to be set.
     */
    public void setUser(final Integer user) {
        this.user = user;
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
     * Returns the calculated count value.
     *
     * @return The counted occurrences.
     */
    public int getCount() {
        return count;
    }

    /**
     * Sets the count value.
     *
     * @param count The value to be set.
     */
    public void setCount(final int count) {
        this.count = count;
    }

    /**
     * Returns the event for which the occurrences have been counted.
     *
     * @return The event.
     */
    public String getEvent() {
        return event;
    }

    /**
     * Sets the event.
     *
     * @param event The event to be set.
     */
    public void setEvent(final String event) {
        this.event = event;
    }

}
