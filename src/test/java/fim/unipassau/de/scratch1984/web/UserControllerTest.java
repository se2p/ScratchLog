package fim.unipassau.de.scratch1984.web;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.spring.authentication.CustomAuthenticationProvider;
import fim.unipassau.de.scratch1984.web.controller.UserController;
import fim.unipassau.de.scratch1984.web.dto.UserDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @Mock
    private CustomAuthenticationProvider authenticationProvider;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private ResourceBundle resourceBundle;

    @Mock
    private HttpSession session;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    private MockedStatic<SecurityContextHolder> securityContextHolder;
    private static final String USERNAME = "admin";
    private static final String LONG_USERNAME = "VeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeryLongUsername";
    private static final String BLANK = "   ";
    private static final String PASSWORD = "adminPassword";
    private static final String REDIRECT = "redirect:/";
    private static final String LOGIN = "login";
    private final UserDTO userDTO = new UserDTO(USERNAME, "admin1@admin.de", UserDTO.Role.ADMIN,
            UserDTO.Language.ENGLISH, PASSWORD, "secret1");

    @BeforeEach
    public void setup() {
        userDTO.setUsername(USERNAME);
        userDTO.setPassword(PASSWORD);
        securityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
    }

    @AfterEach
    public void cleanup() {
        securityContextHolder.close();
    }

    @Test
    public void testLoginUser() {
        userDTO.setActive(true);
        when(userService.loginUser(userDTO)).thenReturn(userDTO);
        when(httpServletRequest.getSession(false)).thenReturn(null);
        when(httpServletRequest.getSession(true)).thenReturn(session);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        assertEquals(REDIRECT, userController.loginUser(userDTO, model, httpServletRequest, bindingResult));
        verify(authenticationProvider).authenticate(any());
        verify(userService).loginUser(userDTO);
        verify(model, never()).addAttribute(anyString(), anyString());
        verify(bindingResult, never()).addError(any());
    }

    @Test
    public void testLoginUserInactive() {
        userDTO.setActive(false);
        when(userService.loginUser(userDTO)).thenReturn(userDTO);
        assertEquals(LOGIN, userController.loginUser(userDTO, model, httpServletRequest, bindingResult));
        verify(userService).loginUser(userDTO);
        verify(authenticationProvider, never()).authenticate(any());
        verify(model).addAttribute(anyString(), anyString());
        verify(bindingResult, never()).addError(any());
    }

    @Test
    public void testLoginUserNotFound() {
        when(userService.loginUser(userDTO)).thenThrow(NotFoundException.class);
        assertEquals(LOGIN, userController.loginUser(userDTO, model, httpServletRequest, bindingResult));
        verify(userService).loginUser(userDTO);
        verify(authenticationProvider, never()).authenticate(any());
        verify(model).addAttribute(anyString(), anyString());
        verify(bindingResult, never()).addError(any());
    }

    @Test
    public void testLoginUserUsernameNull() {
        userDTO.setUsername(null);
        when(bindingResult.hasErrors()).thenReturn(true);
        assertEquals(LOGIN, userController.loginUser(userDTO, model, httpServletRequest, bindingResult));
        verify(userService, never()).loginUser(userDTO);
        verify(authenticationProvider, never()).authenticate(any());
        verify(model, never()).addAttribute(anyString(), anyString());
        verify(bindingResult).addError(any());
    }

    @Test
    public void testLoginUserUsernameTooLong() {
        userDTO.setUsername(LONG_USERNAME);
        when(bindingResult.hasErrors()).thenReturn(true);
        assertEquals(LOGIN, userController.loginUser(userDTO, model, httpServletRequest, bindingResult));
        verify(userService, never()).loginUser(userDTO);
        verify(authenticationProvider, never()).authenticate(any());
        verify(model, never()).addAttribute(anyString(), anyString());
        verify(bindingResult).addError(any());
    }

    @Test
    public void testLoginUserPasswordBlank() {
        userDTO.setPassword(BLANK);
        when(bindingResult.hasErrors()).thenReturn(true);
        assertEquals(LOGIN, userController.loginUser(userDTO, model, httpServletRequest, bindingResult));
        verify(userService, never()).loginUser(userDTO);
        verify(authenticationProvider, never()).authenticate(any());
        verify(model, never()).addAttribute(anyString(), anyString());
        verify(bindingResult).addError(any());
    }

    @Test
    public void testLoginUserInvalidUsernameAndPassword() {
        userDTO.setUsername(null);
        userDTO.setPassword(null);
        when(bindingResult.hasErrors()).thenReturn(true);
        assertEquals(LOGIN, userController.loginUser(userDTO, model, httpServletRequest, bindingResult));
        verify(userService, never()).loginUser(userDTO);
        verify(authenticationProvider, never()).authenticate(any());
        verify(model, never()).addAttribute(anyString(), anyString());
        verify(bindingResult, times(2)).addError(any());
    }

    @Test
    public void testLogoutUser() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(httpServletRequest.getSession(false)).thenReturn(session);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.existsUser(USERNAME)).thenReturn(true);
        assertEquals(REDIRECT, userController.logoutUser(httpServletRequest));
        verify(userService).existsUser(USERNAME);
        verify(session).invalidate();
    }

    @Test
    public void testLogoutUserNotFound() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        assertThrows(NotFoundException.class, () -> userController.logoutUser(httpServletRequest));
        verify(userService).existsUser(USERNAME);
    }

    @Test
    public void testLogoutUserAuthenticationNameNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        assertThrows(IllegalStateException.class, () -> userController.logoutUser(httpServletRequest));
        verify(userService, never()).existsUser(anyString());
    }

    @Test
    public void testLogoutUserAuthenticationNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        assertThrows(IllegalStateException.class, () -> userController.logoutUser(httpServletRequest));
        verify(userService, never()).existsUser(anyString());
    }
}
