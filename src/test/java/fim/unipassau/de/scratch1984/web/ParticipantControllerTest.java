package fim.unipassau.de.scratch1984.web;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.application.service.MailService;
import fim.unipassau.de.scratch1984.application.service.ParticipantService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.web.controller.ParticipantController;
import fim.unipassau.de.scratch1984.web.dto.ExperimentDTO;
import fim.unipassau.de.scratch1984.web.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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

    private static final String ERROR = "redirect:/error";
    private static final String PARTICIPANT = "participant";
    private static final String REDIRECT_EXPERIMENT = "redirect:/experiment?id=";
    private static final String EMAIL = "participant@participant.de";
    private static final String BLANK = "   ";
    private static final String ID_STRING = "1";
    private static final int ID = 1;
    private final UserDTO userDTO = new UserDTO(PARTICIPANT, EMAIL, UserDTO.Role.ADMIN,
            UserDTO.Language.ENGLISH, "password", "secret");
    private final ExperimentDTO experimentDTO = new ExperimentDTO(ID, "title", "description", "info", true);

    @BeforeEach
    public void setup() {
        userDTO.setId(ID);
        userDTO.setUsername(PARTICIPANT);
        userDTO.setEmail(EMAIL);
        experimentDTO.setActive(true);
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
    public void testAddParticipant() throws MessagingException {
        when(userService.saveUser(userDTO)).thenReturn(userDTO);
        assertEquals(REDIRECT_EXPERIMENT + ID, participantController.addParticipant(ID_STRING, userDTO,
                bindingResult, httpServletRequest));
        verify(userService).saveUser(userDTO);
        verify(participantService).saveParticipant(userDTO.getId(), ID);
        verify(mailService).sendTemplateMessage(anyString(), any(), any(), any(), anyString(), any(), anyString());
        verify(bindingResult, never()).addError(any());
    }

    @Test
    public void testAddParticipantMessagingException() throws MessagingException {
        when(userService.saveUser(userDTO)).thenReturn(userDTO);
        doThrow(MessagingException.class).when(mailService).sendTemplateMessage(anyString(), any(), any(), any(),
                anyString(), any(), anyString());
        assertEquals(ERROR, participantController.addParticipant(ID_STRING, userDTO,
                bindingResult, httpServletRequest));
        verify(userService).saveUser(userDTO);
        verify(participantService).saveParticipant(userDTO.getId(), ID);
        verify(mailService, times(Constants.MAX_EMAIL_TRIES)).sendTemplateMessage(anyString(), any(), any(), any(),
                anyString(), any(), anyString());
        verify(bindingResult, never()).addError(any());
    }

    @Test
    public void testAddParticipantNotFound() throws MessagingException {
        when(userService.saveUser(userDTO)).thenReturn(userDTO);
        doThrow(NotFoundException.class).when(participantService).saveParticipant(userDTO.getId(), ID);
        assertEquals(ERROR, participantController.addParticipant(ID_STRING, userDTO, bindingResult,
                httpServletRequest));
        verify(userService).saveUser(userDTO);
        verify(participantService).saveParticipant(userDTO.getId(), ID);
        verify(mailService, never()).sendTemplateMessage(anyString(), any(), any(), any(), anyString(), any(),
                anyString());
        verify(bindingResult, never()).addError(any());
    }

    @Test
    public void testAddParticipantExistsEmail() throws MessagingException {
        when(userService.existsEmail(userDTO.getEmail())).thenReturn(true);
        when(bindingResult.hasErrors()).thenReturn(true);
        assertEquals(PARTICIPANT, participantController.addParticipant(ID_STRING, userDTO, bindingResult,
                httpServletRequest));
        verify(userService, never()).saveUser(userDTO);
        verify(participantService, never()).saveParticipant(userDTO.getId(), ID);
        verify(mailService, never()).sendTemplateMessage(anyString(), any(), any(), any(), anyString(), any(),
                anyString());
        verify(bindingResult).addError(any());
    }

    @Test
    public void testAddParticipantEmailInvalid() throws MessagingException {
        userDTO.setEmail(PARTICIPANT);
        when(bindingResult.hasErrors()).thenReturn(true);
        assertEquals(PARTICIPANT, participantController.addParticipant(ID_STRING, userDTO, bindingResult,
                httpServletRequest));
        verify(userService, never()).saveUser(userDTO);
        verify(participantService, never()).saveParticipant(userDTO.getId(), ID);
        verify(mailService, never()).sendTemplateMessage(anyString(), any(), any(), any(), anyString(), any(),
                anyString());
        verify(bindingResult).addError(any());
    }

    @Test
    public void testAddParticipantUsernameExists() throws MessagingException {
        when(userService.existsUser(PARTICIPANT)).thenReturn(true);
        when(bindingResult.hasErrors()).thenReturn(true);
        assertEquals(PARTICIPANT, participantController.addParticipant(ID_STRING, userDTO, bindingResult,
                httpServletRequest));
        verify(userService, never()).saveUser(userDTO);
        verify(participantService, never()).saveParticipant(userDTO.getId(), ID);
        verify(mailService, never()).sendTemplateMessage(anyString(), any(), any(), any(), anyString(), any(),
                anyString());
        verify(bindingResult).addError(any());
    }

    @Test
    public void testAddParticipantUsernameInvalid() throws MessagingException {
        userDTO.setUsername(BLANK);
        when(bindingResult.hasErrors()).thenReturn(true);
        assertEquals(PARTICIPANT, participantController.addParticipant(ID_STRING, userDTO, bindingResult,
                httpServletRequest));
        verify(userService, never()).saveUser(userDTO);
        verify(participantService, never()).saveParticipant(userDTO.getId(), ID);
        verify(mailService, never()).sendTemplateMessage(anyString(), any(), any(), any(), anyString(), any(),
                anyString());
        verify(bindingResult).addError(any());
    }

    @Test
    public void testAddParticipantExperimentIdInvalid() throws MessagingException {
        assertEquals(ERROR, participantController.addParticipant("0", userDTO, bindingResult,
                httpServletRequest));
        verify(userService, never()).saveUser(userDTO);
        verify(participantService, never()).saveParticipant(userDTO.getId(), ID);
        verify(mailService, never()).sendTemplateMessage(anyString(), any(), any(), any(), anyString(), any(),
                anyString());
        verify(bindingResult, never()).addError(any());
    }

    @Test
    public void testAddParticipantUsernameNull() throws MessagingException {
        userDTO.setUsername(null);
        assertEquals(ERROR, participantController.addParticipant(ID_STRING, userDTO, bindingResult,
                httpServletRequest));
        verify(userService, never()).saveUser(userDTO);
        verify(participantService, never()).saveParticipant(userDTO.getId(), ID);
        verify(mailService, never()).sendTemplateMessage(anyString(), any(), any(), any(), anyString(), any(),
                anyString());
        verify(bindingResult, never()).addError(any());
    }

    @Test
    public void testAddParticipantEmailNull() throws MessagingException {
        userDTO.setEmail(null);
        assertEquals(ERROR, participantController.addParticipant(ID_STRING, userDTO, bindingResult,
                httpServletRequest));
        verify(userService, never()).saveUser(userDTO);
        verify(participantService, never()).saveParticipant(userDTO.getId(), ID);
        verify(mailService, never()).sendTemplateMessage(anyString(), any(), any(), any(), anyString(), any(),
                anyString());
        verify(bindingResult, never()).addError(any());
    }

    @Test
    public void testAddParticipantExperimentIdNull() throws MessagingException {
        assertEquals(ERROR, participantController.addParticipant(null, userDTO, bindingResult,
                httpServletRequest));
        verify(userService, never()).saveUser(userDTO);
        verify(participantService, never()).saveParticipant(userDTO.getId(), ID);
        verify(mailService, never()).sendTemplateMessage(anyString(), any(), any(), any(), anyString(), any(),
                anyString());
        verify(bindingResult, never()).addError(any());
    }
}
