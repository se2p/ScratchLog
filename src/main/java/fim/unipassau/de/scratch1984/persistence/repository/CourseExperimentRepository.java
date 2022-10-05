package fim.unipassau.de.scratch1984.persistence.repository;

import fim.unipassau.de.scratch1984.persistence.entity.Course;
import fim.unipassau.de.scratch1984.persistence.entity.CourseExperiment;
import fim.unipassau.de.scratch1984.persistence.entity.CourseExperimentId;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.projection.CourseExperimentProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * A repository providing functionality for retrieving information about experiments conducted in a course.
 */
public interface CourseExperimentRepository extends JpaRepository<CourseExperiment, CourseExperimentId> {

    /**
     * Checks, whether a course experiment for the given course and experiment exists in the database.
     *
     * @param course The {@link Course} to search for.
     * @param experiment The {@link Experiment} to search for.
     * @return {@code true} iff an entry already exists.
     */
    boolean existsByCourseAndExperiment(Course course, Experiment experiment);

    /**
     * Returns a list of all {@link CourseExperiment}s containing the ids of all experiments that are part of the
     * course.
     *
     * @param course The {@link Course} to search for.
     * @return The course experiment list.
     */
    List<CourseExperiment> findAllByCourse(Course course);

    /**
     * Returns a list of all {@link CourseExperiment}s containing the ids of all courses the experiment is part of.
     *
     * @param experiment The {@link Experiment} to search for.
     * @return The course experiment list.
     */
    List<CourseExperiment> findAllByExperiment(Experiment experiment);

    /**
     * Returns the {@link CourseExperiment} containing information to which course the given experiment belongs.
     *
     * @param experiment The {@link Experiment} to search for.
     * @return The course experiment.
     */
    Optional<CourseExperiment> findByExperiment(Experiment experiment);

    /**
     * Returns a list of experiment information of all experiments that are part of the course.
     *
     * @param pageable The {@link Pageable} to use.
     * @param course The {@link Course} to search for.
     * @return A new course experiment page.
     */
    Page<CourseExperimentProjection> findAllProjectedByCourse(Pageable pageable, Course course);

    /**
     * Returns the number of experiment rows for the given course.
     *
     * @param id The course id to search for.
     * @return The number of rows.
     */
    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM course_experiment AS c WHERE c.course_id = :id")
    int getCourseExperimentRowCount(@Param("id") int id);

}
