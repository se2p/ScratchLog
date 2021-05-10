package fim.unipassau.de.scratch1984.web.controller;

import fim.unipassau.de.scratch1984.application.service.EventService;
import fim.unipassau.de.scratch1984.application.service.FileService;
import fim.unipassau.de.scratch1984.web.dto.BlockEventDTO;
import fim.unipassau.de.scratch1984.web.dto.ResourceEventDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The REST controller receiving all the logging requests sent by the Scratch GUI and VM.
 */
@CrossOrigin(origins = "http://localhost:8601")
@RestController
@RequestMapping(value = "/store")
public class EventRestController {

    /**
     * The event service to use to save the received event data.
     */
    private final EventService eventService;

    /**
     * The file service to use to save the received file data.
     */
    private final FileService fileService;

    /**
     * Constructs an event rest controller with the given dependencies.
     *
     * @param eventService The event service to use.
     * @param fileService The file service to use.
     */
    @Autowired
    public EventRestController(final EventService eventService, final FileService fileService) {
        this.eventService = eventService;
        this.fileService = fileService;
    }

    /**
     * Saves the block event data passed in the request body.
     *
     * @param data The string containing the block event data.
     */
    @PostMapping("/block")
    public void storeBlockEvent(@RequestBody final String data) {
    }

    /**
     * Saves the resource event data passed in the request body.
     *
     * @param data The string containing the resource event data.
     */
    @PostMapping("/resource")
    public void storeResourceEvent(@RequestBody final String data) {
    }

    /**
     * Saves the file data passed in the request body.
     *
     * @param data The string containing the file data.
     */
    @PostMapping("/file")
    public void storeFileEvent(@RequestBody final String data) {
    }

    /**
     * Creates a {@link BlockEventDTO} with the given data.
     *
     * @param data The data passed in the request body.
     * @return The new block event DTO containing the information.
     */
    private BlockEventDTO createBlockEventDTO(final String data) {
        return null;
    }

    /**
     * Creates a {@link ResourceEventDTO} with the given data.
     *
     * @param data The data passed in the request body.
     * @return The new resource event DTO containing the information.
     */
    private ResourceEventDTO createResourceEventDTO(final String data) {
        return null;
    }

}
