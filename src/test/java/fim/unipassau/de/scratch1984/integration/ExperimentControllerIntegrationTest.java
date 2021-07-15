package fim.unipassau.de.scratch1984.integration;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.EventService;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.application.service.MailService;
import fim.unipassau.de.scratch1984.application.service.ParticipantService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.Participant;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.spring.configuration.SecurityTestConfig;
import fim.unipassau.de.scratch1984.web.controller.ExperimentController;
import fim.unipassau.de.scratch1984.web.dto.ExperimentDTO;
import fim.unipassau.de.scratch1984.web.dto.ParticipantDTO;
import fim.unipassau.de.scratch1984.web.dto.UserDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ExperimentController.class)
@Import(SecurityTestConfig.class)
@ActiveProfiles("test")
public class ExperimentControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ExperimentService experimentService;

    @MockBean
    private UserService userService;

    @MockBean
    private ParticipantService participantService;

    @MockBean
    private MailService mailService;

    @MockBean
    private EventService eventService;

    private static final String TITLE = "My Experiment";
    private static final String DESCRIPTION = "A description";
    private static final String INFO = "Some info text";
    private static final String POSTSCRIPT = "Some postscript";
    private static final String INFO_PARSED = "<p>Some info text</p>\n";
    private static final String ERROR = "redirect:/error";
    private static final String EXPERIMENT = "experiment";
    private static final String EXPERIMENT_EDIT = "experiment-edit";
    private static final String SAVED_EXPERIMENT = "redirect:/experiment?id=";
    private static final String SUCCESS = "redirect:/?success=true";
    private static final String BLANK = "    ";
    private static final String EXPERIMENT_DTO = "experimentDTO";
    private static final String ID_STRING = "1";
    private static final String INVALID_ID = "-1";
    private static final String ID_PARAM = "id";
    private static final String STATUS_PARAM = "stat";
    private static final String PAGE_PARAM = "page";
    private static final String PARTICIPANTS = "participants";
    private static final String PARTICIPANT = "participant";
    private static final String PAGE = "page";
    private static final String LAST_PAGE_ATTRIBUTE = "lastPage";
    private static final String ERROR_ATTRIBUTE = "error";
    private static final String CURRENT = "3";
    private static final String LAST = "4";
    private static final int FIRST_PAGE = 1;
    private static final int LAST_PAGE = 3;
    private static final int PREVIOUS = 2;
    private static final int NEXT = 4;
    private static final int ID = 1;
    private final ExperimentDTO experimentDTO = new ExperimentDTO(ID, TITLE, DESCRIPTION, INFO, POSTSCRIPT, false);
    private final UserDTO userDTO = new UserDTO("user", "admin1@admin.de", UserDTO.Role.ADMIN,
            UserDTO.Language.ENGLISH, "admin", "secret1");
    private final UserDTO participant = new UserDTO(PARTICIPANT, "participant@part.de", UserDTO.Role.PARTICIPANT,
            UserDTO.Language.ENGLISH, "user", null);
    private final Page<Participant> participants = new PageImpl<>(getParticipants(5));
    private final ParticipantDTO participantDTO = new ParticipantDTO(ID, ID);
    private final String TOKEN_ATTR_NAME = "org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN";
    private final HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
    private final CsrfToken csrfToken = httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());

    @BeforeEach
    public void setup() {
        userDTO.setId(ID);
        userDTO.setActive(true);
        participant.setId(ID + 1);
        participant.setSecret(null);
        experimentDTO.setId(ID);
        experimentDTO.setTitle(TITLE);
        experimentDTO.setDescription(DESCRIPTION);
        experimentDTO.setPostscript(POSTSCRIPT);
        experimentDTO.setActive(false);
        participantDTO.setStart(null);
    }

    @AfterEach
    public void resetService() {
        reset(experimentService, userService);
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void testGetExperiment() throws Exception {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(experimentService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        when(participantService.getParticipantPage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        mvc.perform(get("/experiment")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(PARTICIPANTS, is(participants)))
                .andExpect(model().attribute(PAGE, is(FIRST_PAGE)))
                .andExpect(model().attribute(LAST_PAGE_ATTRIBUTE, is(LAST_PAGE + 1)))
                .andExpect(model().attribute(EXPERIMENT_DTO, allOf(
                        hasProperty("id", is(ID)),
                        hasProperty("title", is(TITLE)),
                        hasProperty("description", is(DESCRIPTION)),
                        hasProperty("postscript", is(POSTSCRIPT)),
                        hasProperty("info", is(INFO_PARSED)),
                        hasProperty("active", is(false))
                )))
                .andExpect(view().name(EXPERIMENT));
        verify(experimentService).getExperiment(ID);
        verify(experimentService).getLastParticipantPage(ID);
        verify(participantService).getParticipantPage(anyInt(), any(PageRequest.class));
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void testGetExperimentNotFound() throws Exception {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/experiment")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(experimentService).getExperiment(ID);
    }

    @Test
    @WithMockUser(username = "user", roles = {"PARTICIPANT"})
    public void testGetExperimentParticipant() throws Exception {
        experimentDTO.setActive(true);
        when(userService.getUser(anyString())).thenReturn(userDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipantPage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        mvc.perform(get("/experiment")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute("participant", is(true)))
                .andExpect(model().attribute(PARTICIPANTS, is(participants)))
                .andExpect(model().attribute(EXPERIMENT_DTO, allOf(
                        hasProperty("id", is(ID)),
                        hasProperty("title", is(TITLE)),
                        hasProperty("description", is(DESCRIPTION)),
                        hasProperty("postscript", is(POSTSCRIPT)),
                        hasProperty("info", is(INFO_PARSED)),
                        hasProperty("active", is(true))
                )))
                .andExpect(view().name(EXPERIMENT));
        verify(userService).getUser(anyString());
        verify(participantService).getParticipant(ID, ID);
        verify(experimentService).getExperiment(ID);
        verify(participantService).getParticipantPage(anyInt(), any(PageRequest.class));
    }

    @Test
    @WithMockUser(username = "user", roles = {"PARTICIPANT"})
    public void testGetExperimentParticipantNotFound() throws Exception {
        when(userService.getUser(anyString())).thenThrow(NotFoundException.class);
        mvc.perform(get("/experiment")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService).getUser(anyString());
        verify(userService, never()).existsParticipant(ID, ID);
        verify(experimentService, never()).getExperiment(ID);
    }

    @Test
    @WithMockUser(username = "user", roles = {"PARTICIPANT"})
    public void testGetExperimentNoParticipant() throws Exception {
        when(userService.getUser(anyString())).thenReturn(userDTO);
        when(participantService.getParticipant(ID, ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/experiment")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService).getUser(anyString());
        verify(participantService).getParticipant(ID, ID);
        verify(experimentService, never()).getExperiment(ID);
    }

    @Test
    @WithMockUser(username = "user", roles = {"PARTICIPANT"})
    public void testGetExperimentParticipantInactive() throws Exception {
        userDTO.setActive(false);
        when(userService.getUser(anyString())).thenReturn(userDTO);
        mvc.perform(get("/experiment")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService).getUser(anyString());
        verify(participantService, never()).getParticipant(anyInt(), anyInt());
        verify(experimentService, never()).getExperiment(anyInt());
    }

    @Test
    @WithMockUser(username = "user", roles = {"PARTICIPANT"})
    public void testGetExperimentParticipantStarted() throws Exception {
        participantDTO.setStart(LocalDateTime.now());
        when(userService.getUser(anyString())).thenReturn(userDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        mvc.perform(get("/experiment")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService).getUser(anyString());
        verify(participantService).getParticipant(ID, ID);
        verify(experimentService, never()).getExperiment(anyInt());
    }

    @Test
    public void testGetExperimentInvalidId() throws Exception {
        mvc.perform(get("/experiment")
                .param(ID_PARAM, INVALID_ID)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(experimentService, never()).getExperiment(ID);
    }

    @Test
    public void testGetExperimentForm() throws  Exception {
        mvc.perform(get("/experiment/create")
                .flashAttr(EXPERIMENT_DTO, new ExperimentDTO())
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT_EDIT));
    }

    @Test
    public void testGetEditExperimentForm() throws Exception {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        mvc.perform(get("/experiment/edit")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(EXPERIMENT_DTO, allOf(
                        hasProperty("id", is(ID)),
                        hasProperty("title", is(TITLE)),
                        hasProperty("description", is(DESCRIPTION)),
                        hasProperty("postscript", is(POSTSCRIPT)),
                        hasProperty("info", is(INFO)),
                        hasProperty("active", is(false))
                )))
                .andExpect(view().name(EXPERIMENT_EDIT));
        verify(experimentService).getExperiment(ID);
    }

    @Test
    public void testGetEditExperimentFormNotFound() throws Exception {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/experiment/edit")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(experimentService).getExperiment(ID);
    }

    @Test
    public void testGetExperimentFormInvalidId() throws Exception {
        mvc.perform(get("/experiment/edit")
                .param(ID_PARAM, INFO)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(experimentService, never()).getExperiment(ID);
    }

    @Test
    public void testEditExperiment() throws Exception {
        when(experimentService.saveExperiment(experimentDTO)).thenReturn(experimentDTO);
        mvc.perform(post("/experiment/update")
                .flashAttr(EXPERIMENT_DTO, experimentDTO)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(SAVED_EXPERIMENT + ID));
        verify(experimentService).existsExperiment(TITLE, ID);
        verify(experimentService).saveExperiment(experimentDTO);
    }

    @Test
    public void testEditExperimentExists() throws Exception {
        when(experimentService.saveExperiment(experimentDTO)).thenReturn(experimentDTO);
        when(experimentService.existsExperiment(TITLE, ID)).thenReturn(true);
        mvc.perform(post("/experiment/update")
                .flashAttr(EXPERIMENT_DTO, experimentDTO)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT_EDIT));
        verify(experimentService).existsExperiment(TITLE, ID);
        verify(experimentService, never()).saveExperiment(experimentDTO);
    }

    @Test
    public void testEditExperimentCreate() throws Exception {
        experimentDTO.setId(null);
        when(experimentService.saveExperiment(experimentDTO)).thenReturn(experimentDTO);
        mvc.perform(post("/experiment/update")
                .flashAttr(EXPERIMENT_DTO, experimentDTO)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(SAVED_EXPERIMENT + null));
        verify(experimentService).existsExperiment(TITLE);
        verify(experimentService).saveExperiment(experimentDTO);
    }

    @Test
    public void testEditExperimentCreateExists() throws Exception {
        experimentDTO.setId(null);
        when(experimentService.saveExperiment(experimentDTO)).thenReturn(experimentDTO);
        when(experimentService.existsExperiment(TITLE)).thenReturn(true);
        mvc.perform(post("/experiment/update")
                .flashAttr(EXPERIMENT_DTO, experimentDTO)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT_EDIT));
        verify(experimentService).existsExperiment(TITLE);
        verify(experimentService, never()).saveExperiment(experimentDTO);
    }

    @Test
    public void testEditExperimentInvalidInput() throws Exception {
        experimentDTO.setTitle(null);
        experimentDTO.setDescription(null);
        mvc.perform(post("/experiment/update")
                .flashAttr(EXPERIMENT_DTO, experimentDTO)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT_EDIT));
        verify(experimentService, never()).existsExperiment(anyString(), anyInt());
        verify(experimentService, never()).existsExperiment(anyString());
        verify(experimentService, never()).saveExperiment(experimentDTO);
    }

    @Test
    public void testDeleteExperiment() throws Exception {
        mvc.perform(get("/experiment/delete")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(SUCCESS));
        verify(experimentService).deleteExperiment(ID);
    }

    @Test
    public void testDeleteExperimentInvalidId() throws Exception {
        mvc.perform(get("/experiment/delete")
                .param(ID_PARAM, BLANK)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(experimentService, never()).deleteExperiment(anyInt());
    }

    @Test
    public void testChangeExperimentStatusOpen() throws Exception {
        experimentDTO.setActive(true);
        List<UserDTO> userDTOS = new ArrayList<>();
        userDTOS.add(participant);
        when(experimentService.changeExperimentStatus(true, ID)).thenReturn(experimentDTO);
        when(userService.reactivateUserAccounts(ID)).thenReturn(userDTOS);
        when(experimentService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        when(participantService.getParticipantPage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        mvc.perform(get("/experiment/status")
                .param(STATUS_PARAM, "open")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(model().attribute(PARTICIPANTS, participants))
                .andExpect(model().attribute(PAGE, is(FIRST_PAGE)))
                .andExpect(model().attribute(LAST_PAGE_ATTRIBUTE, is(LAST_PAGE + 1)))
                .andExpect(model().attribute("experimentDTO", allOf(
                        hasProperty("id", is(ID)),
                        hasProperty("title", is(TITLE)),
                        hasProperty("description", is(DESCRIPTION)),
                        hasProperty("postscript", is(POSTSCRIPT)),
                        hasProperty("info", is(INFO_PARSED)),
                        hasProperty("active", is(true))
                )))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT));
        verify(experimentService).changeExperimentStatus(true, ID);
        verify(userService).reactivateUserAccounts(ID);
        verify(mailService).sendEmail(anyString(), anyString(), any(), anyString());
        verify(experimentService).getLastParticipantPage(ID);
        verify(participantService).getParticipantPage(anyInt(), any(PageRequest.class));
    }

    @Test
    public void testChangeExperimentStatusClose() throws Exception {
        when(experimentService.changeExperimentStatus(false, ID)).thenReturn(experimentDTO);
        when(experimentService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        when(participantService.getParticipantPage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        mvc.perform(get("/experiment/status")
                .param(STATUS_PARAM, "close")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(model().attribute(PARTICIPANTS, participants))
                .andExpect(model().attribute(PAGE, is(FIRST_PAGE)))
                .andExpect(model().attribute(LAST_PAGE_ATTRIBUTE, is(LAST_PAGE + 1)))
                .andExpect(model().attribute("experimentDTO", allOf(
                        hasProperty("id", is(ID)),
                        hasProperty("title", is(TITLE)),
                        hasProperty("description", is(DESCRIPTION)),
                        hasProperty("postscript", is(POSTSCRIPT)),
                        hasProperty("info", is(INFO_PARSED)),
                        hasProperty("active", is(false))
                )))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT));
        verify(experimentService).changeExperimentStatus(false, ID);
        verify(participantService).deactivateParticipantAccounts(ID);
        verify(participantService).getParticipantPage(anyInt(), any(PageRequest.class));
    }

    @Test
    public void testChangeExperimentStatusInvalid() throws Exception {
        mvc.perform(get("/experiment/status")
                .param(STATUS_PARAM, INFO)
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(experimentService, never()).changeExperimentStatus(anyBoolean(), anyInt());
    }

    @Test
    public void testChangeExperimentStatusNotFound() throws Exception {
        when(experimentService.changeExperimentStatus(false, ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/experiment/status")
                .param(STATUS_PARAM, "close")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(experimentService).changeExperimentStatus(false, ID);
    }

    @Test
    public void testSearchForUser() throws Exception {
        experimentDTO.setActive(true);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT)).thenReturn(participant);
        when(userService.updateUser(participant)).thenReturn(participant);
        when(mailService.sendEmail(anyString(), anyString(), any(), anyString())).thenReturn(true);
        mvc.perform(get("/experiment/search")
                .param(PARTICIPANT, PARTICIPANT)
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(SAVED_EXPERIMENT + ID));
        verify(experimentService).getExperiment(ID);
        verify(userService).getUserByUsernameOrEmail(PARTICIPANT);
        verify(userService).updateUser(participant);
        verify(participantService).saveParticipant(participant.getId(), ID);
        verify(mailService).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testSearchForUserExperimentInactive() throws Exception {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT)).thenReturn(participant);
        when(experimentService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        when(participantService.getParticipantPage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        mvc.perform(get("/experiment/search")
                .param(PARTICIPANT, PARTICIPANT)
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(model().attribute(PARTICIPANTS, participants))
                .andExpect(model().attribute(PAGE, FIRST_PAGE))
                .andExpect(model().attribute(LAST_PAGE_ATTRIBUTE, is(LAST_PAGE + 1)))
                .andExpect(model().attribute("experimentDTO", experimentDTO))
                .andExpect(model().attribute(ERROR_ATTRIBUTE, notNullValue()))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT));
        verify(experimentService).getExperiment(ID);
        verify(userService).getUserByUsernameOrEmail(PARTICIPANT);
        verify(userService, never()).updateUser(participant);
        verify(participantService, never()).saveParticipant(participant.getId(), ID);
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testSearchForUserInvalidQuery() throws Exception {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(experimentService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        when(participantService.getParticipantPage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        mvc.perform(get("/experiment/search")
                .param(PARTICIPANT, BLANK)
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(model().attribute(PARTICIPANTS, participants))
                .andExpect(model().attribute(PAGE, FIRST_PAGE))
                .andExpect(model().attribute(LAST_PAGE_ATTRIBUTE, is(LAST_PAGE + 1)))
                .andExpect(model().attribute("experimentDTO", experimentDTO))
                .andExpect(model().attribute(ERROR_ATTRIBUTE, notNullValue()))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT));
        verify(experimentService).getExperiment(ID);
        verify(userService, never()).getUserByUsernameOrEmail(PARTICIPANT);
        verify(userService, never()).updateUser(participant);
        verify(participantService, never()).saveParticipant(participant.getId(), ID);
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testSearchForUserExperimentNotFound() throws Exception {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/experiment/search")
                .param(PARTICIPANT, PARTICIPANT)
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(experimentService).getExperiment(ID);
        verify(userService, never()).getUserByUsernameOrEmail(PARTICIPANT);
        verify(userService, never()).updateUser(participant);
        verify(participantService, never()).saveParticipant(participant.getId(), ID);
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testGetNextPage() throws Exception {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(experimentService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        when(participantService.getParticipantPage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        mvc.perform(get("/experiment/next")
                .param(ID_PARAM, ID_STRING)
                .param(PAGE_PARAM, CURRENT)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(model().attribute(PARTICIPANTS, participants))
                .andExpect(model().attribute(PAGE, is(NEXT)))
                .andExpect(model().attribute(LAST_PAGE_ATTRIBUTE, is(LAST_PAGE + 1)))
                .andExpect(model().attribute("experimentDTO", allOf(
                        hasProperty("id", is(ID)),
                        hasProperty("title", is(TITLE)),
                        hasProperty("description", is(DESCRIPTION)),
                        hasProperty("postscript", is(POSTSCRIPT)),
                        hasProperty("info", is(INFO_PARSED)),
                        hasProperty("active", is(false))
                )))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT));
        verify(experimentService).getExperiment(ID);
        verify(experimentService).getLastParticipantPage(ID);
        verify(participantService).getParticipantPage(anyInt(), any(PageRequest.class));
    }

    @Test
    public void testGetNextPageNotFound() throws Exception {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/experiment/next")
                .param(ID_PARAM, ID_STRING)
                .param(PAGE_PARAM, CURRENT)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(experimentService).getExperiment(ID);
        verify(experimentService, never()).getLastParticipantPage(ID);
        verify(participantService, never()).getParticipantPage(anyInt(), any(PageRequest.class));
    }

    @Test
    public void testGetNextInvalidId() throws Exception {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/experiment/next")
                .param(ID_PARAM, BLANK)
                .param(PAGE_PARAM, CURRENT)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(experimentService, never()).getExperiment(ID);
        verify(experimentService, never()).getLastParticipantPage(ID);
        verify(participantService, never()).getParticipantPage(anyInt(), any(PageRequest.class));
    }

    @Test
    public void testGetNextInvalidCurrent() throws Exception {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/experiment/next")
                .param(ID_PARAM, ID_STRING)
                .param(PAGE_PARAM, "-1")
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(experimentService, never()).getExperiment(ID);
        verify(experimentService, never()).getLastParticipantPage(ID);
        verify(participantService, never()).getParticipantPage(anyInt(), any(PageRequest.class));
    }

    @Test
    public void testGetPreviousPage() throws Exception {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(experimentService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        when(participantService.getParticipantPage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        mvc.perform(get("/experiment/previous")
                .param(ID_PARAM, ID_STRING)
                .param(PAGE_PARAM, CURRENT)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(model().attribute(PARTICIPANTS, participants))
                .andExpect(model().attribute(PAGE, is(PREVIOUS)))
                .andExpect(model().attribute(LAST_PAGE_ATTRIBUTE, is(LAST_PAGE + 1)))
                .andExpect(model().attribute("experimentDTO", allOf(
                        hasProperty("id", is(ID)),
                        hasProperty("title", is(TITLE)),
                        hasProperty("description", is(DESCRIPTION)),
                        hasProperty("postscript", is(POSTSCRIPT)),
                        hasProperty("info", is(INFO_PARSED)),
                        hasProperty("active", is(false))
                )))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT));
        verify(experimentService).getExperiment(ID);
        verify(experimentService).getLastParticipantPage(ID);
        verify(participantService).getParticipantPage(anyInt(), any(PageRequest.class));
    }

    @Test
    public void testGetPreviousPageNotFound() throws Exception {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/experiment/previous")
                .param(ID_PARAM, ID_STRING)
                .param(PAGE_PARAM, CURRENT)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(experimentService).getExperiment(ID);
        verify(experimentService, never()).getLastParticipantPage(ID);
        verify(participantService, never()).getParticipantPage(anyInt(), any(PageRequest.class));
    }

    @Test
    public void testGetPreviousPageInvalidId() throws Exception {
        mvc.perform(get("/experiment/previous")
                .param(ID_PARAM, "0")
                .param(PAGE_PARAM, CURRENT)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(experimentService, never()).getExperiment(ID);
        verify(experimentService, never()).getLastParticipantPage(ID);
        verify(participantService, never()).getParticipantPage(anyInt(), any(PageRequest.class));
    }

    @Test
    public void testGetPreviousPageInvalidCurrent() throws Exception {
        mvc.perform(get("/experiment/previous")
                .param(ID_PARAM, ID_STRING)
                .param(PAGE_PARAM, BLANK)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(experimentService, never()).getExperiment(ID);
        verify(experimentService, never()).getLastParticipantPage(ID);
        verify(participantService, never()).getParticipantPage(anyInt(), any(PageRequest.class));
    }

    @Test
    public void testGetFirstPage() throws Exception {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(experimentService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        when(participantService.getParticipantPage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        mvc.perform(get("/experiment/first")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(model().attribute(PARTICIPANTS, participants))
                .andExpect(model().attribute(PAGE, is(FIRST_PAGE)))
                .andExpect(model().attribute(LAST_PAGE_ATTRIBUTE, is(LAST_PAGE + 1)))
                .andExpect(model().attribute("experimentDTO", allOf(
                        hasProperty("id", is(ID)),
                        hasProperty("title", is(TITLE)),
                        hasProperty("description", is(DESCRIPTION)),
                        hasProperty("postscript", is(POSTSCRIPT)),
                        hasProperty("info", is(INFO_PARSED)),
                        hasProperty("active", is(false))
                )))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT));
        verify(experimentService).getExperiment(ID);
        verify(experimentService).getLastParticipantPage(ID);
        verify(participantService).getParticipantPage(anyInt(), any(PageRequest.class));
    }

    @Test
    public void testGetFirstPageNotFound() throws Exception {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/experiment/first")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(experimentService).getExperiment(ID);
        verify(experimentService, never()).getLastParticipantPage(ID);
        verify(participantService, never()).getParticipantPage(anyInt(), any(PageRequest.class));
    }

    @Test
    public void testGetLastPage() throws Exception {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(experimentService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        when(participantService.getParticipantPage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        mvc.perform(get("/experiment/last")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(model().attribute(PARTICIPANTS, participants))
                .andExpect(model().attribute(PAGE, is(LAST_PAGE + 1)))
                .andExpect(model().attribute(LAST_PAGE_ATTRIBUTE, is(LAST_PAGE + 1)))
                .andExpect(model().attribute("experimentDTO", allOf(
                        hasProperty("id", is(ID)),
                        hasProperty("title", is(TITLE)),
                        hasProperty("description", is(DESCRIPTION)),
                        hasProperty("postscript", is(POSTSCRIPT)),
                        hasProperty("info", is(INFO_PARSED)),
                        hasProperty("active", is(false))
                )))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT));
        verify(experimentService).getExperiment(ID);
        verify(experimentService, times(2)).getLastParticipantPage(ID);
        verify(participantService).getParticipantPage(anyInt(), any(PageRequest.class));
    }

    @Test
    public void testGetLastPageNotFound() throws Exception {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/experiment/last")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(experimentService).getExperiment(ID);
        verify(experimentService, never()).getLastParticipantPage(ID);
        verify(participantService, never()).getParticipantPage(anyInt(), any(PageRequest.class));
    }

    @Test
    public void testDownloadCSVFile() throws Exception {
        when(eventService.getBlockEventData(ID)).thenReturn(new ArrayList<>());
        when(eventService.getResourceEventData(ID)).thenReturn(new ArrayList<>());
        when(eventService.getBlockEventCount(ID)).thenReturn(new ArrayList<>());
        when(eventService.getResourceEventCount(ID)).thenReturn(new ArrayList<>());
        when(eventService.getCodesDataForExperiment(ID)).thenReturn(new ArrayList<>());
        when(experimentService.getExperimentData(ID)).thenReturn(new ArrayList<>());
        mvc.perform(get("/experiment/csv")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk());
        verify(eventService).getBlockEventData(ID);
        verify(eventService).getResourceEventData(ID);
        verify(eventService).getBlockEventCount(ID);
        verify(eventService).getResourceEventCount(ID);
        verify(eventService).getCodesDataForExperiment(ID);
        verify(experimentService).getExperimentData(ID);
    }

    @Test
    public void testDownloadCSVFileInvalidId() throws Exception {
        mvc.perform(get("/experiment/csv")
                .param(ID_PARAM, "0")
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isBadRequest());
        verify(eventService, never()).getBlockEventData(anyInt());
        verify(eventService, never()).getResourceEventData(anyInt());
        verify(eventService, never()).getBlockEventCount(anyInt());
        verify(eventService, never()).getResourceEventCount(anyInt());
        verify(eventService, never()).getCodesDataForExperiment(anyInt());
        verify(experimentService, never()).getExperimentData(anyInt());
    }

    private List<Participant> getParticipants(int number) {
        List<Participant> participants = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            participants.add(new Participant(new User(), new Experiment(), Timestamp.valueOf(LocalDateTime.now()), null));
        }
        return participants;
    }
}
