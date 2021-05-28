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
import static org.hamcrest.Matchers.nullValue;
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
    private static final String NEW_USERNAME = "admin1";
    private static final String PASSWORD = "adminPassword";
    private static final String VALID_PASSWORD = "V4l1d_P4ssw0rd!";
    private static final String EMAIL = "admin1@admin.de";
    private static final String NEW_EMAIL = "admin@admin.com";
    private static final String REDIRECT = "redirect:/";
    private static final String LOGIN = "login";
    private static final String ERROR = "redirect:/error";
    private static final String PROFILE = "profile";
    private static final String PROFILE_EDIT = "profile-edit";
    private static final String PROFILE_REDIRECT = "redirect:/users/profile?name=";
    private static final String USER_DTO = "userDTO";
    private static final String NAME = "name";
    private static final int ID = 1;
    private final UserDTO userDTO = new UserDTO(USERNAME, EMAIL, UserDTO.Role.ADMIN, UserDTO.Language.ENGLISH,
            PASSWORD, "secret1");
    private final UserDTO oldDTO = new UserDTO(USERNAME, EMAIL, UserDTO.Role.ADMIN, UserDTO.Language.ENGLISH, PASSWORD,
            "secret1");
    private final String TOKEN_ATTR_NAME = "org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN";
    private final HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
    private final CsrfToken csrfToken = httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());

    @BeforeEach
    public void setup() {
        oldDTO.setId(ID);
        oldDTO.setActive(true);
        oldDTO.setPassword(PASSWORD);
        oldDTO.setUsername(USERNAME);
        oldDTO.setEmail(EMAIL);
        userDTO.setId(ID);
        userDTO.setActive(true);
        userDTO.setPassword(PASSWORD);
        userDTO.setUsername(USERNAME);
        userDTO.setEmail(EMAIL);
        userDTO.setNewPassword("");
        userDTO.setConfirmPassword("");
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

    @Test
    public void testGetEditProfileForm() throws Exception {
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        mvc.perform(get("/users/edit")
                .param(NAME, USERNAME)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(model().attribute(USER_DTO, allOf(
                        hasProperty("id", is(userDTO.getId())),
                        hasProperty("username", is(USERNAME)),
                        hasProperty("email", is(EMAIL))
                )))
                .andExpect(view().name(PROFILE_EDIT));
        verify(userService).getUser(USERNAME);
    }

    @Test
    public void testGetEditProfileFormNotFound() throws Exception {
        when(userService.getUser(USERNAME)).thenThrow(NotFoundException.class);
        mvc.perform(get("/users/edit")
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
    public void testGetEditProfileFormUsernameNull() throws Exception {
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        mvc.perform(get("/users/edit")
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(model().attribute(USER_DTO, allOf(
                        hasProperty("id", is(userDTO.getId())),
                        hasProperty("username", is(USERNAME)),
                        hasProperty("email", is(EMAIL))
                )))
                .andExpect(view().name(PROFILE_EDIT));
        verify(userService).getUser(USERNAME);
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"ADMIN"})
    public void testGetEditProfileFormUsernameBlank() throws Exception {
        userDTO.setEmail(null);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        mvc.perform(get("/users/edit")
                .param(NAME, "  ")
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(model().attribute(USER_DTO, allOf(
                        hasProperty("id", is(userDTO.getId())),
                        hasProperty("username", is(USERNAME)),
                        hasProperty("email", nullValue())
                )))
                .andExpect(view().name(PROFILE_EDIT));
        verify(userService).getUser(USERNAME);
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"ADMIN"})
    public void testGetEditProfileFormUsernameNotFound() throws Exception {
        when(userService.getUser(USERNAME)).thenThrow(NotFoundException.class);
        mvc.perform(get("/users/edit")
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService).getUser(USERNAME);
    }

    @Test
    public void testUpdateUser() throws Exception {
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(userService.updateUser(oldDTO)).thenReturn(oldDTO);
        mvc.perform(post("/users/update")
                .flashAttr(USER_DTO, userDTO)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(PROFILE_REDIRECT + USERNAME));
        verify(userService, never()).existsUser(anyString());
        verify(userService, never()).existsEmail(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(userService, never()).encodePassword(anyString());
        verify(userService).updateUser(oldDTO);
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"ADMIN"})
    public void testUpdateUserChangeUsernameOwnProfile() throws Exception {
        userDTO.setUsername(NEW_USERNAME);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(userService.updateUser(oldDTO)).thenReturn(oldDTO);
        mvc.perform(post("/users/update")
                .flashAttr(USER_DTO, userDTO)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(PROFILE_REDIRECT + NEW_USERNAME));
        verify(userService).existsUser(NEW_USERNAME);
        verify(userService, never()).existsEmail(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(userService, never()).encodePassword(anyString());
        verify(authenticationProvider).authenticate(any());
        verify(userService).updateUser(userDTO);
    }

    @Test
    public void testUpdateUserChangeEmailAndPassword() throws Exception {
        userDTO.setNewPassword(VALID_PASSWORD);
        userDTO.setConfirmPassword(VALID_PASSWORD);
        userDTO.setEmail(NEW_EMAIL);
        when(userService.matchesPassword(PASSWORD, PASSWORD)).thenReturn(true);
        when(userService.encodePassword(VALID_PASSWORD)).thenReturn(VALID_PASSWORD);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(userService.updateUser(oldDTO)).thenReturn(oldDTO);
        mvc.perform(post("/users/update")
                .flashAttr(USER_DTO, userDTO)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(PROFILE_REDIRECT + USERNAME));
        verify(userService, never()).existsUser(NEW_USERNAME);
        verify(userService).existsEmail(anyString());
        verify(userService).matchesPassword(anyString(), anyString());
        verify(userService).encodePassword(anyString());
        verify(authenticationProvider, never()).authenticate(any());
        verify(userService).updateUser(oldDTO);
    }

    @Test
    public void testUpdateUserNewEmailExistsUsernameExistsPasswordsNotMatching() throws Exception {
        userDTO.setNewPassword(VALID_PASSWORD);
        userDTO.setConfirmPassword(VALID_PASSWORD);
        userDTO.setUsername(NEW_USERNAME);
        userDTO.setEmail(NEW_EMAIL);
        when(userService.existsUser(NEW_USERNAME)).thenReturn(true);
        when(userService.existsEmail(NEW_EMAIL)).thenReturn(true);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        mvc.perform(post("/users/update")
                .flashAttr(USER_DTO, userDTO)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name(PROFILE_EDIT));
        verify(userService).existsUser(NEW_USERNAME);
        verify(userService).existsEmail(NEW_EMAIL);
        verify(userService).matchesPassword(PASSWORD, PASSWORD);
        verify(userService, never()).encodePassword(anyString());
        verify(authenticationProvider, never()).authenticate(any());
        verify(userService, never()).updateUser(any());
    }

    @Test
    public void testUpdateUserChangePasswordInvalidNoInputPassword() throws Exception {
        userDTO.setPassword("");
        userDTO.setNewPassword(PASSWORD);
        userDTO.setConfirmPassword(PASSWORD);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        mvc.perform(post("/users/update")
                .flashAttr(USER_DTO, userDTO)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name(PROFILE_EDIT));
        verify(userService, never()).existsUser(anyString());
        verify(userService, never()).existsEmail(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(userService, never()).encodePassword(anyString());
        verify(authenticationProvider, never()).authenticate(any());
        verify(userService, never()).updateUser(any());
    }

    @Test
    public void testUpdateUserChangeEmailInvalid() throws Exception {
        userDTO.setEmail(PASSWORD);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        mvc.perform(post("/users/update")
                .flashAttr(USER_DTO, userDTO)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name(PROFILE_EDIT));
        verify(userService, never()).existsUser(anyString());
        verify(userService, never()).existsEmail(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(userService, never()).encodePassword(anyString());
        verify(authenticationProvider, never()).authenticate(any());
        verify(userService, never()).updateUser(any());
    }

    @Test
    public void testUpdateUserChangeUsernameInvalid() throws Exception {
        userDTO.setUsername("");
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        mvc.perform(post("/users/update")
                .flashAttr(USER_DTO, userDTO)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name(PROFILE_EDIT));
        verify(userService, never()).existsUser(anyString());
        verify(userService, never()).existsEmail(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(userService, never()).encodePassword(anyString());
        verify(authenticationProvider, never()).authenticate(any());
        verify(userService, never()).updateUser(any());
    }

    @Test
    public void testUpdateUserNewEmailBlank() throws Exception {
        userDTO.setEmail("");
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        mvc.perform(post("/users/update")
                .flashAttr(USER_DTO, userDTO)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name(PROFILE_EDIT));
        verify(userService, never()).existsUser(anyString());
        verify(userService, never()).existsEmail(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(userService, never()).encodePassword(anyString());
        verify(authenticationProvider, never()).authenticate(any());
        verify(userService, never()).updateUser(any());
    }

    @Test
    public void testUpdateUserNotFound() throws Exception {
        when(userService.getUserById(ID)).thenThrow(NotFoundException.class);
        mvc.perform(post("/users/update")
                .flashAttr(USER_DTO, userDTO)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService, never()).existsUser(anyString());
        verify(userService, never()).existsEmail(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(userService, never()).encodePassword(anyString());
        verify(authenticationProvider, never()).authenticate(any());
        verify(userService, never()).updateUser(any());
    }
}
