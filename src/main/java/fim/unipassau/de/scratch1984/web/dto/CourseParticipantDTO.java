package fim.unipassau.de.scratch1984.web.dto;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * An DTO representing the participation of a user in a course.
 */
public class CourseParticipantDTO {

    /**
     * The id of the user participating in this course.
     */
    private Integer user;

    /**
     * The id of the course in which the user is participating.
     */
    private Integer course;

    /**
     * The {@link LocalDateTime} at which the participant was added to the course.
     */
    private LocalDateTime added;

    /**
     * Default constructor for the course participant dto.
     */
    public CourseParticipantDTO() {
    }

    /**
     * Constructs a new course participant dto with the given attributes.
     *
     * @param user The user id.
     * @param course The course id.
     * @param added The time at which the user was added to the course.
     */
    public CourseParticipantDTO(final Integer user, final Integer course, final LocalDateTime added) {
        this.user = user;
        this.course = course;
        this.added = added;
    }

    /**
     * Returns the ID of the course.
     *
     * @return The course ID.
     */
    public Integer getCourse() {
        return course;
    }

    /**
     * Sets the ID of the course.
     *
     * @param course The course ID to be set.
     */
    public void setCourse(final Integer course) {
        this.course = course;
    }

    /**
     * Returns the ID of the user.
     *
     * @return The user's ID.
     */
    public Integer getUser() {
        return user;
    }

    /**
     * Sets the ID of the user.
     *
     * @param user The user ID to be set.
     */
    public void setUser(final Integer user) {
        this.user = user;
    }

    /**
     * Returns the time at which the user was added to the course.
     *
     * @return The {@link LocalDateTime}.
     */
    public LocalDateTime getAdded() {
        return added;
    }

    /**
     * Sets the time at which the user was added to the course.
     *
     * @param added The {@link LocalDateTime} to be set.
     */
    public void setAdded(final LocalDateTime added) {
        this.added = added;
    }

    /**
     * Indicates whether some {@code other} course participant DTO is semantically equal to this course participant DTO.
     *
     * @param other The object to compare this course participant DTO to.
     * @return {@code true} iff {@code other} is a semantically equivalent course participant DTO.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        CourseParticipantDTO that = (CourseParticipantDTO) other;
        return Objects.equals(user, that.user) && Objects.equals(course, that.course);
    }

    /**
     * Calculates a hash code for this course participant DTO for hashing purposes, and to fulfill the
     * {@link Object#equals(Object)} contract.
     *
     * @return The hash code value of the course participant DTO.
     */
    @Override
    public int hashCode() {
        return Objects.hash(user, course);
    }

    /**
     * Converts the course participant DTO into a human-readable string representation.
     *
     * @return A human-readable string representation of the course participant DTO.
     */
    @Override
    public String toString() {
        return "CourseParticipantDTO{"
                + "user=" + user
                + ", course=" + course
                + ", added=" + added
                + '}';
    }

}
