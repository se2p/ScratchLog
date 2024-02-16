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

package fim.unipassau.de.scratchLog.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fim.unipassau.de.scratchLog.application.exception.NotFoundException;
import fim.unipassau.de.scratchLog.application.service.EventService;
import fim.unipassau.de.scratchLog.application.service.ExperimentService;
import fim.unipassau.de.scratchLog.application.service.FileService;
import fim.unipassau.de.scratchLog.application.service.ParticipantService;
import fim.unipassau.de.scratchLog.persistence.projection.ExperimentProjection;
import fim.unipassau.de.scratchLog.web.dto.BlockEventDTO;
import fim.unipassau.de.scratchLog.web.dto.ClickEventDTO;
import fim.unipassau.de.scratchLog.web.dto.DebuggerEventDTO;
import fim.unipassau.de.scratchLog.web.dto.EventDTO;
import fim.unipassau.de.scratchLog.web.dto.FileDTO;
import fim.unipassau.de.scratchLog.web.dto.QuestionEventDTO;
import fim.unipassau.de.scratchLog.web.dto.ResourceEventDTO;
import fim.unipassau.de.scratchLog.web.dto.Sb3ZipDTO;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * The REST controller receiving all the logging requests sent by the Scratch GUI and VM.
 */
@RestController
@RequestMapping(value = "/store")
public class EventRestController {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EventRestController.class);

    /**
     * The event service to use to save the received event data.
     */
    private final EventService eventService;

    /**
     * The file service to use to save the received file data.
     */
    private final FileService fileService;

    /**
     * The experiment service to use for retrieving sb3 files.
     */
    private final ExperimentService experimentService;

    /**
     * The participant service to use for verifying participants.
     */
    private final ParticipantService participantService;

    /**
     * Constructs an event rest controller with the given dependencies.
     *
     * @param eventService The event service to use.
     * @param fileService The file service to use.
     * @param experimentService The experiment service to use.
     * @param participantService The participant service to use.
     */
    @Autowired
    public EventRestController(final EventService eventService, final FileService fileService,
                               final ExperimentService experimentService, final ParticipantService participantService) {
        this.eventService = eventService;
        this.fileService = fileService;
        this.experimentService = experimentService;
        this.participantService = participantService;
    }

    /**
     * Saves the block event data passed in the request body.
     *
     * @param data The string containing the block event data.
     */
    @PostMapping("/block")
    public void storeBlockEvent(@RequestBody final String data) {
        BlockEventDTO blockEventDTO = createBlockEventDTO(data);

        if (blockEventDTO == null || isInvalidRequest(data, blockEventDTO)) {
            return;
        }

        eventService.saveBlockEvent(blockEventDTO);
    }

    /**
     * Saves the click event data passed in the request body.
     *
     * @param data The string containing the click event data.
     */
    @PostMapping("/click")
    public void storeClickEvent(@RequestBody final String data) {
        ClickEventDTO clickEventDTO = createClickEventDTO(data);

        if (clickEventDTO == null || isInvalidRequest(data, clickEventDTO)) {
            return;
        }

        eventService.saveClickEvent(clickEventDTO);
    }

    /**
     * Saves the debugger event data passed in the request body.
     *
     * @param data The string containing the debugger event data.
     */
    @PostMapping("/debugger")
    public void storeDebuggerEvent(@RequestBody final String data) {
        DebuggerEventDTO debuggerEventDTO = createDebuggerEventDTO(data);

        if (debuggerEventDTO == null || isInvalidRequest(data, debuggerEventDTO)) {
            return;
        }

        eventService.saveDebuggerEvent(debuggerEventDTO);
    }

    /**
     * Saves the question event data passed in the request body.
     *
     * @param data The string containing the question event data.
     */
    @PostMapping("/question")
    public void storeQuestionEvent(@RequestBody final String data) {
        QuestionEventDTO questionEventDTO = createQuestionEventDTO(data);

        if (questionEventDTO == null || isInvalidRequest(data, questionEventDTO)) {
            return;
        }

        eventService.saveQuestionEvent(questionEventDTO);
    }

    /**
     * Saves the resource event data passed in the request body.
     *
     * @param data The string containing the resource event data.
     */
    @PostMapping("/resource")
    public void storeResourceEvent(@RequestBody final String data) {
        ResourceEventDTO resourceEventDTO = createResourceEventDTO(data);

        if (resourceEventDTO == null || isInvalidRequest(data, resourceEventDTO)) {
            return;
        }

        eventService.saveResourceEvent(resourceEventDTO);
    }

    /**
     * Saves the file data passed in the request body.
     *
     * @param data The string containing the file data.
     */
    @PostMapping("/file")
    public void storeFileEvent(@RequestBody final String data) {
        FileDTO fileDTO = createFileDTO(data);

        if (fileDTO == null || isInvalidRequest(data, fileDTO)) {
            return;
        }

        fileService.saveFile(fileDTO);
    }

    /**
     * Saves the sb3 project zip data passed in the request body.
     *
     * @param data The string containing the project data.
     */
    @PostMapping("/zip")
    public void storeZipFile(@RequestBody final String data) {
        Sb3ZipDTO sb3ZipDTO = createSb3ZipDTO(data);

        if (sb3ZipDTO == null || isInvalidRequest(data, sb3ZipDTO)) {
            return;
        }

        fileService.saveSb3Zip(sb3ZipDTO);
    }

    /**
     * Retrieves the sb3 file stored for the experiment with the id passed in the request body, if it exists. If the
     * information passed in the body could not be verified or no file was stored for the experiment, the
     * {@link HttpServletResponse} returns an error status code instead.
     *
     * @param data The request body containing the required information.
     * @param response The servlet response.
     */
    @PostMapping("/sb3")
    public void retrieveSb3File(@RequestBody final String data, final HttpServletResponse response) {
        List<Integer> ids = checkValidRequestData(data);

        if (ids.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int experimentId = ids.get(0);

        try {
            ExperimentProjection projection = experimentService.getSb3File(experimentId);

            if (projection.getProject() == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            response.setContentType("application/zip");
            response.setContentLength(projection.getProject().length);
            response.setHeader("Content-Disposition", "attachment;filename=\"" + "sb3zip_eid_" + experimentId
                    + "\"");
            response.setStatus(HttpServletResponse.SC_OK);
            ServletOutputStream op = response.getOutputStream();
            op.write(projection.getProject());
            op.flush();
        } catch (NotFoundException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } catch (IOException e) {
            LOGGER.error("Could not retrieve sb3 file for experiment with id " + experimentId + " due to IOException!",
                    e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves the last json code saved for the user and experiment passed in the given request body from the
     * database, if it exists. If information passed in the body could not be verified or no json code could be found,
     * the {@link HttpServletResponse} returns an error status code instead.
     *
     * @param data The request body containing the required information.
     * @param response The servlet response.
     */
    @PostMapping("/json")
    public void retrieveLastJson(@RequestBody final String data, final HttpServletResponse response) {
        List<Integer> ids = checkValidRequestData(data);

        if (ids.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            String json = eventService.findFirstJSON(ids.get(1), ids.get(0));

            if (json == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);
            ServletOutputStream op = response.getOutputStream();
            op.write(json.getBytes(StandardCharsets.UTF_8));
            op.flush();
        } catch (NotFoundException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } catch (IOException e) {
            LOGGER.error("Could not retrieve the last saved json code for user with id " + ids.get(1)
                    + " during experiment with id " + ids.get(0) + " due to IOException!", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Extracts the user and experiment id as well as the user's secret from the given request data and checks if the
     * user is a valid participant in the experiment with the given secret.
     *
     * @param data The data containing the required information.
     * @return A list containing the user and experiment id, or an empty list, if the passed data is invalid.
     */
    private List<Integer> checkValidRequestData(final String data) {
        List<Integer> ids = new ArrayList<>();
        JSONObject object = new JSONObject(data);
        int userId = object.getInt("user");
        int experimentId = object.getInt("experiment");
        String secret = object.getString("secret");

        if (!participantService.isInvalidParticipant(userId, experimentId, secret, true)) {
            ids.add(experimentId);
            ids.add(userId);
        }

        return ids;
    }

    /**
     * Checks, if the data passed to the REST controller should be stored in the database. The data should not be stored
     * if the participant data is invalid.
     *
     * @param data The data passed in the request body.
     * @param eventDTO The {@link EventDTO} to check.
     * @return {@code true} if the event should not be persisted or {@code false} otherwise.
     */
    private boolean isInvalidRequest(final String data, final EventDTO eventDTO) {
        JSONObject object = new JSONObject(data);
        String secret = object.getString("secret");
        return participantService.isInvalidParticipant(eventDTO.getUser(), eventDTO.getExperiment(), secret, true);
    }

    /**
     * Creates a {@link BlockEventDTO} with the given data.
     *
     * @param data The data passed in the request body.
     * @return The new block event DTO containing the information.
     */
    private BlockEventDTO createBlockEventDTO(final String data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.readValue(data, BlockEventDTO.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("The block event data sent to the server was incomplete!", e);
            return null;
        }
    }

    /**
     * Creates a {@link ClickEventDTO} with the given data.
     *
     * @param data The data passed in the request body.
     * @return The new click event DTO containing the information.
     */
    private ClickEventDTO createClickEventDTO(final String data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.readValue(data, ClickEventDTO.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("The click event data sent to the server was incomplete!", e);
            return null;
        }
    }

    /**
     * Creates a {@link DebuggerEventDTO} with the given data.
     *
     * @param data The data passed in the request body.
     * @return The new debugger event DTO containing the information.
     */
    private DebuggerEventDTO createDebuggerEventDTO(final String data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.readValue(data, DebuggerEventDTO.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("The debugger event data sent to the server was incomplete!", e);
            return null;
        }
    }

    /**
     * Creates a {@link QuestionEventDTO} with the given data.
     *
     * @param data The data passed in the request body.
     * @return The new question event DTO containing the information.
     */
    private QuestionEventDTO createQuestionEventDTO(final String data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.readValue(data, QuestionEventDTO.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("The question event data sent to the server was incomplete!", e);
            return null;
        }
    }

    /**
     * Creates a {@link ResourceEventDTO} with the given data.
     *
     * @param data The data passed in the request body.
     * @return The new resource event DTO containing the information.
     */
    private ResourceEventDTO createResourceEventDTO(final String data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.readValue(data, ResourceEventDTO.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("The resource event data sent to the server was incomplete!", e);
            return null;
        }
    }

    /**
     * Creates a {@link FileDTO} with the given data.
     *
     * @param data The data passed in the request body.
     * @return The new file DTO containing the information.
     */
    private FileDTO createFileDTO(final String data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.readValue(data, FileDTO.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("The file data sent to the server was incomplete!", e);
            return null;
        }
    }

    /**
     * Creates a {@link Sb3ZipDTO} with the given data.
     *
     * @param data The data passed in the request body.
     * @return The new sb3 zip DTO containing the information.
     */
    private Sb3ZipDTO createSb3ZipDTO(final String data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.readValue(data, Sb3ZipDTO.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("The sb3 zip file data sent to the server was incomplete!", e);
            return null;
        }
    }

}
