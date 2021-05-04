package fim.unipassau.de.scratch1984.web.dto;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A DTO representing a participation in an experiment.
 */
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
     * Returns user's ID.
     *
     * @return The user ID.
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
     * Returns the ID of the experiment.
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
     * Returns the starting time.
     *
     * @return The starting time.
     */
    public LocalDateTime getStart() {
        return start;
    }

    /**
     * Sets the time at which the user started the experiment.
     *
     * @param start The starting time to be set.
     */
    public void setStart(final LocalDateTime start) {
        this.start = start;
    }

    /**
     * Returns the finishing time.
     *
     * @return The finishing time.
     */
    public LocalDateTime getEnd() {
        return end;
    }

    /**
     * Sets the time at which the user finished the experiment.
     *
     * @param end The finishing time to be set.
     */
    public void setEnd(final LocalDateTime end) {
        this.end = end;
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
        return user.equals(that.user) && experiment.equals(that.experiment);
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

    /**
     * Converts the participant DTO into a human-readable string representation.
     *
     * @return A human-readable string representation of the participant DTO.
     */
    @Override
    public String toString() {
        return "ParticipantDTO{"
                + "user=" + user
                + ", experiment=" + experiment
                + ", start=" + start
                + ", end=" + end
                + '}';
    }

}
