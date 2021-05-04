package fim.unipassau.de.scratch1984.application.service;

import fim.unipassau.de.scratch1984.persistence.entity.Participant;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.repository.ParticipantRepository;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
import fim.unipassau.de.scratch1984.web.dto.ExperimentDTO;
import fim.unipassau.de.scratch1984.web.dto.ParticipantDTO;
import fim.unipassau.de.scratch1984.web.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A service providing methods related to users and experiment participation.
 */
@Service
public class UserService {

    /**
     * The user repository to use for database queries related to user data.
     */
    private final UserRepository userRepository;

    /**
     * The participant repository to use for database queries participation data.
     */
    private final ParticipantRepository participantRepository;

    /**
     * Constructs a user service with the given dependencies.
     *
     * @param userRepository The user repository to use.
     * @param participantRepository The participant repository to use.
     */
    @Autowired
    public UserService(final UserRepository userRepository, final ParticipantRepository participantRepository) {
        this.userRepository = userRepository;
        this.participantRepository = participantRepository;
    }

    /**
     * Checks, whether any user with the given username exists in the database.
     *
     * @param username The username to search for.
     * @return {@code true} if a user exists, or {@code false} if not.
     */
    public boolean existsUser(final String username) {
        return false;
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
     * Returns the user with the specified username. If no such user exists, returns {@code null}.
     *
     * @param username The username to search for.
     * @return The user, if they exist, or {@code null} if no user with that username exists.
     */
    public UserDTO getUser(final String username) {
        return null;
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
        return null;
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
