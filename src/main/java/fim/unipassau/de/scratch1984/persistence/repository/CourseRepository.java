package fim.unipassau.de.scratch1984.persistence.repository;

import fim.unipassau.de.scratch1984.persistence.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * A repository providing functionality for retrieving course data.
 */
public interface CourseRepository extends JpaRepository<Course, Integer> {
}
