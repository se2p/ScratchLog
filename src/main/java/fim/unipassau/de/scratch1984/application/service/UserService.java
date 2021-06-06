package fim.unipassau.de.scratch1984.application.service;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.exception.StoreException;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ParticipantRepository;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.web.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

/**
 * A service providing methods related to users.
 */
@Service
public class UserService {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    /**
     * The user repository to use for database queries related to user data.
     */
    private final UserRepository userRepository;

    /**
     * The experiment repository to use for database queries related to experiment data.
     */
    private final ExperimentRepository experimentRepository;

    /**
     * The participant repository to use for database queries participation data.
     */
    private final ParticipantRepository participantRepository;

    /**
     * The password encoder to use for hashing passwords.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs a user service with the given dependencies.
     *
     * @param userRepository The user repository to use.
     * @param participantRepository The participant repository to use.
     * @param experimentRepository The experiment repository to use.
     * @param passwordEncoder The password encoder to use.
     */
    @Autowired
    public UserService(final UserRepository userRepository, final ParticipantRepository participantRepository,
                       final ExperimentRepository experimentRepository, final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.participantRepository = participantRepository;
        this.experimentRepository = experimentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Checks, whether any user with the given username exists in the database.
     *
     * @param username The username to search for.
     * @return {@code true} if a user exists, or {@code false} if not.
     */
    @Transactional
    public boolean existsUser(final String username) {
        if (username == null || username.trim().isBlank()) {
            return false;
        }

        return userRepository.existsByUsername(username);
    }

    /**
     * Checks, whether any user with the given email exists in the database.
     *
     * @param email The email to search for.
     * @return {@code true} if a user exists, or {@code false} if not.
     */
    @Transactional
    public boolean existsEmail(final String email) {
        if (email == null || email.trim().isBlank()) {
            return false;
        }

        return userRepository.existsByEmail(email);
    }

    /**
     * Checks, whether any participant relation for the given user and experiment IDs exists.
     *
     * @param userId The user id to search for.
     * @param experimentId The experiment id to search for.
     * @return {@code true} if a user exists, or {@code false} if not.
     */
    @Transactional
    public boolean existsParticipant(final int userId, final int experimentId) {
        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            return false;
        }

        User user = userRepository.getOne(userId);
        Experiment experiment = experimentRepository.getOne(experimentId);

        try {
            return participantRepository.existsByUserAndExperiment(user, experiment);
        } catch (EntityNotFoundException e) {
            return false;
        }
    }

    /**
     * Creates a new user with the given parameters in the database.
     *
     * @param userDTO The dto containing the user information to set.
     * @return The newly created user, if the information was persisted, or {@code null} if not.
     */
    @Transactional
    public UserDTO saveUser(final UserDTO userDTO) {
        if (userDTO.getUsername() == null || userDTO.getUsername().trim().isBlank()) {
            logger.error("Cannot create user with username null or blank!");
            throw new IllegalArgumentException("Cannot create user with username null or blank!");
        }

        User user = userRepository.save(createUser(userDTO));

        if (user.getId() == null) {
            logger.error("Failed to save user with username " + userDTO.getUsername());
            throw new StoreException("Failed to save user with username " + userDTO.getUsername());
        }

        return createUserDTO(user);
    }

    /**
     * Returns the user with the specified username. If no such user exists, a {@link NotFoundException} is thrown
     * instead.
     *
     * @param username The username to search for.
     * @return The user, if they exist.
     */
    @Transactional
    public UserDTO getUser(final String username) {
        if (username == null || username.trim().isBlank()) {
            logger.error("The username cannot be null or blank!");
            throw new IllegalArgumentException("The username cannot be null or blank!");
        }

        User user = userRepository.findUserByUsername(username);

        if (user == null) {
            logger.error("Could not find user with username " + username + ".");
            throw new NotFoundException("Could not find user with username " + username + ".");
        }

        return createUserDTO(user);
    }

