package fim.unipassau.de.scratch1984.web.dto;

import java.util.Objects;

/**
 * A DTO that represents adding multiple participants at once.
 */
public class UserBulkDTO {

    /**
     * The number of participants to add.
     */
    private int amount;

    /**
     * One of the available languages in {@link UserDTO.Language}.
     */
    private UserDTO.Language language;

    /**
     * The username pattern.
     */
    private String username;

    /**
     * The boolean indicating whether the usernames should be numbered starting with one. Alternatively, the currently
     * highest user ID found in the database will be used.
     */
    private boolean startAtOne;

    /**
     * Default constructor for the user bulk DTO.
     */
    public UserBulkDTO() {
    }

    /**
     * Constructs a new user bulk DTO with the given attributes.
     *
     * @param amount The number of participants to add.
     * @param language The preferred language.
     * @param username The username pattern.
     * @param startAtOne Whether the username numbering should start at one.
     */
    public UserBulkDTO(final int amount, final UserDTO.Language language, final String username,
                       final boolean startAtOne) {
        this.amount = amount;
        this.language = language;
        this.username = username;
        this.startAtOne = startAtOne;
    }

    /**
     * Returns the amount of participants to be added.
     *
     * @return The amount of participants.
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Sets the amount of participants to be added.
     *
     * @param amount The amount of participants to be set.
     */
    public void setAmount(final int amount) {
        this.amount = amount;
    }

    /**
     * Returns the participants' preferred language.
     *
     * @return The participants' language.
     */
    public UserDTO.Language getLanguage() {
        return language;
    }

    /**
     * Sets the participants' preferred language.
     *
     * @param language The language to be set.
     */
    public void setLanguage(final UserDTO.Language language) {
        this.language = language;
    }

    /**
     * Returns the username pattern.
     *
     * @return The username pattern.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username pattern.
     *
     * @param username The username pattern to be set.
     */
    public void setUsername(final String username) {
        this.username = username;
    }

    /**
     * Indicates whether the numbering of the username pattern should start at one.
     *
     * @return {@code true} iff the number added to the username starts at one.
     */
    public boolean isStartAtOne() {
        return startAtOne;
    }

    /**
     * Sets whether the username pattern numbering should start at one.
     *
     * @param startAtOne The value to be set.
     */
    public void setStartAtOne(final boolean startAtOne) {
        this.startAtOne = startAtOne;
    }

    /**
     * Indicates whether some {@code other} user bulk DTO is semantically equal to this user bulk DTO.
     *
     * @param other The object to compare this user bulk DTO to.
     * @return {@code true} iff {@code other} is a semantically equivalent user bulk DTO.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        UserBulkDTO that = (UserBulkDTO) other;
        return amount == that.amount
                && startAtOne == that.startAtOne
                && language == that.language
                && username.equals(that.username);
    }

    /**
     * Calculates a hash code for this user bulk DTO for hashing purposes, and to fulfill the
     * {@link Object#equals(Object)} contract.
     *
     * @return The hash code value of the user bulk DTO.
     */
    @Override
    public int hashCode() {
        return Objects.hash(amount, language, username, startAtOne);
    }

    /**
     * Converts the user bulk DTO into a human-readable string representation.
     *
     * @return A human-readable string representation of the user bulk DTO.
     */
    @Override
    public String toString() {
        return "UserBulkDTO{"
                + "amount=" + amount
                + ", language='" + language + '\''
                + ", username='" + username + '\''
                + ", startAtOne=" + startAtOne
                + '}';
    }

}
