package fim.unipassau.de.scratch1984.persistence;

import fim.unipassau.de.scratch1984.persistence.entity.Course;
import fim.unipassau.de.scratch1984.persistence.entity.CourseParticipant;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.projection.CourseTableProjection;
import fim.unipassau.de.scratch1984.persistence.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
public class CourseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CourseRepository courseRepository;

    private static final String SHORT_QUERY = "Course";
    private static final String TITLE_QUERY = "Course 1";
    private static final String NO_RESULTS = "blubb";
    private static final int LIMIT = 3;
    private static final int SEARCH_LIMIT = 5;
    private static final Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
    private PageRequest pageRequest;
    private User user1 = new User("user1", "email1", "PARTICIPANT", "ENGLISH", "password", "secret1");
    private User user2 = new User("user2", "email2", "PARTICIPANT", "ENGLISH", "password", "secret2");
    private Course course1 = new Course(null, "Course 1", "Description 1", "", false, timestamp);
    private Course course2 = new Course(null, "Course 2", "Description 2", "", false, timestamp);
    private Course course3 = new Course(null, "Course 3", "Description 3", "", false, timestamp);
    private Course course4 = new Course(null, "Course 4", "Description 4", "", false, timestamp);
    private CourseParticipant participant1 = new CourseParticipant(user1, course1, timestamp);
    private CourseParticipant participant2 = new CourseParticipant(user1, course2, timestamp);
    private CourseParticipant participant3 = new CourseParticipant(user1, course3, timestamp);
    private CourseParticipant participant4 = new CourseParticipant(user1, course4, timestamp);
    private CourseParticipant participant5 = new CourseParticipant(user2, course1, timestamp);

    @BeforeEach
    public void setUp() {
        pageRequest = PageRequest.of(0, LIMIT);
        user1 = entityManager.persist(user1);
        user2 = entityManager.persist(user2);
        course1 = entityManager.persist(course1);
        course2 = entityManager.persist(course2);
        course3 = entityManager.persist(course3);
        course4 = entityManager.persist(course4);
        participant1 = entityManager.persist(participant1);
        participant2 = entityManager.persist(participant2);
        participant3 = entityManager.persist(participant3);
        participant4 = entityManager.persist(participant4);
        participant5 = entityManager.persist(participant5);
    }

    @Test
    public void testFindCourseSuggestions() {
        List<CourseTableProjection> projections = courseRepository.findCourseSuggestions(SHORT_QUERY, SEARCH_LIMIT);
        assertAll(
                () -> assertEquals(4, projections.size()),
                () -> assertTrue(projections.stream().anyMatch(projection
                        -> projection.getTitle().equals(course1.getTitle()))),
                () -> assertTrue(projections.stream().anyMatch(projection
                        -> projection.getTitle().equals(course2.getTitle()))),
                () -> assertTrue(projections.stream().anyMatch(projection
                        -> projection.getTitle().equals(course3.getTitle()))),
                () -> assertTrue(projections.stream().anyMatch(projection
                        -> projection.getTitle().equals(course4.getTitle())))
        );
    }

    @Test
    public void testFindCourseSuggestionsTitleQuery() {
        List<CourseTableProjection> projections = courseRepository.findCourseSuggestions(TITLE_QUERY, SEARCH_LIMIT);
        assertAll(
                () -> assertEquals(1, projections.size()),
                () -> assertTrue(projections.stream().anyMatch(projection
                        -> projection.getTitle().equals(course1.getTitle())))
        );
    }

    @Test
    public void testFindCourseSuggestionsNoSuggestions() {
        assertTrue(courseRepository.findCourseSuggestions(NO_RESULTS, SEARCH_LIMIT).isEmpty());
    }

    @Test
    public void testFindCourseResults() {
        List<CourseTableProjection> projections = courseRepository.findCourseResults(SHORT_QUERY, LIMIT, 0);
        assertAll(
                () -> assertEquals(3, projections.size()),
                () -> assertTrue(projections.stream().anyMatch(projection
                        -> projection.getTitle().equals(course1.getTitle()))),
                () -> assertTrue(projections.stream().anyMatch(projection
                        -> projection.getTitle().equals(course2.getTitle()))),
                () -> assertTrue(projections.stream().anyMatch(projection
                        -> projection.getTitle().equals(course3.getTitle())))
        );
    }

    @Test
    public void testFindCourseResultsAll() {
        List<CourseTableProjection> projections = courseRepository.findCourseResults(SHORT_QUERY, SEARCH_LIMIT, 0);
        assertAll(
                () -> assertEquals(4, projections.size()),
                () -> assertTrue(projections.stream().anyMatch(projection
                        -> projection.getTitle().equals(course1.getTitle()))),
                () -> assertTrue(projections.stream().anyMatch(projection
                        -> projection.getTitle().equals(course2.getTitle()))),
                () -> assertTrue(projections.stream().anyMatch(projection
                        -> projection.getTitle().equals(course3.getTitle()))),
                () -> assertTrue(projections.stream().anyMatch(projection
                        -> projection.getTitle().equals(course4.getTitle())))
        );
    }

    @Test
    public void testFindCourseResultsOffset() {
        List<CourseTableProjection> projections = courseRepository.findCourseResults(SHORT_QUERY, LIMIT, 2);
        assertAll(
                () -> assertEquals(2, projections.size()),
                () -> assertTrue(projections.stream().anyMatch(projection
                        -> projection.getTitle().equals(course4.getTitle()))),
                () -> assertTrue(projections.stream().anyMatch(projection
                        -> projection.getTitle().equals(course3.getTitle())))
        );
    }

    @Test
    public void testFindCourseResultsNone() {
        assertTrue(courseRepository.findCourseResults(SHORT_QUERY, LIMIT, 5).isEmpty());
    }

    @Test
    public void testGetCourseResultsCount() {
        assertAll(
                () -> assertEquals(4, courseRepository.getCourseResultsCount(SHORT_QUERY)),
                () -> assertEquals(1, courseRepository.getCourseResultsCount(TITLE_QUERY)),
                () -> assertEquals(0, courseRepository.getCourseResultsCount(NO_RESULTS))
        );
    }

    @Test
    public void testFindCoursesByParticipant() {
        Page<CourseTableProjection> page = courseRepository.findCoursesByParticipant(user1.getId(), pageRequest);
        assertAll(
                () -> assertEquals(LIMIT, page.getNumberOfElements()),
                () -> assertTrue(page.stream().anyMatch(projection
                        -> projection.getTitle().equals(course1.getTitle()))),
                () -> assertTrue(page.stream().anyMatch(projection
                        -> projection.getTitle().equals(course2.getTitle()))),
                () -> assertTrue(page.stream().anyMatch(projection -> projection.getTitle().equals(course3.getTitle())))
        );
    }

    @Test
    public void testFindCoursesByParticipantOffset() {
        pageRequest = PageRequest.of(1, LIMIT);
        Page<CourseTableProjection> page = courseRepository.findCoursesByParticipant(user1.getId(), pageRequest);
        assertAll(
                () -> assertEquals(1, page.getNumberOfElements()),
                () -> assertTrue(page.stream().anyMatch(projection -> projection.getTitle().equals(course4.getTitle())))
        );
    }

    @Test
    public void testFindCoursesByParticipantTooFewEntries() {
        Page<CourseTableProjection> page = courseRepository.findCoursesByParticipant(user2.getId(), pageRequest);
        assertAll(
                () -> assertEquals(1, page.getNumberOfElements()),
                () -> assertTrue(page.stream().findFirst().get().getTitle().equals(course1.getTitle()))
        );
    }

    @Test
    public void testGetParticipantPageCount() {
        assertAll(
                () -> assertEquals(4, courseRepository.getParticipantPageCount(user1.getId())),
                () -> assertEquals(1, courseRepository.getParticipantPageCount(user2.getId()))
        );
    }

}
