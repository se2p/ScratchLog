package fim.unipassau.de.scratch1984.persistence.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Utility class for the composite key of {@link CodesData} as per JPA specification.
 */
public class CodesDataId implements Serializable {

    /**
     * The user ID.
     */
    private Integer user;

    /**
     * The experiment ID.
     */
    private Integer experiment;

    /**
     * Default constructor for the ID.
     */
    public CodesDataId() {
    }

    /**
     * Constructs a new codes data ID with the given user and experiment IDs.
     *
     * @param user The user ID.
     * @param experiment The experiment ID.
     */
    public CodesDataId(final Integer user, final Integer experiment) {
        this.user = user;
        this.experiment = experiment;
    }

    /**
     * Indicates whether some {@code other} codes data id is semantically equal to this id.
     *
     * @param other The object to compare this id to.
     * @return {@code true} iff {@code other} is a semantically equivalent codes data id.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        CodesDataId that = (CodesDataId) other;
        return user.equals(that.user) && experiment.equals(that.experiment);
    }

    /**
     * Calculates a hash code for the codes data id for hashing purposes, and to fulfill the
     * {@link Object#equals(Object)} contract.
     *
     * @return The hash code value of the codes data id.
     */
    @Override
    public int hashCode() {
        return Objects.hash(user, experiment);
    }

}