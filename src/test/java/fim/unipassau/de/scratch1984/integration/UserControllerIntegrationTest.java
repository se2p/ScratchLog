package fim.unipassau.de.scratch1984.integration;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.spring.authentication.CustomAuthenticationProvider;
import fim.unipassau.de.scratch1984.spring.configuration.SecurityTestConfig;
import fim.unipassau.de.scratch1984.web.controller.UserController;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
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
@WebMvcTest(UserController.class)
@Import(SecurityTestConfig.class)
@ActiveProfiles("test")
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;

    @MockBean
    private CustomAuthenticationProvider authenticationProvider;

    private static final String USERNAME = "admin";
    private static final String PASSWORD = "adminPassword";
    private static final String REDIRECT = "redirect:/";
    private static final String LOGIN = "login";
    private static final String ERROR = "redirect:/error";
    private static final String PROFILE = "profile";
    private static final String USER_DTO = "userDTO";
    private static final String NAME = "name";
    private final UserDTO userDTO = new UserDTO(USERNAME, "admin1@admin.de", UserDTO.Role.ADMIN,
            UserDTO.Language.ENGLISH, PASSWORD, "secret1");
    private final String TOKEN_ATTR_NAME = "org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN";
    private final HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
    private final CsrfToken csrfToken = httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());

    @BeforeEach
    public void setup() {
        userDTO.setActive(true);
        userDTO.setPassword(PASSWORD);
        userDTO.setUsername(USERNAME);
    }

    @AfterEach
    public void resetService() {reset(userService);}

    @Test
    public void testLoginUser() throws Exception {
        when(userService.loginUser(any())).thenReturn(userDTO);
        mvc.perform(post("/users/login")
                .flashAttr(USER_DTO, userDTO)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT));
        verify(userService).loginUser(any());
    }

    @Test
    public void testLoginUserInactive() throws Exception {
        userDTO.setActive(false);
        when(userService.loginUser(any())).thenReturn(userDTO);
        mvc.perform(post("/users/login")
                .flashAttr(USER_DTO, userDTO)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name(LOGIN));
        verify(userService).loginUser(any());
    }

    @Test
    public void testLoginUserNotFound() throws Exception {
        when(userService.loginUser(any())).thenThrow(NotFoundException.class);
        mvc.perform(post("/users/login")
                .flashAttr(USER_DTO, userDTO)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name(LOGIN));
        verify(userService).loginUser(any());
    }

    @Test
    public void testLoginUserInvalidInput() throws Exception {
        userDTO.setUsername(null);
        userDTO.setPassword(null);
        mvc.perform(post("/users/login")
                .flashAttr(USER_DTO, userDTO)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name(LOGIN));
        verify(userService, never()).loginUser(any());
    }

    @Test
    public void testLogoutUser() throws Exception {
        when(userService.existsUser(anyString())).thenReturn(true);
        mvc.perform(post("/users/logout")
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT));
        verify(userService).existsUser(anyString());
    }

    @Test
    public void testLogoutUserNonExistent() throws Exception {
        mvc.perform(post("/users/logout")
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService).existsUser(anyString());
    }

    @Test
    public void testGetProfile() throws Exception {
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        mvc.perform(get("/users/profile")
                .param(NAME, USERNAME)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(model().attribute(USER_DTO, allOf(
                        hasProperty("id", is(userDTO.getId())),
                        hasProperty("username", is(USERNAME))
                )))
                .andExpect(view().name(PROFILE));
        verify(userService).getUser(USERNAME);
    }

    @Test
    public void testGetProfileNotFound() throws Exception {
        when(userService.getUser(USERNAME)).thenThrow(NotFoundException.class);
        mvc.perform(get("/users/profile")
                .param(NAME, USERNAME)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService).getUser(USERNAME);
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"ADMIN"})
    public void testGetProfileOwnProfile() throws Exception {
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        mvc.perform(get("/users/profile")
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(model().attribute(USER_DTO, allOf(
                        hasProperty("id", is(userDTO.getId())),
                        hasProperty("username", is(USERNAME))
                )))
                .andExpect(view().name(PROFILE));
        verify(userService).getUser(USERNAME);
    }
}
