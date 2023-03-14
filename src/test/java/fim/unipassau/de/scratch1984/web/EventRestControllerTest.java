package fim.unipassau.de.scratch1984.web;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.EventService;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.application.service.FileService;
import fim.unipassau.de.scratch1984.application.service.ParticipantService;
import fim.unipassau.de.scratch1984.persistence.projection.ExperimentProjection;
import fim.unipassau.de.scratch1984.web.controller.EventRestController;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EventRestControllerTest {

    @InjectMocks
    private EventRestController eventRestController;

    @Mock
    private EventService eventService;

    @Mock
    private FileService fileService;

    @Mock
    private ExperimentService experimentService;

    @Mock
    private ParticipantService participantService;

    @Mock
    private HttpServletResponse httpServletResponse;

    private static final String JSON = "json";
    private static final String SECRET = "secret";
    private static final int USER_ID = 3;
    private static final int Experiment_ID = 39;
    private final JSONObject blockEventObject = new JSONObject();
    private final JSONObject clickEventObject = new JSONObject();
    private final JSONObject debuggerEventObject = new JSONObject();
    private final JSONObject questionEventObject = new JSONObject();
    private final JSONObject resourceEventObject = new JSONObject();
    private final JSONObject fileEventObject = new JSONObject();
    private final JSONObject sb3ZipObject = new JSONObject();
    private final JSONObject dataObject = new JSONObject();
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

    @BeforeEach
    public void setup() throws JSONException {
        blockEventObject.put("user", USER_ID);
        blockEventObject.put("experiment", Experiment_ID);
        blockEventObject.put(SECRET, SECRET);
        blockEventObject.put("type", "DRAG");
        blockEventObject.put("time", "2021-06-28T12:36:37.601Z");
        blockEventObject.put("event", "ENDDRAG");
        blockEventObject.put("metadata", "meta");
        blockEventObject.put("spritename", "Figur1");
        blockEventObject.put("xml", "xml");
        blockEventObject.put("json", "json");
        clickEventObject.put("user", USER_ID);
        clickEventObject.put("experiment", Experiment_ID);
        clickEventObject.put(SECRET, SECRET);
        clickEventObject.put("type", "ICON");
        clickEventObject.put("time", "2021-06-28T12:36:37.601Z");
        clickEventObject.put("event", "STOPALL");
        clickEventObject.put("metadata", "meta");
        debuggerEventObject.put("user", USER_ID);
        debuggerEventObject.put("experiment", Experiment_ID);
        debuggerEventObject.put(SECRET, SECRET);
        debuggerEventObject.put("type", "SPRITE");
        debuggerEventObject.put("time", "2021-06-28T12:36:37.601Z");
        debuggerEventObject.put("event", "SELECT_SPRITE");
        debuggerEventObject.put("scratchId", "id");
        debuggerEventObject.put("name", "name");
        debuggerEventObject.put("original", 1);
        debuggerEventObject.put("execution", 5);
        questionEventObject.put("user", USER_ID);
        questionEventObject.put("experiment", Experiment_ID);
        questionEventObject.put(SECRET, SECRET);
        questionEventObject.put("type", "QUESTION");
        questionEventObject.put("time", "2021-06-28T12:36:37.601Z");
        questionEventObject.put("event", "SELECT");
        questionEventObject.put("feedback", 1);
        questionEventObject.put("q_type", "block-execution");
        questionEventObject.put("values", new String[]{"Cat", "Costume"});
        questionEventObject.put("category", "execution");
        questionEventObject.put("form", "negative");
        questionEventObject.put("blockID", "id");
        questionEventObject.put("opcode", "opcode");
        resourceEventObject.put("user", USER_ID);
        resourceEventObject.put("experiment", Experiment_ID);
        resourceEventObject.put(SECRET, SECRET);
        resourceEventObject.put("type", "DELETE");
        resourceEventObject.put("time", "2021-06-28T12:36:37.601Z");
        resourceEventObject.put("event", "DELETE_SOUND");
        resourceEventObject.put("name", "Miau");
        resourceEventObject.put("md5", "md5");
        resourceEventObject.put("dataFormat", "wav");
        resourceEventObject.put("libraryResource", "UNKNOWN");
        fileEventObject.put("user", USER_ID);
        fileEventObject.put("experiment", Experiment_ID);
        fileEventObject.put(SECRET, SECRET);
        fileEventObject.put("name", "Miau.wav");
        fileEventObject.put("type", "audio/x-wav");
        fileEventObject.put("file", "blub");
        fileEventObject.put("time", "2021-06-28T12:36:37.601Z");
        sb3ZipObject.put("user", USER_ID);
        sb3ZipObject.put("experiment", Experiment_ID);
        sb3ZipObject.put(SECRET, SECRET);
        sb3ZipObject.put("name", "sb3zip.sb3");
        sb3ZipObject.put("time", "2021-06-28T12:36:37.601Z");
        sb3ZipObject.put("zip", "blub");
        dataObject.put("user", USER_ID);
        dataObject.put("experiment", Experiment_ID);
        dataObject.put(SECRET, SECRET);
    }

    @Test
    public void testStoreBlockEvent() {
        assertDoesNotThrow(
                () -> eventRestController.storeBlockEvent(blockEventObject.toString())
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET);
        verify(eventService).saveBlockEvent(any());
    }

    @Test
    public void testStoreBlockEventInvalidParticipant() {
        when(participantService.isInvalidParticipant(USER_ID, Experiment_ID, SECRET)).thenReturn(true);
        assertDoesNotThrow(
                () -> eventRestController.storeBlockEvent(blockEventObject.toString())
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET);
        verify(eventService, never()).saveBlockEvent(any());
    }

    @Test
    public void testStoreBlockEventJsonProcessing() throws JSONException {
        blockEventObject.put("time", "0");
        assertDoesNotThrow(
                () -> eventRestController.storeBlockEvent(blockEventObject.toString())
        );
        verify(participantService, never()).isInvalidParticipant(anyInt(), anyInt(), anyString());
        verify(eventService, never()).saveBlockEvent(any());
    }

    @Test
    public void testStoreClickEvent() throws JSONException {
        assertDoesNotThrow(
                () -> eventRestController.storeClickEvent(clickEventObject.toString())
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET);
        verify(eventService).saveClickEvent(any());
    }

    @Test
    public void testStoreClickEventInvalidParticipant() {
        when(participantService.isInvalidParticipant(USER_ID, Experiment_ID, SECRET)).thenReturn(true);
        assertDoesNotThrow(
                () -> eventRestController.storeClickEvent(clickEventObject.toString())
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET);
        verify(eventService, never()).saveBlockEvent(any());
    }

    @Test
    public void testStoreClickEventJsonProcessing() throws JSONException {
        clickEventObject.put("event", "");
        assertDoesNotThrow(
                () -> eventRestController.storeClickEvent(clickEventObject.toString())
        );
        verify(participantService, never()).isInvalidParticipant(anyInt(), anyInt(), anyString());
        verify(eventService, never()).saveClickEvent(any());
    }

    @Test
    public void testStoreDebuggerEvent() {
        assertDoesNotThrow(
                () -> eventRestController.storeDebuggerEvent(debuggerEventObject.toString())
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET);
        verify(eventService).saveDebuggerEvent(any());
    }

    @Test
    public void testStoreDebuggerEventInvalidParticipant() {
        when(participantService.isInvalidParticipant(USER_ID, Experiment_ID, SECRET)).thenReturn(true);
        assertDoesNotThrow(
                () -> eventRestController.storeDebuggerEvent(debuggerEventObject.toString())
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET);
        verify(eventService, never()).saveBlockEvent(any());
    }

    @Test
    public void testStoreDebuggerEventJsonProcessing() {
        debuggerEventObject.put("original", "one");
        assertDoesNotThrow(
                () -> eventRestController.storeDebuggerEvent(debuggerEventObject.toString())
        );
        verify(participantService, never()).isInvalidParticipant(anyInt(), anyInt(), anyString());
        verify(eventService, never()).saveDebuggerEvent(any());
    }

    @Test
    public void testStoreQuestionEvent() {
        assertDoesNotThrow(
                () -> eventRestController.storeQuestionEvent(questionEventObject.toString())
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET);
        verify(eventService).saveQuestionEvent(any());
    }

    @Test
    public void testStoreQuestionEventInvalidParticipant() {
        when(participantService.isInvalidParticipant(USER_ID, Experiment_ID, SECRET)).thenReturn(true);
        assertDoesNotThrow(
                () -> eventRestController.storeQuestionEvent(questionEventObject.toString())
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET);
        verify(eventService, never()).saveBlockEvent(any());
    }

    @Test
    public void testStoreQuestionEventJsonProcessing() {
        questionEventObject.put("feedback", "no");
        assertDoesNotThrow(
                () -> eventRestController.storeQuestionEvent(questionEventObject.toString())
        );
        verify(participantService, never()).isInvalidParticipant(anyInt(), anyInt(), anyString());
        verify(eventService, never()).saveQuestionEvent(any());
    }

    @Test
    public void testStoreResourceEvent() {
        assertDoesNotThrow(
                () -> eventRestController.storeResourceEvent(resourceEventObject.toString())
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET);
        verify(eventService).saveResourceEvent(any());
    }

    @Test
    public void testStoreResourceEventInvalidParticipant() {
        when(participantService.isInvalidParticipant(USER_ID, Experiment_ID, SECRET)).thenReturn(true);
        assertDoesNotThrow(
                () -> eventRestController.storeResourceEvent(resourceEventObject.toString())
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET);
        verify(eventService, never()).saveBlockEvent(any());
    }

    @Test
    public void testStoreResourceEventJsonProcessing() throws JSONException {
        resourceEventObject.put("event", "");
        assertDoesNotThrow(
                () -> eventRestController.storeResourceEvent(resourceEventObject.toString())
        );
        verify(participantService, never()).isInvalidParticipant(anyInt(), anyInt(), anyString());
        verify(eventService, never()).saveResourceEvent(any());
    }

    @Test
    public void testStoreFileEvent() {
        assertDoesNotThrow(
                () -> eventRestController.storeFileEvent(fileEventObject.toString())
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET);
        verify(fileService).saveFile(any());
    }

    @Test
    public void testStoreFileEventInvalidParticipant() {
        when(participantService.isInvalidParticipant(USER_ID, Experiment_ID, SECRET)).thenReturn(true);
        assertDoesNotThrow(
                () -> eventRestController.storeFileEvent(fileEventObject.toString())
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET);
        verify(eventService, never()).saveBlockEvent(any());
    }

    @Test
    public void testStoreFileEventJsonProcessing() throws JSONException {
        fileEventObject.put("user", "theGordon");
        assertDoesNotThrow(
                () -> eventRestController.storeFileEvent(fileEventObject.toString())
        );
        verify(participantService, never()).isInvalidParticipant(anyInt(), anyInt(), anyString());
        verify(fileService, never()).saveFile(any());
    }

    @Test
    public void testStoreZipFile() {
        assertDoesNotThrow(
                () -> eventRestController.storeZipFile(sb3ZipObject.toString())
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET);
        verify(fileService).saveSb3Zip(any());
    }

    @Test
    public void testStoreZipFileInvalidParticipant() {
        when(participantService.isInvalidParticipant(USER_ID, Experiment_ID, SECRET)).thenReturn(true);
        assertDoesNotThrow(
                () -> eventRestController.storeZipFile(sb3ZipObject.toString())
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET);
        verify(eventService, never()).saveBlockEvent(any());
    }

    @Test
    public void testStoreZipFileJsonProcessing() {
        sb3ZipObject.put("time", "%");
        assertDoesNotThrow(
                () -> eventRestController.storeZipFile(sb3ZipObject.toString())
        );
        verify(participantService, never()).isInvalidParticipant(anyInt(), anyInt(), anyString());
        verify(fileService, never()).saveSb3Zip(any());
    }

    @Test
    public void testRetrieveSb3File() throws IOException {
        when(experimentService.getSb3File(Experiment_ID)).thenReturn(experimentProjection);
        when(httpServletResponse.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            @Override
            public void write(int b) throws IOException {

            }
        });
        assertDoesNotThrow(
                () -> eventRestController.retrieveSb3File(dataObject.toString(), httpServletResponse)
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET);
        verify(experimentService).getSb3File(Experiment_ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
        verify(httpServletResponse, never()).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testRetrieveSb3FileIO() throws IOException {
        when(experimentService.getSb3File(Experiment_ID)).thenReturn(experimentProjection);
        when(httpServletResponse.getOutputStream()).thenThrow(IOException.class);
        assertDoesNotThrow(
                () -> eventRestController.retrieveSb3File(dataObject.toString(), httpServletResponse)
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET);
        verify(experimentService).getSb3File(Experiment_ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testRetrieveSb3FileProjectNull() throws IOException {
        when(experimentService.getSb3File(Experiment_ID)).thenReturn(new ExperimentProjection() {
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
        assertDoesNotThrow(
                () -> eventRestController.retrieveSb3File(dataObject.toString(), httpServletResponse)
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET);
        verify(experimentService).getSb3File(Experiment_ID);
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setContentType(anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testRetrieveSb3FileNotFound() throws IOException {
        when(experimentService.getSb3File(Experiment_ID)).thenThrow(NotFoundException.class);
        assertDoesNotThrow(
                () -> eventRestController.retrieveSb3File(dataObject.toString(), httpServletResponse)
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET);
        verify(experimentService).getSb3File(Experiment_ID);
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setContentType(anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testRetrieveSb3FileInvalidParticipant() throws IOException {
        when(participantService.isInvalidParticipant(USER_ID, Experiment_ID, SECRET)).thenReturn(true);
        assertDoesNotThrow(
                () -> eventRestController.retrieveSb3File(dataObject.toString(), httpServletResponse)
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET);
        verify(experimentService, never()).getSb3File(anyInt());
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setContentType(anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void testRetrieveLastJson() throws IOException {
        when(eventService.findFirstJSON(USER_ID, Experiment_ID)).thenReturn(JSON);
        when(httpServletResponse.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            @Override
            public void write(int b) throws IOException {

            }
        });
        assertDoesNotThrow(
                () -> eventRestController.retrieveLastJson(dataObject.toString(), httpServletResponse)
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET);
        verify(eventService).findFirstJSON(USER_ID, Experiment_ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/json");
        verify(httpServletResponse).setCharacterEncoding("UTF-8");
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testRetrieveLastJsonJsonNull() throws IOException {
        assertDoesNotThrow(
                () -> eventRestController.retrieveLastJson(dataObject.toString(), httpServletResponse)
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET);
        verify(eventService).findFirstJSON(USER_ID, Experiment_ID);
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setContentType(anyString());
        verify(httpServletResponse, never()).setCharacterEncoding(anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testRetrieveLastJsonNotFound() throws IOException {
        when(eventService.findFirstJSON(USER_ID, Experiment_ID)).thenThrow(NotFoundException.class);
        assertDoesNotThrow(
                () -> eventRestController.retrieveLastJson(dataObject.toString(), httpServletResponse)
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET);
        verify(eventService).findFirstJSON(USER_ID, Experiment_ID);
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setContentType(anyString());
        verify(httpServletResponse, never()).setCharacterEncoding(anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testRetrieveLastJsonIO() throws IOException {
        when(eventService.findFirstJSON(USER_ID, Experiment_ID)).thenReturn(JSON);
        when(httpServletResponse.getOutputStream()).thenThrow(IOException.class);
        assertDoesNotThrow(
                () -> eventRestController.retrieveLastJson(dataObject.toString(), httpServletResponse)
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET);
        verify(eventService).findFirstJSON(USER_ID, Experiment_ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/json");
        verify(httpServletResponse).setCharacterEncoding("UTF-8");
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testRetrieveLastJsonInvalidParticipant() throws IOException {
        when(participantService.isInvalidParticipant(USER_ID, Experiment_ID, SECRET)).thenReturn(true);
        assertDoesNotThrow(
                () -> eventRestController.retrieveLastJson(dataObject.toString(), httpServletResponse)
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET);
        verify(eventService, never()).findFirstJSON(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setContentType(anyString());
        verify(httpServletResponse, never()).setCharacterEncoding(anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

}
