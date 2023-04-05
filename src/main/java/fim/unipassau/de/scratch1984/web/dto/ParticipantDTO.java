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
 * A DTO representing a participation in an experiment.
 */
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantDTO {

    /**
     * The participating user's ID.
     */
    private Integer user;

    /**
     * The ID of the experiment.
     */
    private Integer experiment;

    /**
     * The local date time at which the user started the experiment.
     */
    private LocalDateTime start;

    /**
     * The local date time at which the user finished the experiment.
     */
    private LocalDateTime end;

    /**
     * Constructs a new participant dto with the given attributes.
     *
     * @param user The participating user's id.
     * @param experiment The id of the experiment in which the user is participating.
     */
    public ParticipantDTO(final int user, final int experiment) {
        this.user = user;
        this.experiment = experiment;
    }

    /**
     * Indicates whether some {@code other} participant DTO is semantically equal to this participant DTO.
     *
     * @param other The object to compare this participant DTO to.
     * @return {@code true} iff {@code other} is a semantically equivalent participant DTO.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        ParticipantDTO that = (ParticipantDTO) other;
        return Objects.equals(user, that.user) && Objects.equals(experiment, that.experiment);
    }

    /**
     * Calculates a hash code for this participant DTO for hashing purposes, and to fulfill the
     * {@link Object#equals(Object)} contract.
     *
     * @return The hash code value of the participant DTO.
     */
    @Override
    public int hashCode() {
        return Objects.hash(user, experiment);
    }

}
