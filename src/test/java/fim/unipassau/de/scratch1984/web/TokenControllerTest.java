package fim.unipassau.de.scratch1984.web;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.TokenService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.web.controller.TokenController;
import fim.unipassau.de.scratch1984.web.dto.TokenDTO;
import fim.unipassau.de.scratch1984.web.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TokenControllerTest {

    @InjectMocks
    private TokenController tokenController;

    @Mock
    private TokenService tokenService;

    @Mock
    private UserService userService;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private Model model;

    private static final String ERROR = "redirect:/error";
    private static final String REDIRECT_SUCCESS = "redirect:/?success=true";
    private static final String REDIRECT_ERROR = "redirect:/?error=true";
    private static final String PASSWORD_SET = "password-set";
    private static final String VALUE = "value";
    private static final String EMAIL = "admin@admin.com";
    private static final String VALID_PASSWORD = "V4l1d_P4ssw0rd!";
    private static final String BLANK = "   ";
    private static final String TOKEN = "token";
    private static final int ID = 1;
    private final TokenDTO tokenDTO = new TokenDTO(TokenDTO.Type.CHANGE_EMAIL, LocalDateTime.now(), EMAIL, ID);
    private final TokenDTO registerToken = new TokenDTO(TokenDTO.Type.REGISTER, LocalDateTime.now(), null, ID);
    private final TokenDTO forgotToken = new TokenDTO(TokenDTO.Type.FORGOT_PASSWORD, LocalDateTime.now(), null, ID);
    private final UserDTO userDTO = new UserDTO("admin", "admin1@admin.de", UserDTO.Role.ADMIN,
            UserDTO.Language.ENGLISH, "admin", "secret1");

    @BeforeEach
    public void setup() {
        LocalDateTime expirationDate = LocalDateTime.now();
        expirationDate = expirationDate.plusHours(1);
        tokenDTO.setValue(VALUE);
        tokenDTO.setType(TokenDTO.Type.CHANGE_EMAIL);
        tokenDTO.setExpirationDate(expirationDate);
        registerToken.setValue(VALUE);
        registerToken.setExpirationDate(expirationDate);
        forgotToken.setExpirationDate(expirationDate);
        userDTO.setId(ID);
        userDTO.setPassword(VALID_PASSWORD);
        userDTO.setConfirmPassword(null);
    }

    @Test
    public void testValidateToken() {
        when(tokenService.findToken(VALUE)).thenReturn(tokenDTO);
        assertEquals(REDIRECT_SUCCESS, tokenController.validateToken(VALUE, model));
        verify(tokenService).findToken(VALUE);
        verify(userService).updateEmail(ID, EMAIL);
        verify(tokenService).deleteToken(VALUE);
    }

    @Test
    public void testValidateTokenRegister() {
        when(tokenService.findToken(VALUE)).thenReturn(registerToken);
        when(userService.getUserById(ID)).thenReturn(userDTO);
        assertEquals(PASSWORD_SET, tokenController.validateToken(VALUE, model));
        verify(tokenService).findToken(VALUE);
        verify(userService).getUserById(ID);
        verify(model, times(2)).addAttribute(anyString(), any());
    }

    @Test
    public void testValidateTokenForgotPassword() {
        when(tokenService.findToken(VALUE)).thenReturn(forgotToken);
        when(userService.getUserById(ID)).thenReturn(userDTO);
        assertEquals(PASSWORD_SET, tokenController.validateToken(VALUE, model));
        verify(tokenService).findToken(VALUE);
        verify(userService).getUserById(ID);
        verify(model, times(2)).addAttribute(anyString(), any());
    }

    @Test
    public void testValidateTokenUserNotFound() {
        when(tokenService.findToken(VALUE)).thenReturn(tokenDTO);
        doThrow(NotFoundException.class).when(userService).updateEmail(ID, EMAIL);
        assertEquals(ERROR, tokenController.validateToken(VALUE, model));
        verify(tokenService).findToken(VALUE);
        verify(userService).updateEmail(ID, EMAIL);
        verify(tokenService, never()).deleteToken(VALUE);
    }

    @Test
    public void testValidateTokenExpired() {
        tokenDTO.setExpirationDate(LocalDateTime.now());
        when(tokenService.findToken(VALUE)).thenReturn(tokenDTO);
        assertEquals(REDIRECT_ERROR, tokenController.validateToken(VALUE, model));
        verify(tokenService).findToken(VALUE);
        verify(userService, never()).updateEmail(ID, EMAIL);
        verify(tokenService, never()).deleteToken(VALUE);
    }

    @Test
    public void testValidateTokenNotFound() {
        when(tokenService.findToken(VALUE)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, tokenController.validateToken(VALUE, model));
        verify(tokenService).findToken(VALUE);
        verify(userService, never()).updateEmail(ID, EMAIL);
        verify(tokenService, never()).deleteToken(VALUE);
    }

    @Test
    public void testRegisterUser() {
        userDTO.setPassword(VALID_PASSWORD);
        userDTO.setConfirmPassword(VALID_PASSWORD);
        when(tokenService.findToken(VALUE)).thenReturn(registerToken);
        when(userService.getUserById(ID)).thenReturn(userDTO);
        assertEquals(REDIRECT_SUCCESS, tokenController.registerUser(userDTO, VALUE, bindingResult, model));
        verify(bindingResult, never()).addError(any());
        verify(model, never()).addAttribute(anyString(), any());
        verify(tokenService).findToken(VALUE);
        verify(userService).getUserById(ID);
        verify(userService).saveUser(userDTO);
        verify(tokenService).deleteToken(VALUE);
    }

    @Test
    public void testRegisterUserUserNotFound() {
        userDTO.setPassword(VALID_PASSWORD);
        userDTO.setConfirmPassword(VALID_PASSWORD);
        when(tokenService.findToken(VALUE)).thenReturn(registerToken);
        when(userService.getUserById(ID)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, tokenController.registerUser(userDTO, VALUE, bindingResult, model));
        verify(bindingResult, never()).addError(any());
        verify(model, never()).addAttribute(anyString(), any());
        verify(tokenService).findToken(VALUE);
        verify(userService).getUserById(ID);
        verify(userService, never()).saveUser(any());
        verify(tokenService, never()).deleteToken(anyString());
    }

    @Test
    public void testRegisterUserTokenNotFound() {
        userDTO.setPassword(VALID_PASSWORD);
        userDTO.setConfirmPassword(VALID_PASSWORD);
        when(tokenService.findToken(VALUE)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, tokenController.registerUser(userDTO, VALUE, bindingResult, model));
        verify(bindingResult, never()).addError(any());
        verify(model, never()).addAttribute(anyString(), any());
        verify(tokenService).findToken(VALUE);
        verify(userService, never()).getUserById(anyInt());
        verify(userService, never()).saveUser(any());
        verify(tokenService, never()).deleteToken(anyString());
    }

    @Test
    public void testRegisterUserPasswordsNotMatching() {
        userDTO.setPassword(VALID_PASSWORD);
        userDTO.setConfirmPassword("bla");
        assertEquals(PASSWORD_SET, tokenController.registerUser(userDTO, VALUE, bindingResult, model));
        verify(bindingResult).addError(any());
        verify(model).addAttribute(TOKEN, VALUE);
        verify(tokenService, never()).findToken(anyString());
        verify(userService, never()).getUserById(anyInt());
        verify(userService, never()).saveUser(any());
        verify(tokenService, never()).deleteToken(anyString());
    }

    @Test
    public void testRegisterUserTokenNull() {
        userDTO.setPassword(VALID_PASSWORD);
        userDTO.setConfirmPassword(VALID_PASSWORD);
        assertEquals(ERROR, tokenController.registerUser(userDTO, null, bindingResult, model));
        verify(bindingResult, never()).addError(any());
        verify(model, never()).addAttribute(anyString(), any());
        verify(tokenService, never()).findToken(anyString());
        verify(userService, never()).getUserById(anyInt());
        verify(userService, never()).saveUser(any());
        verify(tokenService, never()).deleteToken(anyString());
    }

    @Test
    public void testRegisterUserTokenBlank() {
        userDTO.setPassword(VALID_PASSWORD);
        userDTO.setConfirmPassword(VALID_PASSWORD);
        assertEquals(ERROR, tokenController.registerUser(userDTO, BLANK, bindingResult, model));
        verify(bindingResult, never()).addError(any());
        verify(model, never()).addAttribute(anyString(), any());
        verify(tokenService, never()).findToken(anyString());
        verify(userService, never()).getUserById(anyInt());
        verify(userService, never()).saveUser(any());
        verify(tokenService, never()).deleteToken(anyString());
    }

    @Test
    public void testRegisterUserConfirmPasswordNull() {
        assertEquals(ERROR, tokenController.registerUser(userDTO, VALUE, bindingResult, model));
        verify(bindingResult, never()).addError(any());
        verify(model, never()).addAttribute(anyString(), any());
        verify(tokenService, never()).findToken(anyString());
        verify(userService, never()).getUserById(anyInt());
        verify(userService, never()).saveUser(any());
        verify(tokenService, never()).deleteToken(anyString());
    }

    @Test
    public void testRegisterUserPasswordNull() {
        userDTO.setPassword(null);
        userDTO.setConfirmPassword(VALID_PASSWORD);
        assertEquals(ERROR, tokenController.registerUser(userDTO, VALUE, bindingResult, model));
        verify(bindingResult, never()).addError(any());
        verify(model, never()).addAttribute(anyString(), any());
        verify(tokenService, never()).findToken(anyString());
        verify(userService, never()).getUserById(anyInt());
        verify(userService, never()).saveUser(any());
        verify(tokenService, never()).deleteToken(anyString());
    }

    @Test
    public void testRegisterUserIdNull() {
        userDTO.setId(null);
        userDTO.setConfirmPassword(VALID_PASSWORD);
        assertEquals(ERROR, tokenController.registerUser(userDTO, VALUE, bindingResult, model));
        verify(bindingResult, never()).addError(any());
        verify(model, never()).addAttribute(anyString(), any());
        verify(tokenService, never()).findToken(anyString());
        verify(userService, never()).getUserById(anyInt());
        verify(userService, never()).saveUser(any());
        verify(tokenService, never()).deleteToken(anyString());
    }

    @Test
    public void testRegisterUserUserNull() {
        assertEquals(ERROR, tokenController.registerUser(null, VALUE, bindingResult, model));
        verify(bindingResult, never()).addError(any());
        verify(model, never()).addAttribute(anyString(), any());
        verify(tokenService, never()).findToken(anyString());
        verify(userService, never()).getUserById(anyInt());
        verify(userService, never()).saveUser(any());
        verify(tokenService, never()).deleteToken(anyString());
    }
}
