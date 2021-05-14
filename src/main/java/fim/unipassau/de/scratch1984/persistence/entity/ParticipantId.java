package fim.unipassau.de.scratch1984.persistence.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Utility class for the composite key of {@link Participant} as per JPA specification.
 */
public class ParticipantId implements Serializable {

    /**
     * The participating {@link User}.
     */
    private User user;

    /**
     * The {@link Experiment} in which the user participated.
     */
    private Experiment experiment;

    /**
     * Default constructor for the ID.
     */
    public ParticipantId() {
    }

    /**
     * Constructs a new participant ID with the given user and experiment.
     *
     * @param user The participating user.
     * @param experiment The respective experiment.
     */
    public ParticipantId(final User user, final Experiment experiment) {
        this.user = user;
        this.experiment = experiment;
    }

    /**
     * Indicates whether some {@code other} participant id is semantically equal to this id.
     *
     * @param other The object to compare this id to.
     * @return {@code true} iff {@code other} is a semantically equivalent participant id.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        ParticipantId that = (ParticipantId) other;
        return user.equals(that.user) && experiment.equals(that.experiment);
    }

    /**
     * Calculates a hash code for the participant id for hashing purposes, and to fulfill the
     * {@link Object#equals(Object)} contract.
     *
     * @return The hash code value of the participant id.
     */
    @Override
    public int hashCode() {
        return Objects.hash(user, experiment);
    }

}