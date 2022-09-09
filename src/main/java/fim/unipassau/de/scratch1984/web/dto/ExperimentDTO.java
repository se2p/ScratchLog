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
     * The text shown after the user has finished the experiment.
     */
    private String postscript;

    /**
     * Boolean indicating whether the experiment is running or not.
     */
    private boolean active;

    /**
     * The URL of the instrumented Scratch-GUI instance to be used for this experiment.
     */
    private String guiURL;

    /**
     * Default constructor for the experiment dto.
     */
    public ExperimentDTO() {
    }

    /**
     * Constructs a new experiment dto with the given attributes.
     *
     * @param id The experiment id.
     * @param title The experiment title.
     * @param description The experiment description.
     * @param info The experiment information text.
     * @param postscript The postscript text.
     * @param active Whether the experiment is currently running or not.
     * @param guiURL The URL of the instrumented Scratch-GUI this experiment uses.
     */
    public ExperimentDTO(final Integer id, final String title, final String description, final String info,
                         final String postscript, final boolean active, final String guiURL) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.info = info;
        this.postscript = postscript;
        this.active = active;
        this.guiURL = guiURL;
    }

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
     * Returns the postscript of the experiment.
     *
     * @return The postscript text.
     */
    public String getPostscript() {
        return postscript;
    }

    /**
     * Sets the text shown after the user has finished the experiment.
     *
     * @param postscript The postscript to be set.
     */
    public void setPostscript(final String postscript) {
        this.postscript = postscript;
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
     * Sets the running status of the experiment.
     *
     * @param active The status.
     */
    public void setActive(final boolean active) {
        this.active = active;
    }

    /**
     * Returns whether the GUI-URL of the experiment.
     *
     * @return The GUI-URL.
     */
    public String getGuiURL() {
        return guiURL;
    }

    /**
     * Sets the GUI_URL of the experiment.
     *
     * @param guiURL The GUI_URL.
     */
    public void setGuiURL(final String guiURL) {
        this.guiURL = guiURL;
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
        return Objects.equals(id, that.id);
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
                + ", postscript='" + postscript + '\''
                + ", info='" + info + '\''
                + ", active='" + active + '\''
                + ", url='" + guiURL + '\''
                + '}';
    }

}
