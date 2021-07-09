package fim.unipassau.de.scratch1984.web.controller;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.EventService;
import fim.unipassau.de.scratch1984.application.service.FileService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.persistence.projection.FileProjection;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.web.dto.EventCountDTO;
import fim.unipassau.de.scratch1984.web.dto.FileDTO;
import fim.unipassau.de.scratch1984.web.dto.Sb3ZipDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The controller for result management.
 */
@Controller
@RequestMapping(value = "/result")
public class ResultController {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger logger = LoggerFactory.getLogger(ResultController.class);

    /**
     * The user service to use for user management.
     */
    private final UserService userService;

    /**
     * The event service to use for event management.
     */
    private final EventService eventService;

    /**
     * The file service to use for file management.
     */
    private final FileService fileService;

    /**
     * String corresponding to the result page.
     */
    private static final String RESULT = "result";

    /**
     * String corresponding to redirecting to the error page.
     */
    private static final String ERROR = "redirect:/error";

    /**
     * Constructs a new result controller with the given dependencies.
     *
     * @param userService The {@link UserService} to use.
     * @param eventService The {@link EventService} to use.
     * @param fileService The {@link FileService} to use.
     */
    @Autowired
    public ResultController(final UserService userService, final EventService eventService,
                            final FileService fileService) {
        this.userService = userService;
        this.eventService = eventService;
        this.fileService = fileService;
    }

    /**
     * Returns the result page containing the result information for the user with the given id during the experiment
     * with the given id. If the passed parameters are invalid, the user is not a participant in the given experiment,
     * or no corresponding user or experiment could be found, the user is redirected to the error page instead.
     *
     * @param experiment The experiment id.
     * @param user The user id.
     * @param model The model used to store information.
     * @return The result page on success, or the error page otherwise.
     */
    @GetMapping("")
    @Secured("ROLE_ADMIN")
    public String getResult(@RequestParam("experiment") final String experiment,
                            @RequestParam("user") final String user, final Model model) {
        if (user == null || experiment == null) {
            logger.error("Cannot return result page for user with id null or experiment with id null!");
            return ERROR;
        }

        int userId = parseId(user);
        int experimentId = parseId(experiment);

        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            logger.error("Cannot return result page for user with invalid id " + userId + " or experiment with invalid "
                    + "id " + experimentId + "!");
            return ERROR;
        }
        if (!userService.existsParticipant(userId, experimentId)) {
            logger.error("Could not find participant entry for user with id " + userId + " for experiment with id "
                    + experimentId);
            return ERROR;
        }

        try {
            List<EventCountDTO> blockEvents = eventService.getBlockEventCounts(userId, experimentId);
            List<EventCountDTO> resourceEvents = eventService.getResourceEventCounts(userId, experimentId);
            List<FileProjection> files = fileService.getFiles(userId, experimentId);
            List<Integer> zipIds = fileService.getZipIds(userId, experimentId);
            model.addAttribute("blockEvents", blockEvents);
            model.addAttribute("resourceEvents", resourceEvents);
            model.addAttribute("files", files);
            model.addAttribute("zips", zipIds);
            model.addAttribute("user", userId);
            model.addAttribute("experiment", experimentId);
            return RESULT;
        } catch (NotFoundException e) {
            return ERROR;
        }
    }

    /**
     * Makes the file with the given id available for download, if it exists. If the given id is invalid or no file
     * could be found in the database, the user is redirected to the error page instead.
     *
     * @param id The file id to search for.
     * @return The file for download on success, or the error page otherwise.
     */
    @GetMapping("/file")
    @Secured("ROLE_ADMIN")
    public Object downloadFile(@RequestParam("id") final String id) {
        if (id == null) {
            logger.error("Cannot download file with invalid id null!");
            return ERROR;
        }

        int fileId = parseId(id);

        if (fileId < Constants.MIN_ID) {
            logger.error("Cannot download file with invalid id " + fileId + "!");
            return ERROR;
        }

        try {
            FileDTO fileDTO = fileService.findFile(fileId);
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""
                    + fileDTO.getName() + "\"").body(fileDTO.getContent());
        } catch (NotFoundException e) {
            return ERROR;
        }
    }

    /**
     * Retrieves the zip file with the given id and makes it available for download, if it exists. If the id is invalid
     * or no zip file could be found in the database, the user is redirected to the error page instead.
     *
     * @param id The zip file id to search for.
     * @return The zip file for download on success, or the error page otherwise.
     */
    @GetMapping("/zip")
    @Secured("ROLE_ADMIN")
    public Object downloadZip(@RequestParam("id") final String id) {
        if (id == null) {
            logger.error("Cannot download zip file with invalid id null!");
            return ERROR;
        }

        int zipId = parseId(id);

        if (zipId < Constants.MIN_ID) {
            logger.error("Cannot download zip file with invalid id " + zipId + "!");
            return ERROR;
        }

        try {
            Sb3ZipDTO sb3ZipDTO = fileService.findZip(zipId);
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""
                    + sb3ZipDTO.getName() + "\"").body(sb3ZipDTO.getContent());
        } catch (NotFoundException e) {
            return ERROR;
        }
    }

    /**
     * Retrieves all zip files created for the given user during the given experiment and makes them available for
     * download, in a zip file. If the ids are invalid, no files could be found, or an {@link IOException} occurred,
     * nothing happens.
     *
     * @param experiment The experiment id to search for.
     * @param user The user id to search for.
     * @param httpServletResponse The servlet response returning the files.
     */
    @GetMapping("/zips")
    @Secured("ROLE_ADMIN")
    public void downloadAllZips(@RequestParam("experiment") final String experiment,
                                @RequestParam("user") final String user,
                                final HttpServletResponse httpServletResponse) {
        if (user == null || experiment == null) {
            logger.error("Cannot download zip files for user with id null or experiment with id null!");
            httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int userId = parseId(user);
        int experimentId = parseId(experiment);

        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            logger.error("Cannot download zip files for user with invalid id " + userId + " or experiment with invalid "
                    + "id " + experimentId + "!");
            httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            List<Sb3ZipDTO> sb3ZipDTOS = fileService.getZipFiles(userId, experimentId);
            httpServletResponse.setContentType("application/zip");
            httpServletResponse.setHeader("Content-Disposition", "attachment;filename=projects_user" + userId
                    + "_experiment" + experimentId + ".zip");
            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            ZipOutputStream zos = new ZipOutputStream(httpServletResponse.getOutputStream());

            for (Sb3ZipDTO sb3ZipDTO : sb3ZipDTOS) {
                ZipEntry entry = new ZipEntry(sb3ZipDTO.getName() + sb3ZipDTO.getId());
                entry.setSize(sb3ZipDTO.getContent().length);
                zos.putNextEntry(entry);
                zos.write(sb3ZipDTO.getContent());
                zos.closeEntry();
            }

            zos.finish();
        } catch (NotFoundException e) {
            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } catch (IOException e) {
            logger.error("Could not download zip files due to IOException!", e);
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Returns the corresponding int value of the given id, or -1, if the id is not a number.
     *
     * @param id The id in its string representation.
     * @return The corresponding int value, or -1.
     */
    private int parseId(final String id) {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

}
