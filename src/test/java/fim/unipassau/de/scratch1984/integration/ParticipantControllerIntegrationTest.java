package fim.unipassau.de.scratch1984.integration;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.application.service.MailService;
import fim.unipassau.de.scratch1984.application.service.ParticipantService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.persistence.entity.Participant;
import fim.unipassau.de.scratch1984.spring.configuration.SecurityTestConfig;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.web.controller.ParticipantController;
import fim.unipassau.de.scratch1984.web.dto.ExperimentDTO;
import fim.unipassau.de.scratch1984.web.dto.ParticipantDTO;
import fim.unipassau.de.scratch1984.web.dto.UserDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
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
@WebMvcTest(ParticipantController.class)
@Import(SecurityTestConfig.class)
@ActiveProfiles("test")
public class ParticipantControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;

    @MockBean
    private ExperimentService experimentService;

    @MockBean
    private ParticipantService participantService;

    @MockBean
    private MailService mailService;

    private static final String ERROR = "redirect:/error";
    private static final String PARTICIPANT = "participant";
    private static final String REDIRECT_EXPERIMENT = "redirect:/experiment?id=";
    private static final String REDIRECT_GUI = "redirect:" + Constants.GUI_URL + "?uid=";
    private static final String REDIRECT_FINISH = "redirect:/finish?id=";
    private static final String REDIRECT_SECRET = "redirect:/secret?user=";
    private static final String EXP_ID = "&expid=";
    private static final String RESTART = "&restart=true";
    private static final String EMAIL = "participant@participant.de";
    private static final String BLANK = "   ";
    private static final String ID_STRING = "1";
    private static final String EXPERIMENT = "experiment";
    private static final String USER_DTO = "userDTO";
    private static final String EXPERIMENT_DTO = "experimentDTO";
    private static final String INFO = "info";
    private static final String POSTSCRIPT = "postscript";
    private static final String ERROR_ATTRIBUTE = "error";
    private static final String ID_PARAM = "id";
    private static final String EXP_ID_PARAM = "expId";
    private static final String USER_PARAM = "user";
    private static final String EXPERIMENT_PARAM = "experiment";
    private static final int ID = 1;
    private static final int LAST_ID = ID + 1;
    private final UserDTO newUser = new UserDTO(PARTICIPANT, EMAIL, UserDTO.Role.PARTICIPANT,
            UserDTO.Language.ENGLISH, "password", "secret");
    private final UserDTO userDTO = new UserDTO(PARTICIPANT, EMAIL, UserDTO.Role.PARTICIPANT,
            UserDTO.Language.ENGLISH, "password", "secret");
    private final ExperimentDTO experimentDTO = new ExperimentDTO(ID, "title", "description", INFO, POSTSCRIPT, true);
    private final ParticipantDTO participantDTO = new ParticipantDTO(ID, ID);
    private final String TOKEN_ATTR_NAME = "org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN";
    private final HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
    private final CsrfToken csrfToken = httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());

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
    }

    @AfterEach
    public void resetService() {reset(userService, experimentService, participantService, mailService);}

    @Test
    public void testGetParticipantForm() throws Exception {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.findLastId()).thenReturn(ID);
        mvc.perform(get("/participant/add")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(EXPERIMENT, is(ID)))
                .andExpect(model().attribute(USER_DTO, hasProperty("username", is(PARTICIPANT + LAST_ID))))
                .andExpect(view().name(PARTICIPANT));
        verify(experimentService).getExperiment(ID);
        verify(userService).findLastId();
    }

    @Test
    public void testGetParticipantFormNotFound() throws Exception {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/participant/add")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(experimentService).getExperiment(ID);
        verify(userService, never()).findLastId();
    }

    @Test
    public void testGetParticipantFormExperimentInactive() throws Exception {
        experimentDTO.setActive(false);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        mvc.perform(get("/participant/add")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(experimentService).getExperiment(ID);
        verify(userService, never()).findLastId();
    }

    @Test
    public void testGetParticipantFormExperimentIdInvalid() throws Exception {
        mvc.perform(get("/participant/add")
                .param(ID_PARAM, "0")
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(experimentService, never()).getExperiment(ID);
        verify(userService, never()).findLastId();
    }

    @Test
    public void testAddParticipant() throws Exception {
        Mockito.mock(Constants.class);
        Whitebox.setInternalState(Constants.class, "MAIL_SERVER", true);
        when(userService.saveUser(newUser)).thenReturn(userDTO);
        when(mailService.sendEmail(anyString(), any(), any(), anyString())).thenReturn(true);
        mvc.perform(post("/participant/add")
                .flashAttr(USER_DTO, newUser)
                .param(EXP_ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_EXPERIMENT + ID));
        verify(userService).saveUser(newUser);
        verify(participantService).saveParticipant(userDTO.getId(), ID);
        verify(mailService).sendEmail(anyString(), any(), any(), anyString());
    }

    @Test
    public void testAddParticipantNoMailServer() throws Exception {
        Mockito.mock(Constants.class);
        Whitebox.setInternalState(Constants.class, "MAIL_SERVER", false);
        when(userService.saveUser(newUser)).thenReturn(userDTO);
        mvc.perform(post("/participant/add")
                        .flashAttr(USER_DTO, newUser)
                        .param(EXP_ID_PARAM, ID_STRING)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_SECRET + userDTO.getId() + "&" + EXPERIMENT_PARAM + "="
                        + ID));
        verify(userService).saveUser(newUser);
        verify(participantService).saveParticipant(userDTO.getId(), ID);
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
    }

    @Test
    public void testAddParticipantMessagingError() throws Exception {
        Mockito.mock(Constants.class);
        Whitebox.setInternalState(Constants.class, "MAIL_SERVER", true);
        when(userService.saveUser(newUser)).thenReturn(userDTO);
        mvc.perform(post("/participant/add")
                .flashAttr(USER_DTO, newUser)
                .param(EXP_ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService).saveUser(newUser);
        verify(participantService).saveParticipant(userDTO.getId(), ID);
        verify(mailService).sendEmail(anyString(), any(), any(), anyString());
    }

    @Test
    public void testAddParticipantNotFound() throws Exception {
        when(userService.saveUser(newUser)).thenReturn(userDTO);
        doThrow(NotFoundException.class).when(participantService).saveParticipant(userDTO.getId(), ID);
        mvc.perform(post("/participant/add")
                .flashAttr(USER_DTO, newUser)
                .param(EXP_ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService).saveUser(newUser);
        verify(participantService).saveParticipant(userDTO.getId(), ID);
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
    }

    @Test
    public void testAddParticipantUsernameAndEmailExists() throws Exception {
        when(userService.existsUser(PARTICIPANT)).thenReturn(true);
        when(userService.existsEmail(EMAIL)).thenReturn(true);
        mvc.perform(post("/participant/add")
                .flashAttr(USER_DTO, newUser)
                .param(EXP_ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(EXPERIMENT, is(ID)))
                .andExpect(view().name(PARTICIPANT));
        verify(userService, never()).saveUser(any());
        verify(participantService, never()).saveParticipant(anyInt(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
    }

    @Test
    public void testAddParticipantInvalidUsername() throws Exception {
        newUser.setUsername(BLANK);
        mvc.perform(post("/participant/add")
                .flashAttr(USER_DTO, newUser)
                .param(EXP_ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(EXPERIMENT, is(ID)))
                .andExpect(view().name(PARTICIPANT));
        verify(userService, never()).saveUser(any());
        verify(participantService, never()).saveParticipant(anyInt(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
    }

    @Test
    public void testAddParticipantInvalidEmail() throws Exception {
        newUser.setEmail(PARTICIPANT);
        mvc.perform(post("/participant/add")
                .flashAttr(USER_DTO, newUser)
                .param(EXP_ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(EXPERIMENT, is(ID)))
                .andExpect(view().name(PARTICIPANT));
        verify(userService, never()).saveUser(any());
        verify(participantService, never()).saveParticipant(anyInt(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
    }

    @Test
    public void testAddParticipantExperimentIdInvalid() throws Exception {
        mvc.perform(post("/participant/add")
                .flashAttr(USER_DTO, newUser)
                .param(EXP_ID_PARAM, "-1")
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService, never()).saveUser(any());
        verify(participantService, never()).saveParticipant(anyInt(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
    }

    @Test
    public void testAddParticipantUserIdNotNull() throws Exception {
        mvc.perform(post("/participant/add")
                .flashAttr(USER_DTO, userDTO)
                .param(EXP_ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService, never()).saveUser(any());
        verify(participantService, never()).saveParticipant(anyInt(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
    }

    @Test
    public void testDeleteParticipant() throws Exception {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT)).thenReturn(userDTO);
        when(userService.existsParticipant(userDTO.getId(), ID)).thenReturn(true);
        when(userService.updateUser(userDTO)).thenReturn(userDTO);
        mvc.perform(get("/participant/delete")
                .param(ID_PARAM, ID_STRING)
                .param(PARTICIPANT, PARTICIPANT)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_EXPERIMENT + ID));
        verify(userService).getUserByUsernameOrEmail(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(userService).existsParticipant(userDTO.getId(), ID);
        verify(participantService).simultaneousParticipation(ID);
        verify(userService).updateUser(userDTO);
        verify(participantService).deleteParticipant(userDTO.getId(), ID);
    }

    @Test
    public void testDeleteParticipantSimultaneousParticipation() throws Exception {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT)).thenReturn(userDTO);
        when(userService.existsParticipant(userDTO.getId(), ID)).thenReturn(true);
        when(participantService.simultaneousParticipation(ID)).thenReturn(true);
        when(userService.updateUser(userDTO)).thenReturn(userDTO);
        mvc.perform(get("/participant/delete")
                .param(ID_PARAM, ID_STRING)
                .param(PARTICIPANT, PARTICIPANT)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_EXPERIMENT + ID));
        verify(userService).getUserByUsernameOrEmail(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(userService).existsParticipant(userDTO.getId(), ID);
        verify(participantService).simultaneousParticipation(ID);
        verify(userService, never()).updateUser(any());
        verify(participantService).deleteParticipant(userDTO.getId(), ID);
    }

    @Test
    public void testDeleteParticipantUserNotFound() throws Exception {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT)).thenReturn(userDTO);
        when(userService.existsParticipant(userDTO.getId(), ID)).thenReturn(true);
        when(userService.updateUser(userDTO)).thenThrow(NotFoundException.class);
        mvc.perform(get("/participant/delete")
                .param(ID_PARAM, ID_STRING)
                .param(PARTICIPANT, PARTICIPANT)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService).getUserByUsernameOrEmail(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(userService).existsParticipant(userDTO.getId(), ID);
        verify(participantService).simultaneousParticipation(ID);
        verify(userService).updateUser(userDTO);
        verify(participantService, never()).deleteParticipant(anyInt(), anyInt());
    }

    @Test
    public void testDeleteParticipantNotParticipantEntry() throws Exception {
        List<Participant> list = new ArrayList<>();
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT)).thenReturn(userDTO);
        when(experimentService.getLastParticipantPage(ID)).thenReturn(ID);
        when(participantService.getParticipantPage(anyInt(), any(PageRequest.class))).thenReturn(new PageImpl<>(list));
        mvc.perform(get("/participant/delete")
                .param(ID_PARAM, ID_STRING)
                .param(PARTICIPANT, PARTICIPANT)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(model().attribute("page", 1))
                .andExpect(model().attribute("lastPage", ID + 1))
                .andExpect(model().attribute(EXPERIMENT_DTO, experimentDTO))
                .andExpect(model().attribute(ERROR_ATTRIBUTE, notNullValue()))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT));
        verify(userService).getUserByUsernameOrEmail(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(userService).existsParticipant(userDTO.getId(), ID);
        verify(experimentService).getLastParticipantPage(ID);
        verify(participantService).getParticipantPage(anyInt(), any(PageRequest.class));
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(userService, never()).updateUser(any());
        verify(participantService, never()).deleteParticipant(anyInt(), anyInt());
    }

    @Test
    public void testDeleteParticipantExperimentNotFound() throws Exception {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT)).thenReturn(userDTO);
        mvc.perform(get("/participant/delete")
                .param(ID_PARAM, ID_STRING)
                .param(PARTICIPANT, PARTICIPANT)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService).getUserByUsernameOrEmail(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(userService, never()).existsParticipant(anyInt(), anyInt());
        verify(experimentService, never()).getLastParticipantPage(anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(userService, never()).updateUser(any());
        verify(participantService, never()).deleteParticipant(anyInt(), anyInt());
    }

    @Test
    public void testDeleteParticipantExperimentIdInvalid() throws Exception {
        mvc.perform(get("/participant/delete")
                .param(ID_PARAM, BLANK)
                .param(PARTICIPANT, PARTICIPANT)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService, never()).getUserByUsernameOrEmail(anyString());
        verify(experimentService, never()).getExperiment(anyInt());
        verify(userService, never()).existsParticipant(anyInt(), anyInt());
        verify(experimentService, never()).getLastParticipantPage(anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(userService, never()).updateUser(any());
        verify(participantService, never()).deleteParticipant(anyInt(), anyInt());
    }

    @Test
    @WithMockUser(username = PARTICIPANT, roles = {"PARTICIPANT"})
    public void testStartExperiment() throws Exception {
        when(userService.getUser(PARTICIPANT)).thenReturn(userDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        when(participantService.updateParticipant(participantDTO)).thenReturn(true);
        mvc.perform(get("/participant/start")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_GUI + ID + EXP_ID + ID));
        verify(userService).getUser(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService).updateParticipant(participantDTO);
    }

    @Test
    @WithMockUser(username = PARTICIPANT, roles = {"PARTICIPANT"})
    public void testStartExperimentNotUpdated() throws Exception {
        when(userService.getUser(PARTICIPANT)).thenReturn(userDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        mvc.perform(get("/participant/start")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService).getUser(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService).updateParticipant(participantDTO);
    }

    @Test
    @WithMockUser(username = PARTICIPANT, roles = {"PARTICIPANT"})
    public void testStartExperimentParticipantStarted() throws Exception {
        participantDTO.setStart(LocalDateTime.now());
        when(userService.getUser(PARTICIPANT)).thenReturn(userDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        mvc.perform(get("/participant/start")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_GUI + ID + EXP_ID + ID + RESTART));
        verify(userService).getUser(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService, never()).updateParticipant(any());
    }

    @Test
    @WithMockUser(username = PARTICIPANT, roles = {"PARTICIPANT"})
    public void testStartExperimentParticipantFinished() throws Exception {
        participantDTO.setEnd(LocalDateTime.now());
        when(userService.getUser(PARTICIPANT)).thenReturn(userDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        mvc.perform(get("/participant/start")
                        .param(ID_PARAM, ID_STRING)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService).getUser(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService, never()).updateParticipant(any());
    }

    @Test
    @WithMockUser(username = PARTICIPANT, roles = {"PARTICIPANT"})
    public void testStartExperimentParticipantInactive() throws Exception {
        userDTO.setActive(false);
        when(userService.getUser(PARTICIPANT)).thenReturn(userDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        mvc.perform(get("/participant/start")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService).getUser(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService, never()).updateParticipant(any());
    }

    @Test
    @WithMockUser(username = PARTICIPANT, roles = {"PARTICIPANT"})
    public void testStartExperimentUserNotFound() throws Exception {
        when(userService.getUser(PARTICIPANT)).thenThrow(NotFoundException.class);
        mvc.perform(get("/participant/start")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService).getUser(PARTICIPANT);
        verify(experimentService, never()).getExperiment(anyInt());
        verify(participantService, never()).getParticipant(anyInt(), anyInt());
        verify(participantService, never()).updateParticipant(any());
    }

    @Test
    @WithMockUser(username = PARTICIPANT, roles = {"ADMIN"})
    public void testStartExperimentUserAdmin() throws Exception {
        mvc.perform(get("/participant/start")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService, never()).getUser(anyString());
        verify(experimentService, never()).getExperiment(anyInt());
        verify(participantService, never()).getParticipant(anyInt(), anyInt());
        verify(participantService, never()).updateParticipant(any());
    }

    @Test
    public void testStartExperimentIdInvalid() throws Exception {
        mvc.perform(get("/participant/start")
                .param(ID_PARAM, BLANK)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService, never()).getUser(anyString());
        verify(experimentService, never()).getExperiment(anyInt());
        verify(participantService, never()).getParticipant(anyInt(), anyInt());
        verify(participantService, never()).updateParticipant(any());
    }

    @Test
    public void testStopExperiment() throws Exception {
        participantDTO.setStart(LocalDateTime.now());
        when(userService.getUserById(ID)).thenReturn(userDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        when(participantService.updateParticipant(participantDTO)).thenReturn(true);
        mvc.perform(get("/participant/stop")
                .param(USER_PARAM, ID_STRING)
                .param(EXPERIMENT_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_FINISH + ID));
        verify(userService).getUserById(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService).simultaneousParticipation(ID);
        verify(participantService).updateParticipant(participantDTO);
        verify(userService).saveUser(userDTO);
    }

    @Test
    public void testStopExperimentSimultaneousParticipation() throws Exception {
        participantDTO.setStart(LocalDateTime.now());
        when(userService.getUserById(ID)).thenReturn(userDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        when(participantService.simultaneousParticipation(ID)).thenReturn(true);
        when(participantService.updateParticipant(participantDTO)).thenReturn(true);
        mvc.perform(get("/participant/stop")
                .param(USER_PARAM, ID_STRING)
                .param(EXPERIMENT_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_FINISH + ID));
        verify(userService).getUserById(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService).simultaneousParticipation(ID);
        verify(participantService).updateParticipant(participantDTO);
        verify(userService, never()).saveUser(any());
    }

    @Test
    public void testStopExperimentParticipantNotUpdated() throws Exception {
        participantDTO.setStart(LocalDateTime.now());
        when(userService.getUserById(ID)).thenReturn(userDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        mvc.perform(get("/participant/stop")
                .param(USER_PARAM, ID_STRING)
                .param(EXPERIMENT_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService).getUserById(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService).simultaneousParticipation(ID);
        verify(participantService).updateParticipant(participantDTO);
        verify(userService, never()).saveUser(any());
    }

    @Test
    public void testStopExperimentUserNotFound() throws Exception {
        when(userService.getUserById(ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/participant/stop")
                .param(USER_PARAM, ID_STRING)
                .param(EXPERIMENT_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService).getUserById(ID);
        verify(participantService, never()).getParticipant(anyInt(), anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(participantService, never()).updateParticipant(any());
        verify(userService, never()).saveUser(any());
    }

    @Test
    @WithMockUser(username = PARTICIPANT, roles = {"ADMIN"})
    public void testStopExperimentUserAdmin() throws Exception {
        mvc.perform(get("/participant/stop")
                .param(USER_PARAM, ID_STRING)
                .param(EXPERIMENT_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService, never()).getUserById(anyInt());
        verify(participantService, never()).getParticipant(anyInt(), anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(participantService, never()).updateParticipant(any());
        verify(userService, never()).saveUser(any());
    }

    @Test
    public void testStopExperimentUserIdInvalid() throws Exception {
        mvc.perform(get("/participant/stop")
                .param(USER_PARAM, BLANK)
                .param(EXPERIMENT_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService, never()).getUserById(anyInt());
        verify(participantService, never()).getParticipant(anyInt(), anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(participantService, never()).updateParticipant(any());
        verify(userService, never()).saveUser(any());
    }

    @Test
    public void testStopExperimentExperimentIdInvalid() throws Exception {
        mvc.perform(get("/participant/stop")
                .param(USER_PARAM, ID_STRING)
                .param(EXPERIMENT_PARAM, PARTICIPANT)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService, never()).getUserById(anyInt());
        verify(participantService, never()).getParticipant(anyInt(), anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(participantService, never()).updateParticipant(any());
        verify(userService, never()).saveUser(any());
    }
}
