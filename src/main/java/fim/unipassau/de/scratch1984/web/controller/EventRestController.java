package fim.unipassau.de.scratch1984.web.controller;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.EventService;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.application.service.FileService;
import fim.unipassau.de.scratch1984.persistence.projection.ExperimentProjection;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.util.NumberParser;
import fim.unipassau.de.scratch1984.web.dto.BlockEventDTO;
import fim.unipassau.de.scratch1984.web.dto.ClickEventDTO;
import fim.unipassau.de.scratch1984.web.dto.DebuggerEventDTO;
import fim.unipassau.de.scratch1984.web.dto.EventDTO;
import fim.unipassau.de.scratch1984.web.dto.FileDTO;
import fim.unipassau.de.scratch1984.web.dto.QuestionEventDTO;
import fim.unipassau.de.scratch1984.web.dto.ResourceEventDTO;
import fim.unipassau.de.scratch1984.web.dto.Sb3ZipDTO;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Base64;

/**
 * The REST controller receiving all the logging requests sent by the Scratch GUI and VM.
 */
@RestController
@RequestMapping(value = "/store")
public class EventRestController {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger logger = LoggerFactory.getLogger(EventRestController.class);

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
     * Constructs an event rest controller with the given dependencies.
     *
     * @param eventService The event service to use.
     * @param fileService The file service to use.
     * @param experimentService The experiment service to use.
     */
    @Autowired
    public EventRestController(final EventService eventService, final FileService fileService,
                               final ExperimentService experimentService) {
        this.eventService = eventService;
        this.fileService = fileService;
        this.experimentService = experimentService;
    }

