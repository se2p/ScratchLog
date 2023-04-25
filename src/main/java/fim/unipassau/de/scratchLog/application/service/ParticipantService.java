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

package fim.unipassau.de.scratchLog.application.service;

import fim.unipassau.de.scratchLog.application.exception.NotFoundException;
import fim.unipassau.de.scratchLog.application.exception.StoreException;
import fim.unipassau.de.scratchLog.persistence.entity.Course;
import fim.unipassau.de.scratchLog.persistence.entity.CourseParticipant;
import fim.unipassau.de.scratchLog.persistence.entity.Experiment;
import fim.unipassau.de.scratchLog.persistence.entity.Participant;
import fim.unipassau.de.scratchLog.persistence.entity.ParticipantId;
import fim.unipassau.de.scratchLog.persistence.entity.User;
import fim.unipassau.de.scratchLog.persistence.repository.CourseExperimentRepository;
import fim.unipassau.de.scratchLog.persistence.repository.CourseParticipantRepository;
import fim.unipassau.de.scratchLog.persistence.repository.CourseRepository;
import fim.unipassau.de.scratchLog.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratchLog.persistence.repository.ParticipantRepository;
import fim.unipassau.de.scratchLog.persistence.repository.UserRepository;
import fim.unipassau.de.scratchLog.util.Constants;
import fim.unipassau.de.scratchLog.util.Secrets;
import fim.unipassau.de.scratchLog.web.dto.ParticipantDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A service providing methods related experiment participation.
 */
