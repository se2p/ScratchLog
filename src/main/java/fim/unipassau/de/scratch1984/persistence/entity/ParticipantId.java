package fim.unipassau.de.scratch1984.persistence.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Utility class for the composite key of {@link Participant} as per JPA specification.
 */
public class ParticipantId implements Serializable {

    /**
     * The id of the participating user.
     */
    private int user;

    /**
     * The experiment id in which the user participated.
     */
    private int experiment;

    /**
     * Default constructor for the ID.
     */
    public ParticipantId() {
    }

    /**
     * Constructs a new participant ID with the given user and experiment IDs.
     *
     * @param user The participating user's ID.
     * @param experiment The respective experiment ID.
     */
    public ParticipantId(final int user, final int experiment) {
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
        return user == that.user && experiment == that.experiment;
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
