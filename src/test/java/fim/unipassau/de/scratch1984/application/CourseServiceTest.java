package fim.unipassau.de.scratch1984.application;

import fim.unipassau.de.scratch1984.application.exception.IncompleteDataException;
import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.exception.StoreException;
import fim.unipassau.de.scratch1984.application.service.CourseService;
import fim.unipassau.de.scratch1984.persistence.entity.Course;
import fim.unipassau.de.scratch1984.persistence.repository.CourseExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.CourseParticipantRepository;
import fim.unipassau.de.scratch1984.persistence.repository.CourseRepository;
import fim.unipassau.de.scratch1984.web.dto.CourseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
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

    private static final String TITLE = "My Course";
    private static final String DESCRIPTION = "A description";
    private static final String CONTENT = "content";
    private static final String BLANK = "    ";
    private static final int ID = 1;
    private static final int INVALID_ID = 2;
    private final CourseDTO courseDTO = new CourseDTO(ID, TITLE, DESCRIPTION, CONTENT, false, LocalDateTime.now());
    private final Course course = new Course(ID, TITLE, DESCRIPTION, CONTENT, false,
            Timestamp.valueOf(LocalDateTime.now()));

    @BeforeEach
    public void setUp() {
        courseDTO.setId(ID);
        courseDTO.setTitle(TITLE);
        courseDTO.setDescription(DESCRIPTION);
        courseDTO.setLastChanged(LocalDateTime.now());
        course.setId(ID);
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
