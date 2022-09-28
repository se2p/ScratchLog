package fim.unipassau.de.scratch1984.integration;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.CourseService;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.application.service.PageService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.persistence.entity.CourseParticipant;
import fim.unipassau.de.scratch1984.persistence.projection.CourseExperimentProjection;
import fim.unipassau.de.scratch1984.persistence.projection.ExperimentTableProjection;
import fim.unipassau.de.scratch1984.spring.configuration.SecurityTestConfig;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.web.controller.CourseController;
import fim.unipassau.de.scratch1984.web.dto.CourseDTO;
import fim.unipassau.de.scratch1984.web.dto.UserDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(SpringExtension.class)
@WebMvcTest(CourseController.class)
@Import(SecurityTestConfig.class)
@ActiveProfiles("test")
public class CourseControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private CourseService courseService;

    @MockBean
    private ExperimentService experimentService;

    @MockBean
    private UserService userService;

    @MockBean
    private PageService pageService;

    private static final String COURSE = "course";
    private static final String REDIRECT_COURSE = "redirect:/course?id=";
    private static final String COURSE_EDIT = "course-edit";
    private static final String EXPERIMENT_TABLE = "course::course_experiment_table";
    private static final String PARTICIPANT_TABLE = "course::course_participant_table";
    private static final String COURSE_DTO = "courseDTO";
    private static final String ID_STRING = "1";
    private static final String CURRENT = "3";
    private static final String LAST = "5";
    private static final String ID_PARAM = "id";
    private static final String TITLE_PARAM = "title";
    private static final String PARTICIPANT_PARAM = "participant";
    private static final String PAGE_PARAM = "page";
    private static final String LAST_PAGE_PARAM = "lastPage";
    private static final String ERROR_ATTRIBUTE = "error";
    private static final int ID = 1;
    private static final int LAST_PAGE = 5;
    private static final String TITLE = "Title";
    private static final String DESCRIPTION = "Description";
    private static final String CONTENT = "content";
    private static final String USERNAME = "participant";
    private static final LocalDateTime CHANGED = LocalDateTime.now();
    private final CourseDTO courseDTO = new CourseDTO(ID, TITLE, DESCRIPTION, CONTENT, true, CHANGED);
    private final UserDTO userDTO = new UserDTO(USERNAME, "part@part.de", UserDTO.Role.PARTICIPANT,
            UserDTO.Language.ENGLISH, "password", "secret");
    private final String TOKEN_ATTR_NAME = "org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN";
    private final HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
    private final CsrfToken csrfToken = httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());
    private final Page<CourseExperimentProjection> experiments = new PageImpl<>(getCourseExperiments(2));
    private final Page<CourseParticipant> participants = new PageImpl<>(new ArrayList<>());

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

    @AfterEach
    public void resetService() {
        reset(courseService);
    }

    @Test
    public void testGetCourseForm() throws Exception {
        mvc.perform(get("/course/create")
                        .flashAttr(COURSE_DTO, courseDTO)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(COURSE_EDIT));
    }

    @Test
    public void testGetEditCourseForm() throws Exception {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        mvc.perform(get("/course/edit")
                        .param(ID_PARAM, ID_STRING)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(COURSE_EDIT))
                .andExpect(model().attribute(COURSE_DTO, is(courseDTO)));
        verify(courseService).getCourse(ID);
    }

    @Test
    public void testGetEditCourseFormNotFound() throws Exception {
        when(courseService.getCourse(ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/course/edit")
                        .param(ID_PARAM, ID_STRING)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(courseService).getCourse(ID);
    }

    @Test
    public void testGetEditCourseFormInvalidId() throws Exception {
        mvc.perform(get("/course/edit")
                        .param(ID_PARAM, TITLE)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(courseService, never()).getCourse(anyInt());
    }

    @Test
    public void testUpdateCourse() throws Exception {
        when(courseService.saveCourse(courseDTO)).thenReturn(ID);
        mvc.perform(post("/course/update")
                        .flashAttr(COURSE_DTO, courseDTO)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_COURSE + ID));
        verify(courseService).existsCourse(ID, TITLE);
        verify(courseService).saveCourse(courseDTO);
    }

    @Test
    public void testUpdateCourseTitleExists() throws Exception {
        when(courseService.existsCourse(ID, TITLE)).thenReturn(true);
        mvc.perform(post("/course/update")
                        .flashAttr(COURSE_DTO, courseDTO)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(COURSE_EDIT));
        verify(courseService).existsCourse(ID, TITLE);
        verify(courseService, never()).saveCourse(any());
    }

    @Test
    public void testUpdateNewCourseTitleExists() throws Exception {
        courseDTO.setId(null);
        when(courseService.existsCourse(TITLE)).thenReturn(true);
        mvc.perform(post("/course/update")
                        .flashAttr(COURSE_DTO, courseDTO)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(COURSE_EDIT));
        verify(courseService).existsCourse(TITLE);
        verify(courseService, never()).saveCourse(any());
    }

    @Test
    public void testUpdateCourseInvalidTitleAndDescription() throws Exception {
        courseDTO.setTitle("");
        courseDTO.setDescription(null);
        mvc.perform(post("/course/update")
                        .flashAttr(COURSE_DTO, courseDTO)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(COURSE_EDIT));
        verify(courseService).existsCourse(ID, "");
        verify(courseService, never()).saveCourse(any());
    }

    @Test
    public void testAddParticipant() throws Exception {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(userService.getUserByUsernameOrEmail(USERNAME)).thenReturn(userDTO);
        when(courseService.saveCourseParticipant(ID, USERNAME)).thenReturn(ID);
        mvc.perform(get("/course/participant/add")
                        .param(ID_PARAM, ID_STRING)
                        .param(PARTICIPANT_PARAM, USERNAME)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_COURSE + ID));
        verify(courseService).getCourse(ID);
        verify(userService).getUserByUsernameOrEmail(USERNAME);
        verify(courseService).existsCourseParticipant(ID, USERNAME);
        verify(courseService).saveCourseParticipant(ID, USERNAME);
        verify(courseService, never()).addParticipantToCourseExperiments(anyInt(), anyInt());
    }

    @Test
    public void testAddParticipantInvalidInput() throws Exception {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        mvc.perform(get("/course/participant/add")
                        .param(ID_PARAM, ID_STRING)
                        .param(PARTICIPANT_PARAM, " ")
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(COURSE))
                .andExpect(model().attribute(ERROR_ATTRIBUTE, notNullValue()));
        verify(courseService).getCourse(ID);
        verify(userService, never()).getUserByUsernameOrEmail(anyString());
        verify(courseService, never()).existsCourseParticipant(anyInt(), anyString());
        verify(courseService, never()).saveCourseParticipant(anyInt(), anyString());
        verify(courseService, never()).addParticipantToCourseExperiments(anyInt(), anyInt());
    }

    @Test
    public void testAddParticipantCourseNotFound() throws Exception {
        when(courseService.getCourse(ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/course/participant/add")
                        .param(ID_PARAM, ID_STRING)
                        .param(PARTICIPANT_PARAM, USERNAME)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(courseService).getCourse(ID);
        verify(userService, never()).getUserByUsernameOrEmail(anyString());
        verify(courseService, never()).existsCourseParticipant(anyInt(), anyString());
        verify(courseService, never()).saveCourseParticipant(anyInt(), anyString());
        verify(courseService, never()).addParticipantToCourseExperiments(anyInt(), anyInt());
    }

    @Test
    public void testDeleteParticipant() throws Exception {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(userService.getUserByUsernameOrEmail(USERNAME)).thenReturn(userDTO);
        when(courseService.existsCourseParticipant(ID, USERNAME)).thenReturn(true);
        mvc.perform(get("/course/participant/delete")
                        .param(ID_PARAM, ID_STRING)
                        .param(PARTICIPANT_PARAM, USERNAME)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_COURSE + ID));
        verify(courseService).getCourse(ID);
        verify(userService).getUserByUsernameOrEmail(USERNAME);
        verify(courseService).existsCourseParticipant(ID, USERNAME);
        verify(courseService).deleteCourseParticipant(ID, USERNAME);
    }

    @Test
    public void testDeleteParticipantNotFound() throws Exception {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        mvc.perform(get("/course/participant/delete")
                        .param(ID_PARAM, ID_STRING)
                        .param(PARTICIPANT_PARAM, USERNAME)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(COURSE))
                .andExpect(model().attribute(ERROR_ATTRIBUTE, notNullValue()));
        verify(courseService).getCourse(ID);
        verify(userService).getUserByUsernameOrEmail(USERNAME);
        verify(courseService, never()).existsCourseParticipant(anyInt(), anyString());
        verify(courseService, never()).deleteCourseParticipant(anyInt(), anyString());
    }

    @Test
    public void testDeleteParticipantCourseInactive() throws Exception {
        courseDTO.setActive(false);
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        mvc.perform(get("/course/participant/delete")
                        .param(ID_PARAM, ID_STRING)
                        .param(PARTICIPANT_PARAM, USERNAME)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(courseService).getCourse(ID);
        verify(userService, never()).getUserByUsernameOrEmail(anyString());
        verify(courseService, never()).existsCourseParticipant(anyInt(), anyString());
        verify(courseService, never()).deleteCourseParticipant(anyInt(), anyString());
    }

    @Test
    public void testAddExperiment() throws Exception {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(experimentService.existsExperiment(TITLE)).thenReturn(true);
        mvc.perform(get("/course/experiment/add")
                        .param(ID_PARAM, ID_STRING)
                        .param(TITLE_PARAM, TITLE)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_COURSE + ID));
        verify(courseService).getCourse(ID);
        verify(experimentService).existsExperiment(TITLE);
        verify(courseService).existsCourseExperiment(ID, TITLE);
        verify(courseService).saveCourseExperiment(ID, TITLE);
    }

    @Test
    public void testAddExperimentNotExistent() throws Exception {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        mvc.perform(get("/course/experiment/add")
                        .param(ID_PARAM, ID_STRING)
                        .param(TITLE_PARAM, TITLE)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(COURSE))
                .andExpect(model().attribute(ERROR_ATTRIBUTE, notNullValue()));
        verify(courseService).getCourse(ID);
        verify(experimentService).existsExperiment(TITLE);
        verify(courseService, never()).existsCourseExperiment(anyInt(), anyString());
        verify(courseService, never()).saveCourseExperiment(anyInt(), anyString());
    }

    @Test
    public void testAddExperimentInvalidId() throws Exception {
        mvc.perform(get("/course/experiment/add")
                        .param(ID_PARAM, "0")
                        .param(TITLE_PARAM, TITLE)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(courseService, never()).getCourse(anyInt());
        verify(experimentService, never()).existsExperiment(anyString());
        verify(courseService, never()).existsCourseExperiment(anyInt(), anyString());
        verify(courseService, never()).saveCourseExperiment(anyInt(), anyString());
    }

    @Test
    public void testDeleteExperiment() throws Exception {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(experimentService.existsExperiment(TITLE)).thenReturn(true);
        when(courseService.existsCourseExperiment(ID, TITLE)).thenReturn(true);
        mvc.perform(get("/course/experiment/delete")
                        .param(ID_PARAM, ID_STRING)
                        .param(TITLE_PARAM, TITLE)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_COURSE + ID));
        verify(courseService).getCourse(ID);
        verify(experimentService).existsExperiment(TITLE);
        verify(courseService).existsCourseExperiment(ID, TITLE);
        verify(courseService).deleteCourseExperiment(ID, TITLE);
    }

    @Test
    public void testDeleteExperimentInvalidInput() throws Exception {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        mvc.perform(get("/course/experiment/delete")
                        .param(ID_PARAM, ID_STRING)
                        .param(TITLE_PARAM, "  ")
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(COURSE))
                .andExpect(model().attribute(ERROR_ATTRIBUTE, notNullValue()));
        verify(courseService).getCourse(ID);
        verify(experimentService, never()).existsExperiment(anyString());
        verify(courseService, never()).existsCourseExperiment(anyInt(), anyString());
        verify(courseService, never()).deleteCourseExperiment(anyInt(), anyString());
    }

    @Test
    public void testDeleteExperimentNotFound() throws Exception {
        when(courseService.getCourse(ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/course/experiment/delete")
                        .param(ID_PARAM, ID_STRING)
                        .param(TITLE_PARAM, TITLE)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(courseService).getCourse(ID);
        verify(experimentService, never()).existsExperiment(anyString());
        verify(courseService, never()).existsCourseExperiment(anyInt(), anyString());
        verify(courseService, never()).deleteCourseExperiment(anyInt(), anyString());
    }

    @Test
    public void testGetNextParticipantPage() throws Exception {
        when(pageService.getParticipantCoursePage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        mvc.perform(get("/course/next/participant")
                        .param(ID_PARAM, ID_STRING)
                        .param(LAST_PAGE_PARAM, LAST)
                        .param(PAGE_PARAM, CURRENT)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(PARTICIPANT_TABLE))
                .andExpect(model().attribute(COURSE_DTO, is(courseDTO)))
                .andExpect(model().attribute("participants", is(participants)))
                .andExpect(model().attribute("participantPage", is(4)))
                .andExpect(model().attribute("lastParticipantPage", is(LAST_PAGE)));
        verify(pageService).getParticipantCoursePage(anyInt(), any(PageRequest.class));
        verify(courseService).getCourse(ID);
    }

    @Test
    public void testGetNextParticipantPageError() throws Exception {
        mvc.perform(get("/course/next/participant")
                        .param(ID_PARAM, ID_STRING)
                        .param(LAST_PAGE_PARAM, TITLE)
                        .param(PAGE_PARAM, CURRENT)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(pageService, never()).getParticipantCoursePage(anyInt(), any(PageRequest.class));
        verify(courseService, never()).getCourse(anyInt());
    }

    @Test
    public void testGetPreviousParticipantPage() throws Exception {
        when(pageService.getParticipantCoursePage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        mvc.perform(get("/course/previous/participant")
                        .param(ID_PARAM, ID_STRING)
                        .param(LAST_PAGE_PARAM, LAST)
                        .param(PAGE_PARAM, CURRENT)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(PARTICIPANT_TABLE))
                .andExpect(model().attribute(COURSE_DTO, is(courseDTO)))
                .andExpect(model().attribute("participants", is(participants)))
                .andExpect(model().attribute("participantPage", is(2)))
                .andExpect(model().attribute("lastParticipantPage", is(LAST_PAGE)));
        verify(pageService).getParticipantCoursePage(anyInt(), any(PageRequest.class));
        verify(courseService).getCourse(ID);
    }

    @Test
    public void testGetPreviousParticipantPageError() throws Exception {
        mvc.perform(get("/course/previous/participant")
                        .param(ID_PARAM, "0")
                        .param(LAST_PAGE_PARAM, LAST)
                        .param(PAGE_PARAM, CURRENT)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(pageService, never()).getParticipantCoursePage(anyInt(), any(PageRequest.class));
        verify(courseService, never()).getCourse(anyInt());
    }

    @Test
    public void testGetFirstParticipantPage() throws Exception {
        when(pageService.getParticipantCoursePage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        mvc.perform(get("/course/first/participant")
                        .param(ID_PARAM, ID_STRING)
                        .param(LAST_PAGE_PARAM, LAST)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(PARTICIPANT_TABLE))
                .andExpect(model().attribute(COURSE_DTO, is(courseDTO)))
                .andExpect(model().attribute("participants", is(participants)))
                .andExpect(model().attribute("participantPage", is(1)))
                .andExpect(model().attribute("lastParticipantPage", is(LAST_PAGE)));
        verify(pageService).getParticipantCoursePage(anyInt(), any(PageRequest.class));
        verify(courseService).getCourse(ID);
    }

    @Test
    public void testGetFirstParticipantPageError() throws Exception {
        mvc.perform(get("/course/first/participant")
                        .param(ID_PARAM, TITLE)
                        .param(LAST_PAGE_PARAM, LAST)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(pageService, never()).getParticipantCoursePage(anyInt(), any(PageRequest.class));
        verify(courseService, never()).getCourse(anyInt());
    }

    @Test
    public void testGetLastParticipantPage() throws Exception {
        when(pageService.getParticipantCoursePage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        mvc.perform(get("/course/last/participant")
                        .param(ID_PARAM, ID_STRING)
                        .param(LAST_PAGE_PARAM, LAST)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(PARTICIPANT_TABLE))
                .andExpect(model().attribute(COURSE_DTO, is(courseDTO)))
                .andExpect(model().attribute("participants", is(participants)))
                .andExpect(model().attribute("participantPage", is(LAST_PAGE)))
                .andExpect(model().attribute("lastParticipantPage", is(LAST_PAGE)));
        verify(pageService).getParticipantCoursePage(anyInt(), any(PageRequest.class));
        verify(courseService).getCourse(ID);
    }

    @Test
    public void testGetLastParticipantPageError() throws Exception {
        mvc.perform(get("/course/last/participant")
                        .param(ID_PARAM, ID_STRING)
                        .param(LAST_PAGE_PARAM, " ")
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(pageService, never()).getParticipantCoursePage(anyInt(), any(PageRequest.class));
        verify(courseService, never()).getCourse(anyInt());
    }

    @Test
    public void testGetNextExperimentPage() throws Exception {
        when(pageService.getCourseExperimentPage(any(PageRequest.class), anyInt())).thenReturn(experiments);
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        mvc.perform(get("/course/next/experiment")
                        .param(ID_PARAM, ID_STRING)
                        .param(LAST_PAGE_PARAM, LAST)
                        .param(PAGE_PARAM, CURRENT)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT_TABLE))
                .andExpect(model().attribute(COURSE_DTO, is(courseDTO)))
                .andExpect(model().attribute("experiments", is(experiments)))
                .andExpect(model().attribute("experimentPage", is(4)))
                .andExpect(model().attribute("lastExperimentPage", is(LAST_PAGE)));
        verify(pageService).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(courseService).getCourse(ID);
    }

    @Test
    public void testGetNextExperimentPageError() throws Exception {
        mvc.perform(get("/course/next/experiment")
                        .param(ID_PARAM, "0")
                        .param(LAST_PAGE_PARAM, LAST)
                        .param(PAGE_PARAM, CURRENT)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(pageService, never()).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(courseService, never()).getCourse(anyInt());
    }

    @Test
    public void testGetPreviousExperimentPage() throws Exception {
        when(pageService.getCourseExperimentPage(any(PageRequest.class), anyInt())).thenReturn(experiments);
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        mvc.perform(get("/course/previous/experiment")
                        .param(ID_PARAM, ID_STRING)
                        .param(LAST_PAGE_PARAM, LAST)
                        .param(PAGE_PARAM, CURRENT)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT_TABLE))
                .andExpect(model().attribute(COURSE_DTO, is(courseDTO)))
                .andExpect(model().attribute("experiments", is(experiments)))
                .andExpect(model().attribute("experimentPage", is(2)))
                .andExpect(model().attribute("lastExperimentPage", is(LAST_PAGE)));
        verify(pageService).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(courseService).getCourse(ID);
    }

    @Test
    public void testGetPreviousExperimentPageError() throws Exception {
        mvc.perform(get("/course/previous/experiment")
                        .param(ID_PARAM, "  ")
                        .param(LAST_PAGE_PARAM, LAST)
                        .param(PAGE_PARAM, CURRENT)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(pageService, never()).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(courseService, never()).getCourse(anyInt());
    }

    @Test
    public void testGetFirstExperimentPage() throws Exception {
        when(pageService.getCourseExperimentPage(any(PageRequest.class), anyInt())).thenReturn(experiments);
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        mvc.perform(get("/course/first/experiment")
                        .param(ID_PARAM, ID_STRING)
                        .param(LAST_PAGE_PARAM, LAST)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT_TABLE))
                .andExpect(model().attribute(COURSE_DTO, is(courseDTO)))
                .andExpect(model().attribute("experiments", is(experiments)))
                .andExpect(model().attribute("experimentPage", is(1)))
                .andExpect(model().attribute("lastExperimentPage", is(LAST_PAGE)));
        verify(pageService).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(courseService).getCourse(ID);
    }

    @Test
    public void testGetFirstExperimentPageError() throws Exception {
        mvc.perform(get("/course/first/experiment")
                        .param(ID_PARAM, ID_STRING)
                        .param(LAST_PAGE_PARAM, "-1")
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(pageService, never()).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(courseService, never()).getCourse(anyInt());
    }

    @Test
    public void testGetLastExperimentPage() throws Exception {
        when(pageService.getCourseExperimentPage(any(PageRequest.class), anyInt())).thenReturn(experiments);
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        mvc.perform(get("/course/last/experiment")
                        .param(ID_PARAM, ID_STRING)
                        .param(LAST_PAGE_PARAM, LAST)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT_TABLE))
                .andExpect(model().attribute(COURSE_DTO, is(courseDTO)))
                .andExpect(model().attribute("experiments", is(experiments)))
                .andExpect(model().attribute("experimentPage", is(LAST_PAGE)))
                .andExpect(model().attribute("lastExperimentPage", is(LAST_PAGE)));
        verify(pageService).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(courseService).getCourse(ID);
    }

    @Test
    public void testGetLastExperimentPageError() throws Exception {
        mvc.perform(get("/course/last/experiment")
                        .param(ID_PARAM, "  ")
                        .param(LAST_PAGE_PARAM, LAST)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(pageService, never()).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(courseService, never()).getCourse(anyInt());
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
