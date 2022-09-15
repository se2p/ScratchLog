package fim.unipassau.de.scratch1984.application.service;

import fim.unipassau.de.scratch1984.application.exception.IncompleteDataException;
import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.exception.StoreException;
import fim.unipassau.de.scratch1984.persistence.entity.Course;
import fim.unipassau.de.scratch1984.persistence.entity.CourseExperiment;
import fim.unipassau.de.scratch1984.persistence.entity.CourseExperimentId;
import fim.unipassau.de.scratch1984.persistence.entity.CourseParticipant;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.repository.CourseExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.CourseParticipantRepository;
import fim.unipassau.de.scratch1984.persistence.repository.CourseRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.web.dto.CourseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import java.sql.Timestamp;
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
    private static final Logger logger = LoggerFactory.getLogger(CourseService.class);

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
     * Constructs a new course service with the given dependencies.
     *
     * @param courseRepository The {@link CourseRepository} to use.
     * @param courseParticipantRepository The {@link CourseParticipantRepository} to use.
     * @param courseExperimentRepository The {@link CourseExperimentRepository} to use.
     * @param experimentRepository The {@link ExperimentRepository} to use.
     * @param userRepository The {@link UserRepository} to use.
     */
    @Autowired
    public CourseService(final CourseRepository courseRepository,
                         final CourseParticipantRepository courseParticipantRepository,
                         final CourseExperimentRepository courseExperimentRepository,
                         final ExperimentRepository experimentRepository,
                         final UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.courseParticipantRepository = courseParticipantRepository;
        this.courseExperimentRepository = courseExperimentRepository;
        this.experimentRepository = experimentRepository;
        this.userRepository = userRepository;
    }

    /**
     * Checks, whether a course with the given title already exists in the database.
     *
     * @param title The title to search for.
     * @return {@code true} iff a course with the given title was found.
     */
    @Transactional
    public boolean existsCourse(final String title) {
        if (title == null || title.trim().isBlank()) {
            return false;
        }

        return courseRepository.existsByTitle(title);
    }

    /**
     * Checks, whether any course with the given title exists in the database where the id does not match the given id.
     *
     * @param title The title to search for.
     * @param id The id to compare to.
     * @return {@code true} if such a course exists, or {@code false} if not.
     */
    @Transactional
    public boolean existsCourse(final int id, final String title) {
        if (title == null || title.trim().isBlank() || id < Constants.MIN_ID) {
            return false;
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
     */
    @Transactional
    public boolean existsCourseExperiment(final int courseId, final String experimentTitle) {
        if (experimentTitle == null || experimentTitle.trim().isBlank() || courseId < Constants.MIN_ID) {
            return false;
        }

        Course course = courseRepository.getOne(courseId);
        Experiment experiment = experimentRepository.findByTitle(experimentTitle);

        try {
            return experiment != null && courseExperimentRepository.existsByCourseAndExperiment(course, experiment);
        } catch (EntityNotFoundException e) {
            return false;
        }
    }

    /**
     * Creates a new course or updates an existing one with the given parameters in the database. If the DTO contains
     * invalid attribute values, an {@link IncompleteDataException} is thrown. If the information could not be persisted
     * correctly, a {@link StoreException} is thrown instead.
     *
     * @param courseDTO The dto containing the course information to set.
     * @return The id of the newly created course, if the information was persisted.
     */
    @Transactional
    public int saveCourse(final CourseDTO courseDTO) {
        if (courseDTO.getTitle() == null || courseDTO.getTitle().trim().isBlank()) {
            logger.error("Cannot save a course with an empty title!");
            throw new IncompleteDataException("Cannot save a course with an empty title!");
        } else if (courseDTO.getDescription() == null || courseDTO.getDescription().trim().isBlank()) {
            logger.error("Cannot save a course with an empty description!");
            throw new IncompleteDataException("Cannot save a course with an empty description!");
        } else if (courseDTO.getLastChanged() == null) {
            logger.error("Cannot save a course with last changed null!");
            throw new IncompleteDataException("Cannot save a course with last changed null!");
        }

        Course course = createCourse(courseDTO);
        course = courseRepository.save(course);

        if (course.getId() == null || course.getId() < Constants.MIN_ID) {
            logger.error("Failed to store course with title " + course.getTitle() + "!");
            throw new StoreException("Failed to store course with title " + course.getTitle() + "!");
        }

        return course.getId();
    }

    /**
     * Creates a new {@link CourseExperiment} entry for the course with the given id and the experiment with the given
     * title. If the passed parameters are invalid, an {@link IllegalArgumentException} is thrown instead. If no
     * corresponding course or experiment entry could be found an {@link EntityNotFoundException} or a
     * {@link ConstraintViolationException} is thrown instead.
     *
     * @param courseId The id of the course.
     * @param experimentTitle The title of the experiment.
     */
    @Transactional
    public void saveCourseExperiment(final int courseId, final String experimentTitle) {
        if (experimentTitle == null || experimentTitle.trim().isBlank() || courseId < Constants.MIN_ID) {
            logger.error("Cannot save course experiment with experiment title null or blank or invalid course id!");
            throw new IllegalArgumentException("Cannot save course experiment with experiment title null or blank or "
                    + "invalid course id!");
        }

        Course course = courseRepository.getOne(courseId);
        Experiment experiment = experimentRepository.findByTitle(experimentTitle);

        try {
            if (experiment == null) {
                logger.error("Could not find the experiment with title " + experimentTitle + " when trying to add a "
                        + "course experiment!");
                throw new NotFoundException("Could not find the experiment with title " + experimentTitle
                        + " when trying to add a course experiment!");
            }

            CourseExperiment courseExperiment = new CourseExperiment(course, experiment,
                    Timestamp.valueOf(LocalDateTime.now()));
            course.setLastChanged(courseExperiment.getAdded());
            courseExperimentRepository.save(courseExperiment);
            courseRepository.save(course);
        } catch (EntityNotFoundException e) {
            logger.error("Could not find the course when saving the course experiment data!", e);
            throw new NotFoundException("Could not find the course when saving the course experiment data!", e);
        } catch (ConstraintViolationException e) {
            logger.error("The given course experiment data does not meet the foreign key constraints!", e);
            throw new StoreException("The given course experiment data does not meet the foreign key constraints!", e);
        }
    }

    /**
     * Deletes the course experiment entry for the course with the given id and the experiment with the given title from
     * the database. If the passed parameters are invalid, an {@link IllegalArgumentException} is thrown instead. If no
     * corresponding course or experiment entry could be found an {@link EntityNotFoundException} is thrown.
     *
     * @param courseId The id of the course.
     * @param experimentTitle The title of the experiment.
     */
    @Transactional
    public void deleteCourseExperiment(final int courseId, final String experimentTitle) {
        if (experimentTitle == null || experimentTitle.trim().isBlank() || courseId < Constants.MIN_ID) {
            logger.error("Cannot delete course experiment with experiment title null or blank or invalid course id!");
            throw new IllegalArgumentException("Cannot delete course experiment with experiment title null or blank or "
                    + "invalid course id!");
        }

        Course course = courseRepository.getOne(courseId);
        Experiment experiment = experimentRepository.findByTitle(experimentTitle);

        try {
            if (experiment == null) {
                logger.error("Could not find the experiment with title " + experimentTitle + " when trying to delete a "
                        + "course experiment!");
                throw new NotFoundException("Could not find the experiment with title " + experimentTitle
                        + " when trying to delete a course experiment!");
            }

            CourseExperimentId courseExperimentId = new CourseExperimentId(courseId, experiment.getId());
            course.setLastChanged(Timestamp.valueOf(LocalDateTime.now()));
            courseExperimentRepository.deleteById(courseExperimentId);
            courseRepository.save(course);
        } catch (EntityNotFoundException e) {
            logger.error("Could not find the course when deleting the course experiment data!", e);
            throw new NotFoundException("Could not find the course when deleting the course experiment data!", e);
        }
    }

    /**
     * Returns a {@link CourseDTO} containing the information about the course with the specified id. If no such course
     * exists, a {@link NotFoundException} is thrown instead.
     *
     * @param id The id to search for.
     * @return The course information, if it exists.
     */
    @Transactional
    public CourseDTO getCourse(final int id) {
        if (id < Constants.MIN_ID) {
            logger.error("Cannot search for course with invalid id " + id + "!");
            throw new IllegalArgumentException("Cannot search for course with invalid id " + id + "!");
        }

        Optional<Course> course = courseRepository.findById(id);

        if (course.isEmpty()) {
            logger.error("Could not find course with id " + id + " in the database!");
            throw new NotFoundException("Could not find course with id " + id + " in the database!");
        }

        return createCourseDTO(course.get());
    }

    /**
     * Changes the status of the course with the given id to the given status value. Additionally, the status of all
     * experiments that are part of the course and all users participating in the course is updated accordingly.
     *
     * @param status The new status.
     * @param id The course id.
     * @return The updated course data.
     */
    @Transactional
    public CourseDTO changeCourseStatus(final boolean status, final int id) {
        if (id < Constants.MIN_ID) {
            logger.error("Cannot update status for course with invalid id " + id + "!");
            throw new IllegalArgumentException("Cannot update status for course with invalid id " + id + "!");
        }

        Course course = courseRepository.getOne(id);

        try {
            List<CourseExperiment> courseExperiments = courseExperimentRepository.findAllByCourse(course);
            List<CourseParticipant> courseParticipants = courseParticipantRepository.findAllByCourse(course);
            courseExperiments.forEach(courseExperiment -> updateExperimentStatus(status,
                    courseExperiment.getExperiment()));
            courseParticipants.forEach(courseParticipant -> updateUserStatus(status, courseParticipant.getUser()));
            course.setActive(status);
            course.setLastChanged(Timestamp.valueOf(LocalDateTime.now()));
            courseRepository.save(course);
            Optional<Course> updated = courseRepository.findById(id);

            if (updated.isEmpty()) {
                logger.error("The course with the id " + id + " can no longer be found after updating its status!");
                throw new IllegalStateException("The course with the id " + id + " can no longer be found after "
                        + "updating its status!");
            } else {
                return createCourseDTO(updated.get());
            }
        } catch (EntityNotFoundException e) {
            logger.error("Could not update the status for non-existent course with id " + id + "!");
            throw new NotFoundException("Could not update the status for non-existent course with id " + id + "!");
        }
    }

    /**
     * Changes the activation status of the given experiment according to the passed status. If the experiment is part
     * of multiple courses, it will not be deactivated.
     *
     * @param status The status to change to.
     * @param experiment The {@link Experiment} to update.
     */
    private void updateExperimentStatus(final boolean status, final Experiment experiment) {
        if (status) {
            experimentRepository.updateStatusById(experiment.getId(), true);
        } else {
            List<CourseExperiment> courses = courseExperimentRepository.findAllByExperiment(experiment);

            if (courses.size() <= 1) {
                experimentRepository.updateStatusById(experiment.getId(), false);
            }
        }
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
     * Creates a {@link Course} entity with the information of the given {@link CourseDTO}.
     *
     * @param courseDTO The course DTO containing the information.
     * @return The new course containing the information passed in the DTO.
     */
    private Course createCourse(final CourseDTO courseDTO) {
        Course course = new Course();

        if (courseDTO.getId() != null) {
            course.setId(courseDTO.getId());
        }
        if (courseDTO.getTitle() != null) {
            course.setTitle(courseDTO.getTitle());
        }
        if (courseDTO.getDescription() != null) {
            course.setDescription(courseDTO.getDescription());
        }
        if (courseDTO.getContent() != null) {
            course.setContent(courseDTO.getContent());
        }
        if (courseDTO.getLastChanged() != null) {
            course.setLastChanged(Timestamp.valueOf(courseDTO.getLastChanged()));
        }
        course.setActive(courseDTO.isActive());

        return course;
    }

    /**
     * Creates a new {@link CourseDTO} with the information passed in the given {@link Course} entity.
     *
     * @param course The entity containing the information.
     * @return The new course DTO.
     */
    private CourseDTO createCourseDTO(final Course course) {
        CourseDTO courseDTO = new CourseDTO();

        if (course.getId() != null) {
            courseDTO.setId(course.getId());
        }
        if (course.getTitle() != null) {
            courseDTO.setTitle(course.getTitle());
        }
        if (course.getDescription() != null) {
            courseDTO.setDescription(course.getDescription());
        }
        if (course.getContent() != null) {
            courseDTO.setContent(course.getContent());
        }
        if (course.getLastChanged() != null) {
            courseDTO.setLastChanged(course.getLastChanged().toLocalDateTime());
        }
        courseDTO.setActive(course.isActive());

        return courseDTO;
    }

}
