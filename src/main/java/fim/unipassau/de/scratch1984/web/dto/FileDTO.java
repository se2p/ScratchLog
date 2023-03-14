package fim.unipassau.de.scratch1984.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import fim.unipassau.de.scratch1984.util.ByteArrayDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A DTO representing a file uploaded during an experiment.
 */
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
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
    @JsonProperty("time")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime date;

    /**
     * The file name.
     */
    private String name;

    /**
     * The filetype.
     */
    @JsonProperty("type")
    private String filetype;

    /**
     * The file content itself.
     */
    @JsonProperty("file")
    @JsonDeserialize(using = ByteArrayDeserializer.class)
    private byte[] content;

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

}
