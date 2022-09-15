package fim.unipassau.de.scratch1984.web;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.CourseService;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.application.service.PageService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.web.controller.CourseController;
import fim.unipassau.de.scratch1984.web.dto.CourseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourseControllerTest {

    @InjectMocks
    private CourseController courseController;

    @Mock
    private CourseService courseService;

    @Mock
    private ExperimentService experimentService;

    @Mock
    private UserService userService;

    @Mock
    private PageService pageService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    private static final String COURSE = "course";
    private static final String REDIRECT_COURSE = "redirect:/course?id=";
    private static final String COURSE_EDIT = "course-edit";
    private static final String ERROR = "redirect:/error";
    private static final String COURSE_DTO = "courseDTO";
    private static final String ID_STRING = "1";
    private static final int ID = 1;
    private static final String TITLE = "Title";
    private static final String DESCRIPTION = "Description";
    private static final String CONTENT = "content";
    private static final String MODEL_ERROR = "error";
    private static final String BLANK = "  ";
    private static final LocalDateTime CHANGED = LocalDateTime.now();
    private final CourseDTO courseDTO = new CourseDTO(ID, TITLE, DESCRIPTION, CONTENT, true, CHANGED);

    @BeforeEach
    public void setUp() {
        courseDTO.setId(ID);
        courseDTO.setTitle(TITLE);
        courseDTO.setDescription(DESCRIPTION);
        courseDTO.setContent(CONTENT);
        courseDTO.setActive(true);
        courseDTO.setLastChanged(CHANGED);
    }

    @Test
    public void testGetCourseForm() {
        assertEquals(COURSE_EDIT, courseController.getCourseForm(new CourseDTO()));
    }

    @Test
    public void testGetEditCourseForm() {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        assertEquals(COURSE_EDIT, courseController.getEditCourseForm(ID_STRING, model));
        verify(courseService).getCourse(ID);
        verify(model).addAttribute(COURSE_DTO, courseDTO);
    }

    @Test
    public void testGetEditCourseFormNotFound() {
        when(courseService.getCourse(ID)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, courseController.getEditCourseForm(ID_STRING, model));
        verify(courseService).getCourse(ID);
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetEditCourseFormInvalidId() {
        assertEquals(ERROR, courseController.getEditCourseForm("0", model));
        verify(courseService, never()).getCourse(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetEditCourseFormIdNull() {
        assertEquals(ERROR, courseController.getEditCourseForm(null, model));
        verify(courseService, never()).getCourse(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void tetUpdateCourse() {
        when(courseService.saveCourse(courseDTO)).thenReturn(ID);
        assertAll(
                () -> assertEquals(REDIRECT_COURSE + ID, courseController.updateCourse(courseDTO, bindingResult)),
                () -> assertTrue(courseDTO.getLastChanged().isAfter(CHANGED))
        );
        verify(bindingResult, never()).addError(any());
        verify(courseService).existsCourse(ID, TITLE);
        verify(courseService).saveCourse(courseDTO);
    }

    @Test
    public void tetUpdateNewCourse() {
        courseDTO.setId(null);
        when(courseService.saveCourse(courseDTO)).thenReturn(ID);
        assertAll(
                () -> assertEquals(REDIRECT_COURSE + ID, courseController.updateCourse(courseDTO, bindingResult)),
                () -> assertTrue(courseDTO.getLastChanged().isAfter(CHANGED))
        );
        verify(bindingResult, never()).addError(any());
        verify(courseService).existsCourse(TITLE);
        verify(courseService).saveCourse(courseDTO);
    }

    @Test
    public void tetUpdateCourseExists() {
        when(courseService.existsCourse(ID, TITLE)).thenReturn(true);
        when(bindingResult.hasErrors()).thenReturn(true);
        assertAll(
                () -> assertEquals(COURSE_EDIT, courseController.updateCourse(courseDTO, bindingResult)),
                () -> assertEquals(courseDTO.getLastChanged(), CHANGED)
        );
        verify(bindingResult).addError(any());
        verify(courseService).existsCourse(ID, TITLE);
        verify(courseService, never()).saveCourse(any());
    }

    @Test
    public void tetUpdateCourseInvalidTitleAndDescription() {
        String title = createLongString(200).toString();
        courseDTO.setTitle(title);
        courseDTO.setDescription("");
        when(bindingResult.hasErrors()).thenReturn(true);
        assertAll(
                () -> assertEquals(COURSE_EDIT, courseController.updateCourse(courseDTO, bindingResult)),
                () -> assertEquals(courseDTO.getLastChanged(), CHANGED)
        );
        verify(bindingResult, times(2)).addError(any());
        verify(courseService).existsCourse(ID, title);
        verify(courseService, never()).saveCourse(any());
    }

    @Test
    public void tetUpdateCourseContentTooLong() {
        courseDTO.setContent(createLongString(60000).toString());
        when(bindingResult.hasErrors()).thenReturn(true);
        assertAll(
                () -> assertEquals(COURSE_EDIT, courseController.updateCourse(courseDTO, bindingResult)),
                () -> assertEquals(courseDTO.getLastChanged(), CHANGED)
        );
        verify(bindingResult).addError(any());
        verify(courseService).existsCourse(ID, TITLE);
        verify(courseService, never()).saveCourse(any());
    }

    @Test
    public void testAddExperiment() {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(experimentService.existsExperiment(TITLE)).thenReturn(true);
        assertEquals(REDIRECT_COURSE + ID, courseController.addExperiment(TITLE, ID_STRING, model));
        verify(courseService).getCourse(ID);
        verify(experimentService).existsExperiment(TITLE);
        verify(courseService).existsCourseExperiment(ID, TITLE);
        verify(courseService).saveCourseExperiment(ID, TITLE);
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testAddExperimentEntryExists() {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(experimentService.existsExperiment(TITLE)).thenReturn(true);
        when(courseService.existsCourseExperiment(ID, TITLE)).thenReturn(true);
        when(model.getAttribute(MODEL_ERROR)).thenReturn(TITLE);
        assertEquals(COURSE, courseController.addExperiment(TITLE, ID_STRING, model));
        verify(courseService).getCourse(ID);
        verify(experimentService).existsExperiment(TITLE);
        verify(courseService).existsCourseExperiment(ID, TITLE);
        verify(courseService, never()).saveCourseExperiment(anyInt(), anyString());
        verify(model, times(6)).addAttribute(anyString(), any());
    }

    @Test
    public void testAddExperimentNotExistent() {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(model.getAttribute(MODEL_ERROR)).thenReturn(TITLE);
        assertEquals(COURSE, courseController.addExperiment(TITLE, ID_STRING, model));
        verify(courseService).getCourse(ID);
        verify(experimentService).existsExperiment(TITLE);
        verify(courseService, never()).existsCourseExperiment(anyInt(), anyString());
        verify(courseService, never()).saveCourseExperiment(anyInt(), anyString());
        verify(model, times(6)).addAttribute(anyString(), any());
    }

    @Test
    public void testAddExperimentInvalidInput() {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        assertEquals(COURSE, courseController.addExperiment(BLANK, ID_STRING, model));
        verify(courseService).getCourse(ID);
        verify(experimentService, never()).existsExperiment(anyString());
        verify(courseService, never()).existsCourseExperiment(anyInt(), anyString());
        verify(courseService, never()).saveCourseExperiment(anyInt(), anyString());
        verify(model, times(6)).addAttribute(anyString(), any());
    }

    @Test
    public void testAddExperimentCourseInactive() {
        courseDTO.setActive(false);
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        assertEquals(Constants.ERROR, courseController.addExperiment(TITLE, ID_STRING, model));
        verify(courseService).getCourse(ID);
        verify(experimentService, never()).existsExperiment(anyString());
        verify(courseService, never()).existsCourseExperiment(anyInt(), anyString());
        verify(courseService, never()).saveCourseExperiment(anyInt(), anyString());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testAddExperimentCourseNotFound() {
        when(courseService.getCourse(ID)).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, courseController.addExperiment(TITLE, ID_STRING, model));
        verify(courseService).getCourse(ID);
        verify(experimentService, never()).existsExperiment(anyString());
        verify(courseService, never()).existsCourseExperiment(anyInt(), anyString());
        verify(courseService, never()).saveCourseExperiment(anyInt(), anyString());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testAddExperimentInvalidId() {
        assertEquals(Constants.ERROR, courseController.addExperiment(TITLE, TITLE, model));
        verify(courseService, never()).getCourse(anyInt());
        verify(experimentService, never()).existsExperiment(anyString());
        verify(courseService, never()).existsCourseExperiment(anyInt(), anyString());
        verify(courseService, never()).saveCourseExperiment(anyInt(), anyString());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteExperiment() {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(experimentService.existsExperiment(TITLE)).thenReturn(true);
        when(courseService.existsCourseExperiment(ID, TITLE)).thenReturn(true);
        assertEquals(REDIRECT_COURSE + ID, courseController.deleteExperiment(TITLE, ID_STRING, model));
        verify(courseService).getCourse(ID);
        verify(experimentService).existsExperiment(TITLE);
        verify(courseService).existsCourseExperiment(ID, TITLE);
        verify(courseService).deleteCourseExperiment(ID, TITLE);
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteExperimentNoEntry() {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(experimentService.existsExperiment(TITLE)).thenReturn(true);
        when(model.getAttribute(MODEL_ERROR)).thenReturn(TITLE);
        assertEquals(COURSE, courseController.deleteExperiment(TITLE, ID_STRING, model));
        verify(courseService).getCourse(ID);
        verify(experimentService).existsExperiment(TITLE);
        verify(courseService).existsCourseExperiment(ID, TITLE);
        verify(courseService, never()).deleteCourseExperiment(anyInt(), anyString());
        verify(model, times(6)).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteExperimentNotExistent() {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(model.getAttribute(MODEL_ERROR)).thenReturn(TITLE);
        assertEquals(COURSE, courseController.deleteExperiment(TITLE, ID_STRING, model));
        verify(courseService).getCourse(ID);
        verify(experimentService).existsExperiment(TITLE);
        verify(courseService, never()).existsCourseExperiment(anyInt(), anyString());
        verify(courseService, never()).deleteCourseExperiment(anyInt(), anyString());
        verify(model, times(6)).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteExperimentCourseInactive() {
        courseDTO.setActive(false);
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        assertEquals(Constants.ERROR, courseController.deleteExperiment(TITLE, ID_STRING, model));
        verify(courseService).getCourse(ID);
        verify(experimentService, never()).existsExperiment(anyString());
        verify(courseService, never()).existsCourseExperiment(anyInt(), anyString());
        verify(courseService, never()).deleteCourseExperiment(anyInt(), anyString());
        verify(model, never()).addAttribute(anyString(), any());
    }

    private StringBuilder createLongString(int length) {
        StringBuilder longString = new StringBuilder();
        longString.append("a".repeat(Math.max(0, length)));
        return longString;
    }

}
