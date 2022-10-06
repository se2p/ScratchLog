package fim.unipassau.de.scratch1984.application.service;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.exception.StoreException;
import fim.unipassau.de.scratch1984.persistence.entity.Course;
import fim.unipassau.de.scratch1984.persistence.entity.CourseParticipant;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.Participant;
import fim.unipassau.de.scratch1984.persistence.entity.ParticipantId;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.repository.CourseParticipantRepository;
import fim.unipassau.de.scratch1984.persistence.repository.CourseRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ParticipantRepository;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.util.Secrets;
import fim.unipassau.de.scratch1984.web.dto.ParticipantDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * A service providing methods related experiment participation.
 */
@Service
public class ParticipantService {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger logger = LoggerFactory.getLogger(ParticipantService.class);

    /**
     * The user repository to use for database queries related to user data.
     */
    private final UserRepository userRepository;

    /**
     * The experiment repository to use for database queries related to experiment data.
     */
    private final ExperimentRepository experimentRepository;

    /**
     * The course repository to use for database queries related to course data.
     */
    private final CourseRepository courseRepository;

    /**
     * The course participant repository to use for database queries related to course participant data.
     */
    private final CourseParticipantRepository courseParticipantRepository;

    /**
     * The participant repository to use for database queries participation data.
     */
    private final ParticipantRepository participantRepository;

    /**
     * Constructs a participant service with the given dependencies.
     *
     * @param userRepository The {@link UserRepository} to use.
     * @param participantRepository The {@link ParticipantRepository} to use.
     * @param courseRepository The {@link CourseRepository} to use.
     * @param courseParticipantRepository The {@link CourseParticipantRepository} to use.
     * @param experimentRepository The {@link ExperimentRepository} to use.
     */
    @Autowired
    public ParticipantService(final UserRepository userRepository, final ParticipantRepository participantRepository,
                              final CourseRepository courseRepository,
                              final CourseParticipantRepository courseParticipantRepository,
                              final ExperimentRepository experimentRepository) {
        this.userRepository = userRepository;
        this.participantRepository = participantRepository;
        this.courseRepository = courseRepository;
        this.courseParticipantRepository = courseParticipantRepository;
        this.experimentRepository = experimentRepository;
    }

    /**
     * Retrieves the participant information for the given experiment id and the given user id from the database. If no
     * corresponding user, experiment or participant can be found, a {@link NotFoundException} is thrown instead.
     *
     * @param experimentId The experiment id.
     * @param userId The user id.
     * @return The {@link ParticipantDTO} containing the participant information.
     */
    @Transactional
    public ParticipantDTO getParticipant(final int experimentId, final int userId) {
        if (experimentId < Constants.MIN_ID || userId < Constants.MIN_ID) {
            logger.error("Cannot search for participant with invalid experiment id " + experimentId
                    + " or invalid user id " + userId + "!");
            throw new IllegalArgumentException("Cannot search for participant with invalid experiment id "
                    + experimentId + " or invalid user id " + userId + "!");
        }

        User user = userRepository.getOne(userId);
        Experiment experiment = experimentRepository.getOne(experimentId);

        try {
            Participant participant = participantRepository.findByUserAndExperiment(user, experiment);

            if (participant == null) {
                logger.error("Could not find any participant entry for user with id " + userId + " for experiment with "
                        + "id " + experimentId + "!");
                throw new NotFoundException("Could not find any participant entry for user with id " + userId
                        + " for experiment with id " + experimentId + "!");
            }

            return createParticipantDTO(participant);
        } catch (EntityNotFoundException e) {
            logger.error("Could not find user with id " + userId + " or experiment with id " + experimentId
                    + " in the database!", e);
            throw new NotFoundException("Could not find user with id " + userId + " or experiment with id "
                    + experimentId + " in the database!", e);
        }
    }

