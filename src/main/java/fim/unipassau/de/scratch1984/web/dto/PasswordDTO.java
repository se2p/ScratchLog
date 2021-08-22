package fim.unipassau.de.scratch1984.web.dto;

/**
 * A DTO representing a user password.
 */
public class PasswordDTO {

    /**
     * The user's password.
     */
    private String password;

    /**
     * Default constructor for the password dto.
     */
    public PasswordDTO() {
    }

    /**
     * Constructs a new password dto with the given attributes.
     *
     * @param password The password.
     */
    public PasswordDTO(final String password) {
        this.password = password;
    }

    /**
     * Returns the password.
     *
     * @return The password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password The password to be set.
     */
    public void setPassword(final String password) {
        this.password = password;
    }

}
