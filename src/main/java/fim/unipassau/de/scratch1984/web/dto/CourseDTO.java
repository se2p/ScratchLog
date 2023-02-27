package fim.unipassau.de.scratch1984.web.dto;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A DTO representing an experiment.
 */
public class CourseDTO {

    /**
     * The unique ID of the course.
     */
    private Integer id;

    /**
     * The unique title of the course.
     */
    private String title;

    /**
     * The short description text of the course.
     */
    private String description;

    /**
     * The content text containing further information about the course.
     */
    private String content;

    /**
     * Boolean indicating whether the course is currently being conducted or not.
     */
    private boolean active;

    /**
     * The time at which the course information was last updated or an experiment or participant added.
     */
    private LocalDateTime lastChanged;

    /**
     * Default constructor for the experiment dto.
     */
    public CourseDTO() {
    }

    /**
     * Constructs a new course dto with the given attributes.
     *
     * @param id The course id.
     * @param title The course title.
     * @param description The course description.
     * @param content The course content text.
     * @param active Whether the course is currently being conducted or not.
     * @param lastChanged The last time at which the course was updated.
     */
    public CourseDTO(final Integer id, final String title, final String description, final String content,
                     final boolean active, final LocalDateTime lastChanged) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.content = content;
        this.active = active;
        this.lastChanged = lastChanged;
    }

    /**
     * Returns the ID of the course.
     *
     * @return The course ID.
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the ID of the course.
     *
     * @param id The course ID to be set.
     */
    public void setId(final Integer id) {
        this.id = id;
    }

    /**
     * Returns the title of the course.
     *
     * @return The course title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the course.
     *
     * @param name The course title to be set.
     */
    public void setTitle(final String name) {
        this.title = name;
    }

    /**
     * Returns the description of the course.
     *
     * @return The course description text.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the course.
     *
     * @param description The course description to be set.
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Returns the content of the course.
     *
     * @return The course content text.
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the content of the course.
     *
     * @param content The course content to be set.
     */
    public void setContent(final String content) {
        this.content = content;
    }

    /**
     * Returns whether the course is currently being conducted.
     *
     * @return The course status.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the status of the course.
     *
     * @param active The status.
     */
    public void setActive(final boolean active) {
        this.active = active;
    }

    /**
     * Returns the time at which the course has last been updated.
     *
     * @return The {@link LocalDateTime}.
     */
    public LocalDateTime getLastChanged() {
        return lastChanged;
    }

    /**
     * Sets the time of the last change to the course.
     *
     * @param lastChanged The {@link LocalDateTime} to be set.
     */
    public void setLastChanged(final LocalDateTime lastChanged) {
        this.lastChanged = lastChanged;
    }

    /**
     * Indicates whether some {@code other} course DTO is semantically equal to this course DTO.
     *
     * @param other The object to compare this course DTO to.
     * @return {@code true} iff {@code other} is a semantically equivalent course DTO.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        CourseDTO that = (CourseDTO) other;
        return Objects.equals(id, that.id);
    }

    /**
     * Calculates a hash code for this course DTO for hashing purposes, and to fulfill the {@link Object#equals(Object)}
     * contract.
     *
     * @return The hash code value of the course DTO.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Converts the course DTO into a human-readable string representation.
     *
     * @return A human-readable string representation of the course DTO.
     */
    @Override
    public String toString() {
        return "CourseDTO{"
                + "id=" + id
                + ", title='" + title + '\''
                + ", description='" + description + '\''
                + ", content='" + content + '\''
                + ", active=" + active
                + ", lastChanged=" + lastChanged
                + '}';
    }

}
