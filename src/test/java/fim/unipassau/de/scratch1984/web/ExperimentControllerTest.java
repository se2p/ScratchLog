package fim.unipassau.de.scratch1984.web;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.application.service.ParticipantService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.Participant;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.web.controller.ExperimentController;
import fim.unipassau.de.scratch1984.web.dto.ExperimentDTO;
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
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private MockedStatic<SecurityContextHolder> securityContextHolder;
    private static final String TITLE = "My Experiment";
    private static final String DESCRIPTION = "A description";
    private static final String INFO = "Some info text";
    private static final String ERROR = "redirect:/error";
    private static final String EXPERIMENT = "experiment";
    private static final String EXPERIMENT_EDIT = "experiment-edit";
    private static final String SAVED_EXPERIMENT = "redirect:/experiment?id=";
    private static final String SUCCESS = "redirect:/?success=true";
    private static final String BLANK = "    ";
    private static final String EXPERIMENT_DTO = "experimentDTO";
    private static final String ID_STRING = "1";
    private static final String INVALID_ID = "-1";
    private static final String ADMIN = "ROLE_ADMIN";
    private static final String USERNAME = "Admin";
    private static final String PARTICIPANTS = "participants";
    private static final int ID = 1;
    private final ExperimentDTO experimentDTO = new ExperimentDTO(ID, TITLE, DESCRIPTION, INFO, false);
    private final UserDTO userDTO = new UserDTO(USERNAME, "admin1@admin.de", UserDTO.Role.ADMIN,
            UserDTO.Language.ENGLISH, "admin", "secret1");
    private final Page<Participant> participants = new PageImpl<>(getParticipants(5));

    @BeforeEach
    public void setup() {
        userDTO.setId(ID);
        userDTO.setActive(true);
        experimentDTO.setId(ID);
        experimentDTO.setTitle(TITLE);
        experimentDTO.setDescription(DESCRIPTION);
        experimentDTO.setInfo(INFO);
        securityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
    }

    @AfterEach
    public void cleanup() {
        securityContextHolder.close();
    }

    @Test
    public void testGetExperiment() {
        when(httpServletRequest.isUserInRole(ADMIN)).thenReturn(true);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipantPage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        String returnString = experimentController.getExperiment(ID_STRING, model, httpServletRequest);
        assertEquals(EXPERIMENT, returnString);
        verify(httpServletRequest).isUserInRole(ADMIN);
        verify(experimentService).getExperiment(ID);
        verify(participantService).getParticipantPage(anyInt(), any(PageRequest.class));
        verify(model).addAttribute(EXPERIMENT_DTO, experimentDTO);
        verify(model).addAttribute(PARTICIPANTS, participants);
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
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(userService.existsParticipant(ID, ID)).thenReturn(true);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipantPage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        String returnString = experimentController.getExperiment(ID_STRING, model, httpServletRequest);
        assertEquals(EXPERIMENT, returnString);
        verify(securityContext).getAuthentication();
        verify(authentication).getName();
        verify(userService).existsParticipant(userDTO.getId(), ID);
        verify(httpServletRequest).isUserInRole(ADMIN);
        verify(experimentService).getExperiment(ID);
        verify(participantService).getParticipantPage(anyInt(), any(PageRequest.class));
        verify(model).addAttribute(EXPERIMENT_DTO, experimentDTO);
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
        String returnString = experimentController.getExperiment(ID_STRING, model, httpServletRequest);
        assertEquals(ERROR, returnString);
        verify(securityContext).getAuthentication();
        verify(authentication, times(2)).getName();
        verify(userService).existsParticipant(userDTO.getId(), ID);
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
        when(userService.existsParticipant(ID, ID)).thenReturn(true);
        String returnString = experimentController.getExperiment(ID_STRING, model, httpServletRequest);
        assertEquals(ERROR, returnString);
        verify(securityContext).getAuthentication();
        verify(authentication, times(2)).getName();
        verify(userService).existsParticipant(userDTO.getId(), ID);
        verify(httpServletRequest).isUserInRole(ADMIN);
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
        assertEquals(SAVED_EXPERIMENT + ID, returnString);
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
        when(experimentService.saveExperiment(experimentDTO)).thenReturn(experimentDTO);
        String returnString = experimentController.editExperiment(experimentDTO, bindingResult);
        assertEquals(SAVED_EXPERIMENT + experimentDTO.getId(), returnString);
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
        String returnString = experimentController.editExperiment(experimentDTO, bindingResult);
        assertEquals(EXPERIMENT_EDIT, returnString);
        verify(bindingResult).addError(any());
        verify(experimentService).existsExperiment(experimentDTO.getTitle(), experimentDTO.getId());
        verify(experimentService, never()).saveExperiment(any());
    }

    @Test
    public void testDeleteExperiment() {
        String returnString = experimentController.deleteExperiment(ID_STRING);
        assertEquals(SUCCESS, returnString);
        verify(experimentService).deleteExperiment(ID);
    }

    @Test
    public void testDeleteExperimentInvalidId() {
        String returnString = experimentController.deleteExperiment(INVALID_ID);
        assertEquals(ERROR, returnString);
        verify(experimentService, never()).deleteExperiment(ID);
    }

    @Test
    public void testChangeExperimentStatusOpen() {
        when(experimentService.changeExperimentStatus(true, ID)).thenReturn(experimentDTO);
        when(participantService.getParticipantPage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        String returnString = experimentController.changeExperimentStatus("open", ID_STRING, model);
        assertEquals(EXPERIMENT, returnString);
        verify(experimentService).changeExperimentStatus(true, ID);
        verify(participantService).getParticipantPage(anyInt(), any(PageRequest.class));
        verify(model).addAttribute(EXPERIMENT_DTO, experimentDTO);
        verify(model).addAttribute(PARTICIPANTS, participants);
    }

    @Test
    public void testChangeExperimentStatusClose() {
        when(experimentService.changeExperimentStatus(false, ID)).thenReturn(experimentDTO);
        when(participantService.getParticipantPage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        String returnString = experimentController.changeExperimentStatus("close", ID_STRING, model);
        assertEquals(EXPERIMENT, returnString);
        verify(experimentService).changeExperimentStatus(false, ID);
        verify(participantService).deactivateParticipantAccounts(ID);
        verify(participantService).getParticipantPage(anyInt(), any(PageRequest.class));
        verify(model).addAttribute(EXPERIMENT_DTO, experimentDTO);
        verify(model).addAttribute(PARTICIPANTS, participants);
    }

    @Test
    public void testChangeExperimentStatusCloseInfoNull() {
        experimentDTO.setInfo(null);
        when(experimentService.changeExperimentStatus(false, ID)).thenReturn(experimentDTO);
        when(participantService.getParticipantPage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        String returnString = experimentController.changeExperimentStatus("close", ID_STRING, model);
        assertEquals(EXPERIMENT, returnString);
        verify(experimentService).changeExperimentStatus(false, ID);
        verify(participantService).deactivateParticipantAccounts(ID);
        verify(participantService).getParticipantPage(anyInt(), any(PageRequest.class));
        verify(model).addAttribute(EXPERIMENT_DTO, experimentDTO);
        verify(model).addAttribute(PARTICIPANTS, participants);
    }

    @Test
    public void testChangeExperimentStatusInvalid() {
        String returnString = experimentController.changeExperimentStatus("blabla", ID_STRING, model);
        assertEquals(ERROR, returnString);
        verify(experimentService, never()).changeExperimentStatus(anyBoolean(), anyInt());
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testChangeExperimentStatusOpenNotFound() {
        when(experimentService.changeExperimentStatus(true, ID)).thenThrow(NotFoundException.class);
        String returnString = experimentController.changeExperimentStatus("open", ID_STRING, model);
        assertEquals(ERROR, returnString);
        verify(experimentService).changeExperimentStatus(true, ID);
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testChangeExperimentStatusStatusNull() {
        String returnString = experimentController.changeExperimentStatus(null, ID_STRING, model);
        assertEquals(ERROR, returnString);
        verify(experimentService, never()).changeExperimentStatus(anyBoolean(), anyInt());
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testChangeExperimentStatusIdNull() {
        String returnString = experimentController.changeExperimentStatus("open", null, model);
        assertEquals(ERROR, returnString);
        verify(experimentService, never()).changeExperimentStatus(anyBoolean(), anyInt());
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
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