@Service
public class ParticipantService {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ParticipantService.class);

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
     * The course experiment repository to use for database queries related to course experiment data.
     */
    private final CourseExperimentRepository courseExperimentRepository;

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
     * @param courseExperimentRepository The {@link CourseExperimentRepository} to use.
     * @param courseParticipantRepository The {@link CourseParticipantRepository} to use.
     * @param experimentRepository The {@link ExperimentRepository} to use.
     */
    @Autowired
    public ParticipantService(final UserRepository userRepository, final ParticipantRepository participantRepository,
                              final CourseRepository courseRepository,
                              final CourseExperimentRepository courseExperimentRepository,
                              final CourseParticipantRepository courseParticipantRepository,
                              final ExperimentRepository experimentRepository) {
        this.userRepository = userRepository;
        this.participantRepository = participantRepository;
        this.courseRepository = courseRepository;
        this.courseExperimentRepository = courseExperimentRepository;
        this.courseParticipantRepository = courseParticipantRepository;
        this.experimentRepository = experimentRepository;
    }

    /**
     * Retrieves the participant information for the given experiment id and the given user id from the database.
     *
     * @param experimentId The experiment id.
     * @param userId The user id.
     * @return The {@link ParticipantDTO} containing the participant information.
     * @throws IllegalArgumentException if the passed user or experiment ids are invalid.
     * @throws NotFoundException if no corresponding user, experiment or participant entry could be found.
     */
    @Transactional
    public ParticipantDTO getParticipant(final int experimentId, final int userId) {
        if (experimentId < Constants.MIN_ID || userId < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot search for participant with invalid experiment id "
                    + experimentId + " or invalid user id " + userId + "!");
        }

        User user = userRepository.getReferenceById(userId);
        Experiment experiment = experimentRepository.getReferenceById(experimentId);

        try {
            Optional<Participant> participant = participantRepository.findByUserAndExperiment(user, experiment);

            if (participant.isEmpty()) {
                LOGGER.error("Could not find any participant entry for user with id " + userId + " for experiment with "
                        + "id " + experimentId + "!");
                throw new NotFoundException("Could not find any participant entry for user with id " + userId
                        + " for experiment with id " + experimentId + "!");
            }

            return createParticipantDTO(participant.get());
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find user with id " + userId + " or experiment with id " + experimentId
                    + " in the database!", e);
            throw new NotFoundException("Could not find user with id " + userId + " or experiment with id "
                    + experimentId + " in the database!", e);
        }
    }

    /**
     * Adds the users participating in the course with the given id as participants to the experiment with the given id.
     *
     * @param experimentId The id of the experiment.
     * @param courseId The id of the course.
     * @throws IllegalArgumentException if the passed course or experiment ids are invalid.
     * @throws IllegalStateException if the course or experiment are inactive or the experiment is not part of a course.
     * @throws NotFoundException if no corresponding course or experiment entries could be found.
     * @throws StoreException if adding a course participant to the experiment violated the foreign key constraints.
     */
    @Transactional
    public void saveParticipants(final int experimentId, final int courseId) {
        if (experimentId < Constants.MIN_ID || courseId < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot add participants to course experiment with invalid experiment "
                    + "id " + experimentId + " or invalid course id " + courseId + "!");
        }

        Course course = courseRepository.getReferenceById(courseId);
        Experiment experiment = experimentRepository.getReferenceById(experimentId);

        try {
            if (!course.isActive() || !experiment.isActive()) {
                throw new IllegalStateException("Cannot add participants to inactive course or experiment!");
            } else if (!courseExperimentRepository.existsByCourseAndExperiment(course, experiment)) {
                throw new IllegalStateException("Cannot add course participants to experiment which is not part of the "
                        + "course!");
            }

            List<CourseParticipant> courseParticipants = courseParticipantRepository.findAllByCourse(course);
            courseParticipants.forEach(courseParticipant -> addCourseParticipantToExperiment(courseParticipant,
                    experiment));
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find the course or experiment data when trying to save course participants!", e);
            throw new NotFoundException("Could not find the course or experiment data when trying to save course "
                    + "participants!", e);
        } catch (ConstraintViolationException e) {
            throw new StoreException("The given participant data does not meet the foreign key constraints!", e);
        }
    }

    /**
     * Creates a new participation for the given user and experiment in the database.
     *
     * @param userId The user id.
     * @param experimentId The experiment id.
     * @throws IllegalArgumentException if the passed user or experiment ids are invalid.
     * @throws NotFoundException if no corresponding user or experiment entries could be found.
     * @throws StoreException if adding the user as a participant violates the foreign key constraints.
     */
    @Transactional
    public void saveParticipant(final int userId, final int experimentId) {
        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot add participant with invalid user id " + userId
                    + " or invalid experiment id " + experimentId + "!");
        }

        User user = userRepository.getReferenceById(userId);
        Experiment experiment = experimentRepository.getReferenceById(experimentId);

        try {
            Participant participant = new Participant(user, experiment, null, null);
            participantRepository.save(participant);
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find the user or experiment when saving the participant data!", e);
            throw new NotFoundException("Could not find the user or experiment when saving the participant data!", e);
        } catch (ConstraintViolationException e) {
            throw new StoreException("The given participant data does not meet the foreign key constraints!", e);
        }
    }

    /**
     * Updates the participation information with the given values.
     *
     * @param participantDTO The dto containing the updated participation information.
     * @return {@code true} if the information was persisted, or {@code false} if not.
     * @throws IllegalArgumentException if the experiment or user ids of the {@link ParticipantDTO} are invalid.
     */
    @Transactional
    public boolean updateParticipant(final ParticipantDTO participantDTO) {
        if (participantDTO.getExperiment() < Constants.MIN_ID || participantDTO.getUser() < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot search for participant with invalid experiment id "
                    + participantDTO.getExperiment() + " or invalid user id " + participantDTO.getUser() + "!");
        }

        User user = userRepository.getReferenceById(participantDTO.getUser());
        Experiment experiment = experimentRepository.getReferenceById(participantDTO.getExperiment());

        try {
            participantRepository.save(createParticipant(participantDTO, user, experiment));
            return true;
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find the user with id " + participantDTO.getUser() + " or experiment with id "
                    + participantDTO.getExperiment() + " when trying to update a participant!", e);
        } catch (ConstraintViolationException e) {
            LOGGER.error("No participant entry could be found for user with id " + participantDTO.getUser()
                    + " for experiment with id " + participantDTO.getExperiment() + "!", e);
        }

        return false;
    }

    /**
     * Retrieves a list of participant ids for the experiment with the given id.
     *
     * @param experimentId The experiment id.
     * @throws IllegalArgumentException if the passed id is invalid.
     * @throws NotFoundException if no corresponding experiment entry could be found.
     * @throws IllegalStateException if no user entry could be found for a retrieved participant.
     */
    @Transactional
    public void deactivateParticipantAccounts(final int experimentId) {
        if (experimentId < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot find participant data for experiment with invalid id "
                    + experimentId + "!");
        }

        List<Participant> participants;
        Experiment experiment = experimentRepository.getReferenceById(experimentId);

        try {
            participants = participantRepository.findAllByExperiment(experiment);
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find experiment with id " + experimentId + "!");
            throw new NotFoundException("Could not find experiment with id " + experimentId + "!");
        }

        for (Participant participant : participants) {
            Optional<User> user = userRepository.findById(participant.getUser().getId());

            if (user.isEmpty()) {
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
     * participating.
     *
     * @param userId The user id.
     * @return The list of experiment ids.
     * @throws IllegalArgumentException if the passed id is invalid.
     * @throws NotFoundException if no corresponding user entry could be found.
     */
    @Transactional
    public HashMap<Integer, String> getExperimentInfoForParticipant(final int userId) {
        if (userId < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot find participant data for user with invalid id "
                    + userId + "!");
        }

        List<Participant> participants;
        HashMap<Integer, String> experiments = new HashMap<>();
        User user = userRepository.getReferenceById(userId);

        try {
            participants = participantRepository.findAllByUser(user);
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find user with id " + userId + "!");
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
     * @throws IllegalArgumentException if the passed user or experiment ids are invalid.
     */
    @Transactional
    public void deleteParticipant(final int userId, final int experimentId) {
        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
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
     * @throws IllegalArgumentException if the passed user id is invalid.
     * @throws NotFoundException if no corresponding user entry could be found.
     */
    @Transactional
    public boolean simultaneousParticipation(final int userId) {
        if (userId < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot search for experiment participation for user with invalid id "
                    + userId + "!");
        }

        User user = userRepository.getReferenceById(userId);

        try {
            List<Participant> participation = participantRepository.findAllByEndIsNullAndUser(user);
            return participation.size() > 1;
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find participation entries for user with id " + userId + "!", e);
            throw new NotFoundException("Could not find participation entries for user with id " + userId + "!", e);
        }
    }

    /**
     * Checks, whether the user with the given id is participating in the experiment with the given id and has the
     * given secret.
     *
     * @param userId The id of the user.
     * @param experimentId The id of the experiment.
     * @param secret The user's secret.
     * @param userActive Boolean indicating whether the user account should be active.
     * @return {@code true} if the user is a participant with the given secret or {@code false} otherwise.
     * @throws IllegalArgumentException if the passed secret, user or experiment id are invalid.
     */
    @Transactional
    public boolean isInvalidParticipant(final int userId, final int experimentId, final String secret,
                                        final boolean userActive) {
        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot verify participant with invalid user id " + userId
                    + " or invalid experiment id " + experimentId + "!");
        } else if (secret == null || secret.trim().isBlank()) {
            throw new IllegalArgumentException("Cannot verify participant with secret null or blank!");
        }

        User user = userRepository.getReferenceById(userId);
        Experiment experiment = experimentRepository.getReferenceById(experimentId);

        try {
            Optional<Participant> participant = participantRepository.findByUserAndExperiment(user, experiment);

            if (participant.isEmpty()) {
                LOGGER.error("Cannot save event data for participant null!");
                return true;
            } else if ((!user.isActive() && userActive) || !experiment.isActive()) {
                LOGGER.error("Cannot save event data for inactive experiment or user!");
                return true;
            } else {
                return !user.getSecret().equals(secret);
            }
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find user or experiment when trying to verify a participant!", e);
            return true;
        }
    }

    /**
     * Deactivates all experiments where participants have not started or finished the experiment for a specified number
     * of days. Only the experiment itself is deactivated, the status of the participant accounts does not change.
     */
    @Transactional
    public void deactivateInactiveExperiments() {
        List<Experiment> experiments = experimentRepository.findAllByActiveIsTrue();
        experiments.forEach(this::checkDeactivateExperiment);
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
     * Checks whether an experiment should be deactivated due to inactivity. An experiment is considered inactive if no
     * participant has started or finished it for a specified number of days. If the experiment is considered to be
     * inactive, it is deactivated.
     *
     * @param experiment The {@link Experiment} to check.
     */
    private void checkDeactivateExperiment(final Experiment experiment) {
        List<Participant> participants = participantRepository.findAllByExperiment(experiment);

        if (participants.size() > 0) {
            LocalDateTime maxInactiveTime = LocalDateTime.now().minusDays(Constants.EXPERIMENT_INACTIVE_DAYS);
            LocalDateTime lastStart = participants.stream().map(
                    Participant::getStart).filter(Objects::nonNull).max(LocalDateTime::compareTo).orElse(null);
            LocalDateTime lastEnd = participants.stream().map(
                    Participant::getEnd).filter(Objects::nonNull).max(LocalDateTime::compareTo).orElse(null);
            boolean inactiveStart = lastStart != null && lastStart.isBefore(maxInactiveTime);
            boolean inactiveEnd = lastEnd != null && lastEnd.isBefore(maxInactiveTime);
            if (inactiveStart || inactiveEnd) {
                experiment.setActive(false);
                experimentRepository.save(experiment);
            }
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
        Participant participant = Participant.builder()
                .user(user)
                .experiment(experiment)
                .build();

        if (participantDTO.getStart() != null) {
            participant.setStart(participantDTO.getStart());
        }
        if (participantDTO.getEnd() != null) {
            participant.setEnd(participantDTO.getEnd());
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
        ParticipantDTO participantDTO = ParticipantDTO.builder()
                .user(participant.getUser().getId())
                .experiment(participant.getExperiment().getId())
                .build();

        if (participant.getStart() != null) {
            participantDTO.setStart(participant.getStart());
        }
        if (participant.getEnd() != null) {
            participantDTO.setEnd(participant.getEnd());
        }

        return participantDTO;
    }

}