    /**
     * Returns the user with the specified id. If no such user exists, a {@link NotFoundException} is thrown instead.
     *
     * @param id The id to search for.
     * @return The user, if they exist.
     */
    @Transactional
    public UserDTO getUserById(final int id) {
        if (id < Constants.MIN_ID) {
            logger.error("Cannot search for user with invalid id " + id + "!");
            throw new IllegalArgumentException("Cannot search for user with invalid id " + id + "!");
        }

        Optional<User> user = userRepository.findById(id);

        if (user.isEmpty()) {
            logger.error("Could not find user with id " + id + ".");
            throw new NotFoundException("Could not find user with id " + id + ".");
        }

        return createUserDTO(user.get());
    }

    /**
     * Verifies the given user's credentials on login and returns a {@link UserDTO} containing the user's information.
     * If no user with matching username and password could be found, a {@link NotFoundException} is thrown instead.
     *
     * @param userDTO The {@link UserDTO} containing the username and password entered in the login form.
     * @return A new {@link UserDTO} containing the user's information stored in the database.
     */
    @Transactional
    public UserDTO loginUser(final UserDTO userDTO) {
        User user = userRepository.findUserByUsername(userDTO.getUsername());

        if (user != null) {
            if ((userDTO.getPassword() != null) && (matchesPassword(userDTO.getPassword(), user.getPassword()))) {
                return createUserDTO(user);
            } else {
                logger.error("Failed to log in user with username " + userDTO.getUsername() + ".");
                throw new NotFoundException("Incorrect username or password!");
            }
        }

        logger.error("Could not find user with username " + userDTO.getUsername() + " in the database.");
        throw new NotFoundException("Incorrect username or password!");
    }

    /**
     * Searches for the user with the given secret, activates their account, and returns a {@link UserDTO} containing
     * the user's information. If no user with matching secret could be found, a {@link NotFoundException} is thrown
     * instead.
     *
     * @param secret The secret to search for.
     * @return A new {@link UserDTO} containing the user's information stored in the database.
     */
    @Transactional
    public UserDTO authenticateUser(final String secret) {
        if (secret == null || secret.trim().isBlank()) {
            logger.error("Cannot search for user with secret null or blank!");
            throw new IllegalArgumentException("Cannot search for user with secret null or blank!");
        }

        User user = userRepository.findUserBySecret(secret);

        if (user == null) {
            logger.error("Could not find any user with the secret " + secret + " in the database!");
            throw new NotFoundException("Could not find any user with the secret " + secret + " in the database!");
        }

        user.setActive(true);
        User saved = userRepository.save(user);
        return createUserDTO(saved);
    }

    /**
     * Updates the information of the given user with the given values.
     *
     * @param userDTO The {@link UserDTO} containing the updated user information.
     * @return The updated user information.
     */
    @Transactional
    public UserDTO updateUser(final UserDTO userDTO) {
        if (userDTO.getId() == null || userDTO.getId() < Constants.MIN_ID) {
            logger.error("Cannot save user with invalid id " + userDTO.getId() + "!");
            throw new IllegalArgumentException("Cannot save user with invalid id " + userDTO.getId() + "!");
        }

        User user = userRepository.save(createUser(userDTO));
        return createUserDTO(user);
    }

    /**
     * Updates the email of the user with the given id to the given value. If no corresponding user exists, a
     * {@link NotFoundException} is thrown instead.
     *
     * @param id The user's id.
     * @param email The new email to be set.
     */
    @Transactional
    public void updateEmail(final int id, final String email) {
        if (id < Constants.MIN_ID) {
            logger.error("Cannot search for user with invalid id " + id + "!");
            throw new IllegalArgumentException("Cannot search for user with invalid id " + id + "!");
        } else if (email == null || email.trim().isBlank()) {
            logger.error("Cannot update email for user with id " + id + " with email null or blank!");
            throw new IllegalArgumentException("Cannot update email for user with id " + id
                    + " with email null or blank!");
        }

        Optional<User> user = userRepository.findById(id);

        if (user.isEmpty()) {
            logger.error("Could not find user with id " + id + " in the database!");
            throw new NotFoundException("Could not find user with id " + id + " in the database!");
        }

        User found = user.get();
        found.setEmail(email);
        userRepository.save(found);
    }