    /**
     * Adds the users participating in the course with the given id as participants to the experiment with the given id.
     * If no corresponding course or experiment could be found, an {@link EntityNotFoundException} is thrown instead. If
     * the user or experiment used to create a new participation violate the foreign key constraints of the participant
     * table, a {@link ConstraintViolationException} is thrown. If the given ids are invalid, an
     * {@link IllegalArgumentException} is thrown.
     *
     * @param experimentId The id of the experiment.
     * @param courseId The id of the course.
     */
    @Transactional
    public void saveParticipants(final int experimentId, final int courseId) {
        if (experimentId < Constants.MIN_ID || courseId < Constants.MIN_ID) {
            logger.error("Cannot add participants to course experiment with invalid experiment id " + experimentId
                    + " or invalid course id " + courseId + "!");
            throw new IllegalArgumentException("Cannot add participants to course experiment with invalid experiment "
                    + "id " + experimentId + " or invalid course id " + courseId + "!");
        }

        Course course = courseRepository.getOne(courseId);
        Experiment experiment = experimentRepository.getOne(experimentId);

        try {
            if (!course.isActive() || !experiment.isActive()) {
                logger.error("Cannot add participants to inactive course or experiment!");
                throw new IllegalStateException("Cannot add participants to inactive course or experiment!");
            } else if (!experiment.isCourseExperiment()) {
                logger.error("Cannot add course participants to experiment which is not part of the course!");
                throw new IllegalStateException("Cannot add course participants to experiment which is not part of the "
                        + "course!");
            }

            List<CourseParticipant> courseParticipants = courseParticipantRepository.findAllByCourse(course);
            courseParticipants.forEach(courseParticipant -> addCourseParticipantToExperiment(courseParticipant,
                    experiment));
        } catch (EntityNotFoundException e) {
            logger.error("Could not find the course or experiment data when trying to save course participants!", e);
            throw new NotFoundException("Could not find the course or experiment data when trying to save course "
                    + "participants!", e);
        } catch (ConstraintViolationException e) {
            logger.error("The given participant data does not meet the foreign key constraints!", e);
            throw new StoreException("The given participant data does not meet the foreign key constraints!", e);
        }
    }

    /**
     * Creates a new participation for the given user and experiment in the database.
     *
     * @param userId The user id.
     * @param experimentId The experiment id.
     */
    @Transactional
    public void saveParticipant(final int userId, final int experimentId) {
        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            logger.error("Cannot add participant with invalid user id " + userId + " or invalid experiment id "
                    + experimentId + "!");
            throw new IllegalArgumentException("Cannot add participant with invalid user id " + userId
                    + " or invalid experiment id " + experimentId + "!");
        }

        User user = userRepository.getOne(userId);
        Experiment experiment = experimentRepository.getOne(experimentId);

