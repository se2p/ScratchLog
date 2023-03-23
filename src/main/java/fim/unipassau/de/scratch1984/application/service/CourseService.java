package fim.unipassau.de.scratch1984.application.service;

import fim.unipassau.de.scratch1984.application.exception.IncompleteDataException;
import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.exception.StoreException;
import fim.unipassau.de.scratch1984.persistence.entity.Course;
import fim.unipassau.de.scratch1984.persistence.entity.CourseExperiment;
import fim.unipassau.de.scratch1984.persistence.entity.CourseExperimentId;
import fim.unipassau.de.scratch1984.persistence.entity.CourseParticipant;
import fim.unipassau.de.scratch1984.persistence.entity.CourseParticipantId;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.Participant;
import fim.unipassau.de.scratch1984.persistence.entity.ParticipantId;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.repository.CourseExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.CourseParticipantRepository;
import fim.unipassau.de.scratch1984.persistence.repository.CourseRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ParticipantRepository;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.util.Secrets;
import fim.unipassau.de.scratch1984.util.enums.Role;
import fim.unipassau.de.scratch1984.web.dto.CourseDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * A service providing methods related to courses.
 */
@Service
public class CourseService {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CourseService.class);

    /**
     * The course repository to use for database queries related to course data.
     */
    private final CourseRepository courseRepository;

    /**
     * The course participant repository to use for database queries related to course participant data.
     */
    private final CourseParticipantRepository courseParticipantRepository;

    /**
     * The course experiment repository to use for database queries related to course experiment data.
     */
    private final CourseExperimentRepository courseExperimentRepository;

    /**
     * The experiment repository to use for database queries related to experiment data.
     */
    private final ExperimentRepository experimentRepository;

    /**
     * The user repository to use for database queries related to user data.
     */
    private final UserRepository userRepository;

    /**
     * The participant repository to use for database queries related to experiment participation data.
     */
    private final ParticipantRepository participantRepository;

    /**
     * Constructs a new course service with the given dependencies.
     *
     * @param courseRepository The {@link CourseRepository} to use.
     * @param courseParticipantRepository The {@link CourseParticipantRepository} to use.
     * @param courseExperimentRepository The {@link CourseExperimentRepository} to use.
     * @param experimentRepository The {@link ExperimentRepository} to use.
     * @param userRepository The {@link UserRepository} to use.
     * @param participantRepository The {@link ParticipantRepository} to use.
     */
    @Autowired
    public CourseService(final CourseRepository courseRepository,
                         final CourseParticipantRepository courseParticipantRepository,
                         final CourseExperimentRepository courseExperimentRepository,
                         final ExperimentRepository experimentRepository,
                         final UserRepository userRepository,
                         final ParticipantRepository participantRepository) {
        this.courseRepository = courseRepository;
        this.courseParticipantRepository = courseParticipantRepository;
        this.courseExperimentRepository = courseExperimentRepository;
        this.experimentRepository = experimentRepository;
        this.userRepository = userRepository;
        this.participantRepository = participantRepository;
    }

    /**
     * Checks, whether an active course with the given id already exists in the database.
     *
     * @param id The id to search for.
     * @return {@code true} iff a course with the given id was found.
     * @throws IllegalArgumentException if the passed id is invalid.
     */
    @Transactional
    public boolean existsActiveCourse(final int id) {
        if (id < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot check for existing course with invalid id " + id + "!");
        }

        try {
            Course course = courseRepository.getReferenceById(id);
            return course.isActive();
        } catch (EntityNotFoundException e) {
            return false;
        }
    }

    /**
     * Checks, whether a course with the given title already exists in the database.
     *
     * @param title The title to search for.
     * @return {@code true} iff a course with the given title was found.
     * @throws IllegalArgumentException if the passed title is null or blank.
     */
    @Transactional
    public boolean existsCourse(final String title) {
        if (title == null || title.trim().isBlank()) {
            throw new IllegalArgumentException("Cannot check for existing course with title null or blank!");
        }

        return courseRepository.existsByTitle(title);
    }

    /**
     * Checks, whether any course with the given title exists in the database where the id does not match the given id.
     *
     * @param title The title to search for.
     * @param id The id to compare to.
     * @return {@code true} if such a course exists, or {@code false} if not.
     * @throws IllegalArgumentException if the passed title is null or blank or the id is invalid.
     */
    @Transactional
    public boolean existsCourse(final int id, final String title) {
        if (title == null || title.trim().isBlank() || id < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot check for existing course without a title or invalid id " + id
                    + "!");
        }

        Optional<Course> course = courseRepository.findByTitle(title);

        if (course.isEmpty()) {
            return false;
        } else {
            return course.get().getId() != id;
        }
    }

    /**
     * Checks, whether a course experiment entry exists for the course with the given id and the experiment with the
     * given title.
     *
     * @param experimentTitle The title of the experiment.
     * @param courseId The id of the course.
     * @return {@code true} if such an entry exists, or {@code false} if not.
     * @throws IllegalArgumentException if the experiment title is null or blank or the course id is invalid.
     */
    @Transactional
    public boolean existsCourseExperiment(final int courseId, final String experimentTitle) {
        if (experimentTitle == null || experimentTitle.trim().isBlank() || courseId < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot check for an existing course experiment without an experiment "
                    + "title or an invalid course id " + courseId + "!");
        }

        Course course = courseRepository.getReferenceById(courseId);
        Optional<Experiment> experiment = experimentRepository.findByTitle(experimentTitle);

        try {
            return experiment.isPresent() && courseExperimentRepository.existsByCourseAndExperiment(course,
                    experiment.get());
        } catch (EntityNotFoundException e) {
            return false;
        }
    }

    /**
     * Checks, whether a course participant entry exists for the course with the given id and the participant with the
     * given username or email.
     *
     * @param input The username or email.
     * @param courseId The id of the course.
     * @return {@code true} if such an entry exists, or {@code false} if not.
     * @throws IllegalArgumentException if the given username or email is null or blank or the course id is invalid.
     */
    @Transactional
    public boolean existsCourseParticipant(final int courseId, final String input) {
        if (input == null || input.trim().isBlank() || courseId < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot check for existing course participant without an search string "
                    + "or an invalid course id " + courseId + "!");
        }

        Course course = courseRepository.getReferenceById(courseId);
        Optional<User> user = userRepository.findUserByUsernameOrEmail(input, input);

        try {
            return user.isPresent() && courseParticipantRepository.existsByCourseAndUser(course, user.get());
        } catch (EntityNotFoundException e) {
            return false;
        }
    }

    /**
     * Checks, whether a course participant entry exists for the user with the given id given the experiment with the
     * passed id which is assumed to be an experiment belonging to a course. If a course experiment entry could be found
     * for the experiment and the user with the given id is participating in that course, {@code true} is returned.
     *
     * @param experimentId The id of the experiment.
     * @param userId The id of the user.
     * @return {@code true} if such an entry exists, or {@code false} if not.
     * @throws IllegalArgumentException if the experiment or user ids are invalid.
     */
    @Transactional
    public boolean existsCourseParticipant(final int experimentId, final int userId) {
        if (experimentId < Constants.MIN_ID || userId < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot check for existing course participant with invalid user id "
                    + userId + " or invalid experiment id " + experimentId + "!");
        }

        Experiment experiment = experimentRepository.getReferenceById(experimentId);
        User user = userRepository.getReferenceById(userId);

        try {
            Optional<CourseExperiment> courseExperiment = courseExperimentRepository.findByExperiment(experiment);

            if (courseExperiment.isEmpty()) {
                return false;
            } else {
                return courseParticipantRepository.existsByCourseAndUser(courseExperiment.get().getCourse(), user);
            }
        } catch (EntityNotFoundException e) {
            return false;
        }
    }

    /**
     * Creates a new course or updates an existing one with the given parameters in the database.
     *
     * @param courseDTO The dto containing the course information to set.
     * @return The id of the newly created course, if the information was persisted.
     * @throws IncompleteDataException if the {@link CourseDTO} contains invalid attribute values.
     * @throws StoreException if the course information could not be persisted.
     */
    @Transactional
    public int saveCourse(final CourseDTO courseDTO) {
        if (courseDTO.getTitle() == null || courseDTO.getTitle().trim().isBlank()) {
            throw new IncompleteDataException("Cannot save a course with an empty title!");
        } else if (courseDTO.getDescription() == null || courseDTO.getDescription().trim().isBlank()) {
            throw new IncompleteDataException("Cannot save a course with an empty description!");
        } else if (courseDTO.getLastChanged() == null) {
            throw new IncompleteDataException("Cannot save a course with last changed null!");
        }

        Course course = createCourse(courseDTO);
        course = courseRepository.save(course);

        if (course.getId() == null || course.getId() < Constants.MIN_ID) {
            throw new StoreException("Failed to store course with title " + course.getTitle() + "!");
        }

        return course.getId();
    }

    /**
     * Deletes the course with the given id along with all its experiments.
     *
     * @param id The id of the course.
     * @throws IllegalArgumentException if the passed id is invalid.
     * @throws NotFoundException if no corresponding course could be found.
     */
    @Transactional
    public void deleteCourse(final int id) {
        if (id < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot delete course with invalid id " + id);
        }

        Course course = courseRepository.getReferenceById(id);

        try {
            List<CourseExperiment> courseExperiments = courseExperimentRepository.findAllByCourse(course);
            courseExperimentRepository.deleteAll(courseExperiments);
            courseExperiments.forEach(experiment -> experimentRepository.delete(experiment.getExperiment()));
            courseRepository.deleteById(id);
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find the course when trying to delete the course experiments of course with id "
                    + id + "!", e);
            throw new NotFoundException("Could not find the course when trying to delete the course experiments of "
                    + "course with id " + id + "!", e);
        }
    }

    /**
     * Creates a new {@link CourseParticipant} entry for the course with the given id and the participant with the given
     * username or email.
     *
     * @param courseId The id of the course.
     * @param participant The username or email.
     * @return The id of the user.
     * @throws IllegalArgumentException if the passed course id or participant string are invalid.
     * @throws NotFoundException if no corresponding user could be found.
     * @throws IllegalStateException if a user could be found who is not participating in the course.
     * @throws EntityNotFoundException if no course could be found for the given id.
     * @throws ConstraintViolationException if saving the course participant violated the foreign key constraints.
     */
    @Transactional
    public int saveCourseParticipant(final int courseId, final String participant) {
        if (participant == null || participant.trim().isBlank() || courseId < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot save course experiment with participant null or blank or "
                    + "invalid course id!");
        }

        Course course = courseRepository.getReferenceById(courseId);
        Optional<User> optionalUser = userRepository.findUserByUsernameOrEmail(participant, participant);

        try {
            if (optionalUser.isEmpty()) {
                LOGGER.error("Could not find the user with username or email " + participant + " when trying to add a "
                        + "course participant!");
                throw new NotFoundException("Could not find the user with username or email " + participant
                        + " when trying to add a course participant!");
            } else if (!optionalUser.get().getRole().equals(Role.PARTICIPANT)) {
                throw new IllegalStateException("Tried to add administrator with username or email " + participant
                        + " as a course participant!");
            }

            User user = optionalUser.get();
            LocalDateTime now = LocalDateTime.now();
            CourseParticipant courseParticipant = new CourseParticipant(user, course, now);
            course.setLastChanged(now);
            user.setActive(true);
            courseParticipantRepository.save(courseParticipant);
            courseRepository.save(course);
            userRepository.save(user);
            return user.getId();
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find the course when saving the course participant data!", e);
            throw new NotFoundException("Could not find the course when saving the course participant data!", e);
        } catch (ConstraintViolationException e) {
            throw new StoreException("The given course participant data does not meet the foreign key constraints!", e);
        }
    }

    /**
     * Removes the user with the given username or email as a participant of the course with the given id. Additionally,
     * the user is removed as a participant from all experiments offered in the course.
     *
     * @param courseId The id of the course.
     * @param participant The username or email of the participant to be removed.
     * @throws IllegalArgumentException if the passed course id or participant string are invalid.
     * @throws NotFoundException if no corresponding user entry could be found.
     * @throws EntityNotFoundException if no course could be found for the given id.
     */
    @Transactional
    public void deleteCourseParticipant(final int courseId, final String participant) {
        if (participant == null || participant.trim().isBlank() || courseId < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot delete course experiment with participant null or blank or "
                    + "invalid course id!");
        }

        Course course = courseRepository.getReferenceById(courseId);
        Optional<User> user = userRepository.findUserByUsernameOrEmail(participant, participant);

        try {
            if (user.isEmpty()) {
                LOGGER.error("Could not find the user with username or email " + participant + " when trying to delete "
                        + "a course participant!");
                throw new NotFoundException("Could not find the user with username or email " + participant + " when "
                        + "trying to delete a course participant!");
            }

            CourseParticipantId courseParticipantId = new CourseParticipantId(user.get().getId(), courseId);
            List<CourseExperiment> courseExperiments = courseExperimentRepository.findAllByCourse(course);
            course.setLastChanged(LocalDateTime.now());
            courseExperiments.forEach(courseExperiment -> deleteExperimentParticipant(user.get(),
                    courseExperiment.getExperiment()));
            courseParticipantRepository.deleteById(courseParticipantId);
            courseRepository.save(course);
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find the course when deleting the course participant data!", e);
            throw new NotFoundException("Could not find the course when deleting the course participant data!", e);
        }
    }

    /**
     * Creates a new {@link CourseExperiment} entry for the course and experiment with the given ids.
     *
     * @param courseId The id of the course.
     * @param experimentId The id of the experiment.
     * @throws IllegalArgumentException if the passed course id or participant string are invalid.
     * @throws EntityNotFoundException if no corresponding course or experiment entries could be found.
     * @throws ConstraintViolationException if saving the course experiment violated the foreign key constraints.
     */
    @Transactional
    public void saveCourseExperiment(final int courseId, final int experimentId) {
        if (courseId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot save course experiment with invalid course id " + courseId
                    + " or experiment id " + experimentId + "!");
        }

        Course course = courseRepository.getReferenceById(courseId);
        Experiment experiment = experimentRepository.getReferenceById(experimentId);
        persistCourseExperiment(course, experiment);
    }

    /**
     * Deletes the course experiment entry for the course with the given id and the experiment with the given title from
     * the database.
     *
     * @param courseId The id of the course.
     * @param experimentTitle The title of the experiment.
     * @throws IllegalArgumentException if the passed course or experiment ids are invalid.
     * @throws EntityNotFoundException if no corresponding course or experiment entry could be found.
     */
    @Transactional
    public void deleteCourseExperiment(final int courseId, final String experimentTitle) {
        if (experimentTitle == null || experimentTitle.trim().isBlank() || courseId < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot delete course experiment with experiment title null or blank or "
                    + "invalid course id!");
        }

        Course course = courseRepository.getReferenceById(courseId);
        Optional<Experiment> experiment = experimentRepository.findByTitle(experimentTitle);

        try {
            if (experiment.isEmpty()) {
                LOGGER.error("Could not find the experiment with title " + experimentTitle + " when trying to delete a "
                        + "course experiment!");
                throw new NotFoundException("Could not find the experiment with title " + experimentTitle
                        + " when trying to delete a course experiment!");
            }

            CourseExperimentId courseExperimentId = new CourseExperimentId(courseId, experiment.get().getId());
            course.setLastChanged(LocalDateTime.now());
            courseExperimentRepository.deleteById(courseExperimentId);
            courseRepository.save(course);
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find the course when deleting the course experiment data!", e);
            throw new NotFoundException("Could not find the course when deleting the course experiment data!", e);
        }
    }

    /**
     * Adds the course participant with the given id as a participant to all experiments offered as part of that course.
     *
     * @param courseId The id of the course.
     * @param userId The id of the user.
     * @throws IllegalArgumentException if the passed course or user ids are invalid.
     * @throws EntityNotFoundException if no corresponding course or user entry could be found.
     * @throws ConstraintViolationException if saving any experiment participation violated a foreign key constraint.
     */
    @Transactional
    public void addParticipantToCourseExperiments(final int courseId, final int userId) {
        if (courseId < Constants.MIN_ID || userId < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot add participant to course experiment with invalid course id "
                    + courseId + " or invalid user id " + userId);
        }

        Course course = courseRepository.getReferenceById(courseId);
        User user = userRepository.getReferenceById(userId);

        try {
            List<CourseExperiment> courseExperiments = courseExperimentRepository.findAllByCourse(course);
            courseExperiments.forEach(courseExperiment -> addExperimentParticipant(user,
                    courseExperiment.getExperiment()));

            if (!courseExperiments.isEmpty() && user.getSecret() == null) {
                user.setSecret(Secrets.generateRandomBytes(Constants.SECRET_LENGTH));
                userRepository.save(user);
            }
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find the course or user when adding course participants to course experiments!", e);
            throw new NotFoundException("Could not find the course or user when adding course participants to course "
                    + "experiments!", e);
        } catch (ConstraintViolationException e) {
            throw new StoreException("The given experiment participant data does not meet the foreign key constraints!",
                    e);
        }
    }

    /**
     * Returns a {@link CourseDTO} containing the information about the course with the specified id.
     *
     * @param id The id to search for.
     * @return The course information, if it exists.
     * @throws IllegalArgumentException if the passed id is invalid.
     * @throws NotFoundException if no corresponding course entry could be found.
     */
    @Transactional
    public CourseDTO getCourse(final int id) {
        if (id < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot search for course with invalid id " + id + "!");
        }

        Optional<Course> course = courseRepository.findById(id);

        if (course.isEmpty()) {
            LOGGER.error("Could not find course with id " + id + " in the database!");
            throw new NotFoundException("Could not find course with id " + id + " in the database!");
        }

        return createCourseDTO(course.get());
    }

    /**
     * Changes the status of the course with the given id to the given status value. Additionally, the status of all
     * users participating in the course is updated accordingly. If the course is deactivated, all experiments that are
     * part of the course are deactivated as well.
     *
     * @param status The new status.
     * @param id The course id.
     * @return The updated course data.
     * @throws IllegalArgumentException if the passed status or id are invalid.
     * @throws NotFoundException if no corresponding course entry could be found.
     */
    @Transactional
    public CourseDTO changeCourseStatus(final boolean status, final int id) {
        if (id < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot update status for course with invalid id " + id + "!");
        }

        Course course = courseRepository.getReferenceById(id);

        try {
            if (!status) {
                courseExperimentRepository.findAllByCourse(course).stream()
                        .map(CourseExperiment::getExperiment).map(Experiment::getId)
                        .forEach(experimentId -> experimentRepository.updateStatusById(experimentId, false));

            }

            List<CourseParticipant> courseParticipants = courseParticipantRepository.findAllByCourse(course);
            courseParticipants.forEach(courseParticipant -> updateUserStatus(status, courseParticipant.getUser()));
            course.setActive(status);
            course.setLastChanged(LocalDateTime.now());
            Course updated = courseRepository.save(course);
            return createCourseDTO(updated);
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not update the status for non-existent course with id " + id + "!");
            throw new NotFoundException("Could not update the status for non-existent course with id " + id + "!");
        }
    }

    /**
     * Deactivates all courses in which all experiments are deactivated and which have not been updated for a specific
     * time. Only the course itself is deactivated, the status of the participant accounts does not change.
     */
    @Transactional
    public void deactivateInactiveCourses() {
        courseRepository.findAllByActiveIsTrue().forEach(this::checkDeactivateCourse);
    }

    /**
     * Changes the activation status of the user with the given id according to the passed status.
     *
     * @param status The status to change to.
     * @param user The {@link User} to be updated.
     */
    private void updateUserStatus(final boolean status, final User user) {
        if (status) {
            user.setActive(true);
        } else {
            user.setActive(false);
            user.setSecret(null);
        }

        userRepository.save(user);
    }

    /**
     * Adds the given user as a participant to the given experiment.
     *
     * @param user The {@link User} to be added as a participant.
     * @param experiment The {@link Experiment} in which the user should participate.
     * @throws IllegalStateException if the user is already participating in the experiment.
     */
    private void addExperimentParticipant(final User user, final Experiment experiment) {
        if (participantRepository.existsByUserAndExperiment(user, experiment)) {
            throw new IllegalStateException("A participant entry for the user with id " + user.getId()
                    + " and experiment with id " + experiment.getId() + " already exists!");
        }

        Participant participant = new Participant(user, experiment, null, null);
        participantRepository.save(participant);
    }

    /**
     * Removes the participant entry for the given user in the given experiment, if such an entry exists.
     *
     * @param user The {@link User} to search for.
     * @param experiment The {@link Experiment} to search for.
     */
    private void deleteExperimentParticipant(final User user, final Experiment experiment) {
        if (participantRepository.existsByUserAndExperiment(user, experiment)) {
            ParticipantId participantId = new ParticipantId(user.getId(), experiment.getId());
            participantRepository.deleteById(participantId);
        }
    }

    /**
     * Creates a new {@link CourseExperiment} relation between the given course and experiment and updates the last
     * changed attribute of the course.
     *
     * @param course The course.
     * @param experiment The experiment.
     * @throws EntityNotFoundException if the given course and experiment do not exist.
     * @throws ConstraintViolationException if saving the {@link CourseExperiment} violated the foreign key constraint.
     */
    private void persistCourseExperiment(final Course course, final Experiment experiment) {
        try {
            LocalDateTime now = LocalDateTime.now();
            CourseExperiment courseExperiment = new CourseExperiment(course, experiment, now);
            experiment.setActive(true);
            course.setActive(true);
            course.setLastChanged(now);
            courseExperimentRepository.save(courseExperiment);
            courseRepository.save(course);
            experimentRepository.save(experiment);
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find the course or experiment when saving the course experiment data!", e);
            throw new NotFoundException("Could not find the course or experiment when saving the course experiment "
                    + "data!", e);
        } catch (ConstraintViolationException e) {
            throw new StoreException("The given course experiment data does not meet the foreign key constraints!", e);
        }
    }

    /**
     * Checks whether a course should be deactivated due to inactivity. A course is considered to be inactive if all its
     * experiments are inactive, and it hasn't been updated for a specified time. If the course is considered to be
     * inactive, it is deactivated.
     *
     * @param course The {@link Course} to check.
     */
    private void checkDeactivateCourse(final Course course) {
        List<CourseExperiment> courseExperiments = courseExperimentRepository.findAllByCourse(course);
        boolean experimentsInactive = !courseExperiments.isEmpty() && courseExperiments.stream().noneMatch(
                courseExperiment -> courseExperiment.getExperiment().isActive());

        if (experimentsInactive && course.getLastChanged().isBefore(LocalDateTime.now().minusDays(
                Constants.COURSE_INACTIVE_DAYS))) {
            course.setActive(false);
            courseRepository.save(course);
        }
    }

    /**
     * Creates a {@link Course} entity with the information of the given {@link CourseDTO}.
     *
     * @param courseDTO The course DTO containing the information.
     * @return The new course containing the information passed in the DTO.
     */
    private Course createCourse(final CourseDTO courseDTO) {
        Course course = Course.builder()
                .title(courseDTO.getTitle())
                .description(courseDTO.getDescription())
                .content(courseDTO.getContent())
                .lastChanged(courseDTO.getLastChanged())
                .active(courseDTO.isActive())
                .build();

        if (courseDTO.getId() != null) {
            course.setId(courseDTO.getId());
        }

        return course;
    }

    /**
     * Creates a new {@link CourseDTO} with the information passed in the given {@link Course} entity.
     *
     * @param course The entity containing the information.
     * @return The new course DTO.
     */
    private CourseDTO createCourseDTO(final Course course) {
        CourseDTO courseDTO = CourseDTO.builder()
                .title(course.getTitle())
                .description(course.getDescription())
                .content(course.getContent())
                .lastChanged(course.getLastChanged())
                .active(course.isActive())
                .build();

        if (course.getId() != null) {
            courseDTO.setId(course.getId());
        }

        return courseDTO;
    }

}
