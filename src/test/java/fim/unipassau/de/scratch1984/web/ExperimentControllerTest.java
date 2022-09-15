package fim.unipassau.de.scratch1984.web;

import fim.unipassau.de.scratch1984.MailServerSetter;
import fim.unipassau.de.scratch1984.application.exception.IncompleteDataException;
import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.EventService;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.application.service.MailService;
import fim.unipassau.de.scratch1984.application.service.PageService;
import fim.unipassau.de.scratch1984.application.service.ParticipantService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.Participant;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.web.controller.ExperimentController;
import fim.unipassau.de.scratch1984.web.dto.ExperimentDTO;
import fim.unipassau.de.scratch1984.web.dto.ParticipantDTO;
import fim.unipassau.de.scratch1984.web.dto.PasswordDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
public class ExperimentControllerTest {

    @InjectMocks
    private ExperimentController experimentController;

    @Mock
    private ExperimentService experimentService;

    @Mock
    private UserService userService;

    @Mock
    private ParticipantService participantService;

    @Mock
    private PageService pageService;

    @Mock
    private MailService mailService;

    @Mock
    private EventService eventService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private ResourceBundle resourceBundle;

    @Mock
    private LocaleResolver localeResolver;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private MultipartFile file;

    private MockedStatic<SecurityContextHolder> securityContextHolder;
    private static final String TITLE = "My Experiment";
    private static final String DESCRIPTION = "A description";
    private static final String INFO = "Some info text";
    private static final String POSTSCRIPT = "Some postscript";
    private static final String ERROR = "redirect:/error";
    private static final String EXPERIMENT = "experiment";
    private static final String EXPERIMENT_EDIT = "experiment-edit";
    private static final String REDIRECT_EXPERIMENT = "redirect:/experiment?id=";
    private static final String INVALID = "redirect:/experiment?invalid=true&id=";
    private static final String SUCCESS = "redirect:/?success=true";
    private static final String REDIRECT_SECRET = "redirect:/secret?user=";
    private static final String REDIRECT_SECRET_LIST = "redirect:/secret/list?experiment=";
    private static final String EXPERIMENT_PARAM = "&experiment=";
    private static final String BLANK = "    ";
    private static final String EXPERIMENT_DTO = "experimentDTO";
    private static final String ID_STRING = "1";
    private static final String INVALID_ID = "-1";
    private static final String ADMIN = "ROLE_ADMIN";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "password";
    private static final String EMAIL = "participant@part.de";
    private static final String LONG_PASSWORD = "VeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeryLongPassword";
    private static final String PARTICIPANTS = "participants";
    private static final String CURRENT = "3";
    private static final String LAST = "4";
    private static final String FILETYPE = "application/octet-stream";
    private static final String FILENAME = "project.sb3";
    private static final String ERROR_ATTRIBUTE = "error";
    private static final int LAST_PAGE = 3;
    private static final int ID = 1;
    private static final String GUI_URL = "scratch";
    private static final byte[] CONTENT = new byte[]{1, 2, 3};
    private final ExperimentDTO experimentDTO = new ExperimentDTO(ID, TITLE, DESCRIPTION, INFO, POSTSCRIPT, false,
            GUI_URL);
    private final UserDTO userDTO = new UserDTO(USERNAME, "admin1@admin.de", UserDTO.Role.ADMIN,
            UserDTO.Language.ENGLISH, PASSWORD, "secret1");
    private final UserDTO participant = new UserDTO(PARTICIPANTS, EMAIL, UserDTO.Role.PARTICIPANT,
            UserDTO.Language.ENGLISH, "user", null);
    private final Page<Participant> participants = new PageImpl<>(getParticipants(5));
    private final List<UserDTO> userDTOS = new ArrayList<>();
    private final ParticipantDTO participantDTO = new ParticipantDTO(ID, ID);
    private final PasswordDTO passwordDTO = new PasswordDTO(PASSWORD);

    @BeforeEach
    public void setup() {
        userDTO.setId(ID);
        participant.setId(ID + 1);
        participant.setSecret(null);
        userDTO.setActive(true);
        userDTO.setSecret("secret1");
        participant.setEmail(EMAIL);
        experimentDTO.setActive(false);
        experimentDTO.setId(ID);
        experimentDTO.setTitle(TITLE);
        experimentDTO.setDescription(DESCRIPTION);
        experimentDTO.setPostscript(POSTSCRIPT);
        experimentDTO.setInfo(INFO);
        securityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        userDTOS.add(participant);
        userDTOS.add(userDTO);
        participantDTO.setStart(null);
        participantDTO.setEnd(null);
        passwordDTO.setPassword(PASSWORD);
    }

    @AfterEach
    public void cleanup() {
        securityContextHolder.close();
    }

