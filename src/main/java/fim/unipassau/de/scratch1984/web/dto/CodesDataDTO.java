package fim.unipassau.de.scratch1984.web.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

/**
 * A DTO representing the number of times an xml code has been saved for a user during an experiment.
 */
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
public class CodesDataDTO {

    /**
     * The ID of the user to whom the data belongs.
     */
    private Integer user;

    /**
     * The ID of the experiment in which the counted events occurred.
     */
    private Integer experiment;

    /**
     * The number of times the event occurred.
     */
    private int count;

    /**
     * Constructs a new codes data dto with the given attributes.
     *
     * @param user The id of the user for whom the code was saved.
     * @param experiment The id of the experiment during which the code was saved.
     * @param count The number of times an xml code was saved.
     */
    public CodesDataDTO(final int user, final int experiment, final int count) {
        this.user = user;
        this.experiment = experiment;
        this.count = count;
    }

    /**
     * Indicates whether some {@code other} codes data DTO is semantically equal to this codes data DTO.
     *
     * @param other The object to compare this codes data DTO to.
     * @return {@code true} iff {@code other} is a semantically equivalent codes data DTO.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        CodesDataDTO that = (CodesDataDTO) other;
        return Objects.equals(user, that.user) && Objects.equals(experiment, that.experiment);
    }

    /**
     * Calculates a hash code for this codes data DTO for hashing purposes, and to fulfill the
     * {@link Object#equals(Object)} contract.
     *
     * @return The hash code value of the codes data DTO.
     */
    @Override
    public int hashCode() {
        return Objects.hash(user, experiment);
    }

}
