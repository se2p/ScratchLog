package fim.unipassau.de.scratch1984.application.service;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.persistence.entity.Course;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.ExperimentData;
import fim.unipassau.de.scratch1984.persistence.entity.Participant;
import fim.unipassau.de.scratch1984.persistence.projection.CourseExperimentProjection;
import fim.unipassau.de.scratch1984.persistence.projection.CourseTableProjection;
import fim.unipassau.de.scratch1984.persistence.projection.ExperimentTableProjection;
import fim.unipassau.de.scratch1984.persistence.repository.CourseExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.CourseParticipantRepository;
import fim.unipassau.de.scratch1984.persistence.repository.CourseRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentDataRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ParticipantRepository;
import fim.unipassau.de.scratch1984.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

/**
 * A service providing methods related to loading information to be displayed in tables with multiple pages.
 */
@Service
public class PageService {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger logger = LoggerFactory.getLogger(PageService.class);

    /**
     * The experiment repository to use for database queries related to experiment data.
     */
    private final ExperimentRepository experimentRepository;

    /**
     * The experiment data repository to use for database queries related to participant numbers.
     */
    private final ExperimentDataRepository experimentDataRepository;

    /**
     * The participant repository to use for database queries participation data.
     */
    private final ParticipantRepository participantRepository;

    /**
     * The course repository to use for database queries related to course data.
     */
    private final CourseRepository courseRepository;

    /**
     * The course participant repository to use for database queries related to course participation.
     */
    private final CourseParticipantRepository courseParticipantRepository;

    /**
     * The course experiment repository to use for database queries related to experiment being offered in a course.
     */
    private final CourseExperimentRepository courseExperimentRepository;

    /**
     * Constructs a page service with the given dependencies.
     *
     * @param experimentRepository The {@link ExperimentRepository} to use.
     * @param experimentDataRepository The {@link ExperimentDataRepository} to use.
     * @param participantRepository The {@link ParticipantRepository} to use.
     * @param courseRepository The {@link CourseRepository} to use.
     * @param courseParticipantRepository The {@link CourseParticipantRepository} to use.
     * @param courseExperimentRepository The {@link CourseExperimentRepository} to use.
     */
    @Autowired
    public PageService(final ExperimentRepository experimentRepository,
                       final ExperimentDataRepository experimentDataRepository,
                       final ParticipantRepository participantRepository,
                       final CourseRepository courseRepository,
                       final CourseParticipantRepository courseParticipantRepository,
                       final CourseExperimentRepository courseExperimentRepository) {
        this.experimentRepository = experimentRepository;
        this.experimentDataRepository = experimentDataRepository;
        this.participantRepository = participantRepository;
        this.courseRepository = courseRepository;
        this.courseParticipantRepository = courseParticipantRepository;
        this.courseExperimentRepository = courseExperimentRepository;
    }

    /**
     * Returns a page of experiments corresponding to the parameters passed in the given pageable.
     *
     * @param pageable The pageable containing the page size and page number.
     * @return The experiment page.
     */
    @Transactional
    public Page<ExperimentTableProjection> getExperimentPage(final Pageable pageable) {
        checkPageable(pageable);
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        Page<ExperimentTableProjection> experiments = experimentRepository.findAllProjectedBy(
                PageRequest.of(currentPage, pageSize, Sort.by("id").descending()));

        if (experiments.isEmpty()) {
            logger.info("Could not find any experiments for the page with page size of " + pageSize
                    + ", current page of " + currentPage + " and offset of " + pageable.getOffset() + "!");
        }

        return experiments;
    }

    /**
     * Returns a page of courses corresponding to the parameters passed in the given pageable.
     *
     * @param pageable The {@link Pageable} containing the page size and page number.
     * @return The course page.
     */
    @Transactional
    public Page<CourseTableProjection> getCoursePage(final Pageable pageable) {
        checkPageable(pageable);

        Page<CourseTableProjection> courses = courseRepository.findAllProjectedBy(
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("id").descending()));

        if (courses.isEmpty()) {
            logger.info("Could not find any courses for the page with page size of " + pageable.getPageSize()
                    + ", current page of " + pageable.getPageNumber() + " and offset of " + pageable.getOffset() + "!");
        }

