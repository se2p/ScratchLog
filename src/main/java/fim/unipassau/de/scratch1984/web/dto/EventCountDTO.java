package fim.unipassau.de.scratch1984.web.dto;

import java.util.Objects;

/**
 * A DTO representing the number of times a user executed a specific event during an experiment.
 */
public class EventCountDTO {

    /**
     * The ID of the user to whom the data belongs.
     */
    private Integer user;

    /**
     * The ID of the experiment in which the counted events occurred.
     */
    private Integer experiment;

    /**
     * The number of times the event occurred.
     */
    private int count;

    /**
     * The event for which its occurrences have been counted.
     */
    private String event;

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

    /**
     * Indicates whether some {@code other} event count DTO is semantically equal to this event count DTO.
     *
     * @param other The object to compare this event count DTO to.
     * @return {@code true} iff {@code other} is a semantically equivalent event count DTO.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        EventCountDTO that = (EventCountDTO) other;
        return user.equals(that.user) && experiment.equals(that.experiment);
    }

    /**
     * Calculates a hash code for this event count DTO for hashing purposes, and to fulfill the
     * {@link Object#equals(Object)} contract.
     *
     * @return The hash code value of the event count DTO.
     */
    @Override
    public int hashCode() {
        return Objects.hash(user, experiment);
    }

    /**
     * Converts the event count DTO into a human-readable string representation.
     *
     * @return A human-readable string representation of the event count DTO.
     */
    @Override
    public String toString() {
        return "EventCountDTO{"
                + "user=" + user
                + ", experiment=" + experiment
                + ", count=" + count
                + ", event='" + event + '\''
                + '}';
    }

}
