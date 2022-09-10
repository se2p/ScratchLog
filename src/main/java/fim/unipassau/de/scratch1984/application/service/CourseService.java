package fim.unipassau.de.scratch1984.application.service;

import fim.unipassau.de.scratch1984.application.exception.IncompleteDataException;
import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.exception.StoreException;
import fim.unipassau.de.scratch1984.persistence.entity.Course;
import fim.unipassau.de.scratch1984.persistence.repository.CourseExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.CourseParticipantRepository;
import fim.unipassau.de.scratch1984.persistence.repository.CourseRepository;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.web.dto.CourseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
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
     * Constructs a new course service with the given dependencies.
     *
     * @param courseRepository The {@link CourseRepository} to use.
     * @param courseParticipantRepository The {@link CourseParticipantRepository} to use.
     * @param courseExperimentRepository The {@link CourseExperimentRepository} to use.
     */
    @Autowired
    public CourseService(final CourseRepository courseRepository,
                         final CourseParticipantRepository courseParticipantRepository,
                         final CourseExperimentRepository courseExperimentRepository) {
        this.courseRepository = courseRepository;
        this.courseParticipantRepository = courseParticipantRepository;
        this.courseExperimentRepository = courseExperimentRepository;
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
