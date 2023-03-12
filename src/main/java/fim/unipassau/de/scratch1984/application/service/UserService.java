package fim.unipassau.de.scratch1984.application.service;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.exception.StoreException;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.Participant;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.projection.UserProjection;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ParticipantRepository;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.util.Secrets;
import fim.unipassau.de.scratch1984.web.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A service providing methods related to users.
 */
@Service
public class UserService {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

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
     * @throws IllegalArgumentException if the passed username is null or blank.
     */
    @Transactional
    public boolean existsUser(final String username) {
        if (username == null || username.trim().isBlank()) {
            throw new IllegalArgumentException("Cannot search for a user with username null or blank!");
        }

        return userRepository.existsByUsername(username);
    }

    /**
     * Checks, whether any user with the given email exists in the database.
     *
     * @param email The email to search for.
     * @return {@code true} if a user exists, or {@code false} if not.
     * @throws IllegalArgumentException if the passed email is null or blank.
     */
    @Transactional
    public boolean existsEmail(final String email) {
        if (email == null || email.trim().isBlank()) {
            throw new IllegalArgumentException("Cannot search for a user with email null or blank!");
        }

        return userRepository.existsByEmail(email);
    }

    /**
     * Checks, whether any participant relation for the given user and experiment IDs exists.
     *
     * @param userId The user id to search for.
     * @param experimentId The experiment id to search for.
     * @return {@code true} if a user exists, or {@code false} if not.
     * @throws IllegalArgumentException if the passed user or experiment ids are invalid.
     */
    @Transactional
    public boolean existsParticipant(final int userId, final int experimentId) {
        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot search for a participant with invalid user id " + userId
                    + " or invalid experiment id " + experimentId + "!");
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
     * @throws IllegalArgumentException if the username is null or blank.
     * @throws StoreException if the user could not be persisted.
     */
    @Transactional
    public UserDTO saveUser(final UserDTO userDTO) {
        if (userDTO.getUsername() == null || userDTO.getUsername().trim().isBlank()) {
            throw new IllegalArgumentException("Cannot create user with username null or blank!");
        }

        User user = userRepository.save(createUser(userDTO));

        if (user.getId() == null) {
            throw new StoreException("Failed to save user with username " + userDTO.getUsername());
        }

        return createUserDTO(user);
    }

    /**
     * Returns the user with the specified username.
     *
     * @param username The username to search for.
     * @return The user, if they exist.
     * @throws IllegalArgumentException if the passed username is null or blank.
     * @throws NotFoundException if no corresponding user entry could be found.
     */
    @Transactional
    public UserDTO getUser(final String username) {
        if (username == null || username.trim().isBlank()) {
            throw new IllegalArgumentException("The username cannot be null or blank!");
        }

        Optional<User> user = userRepository.findUserByUsername(username);

        if (user.isEmpty()) {
            LOGGER.error("Could not find user with username " + username + ".");
            throw new NotFoundException("Could not find user with username " + username + ".");
        }

        return createUserDTO(user.get());
    }

    /**
     * Returns the user with the specified id.
     *
     * @param id The id to search for.
     * @return The user, if they exist.
     * @throws IllegalArgumentException if the passed id is invalid.
     * @throws NotFoundException if no corresponding user entry could be found.
     */
    @Transactional
    public UserDTO getUserById(final int id) {
        if (id < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot search for user with invalid id " + id + "!");
        }

        Optional<User> user = userRepository.findById(id);

        if (user.isEmpty()) {
            LOGGER.error("Could not find user with id " + id + ".");
            throw new NotFoundException("Could not find user with id " + id + ".");
        }

        return createUserDTO(user.get());
    }

    /**
     * Returns the user with the specified email.
     *
     * @param email The email to search for.
     * @return The user, if they exist.
     * @throws IllegalArgumentException if the passed email is null or blank.
     * @throws NotFoundException if no corresponding user entry could be found.
     */
    @Transactional
    public UserDTO getUserByEmail(final String email) {
        if (email == null || email.trim().isBlank()) {
            throw new IllegalArgumentException("Cannot find user with email null or blank!");
        }

        Optional<User> user = userRepository.findByEmail(email);

        if (user.isEmpty()) {
            LOGGER.error("Could not find user with email " + email + ".");
            throw new NotFoundException("Could not find user with email " + email + ".");
        }

        return createUserDTO(user.get());
    }

