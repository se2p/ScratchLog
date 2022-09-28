package fim.unipassau.de.scratch1984.application;

import fim.unipassau.de.scratch1984.application.exception.IncompleteDataException;
import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.exception.StoreException;
import fim.unipassau.de.scratch1984.application.service.CourseService;
import fim.unipassau.de.scratch1984.persistence.entity.Course;
import fim.unipassau.de.scratch1984.persistence.entity.CourseExperiment;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.repository.CourseExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.CourseParticipantRepository;
import fim.unipassau.de.scratch1984.persistence.repository.CourseRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ParticipantRepository;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
import fim.unipassau.de.scratch1984.web.dto.CourseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    @InjectMocks
    private CourseService courseService;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseParticipantRepository courseParticipantRepository;

    @Mock
    private CourseExperimentRepository courseExperimentRepository;

    @Mock
    private ExperimentRepository experimentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ParticipantRepository participantRepository;

    private static final String TITLE = "My Course";
    private static final String DESCRIPTION = "A description";
    private static final String CONTENT = "content";
    private static final String USERNAME = "user";
    private static final String BLANK = "    ";
    private static final int ID = 1;
    private static final int INVALID_ID = 2;
    private static final Timestamp TIMESTAMP = Timestamp.valueOf(LocalDateTime.now());
    private final CourseDTO courseDTO = new CourseDTO(ID, TITLE, DESCRIPTION, CONTENT, false, LocalDateTime.now());
    private final Course course = new Course(ID, TITLE, DESCRIPTION, CONTENT, false, TIMESTAMP);
    private final Experiment experiment = new Experiment(ID, TITLE, DESCRIPTION, "", "", false, "url");
    private final User user = new User(USERNAME, "email", "PARTICIPANT", "ENGLISH", "password", "secret");
    private final CourseExperiment courseExperiment = new CourseExperiment(course, experiment, TIMESTAMP);

    @BeforeEach
    public void setUp() {
        courseDTO.setId(ID);
        courseDTO.setTitle(TITLE);
        courseDTO.setDescription(DESCRIPTION);
        courseDTO.setLastChanged(LocalDateTime.now());
        course.setId(ID);
        course.setLastChanged(TIMESTAMP);
        user.setRole("PARTICIPANT");
        user.setId(ID);
        user.setActive(false);
        user.setSecret("secret");
    }

    @Test
    public void testExistsCourseByTitle() {
        when(courseRepository.existsByTitle(TITLE)).thenReturn(true);
        assertAll(
                () -> assertTrue(courseService.existsCourse(TITLE)),
                () -> assertFalse(courseService.existsCourse(DESCRIPTION))
        );
        verify(courseRepository).existsByTitle(TITLE);
        verify(courseRepository).existsByTitle(DESCRIPTION);
    }

    @Test
    public void testExistsCourseByTitleInvalid() {
        assertAll(
                () -> assertFalse(courseService.existsCourse(null)),
                () -> assertFalse(courseService.existsCourse(BLANK))
        );
        verify(courseRepository, never()).existsByTitle(anyString());
    }

    @Test
    public void testExistsCourseByTitleAndId() {
        when(courseRepository.findByTitle(TITLE)).thenReturn(Optional.of(course));
        assertAll(
                () -> assertTrue(courseService.existsCourse(INVALID_ID, TITLE)),
                () -> assertFalse(courseService.existsCourse(ID, TITLE))
        );
        verify(courseRepository, times(2)).findByTitle(TITLE);
    }

    @Test
    public void testExistsCourseByTitleAndIdNoCourse() {
        assertFalse(courseService.existsCourse(ID, TITLE));
        verify(courseRepository).findByTitle(TITLE);
    }

    @Test
    public void testExistsCourseInvalidTitleAndId() {
        assertAll(
                () -> assertFalse(courseService.existsCourse(-1, TITLE)),
                () -> assertFalse(courseService.existsCourse(ID, BLANK)),
                () -> assertFalse(courseService.existsCourse(ID, null))
        );
        verify(courseRepository, never()).findByTitle(anyString());
    }

    @Test
    public void testExistsCourseExperiment() {
        when(courseRepository.getOne(ID)).thenReturn(course);
        when(experimentRepository.findByTitle(TITLE)).thenReturn(experiment);
        when(courseExperimentRepository.existsByCourseAndExperiment(course, experiment)).thenReturn(true);
        assertTrue(courseService.existsCourseExperiment(ID, TITLE));
        verify(courseRepository).getOne(ID);
        verify(experimentRepository).findByTitle(TITLE);
        verify(courseExperimentRepository).existsByCourseAndExperiment(course, experiment);
    }

    @Test
    public void testExistsCourseExperimentNoEntry() {
        when(courseRepository.getOne(ID)).thenReturn(course);
        when(experimentRepository.findByTitle(TITLE)).thenReturn(experiment);
        assertFalse(courseService.existsCourseExperiment(ID, TITLE));
        verify(courseRepository).getOne(ID);
        verify(experimentRepository).findByTitle(TITLE);
        verify(courseExperimentRepository).existsByCourseAndExperiment(course, experiment);
    }

    @Test
    public void testExistsCourseExperimentEntityNotFound() {
        when(courseRepository.getOne(ID)).thenReturn(course);
        when(experimentRepository.findByTitle(TITLE)).thenReturn(experiment);
        when(courseExperimentRepository.existsByCourseAndExperiment(course, experiment)).thenThrow(
                EntityNotFoundException.class);
        assertFalse(courseService.existsCourseExperiment(ID, TITLE));
        verify(courseRepository).getOne(ID);
        verify(experimentRepository).findByTitle(TITLE);
        verify(courseExperimentRepository).existsByCourseAndExperiment(course, experiment);
    }

    @Test
    public void testExistsCourseExperimentNull() {
        when(courseRepository.getOne(ID)).thenReturn(course);
        assertFalse(courseService.existsCourseExperiment(ID, TITLE));
        verify(courseRepository).getOne(ID);
        verify(experimentRepository).findByTitle(TITLE);
        verify(courseExperimentRepository, never()).existsByCourseAndExperiment(any(), any());
    }

    @Test
    public void testExistsCourseExperimentInvalidId() {
        assertFalse(courseService.existsCourseExperiment(0, TITLE));
        verify(courseRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).findByTitle(anyString());
        verify(courseExperimentRepository, never()).existsByCourseAndExperiment(any(), any());
    }

    @Test
    public void testExistsCourseExperimentTitleBlank() {
        assertFalse(courseService.existsCourseExperiment(ID, BLANK));
        verify(courseRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).findByTitle(anyString());
        verify(courseExperimentRepository, never()).existsByCourseAndExperiment(any(), any());
    }

    @Test
    public void testExistsCourseExperimentTitleNull() {
        assertFalse(courseService.existsCourseExperiment(ID, null));
        verify(courseRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).findByTitle(anyString());
        verify(courseExperimentRepository, never()).existsByCourseAndExperiment(any(), any());
    }

    @Test
    public void testExistsCourseParticipant() {
        when(courseRepository.getOne(ID)).thenReturn(course);
        when(userRepository.findUserByUsernameOrEmail(USERNAME, USERNAME)).thenReturn(user);
        when(courseParticipantRepository.existsByCourseAndUser(course, user)).thenReturn(true);
        assertTrue(courseService.existsCourseParticipant(ID, USERNAME));
        verify(courseRepository).getOne(ID);
        verify(userRepository).findUserByUsernameOrEmail(USERNAME, USERNAME);
        verify(courseParticipantRepository).existsByCourseAndUser(course, user);
    }

    @Test
    public void testExistsCourseParticipantNoParticipant() {
        when(courseRepository.getOne(ID)).thenReturn(course);
        when(userRepository.findUserByUsernameOrEmail(USERNAME, USERNAME)).thenReturn(user);
        assertFalse(courseService.existsCourseParticipant(ID, USERNAME));
        verify(courseRepository).getOne(ID);
        verify(userRepository).findUserByUsernameOrEmail(USERNAME, USERNAME);
        verify(courseParticipantRepository).existsByCourseAndUser(course, user);
    }

    @Test
    public void testExistsCourseParticipantNoUser() {
        when(courseRepository.getOne(ID)).thenReturn(course);
        assertFalse(courseService.existsCourseParticipant(ID, USERNAME));
        verify(courseRepository).getOne(ID);
        verify(userRepository).findUserByUsernameOrEmail(USERNAME, USERNAME);
        verify(courseParticipantRepository, never()).existsByCourseAndUser(any(), any());
    }

    @Test
    public void testExistsCourseParticipantNotFound() {
        when(courseRepository.getOne(ID)).thenReturn(course);
        when(userRepository.findUserByUsernameOrEmail(USERNAME, USERNAME)).thenReturn(user);
        when(courseParticipantRepository.existsByCourseAndUser(course, user)).thenThrow(EntityNotFoundException.class);
        assertFalse(courseService.existsCourseParticipant(ID, USERNAME));
        verify(courseRepository).getOne(ID);
        verify(userRepository).findUserByUsernameOrEmail(USERNAME, USERNAME);
        verify(courseParticipantRepository).existsByCourseAndUser(course, user);
    }

    @Test
    public void testExistsCourseParticipantInvalidId() {
        assertFalse(courseService.existsCourseParticipant(0, USERNAME));
        verify(courseRepository, never()).getOne(anyInt());
        verify(userRepository, never()).findUserByUsernameOrEmail(anyString(), anyString());
        verify(courseParticipantRepository, never()).existsByCourseAndUser(any(), any());
    }

    @Test
    public void testExistsCourseParticipantInputBlank() {
        assertFalse(courseService.existsCourseParticipant(ID, BLANK));
        verify(courseRepository, never()).getOne(anyInt());
        verify(userRepository, never()).findUserByUsernameOrEmail(anyString(), anyString());
        verify(courseParticipantRepository, never()).existsByCourseAndUser(any(), any());
    }

    @Test
    public void testExistsCourseParticipantInputNull() {
        assertFalse(courseService.existsCourseParticipant(ID, null));
        verify(courseRepository, never()).getOne(anyInt());
        verify(userRepository, never()).findUserByUsernameOrEmail(anyString(), anyString());
        verify(courseParticipantRepository, never()).existsByCourseAndUser(any(), any());
    }

    @Test
    public void testSaveCourse() {
        when(courseRepository.save(any())).thenReturn(course);
        assertEquals(ID, courseService.saveCourse(courseDTO));
        verify(courseRepository).save(any());
    }

    @Test
    public void testSaveCourseStore() {
        course.setId(0);
        when(courseRepository.save(any())).thenReturn(course);
        assertThrows(StoreException.class,
                () -> courseService.saveCourse(courseDTO)
        );
        verify(courseRepository).save(any());
    }

    @Test
    public void testSaveCourseStoreIdNull() {
        when(courseRepository.save(any())).thenReturn(new Course());
        assertThrows(StoreException.class,
                () -> courseService.saveCourse(courseDTO)
        );
        verify(courseRepository).save(any());
    }

    @Test
    public void testSaveCourseInvalidTitle() {
        courseDTO.setTitle(BLANK);
        assertThrows(IncompleteDataException.class,
                () -> courseService.saveCourse(courseDTO)
        );
        verify(courseRepository, never()).save(any());
    }

    @Test
    public void testSaveCourseTitleNull() {
        courseDTO.setTitle(null);
        assertThrows(IncompleteDataException.class,
                () -> courseService.saveCourse(courseDTO)
        );
        verify(courseRepository, never()).save(any());
    }

    @Test
    public void testSaveCourseInvalidDescription() {
        courseDTO.setDescription(BLANK);
        assertThrows(IncompleteDataException.class,
                () -> courseService.saveCourse(courseDTO)
        );
        verify(courseRepository, never()).save(any());
    }

    @Test
    public void testSaveCourseDescriptionNull() {
        courseDTO.setDescription(null);
        assertThrows(IncompleteDataException.class,
                () -> courseService.saveCourse(courseDTO)
        );
        verify(courseRepository, never()).save(any());
    }

    @Test
    public void testSaveCourseLastChangedNull() {
        courseDTO.setLastChanged(null);
        assertThrows(IncompleteDataException.class,
                () -> courseService.saveCourse(courseDTO)
        );
        verify(courseRepository, never()).save(any());
    }

    @Test
    public void testSaveCourseParticipant() {
        when(courseRepository.getOne(ID)).thenReturn(course);
        when(userRepository.findUserByUsernameOrEmail(USERNAME, USERNAME)).thenReturn(user);
        assertAll(
                () -> assertEquals(ID, courseService.saveCourseParticipant(ID, USERNAME)),
                () -> assertTrue(user.isActive()),
                () -> assertTrue(course.getLastChanged().after(TIMESTAMP))
        );
        verify(courseRepository).getOne(ID);
        verify(userRepository).findUserByUsernameOrEmail(USERNAME, USERNAME);
        verify(courseParticipantRepository).save(any());
        verify(courseRepository).save(course);
        verify(userRepository).save(user);
    }

    @Test
    public void testSaveCourseParticipantConstraintViolation() {
        when(courseRepository.getOne(ID)).thenReturn(course);
        when(userRepository.findUserByUsernameOrEmail(USERNAME, USERNAME)).thenReturn(user);
        when(courseParticipantRepository.save(any())).thenThrow(ConstraintViolationException.class);
        assertThrows(StoreException.class,
                () -> courseService.saveCourseParticipant(ID, USERNAME)
        );
        verify(courseRepository).getOne(ID);
        verify(userRepository).findUserByUsernameOrEmail(USERNAME, USERNAME);
        verify(courseParticipantRepository).save(any());
        verify(courseRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testSaveCourseParticipantEntityNotFound() {
        when(courseRepository.getOne(ID)).thenReturn(course);
        when(userRepository.findUserByUsernameOrEmail(USERNAME, USERNAME)).thenReturn(user);
        when(courseParticipantRepository.save(any())).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> courseService.saveCourseParticipant(ID, USERNAME)
        );
        verify(courseRepository).getOne(ID);
        verify(userRepository).findUserByUsernameOrEmail(USERNAME, USERNAME);
        verify(courseParticipantRepository).save(any());
        verify(courseRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testSaveCourseParticipantAdmin() {
        user.setRole("ADMIN");
        when(courseRepository.getOne(ID)).thenReturn(course);
        when(userRepository.findUserByUsernameOrEmail(USERNAME, USERNAME)).thenReturn(user);
        assertThrows(IllegalStateException.class,
                () -> courseService.saveCourseParticipant(ID, USERNAME)
        );
        verify(courseRepository).getOne(ID);
        verify(userRepository).findUserByUsernameOrEmail(USERNAME, USERNAME);
        verify(courseParticipantRepository, never()).save(any());
        verify(courseRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testSaveCourseParticipantNoUser() {
        when(courseRepository.getOne(ID)).thenReturn(course);
        assertThrows(NotFoundException.class,
                () -> courseService.saveCourseParticipant(ID, USERNAME)
        );
        verify(courseRepository).getOne(ID);
        verify(userRepository).findUserByUsernameOrEmail(USERNAME, USERNAME);
        verify(courseParticipantRepository, never()).save(any());
        verify(courseRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testSaveCourseParticipantInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> courseService.saveCourseParticipant(-1, USERNAME)
        );
        verify(courseRepository, never()).getOne(anyInt());
        verify(userRepository, never()).findUserByUsernameOrEmail(anyString(), anyString());
        verify(courseParticipantRepository, never()).save(any());
        verify(courseRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testSaveCourseParticipantInputBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> courseService.saveCourseParticipant(ID, BLANK)
        );
        verify(courseRepository, never()).getOne(anyInt());
        verify(userRepository, never()).findUserByUsernameOrEmail(anyString(), anyString());
        verify(courseParticipantRepository, never()).save(any());
        verify(courseRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testSaveCourseParticipantInputNull() {
        assertThrows(IllegalArgumentException.class,
                () -> courseService.saveCourseParticipant(ID, null)
        );
        verify(courseRepository, never()).getOne(anyInt());
        verify(userRepository, never()).findUserByUsernameOrEmail(anyString(), anyString());
        verify(courseParticipantRepository, never()).save(any());
        verify(courseRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testDeleteCourseParticipant() {
        when(courseRepository.getOne(ID)).thenReturn(course);
        when(userRepository.findUserByUsernameOrEmail(USERNAME, USERNAME)).thenReturn(user);
        when(courseExperimentRepository.findAllByCourse(course)).thenReturn(List.of(courseExperiment,
                courseExperiment));
        when(participantRepository.existsByUserAndExperiment(user, experiment)).thenReturn(true, false);
        courseService.deleteCourseParticipant(ID, USERNAME);
        assertTrue(course.getLastChanged().after(TIMESTAMP));
        verify(courseRepository).getOne(ID);
        verify(userRepository).findUserByUsernameOrEmail(USERNAME, USERNAME);
        verify(courseExperimentRepository).findAllByCourse(course);
        verify(participantRepository, times(2)).existsByUserAndExperiment(user, experiment);
        verify(participantRepository).deleteById(any());
        verify(courseParticipantRepository).deleteById(any());
        verify(courseRepository).save(course);
    }

    @Test
    public void testDeleteCourseParticipantEntityNotFound() {
        when(courseRepository.getOne(ID)).thenReturn(course);
        when(userRepository.findUserByUsernameOrEmail(USERNAME, USERNAME)).thenReturn(user);
        when(courseExperimentRepository.findAllByCourse(course)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> courseService.deleteCourseParticipant(ID, USERNAME)
        );
        verify(courseRepository).getOne(ID);
        verify(userRepository).findUserByUsernameOrEmail(USERNAME, USERNAME);
        verify(courseExperimentRepository).findAllByCourse(course);
        verify(participantRepository, never()).existsByUserAndExperiment(any(), any());
        verify(participantRepository, never()).deleteById(any());
        verify(courseParticipantRepository, never()).deleteById(any());
        verify(courseRepository, never()).save(any());
    }

    @Test
    public void testDeleteCourseParticipantNoUser() {
        when(courseRepository.getOne(ID)).thenReturn(course);
        assertThrows(NotFoundException.class,
                () -> courseService.deleteCourseParticipant(ID, USERNAME)
        );
        verify(courseRepository).getOne(ID);
        verify(userRepository).findUserByUsernameOrEmail(USERNAME, USERNAME);
        verify(courseExperimentRepository, never()).findAllByCourse(any());
        verify(participantRepository, never()).existsByUserAndExperiment(any(), any());
        verify(participantRepository, never()).deleteById(any());
        verify(courseParticipantRepository, never()).deleteById(any());
        verify(courseRepository, never()).save(any());
    }

    @Test
    public void testDeleteCourseParticipantInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> courseService.deleteCourseParticipant(0, USERNAME)
        );
        verify(courseRepository, never()).getOne(anyInt());
        verify(userRepository, never()).findUserByUsernameOrEmail(anyString(), anyString());
        verify(courseExperimentRepository, never()).findAllByCourse(any());
        verify(participantRepository, never()).existsByUserAndExperiment(any(), any());
        verify(participantRepository, never()).deleteById(any());
        verify(courseParticipantRepository, never()).deleteById(any());
        verify(courseRepository, never()).save(any());
    }

    @Test
    public void testDeleteCourseParticipantInputBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> courseService.deleteCourseParticipant(ID, BLANK)
        );
        verify(courseRepository, never()).getOne(anyInt());
        verify(userRepository, never()).findUserByUsernameOrEmail(anyString(), anyString());
        verify(courseExperimentRepository, never()).findAllByCourse(any());
        verify(participantRepository, never()).existsByUserAndExperiment(any(), any());
        verify(participantRepository, never()).deleteById(any());
        verify(courseParticipantRepository, never()).deleteById(any());
        verify(courseRepository, never()).save(any());
    }

    @Test
    public void testDeleteCourseParticipantInputNull() {
        assertThrows(IllegalArgumentException.class,
                () -> courseService.deleteCourseParticipant(ID, null)
        );
        verify(courseRepository, never()).getOne(anyInt());
        verify(userRepository, never()).findUserByUsernameOrEmail(anyString(), anyString());
        verify(courseExperimentRepository, never()).findAllByCourse(any());
        verify(participantRepository, never()).existsByUserAndExperiment(any(), any());
        verify(participantRepository, never()).deleteById(any());
        verify(courseParticipantRepository, never()).deleteById(any());
        verify(courseRepository, never()).save(any());
    }

    @Test
    public void testSaveCourseExperiment() {
        when(courseRepository.getOne(ID)).thenReturn(course);
        when(experimentRepository.findByTitle(TITLE)).thenReturn(experiment);
        courseService.saveCourseExperiment(ID, TITLE);
        assertTrue(course.getLastChanged().toLocalDateTime().isAfter(TIMESTAMP.toLocalDateTime()));
        verify(courseRepository).getOne(ID);
        verify(experimentRepository).findByTitle(TITLE);
        verify(courseExperimentRepository).save(any());
        verify(courseRepository).save(course);
    }

    @Test
    public void testSaveCourseExperimentEntityNotFound() {
        when(courseRepository.getOne(ID)).thenReturn(course);
        when(experimentRepository.findByTitle(TITLE)).thenReturn(experiment);
        when(courseExperimentRepository.save(any())).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> courseService.saveCourseExperiment(ID, TITLE)
        );
        verify(courseRepository).getOne(ID);
        verify(experimentRepository).findByTitle(TITLE);
        verify(courseExperimentRepository).save(any());
        verify(courseRepository, never()).save(any());
    }

    @Test
    public void testSaveCourseExperimentConstraintViolation() {
        when(courseRepository.getOne(ID)).thenReturn(course);
        when(experimentRepository.findByTitle(TITLE)).thenReturn(experiment);
        when(courseExperimentRepository.save(any())).thenThrow(ConstraintViolationException.class);
        assertThrows(StoreException.class,
                () -> courseService.saveCourseExperiment(ID, TITLE)
        );
        verify(courseRepository).getOne(ID);
        verify(experimentRepository).findByTitle(TITLE);
        verify(courseExperimentRepository).save(any());
        verify(courseRepository, never()).save(any());
    }

    @Test
    public void testSaveCourseExperimentNull() {
        when(courseRepository.getOne(ID)).thenReturn(course);
        assertThrows(NotFoundException.class,
                () -> courseService.saveCourseExperiment(ID, TITLE)
        );
        verify(courseRepository).getOne(ID);
        verify(experimentRepository).findByTitle(TITLE);
        verify(courseExperimentRepository, never()).save(any());
        verify(courseRepository, never()).save(any());
    }

    @Test
    public void testSaveCourseExperimentInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> courseService.saveCourseExperiment(-1, TITLE)
        );
        verify(courseRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).findByTitle(anyString());
        verify(courseExperimentRepository, never()).save(any());
        verify(courseRepository, never()).save(any());
    }

    @Test
    public void testSaveCourseExperimentTitleBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> courseService.saveCourseExperiment(ID, BLANK)
        );
        verify(courseRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).findByTitle(anyString());
        verify(courseExperimentRepository, never()).save(any());
        verify(courseRepository, never()).save(any());
    }

    @Test
    public void testSaveCourseExperimentTitleNull() {
        assertThrows(IllegalArgumentException.class,
                () -> courseService.saveCourseExperiment(ID, null)
        );
        verify(courseRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).findByTitle(anyString());
        verify(courseExperimentRepository, never()).save(any());
        verify(courseRepository, never()).save(any());
    }

    @Test
    public void testDeleteCourseExperiment() {
        when(courseRepository.getOne(ID)).thenReturn(course);
        when(experimentRepository.findByTitle(TITLE)).thenReturn(experiment);
        courseService.deleteCourseExperiment(ID, TITLE);
        assertTrue(course.getLastChanged().toLocalDateTime().isAfter(TIMESTAMP.toLocalDateTime()));
        verify(courseRepository).getOne(ID);
        verify(experimentRepository).findByTitle(TITLE);
        verify(courseExperimentRepository).deleteById(any());
        verify(courseRepository).save(course);
    }

    @Test
    public void testDeleteCourseExperimentEntityNotFound() {
        when(courseRepository.getOne(ID)).thenReturn(course);
        when(experimentRepository.findByTitle(TITLE)).thenReturn(experiment);
        when(courseRepository.save(course)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> courseService.deleteCourseExperiment(ID, TITLE)
        );
        verify(courseRepository).getOne(ID);
        verify(experimentRepository).findByTitle(TITLE);
        verify(courseExperimentRepository).deleteById(any());
        verify(courseRepository).save(course);
    }

    @Test
    public void testDeleteCourseExperimentNull() {
        when(courseRepository.getOne(ID)).thenReturn(course);
        assertThrows(NotFoundException.class,
                () -> courseService.deleteCourseExperiment(ID, TITLE)
        );
        verify(courseRepository).getOne(ID);
        verify(experimentRepository).findByTitle(TITLE);
        verify(courseExperimentRepository, never()).deleteById(any());
        verify(courseRepository, never()).save(any());
    }

    @Test
    public void testDeleteCourseExperimentInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> courseService.deleteCourseExperiment(0, TITLE)
        );
        verify(courseRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).findByTitle(anyString());
        verify(courseExperimentRepository, never()).deleteById(any());
        verify(courseRepository, never()).save(any());
    }

    @Test
    public void testDeleteCourseExperimentTitleBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> courseService.deleteCourseExperiment(ID, BLANK)
        );
        verify(courseRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).findByTitle(anyString());
        verify(courseExperimentRepository, never()).deleteById(any());
        verify(courseRepository, never()).save(any());
    }

    @Test
    public void testDeleteCourseExperimentTitleNull() {
        assertThrows(IllegalArgumentException.class,
                () -> courseService.deleteCourseExperiment(ID, null)
        );
        verify(courseRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).findByTitle(anyString());
        verify(courseExperimentRepository, never()).deleteById(any());
        verify(courseRepository, never()).save(any());
    }

    @Test
    public void testAddParticipantToCourseExperiments() {
        user.setSecret(null);
        when(courseRepository.getOne(ID)).thenReturn(course);
        when(userRepository.getOne(ID)).thenReturn(user);
        when(courseExperimentRepository.findAllByCourse(course)).thenReturn(List.of(courseExperiment,
                courseExperiment));
        assertDoesNotThrow(() -> courseService.addParticipantToCourseExperiments(ID, ID));
        verify(courseRepository).getOne(ID);
        verify(userRepository).getOne(ID);
        verify(courseExperimentRepository).findAllByCourse(course);
        verify(participantRepository, times(2)).existsByUserAndExperiment(user, experiment);
        verify(participantRepository, times(2)).save(any());
        verify(userRepository).save(user);
    }

    @Test
    public void testAddParticipantToCourseExperimentsSecretNotNull() {
        when(courseRepository.getOne(ID)).thenReturn(course);
        when(userRepository.getOne(ID)).thenReturn(user);
        when(courseExperimentRepository.findAllByCourse(course)).thenReturn(List.of(courseExperiment,
                courseExperiment));
        assertDoesNotThrow(() -> courseService.addParticipantToCourseExperiments(ID, ID));
        verify(courseRepository).getOne(ID);
        verify(userRepository).getOne(ID);
        verify(courseExperimentRepository).findAllByCourse(course);
        verify(participantRepository, times(2)).existsByUserAndExperiment(user, experiment);
        verify(participantRepository, times(2)).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testAddParticipantToCourseExperimentsNoExperiments() {
        when(courseRepository.getOne(ID)).thenReturn(course);
        when(userRepository.getOne(ID)).thenReturn(user);
        assertDoesNotThrow(() -> courseService.addParticipantToCourseExperiments(ID, ID));
        verify(courseRepository).getOne(ID);
        verify(userRepository).getOne(ID);
        verify(courseExperimentRepository).findAllByCourse(course);
        verify(participantRepository, never()).existsByUserAndExperiment(any(), any());
        verify(participantRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testAddParticipantToCourseExperimentsParticipantExists() {
        when(courseRepository.getOne(ID)).thenReturn(course);
        when(userRepository.getOne(ID)).thenReturn(user);
        when(courseExperimentRepository.findAllByCourse(course)).thenReturn(List.of(courseExperiment,
                courseExperiment));
        when(participantRepository.existsByUserAndExperiment(user, experiment)).thenReturn(true);
        assertThrows(IllegalStateException.class,
                () -> courseService.addParticipantToCourseExperiments(ID, ID)
        );
        verify(courseRepository).getOne(ID);
        verify(userRepository).getOne(ID);
        verify(courseExperimentRepository).findAllByCourse(course);
        verify(participantRepository).existsByUserAndExperiment(user, experiment);
        verify(participantRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testAddParticipantToCourseExperimentsConstraintViolation() {
        when(courseRepository.getOne(ID)).thenReturn(course);
        when(userRepository.getOne(ID)).thenReturn(user);
        when(courseExperimentRepository.findAllByCourse(course)).thenReturn(List.of(courseExperiment,
                courseExperiment));
        when(participantRepository.save(any())).thenThrow(ConstraintViolationException.class);
        assertThrows(StoreException.class,
                () -> courseService.addParticipantToCourseExperiments(ID, ID)
        );
        verify(courseRepository).getOne(ID);
        verify(userRepository).getOne(ID);
        verify(courseExperimentRepository).findAllByCourse(course);
        verify(participantRepository).existsByUserAndExperiment(user, experiment);
        verify(participantRepository).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testAddParticipantToCourseExperimentsEntityNotFound() {
        when(courseRepository.getOne(ID)).thenReturn(course);
        when(userRepository.getOne(ID)).thenReturn(user);
        when(courseExperimentRepository.findAllByCourse(course)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> courseService.addParticipantToCourseExperiments(ID, ID)
        );
        verify(courseRepository).getOne(ID);
        verify(userRepository).getOne(ID);
        verify(courseExperimentRepository).findAllByCourse(course);
        verify(participantRepository, never()).existsByUserAndExperiment(any(), any());
        verify(participantRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testAddParticipantToCourseExperimentsInvalidUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> courseService.addParticipantToCourseExperiments(ID, -1)
        );
        verify(courseRepository, never()).getOne(anyInt());
        verify(userRepository, never()).getOne(anyInt());
        verify(courseExperimentRepository, never()).findAllByCourse(any());
        verify(participantRepository, never()).existsByUserAndExperiment(any(), any());
        verify(participantRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testAddParticipantToCourseExperimentsInvalidCourseId() {
        assertThrows(IllegalArgumentException.class,
                () -> courseService.addParticipantToCourseExperiments(0, ID)
        );
        verify(courseRepository, never()).getOne(anyInt());
        verify(userRepository, never()).getOne(anyInt());
        verify(courseExperimentRepository, never()).findAllByCourse(any());
        verify(participantRepository, never()).existsByUserAndExperiment(any(), any());
        verify(participantRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testGetCourse() {
        when(courseRepository.findById(ID)).thenReturn(Optional.of(course));
        CourseDTO foundCourse = courseService.getCourse(ID);
        assertAll(
                () -> assertEquals(course.getId(), foundCourse.getId()),
                () -> assertEquals(course.getTitle(), foundCourse.getTitle()),
                () -> assertEquals(course.getDescription(), foundCourse.getDescription()),
                () -> assertEquals(course.getContent(), foundCourse.getContent()),
                () -> assertEquals(course.isActive(), foundCourse.isActive()),
                () -> assertEquals(course.getLastChanged(), Timestamp.valueOf(foundCourse.getLastChanged()))
        );
        verify(courseRepository).findById(ID);
    }

    @Test
    public void testGetCourseNotFound() {
        assertThrows(NotFoundException.class,
                () -> courseService.getCourse(ID)
        );
        verify(courseRepository).findById(ID);
    }

    @Test
    public void testGetCourseInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> courseService.getCourse(-1)
        );
        verify(courseRepository, never()).findById(anyInt());
    }

}
