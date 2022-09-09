package fim.unipassau.de.scratch1984.persistence.repository;

import fim.unipassau.de.scratch1984.persistence.entity.CourseParticipant;
import fim.unipassau.de.scratch1984.persistence.entity.CourseParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * A repository providing functionality for retrieving participant information for a course.
 */
public interface CourseParticipantRepository extends JpaRepository<CourseParticipant, CourseParticipantId> {
}
