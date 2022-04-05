package fim.unipassau.de.scratch1984.web;

import fim.unipassau.de.scratch1984.MailServerSetter;
import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.MailService;
import fim.unipassau.de.scratch1984.application.service.ParticipantService;
import fim.unipassau.de.scratch1984.application.service.TokenService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.spring.authentication.CustomAuthenticationProvider;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.web.controller.UserController;
import fim.unipassau.de.scratch1984.web.dto.PasswordDTO;
import fim.unipassau.de.scratch1984.web.dto.TokenDTO;
import fim.unipassau.de.scratch1984.web.dto.UserBulkDTO;
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
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
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
    private ParticipantService participantService;

    @Mock
    private MailService mailService;

    @Mock
    private TokenService tokenService;

    @Mock
    private CustomAuthenticationProvider authenticationProvider;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

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

    @Mock
    private LocaleResolver localeResolver;

    private MockedStatic<SecurityContextHolder> securityContextHolder;
    private static final String USERNAME = "admin";
    private static final String NEW_USERNAME = "admin1";
    private static final String LONG_USERNAME = "VeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeryLongUsername";
    private static final String LONG_EMAIL = "Veeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"
            + "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeryLongEmail";
    private static final String BLANK = "   ";
    private static final String PASSWORD = "adminPassword";
    private static final String VALID_PASSWORD = "V4l1d_P4ssw0rd!";
    private static final String EMAIL = "admin1@admin.de";
    private static final String NEW_EMAIL = "admin@admin.com";
    private static final String REDIRECT = "redirect:/";
    private static final String LOGIN = "login";
    private static final String PROFILE = "profile";
    private static final String PROFILE_EDIT = "profile-edit";
    private static final String PROFILE_REDIRECT = "redirect:/users/profile?name=";
    private static final String EMAIL_REDIRECT = "redirect:/users/profile?update=true&name=";
    private static final String REDIRECT_SUCCESS = "redirect:/?success=true";
    private static final String REDIRECT_EXPERIMENT = "redirect:/experiment?id=";
    private static final String LAST_ADMIN = "redirect:/users/profile?lastAdmin=true";
    private static final String INVALID = "redirect:/users/profile?invalid=true&name=";
    private static final String USER = "user";
    private static final String PASSWORD_PAGE = "password";
    private static final String PASSWORD_RESET = "password-reset";
    private static final String PARTICIPANTS_ADD = "participants-add";
    private static final String USER_DTO = "userDTO";
    private static final String ID_STRING = "1";
    private static final String SECRET = "secret";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final int ID = 1;
    private static final int AMOUNT = 5;
    private final UserDTO userDTO = new UserDTO(USERNAME, EMAIL, UserDTO.Role.ADMIN, UserDTO.Language.ENGLISH,
            PASSWORD, SECRET);
    private final UserDTO oldDTO = new UserDTO(USERNAME, EMAIL, UserDTO.Role.ADMIN, UserDTO.Language.ENGLISH,
            PASSWORD, SECRET);
    private final UserBulkDTO userBulkDTO = new UserBulkDTO(AMOUNT, UserDTO.Language.ENGLISH, USERNAME, false);
    private final TokenDTO tokenDTO = new TokenDTO(TokenDTO.Type.CHANGE_EMAIL, LocalDateTime.now(), NEW_EMAIL, ID);
    private final PasswordDTO passwordDTO = new PasswordDTO(PASSWORD);

    @BeforeEach
    public void setup() {
        oldDTO.setId(ID);
        oldDTO.setUsername(USERNAME);
        oldDTO.setPassword(PASSWORD);
        oldDTO.setEmail(EMAIL);
        userDTO.setId(ID);
        userDTO.setUsername(USERNAME);
        userDTO.setPassword(PASSWORD);
        userDTO.setEmail(EMAIL);
        userDTO.setRole(UserDTO.Role.ADMIN);
        userDTO.setNewPassword("");
        userDTO.setConfirmPassword("");
        userDTO.setActive(true);
        userDTO.setSecret(SECRET);
        userDTO.setAttempts(0);
        userBulkDTO.setAmount(AMOUNT);
        userBulkDTO.setLanguage(UserDTO.Language.ENGLISH);
        userBulkDTO.setUsername(USERNAME);
        userBulkDTO.setStartAtOne(false);
        passwordDTO.setPassword(PASSWORD);
        securityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
    }

    @AfterEach
    public void cleanup() {
        securityContextHolder.close();
    }

    @Test
    public void testAuthenticateUser() {
        when(userService.authenticateUser(SECRET)).thenReturn(userDTO);
        when(userService.existsParticipant(userDTO.getId(), ID)).thenReturn(true);
        when(httpServletRequest.getSession(false)).thenReturn(null);
        when(httpServletRequest.getSession(true)).thenReturn(session);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        assertEquals(REDIRECT_EXPERIMENT + ID, userController.authenticateUser(ID_STRING, SECRET, httpServletRequest,
                httpServletResponse));
        verify(authenticationProvider).authenticate(any());
        verify(userService).authenticateUser(SECRET);
        verify(userService).existsParticipant(userDTO.getId(), ID);
        verify(localeResolver).setLocale(any(), any(), any());
    }

    @Test
    public void testAuthenticateUserNoParticipant() {
        when(userService.authenticateUser(SECRET)).thenReturn(userDTO);
        assertEquals(Constants.ERROR, userController.authenticateUser(ID_STRING, SECRET, httpServletRequest,
                httpServletResponse));
        verify(authenticationProvider, never()).authenticate(any());
        verify(userService).authenticateUser(SECRET);
        verify(userService).existsParticipant(userDTO.getId(), ID);
        verify(localeResolver, never()).setLocale(any(), any(), any());
    }

    @Test
    public void testAuthenticateUserNotFound() {
        when(userService.authenticateUser(SECRET)).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, userController.authenticateUser(ID_STRING, SECRET, httpServletRequest,
                httpServletResponse));
        verify(authenticationProvider, never()).authenticate(any());
        verify(userService).authenticateUser(SECRET);
        verify(userService, never()).existsParticipant(userDTO.getId(), ID);
        verify(localeResolver, never()).setLocale(any(), any(), any());
    }

    @Test
    public void testAuthenticateUserInvalidId() {
        assertEquals(Constants.ERROR, userController.authenticateUser("0", SECRET, httpServletRequest,
                httpServletResponse));
        verify(authenticationProvider, never()).authenticate(any());
        verify(userService, never()).authenticateUser(SECRET);
        verify(userService, never()).existsParticipant(userDTO.getId(), ID);
        verify(localeResolver, never()).setLocale(any(), any(), any());
    }

    @Test
    public void testAuthenticateUserIdNull() {
        assertEquals(Constants.ERROR, userController.authenticateUser(null, SECRET, httpServletRequest,
                httpServletResponse));
        verify(authenticationProvider, never()).authenticate(any());
        verify(userService, never()).authenticateUser(SECRET);
        verify(userService, never()).existsParticipant(userDTO.getId(), ID);
        verify(localeResolver, never()).setLocale(any(), any(), any());
    }

    @Test
    public void testAuthenticateUserIdBlank() {
        assertEquals(Constants.ERROR, userController.authenticateUser(BLANK, SECRET, httpServletRequest,
                httpServletResponse));
        verify(authenticationProvider, never()).authenticate(any());
        verify(userService, never()).authenticateUser(SECRET);
        verify(userService, never()).existsParticipant(userDTO.getId(), ID);
        verify(localeResolver, never()).setLocale(any(), any(), any());
    }

    @Test
    public void testAuthenticateUserSecretNull() {
        assertEquals(Constants.ERROR, userController.authenticateUser(ID_STRING, null, httpServletRequest,
                httpServletResponse));
        verify(authenticationProvider, never()).authenticate(any());
        verify(userService, never()).authenticateUser(SECRET);
        verify(userService, never()).existsParticipant(userDTO.getId(), ID);
        verify(localeResolver, never()).setLocale(any(), any(), any());
    }

    @Test
    public void testAuthenticateUserSecretBlank() {
        assertEquals(Constants.ERROR, userController.authenticateUser(ID_STRING, BLANK, httpServletRequest,
                httpServletResponse));
        verify(authenticationProvider, never()).authenticate(any());
        verify(userService, never()).authenticateUser(SECRET);
        verify(userService, never()).existsParticipant(userDTO.getId(), ID);
        verify(localeResolver, never()).setLocale(any(), any(), any());
    }

    @Test
    public void testLoginUser() {
        userDTO.setActive(true);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(userService.loginUser(userDTO)).thenReturn(true);
        when(httpServletRequest.getSession(false)).thenReturn(null);
        when(httpServletRequest.getSession(true)).thenReturn(session);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        assertEquals(REDIRECT, userController.loginUser(userDTO, model, httpServletRequest, httpServletResponse,
                bindingResult));
        verify(userService).getUser(USERNAME);
        verify(userService, never()).updateUser(any());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(authenticationProvider).authenticate(any());
        verify(userService).loginUser(userDTO);
        verify(model, never()).addAttribute(anyString(), anyString());
        verify(bindingResult, never()).addError(any());
    }

    @Test
    public void testLoginUserFalse() {
        userDTO.setActive(true);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        assertEquals(LOGIN, userController.loginUser(userDTO, model, httpServletRequest, httpServletResponse,
                bindingResult));
        verify(userService).getUser(USERNAME);
        verify(userService, never()).updateUser(any());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(authenticationProvider, never()).authenticate(any());
        verify(userService).loginUser(userDTO);
        verify(model).addAttribute(anyString(), anyString());
        verify(bindingResult, never()).addError(any());
    }

    @Test
    public void testLoginUserMaxAttempts() {
        userDTO.setActive(true);
        userDTO.setAttempts(3);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        assertEquals(LOGIN, userController.loginUser(userDTO, model, httpServletRequest, httpServletResponse,
                bindingResult));
        verify(userService).getUser(USERNAME);
        verify(userService).updateUser(userDTO);
        verify(tokenService).generateToken(TokenDTO.Type.DEACTIVATED, "", userDTO.getId());
        verify(authenticationProvider, never()).authenticate(any());
        verify(userService, never()).loginUser(any());
        verify(model).addAttribute(anyString(), anyString());
        verify(bindingResult, never()).addError(any());
    }

    @Test
    public void testLoginUserInactive() {
        userDTO.setActive(false);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        assertEquals(LOGIN, userController.loginUser(userDTO, model, httpServletRequest, httpServletResponse,
                bindingResult));
        verify(userService).getUser(USERNAME);
        verify(userService, never()).updateUser(any());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(userService, never()).loginUser(any());
        verify(authenticationProvider, never()).authenticate(any());
        verify(model).addAttribute(anyString(), anyString());
        verify(bindingResult, never()).addError(any());
    }

    @Test
    public void testLoginUserNotFound() {
        when(userService.getUser(userDTO.getUsername())).thenThrow(NotFoundException.class);
        assertEquals(LOGIN, userController.loginUser(userDTO, model, httpServletRequest, httpServletResponse,
                bindingResult));
        verify(userService).getUser(USERNAME);
        verify(userService, never()).updateUser(any());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(userService, never()).loginUser(any());
        verify(authenticationProvider, never()).authenticate(any());
        verify(model).addAttribute(anyString(), anyString());
        verify(bindingResult, never()).addError(any());
    }

    @Test
    public void testLoginUserUsernameNull() {
        userDTO.setUsername(null);
        when(bindingResult.hasErrors()).thenReturn(true);
        assertEquals(LOGIN, userController.loginUser(userDTO, model, httpServletRequest, httpServletResponse,
                bindingResult));
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).updateUser(any());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(userService, never()).loginUser(userDTO);
        verify(authenticationProvider, never()).authenticate(any());
        verify(model, never()).addAttribute(anyString(), anyString());
        verify(bindingResult).addError(any());
    }

    @Test
    public void testLoginUserUsernameTooLong() {
        userDTO.setUsername(LONG_USERNAME);
        when(bindingResult.hasErrors()).thenReturn(true);
        assertEquals(LOGIN, userController.loginUser(userDTO, model, httpServletRequest, httpServletResponse,
                bindingResult));
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).updateUser(any());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(userService, never()).loginUser(userDTO);
        verify(authenticationProvider, never()).authenticate(any());
        verify(model, never()).addAttribute(anyString(), anyString());
        verify(bindingResult).addError(any());
    }

    @Test
    public void testLoginUserPasswordBlank() {
        userDTO.setPassword(BLANK);
        when(bindingResult.hasErrors()).thenReturn(true);
        assertEquals(LOGIN, userController.loginUser(userDTO, model, httpServletRequest, httpServletResponse,
                bindingResult));
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).updateUser(any());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
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
        assertEquals(LOGIN, userController.loginUser(userDTO, model, httpServletRequest, httpServletResponse,
                bindingResult));
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).updateUser(any());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
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
        assertEquals(Constants.ERROR, userController.logoutUser(httpServletRequest));
        verify(userService).existsUser(USERNAME);
    }

    @Test
    public void testLogoutUserAuthenticationNameNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        assertEquals(Constants.ERROR, userController.logoutUser(httpServletRequest));
        verify(userService, never()).existsUser(anyString());
    }

    @Test
    public void testLogoutUserAuthenticationNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        assertEquals(Constants.ERROR, userController.logoutUser(httpServletRequest));
        verify(userService, never()).existsUser(anyString());
    }

    @Test
    public void testGetAddUser() {
        assertEquals(USER, userController.getAddUser(userDTO));
    }

    @Test
    public void testAddUser() {
        MailServerSetter.setMailServer(true);
        userDTO.setId(null);
        when(userService.saveUser(userDTO)).thenReturn(oldDTO);
        when(tokenService.generateToken(TokenDTO.Type.REGISTER, null, oldDTO.getId())).thenReturn(tokenDTO);
        when(mailService.sendEmail(anyString(), anyString(), any(), anyString())).thenReturn(true);
        assertEquals(REDIRECT_SUCCESS, userController.addUser(userDTO, bindingResult));
        verify(bindingResult, never()).addError(any());
        verify(userService).existsEmail(userDTO.getEmail());
        verify(userService).existsUser(userDTO.getUsername());
        verify(userService).saveUser(userDTO);
        verify(tokenService).generateToken(TokenDTO.Type.REGISTER, null, oldDTO.getId());
        verify(mailService).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testAddUserMailNotSent() {
        MailServerSetter.setMailServer(true);
        userDTO.setId(null);
        when(userService.saveUser(userDTO)).thenReturn(oldDTO);
        when(tokenService.generateToken(TokenDTO.Type.REGISTER, null, oldDTO.getId())).thenReturn(tokenDTO);
        assertEquals(Constants.ERROR, userController.addUser(userDTO, bindingResult));
        verify(bindingResult, never()).addError(any());
        verify(userService).existsEmail(userDTO.getEmail());
        verify(userService).existsUser(userDTO.getUsername());
        verify(userService).saveUser(userDTO);
        verify(tokenService).generateToken(TokenDTO.Type.REGISTER, null, oldDTO.getId());
        verify(mailService).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testAddUserNoMailServer() {
        MailServerSetter.setMailServer(false);
        userDTO.setId(null);
        when(userService.saveUser(userDTO)).thenReturn(oldDTO);
        assertEquals(PROFILE_REDIRECT + oldDTO.getUsername(), userController.addUser(userDTO, bindingResult));
        verify(bindingResult, never()).addError(any());
        verify(userService).existsEmail(userDTO.getEmail());
        verify(userService).existsUser(userDTO.getUsername());
        verify(userService).saveUser(userDTO);
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testAddUserExistsUser() {
        userDTO.setId(null);
        when(userService.existsUser(userDTO.getUsername())).thenReturn(true);
        when(bindingResult.hasErrors()).thenReturn(true);
        assertEquals(USER, userController.addUser(userDTO, bindingResult));
        verify(bindingResult).addError(any());
        verify(userService).existsEmail(userDTO.getEmail());
        verify(userService).existsUser(userDTO.getUsername());
        verify(userService, never()).saveUser(any());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testAddUserInvalidUsername() {
        userDTO.setId(null);
        userDTO.setUsername(BLANK);
        when(bindingResult.hasErrors()).thenReturn(true);
        assertEquals(USER, userController.addUser(userDTO, bindingResult));
        verify(bindingResult).addError(any());
        verify(userService).existsEmail(userDTO.getEmail());
        verify(userService, never()).existsUser(anyString());
        verify(userService, never()).saveUser(any());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testAddUserEmailExists() {
        userDTO.setId(null);
        when(userService.existsEmail(userDTO.getEmail())).thenReturn(true);
        when(bindingResult.hasErrors()).thenReturn(true);
        assertEquals(USER, userController.addUser(userDTO, bindingResult));
        verify(bindingResult).addError(any());
        verify(userService).existsEmail(userDTO.getEmail());
        verify(userService).existsUser(userDTO.getUsername());
        verify(userService, never()).saveUser(any());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testAddUserEmailInvalid() {
        userDTO.setId(null);
        userDTO.setPassword(VALID_PASSWORD);
        userDTO.setConfirmPassword(VALID_PASSWORD);
        userDTO.setEmail(USERNAME);
        when(bindingResult.hasErrors()).thenReturn(true);
        assertEquals(USER, userController.addUser(userDTO, bindingResult));
        verify(bindingResult).addError(any());
        verify(userService, never()).existsEmail(anyString());
        verify(userService).existsUser(userDTO.getUsername());
        verify(userService, never()).saveUser(any());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testAddUserEmailNull() {
        userDTO.setId(null);
        userDTO.setEmail(null);
        assertEquals(Constants.ERROR, userController.addUser(userDTO, bindingResult));
        verify(bindingResult, never()).addError(any());
        verify(userService, never()).existsEmail(anyString());
        verify(userService, never()).existsUser(anyString());
        verify(userService, never()).saveUser(any());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testAddUserRoleNull() {
        userDTO.setId(null);
        userDTO.setRole(null);
        assertEquals(Constants.ERROR, userController.addUser(userDTO, bindingResult));
        verify(bindingResult, never()).addError(any());
        verify(userService, never()).existsEmail(anyString());
        verify(userService, never()).existsUser(anyString());
        verify(userService, never()).saveUser(any());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testAddUserLanguageNull() {
        userDTO.setId(null);
        userDTO.setLanguage(null);
        assertEquals(Constants.ERROR, userController.addUser(userDTO, bindingResult));
        verify(bindingResult, never()).addError(any());
        verify(userService, never()).existsEmail(anyString());
        verify(userService, never()).existsUser(anyString());
        verify(userService, never()).saveUser(any());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testAddUserIdNotNull() {
        assertEquals(Constants.ERROR, userController.addUser(userDTO, bindingResult));
        verify(bindingResult, never()).addError(any());
        verify(userService, never()).existsEmail(anyString());
        verify(userService, never()).existsUser(anyString());
        verify(userService, never()).saveUser(any());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testGetAddParticipants() {
        MailServerSetter.setMailServer(false);
        assertEquals(PARTICIPANTS_ADD, userController.getAddParticipants(userBulkDTO));
    }

    @Test
    public void testGetAddParticipantsMailServer() {
        MailServerSetter.setMailServer(true);
        assertEquals(REDIRECT, userController.getAddParticipants(userBulkDTO));
    }

    @Test
    public void testAddParticipants() {
        when(userService.findLastId()).thenReturn(AMOUNT);
        assertEquals(REDIRECT_SUCCESS, userController.addParticipants(userBulkDTO, bindingResult, model));
        verify(bindingResult, never()).addError(any());
        verify(userService).findLastId();
        verify(userService, times(AMOUNT)).existsUser(anyString());
        verify(userService, times(AMOUNT)).saveUser(any());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testAddParticipantsStartOneUsernameExists() {
        List<String> existingNames = List.of("admin1");
        userBulkDTO.setStartAtOne(true);
        when(userService.existsUser(existingNames.get(0))).thenReturn(true);
        assertEquals(PARTICIPANTS_ADD, userController.addParticipants(userBulkDTO, bindingResult, model));
        verify(bindingResult, never()).addError(any());
        verify(userService, never()).findLastId();
        verify(userService, times(AMOUNT)).existsUser(anyString());
        verify(userService, times(AMOUNT - existingNames.size())).saveUser(any());
        verify(model).addAttribute("error", existingNames);
    }

    @Test
    public void testAddParticipantsInvalidUsername() {
        userBulkDTO.setUsername(BLANK);
        assertEquals(PARTICIPANTS_ADD, userController.addParticipants(userBulkDTO, bindingResult, model));
        verify(bindingResult).addError(any());
        verify(userService, never()).findLastId();
        verify(userService, never()).existsUser(anyString());
        verify(userService, never()).saveUser(any());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testAddParticipantsAmountBiggerMax() {
        userBulkDTO.setAmount(Constants.MAX_ADD_PARTICIPANTS + 1);
        assertEquals(Constants.ERROR, userController.addParticipants(userBulkDTO, bindingResult, model));
        verify(bindingResult, never()).addError(any());
        verify(userService, never()).findLastId();
        verify(userService, never()).existsUser(anyString());
        verify(userService, never()).saveUser(any());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testAddParticipantsAmountTooSmall() {
        userBulkDTO.setAmount(0);
        assertEquals(Constants.ERROR, userController.addParticipants(userBulkDTO, bindingResult, model));
        verify(bindingResult, never()).addError(any());
        verify(userService, never()).findLastId();
        verify(userService, never()).existsUser(anyString());
        verify(userService, never()).saveUser(any());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testAddParticipantsLanguageNull() {
        userBulkDTO.setLanguage(null);
        assertEquals(Constants.ERROR, userController.addParticipants(userBulkDTO, bindingResult, model));
        verify(bindingResult, never()).addError(any());
        verify(userService, never()).findLastId();
        verify(userService, never()).existsUser(anyString());
        verify(userService, never()).saveUser(any());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testAddParticipantsUsernameNull() {
        userBulkDTO.setUsername(null);
        assertEquals(Constants.ERROR, userController.addParticipants(userBulkDTO, bindingResult, model));
        verify(bindingResult, never()).addError(any());
        verify(userService, never()).findLastId();
        verify(userService, never()).existsUser(anyString());
        verify(userService, never()).saveUser(any());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testPasswordReset() {
        MailServerSetter.setMailServer(true);
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(userService.getUserByEmail(userDTO.getEmail())).thenReturn(userDTO);
        when(tokenService.generateToken(TokenDTO.Type.FORGOT_PASSWORD, null, userDTO.getId())).thenReturn(tokenDTO);
        when(mailService.sendEmail(anyString(), anyString(), any(), anyString())).thenReturn(true);
        assertEquals(REDIRECT_SUCCESS, userController.passwordReset(userDTO, model));
        verify(userService).getUser(userDTO.getUsername());
        verify(userService).getUserByEmail(userDTO.getEmail());
        verify(tokenService).generateToken(TokenDTO.Type.FORGOT_PASSWORD, null, userDTO.getId());
        verify(mailService).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testPasswordResetMailNotSent() {
        MailServerSetter.setMailServer(true);
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(userService.getUserByEmail(userDTO.getEmail())).thenReturn(userDTO);
        when(tokenService.generateToken(TokenDTO.Type.FORGOT_PASSWORD, null, userDTO.getId())).thenReturn(tokenDTO);
        assertEquals(Constants.ERROR, userController.passwordReset(userDTO, model));
        verify(userService).getUser(userDTO.getUsername());
        verify(userService).getUserByEmail(userDTO.getEmail());
        verify(tokenService).generateToken(TokenDTO.Type.FORGOT_PASSWORD, null, userDTO.getId());
        verify(mailService).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testPasswordResetUsersNotEqual() {
        MailServerSetter.setMailServer(true);
        oldDTO.setId(ID + 1);
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(userService.getUserByEmail(userDTO.getEmail())).thenReturn(oldDTO);
        assertEquals(Constants.ERROR, userController.passwordReset(userDTO, model));
        verify(userService).getUser(userDTO.getUsername());
        verify(userService).getUserByEmail(userDTO.getEmail());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testPasswordResetEmailNotFound() {
        MailServerSetter.setMailServer(true);
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(userService.getUserByEmail(userDTO.getEmail())).thenThrow(NotFoundException.class);
        assertEquals(PASSWORD_RESET, userController.passwordReset(userDTO, model));
        verify(userService).getUser(userDTO.getUsername());
        verify(userService).getUserByEmail(userDTO.getEmail());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model).addAttribute(anyString(), any());
    }

    @Test
    public void testPasswordResetNoMailServer() {
        MailServerSetter.setMailServer(false);
        assertEquals(Constants.ERROR, userController.passwordReset(userDTO, model));
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).getUserByEmail(anyString());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testPasswordResetLongUsername() {
        MailServerSetter.setMailServer(true);
        userDTO.setUsername(LONG_USERNAME);
        assertEquals(Constants.ERROR, userController.passwordReset(userDTO, model));
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).getUserByEmail(anyString());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testPasswordResetLongEmail() {
        MailServerSetter.setMailServer(true);
        userDTO.setEmail(LONG_EMAIL);
        assertEquals(Constants.ERROR, userController.passwordReset(userDTO, model));
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).getUserByEmail(anyString());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testPasswordResetEmailBlank() {
        MailServerSetter.setMailServer(true);
        userDTO.setEmail(BLANK);
        assertEquals(Constants.ERROR, userController.passwordReset(userDTO, model));
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).getUserByEmail(anyString());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testPasswordResetUsernameBlank() {
        MailServerSetter.setMailServer(true);
        userDTO.setUsername(BLANK);
        assertEquals(Constants.ERROR, userController.passwordReset(userDTO, model));
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).getUserByEmail(anyString());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testPasswordResetEmailNull() {
        MailServerSetter.setMailServer(true);
        userDTO.setEmail(null);
        assertEquals(Constants.ERROR, userController.passwordReset(userDTO, model));
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).getUserByEmail(anyString());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testPasswordResetUsernameNull() {
        MailServerSetter.setMailServer(true);
        userDTO.setUsername(null);
        assertEquals(Constants.ERROR, userController.passwordReset(userDTO, model));
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).getUserByEmail(anyString());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetProfileUserAdmin() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(httpServletRequest.isUserInRole("ROLE_ADMIN")).thenReturn(true);
        assertEquals(PROFILE, userController.getProfile(USERNAME, model, httpServletRequest));
        verify(userService).getUser(USERNAME);
        verify(participantService, never()).getExperimentInfoForParticipant(anyInt());
        verify(authentication).getName();
        verify(model).addAttribute(USER_DTO, userDTO);
    }

    @Test
    public void testGetProfileParticipant() {
        HashMap<Integer, String> experiments = new HashMap<>();
        experiments.put(ID, "Title");
        userDTO.setRole(UserDTO.Role.PARTICIPANT);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(participantService.getExperimentInfoForParticipant(userDTO.getId())).thenReturn(experiments);
        when(httpServletRequest.isUserInRole("ROLE_ADMIN")).thenReturn(true);
        assertEquals(PROFILE, userController.getProfile(USERNAME, model, httpServletRequest));
        verify(userService).getUser(USERNAME);
        verify(participantService).getExperimentInfoForParticipant(userDTO.getId());
        verify(authentication).getName();
        verify(model).addAttribute(USER_DTO, userDTO);
        verify(model).addAttribute("experiments", experiments);
    }

    @Test
    public void testGetProfileNotFound() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenThrow(NotFoundException.class);
        when(httpServletRequest.isUserInRole("ROLE_ADMIN")).thenReturn(true);
        assertEquals(Constants.ERROR, userController.getProfile(USERNAME, model, httpServletRequest));
        verify(userService).getUser(USERNAME);
        verify(participantService, never()).getExperimentInfoForParticipant(anyInt());
        verify(authentication).getName();
        verify(model, never()).addAttribute(USER_DTO, userDTO);
    }

    @Test
    public void testGetProfileOwnProfile() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        assertEquals(PROFILE, userController.getProfile(null, model, httpServletRequest));
        verify(userService).getUser(USERNAME);
        verify(participantService, never()).getExperimentInfoForParticipant(anyInt());
        verify(authentication, times(2)).getName();
        verify(model).addAttribute(USER_DTO, userDTO);
    }

    @Test
    public void testGetProfileOwnProfileBlank() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        assertEquals(PROFILE, userController.getProfile(BLANK, model, httpServletRequest));
        verify(userService).getUser(USERNAME);
        verify(participantService, never()).getExperimentInfoForParticipant(anyInt());
        verify(authentication, times(2)).getName();
        verify(model).addAttribute(USER_DTO, userDTO);
    }

    @Test
    public void testGetProfileOwnProfileNotFound() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, userController.getProfile(null, model, httpServletRequest));
        verify(userService).getUser(USERNAME);
        verify(participantService, never()).getExperimentInfoForParticipant(anyInt());
        verify(authentication, times(2)).getName();
        verify(httpServletRequest).getSession(false);
        verify(model, never()).addAttribute(USER_DTO, userDTO);
    }

    @Test
    public void testGetProfileAuthenticationNameNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        assertEquals(Constants.ERROR, userController.getProfile(USERNAME, model, httpServletRequest));
        verify(authentication).getName();
        verify(userService, never()).getUser(USERNAME);
        verify(model, never()).addAttribute(USER_DTO, userDTO);
    }

    @Test
    public void testGetProfileAuthenticationNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        assertEquals(Constants.ERROR, userController.getProfile(USERNAME, model, httpServletRequest));
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(USERNAME);
        verify(model, never()).addAttribute(USER_DTO, userDTO);
    }

    @Test
    public void testGetEditProfileForm() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(httpServletRequest.isUserInRole("ROLE_ADMIN")).thenReturn(true);
        assertEquals(PROFILE_EDIT, userController.getEditProfileForm(USERNAME, model, httpServletRequest));
        verify(authentication).getName();
        verify(userService).getUser(USERNAME);
        verify(model).addAttribute(USER_DTO, userDTO);
    }

    @Test
    public void testGetEditProfileFormNotFound() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenThrow(NotFoundException.class);
        when(httpServletRequest.isUserInRole("ROLE_ADMIN")).thenReturn(true);
        assertEquals(Constants.ERROR, userController.getEditProfileForm(USERNAME, model, httpServletRequest));
        verify(authentication).getName();
        verify(userService).getUser(USERNAME);
        verify(model, never()).addAttribute(USER_DTO, userDTO);
    }

    @Test
    public void testGetEditProfileFormUsernameNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        assertEquals(PROFILE_EDIT, userController.getEditProfileForm(null, model, httpServletRequest));
        verify(authentication, times(2)).getName();
        verify(userService).getUser(USERNAME);
        verify(model).addAttribute(USER_DTO, userDTO);
    }

    @Test
    public void testGetEditProfileFormUsernameBlank() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        assertEquals(PROFILE_EDIT, userController.getEditProfileForm(BLANK, model, httpServletRequest));
        verify(authentication, times(2)).getName();
        verify(userService).getUser(USERNAME);
        verify(model).addAttribute(USER_DTO, userDTO);
    }

    @Test
    public void testGetEditProfileFormUsernameBlankNotFound() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, userController.getEditProfileForm(BLANK, model, httpServletRequest));
        verify(authentication, times(2)).getName();
        verify(userService).getUser(USERNAME);
        verify(model, never()).addAttribute(USER_DTO, userDTO);
    }

    @Test
    public void testGetEditProfileFormAuthenticationNameNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        assertEquals(Constants.ERROR, userController.getEditProfileForm(BLANK, model, httpServletRequest));
        verify(authentication).getName();
        verify(userService, never()).getUser(USERNAME);
        verify(model, never()).addAttribute(USER_DTO, userDTO);
    }

    @Test
    public void testGetEditProfileFormAuthenticationNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        assertEquals(Constants.ERROR, userController.getEditProfileForm(BLANK, model, httpServletRequest));
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(USERNAME);
        verify(model, never()).addAttribute(USER_DTO, userDTO);
    }

    @Test
    public void testUpdateUser() {
        userDTO.setNewPassword(null);
        userDTO.setConfirmPassword(null);
        userDTO.setEmail(BLANK);
        oldDTO.setEmail(null);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userService.updateUser(oldDTO)).thenReturn(oldDTO);
        when(httpServletRequest.isUserInRole("ROLE_ADMIN")).thenReturn(true);
        assertEquals(PROFILE_REDIRECT + USERNAME, userController.updateUser(userDTO, bindingResult,
                httpServletRequest, httpServletResponse));
        verify(bindingResult, never()).addError(any());
        verify(userService).getUserById(ID);
        verify(authentication).getName();
        verify(userService).updateUser(oldDTO);
        verify(httpServletRequest, never()).getSession(false);
    }

    @Test
    public void testUpdateUserOwnProfile() {
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.updateUser(oldDTO)).thenReturn(oldDTO);
        when(httpServletRequest.getSession(false)).thenReturn(session);
        when(httpServletRequest.getSession(true)).thenReturn(session);
        when(httpServletRequest.isUserInRole("ROLE_ADMIN")).thenReturn(true);
        assertEquals(PROFILE_REDIRECT + USERNAME, userController.updateUser(userDTO, bindingResult,
                httpServletRequest, httpServletResponse));
        verify(bindingResult, never()).addError(any());
        verify(userService).getUserById(ID);
        verify(authentication).getName();
        verify(userService).updateUser(oldDTO);
        verify(localeResolver).setLocale(httpServletRequest, httpServletResponse, Locale.ENGLISH);
        verify(httpServletRequest).getSession(false);
        verify(httpServletRequest).getSession(true);
    }

    @Test
    public void testUpdateUserParticipant() {
        userDTO.setUsername(null);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.updateUser(oldDTO)).thenReturn(oldDTO);
        when(httpServletRequest.getSession(false)).thenReturn(session);
        when(httpServletRequest.getSession(true)).thenReturn(session);
        assertEquals(PROFILE_REDIRECT + USERNAME, userController.updateUser(userDTO, bindingResult,
                httpServletRequest, httpServletResponse));
        verify(bindingResult, never()).addError(any());
        verify(userService).getUserById(ID);
        verify(authentication).getName();
        verify(userService).updateUser(oldDTO);
        verify(localeResolver).setLocale(httpServletRequest, httpServletResponse, Locale.ENGLISH);
        verify(httpServletRequest).getSession(false);
        verify(httpServletRequest).getSession(true);
    }

    @Test
    public void testUpdateUserParticipantUsernameNotNull() {
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        assertEquals(Constants.ERROR, userController.updateUser(userDTO, bindingResult, httpServletRequest, httpServletResponse));
        verify(bindingResult, never()).addError(any());
        verify(userService).getUserById(ID);
        verify(authentication, never()).getName();
        verify(userService, never()).updateUser(oldDTO);
        verify(localeResolver, never()).setLocale(httpServletRequest, httpServletResponse, Locale.ENGLISH);
        verify(httpServletRequest, never()).getSession(false);
        verify(httpServletRequest, never()).getSession(true);
    }

    @Test
    public void testUpdateUserParticipantUsersNotEqual() {
        oldDTO.setId(ID + 1);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        assertEquals(Constants.ERROR, userController.updateUser(userDTO, bindingResult, httpServletRequest, httpServletResponse));
        verify(bindingResult, never()).addError(any());
        verify(userService).getUserById(ID);
        verify(authentication, never()).getName();
        verify(userService, never()).updateUser(oldDTO);
        verify(localeResolver, never()).setLocale(httpServletRequest, httpServletResponse, Locale.ENGLISH);
        verify(httpServletRequest, never()).getSession(false);
        verify(httpServletRequest, never()).getSession(true);
    }

    @Test
    public void testUpdateUserChangePassword() {
        userDTO.setNewPassword(VALID_PASSWORD);
        userDTO.setConfirmPassword(VALID_PASSWORD);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userService.updateUser(oldDTO)).thenReturn(oldDTO);
        when(userService.matchesPassword(PASSWORD, PASSWORD)).thenReturn(true);
        when(httpServletRequest.isUserInRole("ROLE_ADMIN")).thenReturn(true);
        assertEquals(PROFILE_REDIRECT + USERNAME, userController.updateUser(userDTO, bindingResult,
                httpServletRequest, httpServletResponse));
        verify(bindingResult, never()).addError(any());
        verify(userService).getUserById(ID);
        verify(authentication).getName();
        verify(userService).updateUser(oldDTO);
        verify(userService).matchesPassword(PASSWORD, PASSWORD);
        verify(userService).encodePassword(VALID_PASSWORD);
        verify(httpServletRequest, never()).getSession(false);
    }

    @Test
    public void testUpdateUserChangeEmail() {
        MailServerSetter.setMailServer(true);
        userDTO.setEmail(NEW_EMAIL);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userService.updateUser(oldDTO)).thenReturn(oldDTO);
        when(mailService.sendEmail(anyString(), any(), any(), anyString())).thenReturn(true);
        when(tokenService.generateToken(TokenDTO.Type.CHANGE_EMAIL, NEW_EMAIL, ID)).thenReturn(tokenDTO);
        when(httpServletRequest.isUserInRole("ROLE_ADMIN")).thenReturn(true);
        assertEquals(EMAIL_REDIRECT + USERNAME, userController.updateUser(userDTO, bindingResult,
                httpServletRequest, httpServletResponse));
        verify(bindingResult, never()).addError(any());
        verify(userService).getUserById(ID);
        verify(authentication).getName();
        verify(userService).updateUser(oldDTO);
        verify(userService).existsEmail(NEW_EMAIL);
        verify(tokenService).generateToken(TokenDTO.Type.CHANGE_EMAIL, NEW_EMAIL, ID);
        verify(mailService).sendEmail(anyString(), any(), any(), anyString());
        verify(httpServletRequest, never()).getSession(false);
    }

    @Test
    public void testUpdateUserChangeEmailFalse() {
        MailServerSetter.setMailServer(true);
        userDTO.setEmail(NEW_EMAIL);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userService.updateUser(oldDTO)).thenReturn(oldDTO);
        when(tokenService.generateToken(TokenDTO.Type.CHANGE_EMAIL, NEW_EMAIL, ID)).thenReturn(tokenDTO);
        when(httpServletRequest.isUserInRole("ROLE_ADMIN")).thenReturn(true);
        assertEquals(PROFILE_REDIRECT + USERNAME, userController.updateUser(userDTO, bindingResult,
                httpServletRequest, httpServletResponse));
        verify(bindingResult, never()).addError(any());
        verify(userService).getUserById(ID);
        verify(authentication).getName();
        verify(userService).updateUser(oldDTO);
        verify(userService).existsEmail(NEW_EMAIL);
        verify(tokenService).generateToken(TokenDTO.Type.CHANGE_EMAIL, NEW_EMAIL, ID);
        verify(httpServletRequest, never()).getSession(false);
    }

    @Test
    public void testUpdateUserChangeEmailTokenNotFound() {
        MailServerSetter.setMailServer(true);
        userDTO.setEmail(NEW_EMAIL);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userService.updateUser(oldDTO)).thenReturn(oldDTO);
        when(tokenService.generateToken(TokenDTO.Type.CHANGE_EMAIL, NEW_EMAIL, ID)).thenThrow(NotFoundException.class);
        when(httpServletRequest.isUserInRole("ROLE_ADMIN")).thenReturn(true);
        assertEquals(PROFILE_REDIRECT + USERNAME, userController.updateUser(userDTO, bindingResult,
                httpServletRequest, httpServletResponse));
        verify(bindingResult, never()).addError(any());
        verify(userService).getUserById(ID);
        verify(authentication).getName();
        verify(userService).updateUser(oldDTO);
        verify(userService).existsEmail(NEW_EMAIL);
        verify(tokenService).generateToken(TokenDTO.Type.CHANGE_EMAIL, NEW_EMAIL, ID);
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
        verify(httpServletRequest, never()).getSession(false);
    }

    @Test
    public void testUpdateUserChangeEmailNoMailServer() {
        MailServerSetter.setMailServer(false);
        userDTO.setEmail(NEW_EMAIL);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userService.updateUser(oldDTO)).thenReturn(oldDTO);
        when(httpServletRequest.isUserInRole("ROLE_ADMIN")).thenReturn(true);
        assertEquals(PROFILE_REDIRECT + USERNAME, userController.updateUser(userDTO, bindingResult,
                httpServletRequest, httpServletResponse));
        verify(bindingResult, never()).addError(any());
        verify(userService).getUserById(ID);
        verify(authentication).getName();
        verify(userService).updateUser(oldDTO);
        verify(userService).existsEmail(NEW_EMAIL);
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
        verify(httpServletRequest, never()).getSession(false);
    }

    @Test
    public void testUpdateUserChangeUsername() {
        userDTO.setUsername(NEW_USERNAME);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userService.updateUser(oldDTO)).thenReturn(oldDTO);
        when(httpServletRequest.isUserInRole("ROLE_ADMIN")).thenReturn(true);
        assertEquals(PROFILE_REDIRECT + NEW_USERNAME, userController.updateUser(userDTO, bindingResult,
                httpServletRequest, httpServletResponse));
        verify(bindingResult, never()).addError(any());
        verify(userService).getUserById(ID);
        verify(authentication).getName();
        verify(userService).updateUser(oldDTO);
        verify(userService).existsUser(NEW_USERNAME);
        verify(httpServletRequest, never()).getSession(false);
    }

    @Test
    public void testUpdateUserChangePasswordInvalidNotMatching() {
        userDTO.setPassword(VALID_PASSWORD);
        userDTO.setNewPassword(PASSWORD);
        userDTO.setConfirmPassword(PASSWORD);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(httpServletRequest.isUserInRole("ROLE_ADMIN")).thenReturn(true);
        assertEquals(PROFILE_EDIT, userController.updateUser(userDTO, bindingResult, httpServletRequest,
                httpServletResponse));
        verify(bindingResult, times(2)).addError(any());
        verify(userService).getUserById(ID);
        verify(localeResolver, never()).setLocale(httpServletRequest, httpServletResponse, Locale.ENGLISH);
        verify(httpServletRequest, never()).getSession(false);
    }

    @Test
    public void testUpdateUserChangePasswordInputNull() {
        userDTO.setPassword(null);
        userDTO.setNewPassword(VALID_PASSWORD);
        userDTO.setConfirmPassword(VALID_PASSWORD);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(httpServletRequest.isUserInRole("ROLE_ADMIN")).thenReturn(true);
        assertEquals(PROFILE_EDIT, userController.updateUser(userDTO, bindingResult, httpServletRequest,
                httpServletResponse));
        verify(bindingResult).addError(any());
        verify(userService).getUserById(ID);
        verify(localeResolver, never()).setLocale(httpServletRequest, httpServletResponse, Locale.ENGLISH);
        verify(httpServletRequest, never()).getSession(false);
    }

    @Test
    public void testUpdateUserChangePasswordInputBlank() {
        userDTO.setPassword(BLANK);
        userDTO.setNewPassword(VALID_PASSWORD);
        userDTO.setConfirmPassword(VALID_PASSWORD);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(httpServletRequest.isUserInRole("ROLE_ADMIN")).thenReturn(true);
        assertEquals(PROFILE_EDIT, userController.updateUser(userDTO, bindingResult, httpServletRequest,
                httpServletResponse));
        verify(bindingResult).addError(any());
        verify(userService).getUserById(ID);
        verify(localeResolver, never()).setLocale(httpServletRequest, httpServletResponse, Locale.ENGLISH);
        verify(httpServletRequest, never()).getSession(false);
    }

    @Test
    public void testUpdateUserChangeEmailInvalid() {
        userDTO.setEmail(PASSWORD);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(httpServletRequest.isUserInRole("ROLE_ADMIN")).thenReturn(true);
        assertEquals(PROFILE_EDIT, userController.updateUser(userDTO, bindingResult, httpServletRequest,
                httpServletResponse));
        verify(bindingResult).addError(any());
        verify(userService).getUserById(ID);
        verify(userService, never()).existsEmail(anyString());
        verify(localeResolver, never()).setLocale(httpServletRequest, httpServletResponse, Locale.ENGLISH);
        verify(httpServletRequest, never()).getSession(false);
    }

    @Test
    public void testUpdateUserChangeEmailExists() {
        userDTO.setEmail(NEW_EMAIL);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(userService.existsEmail(NEW_EMAIL)).thenReturn(true);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(httpServletRequest.isUserInRole("ROLE_ADMIN")).thenReturn(true);
        assertEquals(PROFILE_EDIT, userController.updateUser(userDTO, bindingResult, httpServletRequest,
                httpServletResponse));
        verify(bindingResult).addError(any());
        verify(userService).getUserById(ID);
        verify(userService).existsEmail(NEW_EMAIL);
        verify(localeResolver, never()).setLocale(httpServletRequest, httpServletResponse, Locale.ENGLISH);
        verify(httpServletRequest, never()).getSession(false);
    }

    @Test
    public void testUpdateUserChangeUsernameInvalid() {
        userDTO.setUsername(BLANK);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(httpServletRequest.isUserInRole("ROLE_ADMIN")).thenReturn(true);
        assertEquals(PROFILE_EDIT, userController.updateUser(userDTO, bindingResult, httpServletRequest,
                httpServletResponse));
        verify(bindingResult).addError(any());
        verify(userService).getUserById(ID);
        verify(userService, never()).existsUser(anyString());
        verify(localeResolver, never()).setLocale(httpServletRequest, httpServletResponse, Locale.ENGLISH);
        verify(httpServletRequest, never()).getSession(false);
    }

    @Test
    public void testUpdateUserChangeUsernameExists() {
        userDTO.setUsername(NEW_USERNAME);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(userService.existsUser(NEW_USERNAME)).thenReturn(true);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(httpServletRequest.isUserInRole("ROLE_ADMIN")).thenReturn(true);
        assertEquals(PROFILE_EDIT, userController.updateUser(userDTO, bindingResult, httpServletRequest,
                httpServletResponse));
        verify(bindingResult).addError(any());
        verify(userService).getUserById(ID);
        verify(userService).existsUser(NEW_USERNAME);
        verify(localeResolver, never()).setLocale(httpServletRequest, httpServletResponse, Locale.ENGLISH);
        verify(httpServletRequest, never()).getSession(false);
    }

    @Test
    public void testUpdateUserNewEmailBlank() {
        userDTO.setEmail(BLANK);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(httpServletRequest.isUserInRole("ROLE_ADMIN")).thenReturn(true);
        assertEquals(PROFILE_EDIT, userController.updateUser(userDTO, bindingResult, httpServletRequest,
                httpServletResponse));
        verify(bindingResult).addError(any());
        verify(userService).getUserById(ID);
        verify(localeResolver, never()).setLocale(httpServletRequest, httpServletResponse, Locale.ENGLISH);
        verify(httpServletRequest, never()).getSession(false);
    }

    @Test
    public void testUpdateUserNotFound() {
        when(userService.getUserById(ID)).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, userController.updateUser(userDTO, bindingResult, httpServletRequest,
                httpServletResponse));
        verify(userService).getUserById(ID);
    }

    @Test
    public void testUpdateUserEmailNull() {
        userDTO.setEmail(null);
        assertEquals(Constants.ERROR, userController.updateUser(userDTO, bindingResult, httpServletRequest,
                httpServletResponse));
        verify(userService, never()).getUserById(ID);
    }

    @Test
    public void testDeleteUser() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(userService.getUserById(ID)).thenReturn(userDTO);
        when(userService.matchesPassword(PASSWORD, PASSWORD)).thenReturn(true);
        assertEquals(REDIRECT_SUCCESS, userController.deleteUser(passwordDTO, ID_STRING, httpServletRequest));
        verify(authentication, times(2)).getName();
        verify(userService).getUser(USERNAME);
        verify(userService).getUserById(ID);
        verify(userService).matchesPassword(PASSWORD, PASSWORD);
        verify(userService).isLastAdmin();
        verify(userService).deleteUser(ID);
        verify(httpServletRequest).getSession(false);
    }

    @Test
    public void testDeleteUserNotEqual() {
        oldDTO.setId(ID + 1);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(userService.matchesPassword(PASSWORD, PASSWORD)).thenReturn(true);
        assertEquals(REDIRECT_SUCCESS, userController.deleteUser(passwordDTO, ID_STRING, httpServletRequest));
        verify(authentication, times(2)).getName();
        verify(userService).getUser(USERNAME);
        verify(userService).getUserById(ID);
        verify(userService).matchesPassword(PASSWORD, PASSWORD);
        verify(userService).isLastAdmin();
        verify(userService).deleteUser(ID + 1);
        verify(httpServletRequest, never()).getSession(anyBoolean());
    }

    @Test
    public void testDeleteUserParticipant() {
        oldDTO.setId(ID + 1);
        oldDTO.setRole(UserDTO.Role.PARTICIPANT);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(userService.matchesPassword(PASSWORD, PASSWORD)).thenReturn(true);
        assertEquals(REDIRECT_SUCCESS, userController.deleteUser(passwordDTO, ID_STRING, httpServletRequest));
        verify(authentication, times(2)).getName();
        verify(userService).getUser(USERNAME);
        verify(userService).getUserById(ID);
        verify(userService).matchesPassword(PASSWORD, PASSWORD);
        verify(userService, never()).isLastAdmin();
        verify(userService).deleteUser(ID + 1);
        verify(httpServletRequest, never()).getSession(anyBoolean());
    }

    @Test
    public void testDeleteUserLastAdmin() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(userService.getUserById(ID)).thenReturn(userDTO);
        when(userService.matchesPassword(PASSWORD, PASSWORD)).thenReturn(true);
        when(userService.isLastAdmin()).thenReturn(true);
        assertEquals(LAST_ADMIN, userController.deleteUser(passwordDTO, ID_STRING, httpServletRequest));
        verify(authentication, times(2)).getName();
        verify(userService).getUser(USERNAME);
        verify(userService).getUserById(ID);
        verify(userService).matchesPassword(PASSWORD, PASSWORD);
        verify(userService).isLastAdmin();
        verify(userService, never()).deleteUser(anyInt());
        verify(httpServletRequest, never()).getSession(anyBoolean());
    }

    @Test
    public void testDeleteUserPasswordNotMatching() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(userService.getUserById(ID)).thenReturn(userDTO);
        assertEquals(INVALID + USERNAME, userController.deleteUser(passwordDTO, ID_STRING, httpServletRequest));
        verify(authentication, times(2)).getName();
        verify(userService).getUser(USERNAME);
        verify(userService).getUserById(ID);
        verify(userService).matchesPassword(PASSWORD, PASSWORD);
        verify(userService, never()).isLastAdmin();
        verify(userService, never()).deleteUser(anyInt());
        verify(httpServletRequest, never()).getSession(anyBoolean());
    }

    @Test
    public void testDeleteUserPasswordTooLong() {
        passwordDTO.setPassword(LONG_USERNAME);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(userService.getUserById(ID)).thenReturn(userDTO);
        assertEquals(INVALID + USERNAME, userController.deleteUser(passwordDTO, ID_STRING, httpServletRequest));
        verify(authentication, times(2)).getName();
        verify(userService).getUser(USERNAME);
        verify(userService).getUserById(ID);
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(userService, never()).isLastAdmin();
        verify(userService, never()).deleteUser(anyInt());
        verify(httpServletRequest, never()).getSession(anyBoolean());
    }

    @Test
    public void testDeleteUserNotFound() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, userController.deleteUser(passwordDTO, ID_STRING, httpServletRequest));
        verify(authentication, times(2)).getName();
        verify(userService).getUser(USERNAME);
        verify(userService, never()).getUserById(anyInt());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(userService, never()).isLastAdmin();
        verify(userService, never()).deleteUser(anyInt());
        verify(httpServletRequest, never()).getSession(anyBoolean());
    }

    @Test
    public void testDeleteUserAuthenticationNameNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        assertEquals(Constants.ERROR, userController.deleteUser(passwordDTO, ID_STRING, httpServletRequest));
        verify(authentication).getName();
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).getUserById(anyInt());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(userService, never()).isLastAdmin();
        verify(userService, never()).deleteUser(anyInt());
        verify(httpServletRequest, never()).getSession(anyBoolean());
    }

    @Test
    public void testDeleteUserInvalidId() {
        assertEquals(Constants.ERROR, userController.deleteUser(passwordDTO, "0", httpServletRequest));
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).getUserById(anyInt());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(userService, never()).isLastAdmin();
        verify(userService, never()).deleteUser(anyInt());
        verify(httpServletRequest, never()).getSession(anyBoolean());
    }

    @Test
    public void testDeleteUserNumberFormat() {
        assertEquals(Constants.ERROR, userController.deleteUser(passwordDTO, BLANK, httpServletRequest));
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).getUserById(anyInt());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(userService, never()).isLastAdmin();
        verify(userService, never()).deleteUser(anyInt());
        verify(httpServletRequest, never()).getSession(anyBoolean());
    }

    @Test
    public void testDeleteUserIdNull() {
        assertEquals(Constants.ERROR, userController.deleteUser(passwordDTO, null, httpServletRequest));
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).getUserById(anyInt());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(userService, never()).isLastAdmin();
        verify(userService, never()).deleteUser(anyInt());
        verify(httpServletRequest, never()).getSession(anyBoolean());
    }

    @Test
    public void testDeleteUserPasswordNull() {
        passwordDTO.setPassword(null);
        assertEquals(Constants.ERROR, userController.deleteUser(passwordDTO, ID_STRING, httpServletRequest));
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).getUserById(anyInt());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(userService, never()).isLastAdmin();
        verify(userService, never()).deleteUser(anyInt());
        verify(httpServletRequest, never()).getSession(anyBoolean());
    }

    @Test
    public void testChangeActiveStatusDeactivate() {
        userDTO.setRole(UserDTO.Role.PARTICIPANT);
        when(userService.getUserById(ID)).thenReturn(userDTO);
        assertEquals(PROFILE_REDIRECT + userDTO.getUsername(), userController.changeActiveStatus(ID_STRING));
        verify(userService).getUserById(ID);
        verify(userService).updateUser(userDTO);
    }

    @Test
    public void testChangeActiveStatusActivate() {
        userDTO.setRole(UserDTO.Role.PARTICIPANT);
        userDTO.setActive(false);
        when(userService.getUserById(ID)).thenReturn(userDTO);
        assertEquals(PROFILE_REDIRECT + userDTO.getUsername(), userController.changeActiveStatus(ID_STRING));
        verify(userService).getUserById(ID);
        verify(userService).updateUser(userDTO);
    }

    @Test
    public void testChangeActiveStatusUserAdmin() {
        when(userService.getUserById(ID)).thenReturn(userDTO);
        assertEquals(Constants.ERROR, userController.changeActiveStatus(ID_STRING));
        verify(userService).getUserById(ID);
        verify(userService, never()).updateUser(userDTO);
    }

    @Test
    public void testChangeActiveStatusNotFound() {
        when(userService.getUserById(ID)).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, userController.changeActiveStatus(ID_STRING));
        verify(userService).getUserById(ID);
        verify(userService, never()).updateUser(userDTO);
    }

    @Test
    public void testChangeActiveStatusIdInvalid() {
        assertEquals(Constants.ERROR, userController.changeActiveStatus(BLANK));
        verify(userService, never()).getUserById(ID);
        verify(userService, never()).updateUser(userDTO);
    }

    @Test
    public void testChangeActiveStatusIdNull() {
        assertEquals(Constants.ERROR, userController.changeActiveStatus(null));
        verify(userService, never()).getUserById(ID);
        verify(userService, never()).updateUser(userDTO);
    }

    @Test
    public void testGetPasswordResetForm() {
        when(userService.getUserById(ID)).thenReturn(userDTO);
        when(httpServletRequest.isUserInRole(ROLE_ADMIN)).thenReturn(true);
        assertEquals(PASSWORD_PAGE, userController.getPasswordResetForm(ID_STRING, model, httpServletRequest));
        verify(userService).getUserById(ID);
        verify(httpServletRequest).isUserInRole(ROLE_ADMIN);
    }

    @Test
    public void testGetPasswordResetFormNoAdmin() {
        when(userService.getUserById(ID)).thenReturn(userDTO);
        assertEquals(Constants.ERROR, userController.getPasswordResetForm(ID_STRING, model, httpServletRequest));
        verify(userService).getUserById(ID);
        verify(httpServletRequest).isUserInRole(ROLE_ADMIN);
    }

    @Test
    public void testGetPasswordResetFormUserNotFound() {
        when(userService.getUserById(ID)).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, userController.getPasswordResetForm(ID_STRING, model, httpServletRequest));
        verify(userService).getUserById(ID);
        verify(httpServletRequest, never()).isUserInRole(anyString());
    }

    @Test
    public void testGetPasswordResetFormInvalidId() {
        assertEquals(Constants.ERROR, userController.getPasswordResetForm("-1", model, httpServletRequest));
        verify(userService, never()).getUserById(anyInt());
        verify(httpServletRequest, never()).isUserInRole(anyString());
    }

    @Test
    public void testGetPasswordResetFormIdNull() {
        assertEquals(Constants.ERROR, userController.getPasswordResetForm(null, model, httpServletRequest));
        verify(userService, never()).getUserById(anyInt());
        verify(httpServletRequest, never()).isUserInRole(anyString());
    }

    @Test
    public void testResetPassword() {
        userDTO.setNewPassword(VALID_PASSWORD);
        userDTO.setConfirmPassword(VALID_PASSWORD);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(httpServletRequest.isUserInRole(ROLE_ADMIN)).thenReturn(true);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(userService.matchesPassword(PASSWORD, PASSWORD)).thenReturn(true);
        when(userService.encodePassword(VALID_PASSWORD)).thenReturn(VALID_PASSWORD);
        assertEquals(PROFILE_REDIRECT + USERNAME, userController.passwordReset(userDTO, bindingResult,
                httpServletRequest));
        verify(userService).getUserById(ID);
        verify(httpServletRequest).isUserInRole(ROLE_ADMIN);
        verify(authentication, times(2)).getName();
        verify(userService).getUser(USERNAME);
        verify(userService).matchesPassword(PASSWORD, PASSWORD);
        verify(userService).encodePassword(VALID_PASSWORD);
    }

    @Test
    public void testResetPasswordPasswordInvalid() {
        userDTO.setNewPassword(VALID_PASSWORD);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(httpServletRequest.isUserInRole(ROLE_ADMIN)).thenReturn(true);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(userService.matchesPassword(PASSWORD, PASSWORD)).thenReturn(true);
        when(bindingResult.hasErrors()).thenReturn(true);
        assertEquals(PASSWORD_PAGE, userController.passwordReset(userDTO, bindingResult, httpServletRequest));
        verify(userService).getUserById(ID);
        verify(httpServletRequest).isUserInRole(ROLE_ADMIN);
        verify(authentication, times(2)).getName();
        verify(userService).getUser(USERNAME);
        verify(userService).matchesPassword(PASSWORD, PASSWORD);
        verify(bindingResult).hasErrors();
        verify(userService, never()).encodePassword(anyString());
    }

    @Test
    public void testResetPasswordNewPasswordBlank() {
        userDTO.setNewPassword(BLANK);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(httpServletRequest.isUserInRole(ROLE_ADMIN)).thenReturn(true);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(bindingResult.hasErrors()).thenReturn(true);
        assertEquals(PASSWORD_PAGE, userController.passwordReset(userDTO, bindingResult, httpServletRequest));
        verify(userService).getUserById(ID);
        verify(httpServletRequest).isUserInRole(ROLE_ADMIN);
        verify(authentication, times(2)).getName();
        verify(userService).getUser(USERNAME);
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(bindingResult).hasErrors();
        verify(userService, never()).encodePassword(anyString());
    }

    @Test
    public void testResetPasswordAdminNotFound() {
        userDTO.setNewPassword(VALID_PASSWORD);
        userDTO.setConfirmPassword(VALID_PASSWORD);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(httpServletRequest.isUserInRole(ROLE_ADMIN)).thenReturn(true);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, userController.passwordReset(userDTO, bindingResult, httpServletRequest));
        verify(userService).getUserById(ID);
        verify(httpServletRequest).isUserInRole(ROLE_ADMIN);
        verify(authentication, times(2)).getName();
        verify(userService).getUser(USERNAME);
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(bindingResult, never()).hasErrors();
        verify(userService, never()).encodePassword(anyString());
    }

    @Test
    public void testResetPasswordAuthenticationNameNull() {
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(httpServletRequest.isUserInRole(ROLE_ADMIN)).thenReturn(true);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        assertEquals(Constants.ERROR, userController.passwordReset(userDTO, bindingResult, httpServletRequest));
        verify(userService).getUserById(ID);
        verify(httpServletRequest).isUserInRole(ROLE_ADMIN);
        verify(authentication).getName();
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(bindingResult, never()).hasErrors();
        verify(userService, never()).encodePassword(anyString());
    }

    @Test
    public void testResetPasswordUserNotAdmin() {
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        assertEquals(Constants.ERROR, userController.passwordReset(userDTO, bindingResult, httpServletRequest));
        verify(userService).getUserById(ID);
        verify(httpServletRequest).isUserInRole(ROLE_ADMIN);
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(bindingResult, never()).hasErrors();
        verify(userService, never()).encodePassword(anyString());
    }

    @Test
    public void testResetPasswordUserNotFound() {
        when(userService.getUserById(ID)).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, userController.passwordReset(userDTO, bindingResult, httpServletRequest));
        verify(userService).getUserById(ID);
        verify(httpServletRequest, never()).isUserInRole(anyString());
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(bindingResult, never()).hasErrors();
        verify(userService, never()).encodePassword(anyString());
    }

    @Test
    public void testResetPasswordConfirmPasswordNull() {
        userDTO.setConfirmPassword(null);
        assertEquals(Constants.ERROR, userController.passwordReset(userDTO, bindingResult, httpServletRequest));
        verify(userService, never()).getUserById(ID);
        verify(httpServletRequest, never()).isUserInRole(anyString());
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(bindingResult, never()).hasErrors();
        verify(userService, never()).encodePassword(anyString());
    }

    @Test
    public void testResetPasswordNewPasswordNull() {
        userDTO.setNewPassword(null);
        assertEquals(Constants.ERROR, userController.passwordReset(userDTO, bindingResult, httpServletRequest));
        verify(userService, never()).getUserById(ID);
        verify(httpServletRequest, never()).isUserInRole(anyString());
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(bindingResult, never()).hasErrors();
        verify(userService, never()).encodePassword(anyString());
    }

    @Test
    public void testResetPasswordPasswordNull() {
        userDTO.setPassword(null);
        assertEquals(Constants.ERROR, userController.passwordReset(userDTO, bindingResult, httpServletRequest));
        verify(userService, never()).getUserById(ID);
        verify(httpServletRequest, never()).isUserInRole(anyString());
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(bindingResult, never()).hasErrors();
        verify(userService, never()).encodePassword(anyString());
    }

}
