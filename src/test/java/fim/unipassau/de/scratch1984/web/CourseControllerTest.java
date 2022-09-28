package fim.unipassau.de.scratch1984.web;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.CourseService;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.application.service.PageService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.persistence.projection.CourseExperimentProjection;
import fim.unipassau.de.scratch1984.persistence.projection.ExperimentTableProjection;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.web.controller.CourseController;
import fim.unipassau.de.scratch1984.web.dto.CourseDTO;
import fim.unipassau.de.scratch1984.web.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
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
    private static final String EXPERIMENT_TABLE = "course::course_experiment_table";
    private static final String COURSE_DTO = "courseDTO";
    private static final String CURRENT = "3";
    private static final String LAST = "5";
    private static final String ID_STRING = "1";
    private static final int ID = 1;
    private static final int LAST_PAGE = 5;
    private static final String TITLE = "Title";
    private static final String DESCRIPTION = "Description";
    private static final String CONTENT = "content";
    private static final String USERNAME = "participant";
    private static final String MODEL_ERROR = "error";
    private static final String BLANK = "  ";
    private static final LocalDateTime CHANGED = LocalDateTime.now();
    private final CourseDTO courseDTO = new CourseDTO(ID, TITLE, DESCRIPTION, CONTENT, true, CHANGED);
    private final UserDTO userDTO = new UserDTO(USERNAME, "part@part.de", UserDTO.Role.PARTICIPANT,
            UserDTO.Language.ENGLISH, "password", "secret");
    private final Page<CourseExperimentProjection> experiments = new PageImpl<>(getCourseExperiments(3));

    @BeforeEach
    public void setUp() {
        courseDTO.setId(ID);
        courseDTO.setTitle(TITLE);
        courseDTO.setDescription(DESCRIPTION);
        courseDTO.setContent(CONTENT);
        courseDTO.setActive(true);
        courseDTO.setLastChanged(CHANGED);
        userDTO.setRole(UserDTO.Role.PARTICIPANT);
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
        assertEquals(Constants.ERROR, courseController.getEditCourseForm(ID_STRING, model));
        verify(courseService).getCourse(ID);
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetEditCourseFormInvalidId() {
        assertEquals(Constants.ERROR, courseController.getEditCourseForm("0", model));
        verify(courseService, never()).getCourse(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetEditCourseFormIdNull() {
        assertEquals(Constants.ERROR, courseController.getEditCourseForm(null, model));
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
    public void testAddParticipant() {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(userService.getUserByUsernameOrEmail(USERNAME)).thenReturn(userDTO);
        when(courseService.saveCourseParticipant(ID, USERNAME)).thenReturn(ID);
        assertEquals(REDIRECT_COURSE + ID, courseController.addParticipant(USERNAME, null, ID_STRING, model));
        verify(courseService).getCourse(ID);
        verify(userService).getUserByUsernameOrEmail(USERNAME);
        verify(courseService).existsCourseParticipant(ID, USERNAME);
        verify(courseService).saveCourseParticipant(ID, USERNAME);
        verify(courseService, never()).addParticipantToCourseExperiments(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testAddParticipantAddToExperiments() {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(userService.getUserByUsernameOrEmail(USERNAME)).thenReturn(userDTO);
        when(courseService.saveCourseParticipant(ID, USERNAME)).thenReturn(ID);
        assertEquals(REDIRECT_COURSE + ID, courseController.addParticipant(USERNAME, "on", ID_STRING, model));
        verify(courseService).getCourse(ID);
        verify(userService).getUserByUsernameOrEmail(USERNAME);
        verify(courseService).existsCourseParticipant(ID, USERNAME);
        verify(courseService).saveCourseParticipant(ID, USERNAME);
        verify(courseService).addParticipantToCourseExperiments(ID, ID);
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testAddParticipantNotFound() {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(userService.getUserByUsernameOrEmail(USERNAME)).thenReturn(userDTO);
        when(courseService.saveCourseParticipant(ID, USERNAME)).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, courseController.addParticipant(USERNAME, "on", ID_STRING, model));
        verify(courseService).getCourse(ID);
        verify(userService).getUserByUsernameOrEmail(USERNAME);
        verify(courseService).existsCourseParticipant(ID, USERNAME);
        verify(courseService).saveCourseParticipant(ID, USERNAME);
        verify(courseService, never()).addParticipantToCourseExperiments(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testAddParticipantExists() {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(userService.getUserByUsernameOrEmail(USERNAME)).thenReturn(userDTO);
        when(courseService.existsCourseParticipant(ID, USERNAME)).thenReturn(true);
        when(model.getAttribute(MODEL_ERROR)).thenReturn(USERNAME);
        assertEquals(COURSE, courseController.addParticipant(USERNAME, "on", ID_STRING, model));
        verify(courseService).getCourse(ID);
        verify(userService).getUserByUsernameOrEmail(USERNAME);
        verify(courseService).existsCourseParticipant(ID, USERNAME);
        verify(courseService, never()).saveCourseParticipant(anyInt(), anyString());
        verify(courseService, never()).addParticipantToCourseExperiments(anyInt(), anyInt());
        verify(model, times(9)).addAttribute(anyString(), any());
    }

    @Test
    public void testAddParticipantAdmin() {
        userDTO.setRole(UserDTO.Role.ADMIN);
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(userService.getUserByUsernameOrEmail(USERNAME)).thenReturn(userDTO);
        when(model.getAttribute(MODEL_ERROR)).thenReturn(USERNAME);
        assertEquals(COURSE, courseController.addParticipant(USERNAME, "on", ID_STRING, model));
        verify(courseService).getCourse(ID);
        verify(userService).getUserByUsernameOrEmail(USERNAME);
        verify(courseService, never()).existsCourseParticipant(anyInt(), anyString());
        verify(courseService, never()).saveCourseParticipant(anyInt(), anyString());
        verify(courseService, never()).addParticipantToCourseExperiments(anyInt(), anyInt());
        verify(model, times(9)).addAttribute(anyString(), any());
    }

    @Test
    public void testAddParticipantNoUser() {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(model.getAttribute(MODEL_ERROR)).thenReturn(USERNAME);
        assertEquals(COURSE, courseController.addParticipant(USERNAME, "on", ID_STRING, model));
        verify(courseService).getCourse(ID);
        verify(userService).getUserByUsernameOrEmail(USERNAME);
        verify(courseService, never()).existsCourseParticipant(anyInt(), anyString());
        verify(courseService, never()).saveCourseParticipant(anyInt(), anyString());
        verify(courseService, never()).addParticipantToCourseExperiments(anyInt(), anyInt());
        verify(model, times(9)).addAttribute(anyString(), any());
    }

    @Test
    public void testAddParticipantCourseInactive() {
        courseDTO.setActive(false);
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        assertEquals(Constants.ERROR, courseController.addParticipant(USERNAME, "on", ID_STRING, model));
        verify(courseService).getCourse(ID);
        verify(userService, never()).getUserByUsernameOrEmail(anyString());
        verify(courseService, never()).existsCourseParticipant(anyInt(), anyString());
        verify(courseService, never()).saveCourseParticipant(anyInt(), anyString());
        verify(courseService, never()).addParticipantToCourseExperiments(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteParticipant() {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(userService.getUserByUsernameOrEmail(USERNAME)).thenReturn(userDTO);
        when(courseService.existsCourseParticipant(ID, USERNAME)).thenReturn(true);
        assertEquals(REDIRECT_COURSE + ID, courseController.deleteParticipant(USERNAME, ID_STRING, model));
        verify(courseService).getCourse(ID);
        verify(userService).getUserByUsernameOrEmail(USERNAME);
        verify(courseService).existsCourseParticipant(ID, USERNAME);
        verify(courseService).deleteCourseParticipant(ID, USERNAME);
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteParticipantNotFound() {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(userService.getUserByUsernameOrEmail(USERNAME)).thenReturn(userDTO);
        when(courseService.existsCourseParticipant(ID, USERNAME)).thenReturn(true);
        doThrow(NotFoundException.class).when(courseService).deleteCourseParticipant(ID, USERNAME);
        assertEquals(Constants.ERROR, courseController.deleteParticipant(USERNAME, ID_STRING, model));
        verify(courseService).getCourse(ID);
        verify(userService).getUserByUsernameOrEmail(USERNAME);
        verify(courseService).existsCourseParticipant(ID, USERNAME);
        verify(courseService).deleteCourseParticipant(ID, USERNAME);
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteParticipantNotExistent() {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(userService.getUserByUsernameOrEmail(USERNAME)).thenReturn(userDTO);
        when(model.getAttribute(MODEL_ERROR)).thenReturn(USERNAME);
        assertEquals(COURSE, courseController.deleteParticipant(USERNAME, ID_STRING, model));
        verify(courseService).getCourse(ID);
        verify(userService).getUserByUsernameOrEmail(USERNAME);
        verify(courseService).existsCourseParticipant(ID, USERNAME);
        verify(courseService, never()).deleteCourseParticipant(anyInt(), anyString());
        verify(model, times(9)).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteParticipantInvalidId() {
        assertEquals(Constants.ERROR, courseController.deleteParticipant(USERNAME, "0", model));
        verify(courseService, never()).getCourse(anyInt());
        verify(userService, never()).getUserByUsernameOrEmail(anyString());
        verify(courseService, never()).existsCourseParticipant(anyInt(), anyString());
        verify(courseService, never()).deleteCourseParticipant(anyInt(), anyString());
        verify(model, never()).addAttribute(anyString(), any());
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
        verify(model, times(9)).addAttribute(anyString(), any());
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
        verify(model, times(9)).addAttribute(anyString(), any());
    }

    @Test
    public void testAddExperimentInvalidInput() {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        assertEquals(COURSE, courseController.addExperiment(BLANK, ID_STRING, model));
        verify(courseService).getCourse(ID);
        verify(experimentService, never()).existsExperiment(anyString());
        verify(courseService, never()).existsCourseExperiment(anyInt(), anyString());
        verify(courseService, never()).saveCourseExperiment(anyInt(), anyString());
        verify(model, times(9)).addAttribute(anyString(), any());
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
        verify(model, times(9)).addAttribute(anyString(), any());
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
        verify(model, times(9)).addAttribute(anyString(), any());
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

    @Test
    public void testGetNextExperimentPage() {
        courseDTO.setContent(null);
        when(pageService.getCourseExperimentPage(any(PageRequest.class), anyInt())).thenReturn(experiments);
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        ModelAndView mv = courseController.getNextExperimentPage(ID_STRING, LAST, CURRENT);
        assertAll(
                () -> assertEquals(EXPERIMENT_TABLE, mv.getViewName()),
                () -> assertEquals(courseDTO, mv.getModel().get("courseDTO")),
                () -> assertEquals(LAST_PAGE, mv.getModel().get("lastExperimentPage")),
                () -> assertEquals(4, mv.getModel().get("experimentPage"))
        );
        verify(pageService).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(courseService).getCourse(ID);
    }

    @Test
    public void testGetNextExperimentPageInvalidCurrent() {
        assertEquals(Constants.ERROR, courseController.getNextExperimentPage(ID_STRING, LAST, LAST).getViewName());
        verify(pageService, never()).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(courseService, never()).getCourse(anyInt());
    }

    @Test
    public void testGetNextExperimentPageInvalidParams() {
        assertEquals(Constants.ERROR, courseController.getNextExperimentPage(ID_STRING, BLANK, LAST).getViewName());
        verify(pageService, never()).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(courseService, never()).getCourse(anyInt());
    }

    @Test
    public void testGetPreviousExperimentPage() {
        when(pageService.getCourseExperimentPage(any(PageRequest.class), anyInt())).thenReturn(experiments);
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        ModelAndView mv = courseController.getPreviousExperimentPage(ID_STRING, LAST, CURRENT);
        assertAll(
                () -> assertEquals(EXPERIMENT_TABLE, mv.getViewName()),
                () -> assertEquals(courseDTO, mv.getModel().get("courseDTO")),
                () -> assertEquals(LAST_PAGE, mv.getModel().get("lastExperimentPage")),
                () -> assertEquals(2, mv.getModel().get("experimentPage"))
        );
        verify(pageService).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(courseService).getCourse(ID);
    }

    @Test
    public void testGetPreviousExperimentPageInvalidCurrent() {
        assertEquals(Constants.ERROR, courseController.getPreviousExperimentPage(ID_STRING, LAST, "1").getViewName());
        verify(pageService, never()).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(courseService, never()).getCourse(anyInt());
    }

    @Test
    public void testGetPreviousExperimentPageInvalidParams() {
        assertEquals(Constants.ERROR, courseController.getPreviousExperimentPage(ID_STRING, null, LAST).getViewName());
        verify(pageService, never()).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(courseService, never()).getCourse(anyInt());
    }

    @Test
    public void testGetPreviousExperimentPageInvalidParamsCurrent() {
        assertEquals(Constants.ERROR, courseController.getPreviousExperimentPage(ID_STRING, LAST, BLANK).getViewName());
        verify(pageService, never()).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(courseService, never()).getCourse(anyInt());
    }

    @Test
    public void testGetFirstExperimentPage() {
        when(pageService.getCourseExperimentPage(any(PageRequest.class), anyInt())).thenReturn(experiments);
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        ModelAndView mv = courseController.getFirstExperimentPage(ID_STRING, LAST);
        assertAll(
                () -> assertEquals(EXPERIMENT_TABLE, mv.getViewName()),
                () -> assertEquals(courseDTO, mv.getModel().get("courseDTO")),
                () -> assertEquals(LAST_PAGE, mv.getModel().get("lastExperimentPage")),
                () -> assertEquals(1, mv.getModel().get("experimentPage"))
        );
        verify(pageService).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(courseService).getCourse(ID);
    }

    @Test
    public void testGetFirstExperimentPageInvalidParams() {
        assertEquals(Constants.ERROR, courseController.getFirstExperimentPage(BLANK, LAST).getViewName());
        verify(pageService, never()).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(courseService, never()).getCourse(anyInt());
    }

    @Test
    public void testGetLastExperimentPage() {
        when(pageService.getCourseExperimentPage(any(PageRequest.class), anyInt())).thenReturn(experiments);
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        ModelAndView mv = courseController.getLastExperimentPage(ID_STRING, LAST);
        assertAll(
                () -> assertEquals(EXPERIMENT_TABLE, mv.getViewName()),
                () -> assertEquals(courseDTO, mv.getModel().get("courseDTO")),
                () -> assertEquals(LAST_PAGE, mv.getModel().get("lastExperimentPage")),
                () -> assertEquals(LAST_PAGE, mv.getModel().get("experimentPage"))
        );
        verify(pageService).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(courseService).getCourse(ID);
    }

    @Test
    public void testGetLastExperimentPageInvalidParams() {
        assertEquals(Constants.ERROR, courseController.getLastExperimentPage(ID_STRING, "-1").getViewName());
        verify(pageService, never()).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(courseService, never()).getCourse(anyInt());
    }

    private StringBuilder createLongString(int length) {
        StringBuilder longString = new StringBuilder();
        longString.append("a".repeat(Math.max(0, length)));
        return longString;
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

}
