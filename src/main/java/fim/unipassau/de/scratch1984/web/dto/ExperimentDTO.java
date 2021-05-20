package fim.unipassau.de.scratch1984.web.dto;

import java.util.Objects;

/**
 * A DTO representing an experiment.
 */
public class ExperimentDTO {

    /**
     * The unique ID of the experiment.
     */
    private Integer id;

    /**
     * The unique title of the experiment.
     */
    private String title;

    /**
     * The short description text of the experiment.
     */
    private String description;

    /**
     * The information text of the experiment.
     */
    private String info;

    /**
     * Boolean indicating whether the experiment is running or not.
     */
    private boolean active;

    /**
     * Returns the ID of the experiment.
     *
     * @return The experiment ID.
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the ID of the experiment.
     *
     * @param id The experiment ID to be set.
     */
    public void setId(final Integer id) {
        this.id = id;
    }

    /**
     * Returns the title of the experiment.
     *
     * @return The experiment title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the experiment.
     *
     * @param title The title to be set.
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * Returns the description for the experiment.
     *
     * @return The description text.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description for the experiment.
     *
     * @param description The description to be set.
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Returns the information text for the experiment.
     *
     * @return The information text.
     */
    public String getInfo() {
        return info;
    }

    /**
     * Sets the information text for the experiment.
     *
     * @param info The text to be set.
     */
    public void setInfo(final String info) {
        this.info = info;
    }

    /**
     * Returns whether the experiment is currently running.
     *
     * @return The experiment status.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the running status the experiment.
     *
     * @param active The status.
     */
    public void setActive(final boolean active) {
        this.active = active;
    }

    /**
     * Indicates whether some {@code other} experiment DTO is semantically equal to this experiment DTO.
     *
     * @param other The object to compare this experiment DTO to.
     * @return {@code true} iff {@code other} is a semantically equivalent experiment DTO.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        ExperimentDTO that = (ExperimentDTO) other;
        return id.equals(that.id);
    }

    /**
     * Calculates a hash code for this experiment DTO for hashing purposes, and to fulfill the
     * {@link Object#equals(Object)} contract.
     *
     * @return The hash code value of the experiment DTO.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Converts the experiment DTO into a human-readable string representation.
     *
     * @return A human-readable string representation of the experiment DTO.
     */
    @Override
    public String toString() {
        return "ExperimentDTO{"
                + "id=" + id
                + ", title='" + title + '\''
                + ", description='" + description + '\''
                + ", info='" + info + '\''
                + '}';
    }

}
