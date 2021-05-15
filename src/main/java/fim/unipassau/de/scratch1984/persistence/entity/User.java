package fim.unipassau.de.scratch1984.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * An entity representing a user.
 */
@Entity
public class User {

    /**
     * The user's unique ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * The user's unique username.
     */
    @Column(unique = true, name = "username")
    private String username;

    /**
     * The user's email.
     */
    @Column(name = "email")
    private String email;

    /**
     * A String representing the {@link fim.unipassau.de.scratch1984.web.dto.UserDTO.Role}.
     */
    @Column(name = "role")
    private String role;

    /**
     * A String representing the {@link fim.unipassau.de.scratch1984.web.dto.UserDTO.Language}.
     */
    @Column(name = "language")
    private String language;

    /**
     * The user's hashed password.
     */
    @Column(name = "password")
    private String password;

    /**
     * The secret used to identify the user.
     */
    @Column(name = "secret")
    private String secret;

    /**
     * Boolean indicating whether the user is trying to reset their password.
     */
    @Column(name = "reset_password")
    private boolean reset;

    /**
     * Boolean indicating whether the user is active and thus able to use certain functions of the application.
     */
    @Column(name = "active")
    private boolean active;

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
    public String getRole() {
        return role;
    }

    /**
     * Sets the user's role.
     *
     * @param role The role to be set.
     */
    public void setRole(final String role) {
        this.role = role;
    }

    /**
     * Returns the user's preferred language.
     *
     * @return The user's preferred language.
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Sets the user's preferred language.
     *
     * @param salt The language to be set.
     */
    public void setLanguage(final String salt) {
        this.language = salt;
    }

    /**
     * Returns the user's hashed password.
     *
     * @return The user's password.
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
    public boolean isReset() {
        return reset;
    }

    /**
     * Sets whether this user has requested to reset their password.
     *
     * @param reset The user's reset status to be set.
     */
    public void setReset(final boolean reset) {
        this.reset = reset;
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

}
