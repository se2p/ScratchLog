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
     * All available languages to choose from.
     */
    public enum Language {
        /**
         * The preferred language is English.
         */
        ENGLISH,

        /**
         * The preferred language is German.
         */
        GERMAN
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
     * The user's preferred language.
     */
    private Language language;

    /**
     * The user's hashed password.
     */
    private String password;

    /**
     * The user's new password.
     */
    private String newPassword;

    /**
     * The user's new password repeated.
     */
    private String confirmPassword;

    /**
     * The secret used to identify the user.
     */
    private String secret;

    /**
     * The number of current login attempts for the user.
     */
    private int attempts;

    /**
     * Boolean indicating whether the user is active and thus able to use certain functions of the application.
     */
    private boolean active;

    /**
     * Default constructor for the user dto.
     */
    public UserDTO() {
    }

    /**
     * Constructs a new user dto with the given attributes.
     *
     * @param username The user's username.
     * @param email The user's email.
     * @param role The user's role.
     * @param language The user's preferred language.
     * @param password The user's hashed password.
     * @param secret The user's secret.
     */
    public UserDTO(final String username, final String email, final Role role, final Language language,
                   final String password, final String secret) {
        this.username = username;
        this.email = email;
        this.role = role;
        this.language = language;
        this.password = password;
        this.secret = secret;
    }

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
     * Returns the user's preferred language.
     *
     * @return The user's language.
     */
    public Language getLanguage() {
        return language;
    }

    /**
     * Sets the user's preferred language.
     *
     * @param language The language to be set.
     */
    public void setLanguage(final Language language) {
        this.language = language;
    }

    /**
     * Returns the user's hashed password.
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
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * Returns the user's new password.
     *
     * @return The user's new password.
     */
    public String getNewPassword() {
        return newPassword;
    }

    /**
     * Sets the user's new password.
     *
     * @param newPassword The new password to be set.
     */
    public void setNewPassword(final String newPassword) {
        this.newPassword = newPassword;
    }

    /**
     * Returns the user's repeated password.
     *
     * @return The user's repeated password.
     */
    public String getConfirmPassword() {
        return confirmPassword;
    }

    /**
     * Sets the user's repeated password.
     *
     * @param confirmPassword The password to be set.
     */
    public void setConfirmPassword(final String confirmPassword) {
        this.confirmPassword = confirmPassword;
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
     * Returns the number of login attempts for the user.
     *
     * @return The number of attempts.
     */
    public int getAttempts() {
        return attempts;
    }

    /**
     * Sets the number of login attempts for the user.
     *
     * @param attempts The number of login attempts to be set.
     */
    public void setAttempts(final int attempts) {
        this.attempts = attempts;
    }

    /**
     * Returns whether the user is active.
     *
     * @return {@code true} iff the user is active.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets whether this user's profile is currently active.
     *
     * @param active The user's active status to be set.
     */
    public void setActive(final boolean active) {
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
                + ", language='" + language + '\''
                + ", secret='" + secret + '\''
                + ", attempts=" + attempts
                + ", active=" + active
                + '}';
    }

}
