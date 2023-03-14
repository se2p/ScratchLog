package fim.unipassau.de.scratch1984.persistence;

import fim.unipassau.de.scratch1984.persistence.entity.Course;
import fim.unipassau.de.scratch1984.persistence.entity.CourseParticipant;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.repository.CourseParticipantRepository;
import fim.unipassau.de.scratch1984.util.enums.Language;
import fim.unipassau.de.scratch1984.util.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
public class CourseParticipantRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CourseParticipantRepository repository;

    private static final LocalDateTime DATE = LocalDateTime.now();
    private Course course1 = new Course(null, "Course 1", "Description 1", "", false, DATE);
    private Course course2 = new Course(null, "Course 2", "Description 2", "", false, DATE);
    private Course course3 = new Course(null, "Course 3", "Description 3", "", false, DATE);
    private User user1 = new User("user1", "part1@test.de", Role.PARTICIPANT, Language.ENGLISH, "user", null);
    private User user2 = new User("user2", "part2@test.de", Role.PARTICIPANT, Language.ENGLISH, "user", "secret4");
    private User user3 = new User("user3", "part3@test.de", Role.PARTICIPANT, Language.ENGLISH, "user", null);
    private CourseParticipant participant1 = new CourseParticipant(user1, course1, DATE);
    private CourseParticipant participant2 = new CourseParticipant(user2, course1, DATE);
    private CourseParticipant participant3 = new CourseParticipant(user3, course1, DATE);
    private CourseParticipant participant4 = new CourseParticipant(user1, course2, DATE);

    @BeforeEach
    public void setUp() {
        user1.setLastLogin(LocalDateTime.now());
        user2.setLastLogin(LocalDateTime.now());
        user3.setLastLogin(LocalDateTime.now());
        course1 = entityManager.persist(course1);
        course2 = entityManager.persist(course2);
        course3 = entityManager.persist(course3);
        user1 = entityManager.persist(user1);
        user2 = entityManager.persist(user2);
        user3 = entityManager.persist(user3);
        participant1 = entityManager.persist(participant1);
        participant2 = entityManager.persist(participant2);
        participant3 = entityManager.persist(participant3);
        participant4 = entityManager.persist(participant4);
    }

    @Test
    public void testGetCourseParticipantRowCount() {
        assertAll(
                () -> assertEquals(3, repository.getCourseParticipantRowCount(course1.getId())),
                () -> assertEquals(1, repository.getCourseParticipantRowCount(course2.getId())),
                () -> assertEquals(0, repository.getCourseParticipantRowCount(course3.getId()))
        );
    }

}
