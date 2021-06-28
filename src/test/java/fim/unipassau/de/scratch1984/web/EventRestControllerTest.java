package fim.unipassau.de.scratch1984.web;

import fim.unipassau.de.scratch1984.application.service.EventService;
import fim.unipassau.de.scratch1984.application.service.FileService;
import fim.unipassau.de.scratch1984.web.controller.EventRestController;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class EventRestControllerTest {

    @InjectMocks
    private EventRestController eventRestController;

    @Mock
    private EventService eventService;

    @Mock
    private FileService fileService;

    private final JSONObject blockEventObject = new JSONObject();
    private final JSONObject resourceEventObject = new JSONObject();
    private final JSONObject fileEventObject = new JSONObject();

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

    @Test
    public void testStoreBlockEvent() {
        eventRestController.storeBlockEvent(blockEventObject.toString());
        verify(eventService).saveBlockEvent(any());
    }

    @Test
    public void testStoreBlockEventJsonBlank() throws JSONException {
        blockEventObject.put("json", "");
        eventRestController.storeBlockEvent(blockEventObject.toString());
        verify(eventService).saveBlockEvent(any());
    }

    @Test
    public void testStoreBlockEventXmlBlank() throws JSONException {
        blockEventObject.put("xml", "");
        eventRestController.storeBlockEvent(blockEventObject.toString());
        verify(eventService).saveBlockEvent(any());
    }

    @Test
    public void testStoreBlockEventMetadataBlank() throws JSONException {
        blockEventObject.put("metadata", "");
        eventRestController.storeBlockEvent(blockEventObject.toString());
        verify(eventService).saveBlockEvent(any());
    }

    @Test
    public void testStoreBlockEventSpritenameBlank() throws JSONException {
        blockEventObject.put("spritename", "");
        eventRestController.storeBlockEvent(blockEventObject.toString());
        verify(eventService).saveBlockEvent(any());
    }

    @Test
    public void testStoreBlockEventJSON() throws JSONException {
        blockEventObject.put("user", "user");
        eventRestController.storeBlockEvent(blockEventObject.toString());
        verify(eventService, never()).saveBlockEvent(any());
    }

    @Test
    public void testStoreBlockEventIllegalArgument() throws JSONException {
        blockEventObject.put("event", "");
        eventRestController.storeBlockEvent(blockEventObject.toString());
        verify(eventService, never()).saveBlockEvent(any());
    }

    @Test
    public void testStoreBlockEventDateTimeParse() throws JSONException {
        blockEventObject.put("time", "0");
        eventRestController.storeBlockEvent(blockEventObject.toString());
        verify(eventService, never()).saveBlockEvent(any());
    }

    @Test
    public void testStoreResourceEvent() {
        eventRestController.storeResourceEvent(resourceEventObject.toString());
        verify(eventService).saveResourceEvent(any());
    }

    @Test
    public void testStoreResourceEventDataFormatBlank() throws JSONException {
        resourceEventObject.put("dataFormat", "");
        eventRestController.storeResourceEvent(resourceEventObject.toString());
        verify(eventService).saveResourceEvent(any());
    }

    @Test
    public void testStoreResourceEventMd5Blank() throws JSONException {
        resourceEventObject.remove("md5");
        resourceEventObject.put("md5", "");
        eventRestController.storeResourceEvent(resourceEventObject.toString());
        verify(eventService).saveResourceEvent(any());
    }

    @Test
    public void testStoreResourceEventNameBlank() throws JSONException {
        resourceEventObject.put("name", "");
        eventRestController.storeResourceEvent(resourceEventObject.toString());
        verify(eventService).saveResourceEvent(any());
    }

    @Test
    public void testStoreResourceEventJSON() throws JSONException {
        resourceEventObject.put("user", "unicorn");
        eventRestController.storeResourceEvent(resourceEventObject.toString());
        verify(eventService, never()).saveResourceEvent(any());
    }

    @Test
    public void testStoreResourceEventIllegalArgument() throws JSONException {
        resourceEventObject.put("event", "unicorn");
        eventRestController.storeResourceEvent(resourceEventObject.toString());
        verify(eventService, never()).saveResourceEvent(any());
    }

    @Test
    public void testStoreResourceEventDateTimeParse() throws JSONException {
        resourceEventObject.put("time", "0");
        eventRestController.storeResourceEvent(resourceEventObject.toString());
        verify(eventService, never()).saveResourceEvent(any());
    }

    @Test
    public void testStoreFile() {
        eventRestController.storeFileEvent(fileEventObject.toString());
        verify(fileService).saveFile(any());
    }

    @Test
    public void testStoreFileJSON() throws JSONException {
        fileEventObject.put("user", "theGordon");
        eventRestController.storeFileEvent(fileEventObject.toString());
        verify(fileService, never()).saveFile(any());
    }

    @Test
    public void testStoreFileIllegalArgument() throws JSONException {
        fileEventObject.put("file", "%");
        eventRestController.storeFileEvent(fileEventObject.toString());
        verify(fileService, never()).saveFile(any());
    }

    @Test
    public void testStoreFileDateTimeParse() throws JSONException {
        fileEventObject.put("time", "%");
        eventRestController.storeFileEvent(fileEventObject.toString());
        verify(fileService, never()).saveFile(any());
    }
}