        return courses;
    }

    /**
     * Returns a page of {@link CourseExperimentProjection}s containing information about the experiments that are part
     * of this course.
     *
     * @param pageable The {@link Pageable} containing the page size and page number.
     * @param courseId The id of the course.
     * @return The course experiment page.
     */
    @Transactional
    public Page<CourseExperimentProjection> getCourseExperimentPage(final Pageable pageable, final int courseId) {
        if (courseId < Constants.MIN_ID) {
            logger.error("Cannot return course experiment page for course with invalid id " + courseId + "!");
            throw new IllegalArgumentException("Cannot return course experiment page for course with invalid id "
                    + courseId + "!");
        }

        checkPageable(pageable);
        Course course = courseRepository.getOne(courseId);
        Page<CourseExperimentProjection> experiments = courseExperimentRepository.findAllProjectedByCourse(pageable,
                course);

        if (experiments.isEmpty()) {
            logger.info("Could not find any course experiments for the page with page size of " + pageable.getPageSize()
                    + ", current page of " + pageable.getPageNumber() + " and offset of " + pageable.getOffset() + "!");
        }

        return experiments;
    }

    /**
     * Returns a page of {@link ExperimentTableProjection}s in which the user with the given id is participating
     * corresponding to the parameters passed in the given pageable.
     *
     * @param pageable The pageable containing the page size and page number.
     * @param userId The user id to search for.
     * @return The page of {@link ExperimentTableProjection}s.
     */
    @Transactional
    public Page<ExperimentTableProjection> getExperimentParticipantPage(final Pageable pageable, final int userId) {
        if (userId < Constants.MIN_ID) {
            logger.error("Cannot return participant experiment page for user with invalid id " + userId + "!");
            throw new IllegalArgumentException("Cannot return participant experiment page for user with invalid id "
                    + userId + "!");
        }

        checkPageable(pageable);
        return experimentRepository.findExperimentsByParticipant(userId, pageable);
    }

    /**
     * Returns a page of {@link CourseTableProjection}s in which the user with the given id is participating
     * corresponding to the parameters passed in the given pageable.
     *
     * @param pageable The {@link Pageable} containing the page size and page number.
     * @param userId The user id to search for.
     * @return The page of {@link CourseTableProjection}s.
     */
    public Page<CourseTableProjection> getCourseParticipantPage(final Pageable pageable, final int userId) {
        if (userId < Constants.MIN_ID) {
            logger.error("Cannot return participant course page for user with invalid id " + userId + "!");
            throw new IllegalArgumentException("Cannot return participant course page for user with invalid id "
                    + userId + "!");
        }

        checkPageable(pageable);
        return courseRepository.findCoursesByParticipant(userId, pageable);
    }

    /**
     * Retrieves a page of participants for the experiment with the given id. If no corresponding experiment exists in
     * the database a {@link NotFoundException} is thrown instead.
     *
     * @param id The experiment id.
     * @param pageable The pageable containing the page size and page number.
     * @return The participant page.
     */
    @Transactional
    public Page<Participant> getParticipantPage(final int id, final Pageable pageable) {
        checkPageable(pageable);
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();

        if (id < Constants.MIN_ID) {
            logger.error("Cannot find participant data for experiment with invalid id " + id + "!");
            throw new IllegalArgumentException("Cannot find participant data for experiment with invalid id " + id
                    + "!");
        }

        Experiment experiment = experimentRepository.getOne(id);
        Page<Participant> participants;

        try {
            participants = participantRepository.findAllByExperiment(experiment, PageRequest.of(currentPage, pageSize,
                    Sort.by("user").descending()));
        } catch (EntityNotFoundException e) {
            logger.error("Could not find experiment with id " + id + " in the database!", e);
            throw new NotFoundException("Could not find experiment with id " + id + " in the database!", e);
        }

        return participants;
    }

    /**
     * Returns the number of the last page for the experiment pagination.
     *
     * @return The last page value.
     */
    @Transactional
    public int computeLastExperimentPage() {
        int rows = countExperimentRows();
        return computeLastPage(rows) + 1;
    }

    /**
     * Returns the number of the last page for the course pagination.
     *
     * @return The last page value.
     */
    @Transactional
    public int computeLastCoursePage() {
        int rows = countCourseRows();
        return computeLastPage(rows) + 1;
    }

    /**
     * Returns the number of the last experiment page for the course with the given id.
     *
     * @param courseId The id of the course.
     * @return The last page value.
     */
    @Transactional
    public int getLastCourseExperimentPage(final int courseId) {
        if (courseId < Constants.MIN_ID) {
            logger.error("Cannot calculate the last course experiment page for course with invalid id " + courseId
                    + "!");
            throw new IllegalArgumentException("Cannot calculate the last course experiment page for course with "
                    + "invalid id " + courseId + "!");
        }

        int rows = courseExperimentRepository.getCourseExperimentRowCount(courseId);
        return computeLastPage(rows) + 1;
    }

    /**
     * Returns the number of the last page for the participant experiment pagination.
     *
     * @param userId The user id of the participant.
     * @return The last page value.
     */
    @Transactional
    public int getLastExperimentPage(final int userId) {
        if (userId < Constants.MIN_ID) {
            logger.error("Cannot calculate the last participant experiment page for user with invalid id " + userId
                    + "!");
            throw new IllegalArgumentException("Cannot calculate the last participant experiment page for user with "
                    + "invalid id " + userId + "!");
        }

        int rows = experimentRepository.getParticipantPageCount(userId);
        return computeLastPage(rows) + 1;
    }

    /**
     * Returns the number of the last page for the participant course pagination.
     *
     * @param userId The user id of the participant.
     * @return The last page value.
     */
    @Transactional
    public int getLastCoursePage(final int userId) {
        if (userId < Constants.MIN_ID) {
            logger.error("Cannot calculate the last participant course page for user with invalid id " + userId + "!");
            throw new IllegalArgumentException("Cannot calculate the last participant course page for user with "
                    + "invalid id " + userId + "!");
        }

        int rows = courseRepository.getParticipantPageCount(userId);
        return computeLastPage(rows) + 1;
    }

    /**
     * Returns the number of the last page for the participant pagination for the experiment with the given id.
     *
     * @param id The experiment id to search for.
     * @return The last page value.
     */
    @Transactional
    public int getLastParticipantPage(final int id) {
        ExperimentData experimentData = experimentDataRepository.findByExperiment(id);

        if (experimentData == null) {
            return 0;
        } else {
            int participants = experimentData.getParticipants();
            return computeLastPage(participants);
        }
    }

    /**
     * Verifies, that the given pageable is not null and that its page size is set to the number defined in the
     * {@link Constants} class. If the pageable is invalid, an {@link IllegalArgumentException} is thrown instead.
     *
     * @param pageable The {@link Pageable} to check
     */
    private void checkPageable(final Pageable pageable) {
        if (pageable == null) {
            logger.error("Cannot return a page with pageable null!");
            throw new IllegalArgumentException("Cannot return a page with pageable null!");
        } else if (pageable.getPageSize() != Constants.PAGE_SIZE) {
            logger.error("Cannot return a page with invalid page size of " + pageable.getPageSize() + "!");
            throw new IllegalArgumentException("Cannot return a page with invalid page size of "
                    + pageable.getPageSize() + "!");
        }
    }

    /**
     * Returns the number of rows currently present in the experiment table. If the number of rows is too big to be
     * represented by an int value, the maximum integer is returned instead.
     *
     * @return The row count value.
     */
    private int countExperimentRows() {
        long rows = experimentRepository.count();
        return checkRowCount(rows);
    }

    /**
     * Returns the number of rows currently present in the course table. If the number of rows is too big to be
     * represented by an int value, the maximum integer is returned instead.
     *
     * @return The row count value.
     */
    private int countCourseRows() {
        long rows = courseRepository.count();
        return checkRowCount(rows);
    }

    /**
     * Checks, whether the given number of rows is too big to be represented by an integer. If this is the case, the
     * maximum integer value is returned instead.
     *
     * @param rows The number to be checked.
     * @return The number cast to an integer.
     */
    private int checkRowCount(final long rows) {
        if (rows > (long) Integer.MAX_VALUE) {
            logger.error("Can't return the correct row count as number of rows is too big to be cast to an int!");
            return Integer.MAX_VALUE;
        }

        return (int) rows;
    }

    /**
     * Returns the number of the last page for the given amount of elements.
     *
     * @param elements The number of elements.
     * @return The last page.
     */
    private int computeLastPage(final int elements) {
        if (elements <= Constants.PAGE_SIZE) {
            return 0;
        } else if (elements % Constants.PAGE_SIZE == 0) {
            return elements / Constants.PAGE_SIZE - 1;
        } else {
            return elements / Constants.PAGE_SIZE;
        }
    }

}
