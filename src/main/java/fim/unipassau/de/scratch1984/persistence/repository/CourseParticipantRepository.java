package fim.unipassau.de.scratch1984.persistence.repository;

import fim.unipassau.de.scratch1984.persistence.entity.Course;
import fim.unipassau.de.scratch1984.persistence.entity.CourseParticipant;
import fim.unipassau.de.scratch1984.persistence.entity.CourseParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * A repository providing functionality for retrieving participant information for a course.
 */
public interface CourseParticipantRepository extends JpaRepository<CourseParticipant, CourseParticipantId> {

    /**
     * Returns a list of all {@link CourseParticipant}s containing the ids of all users participating in the given
     * course.
     *
     * @param course The {@link Course} to search for.
     * @return A list of all course participants.
     */
    List<CourseParticipant> findAllByCourse(Course course);

}
