package fim.unipassau.de.scratch1984.web.dto;

import java.time.LocalDateTime;
import java.util.Objects;

public class TokenDTO {

    /**
     * The available token types.
     */
    public enum Type {
        /**
         * A token for registration.
         */
        REGISTER,

        /**
         * A token for resetting the password.
         */
        FORGOT_PASSWORD,

        /**
         * A token for changing the e-mail address.
         */
        CHANGE_EMAIL
    }

    /**
     * Default constructor for the token dto.
     */
    public TokenDTO() {
    }

    /**
     * Constructs a new token dto with the given attributes.
     *
     * @param type The type of the token.
     * @param expirationDate The expiration date of the token.
     * @param metadata Optional metadata for the token.
     * @param user The id of the user for whom this token is to be created.
     */
    public TokenDTO(final Type type, final LocalDateTime expirationDate, final String metadata, final Integer user) {
        this.type = type;
        this.expirationDate = expirationDate;
        this.metadata = metadata;
        this.user = user;
    }

    /**
     * The unique value of the token.
     */
    private String value;

    /**
     * The type of the token.
     */
    private Type type;

    /**
     * The expiration date of the token.
     */
    private LocalDateTime expirationDate;

    /**
     * The optional metadata for this token.
     */
    private String metadata;

    /**
     * The id of the user to whom this token belongs.
     */
    private Integer user;

    /**
     * Returns the value of the token.
     *
     * @return The respective value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the token.
     *
     * @param value The value to be set.
     */
    public void setValue(final String value) {
        this.value = value;
    }

    /**
     * Returns the type of the token.
     *
     * @return The respective type.
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the type of the token.
     *
     * @param type The type to be set.
     */
    public void setType(final Type type) {
        this.type = type;
    }

    /**
     * Returns the expiration date of the token.
     *
     * @return The respective date.
     */
    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    /**
     * Sets the expiration date of the token.
     *
     * @param expirationDate The expiration date to be set.
     */
    public void setExpirationDate(final LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    /**
     * Returns the metadata of the token.
     *
     * @return The respective metadata.
     */
    public String getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata of the token.
     *
     * @param metadata The metadata to be set.
     */
    public void setMetadata(final String metadata) {
        this.metadata = metadata;
    }

    /**
     * Returns the id of the user to whom this token belongs.
     *
     * @return The respective user id.
     */
    public Integer getUser() {
        return user;
    }

    /**
     * Sets the id of the user to whom this token belongs.
     *
     * @param user The user id to be set.
     */
    public void setUser(final Integer user) {
        this.user = user;
    }

    /**
     * Indicates whether some {@code other} token DTO is semantically equal to this token DTO.
     *
     * @param other The object to compare this token DTO to.
     * @return {@code true} iff {@code other} is a semantically equivalent token DTO.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        TokenDTO tokenDTO = (TokenDTO) other;
        return value.equals(tokenDTO.value);
    }

    /**
     * Calculates a hash code for this token DTO for hashing purposes, and to fulfill the {@link Object#equals(Object)}
     * contract.
     *
     * @return The hash code value of the token DTO.
     */
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    /**
     * Converts the token DTO into a human-readable string representation.
     *
     * @return A human-readable string representation of the token DTO.
     */
    @Override
    public String toString() {
        return "TokenDTO{"
                + "value='" + value + '\''
                + ", type=" + type
                + ", expirationDate=" + expirationDate
                + ", metadata='" + metadata + '\''
                + ", user=" + user
                + '}';
    }

}
