package fim.unipassau.de.scratch1984.integration;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.application.service.MailService;
import fim.unipassau.de.scratch1984.application.service.ParticipantService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.spring.configuration.SecurityTestConfig;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.web.controller.ParticipantController;
import fim.unipassau.de.scratch1984.web.dto.ExperimentDTO;
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

import javax.mail.MessagingException;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
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
    private static final String EMAIL = "participant@participant.de";
    private static final String BLANK = "   ";
    private static final String ID_STRING = "1";
    private static final String EXPERIMENT = "experiment";
    private static final String USER_DTO = "userDTO";
    private static final int ID = 1;
    private static final int LAST_ID = ID + 1;
    private final UserDTO userDTO = new UserDTO(PARTICIPANT, EMAIL, UserDTO.Role.ADMIN,
            UserDTO.Language.ENGLISH, "password", "secret");
    private final ExperimentDTO experimentDTO = new ExperimentDTO(ID, "title", "description", "info", true);
    private final String TOKEN_ATTR_NAME = "org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN";
    private final HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
    private final CsrfToken csrfToken = httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());

    @BeforeEach
    public void setup() {
        userDTO.setId(ID);
        userDTO.setUsername(PARTICIPANT);
        userDTO.setEmail(EMAIL);
        experimentDTO.setActive(true);
    }

    @AfterEach
    public void resetService() {reset(userService, experimentService, participantService, mailService);}

    @Test
    public void testGetParticipantForm() throws Exception {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.findLastId()).thenReturn(ID);
        mvc.perform(get("/participant/add")
                .param("id", ID_STRING)
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
                .param("id", ID_STRING)
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
                .param("id", ID_STRING)
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
                .param("id", "0")
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
        when(userService.saveUser(userDTO)).thenReturn(userDTO);
        mvc.perform(post("/participant/add")
                .flashAttr(USER_DTO, userDTO)
                .param("id", ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_EXPERIMENT + ID));
        verify(userService).saveUser(userDTO);
        verify(participantService).saveParticipant(userDTO.getId(), ID);
        verify(mailService).sendTemplateMessage(anyString(), any(), any(), any(), anyString(), any(), anyString());
    }

    @Test
    public void testAddParticipantMessagingError() throws Exception {
        when(userService.saveUser(userDTO)).thenReturn(userDTO);
        doThrow(MessagingException.class).when(mailService).sendTemplateMessage(anyString(), any(), any(), any(),
                anyString(), any(), anyString());
        mvc.perform(post("/participant/add")
                .flashAttr(USER_DTO, userDTO)
                .param("id", ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService).saveUser(userDTO);
        verify(participantService).saveParticipant(userDTO.getId(), ID);
        verify(mailService, times(Constants.MAX_EMAIL_TRIES)).sendTemplateMessage(anyString(), any(), any(), any(),
                anyString(), any(), anyString());
    }

    @Test
    public void testAddParticipantNotFound() throws Exception {
        when(userService.saveUser(userDTO)).thenReturn(userDTO);
        doThrow(NotFoundException.class).when(participantService).saveParticipant(userDTO.getId(), ID);
        mvc.perform(post("/participant/add")
                .flashAttr(USER_DTO, userDTO)
                .param("id", ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService).saveUser(userDTO);
        verify(participantService).saveParticipant(userDTO.getId(), ID);
        verify(mailService, never()).sendTemplateMessage(anyString(), any(), any(), any(), anyString(), any(),
                anyString());
    }

    @Test
    public void testAddParticipantUsernameAndEmailExists() throws Exception {
        when(userService.existsUser(PARTICIPANT)).thenReturn(true);
        when(userService.existsEmail(EMAIL)).thenReturn(true);
        mvc.perform(post("/participant/add")
                .flashAttr(USER_DTO, userDTO)
                .param("id", ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(EXPERIMENT, is(ID)))
                .andExpect(view().name(PARTICIPANT));
        verify(userService, never()).saveUser(userDTO);
        verify(participantService, never()).saveParticipant(userDTO.getId(), ID);
        verify(mailService, never()).sendTemplateMessage(anyString(), any(), any(), any(), anyString(), any(),
                anyString());
    }

    @Test
    public void testAddParticipantInvalidUsername() throws Exception {
        userDTO.setUsername(BLANK);
        mvc.perform(post("/participant/add")
                .flashAttr(USER_DTO, userDTO)
                .param("id", ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(EXPERIMENT, is(ID)))
                .andExpect(view().name(PARTICIPANT));
        verify(userService, never()).saveUser(userDTO);
        verify(participantService, never()).saveParticipant(userDTO.getId(), ID);
        verify(mailService, never()).sendTemplateMessage(anyString(), any(), any(), any(), anyString(), any(),
                anyString());
    }

    @Test
    public void testAddParticipantInvalidEmail() throws Exception {
        userDTO.setEmail(PARTICIPANT);
        mvc.perform(post("/participant/add")
                .flashAttr(USER_DTO, userDTO)
                .param("id", ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(EXPERIMENT, is(ID)))
                .andExpect(view().name(PARTICIPANT));
        verify(userService, never()).saveUser(userDTO);
        verify(participantService, never()).saveParticipant(userDTO.getId(), ID);
        verify(mailService, never()).sendTemplateMessage(anyString(), any(), any(), any(), anyString(), any(),
                anyString());
    }

    @Test
    public void testAddParticipantExperimentIdInvalid() throws Exception {
        mvc.perform(post("/participant/add")
                .flashAttr(USER_DTO, userDTO)
                .param("id", "-1")
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService, never()).saveUser(userDTO);
        verify(participantService, never()).saveParticipant(userDTO.getId(), ID);
        verify(mailService, never()).sendTemplateMessage(anyString(), any(), any(), any(), anyString(), any(),
                anyString());
    }
}
