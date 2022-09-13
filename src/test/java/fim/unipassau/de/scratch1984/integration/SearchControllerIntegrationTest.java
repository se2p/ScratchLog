package fim.unipassau.de.scratch1984.integration;

import fim.unipassau.de.scratch1984.application.service.SearchService;
import fim.unipassau.de.scratch1984.persistence.projection.CourseTableProjection;
import fim.unipassau.de.scratch1984.persistence.projection.ExperimentTableProjection;
import fim.unipassau.de.scratch1984.persistence.projection.UserProjection;
import fim.unipassau.de.scratch1984.spring.configuration.SecurityTestConfig;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.web.controller.SearchController;
import org.junit.jupiter.api.AfterEach;
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

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
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
@WebMvcTest(SearchController.class)
@Import(SecurityTestConfig.class)
@ActiveProfiles("test")
public class SearchControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private SearchService searchService;

    private static final String QUERY = "query";
    private static final String LONG_QUERY = "queeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"
            + "eeeeeeeeeeeeeeeeeeeeeeeeeery";
    private static final String BLANK = "  ";
    private static final String ADMIN = "ADMIN";
    private static final String PARTICIPANT = "PARTICIPANT";
    private static final String USERS = "users";
    private static final String EXPERIMENTS = "experiments";
    private static final String COURSES = "courses";
    private static final String USER_COUNT = "userCount";
    private static final String EXPERIMENT_COUNT = "experimentCount";
    private static final String COURSE_COUNT = "courseCount";
    private static final String LIMIT = "limit";
    private static final String SEARCH = "search";
    private static final String ERROR = "redirect:/error";
    private static final String PATH = "/search/result";
    private static final int COUNT = 25;
    private List<UserProjection> users;
    private List<ExperimentTableProjection> experiments;
    private List<CourseTableProjection> courses;
    private final String TOKEN_ATTR_NAME = "org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN";
    private final HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
    private final CsrfToken csrfToken = httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());

    @AfterEach
    public void resetService() {reset(searchService);}

    @Test
    public void testGetSearchPage() throws Exception {
        users = getUsers(Constants.PAGE_SIZE);
        experiments = getExperiments(Constants.PAGE_SIZE);
        courses = new ArrayList<>();
        when(searchService.getUserCount(QUERY)).thenReturn(COUNT);
        when(searchService.getExperimentCount(QUERY)).thenReturn(COUNT);
        when(searchService.getCourseCount(QUERY)).thenReturn(COUNT);
        when(searchService.getUserList(QUERY, Constants.PAGE_SIZE)).thenReturn(users);
        when(searchService.getExperimentList(QUERY, Constants.PAGE_SIZE)).thenReturn(experiments);
        when(searchService.getCourseList(QUERY, Constants.PAGE_SIZE)).thenReturn(courses);
        mvc.perform(get(PATH)
                .param(QUERY, QUERY)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(model().attribute(USERS, is(users)))
                .andExpect(model().attribute(EXPERIMENTS, is(experiments)))
                .andExpect(model().attribute(COURSES, is(courses)))
                .andExpect(model().attribute(USER_COUNT, is(COUNT)))
                .andExpect(model().attribute(EXPERIMENT_COUNT, is(COUNT)))
                .andExpect(model().attribute(COURSE_COUNT, is(COUNT)))
                .andExpect(model().attribute(LIMIT, is(Constants.PAGE_SIZE)))
                .andExpect(model().attribute(QUERY, is(QUERY)))
                .andExpect(status().isOk())
                .andExpect(view().name(SEARCH));
        verify(searchService).getUserCount(QUERY);
        verify(searchService).getExperimentCount(QUERY);
        verify(searchService).getCourseCount(QUERY);
        verify(searchService).getUserList(QUERY, Constants.PAGE_SIZE);
        verify(searchService).getExperimentList(QUERY, Constants.PAGE_SIZE);
        verify(searchService).getCourseList(QUERY, Constants.PAGE_SIZE);
    }

    @Test
    public void testGetSearchPageQueryBlank() throws Exception {
        mvc.perform(get(PATH)
                .param(QUERY, BLANK)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(model().attribute(USERS, is(empty())))
                .andExpect(model().attribute(EXPERIMENTS, is(empty())))
                .andExpect(model().attribute(COURSES, is(empty())))
                .andExpect(model().attribute(USER_COUNT, is(0)))
                .andExpect(model().attribute(EXPERIMENT_COUNT, is(0)))
                .andExpect(model().attribute(COURSE_COUNT, is(0)))
                .andExpect(model().attribute(LIMIT, is(10)))
                .andExpect(model().attribute(QUERY, is("")))
                .andExpect(status().isOk())
                .andExpect(view().name(SEARCH));
        verify(searchService, never()).getUserCount(anyString());
        verify(searchService, never()).getExperimentCount(anyString());
        verify(searchService, never()).getCourseCount(anyString());
        verify(searchService, never()).getUserList(anyString(), anyInt());
        verify(searchService, never()).getExperimentList(anyString(), anyInt());
        verify(searchService, never()).getCourseList(anyString(), anyInt());
    }

    @Test
    public void testGetSearchPageQueryTooLong() throws Exception {
        mvc.perform(get(PATH)
                .param(QUERY, LONG_QUERY)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(searchService, never()).getUserCount(anyString());
        verify(searchService, never()).getExperimentCount(anyString());
        verify(searchService, never()).getCourseCount(anyString());
        verify(searchService, never()).getUserList(anyString(), anyInt());
        verify(searchService, never()).getExperimentList(anyString(), anyInt());
        verify(searchService, never()).getCourseList(anyString(), anyInt());
    }

    private List<UserProjection> getUsers(int number) {
        List<UserProjection> userProjections = new ArrayList<>();

        for (int i = 0; i < number; i++) {
            int id = i + 1;

            UserProjection user = new UserProjection() {
                @Override
                public Integer getId() {
                    return id;
                }

                @Override
                public String getUsername() {
                    return "user" + id;
                }

                @Override
                public String getEmail() {
                    return "user" + id + "@user.de";
                }

                @Override
                public String getRole() {
                    return id == 1 ? ADMIN : PARTICIPANT;
                }
            };

            userProjections.add(user);
        }

        return userProjections;
    }

    private List<ExperimentTableProjection> getExperiments(int number) {
        List<ExperimentTableProjection> experiments = new ArrayList<>();

        for (int i = 0; i < number; i++) {
            int id = i + 1;

            ExperimentTableProjection experiment = new ExperimentTableProjection() {
                @Override
                public Integer getId() {
                    return id;
                }

                @Override
                public String getTitle() {
                    return "title" + id;
                }

                @Override
                public String getDescription() {
                    return "description" + id;
                }

                @Override
                public boolean isActive() {
                    return false;
                }
            };

            experiments.add(experiment);
        }

        return experiments;
    }
}
