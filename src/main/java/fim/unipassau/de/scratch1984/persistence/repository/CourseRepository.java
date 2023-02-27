package fim.unipassau.de.scratch1984.persistence.repository;

import fim.unipassau.de.scratch1984.persistence.entity.Course;
import fim.unipassau.de.scratch1984.persistence.projection.CourseTableProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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
     * Returns a list of the first courses up to the given limit whose title contains the given query value.
     *
     * @param query The title to search for.
     * @param limit The maximum number of results to return.
     * @return A list of {@link CourseTableProjection}s.
     */
    @Query(nativeQuery = true, value = "SELECT * FROM course AS c WHERE c.title LIKE CONCAT('%', :query, '%')"
            + " LIMIT :limit")
    List<CourseTableProjection> findCourseSuggestions(@Param("query") String query, @Param("limit") int limit);

    /**
     * Returns a list of at most as many courses as the given limit with the given offset whose title contains the
     * given query value.
     *
     * @param query The title to search for.
     * @param limit The maximum amount of results to be returned.
     * @param offset The offset used to return new results.
     * @return A list of {@link CourseTableProjection}s.
     */
    @Query(nativeQuery = true, value = "SELECT * FROM course AS c WHERE c.title LIKE CONCAT('%', :query, '%')"
            + " LIMIT :limit OFFSET :offset")
    List<CourseTableProjection> findCourseResults(@Param("query") String query, @Param("limit") int limit,
                                                  @Param("offset") int offset);

    /**
     * Returns the number of courses whose title contains the given query value.
     *
     * @param query The title to search for.
     * @return The number of course results.
     */
    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM course AS c WHERE c.title LIKE CONCAT('%', :query, '%')")
    int getCourseResultsCount(@Param("query") String query);

    /**
     * Returns a page of courses in which the user with the given id is participating.
     *
     * @param userId The user id to search for.
     * @param pageable The pageable to use.
     * @return A page of {@link CourseTableProjection}s.
     */
    @Query(nativeQuery = true, value = "SELECT * FROM course AS c INNER JOIN course_participant AS p ON p.course_id"
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
