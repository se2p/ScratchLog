package fim.unipassau.de.scratch1984.web.dto;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

/**
 * A DTO representing a file uploaded during an experiment.
 */
public class FileDTO implements EventDTO {

    /**
     * The unique ID of the file.
     */
    private Integer id;

    /**
     * The ID of the user who uploaded the file.
     */
    private Integer user;

    /**
     * The ID of the experiment during which the file was uploaded.
     */
    private Integer experiment;

    /**
     * The local date time at which the file was uploaded in the Scratch GUI.
     */
    private LocalDateTime date;

    /**
     * The file name.
     */
    private String name;

    /**
     * The filetype.
     */
    private String filetype;

    /**
     * The file content itself.
     */
    private byte[] content;

    /**
     * Default constructor for the file dto.
     */
    public FileDTO() {
    }

    /**
     * Constructs a new file dto with the given attributes.
     *
     * @param user The id of the user who uploaded the file.
     * @param experiment The id of the experiment during which the file was uploaded.
     * @param date The time at which the file was uploaded.
     * @param name The name of the file.
     * @param filetype The filetype.
     * @param content The file content.
     */
    public FileDTO(final Integer user, final Integer experiment, final LocalDateTime date, final String name,
                   final String filetype, final byte[] content) {
        this.user = user;
        this.experiment = experiment;
        this.date = date;
        this.name = name;
        this.filetype = filetype;
        this.content = content;
    }

    /**
     * {@inheritDoc}
     *
     * @return The file ID.
     */
    @Override
    public Integer getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     *
     * @param id The file ID to be set.
     */
    @Override
    public void setId(final Integer id) {
        this.id = id;
    }

    /**
     * {@inheritDoc}
     *
     * @return The user's ID.
     */
    @Override
    public Integer getUser() {
        return user;
    }

    /**
     * {@inheritDoc}
     *
     * @param user The user ID to be set.
     */
    @Override
    public void setUser(final Integer user) {
        this.user = user;
    }

    /**
     * {@inheritDoc}
     *
     * @return The experiment ID.
     */
    @Override
    public Integer getExperiment() {
        return experiment;
    }

    /**
     * {@inheritDoc}
     *
     * @param experiment The experiment ID to be set.
     */
    @Override
    public void setExperiment(final Integer experiment) {
        this.experiment = experiment;
    }

    /**
     * {@inheritDoc}
     *
     * @return The time at which the file was uploaded.
     */
    @Override
    public LocalDateTime getDate() {
        return date;
    }

    /**
     * {@inheritDoc}
     *
     * @param date The time to be set.
     */
    @Override
    public void setDate(final LocalDateTime date) {
        this.date = date;
    }

    /**
     * Returns the name of the file.
     *
     * @return The file name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the file.
     *
     * @param name The file name to be set.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns the type of the file.
     *
     * @return The filetype.
     */
    public String getFiletype() {
        return filetype;
    }

    /**
     * Sets the type of the file.
     *
     * @param filetype The filetype to be set.
     */
    public void setFiletype(final String filetype) {
        this.filetype = filetype;
    }

    /**
     * Returns the content of the file.
     *
     * @return The file content.
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * Sets the content of the file.
     *
     * @param content The file content to be set.
     */
    public void setContent(final byte[] content) {
        this.content = content;
    }

    /**
     * Indicates whether some {@code other} file DTO is semantically equal to this file DTO.
     *
     * @param other The object to compare this file DTO to.
     * @return {@code true} iff {@code other} is a semantically equivalent file DTO.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        FileDTO that = (FileDTO) other;
        return Objects.equals(id, that.id);
    }

    /**
     * Calculates a hash code for this file DTO for hashing purposes, and to fulfill the {@link Object#equals(Object)}
     * contract.
     *
     * @return The hash code value of the file DTO.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Converts the file DTO into a human-readable string representation.
     *
     * @return A human-readable string representation of the file DTO.
     */
    @Override
    public String toString() {
        return "FileDTO{"
                + "id=" + id
                + ", user=" + user
                + ", experiment=" + experiment
                + ", date=" + date
                + ", name='" + name + '\''
                + ", filetype='" + filetype + '\''
                + ", content=" + Arrays.toString(content)
                + '}';
    }

}
