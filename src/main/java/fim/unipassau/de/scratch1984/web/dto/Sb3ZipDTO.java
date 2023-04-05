package fim.unipassau.de.scratch1984.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A DTO representing a sb3 zip file uploaded during an experiment.
 */
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Sb3ZipDTO implements EventDTO {

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

        Sb3ZipDTO that = (Sb3ZipDTO) other;
        return Objects.equals(id, that.id);
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

}
