package fim.unipassau.de.scratch1984.integration;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.spring.configuration.SecurityTestConfig;
import fim.unipassau.de.scratch1984.web.controller.ExperimentController;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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

    private static final String TITLE = "My Experiment";
    private static final String DESCRIPTION = "A description";
    private static final String INFO = "Some info text";
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
    private static final int ID = 1;
    private final ExperimentDTO experimentDTO = new ExperimentDTO(ID, TITLE, DESCRIPTION, INFO, false);
    private final UserDTO userDTO = new UserDTO("user", "admin1@admin.de", UserDTO.Role.ADMIN,
            UserDTO.Language.ENGLISH, "admin", "secret1");
    private final String TOKEN_ATTR_NAME = "org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN";
    private final HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
    private final CsrfToken csrfToken = httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());

    @BeforeEach
    public void setup() {
        userDTO.setId(ID);
        experimentDTO.setId(ID);
        experimentDTO.setTitle(TITLE);
        experimentDTO.setDescription(DESCRIPTION);
        experimentDTO.setActive(false);
    }

    @AfterEach
    public void resetService() {
        reset(experimentService, userService);
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void testGetExperiment() throws Exception {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        mvc.perform(get("/experiment")
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
                        hasProperty("info", is(INFO_PARSED))
                )))
                .andExpect(view().name(EXPERIMENT));
        verify(experimentService).getExperiment(ID);
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
        when(userService.getUser(anyString())).thenReturn(userDTO);
        when(userService.existsParticipant(ID, ID)).thenReturn(true);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        mvc.perform(get("/experiment")
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
                        hasProperty("info", is(INFO_PARSED))
                )))
                .andExpect(view().name(EXPERIMENT));
        verify(userService).getUser(anyString());
        verify(userService).existsParticipant(ID, ID);
        verify(experimentService).getExperiment(ID);
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
        mvc.perform(get("/experiment")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService).getUser(anyString());
        verify(userService).existsParticipant(ID, ID);
        verify(experimentService, never()).getExperiment(ID);
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
                        hasProperty("info", is(INFO))
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
        when(experimentService.changeExperimentStatus(true, ID)).thenReturn(experimentDTO);
        mvc.perform(get("/experiment/status")
                .param(STATUS_PARAM, "open")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(model().attribute("experimentDTO", allOf(
                        hasProperty("id", is(ID)),
                        hasProperty("title", is(TITLE)),
                        hasProperty("description", is(DESCRIPTION)),
                        hasProperty("info", is(INFO_PARSED)),
                        hasProperty("active", is(true))
                )))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT));
        verify(experimentService).changeExperimentStatus(true, ID);
    }

    @Test
    public void testChangeExperimentStatusClose() throws Exception {
        when(experimentService.changeExperimentStatus(false, ID)).thenReturn(experimentDTO);
        mvc.perform(get("/experiment/status")
                .param(STATUS_PARAM, "close")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(model().attribute("experimentDTO", allOf(
                        hasProperty("id", is(ID)),
                        hasProperty("title", is(TITLE)),
                        hasProperty("description", is(DESCRIPTION)),
                        hasProperty("info", is(INFO_PARSED)),
                        hasProperty("active", is(false))
                )))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT));
        verify(experimentService).changeExperimentStatus(false, ID);
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
}
