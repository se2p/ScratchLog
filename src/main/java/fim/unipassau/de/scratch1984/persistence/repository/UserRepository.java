package fim.unipassau.de.scratch1984.persistence.repository;

import fim.unipassau.de.scratch1984.persistence.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * A repository providing functionality for retrieving user data.
 */
public interface UserRepository extends JpaRepository<User, Integer> {

     /**
     * Checks, whether a user with the given username already exists in the database.
     *
     * @param username The username to search for.
     * @return {@code true} iff the username already exists.
     */
    boolean existsByUsername(String username);

    /**
     * Checks, whether a user with the given email already exists in the database.
     *
     * @param email The email to search for.
     * @return {@code true} iff the email already exists.
     */
    boolean existsByEmail(String email);

    /**
     * Returns the user identified by the given username, if one exists.
     *
     * @param username The username to search for.
     * @return The user data or {@code null}, if no user could be found.
     */
    User findUserByUsername(String username);

    /**
     * Returns the user identified by the given id, if one exists.
     *
     * @param id The id to search for.
     * @return The user data or {@code null}, if no user could be found.
     */
    Optional<User> findById(int id);

    /**
     * Returns a list of users with the given role, or {@code null} if no such user exists.
     *
     * @param role The user role to search for.
     * @return A list of users or null.
     */
    List<User> findAllByRole(String role);

}
