package fim.unipassau.de.scratch1984.persistence.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Utility class for the composite key of {@link EventCount} as per JPA specification.
 */
public class EventCountId implements Serializable {

    /**
     * The user ID.
     */
    private Integer user;

    /**
     * The experiment ID.
     */
    private Integer experiment;

    /**
     * The event for which its occurrences have been counted.
     */
    private String event;

    /**
     * Default constructor for the ID.
     */
    public EventCountId() {
    }

    /**
     * Constructs a new event count ID with the given user and experiment IDs as well as the given event.
     *
     * @param user The user ID.
     * @param experiment The experiment ID.
     * @param event The event.
     */
    public EventCountId(final Integer user, final Integer experiment, final String event) {
        this.user = user;
        this.experiment = experiment;
        this.event = event;
    }

    /**
     * Indicates whether some {@code other} event count id is semantically equal to this id.
     *
     * @param other The object to compare this id to.
     * @return {@code true} iff {@code other} is a semantically equivalent event count id.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        EventCountId that = (EventCountId) other;
        return user.equals(that.user) && experiment.equals(that.experiment) && event.equals(that.event);
    }

    /**
     * Calculates a hash code for the event count id for hashing purposes, and to fulfill the
     * {@link Object#equals(Object)} contract.
     *
     * @return The hash code value of the event count id.
     */
    @Override
    public int hashCode() {
        return Objects.hash(user, experiment, event);
    }

}