    /**
     * Returns the {@link UserDTO} whose username or email address matches the specified search string. If no such user
     * exists, {@code null} is returned instead.
     *
     * @param search The username or email to search for.
     * @return The user, if they exist.
     * @throws IllegalArgumentException if the passed search query is null or blank.
     */
    @Transactional
    public UserDTO getUserByUsernameOrEmail(final String search) {
        if (search == null || search.trim().isBlank()) {
            throw new IllegalArgumentException("Cannot search for with search string null or blank!");
        }

        Optional<User> user = userRepository.findUserByUsernameOrEmail(search, search);

        if (user.isEmpty()) {
            LOGGER.debug("Could not find user with username or email " + search + "!");
            return null;
        }

        return createUserDTO(user.get());
    }

    /**
     * Verifies the given user's credentials on login and returns a {@link UserDTO} containing the user's information.
     *
     * @param userDTO The {@link UserDTO} containing the username and password entered in the login form.
     * @return A new {@link UserDTO} containing the user's information stored in the database.
     * @throws NotFoundException if no corresponding user with matching username could be found.
     */
    @Transactional
    public boolean loginUser(final UserDTO userDTO) {
        Optional<User> optionalUser = userRepository.findUserByUsername(userDTO.getUsername());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if ((userDTO.getPassword() != null) && (matchesPassword(userDTO.getPassword(), user.getPassword()))) {
                user.setAttempts(0);
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);
                return true;
            } else {
                int attempts = user.getAttempts() + 1;
                user.setAttempts(attempts);
                userRepository.save(user);
                return false;
            }
        }

        LOGGER.error("Could not find user with username " + userDTO.getUsername() + " in the database.");
        throw new NotFoundException("Incorrect username or password!");
    }

    /**
     * Searches for the user with the given secret, activates their account, and returns a {@link UserDTO} containing
     * the user's information.
     *
     * @param secret The secret to search for.
     * @return A new {@link UserDTO} containing the user's information stored in the database.
     * @throws IllegalArgumentException if the passed secret is null or blank.
     * @throws NotFoundException if no corresponding user entry could be found.
     */
    @Transactional
    public UserDTO authenticateUser(final String secret) {
        if (secret == null || secret.trim().isBlank()) {
            throw new IllegalArgumentException("Cannot search for user with secret null or blank!");
        }

        Optional<User> optionalUser = userRepository.findUserBySecret(secret);

        if (optionalUser.isEmpty()) {
            LOGGER.error("Could not find any user with the secret " + secret + " in the database!");
            throw new NotFoundException("Could not find any user with the secret " + secret + " in the database!");
        }

        User user = optionalUser.get();
        user.setActive(true);
        user.setLastLogin(LocalDateTime.now());
        User saved = userRepository.save(user);
        return createUserDTO(saved);
    }

    /**
     * Updates the information of the given user with the given values.
     *
     * @param userDTO The {@link UserDTO} containing the updated user information.
     * @return The updated user information.
     * @throws IllegalArgumentException if the id of the user is null or invalid.
     */
    @Transactional
    public UserDTO updateUser(final UserDTO userDTO) {
        if (userDTO.getId() == null || userDTO.getId() < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot save user with invalid id " + userDTO.getId() + "!");
        }

        User user = userRepository.save(createUser(userDTO));
        return createUserDTO(user);
    }

    /**
     * Updates the email of the user with the given id to the given value.
     *
     * @param id The user's id.
     * @param email The new email to be set.
     * @throws IllegalArgumentException if the passed id is invalid or the email is null or blank.
     * @throws NotFoundException if no corresponding user entry could be found.
     */
    @Transactional
    public void updateEmail(final int id, final String email) {
        if (id < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot search for user with invalid id " + id + "!");
        } else if (email == null || email.trim().isBlank()) {
            throw new IllegalArgumentException("Cannot update email for user with id " + id
                    + " with email null or blank!");
        }

        Optional<User> user = userRepository.findById(id);

        if (user.isEmpty()) {
            LOGGER.error("Could not find user with id " + id + " in the database!");
            throw new NotFoundException("Could not find user with id " + id + " in the database!");
        }

        User found = user.get();
        found.setEmail(email);
        userRepository.save(found);
    }

    /**
     * Deactivates all participant accounts where participants have not logged in for a specified number of days.
     */
    @Transactional
    public void deactivateOldParticipantAccounts() {
        List<User> inactiveUsers = userRepository.findAllByRoleAndLastLoginBefore(UserDTO.Role.PARTICIPANT.toString(),
                LocalDateTime.now().minusDays(Constants.PARTICIPANT_INACTIVE_DAYS));

        for (User user : inactiveUsers) {
            user.setActive(false);
            user.setSecret(null);
            userRepository.save(user);
        }
    }

    /**
     * Reactivates the accounts of participants who have not finished an experiment when it is being reopened. If the
     * participant is not currently participating in a different experiment, their account is activated and a secret
     * generated. The list of the updated and reactivated users is then passed to the controller to send out new
     * invitation mails.
     *
     * @param experimentId The experiment id to search for.
     * @return A list of {@link UserDTO}s.
     * @throws IllegalArgumentException if the passed id is invalid.
     * @throws NotFoundException if no corresponding experiment could be found.
     */
    @Transactional
    public List<UserDTO> reactivateUserAccounts(final int experimentId) {
        if (experimentId < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot search for user with invalid experiment id " + experimentId
                    + "!");
        }

        return findUnfinishedParticipants(experimentId).stream().map(participant
                -> activateParticipantAccount(participant, experimentId)).collect(Collectors.toList());
    }

    /**
     * Retrieves a list of {@link UserDTO}s who have not yet finished the experiment with the given id.
     *
     * @param experimentId The experiment id to search for.
     * @return The list of users.
     * @throws IllegalArgumentException if the passed id is invalid.
     * @throws NotFoundException if no corresponding experiment could be found.
     */
    @Transactional
    public List<UserDTO> findUnfinishedUsers(final int experimentId) {
        if (experimentId < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot search for reactivated user accounts with invalid experiment id "
                    + experimentId + "!");
        }

        return findUnfinishedParticipants(experimentId).stream().map(participant
                -> createUserDTO(participant.getUser())).collect(Collectors.toList());
    }

    /**
     * Verifies how many users with administrator status are currently registered.
     *
     * @return {@code true} if only one administrator remains in the database, or {@code false} otherwise.
     * @throws IllegalStateException if no administrator could be found.
     */
    @Transactional
    public boolean isLastAdmin() {
        List<User> admins = userRepository.findAllByRole(UserDTO.Role.ADMIN.toString());

        if (admins.size() < 1) {
            throw new IllegalStateException("There are no users with administrator status in the database!");
        }

        return admins.size() == 1;
    }

    /**
     * Deletes the user with the given id from the database, if any such user exists.
     *
     * @param id The id to search for.
     * @throws IllegalArgumentException if the passed id is invalid.
     */
    @Transactional
    public void deleteUser(final int id) {
        if (id < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot delete user with invalid id " + id + "!");
        }

        userRepository.deleteById(id);
    }

    /**
     * Returns the current highest user id value.
     *
     * @return The id.
     * @throws IllegalStateException if no users could be found.
     */
    @Transactional
    public int findLastId() {
        Optional<User> user = userRepository.findFirstByOrderByIdDesc();

        if (user.isEmpty()) {
            throw new IllegalStateException("There are no users in the database!");
        }

        return user.get().getId();
    }

    /**
     * Searches for the user whose username starts with the given username string and ends with the highest number
     * found. The number at the end of the username is then incremented and returned. If no corresponding user could be
     * found, or the username does not end with a digit, 1 is returned instead.
     *
     * @param username The username pattern to search for.
     * @return The number at the end of the retrieved username, or 1.
     * @throws IllegalArgumentException if the passed username is null or blank.
     */
    @Transactional
    public int findValidNumberForUsername(final String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Cannot search for matching username with username null or blank!");
        }

        Optional<UserProjection> user = userRepository.findLastUsername(username);

        if (user.isEmpty()) {
            LOGGER.debug("Couldn't find username starting with " + username + ".");
            return 1;
        } else {
            String name = user.get().getUsername();
            int position = getFirstDigitPositionAtEnd(name);
            return position == name.length() ? 1 : Integer.parseInt(name.substring(position)) + 1;
        }
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
     * Returns the position of the first digit at the end of the string after which only more numbers occur, if any.
     *
     * @param username The username to check.
     * @return The position of the last digit, or the length of the string, if the last character is not a digit.
     */
    private int getFirstDigitPositionAtEnd(final String username) {
        int pos;

        for (pos = username.length() - 1; pos >= 0; pos--) {
            if (!Character.isDigit(username.charAt(pos))) {
                break;
            }
        }

        return pos + 1;
    }

    /**
     * Activates the user account linked to the given {@link Participant} and generates a secret, if the user does not
     * yet have one. Finally, the updated {@link UserDTO} is returned.
     *
     * @param participant The participant whose account is to be activated.
     * @param experimentId The id of the experiment in which the user is participating.
     * @return The updated user information.
     * @throws IllegalStateException if the id of the user is null.
     */
    private UserDTO activateParticipantAccount(final Participant participant, final int experimentId) {
        User user = participant.getUser();

        if (user.getId() == null) {
            throw new IllegalStateException("Could not find corresponding user for participant entry for experiment"
                    + " with id " + experimentId + "!");
        }

        if (user.getSecret() == null) {
            user.setSecret(Secrets.generateRandomBytes(Constants.SECRET_LENGTH));
        }

        user.setActive(true);
        userRepository.save(user);
        return createUserDTO(user);
    }

    /**
     * Returns a list of all participants who have not yet finished the experiment with the given id.
     *
     * @param experimentId The id of the experiment.
     * @return The list of participants.
     * @throws NotFoundException if no corresponding experiment could be found.
     */
    private List<Participant> findUnfinishedParticipants(final int experimentId) {
        Experiment experiment = experimentRepository.getOne(experimentId);
        List<Participant> participants;

        try {
            participants = participantRepository.findAllByExperimentAndEnd(experiment, null);
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find experiment with id " + experimentId + "!", e);
            throw new NotFoundException("Could not find experiment with id " + experimentId + "!", e);
        }

        return participants;
    }

    /**
     * Creates a {@link User} with the given information of the {@link UserDTO}.
     *
     * @param userDTO The DTO containing the information.
     * @return The new user containing the information passed in the DTO.
     */
    private User createUser(final UserDTO userDTO) {
        User user = User.builder()
                .username(userDTO.getUsername())
                .role(userDTO.getRole().toString())
                .language(userDTO.getLanguage().toString())
                .active(userDTO.isActive())
                .attempts(userDTO.getAttempts())
                .build();

        if (userDTO.getId() != null) {
            user.setId(userDTO.getId());
        }
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail());
        }
        if (userDTO.getPassword() != null) {
            user.setPassword(userDTO.getPassword());
        }
        if (userDTO.getSecret() != null) {
            user.setSecret(userDTO.getSecret());
        }
        if (userDTO.getLastLogin() != null) {
            user.setLastLogin(userDTO.getLastLogin());
        }

        return user;
    }

    /**
     * Creates a {@link UserDTO} with the given information from the {@link User}.
     *
     * @param user The user object containing the information.
     * @return The new user DTO containing the information passed in the user object.
     */
    private UserDTO createUserDTO(final User user) {
        UserDTO userDTO = UserDTO.builder()
                .username(user.getUsername())
                .role(UserDTO.Role.valueOf(user.getRole()))
                .language(UserDTO.Language.valueOf(user.getLanguage()))
                .active(user.isActive())
                .attempts(user.getAttempts())
                .build();

        if (user.getId() != null) {
            userDTO.setId(user.getId());
        }
        if (user.getEmail() != null) {
            userDTO.setEmail(user.getEmail());
        }
        if (user.getPassword() != null) {
            userDTO.setPassword(user.getPassword());
        }
        if (user.getSecret() != null) {
            userDTO.setSecret(user.getSecret());
        }
        if (user.getLastLogin() != null) {
            userDTO.setLastLogin(user.getLastLogin());
        }

        return userDTO;
    }

}
