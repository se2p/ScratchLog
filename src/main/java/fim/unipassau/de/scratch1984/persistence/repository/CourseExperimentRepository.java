package fim.unipassau.de.scratch1984.persistence.repository;

import fim.unipassau.de.scratch1984.persistence.entity.CourseExperiment;
import fim.unipassau.de.scratch1984.persistence.entity.CourseExperimentId;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * A repository providing functionality for retrieving information about experiments conducted in a course.
 */
public interface CourseExperimentRepository extends JpaRepository<CourseExperiment, CourseExperimentId> {
}
