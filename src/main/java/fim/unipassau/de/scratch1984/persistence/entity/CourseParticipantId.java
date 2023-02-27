package fim.unipassau.de.scratch1984.persistence.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Utility class for the composite key of {@link CourseParticipant} as per JPA specification.
 */
public class CourseParticipantId implements Serializable {

    /**
     * The id of the participating user.
     */
    private int user;

    /**
     * The id of the course in which the user participates.
     */
    private int course;

    /**
     * Default constructor for the ID.
     */
    public CourseParticipantId() {
    }

    /**
     * Constructs a new course participant ID with the given user and course IDs.
     *
     * @param user The participating user's ID.
     * @param course The respective course ID.
     */
    public CourseParticipantId(final int user, final int course) {
        this.user = user;
        this.course = course;
    }

    /**
     * Indicates whether some {@code other} course participant id is semantically equal to this id.
     *
     * @param other The object to compare this id to.
     * @return {@code true} iff {@code other} is a semantically equivalent course participant id.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        CourseParticipantId that = (CourseParticipantId) other;
        return user == that.user && course == that.course;
    }

    /**
     * Calculates a hash code for the course participant id for hashing purposes, and to fulfill the
     * {@link Object#equals(Object)} contract.
     *
     * @return The hash code value of the course participant id.
     */
    @Override
    public int hashCode() {
        return Objects.hash(user, course);
    }

}