    @Test
    public void testGetExperiment() {
        when(httpServletRequest.isUserInRole(ADMIN)).thenReturn(true);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(pageService.getParticipantPage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        String returnString = experimentController.getExperiment(ID_STRING, model, httpServletRequest);
        assertEquals(EXPERIMENT, returnString);
        verify(httpServletRequest).isUserInRole(ADMIN);
        verify(experimentService).getExperiment(ID);
        verify(pageService).getLastParticipantPage(ID);
        verify(pageService).getParticipantPage(anyInt(), any(PageRequest.class));
        verify(experimentService).hasProjectFile(ID);
        verify(model).addAttribute(EXPERIMENT_DTO, experimentDTO);
        verify(model).addAttribute(PARTICIPANTS, participants);
    }

    @Test
    public void testGetExperimentProjectFile() {
        when(httpServletRequest.isUserInRole(ADMIN)).thenReturn(true);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(pageService.getParticipantPage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        when(experimentService.hasProjectFile(ID)).thenReturn(true);
        String returnString = experimentController.getExperiment(ID_STRING, model, httpServletRequest);
        assertEquals(EXPERIMENT, returnString);
        verify(httpServletRequest).isUserInRole(ADMIN);
        verify(experimentService).getExperiment(ID);
        verify(pageService).getLastParticipantPage(ID);
        verify(pageService).getParticipantPage(anyInt(), any(PageRequest.class));
        verify(experimentService).hasProjectFile(ID);
        verify(model).addAttribute(EXPERIMENT_DTO, experimentDTO);
        verify(model).addAttribute(PARTICIPANTS, participants);
        verify(model).addAttribute("project", true);
    }

    @Test
    public void testGetExperimentNotFound() {
        when(httpServletRequest.isUserInRole(ADMIN)).thenReturn(true);
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        String returnString = experimentController.getExperiment(ID_STRING, model, httpServletRequest);
        assertEquals(ERROR, returnString);
        verify(httpServletRequest).isUserInRole(ADMIN);
        verify(experimentService).getExperiment(ID);
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testGetExperimentParticipant() {
        experimentDTO.setActive(true);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        when(pageService.getParticipantPage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        assertEquals(EXPERIMENT, experimentController.getExperiment(ID_STRING, model, httpServletRequest));
        verify(securityContext).getAuthentication();
        verify(authentication).getName();
        verify(httpServletRequest).isUserInRole(ADMIN);
        verify(experimentService).getExperiment(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(pageService).getParticipantPage(anyInt(), any(PageRequest.class));
        verify(experimentService).hasProjectFile(ID);
        verify(model).addAttribute(EXPERIMENT_DTO, experimentDTO);
        verify(model).addAttribute("participant", participantDTO);
        verify(model, never()).addAttribute("secret", false);
        verify(model).addAttribute(PARTICIPANTS, participants);
    }

    @Test
    public void testGetExperimentParticipantSecretNull() {
        userDTO.setSecret(null);
        experimentDTO.setActive(true);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        when(pageService.getParticipantPage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        assertEquals(EXPERIMENT, experimentController.getExperiment(ID_STRING, model, httpServletRequest));
        verify(securityContext).getAuthentication();
        verify(authentication).getName();
        verify(httpServletRequest).isUserInRole(ADMIN);
        verify(experimentService).getExperiment(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(pageService).getParticipantPage(anyInt(), any(PageRequest.class));
        verify(experimentService).hasProjectFile(ID);
        verify(model).addAttribute(EXPERIMENT_DTO, experimentDTO);
        verify(model).addAttribute("participant", participantDTO);
        verify(model).addAttribute("secret", false);
        verify(model).addAttribute(PARTICIPANTS, participants);
    }

    @Test
    public void testGetExperimentUserNotFound() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenThrow(NotFoundException.class);
        String returnString = experimentController.getExperiment(ID_STRING, model, httpServletRequest);
        assertEquals(ERROR, returnString);
        verify(securityContext).getAuthentication();
        verify(authentication, times(2)).getName();
        verify(userService, never()).existsParticipant(userDTO.getId(), ID);
        verify(httpServletRequest).isUserInRole(ADMIN);
        verify(experimentService, never()).getExperiment(ID);
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testGetExperimentNoParticipant() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(participantService.getParticipant(ID, ID)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, experimentController.getExperiment(ID_STRING, model, httpServletRequest));
        verify(securityContext).getAuthentication();
        verify(authentication, times(2)).getName();
        verify(participantService).getParticipant(ID, ID);
        verify(httpServletRequest).isUserInRole(ADMIN);
        verify(experimentService, never()).getExperiment(ID);
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testGetExperimentParticipantInactive() {
        userDTO.setActive(false);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        assertEquals(ERROR, experimentController.getExperiment(ID_STRING, model, httpServletRequest));
        verify(securityContext).getAuthentication();
        verify(authentication).getName();
        verify(httpServletRequest).isUserInRole(ADMIN);
        verify(participantService, never()).getParticipant(ID, ID);
        verify(experimentService, never()).getExperiment(ID);
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testGetExperimentIdNull() {
        String returnString = experimentController.getExperiment(null, model, httpServletRequest);
        assertEquals(ERROR, returnString);
        verify(httpServletRequest, never()).isUserInRole(anyString());
        verify(experimentService, never()).getExperiment(ID);
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testGetExperimentIdInvalid() {
        String returnString = experimentController.getExperiment(INVALID_ID, model, httpServletRequest);
        assertEquals(ERROR, returnString);
        verify(httpServletRequest, never()).isUserInRole(anyString());
        verify(experimentService, never()).getExperiment(ID);
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testGetExperimentIdNotANumber() {
        String returnString = experimentController.getExperiment(BLANK, model, httpServletRequest);
        assertEquals(ERROR, returnString);
        verify(httpServletRequest, never()).isUserInRole(anyString());
        verify(experimentService, never()).getExperiment(ID);
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testGetExperimentForm() {
        String returnString = experimentController.getExperimentForm(experimentDTO);
        assertEquals(EXPERIMENT_EDIT, returnString);
    }

    @Test
    public void testGetExperimentEditForm() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        String returnString = experimentController.getEditExperimentForm(ID_STRING, model);
        assertEquals(EXPERIMENT_EDIT, returnString);
        verify(experimentService).getExperiment(ID);
        verify(model).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testGetExperimentEditFormNotFound() {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        String returnString = experimentController.getEditExperimentForm(ID_STRING, model);
        assertEquals(ERROR, returnString);
        verify(experimentService).getExperiment(ID);
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testGetExperimentEditFormIdInvalid() {
        String returnString = experimentController.getEditExperimentForm(INVALID_ID, model);
        assertEquals(ERROR, returnString);
        verify(experimentService, never()).getExperiment(ID);
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testEditExperiment() {
        when(experimentService.saveExperiment(experimentDTO)).thenReturn(experimentDTO);
        String returnString = experimentController.editExperiment(experimentDTO, bindingResult);
        assertEquals(REDIRECT_EXPERIMENT + ID, returnString);
        verify(bindingResult, never()).addError(any());
        verify(experimentService).existsExperiment(experimentDTO.getTitle(), experimentDTO.getId());
        verify(experimentService).saveExperiment(experimentDTO);
    }

    @Test
    public void testEditExperimentTitleExists() {
        when(experimentService.existsExperiment(experimentDTO.getTitle(), experimentDTO.getId())).thenReturn(true);
        when(bindingResult.hasErrors()).thenReturn(true);
        String returnString = experimentController.editExperiment(experimentDTO, bindingResult);
        assertEquals(EXPERIMENT_EDIT, returnString);
        verify(bindingResult).addError(any());
        verify(experimentService).existsExperiment(experimentDTO.getTitle(), experimentDTO.getId());
        verify(experimentService, never()).saveExperiment(any());
    }

    @Test
    public void testEditExperimentCreate() {
        experimentDTO.setId(null);
        experimentDTO.setPostscript(BLANK);
        when(experimentService.saveExperiment(experimentDTO)).thenReturn(experimentDTO);
        String returnString = experimentController.editExperiment(experimentDTO, bindingResult);
        assertEquals(REDIRECT_EXPERIMENT + experimentDTO.getId(), returnString);
        verify(bindingResult, never()).addError(any());
        verify(experimentService).existsExperiment(experimentDTO.getTitle());
        verify(experimentService).saveExperiment(experimentDTO);
    }

    @Test
    public void testEditExperimentCreateTitleExists() {
        experimentDTO.setId(null);
        when(experimentService.existsExperiment(experimentDTO.getTitle())).thenReturn(true);
        when(bindingResult.hasErrors()).thenReturn(true);
        String returnString = experimentController.editExperiment(experimentDTO, bindingResult);
        assertEquals(EXPERIMENT_EDIT, returnString);
        verify(bindingResult).addError(any());
        verify(experimentService).existsExperiment(experimentDTO.getTitle());
        verify(experimentService, never()).saveExperiment(any());
    }

    @Test
    public void testEditExperimentTitleTooLong() {
        experimentDTO.setPostscript(null);
        experimentDTO.setTitle(createLongString(200).toString());
        when(bindingResult.hasErrors()).thenReturn(true);
        String returnString = experimentController.editExperiment(experimentDTO, bindingResult);
        assertEquals(EXPERIMENT_EDIT, returnString);
        verify(bindingResult).addError(any());
        verify(experimentService).existsExperiment(experimentDTO.getTitle(), experimentDTO.getId());
        verify(experimentService, never()).saveExperiment(any());
    }

    @Test
    public void testEditExperimentTitleAndDescriptionNull() {
        experimentDTO.setTitle(null);
        experimentDTO.setDescription(null);
        when(bindingResult.hasErrors()).thenReturn(true);
        String returnString = experimentController.editExperiment(experimentDTO, bindingResult);
        assertEquals(EXPERIMENT_EDIT, returnString);
        verify(bindingResult, times(2)).addError(any());
        verify(experimentService).existsExperiment(experimentDTO.getTitle(), experimentDTO.getId());
        verify(experimentService, never()).saveExperiment(any());
    }

    @Test
    public void testEditExperimentTitleAndDescriptionBlank() {
        experimentDTO.setTitle(BLANK);
        experimentDTO.setDescription(BLANK);
        when(bindingResult.hasErrors()).thenReturn(true);
        String returnString = experimentController.editExperiment(experimentDTO, bindingResult);
        assertEquals(EXPERIMENT_EDIT, returnString);
        verify(bindingResult, times(2)).addError(any());
        verify(experimentService).existsExperiment(experimentDTO.getTitle(), experimentDTO.getId());
        verify(experimentService, never()).saveExperiment(any());
    }

    @Test
    public void testEditExperimentDescriptionTooLong() {
        experimentDTO.setDescription(createLongString(2000).toString());
        when(bindingResult.hasErrors()).thenReturn(true);
        String returnString = experimentController.editExperiment(experimentDTO, bindingResult);
        assertEquals(EXPERIMENT_EDIT, returnString);
        verify(bindingResult).addError(any());
        verify(experimentService).existsExperiment(experimentDTO.getTitle(), experimentDTO.getId());
        verify(experimentService, never()).saveExperiment(any());
    }

    @Test
    public void testEditExperimentInfoTooLong() {
        experimentDTO.setInfo(createLongString(60000).toString());
        when(bindingResult.hasErrors()).thenReturn(true);
        assertEquals(EXPERIMENT_EDIT, experimentController.editExperiment(experimentDTO, bindingResult));
        verify(bindingResult).addError(any());
        verify(experimentService).existsExperiment(experimentDTO.getTitle(), experimentDTO.getId());
        verify(experimentService, never()).saveExperiment(any());
    }

    @Test
    public void testEditExperimentPostscriptTooLong() {
        experimentDTO.setPostscript(createLongString(1001).toString());
        when(bindingResult.hasErrors()).thenReturn(true);
        assertEquals(EXPERIMENT_EDIT, experimentController.editExperiment(experimentDTO, bindingResult));
        verify(bindingResult).addError(any());
        verify(experimentService).existsExperiment(experimentDTO.getTitle(), experimentDTO.getId());
        verify(experimentService, never()).saveExperiment(any());
    }

    @Test
    public void testDeleteExperiment() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(userService.matchesPassword(PASSWORD, PASSWORD)).thenReturn(true);
        assertEquals(SUCCESS, experimentController.deleteExperiment(passwordDTO, ID_STRING));
        verify(authentication, times(2)).getName();
        verify(userService).getUser(USERNAME);
        verify(userService).matchesPassword(PASSWORD, PASSWORD);
        verify(experimentService).deleteExperiment(ID);
    }

    @Test
    public void testDeleteExperimentPasswordNotMatching() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        assertEquals(INVALID + ID, experimentController.deleteExperiment(passwordDTO, ID_STRING));
        verify(authentication, times(2)).getName();
        verify(userService).getUser(USERNAME);
        verify(userService).matchesPassword(PASSWORD, PASSWORD);
        verify(experimentService, never()).deleteExperiment(anyInt());
    }

    @Test
    public void testDeleteExperimentPasswordTooLong() {
        passwordDTO.setPassword(LONG_PASSWORD);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        assertEquals(INVALID + ID, experimentController.deleteExperiment(passwordDTO, ID_STRING));
        verify(authentication, times(2)).getName();
        verify(userService).getUser(USERNAME);
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(experimentService, never()).deleteExperiment(anyInt());
    }

    @Test
    public void testDeleteExperimentNotFound() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, experimentController.deleteExperiment(passwordDTO, ID_STRING));
        verify(authentication, times(2)).getName();
        verify(userService).getUser(USERNAME);
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(experimentService, never()).deleteExperiment(anyInt());
    }

    @Test
    public void testDeleteExperimentAuthenticationNameNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        assertEquals(ERROR, experimentController.deleteExperiment(passwordDTO, ID_STRING));
        verify(authentication).getName();
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(experimentService, never()).deleteExperiment(anyInt());
    }

    @Test
    public void testDeleteExperimentInvalidId() {
        assertEquals(ERROR, experimentController.deleteExperiment(passwordDTO, INVALID_ID));
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(experimentService, never()).deleteExperiment(anyInt());
    }

    @Test
    public void testDeleteExperimentPasswordNull() {
        passwordDTO.setPassword(null);
        assertEquals(ERROR, experimentController.deleteExperiment(passwordDTO, INVALID_ID));
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(experimentService, never()).deleteExperiment(anyInt());
    }

    @Test
    public void testDeleteExperimentIdNull() {
        assertEquals(ERROR, experimentController.deleteExperiment(passwordDTO, null));
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(experimentService, never()).deleteExperiment(anyInt());
    }

    @Test
    public void testChangeExperimentStatusOpen() {
        MailServerSetter.setMailServer(true);
        when(experimentService.changeExperimentStatus(true, ID)).thenReturn(experimentDTO);
        when(userService.reactivateUserAccounts(experimentDTO.getId())).thenReturn(userDTOS);
        when(mailService.sendEmail(anyString(), anyString(), any(), anyString())).thenReturn(true).thenReturn(false);
        when(pageService.getParticipantPage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        assertEquals(EXPERIMENT, experimentController.changeExperimentStatus("open", ID_STRING, model));
        verify(experimentService).changeExperimentStatus(true, ID);
        verify(userService).reactivateUserAccounts(ID);
        verify(mailService, times(2)).sendEmail(anyString(), anyString(), any(), anyString());
        verify(pageService).getLastParticipantPage(ID);
        verify(pageService).getParticipantPage(anyInt(), any(PageRequest.class));
        verify(model).addAttribute(EXPERIMENT_DTO, experimentDTO);
        verify(model).addAttribute(PARTICIPANTS, participants);
    }

    @Test
    public void testChangeExperimentStatusOpenNoMailServer() {
        MailServerSetter.setMailServer(false);
        when(experimentService.changeExperimentStatus(true, ID)).thenReturn(experimentDTO);
        when(userService.reactivateUserAccounts(experimentDTO.getId())).thenReturn(userDTOS);
        assertEquals(REDIRECT_SECRET_LIST + ID, experimentController.changeExperimentStatus("open",
                ID_STRING, model));
        verify(experimentService).changeExperimentStatus(true, ID);
        verify(userService).reactivateUserAccounts(ID);
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(pageService, never()).getLastParticipantPage(anyInt());
        verify(pageService, never()).getParticipantPage(anyInt(), any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testChangeExperimentStatusClose() {
        when(experimentService.changeExperimentStatus(false, ID)).thenReturn(experimentDTO);
        when(pageService.getParticipantPage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        assertEquals(EXPERIMENT, experimentController.changeExperimentStatus("close", ID_STRING, model));
        verify(experimentService).changeExperimentStatus(false, ID);
        verify(participantService).deactivateParticipantAccounts(ID);
        verify(pageService).getLastParticipantPage(ID);
        verify(pageService).getParticipantPage(anyInt(), any(PageRequest.class));
        verify(model).addAttribute(EXPERIMENT_DTO, experimentDTO);
        verify(model).addAttribute(PARTICIPANTS, participants);
    }

    @Test
    public void testChangeExperimentStatusCloseInfoNull() {
        experimentDTO.setInfo(null);
        when(experimentService.changeExperimentStatus(false, ID)).thenReturn(experimentDTO);
        when(pageService.getParticipantPage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        assertEquals(EXPERIMENT, experimentController.changeExperimentStatus("close", ID_STRING, model));
        verify(experimentService).changeExperimentStatus(false, ID);
        verify(participantService).deactivateParticipantAccounts(ID);
        verify(pageService).getLastParticipantPage(ID);
        verify(pageService).getParticipantPage(anyInt(), any(PageRequest.class));
        verify(model).addAttribute(EXPERIMENT_DTO, experimentDTO);
        verify(model).addAttribute(PARTICIPANTS, participants);
    }

    @Test
    public void testChangeExperimentStatusInvalid() {
        assertEquals(ERROR, experimentController.changeExperimentStatus("blabla", ID_STRING, model));
        verify(experimentService, never()).changeExperimentStatus(anyBoolean(), anyInt());
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testChangeExperimentStatusOpenNotFound() {
        when(experimentService.changeExperimentStatus(true, ID)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, experimentController.changeExperimentStatus("open", ID_STRING, model));
        verify(experimentService).changeExperimentStatus(true, ID);
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testChangeExperimentStatusStatusNull() {
        assertEquals(ERROR, experimentController.changeExperimentStatus(null, ID_STRING, model));
        verify(experimentService, never()).changeExperimentStatus(anyBoolean(), anyInt());
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testChangeExperimentStatusIdNull() {
        assertEquals(ERROR, experimentController.changeExperimentStatus("open", null, model));
        verify(experimentService, never()).changeExperimentStatus(anyBoolean(), anyInt());
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testSearchForUser() {
        MailServerSetter.setMailServer(true);
        experimentDTO.setActive(true);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANTS)).thenReturn(participant);
        when(userService.updateUser(participant)).thenReturn(participant);
        when(mailService.sendEmail(anyString(), anyString(), any(), anyString())).thenReturn(true);
        assertEquals(REDIRECT_EXPERIMENT + ID, experimentController.searchForUser(PARTICIPANTS, ID_STRING,
                model, httpServletRequest));
        verify(experimentService).getExperiment(ID);
        verify(userService).getUserByUsernameOrEmail(PARTICIPANTS);
        verify(userService).updateUser(participant);
        verify(participantService).saveParticipant(participant.getId(), ID);
        verify(mailService).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, never()).addAttribute(any(), any());
    }

    @Test
    public void testSearchForUserNoMailServer() {
        MailServerSetter.setMailServer(false);
        experimentDTO.setActive(true);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANTS)).thenReturn(participant);
        when(userService.updateUser(participant)).thenReturn(participant);
        assertEquals(REDIRECT_SECRET + participant.getId() + EXPERIMENT_PARAM + ID,
                experimentController.searchForUser(PARTICIPANTS, ID_STRING, model, httpServletRequest));
        verify(experimentService).getExperiment(ID);
        verify(userService).getUserByUsernameOrEmail(PARTICIPANTS);
        verify(userService).updateUser(participant);
        verify(participantService).saveParticipant(participant.getId(), ID);
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, never()).addAttribute(any(), any());
    }

    @Test
    public void testSearchForUserSecretNull() {
        MailServerSetter.setMailServer(true);
        experimentDTO.setActive(true);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANTS)).thenReturn(participant);
        when(userService.updateUser(participant)).thenReturn(participant);
        when(mailService.sendEmail(anyString(), anyString(), any(), anyString())).thenReturn(true);
        assertEquals(REDIRECT_EXPERIMENT + ID, experimentController.searchForUser(PARTICIPANTS, ID_STRING,
                model, httpServletRequest));
        verify(experimentService).getExperiment(ID);
        verify(userService).getUserByUsernameOrEmail(PARTICIPANTS);
        verify(userService).updateUser(participant);
        verify(participantService).saveParticipant(participant.getId(), ID);
        verify(mailService).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, never()).addAttribute(any(), any());
    }

    @Test
    public void testSearchForUserEmailNotSent() {
        MailServerSetter.setMailServer(true);
        experimentDTO.setActive(true);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANTS)).thenReturn(participant);
        when(userService.updateUser(participant)).thenReturn(participant);
        assertEquals(ERROR, experimentController.searchForUser(PARTICIPANTS, ID_STRING, model, httpServletRequest));
        verify(experimentService).getExperiment(ID);
        verify(userService).getUserByUsernameOrEmail(PARTICIPANTS);
        verify(userService).updateUser(participant);
        verify(participantService).saveParticipant(participant.getId(), ID);
        verify(mailService).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, never()).addAttribute(any(), any());
    }

    @Test
    public void testSearchForUserEmailNull() {
        MailServerSetter.setMailServer(true);
        participant.setEmail(null);
        experimentDTO.setActive(true);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANTS)).thenReturn(participant);
        when(userService.updateUser(participant)).thenReturn(participant);
        assertEquals(ERROR, experimentController.searchForUser(PARTICIPANTS, ID_STRING, model, httpServletRequest));
        verify(experimentService).getExperiment(ID);
        verify(userService).getUserByUsernameOrEmail(PARTICIPANTS);
        verify(userService).updateUser(participant);
        verify(participantService).saveParticipant(participant.getId(), ID);
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, never()).addAttribute(any(), any());
    }

    @Test
    public void testSearchForUserSaveParticipantNotFound() {
        experimentDTO.setActive(true);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANTS)).thenReturn(participant);
        when(userService.updateUser(participant)).thenReturn(participant);
        doThrow(NotFoundException.class).when(participantService).saveParticipant(participant.getId(), ID);
        assertEquals(ERROR, experimentController.searchForUser(PARTICIPANTS, ID_STRING, model, httpServletRequest));
        verify(experimentService).getExperiment(ID);
        verify(userService).getUserByUsernameOrEmail(PARTICIPANTS);
        verify(userService).updateUser(participant);
        verify(participantService).saveParticipant(participant.getId(), ID);
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, never()).addAttribute(any(), any());
    }

    @Test
    public void testSearchForUserExperimentInactive() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANTS)).thenReturn(participant);
        when(model.getAttribute("error")).thenReturn("error");
        assertEquals(EXPERIMENT, experimentController.searchForUser(PARTICIPANTS, ID_STRING, model, httpServletRequest));
        verify(experimentService).getExperiment(ID);
        verify(userService).getUserByUsernameOrEmail(PARTICIPANTS);
        verify(userService, never()).updateUser(participant);
        verify(participantService, never()).saveParticipant(participant.getId(), ID);
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, times(6)).addAttribute(anyString(), any());
    }

    @Test
    public void testSearchForUserSecretNotNull() {
        participant.setSecret("secret");
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANTS)).thenReturn(participant);
        when(model.getAttribute("error")).thenReturn("error");
        assertEquals(EXPERIMENT, experimentController.searchForUser(PARTICIPANTS, ID_STRING, model,
                httpServletRequest));
        verify(experimentService).getExperiment(ID);
        verify(userService).getUserByUsernameOrEmail(PARTICIPANTS);
        verify(userService, never()).updateUser(participant);
        verify(participantService, never()).saveParticipant(participant.getId(), ID);
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, times(6)).addAttribute(anyString(), any());
    }

    @Test
    public void testSearchForUserParticipantExists() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANTS)).thenReturn(participant);
        when(userService.existsParticipant(participant.getId(), ID)).thenReturn(true);
        when(model.getAttribute("error")).thenReturn("error");
        assertEquals(EXPERIMENT, experimentController.searchForUser(PARTICIPANTS, ID_STRING, model,
                httpServletRequest));
        verify(experimentService).getExperiment(ID);
        verify(userService).getUserByUsernameOrEmail(PARTICIPANTS);
        verify(userService, never()).updateUser(participant);
        verify(participantService, never()).saveParticipant(participant.getId(), ID);
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, times(6)).addAttribute(anyString(), any());
    }

    @Test
    public void testSearchForUserAdmin() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(USERNAME)).thenReturn(userDTO);
        when(model.getAttribute("error")).thenReturn("error");
        assertEquals(EXPERIMENT, experimentController.searchForUser(USERNAME, ID_STRING, model, httpServletRequest));
        verify(experimentService).getExperiment(ID);
        verify(userService).getUserByUsernameOrEmail(USERNAME);
        verify(userService, never()).updateUser(any());
        verify(participantService, never()).saveParticipant(anyInt(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, times(6)).addAttribute(anyString(), any());
    }

    @Test
    public void testSearchForUserNull() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(model.getAttribute("error")).thenReturn("error");
        assertEquals(EXPERIMENT, experimentController.searchForUser(PARTICIPANTS, ID_STRING, model,
                httpServletRequest));
        verify(experimentService).getExperiment(ID);
        verify(userService).getUserByUsernameOrEmail(PARTICIPANTS);
        verify(userService, never()).updateUser(any());
        verify(participantService, never()).saveParticipant(anyInt(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, times(6)).addAttribute(anyString(), any());
    }

    @Test
    public void testSearchForUserQueryInvalid() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        assertEquals(EXPERIMENT, experimentController.searchForUser(BLANK, ID_STRING, model, httpServletRequest));
        verify(experimentService).getExperiment(ID);
        verify(userService, never()).getUserByUsernameOrEmail(anyString());
        verify(userService, never()).updateUser(any());
        verify(participantService, never()).saveParticipant(anyInt(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, times(6)).addAttribute(anyString(), any());
    }

    @Test
    public void testSearchForUserExperimentNotFound() {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, experimentController.searchForUser(PARTICIPANTS, ID_STRING, model, httpServletRequest));
        verify(experimentService).getExperiment(ID);
        verify(userService, never()).getUserByUsernameOrEmail(anyString());
        verify(userService, never()).updateUser(any());
        verify(participantService, never()).saveParticipant(anyInt(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testSearchForUserInvalidId() {
        assertEquals(ERROR, experimentController.searchForUser(PARTICIPANTS, BLANK, model, httpServletRequest));
        verify(experimentService, never()).getExperiment(ID);
        verify(userService, never()).getUserByUsernameOrEmail(anyString());
        verify(userService, never()).updateUser(any());
        verify(participantService, never()).saveParticipant(anyInt(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetNextPage() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(pageService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        when(pageService.getParticipantPage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        assertEquals(EXPERIMENT, experimentController.getNextPage(ID_STRING, CURRENT, model));
        verify(experimentService).getExperiment(ID);
        verify(pageService).getLastParticipantPage(ID);
        verify(pageService).getParticipantPage(anyInt(), any(PageRequest.class));
        verify(model, times(5)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetNextPageCurrentPageIsLastPage() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(pageService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        assertEquals(ERROR, experimentController.getNextPage(ID_STRING, LAST, model));
        verify(experimentService).getExperiment(ID);
        verify(pageService).getLastParticipantPage(ID);
        verify(pageService, never()).getParticipantPage(anyInt(), any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetNextPageNotFound() {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, experimentController.getNextPage(ID_STRING, CURRENT, model));
        verify(experimentService).getExperiment(ID);
        verify(pageService, never()).getLastParticipantPage(ID);
        verify(pageService, never()).getParticipantPage(anyInt(), any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetNextPageInvalidId() {
        assertEquals(ERROR, experimentController.getNextPage(null, CURRENT, model));
        verify(experimentService, never()).getExperiment(ID);
        verify(pageService, never()).getLastParticipantPage(ID);
        verify(pageService, never()).getParticipantPage(anyInt(), any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetNextPageInvalidCurrent() {
        assertEquals(ERROR, experimentController.getNextPage(ID_STRING, BLANK, model));
        verify(experimentService, never()).getExperiment(ID);
        verify(pageService, never()).getLastParticipantPage(ID);
        verify(pageService, never()).getParticipantPage(anyInt(), any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetNextPageCurrentNull() {
        assertEquals(ERROR, experimentController.getNextPage(ID_STRING, null, model));
        verify(experimentService, never()).getExperiment(ID);
        verify(pageService, never()).getLastParticipantPage(ID);
        verify(pageService, never()).getParticipantPage(anyInt(), any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetPreviousPage() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(pageService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        when(pageService.getParticipantPage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        assertEquals(EXPERIMENT, experimentController.getPreviousPage(ID_STRING, CURRENT, model));
        verify(experimentService).getExperiment(ID);
        verify(pageService).getLastParticipantPage(ID);
        verify(pageService).getParticipantPage(anyInt(), any(PageRequest.class));
        verify(model, times(5)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetPreviousPageCurrentPageBiggerLastPage() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(pageService.getLastParticipantPage(ID)).thenReturn(1);
        assertEquals(ERROR, experimentController.getPreviousPage(ID_STRING, LAST, model));
        verify(experimentService).getExperiment(ID);
        verify(pageService).getLastParticipantPage(ID);
        verify(pageService, never()).getParticipantPage(anyInt(), any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetPreviousPageNotFound() {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, experimentController.getPreviousPage(ID_STRING, CURRENT, model));
        verify(experimentService).getExperiment(ID);
        verify(pageService, never()).getLastParticipantPage(ID);
        verify(pageService, never()).getParticipantPage(anyInt(), any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetPreviousPageInvalidId() {
        assertEquals(ERROR, experimentController.getPreviousPage(BLANK, CURRENT, model));
        verify(experimentService, never()).getExperiment(ID);
        verify(pageService, never()).getLastParticipantPage(ID);
        verify(pageService, never()).getParticipantPage(anyInt(), any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetPreviousPageInvalidCurrent() {
        assertEquals(ERROR, experimentController.getPreviousPage(ID_STRING, "-5", model));
        verify(experimentService, never()).getExperiment(ID);
        verify(pageService, never()).getLastParticipantPage(ID);
        verify(pageService, never()).getParticipantPage(anyInt(), any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetPreviousPageCurrentNull() {
        assertEquals(ERROR, experimentController.getPreviousPage(ID_STRING, null, model));
        verify(experimentService, never()).getExperiment(ID);
        verify(pageService, never()).getLastParticipantPage(ID);
        verify(pageService, never()).getParticipantPage(anyInt(), any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetFirstPage() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(pageService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        when(pageService.getParticipantPage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        assertEquals(EXPERIMENT, experimentController.getFirstPage(ID_STRING, model));
        verify(experimentService).getExperiment(ID);
        verify(pageService).getLastParticipantPage(ID);
        verify(pageService).getParticipantPage(anyInt(), any(PageRequest.class));
        verify(model, times(5)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetFirstPageNotFound() {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, experimentController.getFirstPage(ID_STRING, model));
        verify(experimentService).getExperiment(ID);
        verify(pageService, never()).getLastParticipantPage(ID);
        verify(pageService, never()).getParticipantPage(anyInt(), any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetFirstPageInvalidId() {
        assertEquals(ERROR, experimentController.getFirstPage("-1", model));
        verify(experimentService, never()).getExperiment(ID);
        verify(pageService, never()).getLastParticipantPage(ID);
        verify(pageService, never()).getParticipantPage(anyInt(), any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetLastPage() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(pageService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        when(pageService.getParticipantPage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        assertEquals(EXPERIMENT, experimentController.getLastPage(ID_STRING, model));
        verify(experimentService).getExperiment(ID);
        verify(pageService, times(2)).getLastParticipantPage(ID);
        verify(pageService).getParticipantPage(anyInt(), any(PageRequest.class));
        verify(model, times(5)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetLastPageNotFound() {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, experimentController.getLastPage(ID_STRING, model));
        verify(experimentService).getExperiment(ID);
        verify(pageService, never()).getLastParticipantPage(ID);
        verify(pageService, never()).getParticipantPage(anyInt(), any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetLastPageInvalidId() {
        assertEquals(ERROR, experimentController.getLastPage(BLANK, model));
        verify(experimentService, never()).getExperiment(ID);
        verify(pageService, never()).getLastParticipantPage(ID);
        verify(pageService, never()).getParticipantPage(anyInt(), any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDownloadCSVFile() throws IOException {
        when(eventService.getBlockEventData(ID)).thenReturn(new ArrayList<>());
        when(eventService.getClickEventData(ID)).thenReturn(new ArrayList<>());
        when(eventService.getResourceEventData(ID)).thenReturn(new ArrayList<>());
        when(eventService.getBlockEventCount(ID)).thenReturn(new ArrayList<>());
        when(eventService.getClickEventCount(ID)).thenReturn(new ArrayList<>());
        when(eventService.getResourceEventCount(ID)).thenReturn(new ArrayList<>());
        when(eventService.getCodesDataForExperiment(ID)).thenReturn(new ArrayList<>());
        when(experimentService.getExperimentData(ID)).thenReturn(new ArrayList<>());
        assertDoesNotThrow(
                () -> experimentController.downloadCSVFile(ID_STRING, httpServletResponse)
        );
        verify(eventService).getBlockEventData(ID);
        verify(eventService).getClickEventData(ID);
        verify(eventService).getResourceEventData(ID);
        verify(eventService).getBlockEventCount(ID);
        verify(eventService).getClickEventCount(ID);
        verify(eventService).getResourceEventCount(ID);
        verify(eventService).getCodesDataForExperiment(ID);
        verify(experimentService).getExperimentData(ID);
        verify(httpServletResponse).getWriter();
    }

    @Test
    public void testDownloadCSVFileIO() throws IOException {
        when(httpServletResponse.getWriter()).thenThrow(IOException.class);
        assertThrows(RuntimeException.class,
                () -> experimentController.downloadCSVFile(ID_STRING, httpServletResponse)
        );
        verify(eventService, never()).getBlockEventData(anyInt());
        verify(eventService, never()).getClickEventData(anyInt());
        verify(eventService, never()).getResourceEventData(anyInt());
        verify(eventService, never()).getBlockEventCount(anyInt());
        verify(eventService, never()).getClickEventCount(anyInt());
        verify(eventService, never()).getResourceEventCount(anyInt());
        verify(eventService, never()).getCodesDataForExperiment(anyInt());
        verify(experimentService, never()).getExperimentData(anyInt());
        verify(httpServletResponse).getWriter();
    }

    @Test
    public void testDownloadCSVFileInvalidId() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> experimentController.downloadCSVFile(BLANK, httpServletResponse)
        );
        verify(eventService, never()).getBlockEventData(anyInt());
        verify(eventService, never()).getClickEventData(anyInt());
        verify(eventService, never()).getResourceEventData(anyInt());
        verify(eventService, never()).getBlockEventCount(anyInt());
        verify(eventService, never()).getClickEventCount(anyInt());
        verify(eventService, never()).getResourceEventCount(anyInt());
        verify(eventService, never()).getCodesDataForExperiment(anyInt());
        verify(experimentService, never()).getExperimentData(anyInt());
        verify(httpServletResponse, never()).getWriter();
    }

    @Test
    public void testDownloadCSVFileIdNull() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> experimentController.downloadCSVFile(null, httpServletResponse)
        );
        verify(eventService, never()).getBlockEventData(anyInt());
        verify(eventService, never()).getClickEventData(anyInt());
        verify(eventService, never()).getResourceEventData(anyInt());
        verify(eventService, never()).getBlockEventCount(anyInt());
        verify(eventService, never()).getClickEventCount(anyInt());
        verify(eventService, never()).getResourceEventCount(anyInt());
        verify(eventService, never()).getCodesDataForExperiment(anyInt());
        verify(experimentService, never()).getExperimentData(anyInt());
        verify(httpServletResponse, never()).getWriter();
    }

    @Test
    public void testUploadProjectFile() throws IOException {
        when(file.getContentType()).thenReturn(FILETYPE);
        when(file.getOriginalFilename()).thenReturn(FILENAME);
        when(file.getBytes()).thenReturn(CONTENT);
        assertEquals(REDIRECT_EXPERIMENT + ID, experimentController.uploadProjectFile(file, ID_STRING, model));
        verify(experimentService).uploadSb3Project(ID, CONTENT);
        verify(file).isEmpty();
        verify(file, times(2)).getOriginalFilename();
        verify(file, times(2)).getContentType();
        verify(file).getBytes();
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testUploadProjectFileIO() throws IOException {
        when(file.getContentType()).thenReturn(FILETYPE);
        when(file.getOriginalFilename()).thenReturn(FILENAME);
        when(file.getBytes()).thenThrow(IOException.class);
        assertEquals(ERROR, experimentController.uploadProjectFile(file, ID_STRING, model));
        verify(experimentService, never()).uploadSb3Project(anyInt(), any());
        verify(file).isEmpty();
        verify(file, times(2)).getOriginalFilename();
        verify(file, times(2)).getContentType();
        verify(file).getBytes();
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testUploadProjectFileNotFound() throws IOException {
        when(file.getContentType()).thenReturn(FILETYPE);
        when(file.getOriginalFilename()).thenReturn(FILENAME);
        when(file.getBytes()).thenReturn(CONTENT);
        doThrow(NotFoundException.class).when(experimentService).uploadSb3Project(ID, CONTENT);
        assertEquals(ERROR, experimentController.uploadProjectFile(file, ID_STRING, model));
        verify(experimentService).uploadSb3Project(ID, CONTENT);
        verify(file).isEmpty();
        verify(file, times(2)).getOriginalFilename();
        verify(file, times(2)).getContentType();
        verify(file).getBytes();
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testUploadProjectFileFilenameInvalid() throws IOException {
        when(file.getContentType()).thenReturn(FILETYPE);
        when(file.getOriginalFilename()).thenReturn("name");
        when(model.getAttribute(ERROR_ATTRIBUTE)).thenReturn(ERROR_ATTRIBUTE);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        assertEquals(EXPERIMENT, experimentController.uploadProjectFile(file, ID_STRING, model));
        verify(experimentService, never()).uploadSb3Project(anyInt(), any());
        verify(file).isEmpty();
        verify(file, times(3)).getOriginalFilename();
        verify(file, times(2)).getContentType();
        verify(file, never()).getBytes();
        verify(model, times(6)).addAttribute(anyString(), any());
    }

    @Test
    public void testUploadProjectFileFilenameNull() throws IOException {
        when(file.getContentType()).thenReturn(FILETYPE);
        when(model.getAttribute(ERROR_ATTRIBUTE)).thenReturn(ERROR_ATTRIBUTE);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        assertEquals(EXPERIMENT, experimentController.uploadProjectFile(file, ID_STRING, model));
        verify(experimentService, never()).uploadSb3Project(anyInt(), any());
        verify(file).isEmpty();
        verify(file, times(2)).getOriginalFilename();
        verify(file, times(2)).getContentType();
        verify(file, never()).getBytes();
        verify(model, times(6)).addAttribute(anyString(), any());
    }

    @Test
    public void testUploadProjectFileContentTypeInvalid() throws IOException {
        when(file.getContentType()).thenReturn("type");
        when(model.getAttribute(ERROR_ATTRIBUTE)).thenReturn(ERROR_ATTRIBUTE);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        assertEquals(EXPERIMENT, experimentController.uploadProjectFile(file, ID_STRING, model));
        verify(experimentService, never()).uploadSb3Project(anyInt(), any());
        verify(file).isEmpty();
        verify(file, never()).getOriginalFilename();
        verify(file, times(3)).getContentType();
        verify(file, never()).getBytes();
        verify(model, times(6)).addAttribute(anyString(), any());
    }

    @Test
    public void testUploadProjectFileContentTypeNull() throws IOException {
        when(model.getAttribute(ERROR_ATTRIBUTE)).thenReturn(ERROR_ATTRIBUTE);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        assertEquals(EXPERIMENT, experimentController.uploadProjectFile(file, ID_STRING, model));
        verify(experimentService, never()).uploadSb3Project(anyInt(), any());
        verify(file).isEmpty();
        verify(file, never()).getOriginalFilename();
        verify(file, times(2)).getContentType();
        verify(file, never()).getBytes();
        verify(model, times(6)).addAttribute(anyString(), any());
    }

    @Test
    public void testUploadProjectFileEmpty() throws IOException {
        when(file.isEmpty()).thenReturn(true);
        when(model.getAttribute(ERROR_ATTRIBUTE)).thenReturn(ERROR_ATTRIBUTE);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        assertEquals(EXPERIMENT, experimentController.uploadProjectFile(file, ID_STRING, model));
        verify(experimentService, never()).uploadSb3Project(anyInt(), any());
        verify(file).isEmpty();
        verify(file, never()).getOriginalFilename();
        verify(file, never()).getContentType();
        verify(file, never()).getBytes();
        verify(model, times(6)).addAttribute(anyString(), any());
    }

    @Test
    public void testUploadProjectFileInvalidId() throws IOException {
        assertEquals(ERROR, experimentController.uploadProjectFile(file, BLANK, model));
        verify(experimentService, never()).uploadSb3Project(anyInt(), any());
        verify(file, never()).isEmpty();
        verify(file, never()).getOriginalFilename();
        verify(file, never()).getContentType();
        verify(file, never()).getBytes();
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testUploadProjectFileIdNull() throws IOException {
        assertEquals(ERROR, experimentController.uploadProjectFile(file, null, model));
        verify(experimentService, never()).uploadSb3Project(anyInt(), any());
        verify(file, never()).isEmpty();
        verify(file, never()).getOriginalFilename();
        verify(file, never()).getContentType();
        verify(file, never()).getBytes();
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testUploadProjectFileFileNull() throws IOException {
        assertEquals(ERROR, experimentController.uploadProjectFile(null, ID_STRING, model));
        verify(experimentService, never()).uploadSb3Project(anyInt(), any());
        verify(file, never()).isEmpty();
        verify(file, never()).getOriginalFilename();
        verify(file, never()).getContentType();
        verify(file, never()).getBytes();
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteProjectFile() {
        assertEquals(REDIRECT_EXPERIMENT + ID, experimentController.deleteProjectFile(ID_STRING));
        verify(experimentService).deleteSb3Project(ID);
    }

    @Test
    public void testDeleteProjectFileNotFound() {
        doThrow(NotFoundException.class).when(experimentService).deleteSb3Project(ID);
        assertEquals(ERROR, experimentController.deleteProjectFile(ID_STRING));
        verify(experimentService).deleteSb3Project(ID);
    }

    @Test
    public void testDeleteProjectFileInvalidId() {
        assertEquals(ERROR, experimentController.deleteProjectFile("0"));
        verify(experimentService, never()).deleteSb3Project(anyInt());
    }

    @Test
    public void testDeleteProjectFileIdNull() {
        assertEquals(ERROR, experimentController.deleteProjectFile(null));
        verify(experimentService, never()).deleteSb3Project(anyInt());
    }

    private StringBuilder createLongString(int length) {
        StringBuilder longString = new StringBuilder();
        longString.append("a".repeat(Math.max(0, length)));
        return longString;
    }

    private List<Participant> getParticipants(int number) {
        List<Participant> participants = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            participants.add(new Participant(new User(), new Experiment(), Timestamp.valueOf(LocalDateTime.now()), null));
        }
        return participants;
    }
}
