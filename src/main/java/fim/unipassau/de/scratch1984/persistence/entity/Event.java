package fim.unipassau.de.scratch1984.persistence.entity;

import java.time.LocalDateTime;

/**
 * An interface for event entities logged by the instrumented Scratch IDE.
 */
public interface Event {

    /**
     * Returns the ID of the event.
     *
     * @return The event ID.
     */
    Integer getId();

    /**
     * Sets the ID of the event.
     *
     * @param id The event ID to be set.
     */
    void setId(Integer id);

    /**
     * Returns the user of the event.
     *
     * @return The respective user.
     */
    User getUser();

    /**
     * Sets the user of the event.
     *
     * @param user The event user to be set.
     */
    void setUser(User user);

    /**
     * Returns the experiment of the event.
     *
     * @return The respective experiment.
     */
    Experiment getExperiment();

    /**
     * Sets the experiment of the event.
     *
     * @param experiment The event experiment to be set.
     */
    void setExperiment(Experiment experiment);

    /**
     * Returns the datetime of the event.
     *
     * @return The respective timestamp.
     */
    LocalDateTime getDate();

    /**
     * Sets the datetime of the event.
     *
     * @param date The event timestamp to be set.
     */
    void setDate(LocalDateTime date);

}