    /**
     * Saves the block event data passed in the request body.
     *
     * @param data The string containing the block event data.
     */
    @PostMapping("/block")
    public void storeBlockEvent(@RequestBody final String data) {
        BlockEventDTO blockEventDTO = createBlockEventDTO(data);

        if (blockEventDTO == null) {
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

        if (clickEventDTO == null) {
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

        if (debuggerEventDTO == null) {
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

        if (questionEventDTO == null) {
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

        if (resourceEventDTO == null) {
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

        if (fileDTO == null) {
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

        if (sb3ZipDTO == null) {
            return;
        }

        fileService.saveSb3Zip(sb3ZipDTO);
    }

    /**
     * Retrieves the sb3 file stored for the experiment with the given id, if it exists. If the passed id is invalid, no
     * experiment could be found or no file was stored for the experiment, the {@link HttpServletResponse} returns an
     * error status code instead.
     *
     * @param id The experiment id.
     * @param response The servlet response.
     */
    @GetMapping("/sb3")
    public void retrieveSb3File(@RequestParam("id") final String id, final HttpServletResponse response) {
        if (id == null) {
            logger.error("Cannot retrieve sb3 file for experiment with id null!");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int experimentId = NumberParser.parseNumber(id);

        if (experimentId < Constants.MIN_ID) {
            logger.error("Cannot retrieve sb3 file for experiment with invalid id " + experimentId + "!");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

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
            logger.error("Could not retrieve sb3 file for experiment with id " + experimentId + " due to IOException!",
                    e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves the last json code saved for the given user during the given experiment from the database, if it
     * exists. If the passed ids are invalid, no corresponding user or participant or no json code could be found, the
     * {@link HttpServletResponse} returns an error status code instead.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @param response The servlet response.
     */
    @GetMapping("/json")
    public void retrieveLastJson(@RequestParam("user") final String user,
                                  @RequestParam("experiment") final String experiment,
                                  final HttpServletResponse response) {
        if (user == null || experiment == null) {
            logger.error("Cannot retrieve the last json file for experiment or user with id null!");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int userId = NumberParser.parseNumber(user);
        int experimentId = NumberParser.parseNumber(experiment);

        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            logger.error("Cannot retrieve the last json file for experiment with invalid id " + experiment
                    + " or user with invalid id " + user + "!");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            String json = eventService.findFirstJSON(userId, experimentId);

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
            logger.error("Could not retrieve the last saved json code for user with id " + userId
                    + " during experiment with id " + experimentId + " due to IOException!", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Sets the user, experiment and time for every {@link EventDTO} using the values from the passed
     * {@link JSONObject}.
     *
     * @param eventDTO The dto for which the properties are to be set.
     * @param object The JSON providing the values.
     */
    private void setEventDTOData(final EventDTO eventDTO, final JSONObject object) {
        eventDTO.setUser(object.getInt("user"));
        eventDTO.setExperiment(object.getInt("experiment"));
        eventDTO.setDate(LocalDateTime.ofInstant(Instant.parse(object.getString("time")), ZoneId.systemDefault()));
    }

    /**
     * Creates a {@link BlockEventDTO} with the given data.
     *
     * @param data The data passed in the request body.
     * @return The new block event DTO containing the information.
     */
    private BlockEventDTO createBlockEventDTO(final String data) {
        BlockEventDTO dto = new BlockEventDTO();

        try {
            JSONObject object = new JSONObject(data);
            String spritename = object.getString("spritename");
            String metadata = object.getString("metadata");
            String xml = object.getString("xml");
            String json = object.getString("json");

            setEventDTOData(dto, object);
            dto.setEventType(BlockEventDTO.BlockEventType.valueOf(object.getString("type")));
            dto.setEvent(BlockEventDTO.BlockEvent.valueOf(object.getString("event")));

            if (!spritename.trim().isBlank()) {
                dto.setSprite(object.getString("spritename"));
            }
            if (!metadata.trim().isBlank()) {
                dto.setMetadata(metadata);
            }
            if (!xml.trim().isBlank()) {
                dto.setXml(xml);
            }
            if (!json.trim().isBlank()) {
                dto.setCode(json);
            }
        } catch (NullPointerException | ClassCastException | DateTimeParseException | IllegalArgumentException
                | JSONException e) {
            logger.error("The block event data sent to the server was incomplete!", e);
            return null;
        }

        return dto;
    }

    /**
     * Creates a {@link ClickEventDTO} with the given data.
     *
     * @param data The data passed in the request body.
     * @return The new click event DTO containing the information.
     */
    private ClickEventDTO createClickEventDTO(final String data) {
        ClickEventDTO dto = new ClickEventDTO();

        try {
            JSONObject object = new JSONObject(data);
            String metadata = object.getString("metadata");

            setEventDTOData(dto, object);
            dto.setEventType(ClickEventDTO.ClickEventType.valueOf(object.getString("type")));
            dto.setEvent(ClickEventDTO.ClickEvent.valueOf(object.getString("event")));

            if (!metadata.trim().isBlank()) {
                dto.setMetadata(metadata);
            }
        } catch (NullPointerException | ClassCastException | DateTimeParseException | IllegalArgumentException
                | JSONException e) {
            logger.error("The click event data sent to the server was incomplete!", e);
            return null;
        }

        return dto;
    }

    /**
     * Creates a {@link DebuggerEventDTO} with the given data.
     *
     * @param data The data passed in the request body.
     * @return The new debugger event DTO containing the information.
     */
    private DebuggerEventDTO createDebuggerEventDTO(final String data) {
        DebuggerEventDTO dto = new DebuggerEventDTO();

        try {
            JSONObject object = new JSONObject(data);
            String id = object.getString("id");
            String name = object.getString("name");
            String original = object.get("original").toString();
            String execution = object.get("execution").toString();

            setEventDTOData(dto, object);
            dto.setEventType(DebuggerEventDTO.DebuggerEventType.valueOf(object.getString("type")));
            dto.setEvent(DebuggerEventDTO.DebuggerEvent.valueOf(object.getString("event")));

            if (!id.trim().isBlank()) {
                dto.setBlockOrTargetID(id);
            }
            if (!name.trim().isBlank()) {
                dto.setNameOrOpcode(name);
            }
            if (!original.trim().isBlank()) {
                dto.setOriginal(object.getInt("original"));
            }
            if (!execution.trim().isBlank()) {
                dto.setExecution(object.getInt("execution"));
            }
        } catch (NullPointerException | ClassCastException | DateTimeParseException | IllegalArgumentException
                | JSONException e) {
            logger.error("The debugger event data sent to the server was incomplete!", e);
            return null;
        }

        return dto;
    }

    /**
     * Creates a {@link QuestionEventDTO} with the given data.
     *
     * @param data The data passed in the request body.
     * @return The new question event DTO containing the information.
     */
    private QuestionEventDTO createQuestionEventDTO(final String data) {
        QuestionEventDTO dto = new QuestionEventDTO();

        try {
            JSONObject object = new JSONObject(data);
            String feedback = object.get("feedback").toString();
            String type = object.getString("q_type");
            String values = object.getString("values");
            String category = object.getString("category");
            String form = object.getString("form");
            String blockId = object.getString("id");
            String opcode = object.getString("opcode");

            setEventDTOData(dto, object);
            dto.setEventType(QuestionEventDTO.QuestionEventType.valueOf(object.getString("type")));
            dto.setEvent(QuestionEventDTO.QuestionEvent.valueOf(object.getString("event")));

            if (!feedback.trim().isBlank()) {
                dto.setFeedback(object.getInt("feedback"));
            }
            if (!type.trim().isBlank()) {
                dto.setType(type);
            }
            if (!values.trim().isBlank()) {
                dto.setValues(values.split(","));
            }
            if (!category.trim().isBlank()) {
                dto.setCategory(category);
            }
            if (!form.trim().isBlank()) {
                dto.setForm(form);
            }
            if (!blockId.trim().isBlank()) {
                dto.setBlockID(blockId);
            }
            if (!opcode.trim().isBlank()) {
                dto.setOpcode(opcode);
            }

            return dto;
        } catch (NullPointerException | ClassCastException | DateTimeParseException | IllegalArgumentException
                | JSONException e) {
            logger.error("The question event data sent to the server was incomplete!", e);
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
        ResourceEventDTO dto = new ResourceEventDTO();

        try {
            JSONObject object = new JSONObject(data);
            String name = object.getString("name");
            String md5 = object.getString("md5");
            String dataFormat = object.getString("dataFormat");

            setEventDTOData(dto, object);
            dto.setEventType(ResourceEventDTO.ResourceEventType.valueOf(object.getString("type")));
            dto.setEvent(ResourceEventDTO.ResourceEvent.valueOf(object.getString("event")));
            dto.setLibraryResource(ResourceEventDTO.LibraryResource.valueOf(object.getString("libraryResource")));

            if (!name.trim().isBlank()) {
                dto.setName(name);
            }
            if (!md5.trim().isBlank()) {
                dto.setMd5(md5);
            }
            if (!dataFormat.isBlank()) {
                dto.setFiletype(dataFormat);
            }
        } catch (NullPointerException | ClassCastException | DateTimeParseException | IllegalArgumentException
                | JSONException e) {
            logger.error("The resource event data sent to the server was incomplete!", e);
            return null;
        }

        return dto;
    }

    /**
     * Creates a {@link FileDTO} with the given data.
     *
     * @param data The data passed in the request body.
     * @return The new file DTO containing the information.
     */
    private FileDTO createFileDTO(final String data) {
        FileDTO dto = new FileDTO();

        try {
            JSONObject object = new JSONObject(data);
            setEventDTOData(dto, object);
            dto.setName(object.getString("name"));
            dto.setFiletype(object.getString("type"));
            dto.setContent(Base64.getDecoder().decode(object.getString("file")));
        } catch (NullPointerException | ClassCastException | DateTimeParseException | IllegalArgumentException
                | JSONException e) {
            logger.error("The file data sent to the server was incomplete!", e);
            return null;
        }

        return dto;
    }

    /**
     * Creates a {@link Sb3ZipDTO} with the given data.
     *
     * @param data The data passed in the request body.
     * @return The new sb3 zip DTO containing the information.
     */
    private Sb3ZipDTO createSb3ZipDTO(final String data) {
        Sb3ZipDTO dto = new Sb3ZipDTO();

        try {
            JSONObject object = new JSONObject(data);
            setEventDTOData(dto, object);
            dto.setName(object.getString("name"));
            dto.setContent(Base64.getDecoder().decode(object.getString("zip")));
        } catch (NullPointerException | ClassCastException | DateTimeParseException | IllegalArgumentException
                | JSONException e) {
            logger.error("The sb3 zip file data sent to the server was incomplete!", e);
            return null;
        }

        return dto;
    }

}
