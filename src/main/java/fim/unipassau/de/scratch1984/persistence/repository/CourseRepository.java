package fim.unipassau.de.scratch1984.persistence.repository;

import fim.unipassau.de.scratch1984.persistence.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * A repository providing functionality for retrieving course data.
 */
public interface CourseRepository extends JpaRepository<Course, Integer> {

    /**
     * Checks, whether a course with the given id already exists in the database.
     *
     * @param id The id to search for.
     * @return {@code true} iff a course with the given id was found.
     */
    boolean existsById(int id);

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

}
