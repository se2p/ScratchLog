package fim.unipassau.de.scratch1984.web.dto;

import java.util.Objects;

/**
 * A DTO representing a user.
 */
public class UserDTO {

    /**
     * All available roles for a user.
     */
    public enum Role {
        /**
         * A user participating in an experiment.
         */
        PARTICIPANT,

        /**
         * A user with administration rights.
         */
        ADMIN
    }

    /**
     * The user's unique ID.
     */
    private Integer id;

    /**
     * The user's unique username.
     */
    private String username;

    /**
     * The user's email.
     */
    private String email;

    /**
     * The user's role.
     */
    private Role role;

    /**
     * The salt to use when hashing this user's password.
     */
    private String salt;

    /**
     * The user's password hashed using the salt {@link #salt}.
     */
    private String password;

    /**
     * The secret used to identify the user.
     */
    private String secret;

    /**
     * Boolean indicating whether the user is trying to reset their password.
     */
    private Boolean reset;

    /**
     * Boolean indicating whether the user is active and thus able to use certain functions of the application.
     */
    private Boolean active;

    /**
     * Returns the user's ID.
     *
     * @return The user ID.
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the user's ID.
     *
     * @param id The user ID to be set.
     */
    public void setId(final Integer id) {
        this.id = id;
    }

    /**
     * Returns the user's username.
     *
     * @return The username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the user's username.
     *
     * @param username The username to be set.
     */
    public void setUsername(final String username) {
        this.username = username;
    }

    /**
     * Returns the user's email.
     *
     * @return The user's email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email.
     *
     * @param email The email to be set.
     */
    public void setEmail(final String email) {
        this.email = email;
    }

    /**
     * Returns the user's role.
     *
     * @return The user's role.
     */
    public Role getRole() {
        return role;
    }

    /**
     * Sets the user's role.
     *
     * @param role The role to be set.
     */
    public void setRole(final Role role) {
        this.role = role;
    }

    /**
     * Returns the user's salt.
     *
     * @return The user's salt.
     */
    public String getSalt() {
        return salt;
    }

    /**
     * Sets the user's salt.
     *
     * @param salt The salt to be set.
     */
    public void setSalt(final String salt) {
        this.salt = salt;
    }

    /**
     * Returns the user's password.
     *
     * @return The user's hashed password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the user's password.
     *
     * @param password The password to be set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the user's secret.
     *
     * @return The user's secret.
     */
    public String getSecret() {
        return secret;
    }

    /**
     * Sets the user's secret.
     *
     * @param secret The secret to be set.
     */
    public void setSecret(final String secret) {
        this.secret = secret;
    }

    /**
     * Returns whether the user is trying to reset their password.
     *
     * @return {@code true} iff a password reset has been requested.
     */
    public Boolean getReset() {
        return reset;
    }

    /**
     * Sets whether this user has requested to reset their password.
     *
     * @param reset The user's reset status to be set.
     */
    public void setReset(final Boolean reset) {
        this.reset = reset;
    }

    /**
     * Returns whether the user is active.
     *
     * @return {@code true} iff the user is active.
     */
    public Boolean getActive() {
        return active;
    }

    /**
     * Sets whether this user's profile is currently active.
     *
     * @param active The user's active status to be set.
     */
    public void setActive(final Boolean active) {
        this.active = active;
    }

    /**
     * Indicates whether some {@code other} user DTO is semantically equal to this user DTO.
     *
     * @param other The object to compare this user DTO to.
     * @return {@code true} iff {@code other} is a semantically equivalent user DTO.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        UserDTO user = (UserDTO) other;
        return id.equals(user.id);
    }

    /**
     * Calculates a hash code for this user DTO for hashing purposes, and to fulfill the {@link Object#equals(Object)}
     * contract.
     *
     * @return The hash code value of the user DTO.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Converts the user DTO into a human-readable string representation.
     *
     * @return A human-readable string representation of the user DTO.
     */
    @Override
    public String toString() {
        return "UserDTO{"
                + "id=" + id
                + ", username='" + username + '\''
                + ", email='" + email + '\''
                + ", role=" + role
                + ", salt='" + salt + '\''
                + ", secret='" + secret + '\''
                + ", reset=" + reset
                + ", active=" + active
                + '}';
    }

}
