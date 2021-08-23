package fim.unipassau.de.scratch1984.web;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.application.service.MailService;
import fim.unipassau.de.scratch1984.application.service.ParticipantService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.web.controller.ParticipantController;
import fim.unipassau.de.scratch1984.web.dto.ExperimentDTO;
import fim.unipassau.de.scratch1984.web.dto.ParticipantDTO;
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

import java.time.LocalDateTime;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParticipantControllerTest {

    @InjectMocks
    private ParticipantController participantController;

    @Mock
    private UserService userService;

    @Mock
    private ExperimentService experimentService;

    @Mock
    private ParticipantService participantService;

    @Mock
    private MailService mailService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private ResourceBundle resourceBundle;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private MockedStatic<SecurityContextHolder> securityContextHolder;
    private static final String ERROR = "redirect:/error";
    private static final String PARTICIPANT = "participant";
    private static final String EXPERIMENT = "experiment";
    private static final String REDIRECT_EXPERIMENT = "redirect:/experiment?id=";
    private static final String REDIRECT_GUI = "redirect:http://localhost:8601?uid=";
    private static final String REDIRECT_FINISH = "redirect:/finish?id=";
    private static final String EXP_ID = "&expid=";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String EMAIL = "participant@participant.de";
    private static final String BLANK = "   ";
    private static final String ID_STRING = "1";
    private static final String INFO = "info";
    private static final String POSTSCRIPT = "postscript";
    private static final String ERROR_ATTRIBUTE = "error";
    private static final String LONG_INPUT = "looooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooong"
            + "iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiinput";
    private static final int ID = 1;
    private final UserDTO newUser = new UserDTO(PARTICIPANT, EMAIL, UserDTO.Role.PARTICIPANT,
            UserDTO.Language.ENGLISH, "password", "secret");
    private final UserDTO userDTO = new UserDTO(PARTICIPANT, EMAIL, UserDTO.Role.PARTICIPANT,
            UserDTO.Language.ENGLISH, "password", "secret");
    private final ExperimentDTO experimentDTO = new ExperimentDTO(ID, "title", "description", INFO, POSTSCRIPT, true);
    private final ParticipantDTO participantDTO = new ParticipantDTO(ID, ID);

    @BeforeEach
    public void setup() {
        userDTO.setId(ID);
        userDTO.setUsername(PARTICIPANT);
        userDTO.setEmail(EMAIL);
        userDTO.setRole(UserDTO.Role.PARTICIPANT);
        userDTO.setActive(true);
        userDTO.setSecret("secret");
        experimentDTO.setActive(true);
        experimentDTO.setInfo(INFO);
        participantDTO.setStart(null);
        participantDTO.setEnd(null);
        securityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
    }

    @AfterEach
    public void cleanup() {
        securityContextHolder.close();
    }

    @Test
    public void testGetParticipantForm() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.findLastId()).thenReturn(ID);
        assertEquals(PARTICIPANT, participantController.getParticipantForm(ID_STRING, model));
        verify(experimentService).getExperiment(ID);
        verify(userService).findLastId();
        verify(model, times(2)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetParticipantFormNotFound() {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, participantController.getParticipantForm(ID_STRING, model));
        verify(experimentService).getExperiment(ID);
        verify(userService, never()).findLastId();
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetParticipantFormExperimentInactive() {
        experimentDTO.setActive(false);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        assertEquals(ERROR, participantController.getParticipantForm(ID_STRING, model));
        verify(experimentService).getExperiment(ID);
        verify(userService, never()).findLastId();
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetParticipantFormExperimentIdInvalid() {
        assertEquals(ERROR, participantController.getParticipantForm("0", model));
        verify(experimentService, never()).getExperiment(ID);
        verify(userService, never()).findLastId();
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetParticipantFormExperimentIdNull() {
        assertEquals(ERROR, participantController.getParticipantForm(null, model));
        verify(experimentService, never()).getExperiment(ID);
        verify(userService, never()).findLastId();
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetParticipantFormExperimentIdBlank() {
        assertEquals(ERROR, participantController.getParticipantForm(BLANK, model));
        verify(experimentService, never()).getExperiment(ID);
        verify(userService, never()).findLastId();
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testAddParticipant() {
        when(userService.saveUser(newUser)).thenReturn(userDTO);
        when(mailService.sendEmail(anyString(), any(), any(), anyString())).thenReturn(true);
        assertEquals(REDIRECT_EXPERIMENT + ID, participantController.addParticipant(ID_STRING, newUser, model,
                bindingResult, httpServletRequest));
        verify(userService).saveUser(newUser);
        verify(participantService).saveParticipant(userDTO.getId(), ID);
        verify(mailService).sendEmail(anyString(), any(), any(), anyString());
        verify(bindingResult, never()).addError(any());
    }

    @Test
    public void testAddParticipantMessagingException() {
        when(userService.saveUser(newUser)).thenReturn(userDTO);
        assertEquals(ERROR, participantController.addParticipant(ID_STRING, newUser, model, bindingResult,
                httpServletRequest));
        verify(userService).saveUser(newUser);
        verify(participantService).saveParticipant(userDTO.getId(), ID);
        verify(mailService).sendEmail(anyString(), any(), any(), anyString());
        verify(bindingResult, never()).addError(any());
    }

    @Test
    public void testAddParticipantNotFound() {
        when(userService.saveUser(newUser)).thenReturn(userDTO);
        doThrow(NotFoundException.class).when(participantService).saveParticipant(userDTO.getId(), ID);
        assertEquals(ERROR, participantController.addParticipant(ID_STRING, newUser, model, bindingResult,
                httpServletRequest));
        verify(userService).saveUser(newUser);
        verify(participantService).saveParticipant(userDTO.getId(), ID);
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
        verify(bindingResult, never()).addError(any());
    }

    @Test
    public void testAddParticipantExistsEmail() {
        when(userService.existsEmail(newUser.getEmail())).thenReturn(true);
        when(bindingResult.hasErrors()).thenReturn(true);
        assertEquals(PARTICIPANT, participantController.addParticipant(ID_STRING, newUser, model, bindingResult,
                httpServletRequest));
        verify(userService, never()).saveUser(any());
        verify(participantService, never()).saveParticipant(anyInt(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
        verify(bindingResult).addError(any());
    }

    @Test
    public void testAddParticipantEmailInvalid() {
        newUser.setEmail(PARTICIPANT);
        when(bindingResult.hasErrors()).thenReturn(true);
        assertEquals(PARTICIPANT, participantController.addParticipant(ID_STRING, newUser, model, bindingResult,
                httpServletRequest));
        verify(userService, never()).saveUser(any());
        verify(participantService, never()).saveParticipant(anyInt(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
        verify(bindingResult).addError(any());
    }

    @Test
    public void testAddParticipantUsernameExists() {
        when(userService.existsUser(PARTICIPANT)).thenReturn(true);
        when(bindingResult.hasErrors()).thenReturn(true);
        assertEquals(PARTICIPANT, participantController.addParticipant(ID_STRING, newUser, model, bindingResult,
                httpServletRequest));
        verify(userService, never()).saveUser(any());
        verify(participantService, never()).saveParticipant(anyInt(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
        verify(bindingResult).addError(any());
    }

    @Test
    public void testAddParticipantUsernameInvalid() {
        newUser.setUsername(BLANK);
        when(bindingResult.hasErrors()).thenReturn(true);
        assertEquals(PARTICIPANT, participantController.addParticipant(ID_STRING, newUser, model, bindingResult,
                httpServletRequest));
        verify(userService, never()).saveUser(any());
        verify(participantService, never()).saveParticipant(anyInt(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
        verify(bindingResult).addError(any());
    }

    @Test
    public void testAddParticipantExperimentIdInvalid() {
        assertEquals(ERROR, participantController.addParticipant("0", newUser, model, bindingResult,
                httpServletRequest));
        verify(userService, never()).saveUser(userDTO);
        verify(participantService, never()).saveParticipant(userDTO.getId(), ID);
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
        verify(bindingResult, never()).addError(any());
    }

    @Test
    public void testAddParticipantUserIdNotNull() {
        assertEquals(ERROR, participantController.addParticipant(ID_STRING, userDTO, model, bindingResult,
                httpServletRequest));
        verify(userService, never()).saveUser(userDTO);
        verify(participantService, never()).saveParticipant(userDTO.getId(), ID);
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
        verify(bindingResult, never()).addError(any());
    }

    @Test
    public void testAddParticipantUsernameNull() {
        userDTO.setUsername(null);
        assertEquals(ERROR, participantController.addParticipant(ID_STRING, userDTO, model, bindingResult,
                httpServletRequest));
        verify(userService, never()).saveUser(userDTO);
        verify(participantService, never()).saveParticipant(userDTO.getId(), ID);
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
        verify(bindingResult, never()).addError(any());
    }

    @Test
    public void testAddParticipantEmailNull() {
        userDTO.setEmail(null);
        assertEquals(ERROR, participantController.addParticipant(ID_STRING, userDTO, model, bindingResult,
                httpServletRequest));
        verify(userService, never()).saveUser(userDTO);
        verify(participantService, never()).saveParticipant(userDTO.getId(), ID);
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
        verify(bindingResult, never()).addError(any());
    }

    @Test
    public void testAddParticipantExperimentIdNull() {
        assertEquals(ERROR, participantController.addParticipant(null, userDTO, model, bindingResult,
                httpServletRequest));
        verify(userService, never()).saveUser(userDTO);
        verify(participantService, never()).saveParticipant(userDTO.getId(), ID);
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
        verify(bindingResult, never()).addError(any());
    }

    @Test
    public void testDeleteParticipant() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT)).thenReturn(userDTO);
        when(userService.existsParticipant(userDTO.getId(), ID)).thenReturn(true);
        when(userService.updateUser(userDTO)).thenReturn(userDTO);
        assertEquals(REDIRECT_EXPERIMENT + ID, participantController.deleteParticipant(PARTICIPANT, ID_STRING,
                model));
        verify(userService).getUserByUsernameOrEmail(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(userService).existsParticipant(userDTO.getId(), ID);
        verify(participantService).simultaneousParticipation(ID);
        verify(userService).updateUser(userDTO);
        verify(participantService).deleteParticipant(userDTO.getId(), ID);
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteParticipantSimultaneousParticipation() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT)).thenReturn(userDTO);
        when(userService.existsParticipant(userDTO.getId(), ID)).thenReturn(true);
        when(participantService.simultaneousParticipation(ID)).thenReturn(true);
        assertEquals(REDIRECT_EXPERIMENT + ID, participantController.deleteParticipant(PARTICIPANT, ID_STRING,
                model));
        verify(userService).getUserByUsernameOrEmail(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(userService).existsParticipant(userDTO.getId(), ID);
        verify(participantService).simultaneousParticipation(ID);
        verify(userService, never()).updateUser(any());
        verify(participantService).deleteParticipant(userDTO.getId(), ID);
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteParticipantUpdateUserNotFound() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT)).thenReturn(userDTO);
        when(userService.existsParticipant(userDTO.getId(), ID)).thenReturn(true);
        when(userService.updateUser(userDTO)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, participantController.deleteParticipant(PARTICIPANT, ID_STRING, model));
        verify(userService).getUserByUsernameOrEmail(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(userService).existsParticipant(userDTO.getId(), ID);
        verify(participantService).simultaneousParticipation(ID);
        verify(userService).updateUser(userDTO);
        verify(participantService, never()).deleteParticipant(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteParticipantExperimentInactiveInfoNull() {
        experimentDTO.setInfo(null);
        experimentDTO.setActive(false);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT)).thenReturn(userDTO);
        when(userService.existsParticipant(userDTO.getId(), ID)).thenReturn(true);
        when(model.getAttribute(ERROR_ATTRIBUTE)).thenReturn(ERROR_ATTRIBUTE);
        assertEquals(EXPERIMENT, participantController.deleteParticipant(PARTICIPANT, ID_STRING, model));
        verify(userService).getUserByUsernameOrEmail(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(userService).existsParticipant(userDTO.getId(), ID);
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(userService, never()).updateUser(any());
        verify(participantService, never()).deleteParticipant(anyInt(), anyInt());
        verify(model, times(5)).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteParticipantNoParticipantEntry() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT)).thenReturn(userDTO);
        when(model.getAttribute(ERROR_ATTRIBUTE)).thenReturn(ERROR_ATTRIBUTE);
        assertEquals(EXPERIMENT, participantController.deleteParticipant(PARTICIPANT, ID_STRING, model));
        verify(userService).getUserByUsernameOrEmail(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(userService).existsParticipant(userDTO.getId(), ID);
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(userService, never()).updateUser(any());
        verify(participantService, never()).deleteParticipant(anyInt(), anyInt());
        verify(model, times(5)).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteParticipantUserAdmin() {
        userDTO.setRole(UserDTO.Role.ADMIN);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT)).thenReturn(userDTO);
        when(model.getAttribute(ERROR_ATTRIBUTE)).thenReturn(ERROR_ATTRIBUTE);
        assertEquals(EXPERIMENT, participantController.deleteParticipant(PARTICIPANT, ID_STRING, model));
        verify(userService).getUserByUsernameOrEmail(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(userService, never()).existsParticipant(anyInt(), anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(userService, never()).updateUser(any());
        verify(participantService, never()).deleteParticipant(anyInt(), anyInt());
        verify(model, times(5)).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteParticipantUserNull() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        assertEquals(EXPERIMENT, participantController.deleteParticipant(PARTICIPANT, ID_STRING, model));
        verify(userService).getUserByUsernameOrEmail(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(userService, never()).existsParticipant(anyInt(), anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(userService, never()).updateUser(any());
        verify(participantService, never()).deleteParticipant(anyInt(), anyInt());
        verify(model, times(5)).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteParticipantExperimentNotFound() {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, participantController.deleteParticipant(PARTICIPANT, ID_STRING, model));
        verify(userService).getUserByUsernameOrEmail(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(userService, never()).existsParticipant(anyInt(), anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(userService, never()).updateUser(any());
        verify(participantService, never()).deleteParticipant(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteParticipantExperimentIdInvalid() {
        assertEquals(ERROR, participantController.deleteParticipant(PARTICIPANT, "0", model));
        verify(userService, never()).getUserByUsernameOrEmail(anyString());
        verify(experimentService, never()).getExperiment(anyInt());
        verify(userService, never()).existsParticipant(anyInt(), anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(userService, never()).updateUser(any());
        verify(participantService, never()).deleteParticipant(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteParticipantInputTooLong() {
        assertEquals(ERROR, participantController.deleteParticipant(LONG_INPUT, ID_STRING, model));
        verify(userService, never()).getUserByUsernameOrEmail(anyString());
        verify(experimentService, never()).getExperiment(anyInt());
        verify(userService, never()).existsParticipant(anyInt(), anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(userService, never()).updateUser(any());
        verify(participantService, never()).deleteParticipant(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteParticipantInputBlank() {
        assertEquals(ERROR, participantController.deleteParticipant(BLANK, ID_STRING, model));
        verify(userService, never()).getUserByUsernameOrEmail(anyString());
        verify(experimentService, never()).getExperiment(anyInt());
        verify(userService, never()).existsParticipant(anyInt(), anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(userService, never()).updateUser(any());
        verify(participantService, never()).deleteParticipant(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteParticipantInputNull() {
        assertEquals(ERROR, participantController.deleteParticipant(null, ID_STRING, model));
        verify(userService, never()).getUserByUsernameOrEmail(anyString());
        verify(experimentService, never()).getExperiment(anyInt());
        verify(userService, never()).existsParticipant(anyInt(), anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(userService, never()).updateUser(any());
        verify(participantService, never()).deleteParticipant(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteParticipantIdNull() {
        assertEquals(ERROR, participantController.deleteParticipant(PARTICIPANT, null, model));
        verify(userService, never()).getUserByUsernameOrEmail(anyString());
        verify(experimentService, never()).getExperiment(anyInt());
        verify(userService, never()).existsParticipant(anyInt(), anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(userService, never()).updateUser(any());
        verify(participantService, never()).deleteParticipant(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testStartExperiment() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(PARTICIPANT);
        when(userService.getUser(PARTICIPANT)).thenReturn(userDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        when(participantService.updateParticipant(participantDTO)).thenReturn(true);
        assertEquals(REDIRECT_GUI + ID + EXP_ID + ID, participantController.startExperiment(ID_STRING,
                httpServletRequest));
        verify(httpServletRequest).isUserInRole(ROLE_ADMIN);
        verify(authentication, times(2)).getName();
        verify(userService).getUser(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService).updateParticipant(participantDTO);
    }

    @Test
    public void testStartExperimentParticipantNotUpdated() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(PARTICIPANT);
        when(userService.getUser(PARTICIPANT)).thenReturn(userDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        assertEquals(ERROR, participantController.startExperiment(ID_STRING, httpServletRequest));
        verify(httpServletRequest).isUserInRole(ROLE_ADMIN);
        verify(authentication, times(2)).getName();
        verify(userService).getUser(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService).updateParticipant(participantDTO);
    }

    @Test
    public void testStartExperimentParticipantFinished() {
        participantDTO.setEnd(LocalDateTime.now());
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(PARTICIPANT);
        when(userService.getUser(PARTICIPANT)).thenReturn(userDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        assertEquals(ERROR, participantController.startExperiment(ID_STRING, httpServletRequest));
        verify(httpServletRequest).isUserInRole(ROLE_ADMIN);
        verify(authentication, times(2)).getName();
        verify(userService).getUser(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService, never()).updateParticipant(any());
    }

    @Test
    public void testStartExperimentParticipantStarted() {
        participantDTO.setStart(LocalDateTime.now());
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(PARTICIPANT);
        when(userService.getUser(PARTICIPANT)).thenReturn(userDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        assertEquals(ERROR, participantController.startExperiment(ID_STRING, httpServletRequest));
        verify(httpServletRequest).isUserInRole(ROLE_ADMIN);
        verify(authentication, times(2)).getName();
        verify(userService).getUser(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService, never()).updateParticipant(any());
    }

    @Test
    public void testStartExperimentParticipantInactive() {
        userDTO.setActive(false);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(PARTICIPANT);
        when(userService.getUser(PARTICIPANT)).thenReturn(userDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        assertEquals(ERROR, participantController.startExperiment(ID_STRING, httpServletRequest));
        verify(httpServletRequest).isUserInRole(ROLE_ADMIN);
        verify(authentication, times(2)).getName();
        verify(userService).getUser(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService, never()).updateParticipant(any());
    }

    @Test
    public void testStartExperimentParticipantSecretNull() {
        userDTO.setSecret(null);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(PARTICIPANT);
        when(userService.getUser(PARTICIPANT)).thenReturn(userDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        assertEquals(ERROR, participantController.startExperiment(ID_STRING, httpServletRequest));
        verify(httpServletRequest).isUserInRole(ROLE_ADMIN);
        verify(authentication, times(2)).getName();
        verify(userService).getUser(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService, never()).updateParticipant(any());
    }

    @Test
    public void testStartExperimentParticipantExperimentClosed() {
        experimentDTO.setActive(false);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(PARTICIPANT);
        when(userService.getUser(PARTICIPANT)).thenReturn(userDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        assertEquals(ERROR, participantController.startExperiment(ID_STRING, httpServletRequest));
        verify(httpServletRequest).isUserInRole(ROLE_ADMIN);
        verify(authentication, times(2)).getName();
        verify(userService).getUser(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService, never()).updateParticipant(any());
    }

    @Test
    public void testStartExperimentParticipantNotFound() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(PARTICIPANT);
        when(userService.getUser(PARTICIPANT)).thenReturn(userDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipant(ID, ID)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, participantController.startExperiment(ID_STRING, httpServletRequest));
        verify(httpServletRequest).isUserInRole(ROLE_ADMIN);
        verify(authentication, times(2)).getName();
        verify(userService).getUser(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService, never()).updateParticipant(any());
    }

    @Test
    public void testStartExperimentParticipantAuthenticationNameNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        assertEquals(ERROR, participantController.startExperiment(ID_STRING, httpServletRequest));
        verify(httpServletRequest).isUserInRole(ROLE_ADMIN);
        verify(authentication).getName();
        verify(userService, never()).getUser(anyString());
        verify(experimentService, never()).getExperiment(anyInt());
        verify(participantService, never()).getParticipant(anyInt(), anyInt());
        verify(participantService, never()).updateParticipant(any());
    }

    @Test
    public void testStartExperimentParticipantUserAdmin() {
        when(httpServletRequest.isUserInRole(ROLE_ADMIN)).thenReturn(true);
        assertEquals(ERROR, participantController.startExperiment(ID_STRING, httpServletRequest));
        verify(httpServletRequest).isUserInRole(ROLE_ADMIN);
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(experimentService, never()).getExperiment(anyInt());
        verify(participantService, never()).getParticipant(anyInt(), anyInt());
        verify(participantService, never()).updateParticipant(any());
    }

    @Test
    public void testStartExperimentParticipantInvalidId() {
        assertEquals(ERROR, participantController.startExperiment(BLANK, httpServletRequest));
        verify(httpServletRequest, never()).isUserInRole(anyString());
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(experimentService, never()).getExperiment(anyInt());
        verify(participantService, never()).getParticipant(anyInt(), anyInt());
        verify(participantService, never()).updateParticipant(any());
    }

    @Test
    public void testStartExperimentParticipantIdNull() {
        assertEquals(ERROR, participantController.startExperiment(null, httpServletRequest));
        verify(httpServletRequest, never()).isUserInRole(anyString());
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(experimentService, never()).getExperiment(anyInt());
        verify(participantService, never()).getParticipant(anyInt(), anyInt());
        verify(participantService, never()).updateParticipant(any());
    }

    @Test
    public void testStopExperiment() {
        participantDTO.setStart(LocalDateTime.now());
        when(userService.getUserById(ID)).thenReturn(userDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        when(participantService.updateParticipant(participantDTO)).thenReturn(true);
        assertEquals(REDIRECT_FINISH + ID, participantController.stopExperiment(ID_STRING, ID_STRING, httpServletRequest));
        verify(userService).getUserById(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService).simultaneousParticipation(ID);
        verify(participantService).updateParticipant(participantDTO);
        verify(userService).saveUser(userDTO);
        verify(httpServletRequest).getSession(false);
    }

    @Test
    public void testStopExperimentSimultaneousParticipation() {
        participantDTO.setStart(LocalDateTime.now());
        when(userService.getUserById(ID)).thenReturn(userDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        when(participantService.simultaneousParticipation(ID)).thenReturn(true);
        when(participantService.updateParticipant(participantDTO)).thenReturn(true);
        assertEquals(REDIRECT_FINISH + ID, participantController.stopExperiment(ID_STRING, ID_STRING, httpServletRequest));
        verify(userService).getUserById(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService).simultaneousParticipation(ID);
        verify(participantService).updateParticipant(participantDTO);
        verify(userService, never()).saveUser(any());
        verify(httpServletRequest).getSession(false);
    }

    @Test
    public void testStopExperimentParticipantNotUpdated() {
        participantDTO.setStart(LocalDateTime.now());
        when(userService.getUserById(ID)).thenReturn(userDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        assertEquals(ERROR, participantController.stopExperiment(ID_STRING, ID_STRING, httpServletRequest));
        verify(userService).getUserById(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService).simultaneousParticipation(ID);
        verify(participantService).updateParticipant(participantDTO);
        verify(userService, never()).saveUser(any());
        verify(httpServletRequest).getSession(false);
    }

    @Test
    public void testStopExperimentParticipantAlreadyFinished() {
        participantDTO.setStart(LocalDateTime.now());
        participantDTO.setEnd(LocalDateTime.now());
        when(userService.getUserById(ID)).thenReturn(userDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        assertEquals(ERROR, participantController.stopExperiment(ID_STRING, ID_STRING, httpServletRequest));
        verify(userService).getUserById(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(participantService, never()).updateParticipant(any());
        verify(userService, never()).saveUser(any());
        verify(httpServletRequest).getSession(false);
    }

    @Test
    public void testStopExperimentParticipantNotStarted() {
        when(userService.getUserById(ID)).thenReturn(userDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        assertEquals(ERROR, participantController.stopExperiment(ID_STRING, ID_STRING, httpServletRequest));
        verify(userService).getUserById(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(participantService, never()).updateParticipant(any());
        verify(userService, never()).saveUser(any());
        verify(httpServletRequest).getSession(false);
    }

    @Test
    public void testStopExperimentParticipantNotFound() {
        when(userService.getUserById(ID)).thenReturn(userDTO);
        when(participantService.getParticipant(ID, ID)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, participantController.stopExperiment(ID_STRING, ID_STRING, httpServletRequest));
        verify(userService).getUserById(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(participantService, never()).updateParticipant(any());
        verify(userService, never()).saveUser(any());
        verify(httpServletRequest).getSession(false);
    }

    @Test
    public void testStopExperimentParticipantUserAdmin() {
        when(httpServletRequest.isUserInRole(ROLE_ADMIN)).thenReturn(true);
        assertEquals(ERROR, participantController.stopExperiment(ID_STRING, ID_STRING, httpServletRequest));
        verify(userService, never()).getUserById(anyInt());
        verify(participantService, never()).getParticipant(anyInt(), anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(participantService, never()).updateParticipant(any());
        verify(userService, never()).saveUser(any());
        verify(httpServletRequest, never()).getSession(anyBoolean());
    }

    @Test
    public void testStopExperimentParticipantInvalidUserId() {
        assertEquals(ERROR, participantController.stopExperiment(ID_STRING, "0", httpServletRequest));
        verify(userService, never()).getUserById(anyInt());
        verify(participantService, never()).getParticipant(anyInt(), anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(participantService, never()).updateParticipant(any());
        verify(userService, never()).saveUser(any());
        verify(httpServletRequest, never()).getSession(anyBoolean());
    }

    @Test
    public void testStopExperimentParticipantInvalidExperimentId() {
        assertEquals(ERROR, participantController.stopExperiment("-1", ID_STRING, httpServletRequest));
        verify(userService, never()).getUserById(anyInt());
        verify(participantService, never()).getParticipant(anyInt(), anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(participantService, never()).updateParticipant(any());
        verify(userService, never()).saveUser(any());
        verify(httpServletRequest, never()).getSession(anyBoolean());
    }

    @Test
    public void testStopExperimentParticipantUserIdNull() {
        assertEquals(ERROR, participantController.stopExperiment(ID_STRING, null, httpServletRequest));
        verify(userService, never()).getUserById(anyInt());
        verify(participantService, never()).getParticipant(anyInt(), anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(participantService, never()).updateParticipant(any());
        verify(userService, never()).saveUser(any());
        verify(httpServletRequest, never()).getSession(anyBoolean());
    }

    @Test
    public void testStopExperimentParticipantExperimentIdNull() {
        assertEquals(ERROR, participantController.stopExperiment(null, ID_STRING, httpServletRequest));
        verify(userService, never()).getUserById(anyInt());
        verify(participantService, never()).getParticipant(anyInt(), anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(participantService, never()).updateParticipant(any());
        verify(userService, never()).saveUser(any());
        verify(httpServletRequest, never()).getSession(anyBoolean());
    }
}
