package fim.unipassau.de.scratch1984.integration;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.TokenService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.spring.configuration.SecurityTestConfig;
import fim.unipassau.de.scratch1984.web.controller.TokenController;
import fim.unipassau.de.scratch1984.web.dto.TokenDTO;
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

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
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
@WebMvcTest(TokenController.class)
@Import(SecurityTestConfig.class)
@ActiveProfiles("test")
public class TokenControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;

    @MockBean
    private TokenService tokenService;

    private static final String ERROR = "redirect:/error";
    private static final String REDIRECT_SUCCESS = "redirect:/?success=true";
    private static final String REDIRECT_ERROR = "redirect:/?error=true";
    private static final String PASSWORD_SET = "password-set";
    private static final String VALUE = "value";
    private static final String EMAIL = "admin@admin.com";
    private static final String VALID_PASSWORD = "V4l1d_P4ssw0rd!";
    private static final String BLANK = "   ";
    private static final String USER_DTO = "userDTO";
    private static final String TOKEN = "token";
    private static final int ID = 1;
    private final TokenDTO tokenDTO = new TokenDTO(TokenDTO.Type.CHANGE_EMAIL, LocalDateTime.now(), EMAIL, ID);
    private final TokenDTO registerToken = new TokenDTO(TokenDTO.Type.REGISTER, LocalDateTime.now(), null, ID);
    private final TokenDTO forgotToken = new TokenDTO(TokenDTO.Type.FORGOT_PASSWORD, LocalDateTime.now(), null, ID);
    private final UserDTO userDTO = new UserDTO("admin", "admin1@admin.de", UserDTO.Role.ADMIN,
            UserDTO.Language.ENGLISH, "admin", "secret1");
    private final String TOKEN_ATTR_NAME = "org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN";
    private final HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
    private final CsrfToken csrfToken = httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());

    @BeforeEach
    public void setup() {
        LocalDateTime expirationDate = LocalDateTime.now();
        expirationDate = expirationDate.plusHours(1);
        tokenDTO.setValue(VALUE);
        tokenDTO.setType(TokenDTO.Type.CHANGE_EMAIL);
        tokenDTO.setExpirationDate(expirationDate);
        registerToken.setValue(VALUE);
        registerToken.setExpirationDate(expirationDate);
        forgotToken.setValue(VALUE);
        forgotToken.setExpirationDate(expirationDate);
        userDTO.setId(ID);
        userDTO.setPassword(VALID_PASSWORD);
        userDTO.setConfirmPassword(VALID_PASSWORD);
    }

    @AfterEach
    public void resetService() {reset(userService, tokenService);}

    @Test
    public void testValidateToken() throws Exception {
        when(tokenService.findToken(VALUE)).thenReturn(tokenDTO);
        mvc.perform(get("/token")
                .param("value", VALUE)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_SUCCESS));
        verify(tokenService).findToken(VALUE);
        verify(userService).updateEmail(ID, EMAIL);
        verify(tokenService).deleteToken(VALUE);
    }

    @Test
    public void testValidateTokenRegister() throws Exception {
        when(tokenService.findToken(VALUE)).thenReturn(registerToken);
        when(userService.getUserById(ID)).thenReturn(userDTO);
        mvc.perform(get("/token")
                .param("value", VALUE)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(model().attribute(USER_DTO, is(userDTO)))
                .andExpect(model().attribute(TOKEN, is(VALUE)))
                .andExpect(status().isOk())
                .andExpect(view().name(PASSWORD_SET));
        verify(tokenService).findToken(VALUE);
        verify(userService).getUserById(ID);
    }

    @Test
    public void testValidateTokenForgot() throws Exception {
        when(tokenService.findToken(VALUE)).thenReturn(forgotToken);
        when(userService.getUserById(ID)).thenReturn(userDTO);
        mvc.perform(get("/token")
                .param("value", VALUE)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(model().attribute(USER_DTO, is(userDTO)))
                .andExpect(model().attribute(TOKEN, is(VALUE)))
                .andExpect(status().isOk())
                .andExpect(view().name(PASSWORD_SET));
        verify(tokenService).findToken(VALUE);
        verify(userService).getUserById(ID);
    }

    @Test
    public void testValidateTokenUserNotFound() throws Exception {
        when(tokenService.findToken(VALUE)).thenReturn(tokenDTO);
        doThrow(NotFoundException.class).when(userService).updateEmail(ID, EMAIL);
        mvc.perform(get("/token")
                .param("value", VALUE)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(tokenService).findToken(VALUE);
        verify(userService).updateEmail(ID, EMAIL);
        verify(tokenService, never()).deleteToken(VALUE);
    }

    @Test
    public void testValidateTokenExpired() throws Exception {
        tokenDTO.setExpirationDate(LocalDateTime.now());
        when(tokenService.findToken(VALUE)).thenReturn(tokenDTO);
        mvc.perform(get("/token")
                .param("value", VALUE)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_ERROR));
        verify(tokenService).findToken(VALUE);
        verify(userService, never()).updateEmail(ID, EMAIL);
        verify(tokenService, never()).deleteToken(VALUE);
    }

    @Test
    public void testValidateTokenNotFound() throws Exception {
        when(tokenService.findToken(VALUE)).thenThrow(NotFoundException.class);
        mvc.perform(get("/token")
                .param("value", VALUE)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(tokenService).findToken(VALUE);
        verify(userService, never()).updateEmail(ID, EMAIL);
        verify(tokenService, never()).deleteToken(VALUE);
    }

    @Test
    public void testRegisterUser() throws Exception {
        when(tokenService.findToken(VALUE)).thenReturn(registerToken);
        when(userService.getUserById(ID)).thenReturn(userDTO);
        mvc.perform(post("/token/password")
                .flashAttr(USER_DTO, userDTO)
                .param("value", VALUE)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_SUCCESS));
        verify(tokenService).findToken(VALUE);
        verify(userService).getUserById(ID);
        verify(userService).saveUser(userDTO);
        verify(tokenService).deleteToken(VALUE);
    }

    @Test
    public void testRegisterUserNotFound() throws Exception {
        when(tokenService.findToken(VALUE)).thenThrow(NotFoundException.class);
        mvc.perform(post("/token/password")
                .flashAttr(USER_DTO, userDTO)
                .param("value", VALUE)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(tokenService).findToken(VALUE);
        verify(userService, never()).getUserById(anyInt());
        verify(userService, never()).saveUser(any());
        verify(tokenService, never()).deleteToken(anyString());
    }

    @Test
    public void testRegisterUserInvalidPassword() throws Exception {
        userDTO.setPassword(USER_DTO);
        userDTO.setConfirmPassword(USER_DTO);
        mvc.perform(post("/token/password")
                .flashAttr(USER_DTO, userDTO)
                .param("value", VALUE)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name(PASSWORD_SET));
        verify(tokenService, never()).findToken(anyString());
        verify(userService, never()).getUserById(anyInt());
        verify(userService, never()).saveUser(any());
        verify(tokenService, never()).deleteToken(anyString());
    }
}