package fim.unipassau.de.scratch1984.integration;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.EventService;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.application.service.FileService;
import fim.unipassau.de.scratch1984.persistence.projection.ExperimentProjection;
import fim.unipassau.de.scratch1984.spring.configuration.SecurityTestConfig;
import fim.unipassau.de.scratch1984.web.controller.EventRestController;
import org.json.JSONException;
import org.json.JSONObject;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(EventRestController.class)
@Import(SecurityTestConfig.class)
@ActiveProfiles("test")
public class EventRestControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private EventService eventService;

    @MockBean
    private FileService fileService;

    @MockBean
    private ExperimentService experimentService;

    private static final String ID_PARAM = "id";
    private static final String USER_PARAM = "user";
    private static final String EXPERIMENT_PARAM = "experiment";
    private static final String ID_STRING = "1";
    private static final String JSON = "json";
    private static final int ID = 1;
    private final JSONObject blockEventObject = new JSONObject();
    private final JSONObject clickEventObject = new JSONObject();
    private final JSONObject debuggerEventObject = new JSONObject();
    private final JSONObject questionEventObject = new JSONObject();
    private final JSONObject resourceEventObject = new JSONObject();
    private final JSONObject fileEventObject = new JSONObject();
    private final JSONObject sb3ZipObject = new JSONObject();
    private final ExperimentProjection experimentProjection = new ExperimentProjection() {
        @Override
        public Integer getId() {
            return 1;
        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public byte[] getProject() {
            return new byte[]{1, 2, 3};
        }
    };
    private final String TOKEN_ATTR_NAME = "org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN";
    private final HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
    private final CsrfToken csrfToken = httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());

    @BeforeEach
    public void setup() throws JSONException {
        blockEventObject.put("user", 3);
        blockEventObject.put("experiment", 39);
        blockEventObject.put("type", "DRAG");
        blockEventObject.put("time", "2021-06-28T12:36:37.601Z");
        blockEventObject.put("event", "ENDDRAG");
        blockEventObject.put("metadata", "meta");
        blockEventObject.put("spritename", "Figur1");
        blockEventObject.put("xml", "xml");
        blockEventObject.put("json", "json");
        clickEventObject.put("user", 3);
        clickEventObject.put("experiment", 39);
        clickEventObject.put("type", "ICON");
        clickEventObject.put("time", "2021-06-28T12:36:37.601Z");
        clickEventObject.put("event", "STOPALL");
        clickEventObject.put("metadata", "meta");
        debuggerEventObject.put("user", 3);
        debuggerEventObject.put("experiment", 39);
        debuggerEventObject.put("type", "SPRITE");
        debuggerEventObject.put("time", "2021-06-28T12:36:37.601Z");
        debuggerEventObject.put("event", "SELECT_SPRITE");
        debuggerEventObject.put("id", "id");
        debuggerEventObject.put("name", "name");
        debuggerEventObject.put("original", 1);
        debuggerEventObject.put("execution", 5);
        questionEventObject.put("user", 3);
        questionEventObject.put("experiment", 39);
        questionEventObject.put("type", "QUESTION");
        questionEventObject.put("time", "2021-06-28T12:36:37.601Z");
        questionEventObject.put("event", "SELECT");
        questionEventObject.put("feedback", 1);
        questionEventObject.put("q_type", "block-execution");
        questionEventObject.put("values", "Cat, Costume");
        questionEventObject.put("category", "execution");
        questionEventObject.put("form", "negative");
        questionEventObject.put("id", "id");
        questionEventObject.put("opcode", "opcode");
        resourceEventObject.put("user", 3);
        resourceEventObject.put("experiment", 39);
        resourceEventObject.put("type", "DELETE");
        resourceEventObject.put("time", "2021-06-28T12:36:37.601Z");
        resourceEventObject.put("event", "DELETE_SOUND");
        resourceEventObject.put("name", "Miau");
        resourceEventObject.put("md5", "md5");
        resourceEventObject.put("dataFormat", "wav");
        resourceEventObject.put("libraryResource", "UNKNOWN");
        fileEventObject.put("user", 3);
        fileEventObject.put("experiment", 39);
        fileEventObject.put("name", "Miau.wav");
        fileEventObject.put("type", "audio/x-wav");
        fileEventObject.put("file", "blub");
        fileEventObject.put("time", "2021-06-28T12:36:37.601Z");
        sb3ZipObject.put("user", 3);
        sb3ZipObject.put("experiment", 39);
        sb3ZipObject.put("name", "sb3zip.sb3");
        sb3ZipObject.put("time", "2021-06-28T12:36:37.601Z");
        sb3ZipObject.put("zip", "blub");
    }

    @AfterEach
    public void resetService() {
        reset(eventService, fileService);
    }

    @Test
    public void testStoreBlockEvent() throws Exception {
        mvc.perform(post("/store/block")
                        .content(blockEventObject.toString())
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(eventService).saveBlockEvent(any());
    }

    @Test
    public void testStoreBlockEventJSON() throws Exception {
        blockEventObject.put("experiment", "no");
        mvc.perform(post("/store/block")
                        .content(blockEventObject.toString())
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(eventService, never()).saveBlockEvent(any());
    }

    @Test
    public void testStoreBlockEventIllegalArgument() throws Exception {
        blockEventObject.put("type", "no");
        mvc.perform(post("/store/block")
                        .content(blockEventObject.toString())
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(eventService, never()).saveBlockEvent(any());
    }

    @Test
    public void testStoreBlockEventDateTimeParse() throws Exception {
        blockEventObject.put("time", "no");
        mvc.perform(post("/store/block")
                        .content(blockEventObject.toString())
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(eventService, never()).saveBlockEvent(any());
    }

    @Test
    public void testStoreClickEvent() throws Exception {
        mvc.perform(post("/store/click")
                        .content(clickEventObject.toString())
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(eventService).saveClickEvent(any());
    }

    @Test
    public void testStoreClickEventJSON() throws Exception {
        clickEventObject.put("user", "unicorn");
        mvc.perform(post("/store/click")
                        .content(clickEventObject.toString())
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(eventService, never()).saveClickEvent(any());
    }

    @Test
    public void testStoreClickEventIllegalArgument() throws Exception {
        clickEventObject.put("event", "unicorn");
        mvc.perform(post("/store/click")
                        .content(clickEventObject.toString())
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(eventService, never()).saveClickEvent(any());
    }

    @Test
    public void testStoreClickEventTimeParse() throws Exception {
        clickEventObject.put("time", "-1");
        mvc.perform(post("/store/click")
                        .content(clickEventObject.toString())
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(eventService, never()).saveClickEvent(any());
    }

    @Test
    public void testStoreDebuggerEvent() throws Exception {
        mvc.perform(post("/store/debugger")
                        .content(debuggerEventObject.toString())
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(eventService).saveDebuggerEvent(any());
    }

    @Test
    public void testStoreDebuggerEventJSON() throws Exception {
        debuggerEventObject.put("user", "user");
        mvc.perform(post("/store/debugger")
                        .content(debuggerEventObject.toString())
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(eventService, never()).saveDebuggerEvent(any());
    }

    @Test
    public void testStoreDebuggerEventTimeParse() throws Exception {
        debuggerEventObject.put("time", ".");
        mvc.perform(post("/store/debugger")
                        .content(debuggerEventObject.toString())
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(eventService, never()).saveDebuggerEvent(any());
    }

    @Test
    public void testStoreQuestionEvent() throws Exception {
        mvc.perform(post("/store/question")
                        .content(questionEventObject.toString())
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(eventService).saveQuestionEvent(any());
    }

    @Test
    public void testStoreQuestionEventJSON() throws Exception {
        questionEventObject.put("experiment", "exp");
        mvc.perform(post("/store/question")
                        .content(questionEventObject.toString())
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(eventService, never()).saveQuestionEvent(any());
    }

    @Test
    public void testStoreQuestionEventIllegalArgument() throws Exception {
        questionEventObject.put("type", "BREAKPOINT");
        mvc.perform(post("/store/question")
                        .content(questionEventObject.toString())
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(eventService, never()).saveQuestionEvent(any());
    }

    @Test
    public void testStoreQuestionEventTimeParse() throws Exception {
        questionEventObject.put("time", "time");
        mvc.perform(post("/store/question")
                        .content(questionEventObject.toString())
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(eventService, never()).saveQuestionEvent(any());
    }

    @Test
    public void testStoreResourceEvent() throws Exception {
        mvc.perform(post("/store/resource")
                        .content(resourceEventObject.toString())
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(eventService).saveResourceEvent(any());
    }

    @Test
    public void testStoreResourceEventJSON() throws Exception {
        resourceEventObject.put("user", "user");
        mvc.perform(post("/store/resource")
                        .content(resourceEventObject.toString())
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(eventService, never()).saveResourceEvent(any());
    }

    @Test
    public void testStoreResourceEventIllegalArgument() throws Exception {
        resourceEventObject.put("event", "user");
        mvc.perform(post("/store/resource")
                        .content(resourceEventObject.toString())
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(eventService, never()).saveResourceEvent(any());
    }

    @Test
    public void testStoreResourceEventDateTimeParse() throws Exception {
        resourceEventObject.put("time", "user");
        mvc.perform(post("/store/resource")
                        .content(resourceEventObject.toString())
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(eventService, never()).saveResourceEvent(any());
    }

    @Test
    public void testStoreFileEvent() throws Exception {
        mvc.perform(post("/store/file")
                        .content(fileEventObject.toString())
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(fileService).saveFile(any());
    }

    @Test
    public void testStoreFileEventJSON() throws Exception {
        fileEventObject.put("user", "unicorn");
        mvc.perform(post("/store/file")
                        .content(fileEventObject.toString())
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(fileService, never()).saveFile(any());
    }

    @Test
    public void testStoreFileEventIllegalArgument() throws Exception {
        fileEventObject.put("file", "%");
        mvc.perform(post("/store/file")
                        .content(fileEventObject.toString())
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(fileService, never()).saveFile(any());
    }

    @Test
    public void testStoreFileEventDateTimeParse() throws Exception {
        fileEventObject.put("time", "%");
        mvc.perform(post("/store/file")
                        .content(fileEventObject.toString())
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(fileService, never()).saveFile(any());
    }

    @Test
    public void testStoreZipFile() throws Exception {
        mvc.perform(post("/store/zip")
                        .content(sb3ZipObject.toString())
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(fileService).saveSb3Zip(any());
    }

    @Test
    public void testStoreZipFileJSON() throws Exception {
        sb3ZipObject.put("user", "unicorn");
        mvc.perform(post("/store/zip")
                        .content(sb3ZipObject.toString())
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(fileService, never()).saveSb3Zip(any());
    }

    @Test
    public void testStoreZipFileIllegalArgument() throws Exception {
        sb3ZipObject.put("zip", "%");
        mvc.perform(post("/store/zip")
                        .content(sb3ZipObject.toString())
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(fileService, never()).saveSb3Zip(any());
    }

    @Test
    public void testStoreZipFileDateTimeParse() throws Exception {
        sb3ZipObject.put("time", "%");
        mvc.perform(post("/store/zip")
                        .content(sb3ZipObject.toString())
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(fileService, never()).saveSb3Zip(any());
    }

    @Test
    public void testRetrieveSb3File() throws Exception {
        when(experimentService.getSb3File(ID)).thenReturn(experimentProjection);
        mvc.perform(get("/store/sb3")
                        .param(ID_PARAM, ID_STRING)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(experimentService).getSb3File(ID);
    }

    @Test
    public void testRetrieveSb3FileProjectionNull() throws Exception {
        when(experimentService.getSb3File(ID)).thenReturn(new ExperimentProjection() {
            @Override
            public Integer getId() {
                return null;
            }

            @Override
            public boolean isActive() {
                return true;
            }

            @Override
            public byte[] getProject() {
                return null;
            }
        });
        mvc.perform(get("/store/sb3")
                        .param(ID_PARAM, ID_STRING)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(experimentService).getSb3File(ID);
    }

    @Test
    public void testRetrieveSb3FileNotFound() throws Exception {
        when(experimentService.getSb3File(ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/store/sb3")
                        .param(ID_PARAM, ID_STRING)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(experimentService).getSb3File(ID);
    }

    @Test
    public void testRetrieveSb3FileInvalidId() throws Exception {
        mvc.perform(get("/store/sb3")
                        .param(ID_PARAM, "0")
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(experimentService, never()).getSb3File(anyInt());
    }

    @Test
    public void testRetrieveLastJson() throws Exception {
        when(eventService.findFirstJSON(ID, ID)).thenReturn(JSON);
        mvc.perform(get("/store/json")
                        .param(USER_PARAM, ID_STRING)
                        .param(EXPERIMENT_PARAM, ID_STRING)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(eventService).findFirstJSON(ID, ID);
    }

    @Test
    public void testRetrieveLastJsonJsonNull() throws Exception {
        mvc.perform(get("/store/json")
                        .param(USER_PARAM, ID_STRING)
                        .param(EXPERIMENT_PARAM, ID_STRING)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(eventService).findFirstJSON(ID, ID);
    }

    @Test
    public void testRetrieveLastJsonNotFound() throws Exception {
        when(eventService.findFirstJSON(ID, ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/store/json")
                        .param(USER_PARAM, ID_STRING)
                        .param(EXPERIMENT_PARAM, ID_STRING)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(eventService).findFirstJSON(ID, ID);
    }

    @Test
    public void testRetrieveLastJsonInvalidIds() throws Exception {
        mvc.perform(get("/store/json")
                        .param(USER_PARAM, "-1")
                        .param(EXPERIMENT_PARAM, JSON)
                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                        .param(csrfToken.getParameterName(), csrfToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(eventService, never()).findFirstJSON(anyInt(), anyInt());
    }
}
