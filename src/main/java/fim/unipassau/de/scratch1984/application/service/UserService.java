package fim.unipassau.de.scratch1984.application.service;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.persistence.entity.Participant;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.repository.ParticipantRepository;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
import fim.unipassau.de.scratch1984.web.dto.ExperimentDTO;
import fim.unipassau.de.scratch1984.web.dto.ParticipantDTO;
import fim.unipassau.de.scratch1984.web.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * A service providing methods related to users and experiment participation.
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
     * @param passwordEncoder The password encoder to use.
     */
    @Autowired
    public UserService(final UserRepository userRepository, final ParticipantRepository participantRepository,
                       final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.participantRepository = participantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Checks, whether any user with the given username exists in the database.
     *
     * @param username The username to search for.
     * @return {@code true} if a user exists, or {@code false} if not.
     */
    public boolean existsUser(final String username) {
        if (username == null || username.trim().isBlank()) {
            return false;
        }

        return userRepository.existsByUsername(username);
    }

    /**
     * Creates a new user with the given parameters in the database.
     *
     * @param userDTO The dto containing the user information to set.
     * @return The newly created user, if the information was persisted, or {@code null} if not.
     */
    public UserDTO saveUser(final UserDTO userDTO) {
        return null;
    }

    /**
     * Creates a new participation for the given user and experiment in the database.
     *
     * @param userDTO The dto containing the user information.
     * @param experimentDTO The dto containing the experiment information.
     * @return {@code true} if the information was persisted, or {@code false} if not.
     */
    public boolean saveParticipant(final UserDTO userDTO, final ExperimentDTO experimentDTO) {
        return false;
    }

    /**
     * Returns the user with the specified username. If no such user exists, a {@link NotFoundException} is thrown
     * instead.
     *
     * @param username The username to search for.
     * @return The user, if they exist.
     */
    public UserDTO getUser(final String username) {
        System.out.println("Searching for user with username " + username);
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
     * Verifies the given user's credentials on login and returns an {@link UserDTO} containing the user's information.
     * If no user with matching username and password could be found, a {@link NotFoundException} is thrown instead.
     *
     * @param userDTO The {@link UserDTO} containing the username and password entered in the login form.
     * @return A new {@link UserDTO} containing the user's information stored in the database.
     */
    public UserDTO loginUser(final UserDTO userDTO) {
        User user = userRepository.findUserByUsername(userDTO.getUsername());

        if (user != null) {
            if ((userDTO.getPassword() != null) && (passwordEncoder.matches(userDTO.getPassword(),
                    user.getPassword()))) {
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
     * Updates the information of the given user with the given values, or creates a new user, if no such user exists.
     *
     * @param userDTO The dto containing the updated user information.
     * @return {@code true} if the information was persisted, or {@code false} if not.
     */
    public boolean updateUser(final UserDTO userDTO) {
        return false;
    }

    /**
     * Updates the participation information with the given values.
     *
     * @param participantDTO The dto containing the updated participation information.
     * @return {@code true} if the information was persisted, or {@code false} if not.
     */
    public boolean updateParticipant(final ParticipantDTO participantDTO) {
        return false;
    }

    /**
     * Deletes the user with the given username from the database, if any such user exists.
     *
     * @param username The username to search for.
     * @return {@code true} if the deletion was successful, or {@code false} if not.
     */
    public boolean deleteUser(final String username) {
        return false;
    }

    /**
     * Creates a {@link User} with the given information of the {@link UserDTO}.
     *
     * @param userDTO The dto containing the information.
     * @return The new user containing the information passed in the DTO.
     */
    private User createUser(final UserDTO userDTO) {
        return null;
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

    /**
     * Creates a {@link Participant} with the given information of the {@link ParticipantDTO}.
     *
     * @param participantDTO The dto containing the information.
     * @return The new participant containing the information passed in the DTO.
     */
    private Participant createParticipant(final ParticipantDTO participantDTO) {
        return null;
    }

    /**
     * Creates a {@link ParticipantDTO} with the given information from the {@link Participant}.
     *
     * @param participant The participant object containing the information.
     * @return The new participant DTO containing the information passed in the participant object.
     */
    private ParticipantDTO createParticipantDTO(final Participant participant) {
        return null;
    }

}
