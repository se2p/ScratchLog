package fim.unipassau.de.scratch1984.persistence.projection;

/**
 * Projection interface for the {@link fim.unipassau.de.scratch1984.persistence.entity.User} class to return only the
 * user id, username, email and role.
 */
public interface UserProjection {

    /**
     * Returns the unique id of the user.
     *
     * @return The user id.
     */
    Integer getId();

    /**
     * Returns the unique username of the user.
     *
     * @return The username.
     */
    String getUsername();

    /**
     * Returns the unique email of the user.
     *
     * @return The email.
     */
    String getEmail();

    /**
     * Returns the role of the user.
     *
     * @return The role.
     */
    String getRole();

}