    /**
     * Verifies how many users with administrator status are currently registered. If no administrator can be found an
     * {@link IllegalStateException} is thrown instead.
     *
     * @return {@code true} if only one administrator remains in the database, or {@code false} otherwise.
     */
    @Transactional
    public boolean isLastAdmin() {
        List<User> admins = userRepository.findAllByRole(UserDTO.Role.ADMIN.toString());

        if (admins.size() < Constants.MIN_ID) {
            logger.error("There are no users with administrator status in the database!");
            throw new IllegalStateException("There are no users with administrator status in the database!");
        }

        return admins.size() == 1;
    }

    /**
     * Deletes the user with the given id from the database, if any such user exists.
     *
     * @param id The id to search for.
     */
    @Transactional
    public void deleteUser(final int id) {
        if (id < Constants.MIN_ID) {
            logger.error("Cannot delete user with invalid id " + id + "!");
            throw new IllegalArgumentException("Cannot delete user with invalid id " + id + "!");
        }

        userRepository.deleteById(id);
    }

    /**
     * Returns the current highest user id value.
     *
     * @return The id.
     */
    @Transactional
    public int findLastId() {
        User user = userRepository.findFirstByOrderByIdDesc();

        if (user == null) {
            logger.error("There are no users in database!");
            throw new IllegalStateException("There are no users in database!");
        }

        return user.getId();
    }

    /**
     * Checks, whether the given input string matches the given hashed password value.
     *
     * @param input The input string.
     * @param password The hashed password.
     * @return {@code true}, if the strings match, or {@code false}, if not.
     */
    public boolean matchesPassword(final String input, final String password) {
        return passwordEncoder.matches(input, password);
    }

    /**
     * Encodes the given password string.
     *
     * @param password The password to be encoded.
     * @return The hashed password value.
     */
    public String encodePassword(final String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * Creates a {@link User} with the given information of the {@link UserDTO}.
     *
     * @param userDTO The DTO containing the information.
     * @return The new user containing the information passed in the DTO.
     */
    private User createUser(final UserDTO userDTO) {
        User user = new User();

        if (userDTO.getId() != null) {
            user.setId(userDTO.getId());
        }
        if (userDTO.getUsername() != null) {
            user.setUsername(userDTO.getUsername());
        }
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail());
        }
        if (userDTO.getRole() != null) {
            user.setRole(userDTO.getRole().toString());
        }
        if (userDTO.getLanguage() != null) {
            user.setLanguage(userDTO.getLanguage().toString());
        }
        if (userDTO.getPassword() != null) {
            user.setPassword(userDTO.getPassword());
        }
        if (userDTO.getSecret() != null) {
            user.setSecret(userDTO.getSecret());
        }

        user.setActive(userDTO.isActive());
        user.setReset(userDTO.isReset());

        return user;
    }

    /**
     * Creates a {@link UserDTO} with the given information from the {@link User}.
     *
     * @param user The user object containing the information.
     * @return The new user DTO containing the information passed in the user object.
     */
    private UserDTO createUserDTO(final User user) {
        UserDTO userDTO = new UserDTO();

        if (user.getId() != null) {
            userDTO.setId(user.getId());
        }
        if (user.getUsername() != null) {
            userDTO.setUsername(user.getUsername());
        }
        if (user.getEmail() != null) {
            userDTO.setEmail(user.getEmail());
        }
        if (user.getRole() != null) {
            userDTO.setRole(UserDTO.Role.valueOf(user.getRole()));
        }
        if (user.getLanguage() != null) {
            userDTO.setLanguage(UserDTO.Language.valueOf(user.getLanguage()));
        }
        if (user.getPassword() != null) {
            userDTO.setPassword(user.getPassword());
        }
        if (user.getSecret() != null) {
            userDTO.setSecret(user.getSecret());
        }

        userDTO.setReset(user.isReset());
        userDTO.setActive(user.isActive());

        return userDTO;
    }

}
