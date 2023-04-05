package fim.unipassau.de.scratch1984.application;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.PageService;
import fim.unipassau.de.scratch1984.persistence.entity.Course;
import fim.unipassau.de.scratch1984.persistence.entity.CourseParticipant;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.ExperimentData;
import fim.unipassau.de.scratch1984.persistence.entity.Participant;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.projection.CourseExperimentProjection;
import fim.unipassau.de.scratch1984.persistence.projection.CourseTableProjection;
import fim.unipassau.de.scratch1984.persistence.projection.ExperimentTableProjection;
import fim.unipassau.de.scratch1984.persistence.repository.CourseExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.CourseParticipantRepository;
import fim.unipassau.de.scratch1984.persistence.repository.CourseRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentDataRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ParticipantRepository;
import fim.unipassau.de.scratch1984.util.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PageServiceTest {

    @InjectMocks
    private PageService pageService;

    @Mock
    private ExperimentRepository experimentRepository;

    @Mock
    private ExperimentDataRepository experimentDataRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseParticipantRepository courseParticipantRepository;

    @Mock
    private CourseExperimentRepository courseExperimentRepository;

    private static final int ID = 1;
    private final ExperimentData experimentData = new ExperimentData(ID, 5, 3, 2);
    private final Course course = new Course(ID, "My course", "Description", "no", false, LocalDateTime.now());
    private final PageRequest pageRequest = PageRequest.of(0, Constants.PAGE_SIZE);
    private Page<ExperimentTableProjection> experimentPage;
    private Page<CourseTableProjection> coursePage;
    private Page<CourseExperimentProjection> courseExperimentPage;
    private Page<CourseParticipant> courseParticipantPage;
    private final List<Participant> participantList = getParticipants(5);
    private final Page<Participant> participants = new PageImpl<>(participantList);

    @Test
    public void testGetExperimentPage() {
        List<ExperimentTableProjection> experiments = getExperimentProjections(5);
        experimentPage = new PageImpl<>(experiments);
        when(experimentRepository.findAllProjectedBy(any(PageRequest.class))).thenReturn(experimentPage);
        Page<ExperimentTableProjection> getPage = pageService.getExperimentPage(pageRequest);
        assertAll(
                () -> assertEquals(experimentPage.getTotalElements(), getPage.getTotalElements()),
                () -> assertEquals(experimentPage.stream().findFirst(), getPage.stream().findFirst()),
                () -> assertEquals(experimentPage.getSize(), getPage.getSize())
        );
        verify(experimentRepository).findAllProjectedBy(any(PageRequest.class));
    }

    @Test
    public void testGetExperimentPageEmpty() {
        experimentPage = new PageImpl<>(new ArrayList<>());
        when(experimentRepository.findAllProjectedBy(any(PageRequest.class))).thenReturn(experimentPage);
        Page<ExperimentTableProjection> getPage = pageService.getExperimentPage(pageRequest);
        assertTrue(getPage.isEmpty());
        verify(experimentRepository).findAllProjectedBy(any(PageRequest.class));
    }

    @Test
    public void testGetExperimentPageWrongPageSize() {
        PageRequest wrongPageSize = PageRequest.of(0, 20);
        assertThrows(IllegalArgumentException.class,
                () -> pageService.getExperimentPage(wrongPageSize)
        );
        verify(experimentRepository, never()).findAllProjectedBy(any(PageRequest.class));
    }

    @Test
    public void testGetExperimentPagePageableNull() {
        assertThrows(IllegalArgumentException.class,
                () -> pageService.getExperimentPage(null)
        );
        verify(experimentRepository, never()).findAllProjectedBy(any(PageRequest.class));
    }

    @Test
    public void testGetCoursePage() {
        List<CourseTableProjection> courses = getCourseProjections(3);
        coursePage = new PageImpl<>(courses);
        when(courseRepository.findAllProjectedBy(any(PageRequest.class))).thenReturn(coursePage);
        Page<CourseTableProjection> getPage = pageService.getCoursePage(pageRequest);
        assertAll(
                () -> assertEquals(coursePage.getTotalElements(), getPage.getTotalElements()),
                () -> assertEquals(coursePage.stream().findFirst(), getPage.stream().findFirst()),
                () -> assertEquals(coursePage.getSize(), getPage.getSize())
        );
        verify(courseRepository).findAllProjectedBy(any(PageRequest.class));
    }

    @Test
    public void testGetCoursePageEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> pageService.getCoursePage(null)
        );
        verify(courseRepository, never()).findAllProjectedBy(any(PageRequest.class));
    }

    @Test
    public void testGetCoursePageInvalidPageable() {
        coursePage = new PageImpl<>(new ArrayList<>());
        when(courseRepository.findAllProjectedBy(any(PageRequest.class))).thenReturn(coursePage);
        assertTrue(pageService.getCoursePage(pageRequest).isEmpty());
        verify(courseRepository).findAllProjectedBy(any(PageRequest.class));
    }

    @Test
    public void testGetCourseExperimentPage() {
        courseExperimentPage = new PageImpl<>(getCourseExperiments(2));
        when(courseRepository.getOne(ID)).thenReturn(course);
        when(courseExperimentRepository.findAllProjectedByCourse(pageRequest, course)).thenReturn(courseExperimentPage);
        Page<CourseExperimentProjection> getPage = pageService.getCourseExperimentPage(pageRequest, ID);
        assertAll(
                () -> assertEquals(courseExperimentPage.getTotalElements(), getPage.getTotalElements()),
                () -> assertEquals(courseExperimentPage.stream().findFirst(), getPage.stream().findFirst()),
                () -> assertEquals(courseExperimentPage.getSize(), getPage.getSize())
        );
        verify(courseRepository).getOne(ID);
        verify(courseExperimentRepository).findAllProjectedByCourse(pageRequest, course);
    }

    @Test
    public void testGetCourseExperimentPageEmpty() {
        courseExperimentPage = new PageImpl<>(new ArrayList<>());
        when(courseRepository.getOne(ID)).thenReturn(course);
        when(courseExperimentRepository.findAllProjectedByCourse(pageRequest, course)).thenReturn(courseExperimentPage);
        assertTrue(pageService.getCourseExperimentPage(pageRequest, ID).isEmpty());
        verify(courseRepository).getOne(ID);
        verify(courseExperimentRepository).findAllProjectedByCourse(pageRequest, course);
    }

    @Test
    public void testGetCourseExperimentInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> pageService.getCourseExperimentPage(pageRequest, 0)
        );
        verify(courseRepository, never()).getOne(anyInt());
        verify(courseExperimentRepository, never()).findAllProjectedByCourse(any(PageRequest.class), any());
    }

    @Test
    public void testGetExperimentParticipantPage() {
        List<ExperimentTableProjection> experiments = getExperimentProjections(4);
        experimentPage = new PageImpl<>(experiments);
        when(experimentRepository.findExperimentsByParticipant(anyInt(),
                any(PageRequest.class))).thenReturn(experimentPage);
        Page<ExperimentTableProjection> getPage = pageService.getExperimentParticipantPage(pageRequest, ID);
        assertAll(
                () -> assertEquals(experimentPage.getTotalElements(), getPage.getTotalElements()),
                () -> assertEquals(experimentPage.stream().findFirst(), getPage.stream().findFirst()),
                () -> assertEquals(experimentPage.getSize(), getPage.getSize())
        );
        verify(experimentRepository).findExperimentsByParticipant(anyInt(), any(PageRequest.class));
    }

    @Test
    public void testGetExperimentParticipantPageWrongPageSize() {
        PageRequest wrongPageSize = PageRequest.of(0, 11);
        assertThrows(IllegalArgumentException.class,
                () -> pageService.getExperimentParticipantPage(wrongPageSize, ID)
        );
        verify(experimentRepository, never()).findExperimentsByParticipant(anyInt(), any(PageRequest.class));
    }

    @Test
    public void testGetExperimentParticipantPagePageableNull() {
        assertThrows(IllegalArgumentException.class,
                () -> pageService.getExperimentParticipantPage(null, ID)
        );
        verify(experimentRepository, never()).findExperimentsByParticipant(anyInt(), any(PageRequest.class));
    }

    @Test
    public void testGetExperimentParticipantPageInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> pageService.getExperimentParticipantPage(pageRequest, 0)
        );
        verify(experimentRepository, never()).findExperimentsByParticipant(anyInt(), any(PageRequest.class));
    }

    @Test
    public void testGetCourseParticipantPage() {
        List<CourseTableProjection> courses = getCourseProjections(1);
        coursePage = new PageImpl<>(courses);
        when(courseRepository.findCoursesByParticipant(ID, pageRequest)).thenReturn(coursePage);
        Page<CourseTableProjection> getPage = pageService.getCourseParticipantPage(pageRequest, ID);
        assertAll(
                () -> assertEquals(coursePage.getTotalElements(), getPage.getTotalElements()),
                () -> assertEquals(coursePage.stream().findFirst(), getPage.stream().findFirst()),
                () -> assertEquals(coursePage.getSize(), getPage.getSize())
        );
        verify(courseRepository).findCoursesByParticipant(ID, pageRequest);
    }

    @Test
    public void testGetCourseParticipantPageInvalidPageable() {
        PageRequest wrongPageSize = PageRequest.of(0, 5);
        assertThrows(IllegalArgumentException.class,
                () -> pageService.getCourseParticipantPage(wrongPageSize, ID)
        );
        verify(courseRepository, never()).findCoursesByParticipant(anyInt(), any(PageRequest.class));
    }

    @Test
    public void testGetCourseParticipantPageInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> pageService.getCourseParticipantPage(pageRequest, -1)
        );
        verify(courseRepository, never()).findCoursesByParticipant(anyInt(), any(PageRequest.class));
    }

    @Test
    public void testGetParticipantPage() {
        when(participantRepository.findAllByExperiment(any(), any(PageRequest.class))).thenReturn(participants);
        assertEquals(participants, pageService.getParticipantPage(ID, pageRequest));
        verify(participantRepository).findAllByExperiment(any(), any(PageRequest.class));
        verify(experimentRepository).getOne(ID);
    }

    @Test
    public void testGetParticipantPageNotFound() {
        when(participantRepository.findAllByExperiment(any(), any(PageRequest.class)))
                .thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> pageService.getParticipantPage(ID, pageRequest)
        );
        verify(participantRepository).findAllByExperiment(any(), any(PageRequest.class));
        verify(experimentRepository).getOne(ID);
    }

    @Test
    public void testGetParticipantPageInvalidPageSize() {
        PageRequest invalidRequest = PageRequest.of(0, Constants.PAGE_SIZE + 1);
        assertThrows(IllegalArgumentException.class,
                () -> pageService.getParticipantPage(ID, invalidRequest)
        );
        verify(participantRepository, never()).findAllByExperiment(any(), any(PageRequest.class));
        verify(experimentRepository, never()).getOne(ID);
    }

    @Test
    public void testGetParticipantPageInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> pageService.getParticipantPage(0, pageRequest)
        );
        verify(participantRepository, never()).findAllByExperiment(any(), any(PageRequest.class));
        verify(experimentRepository, never()).getOne(ID);
    }

    @Test
    public void testGetParticipantCoursePage() {
        List<CourseParticipant> participants = getCourseParticipants(3);
        courseParticipantPage = new PageImpl<>(participants);
        when(courseRepository.getOne(ID)).thenReturn(course);
        when(courseParticipantRepository.findAllByCourse(any(),
                any(PageRequest.class))).thenReturn(courseParticipantPage);
        assertEquals(courseParticipantPage, pageService.getParticipantCoursePage(ID, pageRequest));
        verify(courseRepository).getOne(ID);
        verify(courseParticipantRepository).findAllByCourse(any(), any(PageRequest.class));
    }

    @Test
    public void testGetParticipantCoursePageNotFound() {
        when(courseParticipantRepository.findAllByCourse(any(),
                any(PageRequest.class))).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> pageService.getParticipantCoursePage(ID, pageRequest)
        );
        verify(courseRepository).getOne(ID);
        verify(courseParticipantRepository).findAllByCourse(any(), any(PageRequest.class));
    }

    @Test
    public void testGetParticipantCoursePageInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> pageService.getParticipantCoursePage(0, pageRequest)
        );
        verify(courseRepository, never()).getOne(anyInt());
        verify(courseParticipantRepository, never()).findAllByCourse(any(), any(PageRequest.class));
    }

    @Test
    public void testComputeLastExperimentPage() {
        when(experimentRepository.count()).thenReturn((long) Constants.PAGE_SIZE);
        assertEquals(1, pageService.computeLastExperimentPage());
        verify(experimentRepository).count();
    }

    @Test
    public void testComputeLastExperimentPage5() {
        when(experimentRepository.count()).thenReturn((long) 50);
        assertEquals(5, pageService.computeLastExperimentPage());
        verify(experimentRepository).count();
    }

    @Test
    public void testComputeLastExperimentPage6() {
        when(experimentRepository.count()).thenReturn((long) 51);
        assertEquals(6, pageService.computeLastExperimentPage());
        verify(experimentRepository).count();
    }

    @Test
    public void testComputeLastExperimentPageTooManyRows() {
        when(experimentRepository.count()).thenReturn(Long.MAX_VALUE);
        assertEquals(214748365, pageService.computeLastExperimentPage());
        verify(experimentRepository).count();
    }

    @Test
    public void testComputeLastCoursePage() {
        when(courseRepository.count()).thenReturn((long) Constants.PAGE_SIZE);
        assertEquals(1, pageService.computeLastCoursePage());
        verify(courseRepository).count();
    }

    @Test
    public void testGetLastCourseExperimentPage() {
        when(courseExperimentRepository.getCourseExperimentRowCount(ID)).thenReturn(51);
        assertEquals(6, pageService.getLastCourseExperimentPage(ID));
        verify(courseExperimentRepository).getCourseExperimentRowCount(ID);
    }

    @Test
    public void testGetLastCourseExperimentPageInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> pageService.getLastCourseExperimentPage(-1)
        );
        verify(courseExperimentRepository, never()).getCourseExperimentRowCount(anyInt());
    }

    @Test
    public void testGetLastExperimentPage() {
        when(experimentRepository.getParticipantPageCount(ID)).thenReturn(Constants.PAGE_SIZE);
        assertEquals(1, pageService.getLastExperimentPage(ID));
        verify(experimentRepository).getParticipantPageCount(ID);
    }

    @Test
    public void testGetLastExperimentPageInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> pageService.getLastExperimentPage(-1)
        );
        verify(experimentRepository, never()).getParticipantPageCount(anyInt());
    }

    @Test
    public void testGetLastCoursePage() {
        when(courseRepository.getParticipantPageCount(ID)).thenReturn(Constants.PAGE_SIZE);
        assertEquals(1, pageService.getLastCoursePage(ID));
        verify(courseRepository).getParticipantPageCount(ID);
    }


    @Test
    public void testGetLastCoursePageInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> pageService.getLastCoursePage(0)
        );
        verify(courseRepository, never()).getParticipantPageCount(anyInt());
    }

    @Test
    public void testGetLastParticipantPage() {
        when(experimentDataRepository.findByExperiment(ID)).thenReturn(Optional.of(experimentData));
        assertEquals(0, pageService.getLastParticipantPage(ID));
        verify(experimentDataRepository).findByExperiment(ID);
    }

    @Test
    public void testGetLastParticipantPage3() {
        experimentData.setParticipants(40);
        when(experimentDataRepository.findByExperiment(ID)).thenReturn(Optional.of(experimentData));
        assertEquals(3, pageService.getLastParticipantPage(ID));
        verify(experimentDataRepository).findByExperiment(ID);
    }

    @Test
    public void testGetLastParticipantPage4() {
        experimentData.setParticipants(41);
        when(experimentDataRepository.findByExperiment(ID)).thenReturn(Optional.of(experimentData));
        assertEquals(4, pageService.getLastParticipantPage(ID));
        verify(experimentDataRepository).findByExperiment(ID);
    }

    @Test
    public void testGetLastParticipantPageNull() {
        assertEquals(0, pageService.getLastParticipantPage(ID));
        verify(experimentDataRepository).findByExperiment(ID);
    }

    @Test
    public void testGetLastParticipantCoursePage() {
        when(courseParticipantRepository.getCourseParticipantRowCount(ID)).thenReturn(Constants.PAGE_SIZE + 1);
        assertEquals(2, pageService.getLastParticipantCoursePage(ID));
        verify(courseParticipantRepository).getCourseParticipantRowCount(ID);
    }

    @Test
    public void testGetLastParticipantCoursePageInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> pageService.getLastParticipantCoursePage(-1)
        );
        verify(courseParticipantRepository, never()).getCourseParticipantRowCount(anyInt());
    }

    private List<ExperimentTableProjection> getExperimentProjections(int number) {
        List<ExperimentTableProjection> experiments = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            int finalI = i;
            ExperimentTableProjection projection = new ExperimentTableProjection() {
                @Override
                public Integer getId() {
                    return finalI;
                }

                @Override
                public String getTitle() {
                    return "Experiment " + finalI;
                }

                @Override
                public String getDescription() {
                    return "Description for experiment " + finalI;
                }

                @Override
                public boolean isActive() {
                    return false;
                }
            };
            experiments.add(projection);
        }
        return experiments;
    }

    private List<CourseExperimentProjection> getCourseExperiments(int number) {
        List<CourseExperimentProjection> experiments = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            int id = i + 1;
            CourseExperimentProjection projection = new CourseExperimentProjection() {
                @Override
                public ExperimentTableProjection getExperiment() {
                    return new ExperimentTableProjection() {
                        @Override
                        public Integer getId() {
                            return id;
                        }

                        @Override
                        public String getTitle() {
                            return "Experiment " + id;
                        }

                        @Override
                        public String getDescription() {
                            return "Some description";
                        }

                        @Override
                        public boolean isActive() {
                            return false;
                        }
                    };
                }
            };
            experiments.add(projection);
        }
        return experiments;
    }

    private List<CourseTableProjection> getCourseProjections(int number) {
        List<CourseTableProjection> courses = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            int finalI = i;
            CourseTableProjection projection = new CourseTableProjection() {
                @Override
                public Integer getId() {
                    return finalI;
                }

                @Override
                public String getTitle() {
                    return "Course" + finalI;
                }

                @Override
                public String getDescription() {
                    return "Description for course " + finalI;
                }

                @Override
                public boolean isActive() {
                    return false;
                }
            };
            courses.add(projection);
        }
        return courses;
    }

    private List<CourseParticipant> getCourseParticipants(int number) {
        List<CourseParticipant> participants = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            User user = new User();
            user.setId(i + 1);
            Course course = new Course();
            course.setId(i + 1);
            participants.add(new CourseParticipant(user, course, LocalDateTime.now()));
        }
        return participants;
    }

    private List<Participant> getParticipants(int number) {
        List<Participant> participants = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            User user = new User();
            user.setId(i + 1);
            Experiment experiment = new Experiment();
            experiment.setId(i + 1);
            experiment.setTitle("Title " + i);
            participants.add(new Participant(user, experiment, LocalDateTime.now(), null));
        }
        return participants;
    }

}