        try {
            Participant participant = new Participant(user, experiment, null, null);
            participantRepository.save(participant);
        } catch (EntityNotFoundException e) {
            logger.error("Could not find the user or experiment when saving the participant data!", e);
            throw new NotFoundException("Could not find the user or experiment when saving the participant data!", e);
        } catch (ConstraintViolationException e) {
            logger.error("The given participant data does not meet the foreign key constraints!", e);
            throw new StoreException("The given participant data does not meet the foreign key constraints!", e);
        }
    }

    /**
     * Updates the participation information with the given values.
     *
     * @param participantDTO The dto containing the updated participation information.
     * @return {@code true} if the information was persisted, or {@code false} if not.
     */
    @Transactional
    public boolean updateParticipant(final ParticipantDTO participantDTO) {
        if (participantDTO.getExperiment() < Constants.MIN_ID || participantDTO.getUser() < Constants.MIN_ID) {
            logger.error("Cannot search for participant with invalid experiment id " + participantDTO.getExperiment()
                    + " or invalid user id " + participantDTO.getUser() + "!");
            throw new IllegalArgumentException("Cannot search for participant with invalid experiment id "
                    + participantDTO.getExperiment() + " or invalid user id " + participantDTO.getUser() + "!");
        }

        User user = userRepository.getOne(participantDTO.getUser());
        Experiment experiment = experimentRepository.getOne(participantDTO.getExperiment());

        try {
            participantRepository.save(createParticipant(participantDTO, user, experiment));
            return true;
        } catch (EntityNotFoundException e) {
            logger.error("Could not find the user with id " + participantDTO.getUser() + " or experiment with id "
                    + participantDTO.getExperiment() + " when trying to update a participant!", e);
        } catch (ConstraintViolationException e) {
            logger.error("No participant entry could be found for user with id " + participantDTO.getUser()
                    + " for experiment with id " + participantDTO.getExperiment() + "!", e);
        }

        return false;
    }

    /**
     * Retrieves a list of participant ids for the experiment with the given id. If no corresponding experiment exists
     * in the database a {@link NotFoundException} is thrown instead.
     *
     * @param experimentId The experiment id.
     */
    @Transactional
    public void deactivateParticipantAccounts(final int experimentId) {
        if (experimentId < Constants.MIN_ID) {
            logger.error("Cannot find participant data for experiment with invalid id " + experimentId + "!");
            throw new IllegalArgumentException("Cannot find participant data for experiment with invalid id "
                    + experimentId + "!");
        }

        List<Participant> participants;
        Experiment experiment = experimentRepository.getOne(experimentId);

        try {
            participants = participantRepository.findAllByExperiment(experiment);
        } catch (EntityNotFoundException e) {
            logger.error("Could not find experiment with id " + experimentId + "!");
            throw new NotFoundException("Could not find experiment with id " + experimentId + "!");
        }

        for (Participant participant : participants) {
            Optional<User> user = userRepository.findById(participant.getUser().getId());

            if (user.isEmpty()) {
                logger.error("No user entry could be found for user with id " + participant.getUser().getId()
                        + " corresponding to the participant entry for experiment with id " + experimentId + "!");
                throw new IllegalStateException("No user entry could be found for user with id "
                        + participant.getUser().getId() + " corresponding to the participant entry for experiment with "
                        + "id " + experimentId + "!");
            } else if (!simultaneousParticipation(user.get().getId())) {
                User found = user.get();
                found.setActive(false);
                found.setSecret(null);
                userRepository.save(found);
            }
        }
    }

    /**
     * Retrieves the experiment ids and titles of the experiments in which the user with the given user id is
     * participating. If no corresponding user exists in the database a {@link NotFoundException} is thrown instead.
     *
     * @param userId The user id.
     * @return The list of experiment ids.
     */
    @Transactional
    public HashMap<Integer, String> getExperimentInfoForParticipant(final int userId) {
        if (userId < Constants.MIN_ID) {
            logger.error("Cannot find participant data for user with invalid id " + userId + "!");
            throw new IllegalArgumentException("Cannot find participant data for user with invalid id "
                    + userId + "!");
        }

        List<Participant> participants;
        HashMap<Integer, String> experiments = new HashMap<>();
        User user = userRepository.getOne(userId);

        try {
            participants = participantRepository.findAllByUser(user);
        } catch (EntityNotFoundException e) {
            logger.error("Could not find user with id " + userId + "!");
            throw new NotFoundException("Could not find user with id " + userId + "!");
        }

        for (Participant participant : participants) {
            experiments.put(participant.getExperiment().getId(), participant.getExperiment().getTitle());
        }

        return experiments;
    }

    /**
     * Deletes the participant with the given user and experiment id from the database, if any such participant exists.
     *
     * @param userId The user id to search for.
     * @param experimentId The experiment id to search for.
     */
    @Transactional
    public void deleteParticipant(final int userId, final int experimentId) {
        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            logger.error("Cannot delete participant with invalid user id " + userId + " or invalid experiment id "
                    + experimentId + "!");
            throw new IllegalArgumentException("Cannot delete participant with invalid user id " + userId
                    + " or invalid experiment id " + experimentId + "!");
        }

        ParticipantId participantId = new ParticipantId(userId, experimentId);
        participantRepository.deleteById(participantId);
    }

    /**
     * Checks whether the user with the given id is participating in another experiment they have not yet completed.
     *
     * @param userId The user id to search for.
     * @return {@code true}, if the user is participating in another experiment, or false otherwise.
     */
    @Transactional
    public boolean simultaneousParticipation(final int userId) {
        if (userId < Constants.MIN_ID) {
            logger.error("Cannot search for experiment participation for user with invalid id " + userId + "!");
            throw new IllegalArgumentException("Cannot search for experiment participation for user with invalid id "
                    + userId + "!");
        }

        User user = userRepository.getOne(userId);

        try {
            List<Participant> participation = participantRepository.findAllByEndIsNullAndUser(user);
            return participation.size() > 1;
        } catch (EntityNotFoundException e) {
            logger.error("Could not find participation entries for user with id " + userId + "!", e);
            throw new NotFoundException("Could not find participation entries for user with id " + userId + "!", e);
        }
    }

    /**
     * Adds the user provided by the given {@link CourseParticipant} as a participant to the given experiment.
     *
     * @param courseParticipant The {@link CourseParticipant} containing the user information.
     * @param experiment The {@link Experiment} to which the user should be added.
     */
    private void addCourseParticipantToExperiment(final CourseParticipant courseParticipant,
                                                  final Experiment experiment) {
        User user = courseParticipant.getUser();
        Participant participant = new Participant(user, experiment, null, null);
        participantRepository.save(participant);
        updateUser(user);
    }

    /**
     * Generates a new secret for the given user and activates their user account, if their secret is null.
     *
     * @param user The {@link User} to update.
     */
    private void updateUser(final User user) {
        if (user.getSecret() == null) {
            user.setSecret(Secrets.generateRandomBytes(Constants.SECRET_LENGTH));
            user.setActive(true);
            userRepository.save(user);
        }
    }

    /**
     * Creates a {@link Participant} with the given information of the {@link ParticipantDTO}, the {@link User}, and the
     * {@link Experiment}.
     *
     * @param participantDTO The dto containing the information.
     * @param user The corresponding user.
     * @param experiment The corresponding experiment.
     * @return The new participant containing the information passed in the DTO.
     */
    private Participant createParticipant(final ParticipantDTO participantDTO, final User user,
                                          final Experiment experiment) {
        Participant participant = new Participant();

        if (user != null) {
            participant.setUser(user);
        }
        if (experiment != null) {
            participant.setExperiment(experiment);
        }
        if (participantDTO.getStart() != null) {
            participant.setStart(Timestamp.valueOf(participantDTO.getStart()));
        }
        if (participantDTO.getEnd() != null) {
            participant.setEnd(Timestamp.valueOf(participantDTO.getEnd()));
        }

        return participant;
    }

    /**
     * Creates a {@link ParticipantDTO} with the given information from the {@link Participant}.
     *
     * @param participant The participant object containing the information.
     * @return The new participant DTO containing the information passed in the participant object.
     */
    private ParticipantDTO createParticipantDTO(final Participant participant) {
        ParticipantDTO participantDTO = new ParticipantDTO();

        if (participant.getUser().getId() != null) {
            participantDTO.setUser(participant.getUser().getId());
        }
        if (participant.getExperiment().getId() != null) {
            participantDTO.setExperiment(participant.getExperiment().getId());
        }
        if (participant.getStart() != null) {
            participantDTO.setStart(participant.getStart().toLocalDateTime());
        }
        if (participant.getEnd() != null) {
            participantDTO.setEnd(participant.getEnd().toLocalDateTime());
        }

        return participantDTO;
    }

}
