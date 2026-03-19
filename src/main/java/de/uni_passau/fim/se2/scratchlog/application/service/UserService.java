/*
 * Copyright (C) 2023 ScratchLog contributors
 *
 * This file is part of ScratchLog.
 *
 * ScratchLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * ScratchLog is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ScratchLog. If not, see <http://www.gnu.org/licenses/>.
 */

package de.uni_passau.fim.se2.scratchlog.application.service;

import com.opencsv.bean.CsvToBeanBuilder;
import de.uni_passau.fim.se2.scratchlog.application.exception.NotFoundException;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Experiment;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Participant;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ExperimentRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ParticipantRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.UserRepository;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import de.uni_passau.fim.se2.scratchlog.util.CustomPasswordGenerator;
import de.uni_passau.fim.se2.scratchlog.util.InactivityConfiguration;
import de.uni_passau.fim.se2.scratchlog.util.Secrets;
import de.uni_passau.fim.se2.scratchlog.util.enums.Role;
import de.uni_passau.fim.se2.scratchlog.util.validation.annotation.ValidFile;
import de.uni_passau.fim.se2.scratchlog.web.dto.UserBulkDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.UserDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A service providing methods related to users.
 */
@Service
@Validated
public class UserService {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    /**
     * The inactivity configuration.
     */
    private final InactivityConfiguration inactivityConfiguration;

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
     * @param inactivityConfiguration The inactivity configuration.
     * @param userRepository The user repository to use.
     * @param participantRepository The participant repository to use.
     * @param experimentRepository The experiment repository to use.
     * @param passwordEncoder The password encoder to use.
     */
    @Autowired
    public UserService(final InactivityConfiguration inactivityConfiguration,
                       final UserRepository userRepository,
                       final ParticipantRepository participantRepository,
                       final ExperimentRepository experimentRepository,
                       final PasswordEncoder passwordEncoder) {
        this.inactivityConfiguration = inactivityConfiguration;
        this.userRepository = userRepository;
        this.participantRepository = participantRepository;
        this.experimentRepository = experimentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Finds all users that already exist in the database.
     *
     * @param users Some new users.
     * @return The usernames of the users that already exist, either because the username or email is already in use.
     */
    public Set<String> findAlreadyExistingByUsernameOrEmail(final List<UserDTO> users) {
        final Set<String> usernames = users.stream()
            .map(UserDTO::getUsername)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        final Set<String> emails = users.stream()
            .map(UserDTO::getEmail)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        return userRepository.findAllByUsernameOrEmailExisting(usernames, emails);
    }

    /**
     * Checks, whether any user with the given username exists in the database.
     *
     * @param username The username to search for.
     * @return {@code true} if a user exists, or {@code false} if not.
     * @throws IllegalArgumentException if the passed username is null or blank.
     */
    public boolean existsUser(final String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Checks, whether any user with the given email exists in the database.
     *
     * @param email The email to search for.
     * @return {@code true} if a user exists, or {@code false} if not.
     * @throws IllegalArgumentException if the passed email is null or blank.
     */
    public boolean existsEmail(final String email) {
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
    public boolean existsParticipant(final int userId, final int experimentId) {
        User user = userRepository.getReferenceById(userId);
        Experiment experiment = experimentRepository.getReferenceById(experimentId);

        try {
            return participantRepository.existsByUserAndExperiment(user, experiment);
        } catch (EntityNotFoundException e) {
            return false;
        }
    }

    /**
     * Checks, whether an administrator with the given username exists in the database.
     *
     * @param username The username to search for.
     * @return {@code true} if such a user exists, or {@code false} otherwise.
     * @throws IllegalArgumentException if the passed username is null or blank.
     */
    public boolean isAdmin(final String username) {
        return userRepository.existsByRoleAndUsername(Role.ADMIN, username);
    }

    /**
     * Verifies how many users with administrator status are currently registered.
     *
     * @return {@code true} if only one administrator remains in the database, or {@code false} otherwise.
     * @throws IllegalStateException if no administrator could be found.
     */
    public boolean isLastAdmin() {
        List<User> admins = userRepository.findAllByRole(Role.ADMIN);

        if (admins.isEmpty()) {
            throw new IllegalStateException("There are no users with administrator status in the database!");
        }

        return admins.size() == 1;
    }

    /**
     * Creates new users for each user contained in the given list.
     *
     * @param userDTOS The list of users to be saved.
     * @return The list of persisted users.
     */
    public List<UserDTO> saveUsers(final List<UserDTO> userDTOS) {
        List<User> users = userDTOS.stream().map(this::createUser).toList();
        return userRepository.saveAll(users).stream().map(this::createUserDTO).toList();
    }

    /**
     * Creates a new user with the given parameters in the database.
     *
     * @param userDTO The dto containing the user information to set.
     * @return The newly created user, if the information was persisted.
     * @throws IllegalArgumentException if the username is null or blank.
     */
    @Transactional
    public UserDTO saveUser(final UserDTO userDTO) {
        if (userDTO.getUsername() == null || userDTO.getUsername().trim().isBlank()) {
            throw new IllegalArgumentException("Cannot create user with username null or blank!");
        }

        User user = userRepository.save(createUser(userDTO));
        return createUserDTO(user);
    }

    /**
     * Adds multiple users in bulk to the database according to the data in {@code userBulkDTO}. If not starting at one,
     * the user id is used as distinction in the usernames. If starting at one, starts the numbering at one if possible,
     * else starts numbering at the current maximum number plus one.
     *
     * @param userBulkDTO The {@link UserBulkDTO} containing the necessary information.
     * @return A list of all users that were added.
     */
    @Transactional
    public List<UserDTO> addUsersInBulk(@NotNull @Valid final UserBulkDTO userBulkDTO) {
        String username = userBulkDTO.getUsername();
        int number = userBulkDTO.isStartAtOne() ? findValidNumberForUsername(username) : findLastId() + 1;

        List<UserDTO> usersToAdd = new ArrayList<>();
        for (int i = 0; i < userBulkDTO.getAmount(); i++) {
            // Should always be safe to add since we always take a new number (either a fresh id or the maximum suffix
            // number plus 1).
            UserDTO userDTO = new UserDTO(username + number, null, Role.PARTICIPANT,
                userBulkDTO.getLanguage(), null, null);
            completeUserInformation(userDTO);
            usersToAdd.add(userDTO);

            number++;
        }

        saveUsers(usersToAdd);
        return usersToAdd;
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
            LOGGER.error("Could not find user with username {}.", username);
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
    public UserDTO getUserById(final int id) {
        Optional<User> user = userRepository.findById(id);

        if (user.isEmpty()) {
            LOGGER.error("Could not find user with id {}.", id);
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
    public UserDTO getUserByEmail(final String email) {
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isEmpty()) {
            LOGGER.error("Could not find user with email {}.", email);
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
    public UserDTO getUserByUsernameOrEmail(final String search) {
        Optional<User> user = userRepository.findUserByUsernameOrEmail(search, search);

        if (user.isEmpty()) {
            LOGGER.debug("Could not find user with username or email {}!", search);
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

        LOGGER.error("Could not find user with username {} in the database.", userDTO.getUsername());
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
            LOGGER.error("Could not find any user with the secret {} in the database!", secret);
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
        if (userDTO.getId() == null) {
            throw new IllegalArgumentException("Cannot update fresh user!");
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
        if (email == null || email.trim().isBlank()) {
            throw new IllegalArgumentException("Cannot update email for user with id " + id
                    + " with email null or blank!");
        }

        Optional<User> user = userRepository.findById(id);

        if (user.isEmpty()) {
            LOGGER.error("Could not find user with id {} in the database!", id);
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
        List<User> inactiveUsers = userRepository.findAllByRoleAndLastLoginBefore(Role.PARTICIPANT,
                LocalDateTime.now().minusDays(inactivityConfiguration.getDisableInactiveAccountAfterDays()));

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
        return findUnfinishedParticipants(experimentId).stream().map(participant
                -> activateParticipantAccount(participant, experimentId)).collect(Collectors.toList());
    }

    /**
     * Retrieves a list of active {@link UserDTO}s who have not yet finished the experiment with the given id.
     *
     * @param experimentId The experiment id to search for.
     * @return The list of users.
     * @throws IllegalArgumentException if the passed id is invalid.
     * @throws NotFoundException if no corresponding experiment could be found.
     */
    public List<UserDTO> findUnfinishedUsers(final int experimentId) {
        Stream<UserDTO> participants = findUnfinishedParticipants(experimentId).stream().map(participant
                -> createUserDTO(participant.getUser()));
        return participants.filter(userDTO -> userDTO.isActive() && userDTO.getSecret() != null).collect(
                Collectors.toList());
    }

    /**
     * Deletes the user with the given id from the database, if any such user exists.
     *
     * @param id The id to search for.
     * @throws IllegalArgumentException if the passed id is invalid.
     */
    @Transactional
    public void deleteUser(final int id) {
        userRepository.deleteById(id);
    }

    /**
     * Returns the current highest user id value.
     *
     * @return The id.
     * @throws IllegalStateException if no users could be found.
     */
    public int findLastId() {
        Optional<User> user = userRepository.findFirstByOrderByIdDesc();

        if (user.isEmpty()) {
            throw new IllegalStateException("There are no users in the database!");
        }

        return user.get().getId();
    }

    /**
     * Determines a valid number for a new user with a username starting with the given {@code username}.
     * This is the maximum number that occurs after the given username across all usernames in the database, plus 1.
     * Also 1 in case the pattern is not currently used by any username.
     *
     * @param usernamePattern The username pattern to search for.
     * @return A valid distinction number for a new user with the given username.
     * @throws IllegalArgumentException if the passed username is null or blank.
     */
    private int findValidNumberForUsername(final String usernamePattern) {
        if (usernamePattern == null || usernamePattern.isBlank()) {
            throw new IllegalArgumentException("Cannot search for matching username with username null or blank!");
        }

        Set<String> matchingUsernames = userRepository.getUsernamesWithPrefix(usernamePattern);

        int maxNumber = 0;
        int prefixLength = usernamePattern.length();
        for (String username : matchingUsernames) {
            String numberStr = username.substring(prefixLength);

            try {
                int number = Integer.parseInt(numberStr);
                maxNumber = Math.max(maxNumber, number);
            } catch (NumberFormatException e) {
                // Ignore, since failed parsing means the username isn't exactly the searched for pattern.
            }
        }

        return maxNumber + 1;
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
     * Parse the given CSV file into a list of {@link UserDTO}s.
     *
     * @param file The CSV file to parse user information from.
     * @return A list of user DTO objects with the information provided in the CSV file.
     * @throws IOException If the CSV file could not be read.
     */
    public List<UserDTO> parseUserListCsv(
        @NotNull @ValidFile(contentTypes = "text/csv", fileEndings = {"csv"}) final MultipartFile file)
        throws IOException {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            return new CsvToBeanBuilder<UserDTO>(reader).withType(UserDTO.class).build().parse();
        }
    }

    /**
     * Returns the usernames of the users in the given list who would not be valid participants to add to a course or
     * experiment. This includes administrators and users that don't exist.
     *
     * @param users The list of users to filter for invalid usernames.
     * @return The list of invalid usernames according to the above criteria.
     */
    public List<String> getInvalidParticipantUsernames(final List<UserDTO> users) {
        List<String> invalidUsernames = new ArrayList<>();

        users.forEach(userDTO -> {
            if (!existsUser(userDTO.getUsername()) || isAdmin(userDTO.getUsername())) {
                invalidUsernames.add(userDTO.getUsername());
            }
        });

        return invalidUsernames;
    }

    /**
     * Completes the data of a {@link UserDTO} so that it can be persisted in the database. This sets the password to a
     * random password if not set, marks the user as active and sets their last login to the current timestamp.
     * If the language is unset, it is set to the default. If the role is unset, it is set to participant.
     * The new data is written in-place. The encoded password will be stored in the password field, whereas the
     * plaintext password will be stored in the confirmPassword field.
     *
     * @param userDTO The user DTO to fill with additional information.
     */
    public void completeUserInformation(@NotNull final UserDTO userDTO) {
        String password = userDTO.getPassword();
        if (userDTO.getPassword() == null) {
            password = CustomPasswordGenerator.generatePassword(Constants.PASSWORD_MIN);
        }

        userDTO.setPassword(encodePassword(password));
        userDTO.setConfirmPassword(password);
        userDTO.setActive(true);
        userDTO.setLastLogin(LocalDateTime.now());

        if (userDTO.getLanguage() == null) {
            userDTO.setLanguage(Constants.DEFAULT_LANGUAGE);
        }
        if (userDTO.getRole() == null) {
            userDTO.setRole(Role.PARTICIPANT);
        }
    }

    /**
     * Generates the contents of a CSV file consisting of two columns with the usernames and passwords of each user.
     * Assumes the plaintext password is stored in the confirmPassword field.
     *
     * @param userDTOs The list of users to generate the CSV for. May not be {@code null} and all have non-{@code}
     *                 username and password.
     * @return The generated CSV string.
     */
    public String generateUsernamePasswordCsv(final List<UserDTO> userDTOs) {
        if (userDTOs == null) {
            throw new IllegalArgumentException("Users list may not be null.");
        }

        StringBuilder builder = new StringBuilder("username,password" + System.lineSeparator());
        for (UserDTO userDTO : userDTOs) {
            if (userDTO.getUsername() == null || userDTO.getConfirmPassword() == null) {
                throw new IllegalArgumentException("Username or password may not be null.");
            }

            builder
                .append(userDTO.getUsername())
                .append(",")
                .append(userDTO.getConfirmPassword())
                .append(System.lineSeparator());
        }

        return builder.toString();
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
        Experiment experiment = experimentRepository.getReferenceById(experimentId);
        List<Participant> participants;

        try {
            participants = participantRepository.findAllByExperimentAndEnd(experiment, null);
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find experiment with id {}!", experimentId, e);
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
                .role(userDTO.getRole())
                .language(userDTO.getLanguage())
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
                .role(user.getRole())
                .language(user.getLanguage())
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
