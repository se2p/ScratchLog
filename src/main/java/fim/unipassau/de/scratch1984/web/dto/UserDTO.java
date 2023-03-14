package fim.unipassau.de.scratch1984.web.dto;

import fim.unipassau.de.scratch1984.util.enums.Language;
import fim.unipassau.de.scratch1984.util.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A DTO representing a user.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

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
     * The last time the user logged in to use the application.
     */
    @Column(name = "last_login")
    private LocalDateTime lastLogin;

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

        UserDTO that = (UserDTO) other;
        return Objects.equals(id, that.id);
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
