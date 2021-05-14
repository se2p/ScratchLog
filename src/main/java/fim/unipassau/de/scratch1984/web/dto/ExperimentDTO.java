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
     * The unique name of the experiment.
     */
    private String name;

    /**
     * The short description text of the experiment.
     */
    private String description;

    /**
     * The information text of the experiment.
     */
    private String info;

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
     * Returns the name of the experiment.
     *
     * @return The experiment name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the experiment.
     *
     * @param name The name to be set.
     */
    public void setName(final String name) {
        this.name = name;
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
                + ", name='" + name + '\''
                + ", description='" + description + '\''
                + ", info='" + info + '\''
                + '}';
    }

}