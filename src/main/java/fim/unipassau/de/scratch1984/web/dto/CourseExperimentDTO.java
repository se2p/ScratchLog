package fim.unipassau.de.scratch1984.web.dto;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * An DTO representing an experiment conducted as part of a course.
 */
public class CourseExperimentDTO {

    /**
     * The id of the course in which the experiment is conducted.
     */
    private Integer course;

    /**
     * The id of the experiment which is part of the course.
     */
    private Integer experiment;

    /**
     * The {@link LocalDateTime} at which the experiment was added to the course.
     */
    private LocalDateTime added;

    /**
     * Default constructor for the course experiment dto.
     */
    public CourseExperimentDTO() {
    }

    /**
     * Constructs a new course experiment dto with the given attributes.
     *
     * @param course The course id.
     * @param experiment The id of the experiment.
     * @param added The time at which the experiment was added to the course.
     */
    public CourseExperimentDTO(final int course, final int experiment, final LocalDateTime added) {
        this.course = course;
        this.experiment = experiment;
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
     * Returns the ID of the experiment.
     *
     * @return The experiment ID.
     */
    public Integer getExperiment() {
        return experiment;
    }

    /**
     * Sets the ID of the experiment.
     *
     * @param experiment The experiment ID to be set.
     */
    public void setExperiment(final Integer experiment) {
        this.experiment = experiment;
    }

    /**
     * Returns the time at which the experiment was added to the course.
     *
     * @return The {@link LocalDateTime}.
     */
    public LocalDateTime getAdded() {
        return added;
    }

    /**
     * Sets the time at which the experiment was added to the course.
     *
     * @param added The {@link LocalDateTime} to be set.
     */
    public void setAdded(final LocalDateTime added) {
        this.added = added;
    }

    /**
     * Indicates whether some {@code other} course experiment DTO is semantically equal to this course experiment DTO.
     *
     * @param other The object to compare this course experiment DTO to.
     * @return {@code true} iff {@code other} is a semantically equivalent course experiment DTO.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        CourseExperimentDTO that = (CourseExperimentDTO) other;
        return Objects.equals(course, that.course) && Objects.equals(experiment, that.experiment);
    }

    /**
     * Calculates a hash code for this course experiment DTO for hashing purposes, and to fulfill the
     * {@link Object#equals(Object)} contract.
     *
     * @return The hash code value of the course experiment DTO.
     */
    @Override
    public int hashCode() {
        return Objects.hash(course, experiment);
    }

    /**
     * Converts the course experiment DTO into a human-readable string representation.
     *
     * @return A human-readable string representation of the course experiment DTO.
     */
    @Override
    public String toString() {
        return "CourseExperimentDTO{"
                + "course=" + course
                + ", experiment=" + experiment
                + ", added=" + added
                + '}';
    }

}
