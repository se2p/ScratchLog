package fim.unipassau.de.scratch1984.integration;

import fim.unipassau.de.scratch1984.MailServerSetter;
import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.application.service.PageService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.persistence.projection.CourseTableProjection;
import fim.unipassau.de.scratch1984.persistence.projection.ExperimentTableProjection;
import fim.unipassau.de.scratch1984.spring.configuration.SecurityTestConfig;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.web.controller.HomeController;
import fim.unipassau.de.scratch1984.web.dto.ExperimentDTO;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(SpringExtension.class)
@WebMvcTest(HomeController.class)
@Import(SecurityTestConfig.class)
@ActiveProfiles("test")
public class HomeControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ExperimentService experimentService;

    @MockBean
    private PageService pageService;

    @MockBean
    private UserService userService;

    private static final String INDEX = "index";
    private static final String INDEX_EXPERIMENT = "index::experiment_table";
    private static final String INDEX_COURSE = "index::course_table";
    private static final String FINISH = "experiment-finish";
    private static final String PASSWORD_RESET = "password-reset";
    private static final String CURRENT = "3";
    private static final String LAST = "4";
    private static final String BLANK = "   ";
    private static final String INVALID_NUMBER = "-5";
    private static final String PAGE_PARAM = "page";
    private static final String LAST_EXPERIMENT_PAGE = "lastExperimentPage";
    private static final String LAST_COURSE_PAGE = "lastCoursePage";
    private static final String EXPERIMENT_PAGE = "experimentPage";
    private static final String COURSE_PAGE = "coursePage";
    private static final String EXPERIMENTS = "experiments";
    private static final String COURSES = "courses";
    private static final String ID_STRING = "1";
    private static final String THANKS = "thanks";
    private static final String EXPERIMENT = "experiment";
    private static final String USER = "user";
    private static final String GUI_URL = "scratch";
    private static final String NEXT_COURSE = "/next/course";
    private static final String PREV_COURSE = "/previous/course";
    private static final String FIRST_COURSE = "/first/course";
    private static final String LAST_COURSE = "/last/course";
    private static final String NEXT_EXPERIMENT = "/next/experiment";
    private static final String PREV_EXPERIMENT = "/previous/experiment";
    private static final String FIRST_EXPERIMENT = "/first/experiment";
    private static final String LAST_EXPERIMENT = "/last/experiment";
    private final int pageNum = 3;
    private final int lastPage = 4;
    private static final int ID = 1;
    private static final ExperimentDTO experimentDTO = new ExperimentDTO(ID, "My Experiment", "description",
            "info", "postscript", true, GUI_URL);
    private static final UserDTO userDTO = new UserDTO("participant", "email", UserDTO.Role.PARTICIPANT,
            UserDTO.Language.ENGLISH, "password", "");
    private final Page<ExperimentTableProjection> experimentPage = new PageImpl<>(getExperimentProjections(5));
    private final Page<CourseTableProjection> coursePage = new PageImpl<>(getCourseTableProjections(3));
    private final String TOKEN_ATTR_NAME = "org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN";
    private final HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
    private final CsrfToken csrfToken = httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());

    @BeforeEach
    public void setup() {
        userDTO.setId(ID);
    }

    @AfterEach
    public void resetService() {
        reset(experimentService);
    }

    @Test
    public void testGetIndexPage() throws Exception {
        mvc.perform(get("/")
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(INDEX));
        verify(userService, never()).getUser(anyString());
        verify(pageService, never()).computeLastExperimentPage();
        verify(pageService, never()).getExperimentPage(any(PageRequest.class));
        verify(pageService, never()).getCoursePage(any(PageRequest.class));
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void testGetIndexPageAdmin() throws Exception {
        when(pageService.computeLastExperimentPage()).thenReturn(lastPage);
        when(pageService.computeLastCoursePage()).thenReturn(lastPage);
        when(pageService.getExperimentPage(any(PageRequest.class))).thenReturn(experimentPage);
        when(pageService.getCoursePage(any(PageRequest.class))).thenReturn(coursePage);
        mvc.perform(get("/")
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(EXPERIMENTS, is(experimentPage)))
                .andExpect(model().attribute(LAST_EXPERIMENT_PAGE, is(lastPage)))
                .andExpect(model().attribute(EXPERIMENT_PAGE, is(1)))
                .andExpect(model().attribute(COURSES, is(coursePage)))
                .andExpect(model().attribute(LAST_COURSE_PAGE, is(lastPage)))
                .andExpect(model().attribute(COURSE_PAGE, is(1)))
                .andExpect(view().name(INDEX));
        verify(userService, never()).getUser(anyString());
        verify(pageService).computeLastExperimentPage();
        verify(pageService).computeLastCoursePage();
        verify(pageService).getExperimentPage(any(PageRequest.class));
        verify(pageService).getCoursePage(any(PageRequest.class));
    }

    @Test
    @WithMockUser(username = "participant", roles = {"PARTICIPANT"})
    public void testGetIndexPageParticipant() throws Exception {
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(pageService.getLastExperimentPage(userDTO.getId())).thenReturn(lastPage);
        when(pageService.getLastCoursePage(userDTO.getId())).thenReturn(lastPage);
        when(pageService.getExperimentParticipantPage(any(PageRequest.class),
                anyInt())).thenReturn(experimentPage);
        when(pageService.getCourseParticipantPage(any(PageRequest.class), anyInt())).thenReturn(coursePage);
        mvc.perform(get("/")
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(EXPERIMENTS, is(experimentPage)))
                .andExpect(model().attribute(LAST_EXPERIMENT_PAGE, is(lastPage)))
                .andExpect(model().attribute(EXPERIMENT_PAGE, is(1)))
                .andExpect(model().attribute(COURSES, is(coursePage)))
                .andExpect(model().attribute(LAST_COURSE_PAGE, is(lastPage)))
                .andExpect(model().attribute(COURSE_PAGE, is(1)))
                .andExpect(view().name(INDEX));
        verify(userService).getUser(userDTO.getUsername());
        verify(pageService).getLastExperimentPage(userDTO.getId());
        verify(pageService).getLastCoursePage(userDTO.getId());
        verify(pageService).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(pageService).getCourseParticipantPage(any(PageRequest.class), anyInt());
    }

    @Test
    @WithMockUser(username = "participant", roles = {"PARTICIPANT"})
    public void testGetIndexPageParticipantNotFound() throws Exception {
        when(userService.getUser(userDTO.getUsername())).thenThrow(NotFoundException.class);
        mvc.perform(get("/")
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(userService).getUser(userDTO.getUsername());
        verify(pageService, never()).getLastExperimentPage(anyInt());
        verify(pageService, never()).getLastCoursePage(anyInt());
        verify(pageService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(pageService, never()).getCourseParticipantPage(any(PageRequest.class), anyInt());
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void testGetNextCoursePage() throws Exception {
        when(pageService.computeLastCoursePage()).thenReturn(lastPage);
        when(pageService.getCoursePage(any(PageRequest.class))).thenReturn(coursePage);
        mvc.perform(get(NEXT_COURSE)
                        .param(PAGE_PARAM, CURRENT)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(COURSES, is(coursePage)))
                .andExpect(model().attribute(LAST_COURSE_PAGE, is(lastPage)))
                .andExpect(model().attribute(COURSE_PAGE, is(pageNum + 1)))
                .andExpect(view().name(INDEX_COURSE));
        verify(pageService).computeLastCoursePage();
        verify(pageService).getCoursePage(any(PageRequest.class));
        verify(userService, never()).getUser(anyString());
        verify(pageService, never()).getLastCoursePage(anyInt());
    }

    @Test
    @WithMockUser(username = "participant", roles = {"PARTICIPANT"})
    public void testGetNextCoursePageParticipant() throws Exception {
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(pageService.getLastCoursePage(userDTO.getId())).thenReturn(lastPage);
        when(pageService.getCourseParticipantPage(any(PageRequest.class), anyInt())).thenReturn(coursePage);
        mvc.perform(get(NEXT_COURSE)
                        .param(PAGE_PARAM, CURRENT)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(COURSES, is(coursePage)))
                .andExpect(model().attribute(LAST_COURSE_PAGE, is(lastPage)))
                .andExpect(model().attribute(COURSE_PAGE, is(pageNum + 1)))
                .andExpect(view().name(INDEX_COURSE));
        verify(pageService, never()).computeLastCoursePage();
        verify(userService).getUser(userDTO.getUsername());
        verify(pageService).getLastCoursePage(userDTO.getId());
        verify(pageService).getCourseParticipantPage(any(PageRequest.class), anyInt());
    }

    @Test
    @WithMockUser(username = "participant", roles = {"PARTICIPANT"})
    public void testGetNextCoursePageParticipantNotFound() throws Exception {
        when(userService.getUser(userDTO.getUsername())).thenThrow(NotFoundException.class);
        mvc.perform(get(NEXT_COURSE)
                        .param(PAGE_PARAM, CURRENT)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(pageService, never()).computeLastCoursePage();
        verify(userService).getUser(userDTO.getUsername());
        verify(pageService, never()).getLastCoursePage(anyInt());
        verify(pageService, never()).getCourseParticipantPage(any(PageRequest.class), anyInt());
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void testGetPreviousCoursePage() throws Exception {
        when(pageService.computeLastCoursePage()).thenReturn(lastPage);
        when(pageService.getCoursePage(any(PageRequest.class))).thenReturn(coursePage);
        mvc.perform(get(PREV_COURSE)
                        .param(PAGE_PARAM, CURRENT)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(COURSES, is(coursePage)))
                .andExpect(model().attribute(LAST_COURSE_PAGE, is(lastPage)))
                .andExpect(model().attribute(COURSE_PAGE, is(pageNum - 1)))
                .andExpect(view().name(INDEX_COURSE));
        verify(pageService).computeLastCoursePage();
        verify(pageService).getCoursePage(any(PageRequest.class));
        verify(userService, never()).getUser(anyString());
        verify(pageService, never()).getLastCoursePage(anyInt());
    }

    @Test
    @WithMockUser(username = "participant", roles = {"PARTICIPANT"})
    public void testGetPreviousCoursePageParticipant() throws Exception {
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(pageService.getLastCoursePage(userDTO.getId())).thenReturn(lastPage);
        when(pageService.getCourseParticipantPage(any(PageRequest.class), anyInt())).thenReturn(coursePage);
        mvc.perform(get(PREV_COURSE)
                        .param(PAGE_PARAM, CURRENT)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(COURSES, is(coursePage)))
                .andExpect(model().attribute(LAST_COURSE_PAGE, is(lastPage)))
                .andExpect(model().attribute(COURSE_PAGE, is(pageNum - 1)))
                .andExpect(view().name(INDEX_COURSE));
        verify(pageService, never()).computeLastCoursePage();
        verify(userService).getUser(userDTO.getUsername());
        verify(pageService).getLastCoursePage(userDTO.getId());
        verify(pageService).getCourseParticipantPage(any(PageRequest.class), anyInt());
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void testGetFirstCoursePage() throws Exception {
        when(pageService.computeLastCoursePage()).thenReturn(lastPage);
        when(pageService.getCoursePage(any(PageRequest.class))).thenReturn(coursePage);
        mvc.perform(get(FIRST_COURSE)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(COURSES, is(coursePage)))
                .andExpect(model().attribute(LAST_COURSE_PAGE, is(lastPage)))
                .andExpect(model().attribute(COURSE_PAGE, is(1)))
                .andExpect(view().name(INDEX_COURSE));
        verify(pageService).computeLastCoursePage();
        verify(pageService).getCoursePage(any(PageRequest.class));
        verify(userService, never()).getUser(anyString());
        verify(pageService, never()).getLastCoursePage(anyInt());
    }

    @Test
    @WithMockUser(username = "participant", roles = {"PARTICIPANT"})
    public void testGetFirstCoursePageParticipantNotFound() throws Exception {
        when(userService.getUser(userDTO.getUsername())).thenThrow(NotFoundException.class);
        mvc.perform(get(FIRST_COURSE)
                        .param(PAGE_PARAM, CURRENT)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(pageService, never()).computeLastCoursePage();
        verify(userService).getUser(userDTO.getUsername());
        verify(pageService, never()).getLastCoursePage(anyInt());
        verify(pageService, never()).getCourseParticipantPage(any(PageRequest.class), anyInt());
    }

    @Test
    @WithMockUser(username = "participant", roles = {"PARTICIPANT"})
    public void testGetLastCoursePageParticipant() throws Exception {
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(pageService.getLastCoursePage(userDTO.getId())).thenReturn(lastPage);
        when(pageService.getCourseParticipantPage(any(PageRequest.class), anyInt())).thenReturn(coursePage);
        mvc.perform(get(LAST_COURSE)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(COURSES, is(coursePage)))
                .andExpect(model().attribute(LAST_COURSE_PAGE, is(lastPage)))
                .andExpect(model().attribute(COURSE_PAGE, is(lastPage)))
                .andExpect(view().name(INDEX_COURSE));
        verify(pageService, never()).computeLastCoursePage();
        verify(userService).getUser(userDTO.getUsername());
        verify(pageService).getLastCoursePage(userDTO.getId());
        verify(pageService).getCourseParticipantPage(any(PageRequest.class), anyInt());
    }

    @Test
    @WithMockUser(username = "participant", roles = {"PARTICIPANT"})
    public void testGetLastCoursePageParticipantNotFound() throws Exception {
        when(userService.getUser(userDTO.getUsername())).thenThrow(NotFoundException.class);
        mvc.perform(get(LAST_COURSE)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(pageService, never()).computeLastCoursePage();
        verify(userService).getUser(userDTO.getUsername());
        verify(pageService, never()).getLastCoursePage(anyInt());
        verify(pageService, never()).getCourseParticipantPage(any(PageRequest.class), anyInt());
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void testGetNextExperimentPage() throws Exception {
        when(pageService.computeLastExperimentPage()).thenReturn(lastPage);
        when(pageService.getExperimentPage(any(PageRequest.class))).thenReturn(experimentPage);
        mvc.perform(get(NEXT_EXPERIMENT)
                        .param(PAGE_PARAM, CURRENT)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(EXPERIMENTS, is(experimentPage)))
                .andExpect(model().attribute(LAST_EXPERIMENT_PAGE, is(lastPage)))
                .andExpect(model().attribute(EXPERIMENT_PAGE, is(pageNum + 1)))
                .andExpect(view().name(INDEX_EXPERIMENT));
        verify(pageService).computeLastExperimentPage();
        verify(pageService).getExperimentPage(any(PageRequest.class));
        verify(userService, never()).getUser(anyString());
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void testGetNextExperimentPageCurrentBiggerLast() throws Exception {
        when(pageService.computeLastExperimentPage()).thenReturn(2);
        mvc.perform(get(NEXT_EXPERIMENT)
                        .param(PAGE_PARAM, LAST)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(pageService).computeLastExperimentPage();
        verify(pageService, never()).getExperimentPage(any(PageRequest.class));
        verify(userService, never()).getUser(anyString());
    }

    @Test
    @WithMockUser(username = "participant", roles = {"PARTICIPANT"})
    public void testGetNextExperimentPageParticipant() throws Exception {
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(pageService.getLastExperimentPage(userDTO.getId())).thenReturn(lastPage);
        when(pageService.getExperimentParticipantPage(any(PageRequest.class), anyInt())).thenReturn(experimentPage);
        mvc.perform(get(NEXT_EXPERIMENT)
                        .param(PAGE_PARAM, CURRENT)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(EXPERIMENTS, is(experimentPage)))
                .andExpect(model().attribute(LAST_EXPERIMENT_PAGE, is(lastPage)))
                .andExpect(model().attribute(EXPERIMENT_PAGE, is(pageNum + 1)))
                .andExpect(view().name(INDEX_EXPERIMENT));
        verify(pageService, never()).computeLastExperimentPage();
        verify(userService).getUser(userDTO.getUsername());
        verify(pageService).getLastExperimentPage(userDTO.getId());
        verify(pageService).getExperimentParticipantPage(any(PageRequest.class), anyInt());
    }

    @Test
    @WithMockUser(username = "participant", roles = {"PARTICIPANT"})
    public void testGetNextExperimentPageParticipantCurrentBiggerLast() throws Exception {
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(pageService.getLastExperimentPage(userDTO.getId())).thenReturn(2);
        mvc.perform(get(NEXT_EXPERIMENT)
                        .param(PAGE_PARAM, CURRENT)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(pageService, never()).computeLastExperimentPage();
        verify(userService).getUser(userDTO.getUsername());
        verify(pageService).getLastExperimentPage(userDTO.getId());
        verify(pageService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
    }

    @Test
    @WithMockUser(username = "participant", roles = {"PARTICIPANT"})
    public void testGetNextExperimentPageParticipantNotFound() throws Exception {
        when(userService.getUser(userDTO.getUsername())).thenThrow(NotFoundException.class);
        mvc.perform(get(NEXT_EXPERIMENT)
                        .param(PAGE_PARAM, CURRENT)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(pageService, never()).computeLastExperimentPage();
        verify(userService).getUser(userDTO.getUsername());
        verify(pageService, never()).getLastExperimentPage(userDTO.getId());
        verify(pageService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void testGetNextExperimentPageParamInvalid() throws Exception {
        when(pageService.computeLastExperimentPage()).thenReturn(lastPage);
        mvc.perform(get(NEXT_EXPERIMENT)
                        .param(PAGE_PARAM, INVALID_NUMBER)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(pageService).computeLastExperimentPage();
        verify(pageService, never()).getExperimentPage(any(PageRequest.class));
        verify(userService, never()).getUser(anyString());
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void testGetPreviousExperimentPage() throws Exception {
        when(pageService.computeLastExperimentPage()).thenReturn(lastPage);
        when(pageService.getExperimentPage(any(PageRequest.class))).thenReturn(experimentPage);
        mvc.perform(get(PREV_EXPERIMENT)
                        .param(PAGE_PARAM, CURRENT)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(EXPERIMENTS, is(experimentPage)))
                .andExpect(model().attribute(LAST_EXPERIMENT_PAGE, is(lastPage)))
                .andExpect(model().attribute(EXPERIMENT_PAGE, is(pageNum - 1)))
                .andExpect(view().name(INDEX_EXPERIMENT));
        verify(pageService).computeLastExperimentPage();
        verify(pageService).getExperimentPage(any(PageRequest.class));
        verify(userService, never()).getUser(anyString());
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void testGetPreviousExperimentPageLastSmallerCurrent() throws Exception {
        when(pageService.computeLastExperimentPage()).thenReturn(2);
        mvc.perform(get(PREV_EXPERIMENT)
                        .param(PAGE_PARAM, LAST)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(pageService).computeLastExperimentPage();
        verify(pageService, never()).getExperimentPage(any(PageRequest.class));
        verify(userService, never()).getUser(anyString());
    }

    @Test
    @WithMockUser(username = "participant", roles = {"PARTICIPANT"})
    public void testGetPreviousExperimentPageParticipant() throws Exception {
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(pageService.getLastExperimentPage(userDTO.getId())).thenReturn(lastPage);
        when(pageService.getExperimentParticipantPage(any(PageRequest.class),
                anyInt())).thenReturn(experimentPage);
        mvc.perform(get(PREV_EXPERIMENT)
                        .param(PAGE_PARAM, CURRENT)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(EXPERIMENTS, is(experimentPage)))
                .andExpect(model().attribute(LAST_EXPERIMENT_PAGE, is(lastPage)))
                .andExpect(model().attribute(EXPERIMENT_PAGE, is(pageNum - 1)))
                .andExpect(view().name(INDEX_EXPERIMENT));
        verify(pageService, never()).computeLastExperimentPage();
        verify(userService).getUser(userDTO.getUsername());
        verify(pageService).getLastExperimentPage(userDTO.getId());
        verify(pageService).getExperimentParticipantPage(any(PageRequest.class), anyInt());
    }

    @Test
    @WithMockUser(username = "participant", roles = {"PARTICIPANT"})
    public void testGetPreviousExperimentPageParticipantLastSmallerCurrent() throws Exception {
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(pageService.getLastExperimentPage(userDTO.getId())).thenReturn(1);
        mvc.perform(get(PREV_EXPERIMENT)
                        .param(PAGE_PARAM, CURRENT)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(pageService, never()).computeLastExperimentPage();
        verify(userService).getUser(userDTO.getUsername());
        verify(pageService).getLastExperimentPage(userDTO.getId());
        verify(pageService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
    }

    @Test
    @WithMockUser(username = "participant", roles = {"PARTICIPANT"})
    public void testGetPreviousExperimentPageParticipantNotFound() throws Exception {
        when(userService.getUser(userDTO.getUsername())).thenThrow(NotFoundException.class);
        mvc.perform(get(PREV_EXPERIMENT)
                        .param(PAGE_PARAM, CURRENT)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(pageService, never()).computeLastExperimentPage();
        verify(userService).getUser(userDTO.getUsername());
        verify(pageService, never()).getLastExperimentPage(userDTO.getId());
        verify(pageService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testGetPreviousExperimentPageCurrentFirstPage() throws Exception {
        when(pageService.computeLastExperimentPage()).thenReturn(lastPage);
        mvc.perform(get(PREV_EXPERIMENT)
                        .param(PAGE_PARAM, "1")
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(pageService).computeLastExperimentPage();
        verify(pageService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
    }

    @Test
    @WithMockUser(username = "participant", roles = {"PARTICIPANT"})
    public void testGetPreviousExperimentPageParamInvalid() throws Exception {
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(pageService.getLastExperimentPage(userDTO.getId())).thenReturn(lastPage);
        mvc.perform(get(PREV_EXPERIMENT)
                        .param(PAGE_PARAM, BLANK)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(pageService).getLastExperimentPage(userDTO.getId());
        verify(pageService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void testGetFirstExperimentPage() throws Exception {
        when(pageService.computeLastExperimentPage()).thenReturn(lastPage);
        when(pageService.getExperimentPage(any(PageRequest.class))).thenReturn(experimentPage);
        mvc.perform(get(FIRST_EXPERIMENT)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(EXPERIMENTS, is(experimentPage)))
                .andExpect(model().attribute(LAST_EXPERIMENT_PAGE, is(lastPage)))
                .andExpect(model().attribute(EXPERIMENT_PAGE, is(1)))
                .andExpect(view().name(INDEX_EXPERIMENT));
        verify(pageService).computeLastExperimentPage();
        verify(pageService).getExperimentPage(any(PageRequest.class));
        verify(userService, never()).getUser(anyString());
    }

    @Test
    @WithMockUser(username = "participant", roles = {"PARTICIPANT"})
    public void testGetFirstExperimentPageParticipant() throws Exception {
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(pageService.getLastExperimentPage(userDTO.getId())).thenReturn(lastPage);
        when(pageService.getExperimentParticipantPage(any(PageRequest.class),
                anyInt())).thenReturn(experimentPage);
        mvc.perform(get(FIRST_EXPERIMENT)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(EXPERIMENTS, is(experimentPage)))
                .andExpect(model().attribute(LAST_EXPERIMENT_PAGE, is(lastPage)))
                .andExpect(model().attribute(EXPERIMENT_PAGE, is(1)))
                .andExpect(view().name(INDEX_EXPERIMENT));
        verify(pageService, never()).computeLastExperimentPage();
        verify(userService).getUser(userDTO.getUsername());
        verify(pageService).getLastExperimentPage(userDTO.getId());
        verify(pageService).getExperimentParticipantPage(any(PageRequest.class), anyInt());
    }

    @Test
    @WithMockUser(username = "participant", roles = {"PARTICIPANT"})
    public void testGetFirstExperimentPageParticipantNotFound() throws Exception {
        when(userService.getUser(userDTO.getUsername())).thenThrow(NotFoundException.class);
        mvc.perform(get(FIRST_EXPERIMENT)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(pageService, never()).computeLastExperimentPage();
        verify(userService).getUser(userDTO.getUsername());
        verify(pageService, never()).getLastExperimentPage(anyInt());
        verify(pageService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void testGetLastExperimentPage() throws Exception {
        when(pageService.computeLastExperimentPage()).thenReturn(lastPage);
        when(pageService.getExperimentPage(any(PageRequest.class))).thenReturn(experimentPage);
        mvc.perform(get(LAST_EXPERIMENT)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(EXPERIMENTS, is(experimentPage)))
                .andExpect(model().attribute(LAST_EXPERIMENT_PAGE, is(lastPage)))
                .andExpect(model().attribute(EXPERIMENT_PAGE, is(lastPage)))
                .andExpect(view().name(INDEX_EXPERIMENT));
        verify(pageService).computeLastExperimentPage();
        verify(pageService).getExperimentPage(any(PageRequest.class));
        verify(userService, never()).getUser(anyString());
    }

    @Test
    @WithMockUser(username = "participant", roles = {"PARTICIPANT"})
    public void testGetLastExperimentPageParticipant() throws Exception {
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(pageService.getLastExperimentPage(userDTO.getId())).thenReturn(lastPage);
        when(pageService.getExperimentParticipantPage(any(PageRequest.class), anyInt())).thenReturn(experimentPage);
        mvc.perform(get(LAST_EXPERIMENT)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(EXPERIMENTS, is(experimentPage)))
                .andExpect(model().attribute(LAST_EXPERIMENT_PAGE, is(lastPage)))
                .andExpect(model().attribute(EXPERIMENT_PAGE, is(lastPage)))
                .andExpect(view().name(INDEX_EXPERIMENT));
        verify(pageService, never()).computeLastExperimentPage();
        verify(userService).getUser(userDTO.getUsername());
        verify(pageService).getLastExperimentPage(userDTO.getId());
        verify(pageService).getExperimentParticipantPage(any(PageRequest.class), anyInt());
    }

    @Test
    @WithMockUser(username = "participant", roles = {"PARTICIPANT"})
    public void testGetLastExperimentPageParticipantNotFound() throws Exception {
        when(userService.getUser(userDTO.getUsername())).thenThrow(NotFoundException.class);
        mvc.perform(get(LAST_EXPERIMENT)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(pageService, never()).computeLastExperimentPage();
        verify(userService).getUser(userDTO.getUsername());
        verify(pageService, never()).getLastExperimentPage(anyInt());
        verify(pageService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
    }

    @Test
    public void testGetExperimentFinishPage() throws Exception {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        mvc.perform(get("/finish")
                        .param(EXPERIMENT, ID_STRING)
                        .param(USER, ID_STRING)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(THANKS, is(experimentDTO.getPostscript())))
                .andExpect(model().attribute(EXPERIMENT, is(ID)))
                .andExpect(model().attribute(USER, is(ID)))
                .andExpect(view().name(FINISH));
        verify(experimentService).getExperiment(ID);
    }

    @Test
    public void testGetExperimentFinishPageNotFound() throws Exception {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/finish")
                        .param(EXPERIMENT, ID_STRING)
                        .param(USER, ID_STRING)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(model().attribute(THANKS, nullValue()))
                .andExpect(view().name(Constants.ERROR));
        verify(experimentService).getExperiment(ID);
    }

    @Test
    public void testGetExperimentFinishPageInvalidExperimentId() throws Exception {
        mvc.perform(get("/finish")
                        .param(EXPERIMENT, INVALID_NUMBER)
                        .param(USER, ID_STRING)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(model().attribute(THANKS, nullValue()))
                .andExpect(view().name(Constants.ERROR));
        verify(experimentService, never()).getExperiment(anyInt());
    }

    @Test
    public void testGetExperimentFinishPageUserIdBlank() throws Exception {
        mvc.perform(get("/finish")
                        .param(EXPERIMENT, ID_STRING)
                        .param(USER, BLANK)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(model().attribute(THANKS, nullValue()))
                .andExpect(view().name(Constants.ERROR));
        verify(experimentService, never()).getExperiment(anyInt());
    }

    @Test
    public void testGetResetPage() throws Exception {
        MailServerSetter.setMailServer(true);
        mvc.perform(get("/reset")
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(PASSWORD_RESET));
    }

    @Test
    public void testGetResetPageNoMailServer() throws Exception {
        MailServerSetter.setMailServer(false);
        mvc.perform(get("/reset")
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
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

    private List<CourseTableProjection> getCourseTableProjections(int number) {
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

}
