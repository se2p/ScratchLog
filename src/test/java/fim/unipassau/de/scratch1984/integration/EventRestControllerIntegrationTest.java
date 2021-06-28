package fim.unipassau.de.scratch1984.integration;

import fim.unipassau.de.scratch1984.application.service.EventService;
import fim.unipassau.de.scratch1984.application.service.FileService;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
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

    private final JSONObject blockEventObject = new JSONObject();
    private final JSONObject resourceEventObject = new JSONObject();
    private final JSONObject fileEventObject = new JSONObject();
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
    public void testStoreFile() throws Exception {
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
    public void testStoreFileJSON() throws Exception {
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
    public void testStoreFileIllegalArgument() throws Exception {
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
    public void testStoreFileDateTimeParse() throws Exception {
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
}
