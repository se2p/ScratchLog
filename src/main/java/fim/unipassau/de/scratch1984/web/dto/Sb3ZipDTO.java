package fim.unipassau.de.scratch1984.web.dto;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

/**
 * A DTO representing an sb3 zip file uploaded during an experiment.
 */
public class Sb3ZipDTO {

    /**
     * The unique ID of the zip file.
     */
    private Integer id;

    /**
     * The ID of the user for whom the zip file was created.
     */
    private Integer user;

    /**
     * The ID of the experiment during which the zip file was created.
     */
    private Integer experiment;

    /**
     * The local date time at which the zip file was created by the Scratch VM.
     */
    private LocalDateTime date;

    /**
     * The file name.
     */
    private String name;

    /**
     * The file content itself.
     */
    private byte[] content;

    /**
     * Default constructor for the sb3 zip dto.
     */
    public Sb3ZipDTO() {
    }

    /**
     * Constructs a new sb3 zip dto with the given attributes.
     *
     * @param user The id of the user for whom the zip file was created.
     * @param experiment The id of the experiment during which the zip file was created.
     * @param date The time at which the zip file was created.
     * @param name The name of the zip file.
     * @param content The zip file content.
     */
    public Sb3ZipDTO(final Integer user, final Integer experiment, final LocalDateTime date, final String name,
                     final byte[] content) {
        this.user = user;
        this.experiment = experiment;
        this.date = date;
        this.name = name;
        this.content = content;
    }

    /**
     * Returns the ID of the zip file.
     *
     * @return The file ID.
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the ID of the zip file.
     *
     * @param id The file ID to be set.
     */
    public void setId(final Integer id) {
        this.id = id;
    }

    /**
     * Returns the ID of the user for whom the zip file was created.
     *
     * @return The user's ID.
     */
    public Integer getUser() {
        return user;
    }

    /**
     * Sets the user's ID.
     *
     * @param user The user ID to be set.
     */
    public void setUser(final Integer user) {
        this.user = user;
    }

    /**
     * Returns the ID of the experiment during which the zip file was created.
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
     * Returns the upload time.
     *
     * @return The upload time.
     */
    public LocalDateTime getDate() {
        return date;
    }

    /**
     * Sets the upload time of the zip file.
     *
     * @param date The date and time to be set.
     */
    public void setDate(final LocalDateTime date) {
        this.date = date;
    }

    /**
     * Returns the name of the zip file.
     *
     * @return The file name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the zip file.
     *
     * @param name The file name to be set.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns the content of the zip file.
     *
     * @return The file content.
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * Sets the content of the zip file.
     *
     * @param content The file content to be set.
     */
    public void setContent(final byte[] content) {
        this.content = content;
    }

    /**
     * Indicates whether some {@code other} sb3 zip DTO is semantically equal to this sb3 zip DTO.
     *
     * @param other The object to compare this sb3 zip DTO to.
     * @return {@code true} iff {@code other} is a semantically equivalent sb3 zip DTO.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        Sb3ZipDTO sb3ZipDTO = (Sb3ZipDTO) other;
        return id.equals(sb3ZipDTO.id);
    }

    /**
     * Calculates a hash code for this sb3 zip DTO for hashing purposes, and to fulfill the
     * {@link Object#equals(Object)} contract.
     *
     * @return The hash code value of the sb3 zip DTO.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Converts the sb3 zip DTO into a human-readable string representation.
     *
     * @return A human-readable string representation of the sb3 zip DTO.
     */
    @Override
    public String toString() {
        return "Sb3ZipDTO{"
                + "id=" + id
                + ", user=" + user
                + ", experiment=" + experiment
                + ", date=" + date
                + ", name='" + name + '\''
                + ", content=" + Arrays.toString(content)
                + '}';
    }

}
