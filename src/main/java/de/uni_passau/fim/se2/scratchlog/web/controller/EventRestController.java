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

package de.uni_passau.fim.se2.scratchlog.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.uni_passau.fim.se2.scratchlog.application.exception.IncompleteDataException;
import de.uni_passau.fim.se2.scratchlog.application.exception.NotFoundException;
import de.uni_passau.fim.se2.scratchlog.application.service.CodeService;
import de.uni_passau.fim.se2.scratchlog.application.service.EventService;
import de.uni_passau.fim.se2.scratchlog.application.service.ExperimentService;
import de.uni_passau.fim.se2.scratchlog.application.service.FileService;
import de.uni_passau.fim.se2.scratchlog.application.service.ParticipantService;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.ExperimentProjection;
import de.uni_passau.fim.se2.scratchlog.web.dto.BlockEventDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.ClickEventDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.DebuggerEventDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.EventDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.FileDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.QuestionEventDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.ResourceEventDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.Sb3ZipDTO;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
     * The code service to use for retrieving participant codes.
     */
    private final CodeService codeService;

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
     * Converter from/to JSON.
     */
    private final ObjectMapper objectMapper;

    /**
     * Constructs an event rest controller with the given dependencies.
     *
     * @param eventService The event service to use.
     * @param codeService The coder service to use.
     * @param fileService The file service to use.
     * @param experimentService The experiment service to use.
     * @param participantService The participant service to use.
     */
    @Autowired
    public EventRestController(final EventService eventService, final CodeService codeService,
                               final FileService fileService, final ExperimentService experimentService,
                               final ParticipantService participantService) {
        this.eventService = eventService;
        this.codeService = codeService;
        this.fileService = fileService;
        this.experimentService = experimentService;
        this.participantService = participantService;

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Saves the block event data passed in the request body.
     *
     * @param blockEventDTO The JSON containing the block event data.
     */
    @PostMapping("/block")
    public void storeBlockEvent(@RequestBody final BlockEventDTO blockEventDTO) {
        if (blockEventDTO == null || isInvalidRequest(blockEventDTO)) {
            return;
        }

        eventService.saveBlockEvent(blockEventDTO);
    }

    /**
     * Saves the click event data passed in the request body.
     *
     * @param clickEventDTO The JSON containing the click event data.
     */
    @PostMapping("/click")
    public void storeClickEvent(@RequestBody final ClickEventDTO clickEventDTO) {
        if (clickEventDTO == null || hasInvalidParticipant(clickEventDTO)) {
            return;
        }

        eventService.saveClickEvent(clickEventDTO);
    }

    /**
     * Saves the debugger event data passed in the request body.
     *
     * @param debuggerEventDTO The JSON containing the debugger event data.
     */
    @PostMapping("/debugger")
    public void storeDebuggerEvent(@RequestBody final DebuggerEventDTO debuggerEventDTO) {
        if (debuggerEventDTO == null || hasInvalidParticipant(debuggerEventDTO)) {
            return;
        }

        eventService.saveDebuggerEvent(debuggerEventDTO);
    }

    /**
     * Saves the question event data passed in the request body.
     *
     * @param questionEventDTO The JSON containing the question event data.
     */
    @PostMapping("/question")
    public void storeQuestionEvent(@RequestBody final QuestionEventDTO questionEventDTO) {
        if (questionEventDTO == null || hasInvalidParticipant(questionEventDTO)) {
            return;
        }

        eventService.saveQuestionEvent(questionEventDTO);
    }

    /**
     * Saves the resource event data passed in the request body.
     *
     * @param resourceEventDTO The JSON containing the resource event data.
     */
    @PostMapping("/resource")
    public void storeResourceEvent(@RequestBody final ResourceEventDTO resourceEventDTO) {
        if (resourceEventDTO == null || hasInvalidParticipant(resourceEventDTO)) {
            return;
        }

        eventService.saveResourceEvent(resourceEventDTO);
    }

    /**
     * Saves the file data passed in the request body.
     *
     * @param fileDTO The JSON containing the file data.
     */
    @PostMapping("/file")
    public void storeFileEvent(@RequestBody final FileDTO fileDTO) {
        if (fileDTO == null || hasInvalidParticipant(fileDTO)) {
            return;
        }

        fileService.saveFile(fileDTO);
    }

    /**
     * Saves the sb3 project zip data passed in the request body.
     *
     * @param sb3ZipDTO The JSON containing the project data.
     */
    @PostMapping("/zip")
    public void storeZipFile(@RequestBody final Sb3ZipDTO sb3ZipDTO) {
        if (sb3ZipDTO == null || hasInvalidParticipant(sb3ZipDTO)) {
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
    public void retrieveSb3File(@RequestBody final UserDataRequestDTO data, final HttpServletResponse response) {
        checkValidDataRequestElseThrow(data);

        int experimentId = data.experiment();

        try {
            ExperimentProjection projection = experimentService.getSb3File(experimentId, false);

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
            LOGGER.error("Could not retrieve sb3 file for experiment with id {} due to IOException!", experimentId, e);
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
    public void retrieveLastJson(@RequestBody final UserDataRequestDTO data, final HttpServletResponse response) {
        checkValidDataRequestElseThrow(data);

        try {
            String json = codeService.findFirstJSON(data.user(), data.experiment());

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
            LOGGER.error("Could not retrieve the last saved json code for user with id {}"
                    + " during experiment with id {} due to IOException!", data.user(), data.experiment(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    public record UserDataRequestDTO(int user, int experiment, String secret) { }

    private void checkValidDataRequestElseThrow(final UserDataRequestDTO data) {
        if (participantService.isInvalidParticipant(data.user(), data.experiment(), data.secret(), true)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Checks, if the data passed to the REST controller should be stored in the database. The data should not be stored
     * if the participant data is invalid.
     *
     * @param blockEvent The event passed in the request body.
     * @return {@code true} if the event should not be persisted or {@code false} otherwise.
     */
    private boolean isInvalidRequest(final BlockEventDTO blockEvent) {
        validateScratchJson(blockEvent.getCode());
        return hasInvalidParticipant(blockEvent);
    }

    /**
     * Checks, if the data passed to the REST controller should be stored in the database. The data should not be stored
     * if the participant data is invalid.
     *
     * @param event The event passed in the request body.
     * @return {@code true} if the event should not be persisted or {@code false} otherwise.
     */
    private boolean hasInvalidParticipant(final EventDTO event) {
        String secret = event.getSecret();
        return participantService.isInvalidParticipant(event.getUser(), event.getExperiment(), secret, true);
    }

    /**
     * Checks that the Scratch project JSON is indeed JSON.
     *
     * @param code Some Scratch project json.
     */
    private void validateScratchJson(final String code) throws IncompleteDataException {
        if (code == null) {
            return;
        }

        try {
            final JsonNode node = objectMapper.readTree(code);
            if (!node.isObject()) {
                throw new IncompleteDataException("Invalid Scratch project json!");
            }
        } catch (JsonProcessingException e) {
            throw new IncompleteDataException("Invalid Scratch project JSON!");
        }
    }

}
