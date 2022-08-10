package fim.unipassau.de.scratch1984.integration;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.spring.configuration.SecurityTestConfig;
import fim.unipassau.de.scratch1984.util.ApplicationProperties;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.web.controller.SecretController;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(SpringExtension.class)
@WebMvcTest(SecretController.class)
@Import(SecurityTestConfig.class)
@ActiveProfiles("test")
public class SecretControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;

    @MockBean
    private ExperimentService experimentService;

    private static final String GUI_URL = "scratch";
    private static final int ID = 1;
    private static final String ID_STRING = "1";
    private static final String SECRET = "secret";
    private static final String BLANK = " ";
    private static final String USERS = "users";
    private static final String LINK = "link";
    private static final String INACTIVE = "inactive";
    private static final String URL = ApplicationProperties.BASE_URL + ApplicationProperties.CONTEXT_PATH
            + "/users/authenticate?id=" + ID + "&secret=";
    private static final String USER_PARAM = "user";
    private static final String EXPERIMENT_PARAM = "experiment";
    private final ExperimentDTO experiment = new ExperimentDTO(ID, "experiment", "my experiment", "info", "no", true,
            GUI_URL);
    private final UserDTO user1 = new UserDTO("participant", "part@part.de", UserDTO.Role.PARTICIPANT,
            UserDTO.Language.ENGLISH, "password", SECRET);
    private final UserDTO user2 = new UserDTO("participant2", "part2@part.de", UserDTO.Role.PARTICIPANT,
            UserDTO.Language.ENGLISH, "password2", SECRET);
    private List<UserDTO> users;
    private final String TOKEN_ATTR_NAME = "org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN";
    private final HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
    private final CsrfToken csrfToken = httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());

    @BeforeEach
    public void setUp() {
        user1.setId(ID);
        user1.setSecret(SECRET);
        user2.setId(ID + 1);
        users = Arrays.asList(user1, user2);
        experiment.setActive(true);
    }

    @AfterEach
    public void resetService() {reset(userService);}

    @Test
    public void testDisplaySecret() throws Exception {
        when(experimentService.getExperiment(ID)).thenReturn(experiment);
        when(userService.getUserById(ID)).thenReturn(user1);
        mvc.perform(get("/secret")
                        .param(USER_PARAM, ID_STRING)
                        .param(EXPERIMENT_PARAM, ID_STRING)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(INACTIVE, nullValue()))
                .andExpect(model().attribute(EXPERIMENT_PARAM, is(ID)))
                .andExpect(model().attribute(USERS, contains(user1)))
                .andExpect(model().attribute(LINK, is(URL)))
                .andExpect(view().name(SECRET));
        verify(experimentService).getExperiment(ID);
        verify(userService).getUserById(ID);
    }

    @Test
    public void testDisplaySecretExperimentInactive() throws Exception {
        experiment.setActive(false);
        when(experimentService.getExperiment(ID)).thenReturn(experiment);
        mvc.perform(get("/secret")
                        .param(USER_PARAM, ID_STRING)
                        .param(EXPERIMENT_PARAM, ID_STRING)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(INACTIVE, is(true)))
                .andExpect(model().attribute(EXPERIMENT_PARAM, is(ID)))
                .andExpect(model().attribute(USERS, empty()))
                .andExpect(model().attribute(LINK, is(URL)))
                .andExpect(view().name(SECRET));
        verify(experimentService).getExperiment(ID);
        verify(userService, never()).getUserById(anyInt());
    }

    @Test
    public void testDisplaySecretNull() throws Exception {
        user1.setSecret(null);
        when(experimentService.getExperiment(ID)).thenReturn(experiment);
        when(userService.getUserById(ID)).thenReturn(user1);
        mvc.perform(get("/secret")
                        .param(USER_PARAM, ID_STRING)
                        .param(EXPERIMENT_PARAM, ID_STRING)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(experimentService).getExperiment(ID);
        verify(userService).getUserById(ID);
    }

    @Test
    public void testDisplaySecretNotFound() throws Exception {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/secret")
                        .param(USER_PARAM, ID_STRING)
                        .param(EXPERIMENT_PARAM, ID_STRING)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(experimentService).getExperiment(ID);
        verify(userService, never()).getUserById(anyInt());
    }

    @Test
    public void testDisplaySecretInvalidId() throws Exception {
        mvc.perform(get("/secret")
                        .param(USER_PARAM, ID_STRING)
                        .param(EXPERIMENT_PARAM, "-1")
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(experimentService, never()).getExperiment(anyInt());
        verify(userService, never()).getUserById(anyInt());
    }

    @Test
    public void testDisplaySecretIdBlank() throws Exception {
        mvc.perform(get("/secret")
                        .param(USER_PARAM, BLANK)
                        .param(EXPERIMENT_PARAM, ID_STRING)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(experimentService, never()).getExperiment(anyInt());
        verify(userService, never()).getUserById(anyInt());
    }

    @Test
    public void testDisplaySecrets() throws Exception {
        when(experimentService.getExperiment(ID)).thenReturn(experiment);
        when(userService.findUnfinishedUsers(ID)).thenReturn(users);
        mvc.perform(get("/secret/list")
                        .param(EXPERIMENT_PARAM, ID_STRING)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(INACTIVE, nullValue()))
                .andExpect(model().attribute(EXPERIMENT_PARAM, is(ID)))
                .andExpect(model().attribute(USERS, is(users)))
                .andExpect(model().attribute(LINK, is(URL)))
                .andExpect(view().name(SECRET));
        verify(experimentService).getExperiment(ID);
        verify(userService).findUnfinishedUsers(ID);
    }

    @Test
    public void testDisplaySecretsExperimentInactive() throws Exception {
        experiment.setActive(false);
        when(experimentService.getExperiment(ID)).thenReturn(experiment);
        mvc.perform(get("/secret/list")
                        .param(EXPERIMENT_PARAM, ID_STRING)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(INACTIVE, is(true)))
                .andExpect(model().attribute(EXPERIMENT_PARAM, is(ID)))
                .andExpect(model().attribute(USERS, empty()))
                .andExpect(model().attribute(LINK, is(URL)))
                .andExpect(view().name(SECRET));
        verify(experimentService).getExperiment(ID);
        verify(userService, never()).findUnfinishedUsers(anyInt());
    }

    @Test
    public void testDisplaySecretsNotFound() throws Exception {
        when(experimentService.getExperiment(ID)).thenReturn(experiment);
        when(userService.findUnfinishedUsers(ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/secret/list")
                        .param(EXPERIMENT_PARAM, ID_STRING)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(experimentService).getExperiment(ID);
        verify(userService).findUnfinishedUsers(ID);
    }

    @Test
    public void testDisplaySecretsInvalidId() throws Exception {
        mvc.perform(get("/secret/list")
                        .param(EXPERIMENT_PARAM, "a")
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(experimentService, never()).getExperiment(anyInt());
        verify(userService, never()).findUnfinishedUsers(anyInt());
    }

    @Test
    public void testDisplaySecretsExperimentBlank() throws Exception {
        mvc.perform(get("/secret/list")
                        .param(EXPERIMENT_PARAM, BLANK)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(experimentService, never()).getExperiment(anyInt());
        verify(userService, never()).findUnfinishedUsers(anyInt());
    }

    @Test
    public void testDownloadParticipationLinks() throws Exception {
        when(userService.findUnfinishedUsers(ID)).thenReturn(users);
        mvc.perform(get("/secret/csv")
                        .param(EXPERIMENT_PARAM, ID_STRING)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", is("attachment;filename=experiment_"
                        + ID + ".csv")));
        verify(userService, never()).getUserById(ID);
        verify(userService).findUnfinishedUsers(ID);
    }

    @Test
    public void testDownloadParticipationLinksUser() throws Exception {
        when(userService.getUserById(ID)).thenReturn(user1);
        mvc.perform(get("/secret/csv")
                        .param(EXPERIMENT_PARAM, ID_STRING)
                        .param(USER_PARAM, ID_STRING)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", is("attachment;filename=experiment_"
                        + ID + "_user_" + ID + ".csv")));
        verify(userService).getUserById(ID);
        verify(userService, never()).findUnfinishedUsers(anyInt());
    }

    @Test
    public void testDownloadParticipationLinksInvalidUserId() throws Exception {
        mvc.perform(get("/secret/csv")
                        .param(EXPERIMENT_PARAM, ID_STRING)
                        .param(USER_PARAM, "0")
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isBadRequest());
        verify(userService, never()).getUserById(anyInt());
        verify(userService, never()).findUnfinishedUsers(anyInt());
    }

    @Test
    public void testDownloadParticipationLinksInvalidExperimentId() throws Exception {
        mvc.perform(get("/secret/csv")
                        .param(EXPERIMENT_PARAM, "a")
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isBadRequest());
        verify(userService, never()).getUserById(anyInt());
        verify(userService, never()).findUnfinishedUsers(anyInt());
    }

}
