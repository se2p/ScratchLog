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

package de.uni_passau.fim.se2.scratchlog.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.uni_passau.fim.se2.scratchlog.application.exception.NotFoundException;
import de.uni_passau.fim.se2.scratchlog.application.service.CodeService;
import de.uni_passau.fim.se2.scratchlog.application.service.EventService;
import de.uni_passau.fim.se2.scratchlog.application.service.ExperimentService;
import de.uni_passau.fim.se2.scratchlog.application.service.FileService;
import de.uni_passau.fim.se2.scratchlog.application.service.ParticipantService;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.ExperimentProjection;
import de.uni_passau.fim.se2.scratchlog.util.enums.BlockEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.BlockEventType;
import de.uni_passau.fim.se2.scratchlog.util.enums.ClickEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.ClickEventType;
import de.uni_passau.fim.se2.scratchlog.util.enums.DebuggerEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.DebuggerEventType;
import de.uni_passau.fim.se2.scratchlog.util.enums.LibraryResource;
import de.uni_passau.fim.se2.scratchlog.util.enums.QuestionEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.QuestionEventType;
import de.uni_passau.fim.se2.scratchlog.util.enums.ResourceEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.ResourceEventType;
import de.uni_passau.fim.se2.scratchlog.web.controller.EventRestController;
import de.uni_passau.fim.se2.scratchlog.web.dto.BlockEventDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.ClickEventDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.DebuggerEventDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.FileDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.QuestionEventDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.ResourceEventDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.Sb3ZipDTO;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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
    private CodeService codeService;

    @Mock
    private FileService fileService;

    @Mock
    private ExperimentService experimentService;

    @Mock
    private ParticipantService participantService;

    @Mock
    private HttpServletResponse httpServletResponse;

    private static final String JSON = "json.txt";
    private static final String SECRET = "secret";
    private static final int USER_ID = 3;
    private static final int Experiment_ID = 39;
    private BlockEventDTO blockEvent;
    private ClickEventDTO clickEvent;
    private DebuggerEventDTO debuggerEvent;
    private QuestionEventDTO questionEvent;
    private ResourceEventDTO resourceEvent;
    private FileDTO fileEvent;
    private Sb3ZipDTO sb3Zip;
    private EventRestController.UserDataRequestDTO dataRequest;
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
    public void setup() throws JSONException, JsonProcessingException {
        blockEvent = new BlockEventDTO(USER_ID, Experiment_ID, SECRET, BlockEventType.DRAG, BlockEventSpecific.ENDDRAG,
            "Figur1", "meta", "xml", "{}", LocalDateTime.now());
        clickEvent = new ClickEventDTO(USER_ID, Experiment_ID, SECRET, ClickEventType.ICON,
            ClickEventSpecific.STOPALL, "meta", LocalDateTime.now());
        debuggerEvent = new DebuggerEventDTO(USER_ID, Experiment_ID, SECRET, DebuggerEventType.SPRITE,
            DebuggerEventSpecific.SELECT_SPRITE, "id", "opcode", 1, 5, LocalDateTime.now());
        questionEvent = new QuestionEventDTO(USER_ID, Experiment_ID, SECRET, QuestionEventType.QUESTION,
            QuestionEventSpecific.SELECT, 1, "block-execution", new String[]{"Cat", "Costume"}, "execution",
            "negative", "id", "opcode", LocalDateTime.MIN);
        resourceEvent = new ResourceEventDTO(USER_ID, Experiment_ID, SECRET, ResourceEventType.DELETE,
            ResourceEventSpecific.DELETE_SOUND, "Miau", "md5", "wav", LibraryResource.UNKNOWN, LocalDateTime.MAX);
        fileEvent = new FileDTO(USER_ID, Experiment_ID, SECRET, "Miau.wav", "audio/x-wav", new byte[]{},
            LocalDateTime.now());
        sb3Zip = new Sb3ZipDTO(USER_ID, Experiment_ID, SECRET, "sb3zip.sb3", new byte[]{}, LocalDateTime.now());
        dataRequest = new EventRestController.UserDataRequestDTO(USER_ID, Experiment_ID, SECRET);
    }

    @Test
    public void testStoreBlockEvent() {
        assertDoesNotThrow(
                () -> eventRestController.storeBlockEvent(blockEvent)
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(eventService).saveBlockEvent(any());
    }

    @Test
    public void testStoreBlockEventInvalidParticipant() {
        when(participantService.isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true)).thenReturn(true);
        assertThrows(ResponseStatusException.class,
            () -> eventRestController.storeBlockEvent(blockEvent)
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(eventService, never()).saveBlockEvent(any());
    }

    @Test
    public void testStoreClickEvent() throws JSONException {
        assertDoesNotThrow(
                () -> eventRestController.storeClickEvent(clickEvent)
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(eventService).saveClickEvent(any());
    }

    @Test
    public void testStoreClickEventInvalidParticipant() {
        when(participantService.isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true)).thenReturn(true);
        assertDoesNotThrow(
                () -> eventRestController.storeClickEvent(clickEvent)
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(eventService, never()).saveBlockEvent(any());
    }

    @Test
    public void testStoreDebuggerEvent() {
        assertDoesNotThrow(
                () -> eventRestController.storeDebuggerEvent(debuggerEvent)
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(eventService).saveDebuggerEvent(any());
    }

    @Test
    public void testStoreDebuggerEventInvalidParticipant() {
        when(participantService.isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true)).thenReturn(true);
        assertDoesNotThrow(
                () -> eventRestController.storeDebuggerEvent(debuggerEvent)
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(eventService, never()).saveBlockEvent(any());
    }

    @Test
    public void testStoreQuestionEvent() {
        assertDoesNotThrow(
                () -> eventRestController.storeQuestionEvent(questionEvent)
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(eventService).saveQuestionEvent(any());
    }

    @Test
    public void testStoreQuestionEventInvalidParticipant() {
        when(participantService.isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true)).thenReturn(true);
        assertDoesNotThrow(
                () -> eventRestController.storeQuestionEvent(questionEvent)
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(eventService, never()).saveBlockEvent(any());
    }

    @Test
    public void testStoreResourceEvent() {
        assertDoesNotThrow(
                () -> eventRestController.storeResourceEvent(resourceEvent)
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(eventService).saveResourceEvent(any());
    }

    @Test
    public void testStoreResourceEventInvalidParticipant() {
        when(participantService.isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true)).thenReturn(true);
        assertDoesNotThrow(
                () -> eventRestController.storeResourceEvent(resourceEvent)
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(eventService, never()).saveBlockEvent(any());
    }

    @Test
    public void testStoreFileEvent() {
        assertDoesNotThrow(
                () -> eventRestController.storeFileEvent(fileEvent)
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(fileService).saveFile(any());
    }

    @Test
    public void testStoreFileEventInvalidParticipant() {
        when(participantService.isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true)).thenReturn(true);
        assertDoesNotThrow(
                () -> eventRestController.storeFileEvent(fileEvent)
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(eventService, never()).saveBlockEvent(any());
    }

    @Test
    public void testStoreZipFile() {
        assertDoesNotThrow(
                () -> eventRestController.storeZipFile(sb3Zip)
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(fileService).saveSb3Zip(any());
    }

    @Test
    public void testStoreZipFileInvalidParticipant() {
        when(participantService.isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true)).thenReturn(true);
        assertDoesNotThrow(
                () -> eventRestController.storeZipFile(sb3Zip)
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(eventService, never()).saveBlockEvent(any());
    }

    @Test
    public void testRetrieveSb3File() throws IOException {
        when(experimentService.getSb3File(Experiment_ID, false)).thenReturn(experimentProjection);
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
                () -> eventRestController.retrieveSb3File(dataRequest, httpServletResponse)
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(experimentService).getSb3File(Experiment_ID, false);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
        verify(httpServletResponse, never()).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testRetrieveSb3FileIO() throws IOException {
        when(experimentService.getSb3File(Experiment_ID, false)).thenReturn(experimentProjection);
        when(httpServletResponse.getOutputStream()).thenThrow(IOException.class);
        assertDoesNotThrow(
                () -> eventRestController.retrieveSb3File(dataRequest, httpServletResponse)
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(experimentService).getSb3File(Experiment_ID, false);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testRetrieveSb3FileProjectNull() throws IOException {
        when(experimentService.getSb3File(Experiment_ID, false)).thenReturn(new ExperimentProjection() {
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
                () -> eventRestController.retrieveSb3File(dataRequest, httpServletResponse)
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(experimentService).getSb3File(Experiment_ID, false);
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setContentType(anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testRetrieveSb3FileNotFound() throws IOException {
        when(experimentService.getSb3File(Experiment_ID, false)).thenThrow(NotFoundException.class);
        assertDoesNotThrow(
                () -> eventRestController.retrieveSb3File(dataRequest, httpServletResponse)
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(experimentService).getSb3File(Experiment_ID, false);
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setContentType(anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testRetrieveSb3FileInvalidParticipant() throws IOException {
        when(participantService.isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true)).thenReturn(true);
        assertThrows(
                ResponseStatusException.class,
                () -> eventRestController.retrieveSb3File(dataRequest, httpServletResponse),
            "400 BAD_REQUEST"
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(experimentService, never()).getSb3File(anyInt(), anyBoolean());
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setContentType(anyString());
    }

    @Test
    public void testRetrieveLastJson() throws IOException {
        when(codeService.findFirstJSON(USER_ID, Experiment_ID)).thenReturn(JSON);
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
                () -> eventRestController.retrieveLastJson(dataRequest, httpServletResponse)
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(codeService).findFirstJSON(USER_ID, Experiment_ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/json");
        verify(httpServletResponse).setCharacterEncoding("UTF-8");
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testRetrieveLastJsonJsonNull() throws IOException {
        assertDoesNotThrow(
                () -> eventRestController.retrieveLastJson(dataRequest, httpServletResponse)
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(codeService).findFirstJSON(USER_ID, Experiment_ID);
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setContentType(anyString());
        verify(httpServletResponse, never()).setCharacterEncoding(anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testRetrieveLastJsonNotFound() throws IOException {
        when(codeService.findFirstJSON(USER_ID, Experiment_ID)).thenThrow(NotFoundException.class);
        assertDoesNotThrow(
                () -> eventRestController.retrieveLastJson(dataRequest, httpServletResponse)
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(codeService).findFirstJSON(USER_ID, Experiment_ID);
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setContentType(anyString());
        verify(httpServletResponse, never()).setCharacterEncoding(anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testRetrieveLastJsonIO() throws IOException {
        when(codeService.findFirstJSON(USER_ID, Experiment_ID)).thenReturn(JSON);
        when(httpServletResponse.getOutputStream()).thenThrow(IOException.class);
        assertDoesNotThrow(
                () -> eventRestController.retrieveLastJson(dataRequest, httpServletResponse)
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(codeService).findFirstJSON(USER_ID, Experiment_ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/json");
        verify(httpServletResponse).setCharacterEncoding("UTF-8");
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testRetrieveLastJsonInvalidParticipant() throws IOException {
        when(participantService.isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true)).thenReturn(true);
        assertThrows(
                ResponseStatusException.class,
                () -> eventRestController.retrieveLastJson(dataRequest, httpServletResponse),
                "400 BAD_REQUEST"
        );
        verify(participantService).isInvalidParticipant(USER_ID, Experiment_ID, SECRET, true);
        verify(codeService, never()).findFirstJSON(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setContentType(anyString());
        verify(httpServletResponse, never()).setCharacterEncoding(anyString());
    }

}
