package fim.unipassau.de.scratch1984.web;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.EventService;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.application.service.FileService;
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
    private HttpServletResponse httpServletResponse;

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
        public byte[] getProject() {
            return new byte[]{1, 2, 3};
        }
    };

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

    @Test
    public void testStoreBlockEvent() {
        assertDoesNotThrow(
                () -> eventRestController.storeBlockEvent(blockEventObject.toString())
        );
        verify(eventService).saveBlockEvent(any());
    }

    @Test
    public void testStoreBlockEventJsonBlank() throws JSONException {
        blockEventObject.put("json", "");
        assertDoesNotThrow(
                () -> eventRestController.storeBlockEvent(blockEventObject.toString())
        );
        verify(eventService).saveBlockEvent(any());
    }

    @Test
    public void testStoreBlockEventXmlBlank() throws JSONException {
        blockEventObject.put("xml", "");
        assertDoesNotThrow(
                () -> eventRestController.storeBlockEvent(blockEventObject.toString())
        );
        verify(eventService).saveBlockEvent(any());
    }

    @Test
    public void testStoreBlockEventMetadataBlank() throws JSONException {
        blockEventObject.put("metadata", "");
        assertDoesNotThrow(
                () -> eventRestController.storeBlockEvent(blockEventObject.toString())
        );
        verify(eventService).saveBlockEvent(any());
    }

    @Test
    public void testStoreBlockEventSpritenameBlank() throws JSONException {
        blockEventObject.put("spritename", "");
        assertDoesNotThrow(
                () -> eventRestController.storeBlockEvent(blockEventObject.toString())
        );
        verify(eventService).saveBlockEvent(any());
    }

    @Test
    public void testStoreBlockEventJSON() throws JSONException {
        blockEventObject.put("user", "user");
        assertDoesNotThrow(
                () -> eventRestController.storeBlockEvent(blockEventObject.toString())
        );
        verify(eventService, never()).saveBlockEvent(any());
    }

    @Test
    public void testStoreBlockEventIllegalArgument() throws JSONException {
        blockEventObject.put("event", "");
        assertDoesNotThrow(
                () -> eventRestController.storeBlockEvent(blockEventObject.toString())
        );
        verify(eventService, never()).saveBlockEvent(any());
    }

    @Test
    public void testStoreBlockEventDateTimeParse() throws JSONException {
        blockEventObject.put("time", "0");
        assertDoesNotThrow(
                () -> eventRestController.storeBlockEvent(blockEventObject.toString())
        );
        verify(eventService, never()).saveBlockEvent(any());
    }

    @Test
    public void testStoreClickEvent() throws JSONException {
        assertDoesNotThrow(
                () -> eventRestController.storeClickEvent(clickEventObject.toString())
        );
        verify(eventService).saveClickEvent(any());
    }

    @Test
    public void testStoreClickEventMetadataBlank() throws JSONException {
        clickEventObject.put("metadata", "");
        assertDoesNotThrow(
                () -> eventRestController.storeClickEvent(clickEventObject.toString())
        );
        verify(eventService).saveClickEvent(any());
    }

    @Test
    public void testStoreClickEventJSON() throws JSONException {
        clickEventObject.put("user", "unicorn");
        assertDoesNotThrow(
                () -> eventRestController.storeClickEvent(clickEventObject.toString())
        );
        verify(eventService, never()).saveClickEvent(any());
    }

    @Test
    public void testStoreClickEventIllegalArgument() throws JSONException {
        clickEventObject.put("event", "");
        assertDoesNotThrow(
                () -> eventRestController.storeClickEvent(clickEventObject.toString())
        );
        verify(eventService, never()).saveClickEvent(any());
    }

    @Test
    public void testStoreClickEventDateTimeParse() throws JSONException {
        clickEventObject.put("time", "0");
        assertDoesNotThrow(
                () -> eventRestController.storeClickEvent(clickEventObject.toString())
        );
        verify(eventService, never()).saveClickEvent(any());
    }

    @Test
    public void testStoreDebuggerEvent() {
        assertDoesNotThrow(
                () -> eventRestController.storeDebuggerEvent(debuggerEventObject.toString())
        );
        verify(eventService).saveDebuggerEvent(any());
    }

    @Test
    public void testStoreDebuggerEventIdBlank() {
        debuggerEventObject.put("id", "");
        assertDoesNotThrow(
                () -> eventRestController.storeDebuggerEvent(debuggerEventObject.toString())
        );
        verify(eventService).saveDebuggerEvent(any());
    }

    @Test
    public void testStoreDebuggerEventNameBlank() {
        debuggerEventObject.put("name", "");
        assertDoesNotThrow(
                () -> eventRestController.storeDebuggerEvent(debuggerEventObject.toString())
        );
        verify(eventService).saveDebuggerEvent(any());
    }

    @Test
    public void testStoreDebuggerEventOriginalBlank() {
        debuggerEventObject.put("original", "");
        assertDoesNotThrow(
                () -> eventRestController.storeDebuggerEvent(debuggerEventObject.toString())
        );
        verify(eventService).saveDebuggerEvent(any());
    }

    @Test
    public void testStoreDebuggerEventExecutionBlank() {
        debuggerEventObject.put("execution", "");
        assertDoesNotThrow(
                () -> eventRestController.storeDebuggerEvent(debuggerEventObject.toString())
        );
        verify(eventService).saveDebuggerEvent(any());
    }

    @Test
    public void testStoreDebuggerEventJSON() {
        debuggerEventObject.put("original", "one");
        assertDoesNotThrow(
                () -> eventRestController.storeDebuggerEvent(debuggerEventObject.toString())
        );
        verify(eventService, never()).saveDebuggerEvent(any());
    }

    @Test
    public void testStoreDebuggerEventIllegalArgument() {
        debuggerEventObject.put("type", "one");
        assertDoesNotThrow(
                () -> eventRestController.storeDebuggerEvent(debuggerEventObject.toString())
        );
        verify(eventService, never()).saveDebuggerEvent(any());
    }

    @Test
    public void testStoreDebuggerEventDateTimeParse() {
        debuggerEventObject.put("time", "time");
        assertDoesNotThrow(
                () -> eventRestController.storeDebuggerEvent(debuggerEventObject.toString())
        );
        verify(eventService, never()).saveDebuggerEvent(any());
    }

    @Test
    public void testStoreQuestionEvent() {
        assertDoesNotThrow(
                () -> eventRestController.storeQuestionEvent(questionEventObject.toString())
        );
        verify(eventService).saveQuestionEvent(any());
    }

    @Test
    public void testStoreQuestionEventFeedbackBlank() {
        questionEventObject.put("feedback", "");
        assertDoesNotThrow(
                () -> eventRestController.storeQuestionEvent(questionEventObject.toString())
        );
        verify(eventService).saveQuestionEvent(any());
    }

    @Test
    public void testStoreQuestionEventTypeBlank() {
        questionEventObject.put("q_type", "");
        assertDoesNotThrow(
                () -> eventRestController.storeQuestionEvent(questionEventObject.toString())
        );
        verify(eventService).saveQuestionEvent(any());
    }

    @Test
    public void testStoreQuestionEventValuesBlank() {
        questionEventObject.put("values", "");
        assertDoesNotThrow(
                () -> eventRestController.storeQuestionEvent(questionEventObject.toString())
        );
        verify(eventService).saveQuestionEvent(any());
    }

    @Test
    public void testStoreQuestionEventCategoryBlank() {
        questionEventObject.put("category", "");
        assertDoesNotThrow(
                () -> eventRestController.storeQuestionEvent(questionEventObject.toString())
        );
        verify(eventService).saveQuestionEvent(any());
    }

    @Test
    public void testStoreQuestionEventFormBlank() {
        questionEventObject.put("form", "");
        assertDoesNotThrow(
                () -> eventRestController.storeQuestionEvent(questionEventObject.toString())
        );
        verify(eventService).saveQuestionEvent(any());
    }

    @Test
    public void testStoreQuestionEventBlockIdBlank() {
        questionEventObject.put("id", "");
        assertDoesNotThrow(
                () -> eventRestController.storeQuestionEvent(questionEventObject.toString())
        );
        verify(eventService).saveQuestionEvent(any());
    }

    @Test
    public void testStoreQuestionEventOpcodeBlank() {
        questionEventObject.put("opcode", "");
        assertDoesNotThrow(
                () -> eventRestController.storeQuestionEvent(questionEventObject.toString())
        );
        verify(eventService).saveQuestionEvent(any());
    }

    @Test
    public void testStoreQuestionEventJSON() {
        questionEventObject.put("feedback", "no");
        assertDoesNotThrow(
                () -> eventRestController.storeQuestionEvent(questionEventObject.toString())
        );
        verify(eventService, never()).saveQuestionEvent(any());
    }

    @Test
    public void testStoreQuestionEventIllegalArgument() {
        questionEventObject.put("event", "SPRITE");
        assertDoesNotThrow(
                () -> eventRestController.storeQuestionEvent(questionEventObject.toString())
        );
        verify(eventService, never()).saveQuestionEvent(any());
    }

    @Test
    public void testStoreQuestionEventDateParse() {
        questionEventObject.put("time", "0");
        assertDoesNotThrow(
                () -> eventRestController.storeQuestionEvent(questionEventObject.toString())
        );
        verify(eventService, never()).saveQuestionEvent(any());
    }

    @Test
    public void testStoreResourceEvent() {
        assertDoesNotThrow(
                () -> eventRestController.storeResourceEvent(resourceEventObject.toString())
        );
        verify(eventService).saveResourceEvent(any());
    }

    @Test
    public void testStoreResourceEventDataFormatBlank() throws JSONException {
        resourceEventObject.put("dataFormat", "");
        assertDoesNotThrow(
                () -> eventRestController.storeResourceEvent(resourceEventObject.toString())
        );
        verify(eventService).saveResourceEvent(any());
    }

    @Test
    public void testStoreResourceEventMd5Blank() throws JSONException {
        resourceEventObject.put("md5", "");
        assertDoesNotThrow(
                () -> eventRestController.storeResourceEvent(resourceEventObject.toString())
        );
        verify(eventService).saveResourceEvent(any());
    }

    @Test
    public void testStoreResourceEventNameBlank() throws JSONException {
        resourceEventObject.put("name", "");
        assertDoesNotThrow(
                () -> eventRestController.storeResourceEvent(resourceEventObject.toString())
        );
        verify(eventService).saveResourceEvent(any());
    }

    @Test
    public void testStoreResourceEventJSON() throws JSONException {
        resourceEventObject.put("user", "unicorn");
        assertDoesNotThrow(
                () -> eventRestController.storeResourceEvent(resourceEventObject.toString())
        );
        verify(eventService, never()).saveResourceEvent(any());
    }

    @Test
    public void testStoreResourceEventIllegalArgument() throws JSONException {
        resourceEventObject.put("event", "");
        assertDoesNotThrow(
                () -> eventRestController.storeResourceEvent(resourceEventObject.toString())
        );
        verify(eventService, never()).saveResourceEvent(any());
    }

    @Test
    public void testStoreResourceEventDateTimeParse() throws JSONException {
        resourceEventObject.put("time", "0");
        assertDoesNotThrow(
                () -> eventRestController.storeResourceEvent(resourceEventObject.toString())
        );
        verify(eventService, never()).saveResourceEvent(any());
    }

    @Test
    public void testStoreFile() {
        assertDoesNotThrow(
                () -> eventRestController.storeFileEvent(fileEventObject.toString())
        );
        verify(fileService).saveFile(any());
    }

    @Test
    public void testStoreFileJSON() throws JSONException {
        fileEventObject.put("user", "theGordon");
        assertDoesNotThrow(
                () -> eventRestController.storeFileEvent(fileEventObject.toString())
        );
        verify(fileService, never()).saveFile(any());
    }

    @Test
    public void testStoreFileIllegalArgument() throws JSONException {
        fileEventObject.put("file", "%");
        assertDoesNotThrow(
                () -> eventRestController.storeFileEvent(fileEventObject.toString())
        );
        verify(fileService, never()).saveFile(any());
    }

    @Test
    public void testStoreFileDateTimeParse() throws JSONException {
        fileEventObject.put("time", "%");
        assertDoesNotThrow(
                () -> eventRestController.storeFileEvent(fileEventObject.toString())
        );
        verify(fileService, never()).saveFile(any());
    }

    @Test
    public void testStoreZipFile() {
        assertDoesNotThrow(
                () -> eventRestController.storeZipFile(sb3ZipObject.toString())
        );
        verify(fileService).saveSb3Zip(any());
    }

    @Test
    public void testStoreZipFileJSON() {
        sb3ZipObject.put("user", "theGordon");
        assertDoesNotThrow(
                () -> eventRestController.storeZipFile(sb3ZipObject.toString())
        );
        verify(fileService, never()).saveSb3Zip(any());
    }

    @Test
    public void testStoreZipFileIllegalArgument() {
        sb3ZipObject.put("zip", "%");
        assertDoesNotThrow(
                () -> eventRestController.storeZipFile(sb3ZipObject.toString())
        );
        verify(fileService, never()).saveSb3Zip(any());
    }

    @Test
    public void testStoreZipFileDateTimeParse() {
        sb3ZipObject.put("time", "%");
        assertDoesNotThrow(
                () -> eventRestController.storeZipFile(sb3ZipObject.toString())
        );
        verify(fileService, never()).saveSb3Zip(any());
    }

    @Test
    public void testRetrieveSb3File() throws IOException {
        when(experimentService.getSb3File(ID)).thenReturn(experimentProjection);
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
                () -> eventRestController.retrieveSb3File(ID_STRING, httpServletResponse)
        );
        verify(experimentService).getSb3File(ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
        verify(httpServletResponse, never()).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testRetrieveSb3FileIO() throws IOException {
        when(experimentService.getSb3File(ID)).thenReturn(experimentProjection);
        when(httpServletResponse.getOutputStream()).thenThrow(IOException.class);
        assertDoesNotThrow(
                () -> eventRestController.retrieveSb3File(ID_STRING, httpServletResponse)
        );
        verify(experimentService).getSb3File(ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testRetrieveSb3FileProjectNull() throws IOException {
        when(experimentService.getSb3File(ID)).thenReturn(new ExperimentProjection() {
            @Override
            public Integer getId() {
                return null;
            }

            @Override
            public byte[] getProject() {
                return null;
            }
        });
        assertDoesNotThrow(
                () -> eventRestController.retrieveSb3File(ID_STRING, httpServletResponse)
        );
        verify(experimentService).getSb3File(ID);
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setContentType(anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testRetrieveSb3FileNotFound() throws IOException {
        when(experimentService.getSb3File(ID)).thenThrow(NotFoundException.class);
        assertDoesNotThrow(
                () -> eventRestController.retrieveSb3File(ID_STRING, httpServletResponse)
        );
        verify(experimentService).getSb3File(ID);
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setContentType(anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testRetrieveSb3FileNotFoundInvalidId() throws IOException {
        assertDoesNotThrow(
                () -> eventRestController.retrieveSb3File("0", httpServletResponse)
        );
        verify(experimentService, never()).getSb3File(anyInt());
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setContentType(anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void testRetrieveSb3FileNotFoundIdNull() throws IOException {
        assertDoesNotThrow(
                () -> eventRestController.retrieveSb3File(null, httpServletResponse)
        );
        verify(experimentService, never()).getSb3File(anyInt());
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setContentType(anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void testRetrieveLastJson() throws IOException {
        when(eventService.findFirstJSON(ID, ID)).thenReturn(JSON);
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
                () -> eventRestController.retrieveLastJson(ID_STRING, ID_STRING, httpServletResponse)
        );
        verify(eventService).findFirstJSON(ID, ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/json");
        verify(httpServletResponse).setCharacterEncoding("UTF-8");
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testRetrieveLastJsonJsonNull() throws IOException {
        assertDoesNotThrow(
                () -> eventRestController.retrieveLastJson(ID_STRING, ID_STRING, httpServletResponse)
        );
        verify(eventService).findFirstJSON(ID, ID);
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setContentType(anyString());
        verify(httpServletResponse, never()).setCharacterEncoding(anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testRetrieveLastJsonNotFound() throws IOException {
        when(eventService.findFirstJSON(ID, ID)).thenThrow(NotFoundException.class);
        assertDoesNotThrow(
                () -> eventRestController.retrieveLastJson(ID_STRING, ID_STRING, httpServletResponse)
        );
        verify(eventService).findFirstJSON(ID, ID);
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setContentType(anyString());
        verify(httpServletResponse, never()).setCharacterEncoding(anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testRetrieveLastJsonIO() throws IOException {
        when(eventService.findFirstJSON(ID, ID)).thenReturn(JSON);
        when(httpServletResponse.getOutputStream()).thenThrow(IOException.class);
        assertDoesNotThrow(
                () -> eventRestController.retrieveLastJson(ID_STRING, ID_STRING, httpServletResponse)
        );
        verify(eventService).findFirstJSON(ID, ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/json");
        verify(httpServletResponse).setCharacterEncoding("UTF-8");
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testRetrieveLastJsonInvalidExperimentId() throws IOException {
        assertDoesNotThrow(
                () -> eventRestController.retrieveLastJson(ID_STRING, JSON, httpServletResponse)
        );
        verify(eventService, never()).findFirstJSON(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setContentType(anyString());
        verify(httpServletResponse, never()).setCharacterEncoding(anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void testRetrieveLastJsonInvalidUserId() throws IOException {
        assertDoesNotThrow(
                () -> eventRestController.retrieveLastJson("0", ID_STRING, httpServletResponse)
        );
        verify(eventService, never()).findFirstJSON(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setContentType(anyString());
        verify(httpServletResponse, never()).setCharacterEncoding(anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void testRetrieveLastJsonExperimentNull() throws IOException {
        assertDoesNotThrow(
                () -> eventRestController.retrieveLastJson(ID_STRING, null, httpServletResponse)
        );
        verify(eventService, never()).findFirstJSON(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setContentType(anyString());
        verify(httpServletResponse, never()).setCharacterEncoding(anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void testRetrieveLastJsonUserNull() throws IOException {
        assertDoesNotThrow(
                () -> eventRestController.retrieveLastJson(null, ID_STRING, httpServletResponse)
        );
        verify(eventService, never()).findFirstJSON(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setContentType(anyString());
        verify(httpServletResponse, never()).setCharacterEncoding(anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
}
