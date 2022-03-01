package fim.unipassau.de.scratch1984.integration;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.spring.configuration.SecurityTestConfig;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.web.controller.SecretController;
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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    private static final int ID = 1;
    private static final String ID_STRING = "1";
    private static final String SECRET = "secret";
    private static final String BLANK = " ";
    private static final String USERS = "users";
    private static final String LINK = "link";
    private static final String URL = Constants.BASE_URL + "/users/authenticate?id=" + ID + "&secret=";
    private static final String USER_PARAM = "user";
    private static final String EXPERIMENT_PARAM = "experiment";
    private UserDTO userDTO = new UserDTO("participant", "part@part.de", UserDTO.Role.PARTICIPANT,
            UserDTO.Language.ENGLISH, "password", SECRET);
    private final String TOKEN_ATTR_NAME = "org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN";
    private final HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
    private final CsrfToken csrfToken = httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());

    @BeforeEach
    public void setUp() {
        userDTO.setId(ID);
        userDTO.setSecret(SECRET);
    }

    @AfterEach
    public void resetService() {reset(userService);}

    @Test
    public void testDisplaySecret() throws Exception {
        when(userService.getUserById(ID)).thenReturn(userDTO);
        mvc.perform(get("/secret")
                        .param(USER_PARAM, ID_STRING)
                        .param(EXPERIMENT_PARAM, ID_STRING)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(EXPERIMENT_PARAM, is(ID)))
                .andExpect(model().attribute(USERS, contains(userDTO)))
                .andExpect(model().attribute(LINK, is(URL)))
                .andExpect(view().name(SECRET));
        verify(userService).getUserById(ID);
    }

    @Test
    public void testDisplaySecretNull() throws Exception {
        userDTO.setSecret(null);
        when(userService.getUserById(ID)).thenReturn(userDTO);
        mvc.perform(get("/secret")
                        .param(USER_PARAM, ID_STRING)
                        .param(EXPERIMENT_PARAM, ID_STRING)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(userService).getUserById(ID);
    }

    @Test
    public void testDisplaySecretNotFound() throws Exception {
        when(userService.getUserById(ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/secret")
                        .param(USER_PARAM, ID_STRING)
                        .param(EXPERIMENT_PARAM, ID_STRING)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(userService).getUserById(ID);
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
        verify(userService, never()).getUserById(anyInt());
    }

}
