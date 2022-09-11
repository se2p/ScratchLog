package fim.unipassau.de.scratch1984.integration;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.CourseService;
import fim.unipassau.de.scratch1984.spring.configuration.SecurityTestConfig;
import fim.unipassau.de.scratch1984.web.controller.CourseController;
import fim.unipassau.de.scratch1984.web.dto.CourseDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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

    private static final String COURSE = "course";
    private static final String REDIRECT_COURSE = "redirect:/course?id=";
    private static final String COURSE_EDIT = "course-edit";
    private static final String ERROR = "redirect:/error";
    private static final String COURSE_DTO = "courseDTO";
    private static final String ID_STRING = "1";
    private static final String ID_PARAM = "id";
    private static final int ID = 1;
    private static final String TITLE = "Title";
    private static final String DESCRIPTION = "Description";
    private static final String CONTENT = "content";
    private static final LocalDateTime CHANGED = LocalDateTime.now();
    private final CourseDTO courseDTO = new CourseDTO(ID, TITLE, DESCRIPTION, CONTENT, true, CHANGED);
    private final String TOKEN_ATTR_NAME = "org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN";
    private final HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
    private final CsrfToken csrfToken = httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());

    @BeforeEach
    public void setUp() {
        courseDTO.setId(ID);
        courseDTO.setTitle(TITLE);
        courseDTO.setDescription(DESCRIPTION);
        courseDTO.setContent(CONTENT);
        courseDTO.setActive(true);
        courseDTO.setLastChanged(CHANGED);
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
                .andExpect(view().name(ERROR));
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
                .andExpect(view().name(ERROR));
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

}
