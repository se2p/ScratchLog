package fim.unipassau.de.scratch1984.persistence;

import fim.unipassau.de.scratch1984.persistence.entity.Course;
import fim.unipassau.de.scratch1984.persistence.entity.CourseExperiment;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.repository.CourseExperimentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
public class CourseExperimentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CourseExperimentRepository repository;

    private static final Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
    private Course course1 = new Course(null, "Course 1", "Description 1", "", false, timestamp);
    private Course course2 = new Course(null, "Course 2", "Description 2", "", false, timestamp);
    private Course course3 = new Course(null, "Course 3", "Description 3", "", false, timestamp);
    private Experiment experiment1 = new Experiment(null, "Experiment 1", "Description for experiment 1", "Some info",
            "Some postscript", false, "url");
    private Experiment experiment2 = new Experiment(null, "Experiment 2", "Description for experiment 2", "Some info",
            "Some postscript", true, "url");
    private Experiment experiment3 = new Experiment(null, "Experiment 3", "Description for experiment 3", "Some info",
            "Some postscript", false, "url");
    private CourseExperiment courseExperiment1 = new CourseExperiment(course1, experiment1, timestamp);
    private CourseExperiment courseExperiment2 = new CourseExperiment(course1, experiment2, timestamp);
    private CourseExperiment courseExperiment3 = new CourseExperiment(course1, experiment3, timestamp);
    private CourseExperiment courseExperiment4 = new CourseExperiment(course2, experiment1, timestamp);

    @BeforeEach
    public void setUp() {
        course1 = entityManager.persist(course1);
        course2 = entityManager.persist(course2);
        course3 = entityManager.persist(course3);
        experiment1 = entityManager.persist(experiment1);
        experiment2 = entityManager.persist(experiment2);
        experiment3 = entityManager.persist(experiment3);
        courseExperiment1 = entityManager.persist(courseExperiment1);
        courseExperiment2 = entityManager.persist(courseExperiment2);
        courseExperiment3 = entityManager.persist(courseExperiment3);
        courseExperiment4 = entityManager.persist(courseExperiment4);
    }

    @Test
    public void testGetCourseExperimentRowCount() {
        assertAll(
                () -> assertEquals(3, repository.getCourseExperimentRowCount(course1.getId())),
                () -> assertEquals(1, repository.getCourseExperimentRowCount(course2.getId())),
                () -> assertEquals(0, repository.getCourseExperimentRowCount(course3.getId()))
        );
    }

}
