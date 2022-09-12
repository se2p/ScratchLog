package fim.unipassau.de.scratch1984.persistence.repository;

import fim.unipassau.de.scratch1984.persistence.entity.Course;
import fim.unipassau.de.scratch1984.persistence.projection.CourseTableProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * A repository providing functionality for retrieving course data.
 */
public interface CourseRepository extends JpaRepository<Course, Integer> {

    /**
     * Checks, whether a course with the given title already exists in the database.
     *
     * @param title The title to search for.
     * @return {@code true} iff a course with the given title was found.
     */
    boolean existsByTitle(String title);

    /**
     * Returns the course identified by the given title, if one exists.
     *
     * @param title The title to search for.
     * @return An {@link Optional} containing the course data or an empty optional, if no database entry could be found.
     */
    Optional<Course> findByTitle(String title);

    /**
     * Returns the course identified by the given id, if one exists.
     *
     * @param id The id to search for.
     * @return An {@link Optional} containing the course data or an empty optional, if no database entry could be found.
     */
    Optional<Course> findById(int id);

    /**
     * Returns a page of courses corresponding to the parameters set in the pageable.
     *
     * @param pageable The pageable to use.
     * @return A new course page.
     */
    Page<CourseTableProjection> findAllProjectedBy(Pageable pageable);

    /**
     * Returns a page of courses in which the user with the given id is participating.
     *
     * @param userId The user id to search for.
     * @param pageable The pageable to use.
     * @return A page of {@link CourseTableProjection}s.
     */
    @Query(nativeQuery = true, value = "SELECT * FROM courses AS c INNER JOIN course_participant AS p ON p.course_id"
            + " = c.id WHERE p.user_id = :participant")
    Page<CourseTableProjection> findCoursesByParticipant(@Param("participant") int userId, Pageable pageable);

    /**
     * Returns the number of course pages for the given participant.
     *
     * @param userId The user id to search for.
     * @return The page count.
     */
    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM course AS c INNER JOIN course_participant AS p ON"
            + " p.course_id = c.id WHERE p.user_id = :participant")
    int getParticipantPageCount(@Param("participant") int userId);

}
