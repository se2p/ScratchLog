package fim.unipassau.de.scratch1984.web;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.CourseService;
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

    private StringBuilder createLongString(int length) {
        StringBuilder longString = new StringBuilder();
        longString.append("a".repeat(Math.max(0, length)));
        return longString;
    }

}
