package fim.unipassau.de.scratch1984.persistence.repository;

import fim.unipassau.de.scratch1984.persistence.entity.Course;
import fim.unipassau.de.scratch1984.persistence.entity.CourseParticipant;
import fim.unipassau.de.scratch1984.persistence.entity.CourseParticipantId;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * A repository providing functionality for retrieving participant information for a course.
 */
public interface CourseParticipantRepository extends JpaRepository<CourseParticipant, CourseParticipantId> {

    /**
     * Checks, whether a course participation for the given course and user exists in the database.
     *
     * @param course The {@link Course} to search for.
     * @param user The {@link User} to search for.
     * @return {@code true} iff an entry already exists.
     */
    boolean existsByCourseAndUser(Course course, User user);

    /**
     * Returns a list of all {@link CourseParticipant}s containing the ids of all users participating in the given
     * course.
     *
     * @param course The {@link Course} to search for.
     * @return A list of all course participants.
     */
    List<CourseParticipant> findAllByCourse(Course course);

    /**
     * Returns a page of {@link CourseParticipant}s for the given course, if any entries exist.
     *
     * @param course The {@link Course} to search for.
     * @param pageable The {@link Pageable} to use.
     * @return The course participant page.
     */
    Page<CourseParticipant> findAllByCourse(Course course, Pageable pageable);

    /**
     * Returns the number of course participant rows for the given course.
     *
     * @param id The course id to search for.
     * @return The number of rows.
     */
    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM course_participant AS c WHERE c.course_id = :id")
    int getCourseParticipantRowCount(@Param("id") int id);

}
