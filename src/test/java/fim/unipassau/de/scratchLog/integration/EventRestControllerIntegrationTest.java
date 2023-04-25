/*
 * Copyright (C) 2023 ScratchLog contributors
 *
 * This file is part of ScratchLog.
 *
 * ScratchLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * ScratchLog is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ScratchLog. If not, see <http://www.gnu.org/licenses/>.
 */
package fim.unipassau.de.scratchLog.integration;

import fim.unipassau.de.scratchLog.application.exception.NotFoundException;
import fim.unipassau.de.scratchLog.application.service.EventService;
import fim.unipassau.de.scratchLog.application.service.ExperimentService;
import fim.unipassau.de.scratchLog.application.service.FileService;
import fim.unipassau.de.scratchLog.application.service.ParticipantService;
import fim.unipassau.de.scratchLog.persistence.projection.ExperimentProjection;
import fim.unipassau.de.scratchLog.spring.configuration.SecurityTestConfig;
import fim.unipassau.de.scratchLog.web.controller.EventRestController;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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

    @MockBean
    private ParticipantService participantService;

    private static final String JSON = "json";
    private static final String SECRET = "secret";
    private static final int USER_ID = 2;
    private static final int Experiment_ID = 3;
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

    @AfterEach
    public void resetService() {
        reset(eventService, fileService, participantService);
    }

    @Test
    public void testStoreBlockEvent() throws Exception {
        mvc.perform(post("/store/block")
                        .content(blockEventObject.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(eventService).saveBlockEvent(any());
    }

    @Test
    public void testStoreBlockEventInvalidParticipant() throws Exception {
        when(participantService.isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true)).thenReturn(true);
        mvc.perform(post("/store/block")
                        .content(blockEventObject.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(eventService, never()).saveBlockEvent(any());
    }

    @Test
    public void testStoreBlockEventJsonProcessing() throws Exception {
        blockEventObject.put("experiment", "no");
        mvc.perform(post("/store/block")
                        .content(blockEventObject.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(participantService, never()).isInvalidParticipant(anyInt(), anyInt(), anyString(), anyBoolean());
        verify(eventService, never()).saveBlockEvent(any());
    }

    @Test
    public void testStoreClickEvent() throws Exception {
        mvc.perform(post("/store/click")
                        .content(clickEventObject.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(eventService).saveClickEvent(any());
    }

    @Test
    public void testStoreClickJsonProcessing() throws Exception {
        clickEventObject.put("time", "-1");
        mvc.perform(post("/store/click")
                        .content(clickEventObject.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(participantService, never()).isInvalidParticipant(anyInt(), anyInt(), anyString(), anyBoolean());
        verify(eventService, never()).saveClickEvent(any());
    }

    @Test
    public void testStoreDebuggerEvent() throws Exception {
        mvc.perform(post("/store/debugger")
                        .content(debuggerEventObject.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(eventService).saveDebuggerEvent(any());
    }

    @Test
    public void testStoreDebuggerEventJsonProcessing() throws Exception {
        debuggerEventObject.put("time", ".");
        mvc.perform(post("/store/debugger")
                        .content(debuggerEventObject.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(participantService, never()).isInvalidParticipant(anyInt(), anyInt(), anyString(), anyBoolean());
        verify(eventService, never()).saveDebuggerEvent(any());
    }

    @Test
    public void testStoreQuestionEvent() throws Exception {
        mvc.perform(post("/store/question")
                        .content(questionEventObject.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(eventService).saveQuestionEvent(any());
    }

    @Test
    public void testStoreQuestionEventInvalidParticipant() throws Exception {
        when(participantService.isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true)).thenReturn(true);
        mvc.perform(post("/store/question")
                        .content(questionEventObject.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(eventService, never()).saveQuestionEvent(any());
    }

    @Test
    public void testStoreResourceEvent() throws Exception {
        mvc.perform(post("/store/resource")
                        .content(resourceEventObject.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(eventService).saveResourceEvent(any());
    }

    @Test
    public void testStoreResourceEventJsonProcessing() throws Exception {
        resourceEventObject.put("time", "user");
        mvc.perform(post("/store/resource")
                        .content(resourceEventObject.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(participantService, never()).isInvalidParticipant(anyInt(), anyInt(), anyString(), anyBoolean());
        verify(eventService, never()).saveResourceEvent(any());
    }

    @Test
    public void testStoreFileEvent() throws Exception {
        mvc.perform(post("/store/file")
                        .content(fileEventObject.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(fileService).saveFile(any());
    }

    @Test
    public void testStoreFileEventJsonProcessing() throws Exception {
        fileEventObject.put("file", "%");
        mvc.perform(post("/store/file")
                        .content(fileEventObject.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(participantService, never()).isInvalidParticipant(anyInt(), anyInt(), anyString(), anyBoolean());
        verify(fileService, never()).saveFile(any());
    }

    @Test
    public void testStoreZipFile() throws Exception {
        mvc.perform(post("/store/zip")
                        .content(sb3ZipObject.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(fileService).saveSb3Zip(any());
    }

    @Test
    public void testStoreZipFileInvalidParticipant() throws Exception {
        when(participantService.isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true)).thenReturn(true);
        mvc.perform(post("/store/zip")
                        .content(sb3ZipObject.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(fileService, never()).saveSb3Zip(any());
    }

    @Test
    public void testStoreZipFileJsonProcessing() throws Exception {
        sb3ZipObject.put("user", "unicorn");
        mvc.perform(post("/store/zip")
                        .content(sb3ZipObject.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(participantService, never()).isInvalidParticipant(anyInt(), anyInt(), anyString(), anyBoolean());
        verify(fileService, never()).saveSb3Zip(any());
    }

    @Test
    public void testRetrieveSb3File() throws Exception {
        when(experimentService.getSb3File(Experiment_ID)).thenReturn(experimentProjection);
        mvc.perform(post("/store/sb3")
                        .content(dataObject.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(experimentService).getSb3File(Experiment_ID);
    }

    @Test
    public void testRetrieveSb3FileProjectionNull() throws Exception {
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
        mvc.perform(post("/store/sb3")
                        .content(dataObject.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(experimentService).getSb3File(Experiment_ID);
    }

    @Test
    public void testRetrieveSb3FileNotFound() throws Exception {
        when(experimentService.getSb3File(Experiment_ID)).thenThrow(NotFoundException.class);
        mvc.perform(post("/store/sb3")
                        .content(dataObject.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(experimentService).getSb3File(Experiment_ID);
    }

    @Test
    public void testRetrieveLastJson() throws Exception {
        when(eventService.findFirstJSON(USER_ID, Experiment_ID)).thenReturn(JSON);
        mvc.perform(post("/store/json")
                        .content(dataObject.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(eventService).findFirstJSON(USER_ID, Experiment_ID);
    }

    @Test
    public void testRetrieveLastJsonJsonNull() throws Exception {
        mvc.perform(post("/store/json")
                        .content(dataObject.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(eventService).findFirstJSON(USER_ID, Experiment_ID);
    }

    @Test
    public void testRetrieveLastJsonNotFound() throws Exception {
        when(eventService.findFirstJSON(USER_ID, Experiment_ID)).thenThrow(NotFoundException.class);
        mvc.perform(post("/store/json")
                        .content(dataObject.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(eventService).findFirstJSON(USER_ID, Experiment_ID);
    }

    @Test
    public void testRetrieveLastJsonInvalidParticipant() throws Exception {
        when(participantService.isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true)).thenReturn(true);
        mvc.perform(post("/store/json")
                        .content(dataObject.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(eventService, never()).findFirstJSON(anyInt(), anyInt());
    }
}
