package fim.unipassau.de.scratch1984.web.controller;

import com.opencsv.CSVWriter;
import fim.unipassau.de.scratch1984.application.exception.IncompleteDataException;
import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.EventService;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.application.service.FileService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.persistence.projection.BlockEventJSONProjection;
import fim.unipassau.de.scratch1984.persistence.projection.BlockEventProjection;
import fim.unipassau.de.scratch1984.persistence.projection.BlockEventXMLProjection;
import fim.unipassau.de.scratch1984.persistence.projection.ExperimentProjection;
import fim.unipassau.de.scratch1984.persistence.projection.FileProjection;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.util.NumberParser;
import fim.unipassau.de.scratch1984.web.dto.CodesDataDTO;
import fim.unipassau.de.scratch1984.web.dto.EventCountDTO;
import fim.unipassau.de.scratch1984.web.dto.FileDTO;
import fim.unipassau.de.scratch1984.web.dto.Sb3ZipDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
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
     * The experiment service to use for experiment management.
     */
    private final ExperimentService experimentService;

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
     * String corresponding to the id request parameter.
     */
    private static final String ID = "id";

    /**
     * String corresponding to the user request parameter.
     */
    private static final String USER = "user";

    /**
     * String corresponding to the experiment request parameter.
     */
    private static final String EXPERIMENT = "experiment";

    /**
     * Constructs a new result controller with the given dependencies.
     *
     * @param userService The {@link UserService} to use.
     * @param experimentService The {@link ExperimentService} to use.
     * @param eventService The {@link EventService} to use.
     * @param fileService The {@link FileService} to use.
     */
    @Autowired
    public ResultController(final UserService userService, final ExperimentService experimentService,
                            final EventService eventService, final FileService fileService) {
        this.userService = userService;
        this.experimentService = experimentService;
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
    @Secured(Constants.ROLE_ADMIN)
    public ModelAndView getResult(@RequestParam(EXPERIMENT) final String experiment,
                                  @RequestParam(USER) final String user, final Model model) {
        if (user == null || experiment == null) {
            logger.error("Cannot return result page for user with id null or experiment with id null!");
            return new ModelAndView(Constants.ERROR);
        }

        int userId = NumberParser.parseNumber(user);
        int experimentId = NumberParser.parseNumber(experiment);

        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            logger.error("Cannot return result page for user with invalid id " + userId + " or experiment with invalid "
                    + "id " + experimentId + "!");
            return new ModelAndView(Constants.ERROR);
        }
        if (!userService.existsParticipant(userId, experimentId)) {
            logger.error("Could not find participant entry for user with id " + userId + " for experiment with id "
                    + experimentId);
            return new ModelAndView(Constants.ERROR);
        }

        try {
            List<EventCountDTO> blockEvents = eventService.getBlockEventCounts(userId, experimentId);
            List<EventCountDTO> resourceEvents = eventService.getResourceEventCounts(userId, experimentId);
            List<FileProjection> files = fileService.getFiles(userId, experimentId);
            List<Integer> zipIds = fileService.getZipIds(userId, experimentId);
            CodesDataDTO codesDataDTO = eventService.getCodesData(userId, experimentId);

            model.addAttribute("codeCount", Math.max(codesDataDTO.getCount(), 0));
            model.addAttribute("pageSize", Constants.PAGE_SIZE);
            model.addAttribute("blockEvents", blockEvents);
            model.addAttribute("resourceEvents", resourceEvents);
            model.addAttribute("files", files);
            model.addAttribute("zips", zipIds);
            model.addAttribute("user", userId);
            model.addAttribute("experiment", experimentId);
            return new ModelAndView(RESULT);
        } catch (NotFoundException e) {
            return new ModelAndView(Constants.ERROR);
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
    @Secured(Constants.ROLE_ADMIN)
    public Object downloadFile(@RequestParam(ID) final String id) {
        if (id == null) {
            logger.error("Cannot download file with invalid id null!");
            return Constants.ERROR;
        }

        int fileId = NumberParser.parseNumber(id);

        if (fileId < Constants.MIN_ID) {
            logger.error("Cannot download file with invalid id " + fileId + "!");
            return Constants.ERROR;
        }

        try {
            FileDTO fileDTO = fileService.findFile(fileId);
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""
                    + fileDTO.getName() + "\"").body(fileDTO.getContent());
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }
    }

    /**
     * Generates a sb3 file for the user and experiment with the given id with the project.json corresponding to the
     * json string saved during the block event with the given id and makes the file available for download. Apart
     * from the saved json string, all costumes and sounds present in the experiment project file are added to the sb3
     * file as well as all files saved for the user during the experiment. If the passed parameters are invalid, an
     * {@link IncompleteDataException} is thrown instead. If an IOException occurs during the creation of the sb3 file,
     * a {@link RuntimeException} is thrown.
     *
     * @param experiment The id of the experiment.
     * @param user The id of the user.
     * @param json The block event id to search for.
     * @param httpServletResponse The servlet response.
     */
    @GetMapping("/generate")
    @Secured(Constants.ROLE_ADMIN)
    public void generateZipFile(@RequestParam(EXPERIMENT) final String experiment,
                                @RequestParam(USER) final String user, @RequestParam("json") final String json,
                                final HttpServletResponse httpServletResponse) {
        if (json == null || experiment == null || user == null) {
            logger.error("Cannot generate zip file with JSON, experiment or user null!");
            throw new IncompleteDataException("Cannot generate zip file with JSON, experiment or user null!");
        }

        int userId = NumberParser.parseNumber(user);
        int experimentId = NumberParser.parseNumber(experiment);
        int jsonId = NumberParser.parseNumber(json);

        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID || jsonId < Constants.MIN_ID) {
            logger.error("Cannot generate zip file for user with invalid id " + user + " or experiment with invalid "
                    + "id " + experiment + "or json with invalid id " + json + "!");
            throw new IncompleteDataException("Cannot generate zip file for user with invalid id " + user
                    + " or experiment with invalid id " + experiment + "or json with invalid id " + json + "!");
        }

        ExperimentProjection projection = experimentService.getSb3File(experimentId);
        List<FileDTO> fileDTOS = fileService.getFileDTOs(userId, experimentId);
        byte[] code = eventService.findJsonById(jsonId).getBytes(StandardCharsets.UTF_8);

        try {
            ZipOutputStream zos = getZipOutputStream(httpServletResponse, userId, experimentId, "sb3");

            if (projection.getProject() != null) {
                writeInitialProjectData(zos, projection.getProject());
            }

            for (FileDTO fileDTO : fileDTOS) {
                writeFileDataNoZips(zos, fileDTO);
            }

            writeJsonData(zos, code);
            zos.finish();
        } catch (IOException e) {
            logger.error("Could not generate zip file due to IOException!", e);
            throw new RuntimeException("Could not generate zip file due to IOException!");
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
    @Secured(Constants.ROLE_ADMIN)
    public Object downloadZip(@RequestParam(ID) final String id) {
        if (id == null) {
            logger.error("Cannot download zip file with invalid id null!");
            return Constants.ERROR;
        }

        int zipId = NumberParser.parseNumber(id);

        if (zipId < Constants.MIN_ID) {
            logger.error("Cannot download zip file with invalid id " + zipId + "!");
            return Constants.ERROR;
        }

        try {
            Sb3ZipDTO sb3ZipDTO = fileService.findZip(zipId);
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""
                    + sb3ZipDTO.getName() + "\"").body(sb3ZipDTO.getContent());
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }
    }

    /**
     * Retrieves all zip files created for the given user during the given experiment and makes them available for
     * download in a zip file. If the ids are invalid an {@link IncompleteDataException} is thrown instead. If an
     * {@link IOException} occurs, a {@link RuntimeException} is thrown.
     *
     * @param experiment The experiment id to search for.
     * @param user The user id to search for.
     * @param httpServletResponse The servlet response returning the files.
     */
    @GetMapping("/zips")
    @Secured(Constants.ROLE_ADMIN)
    public void downloadAllZips(@RequestParam(EXPERIMENT) final String experiment,
                                @RequestParam(USER) final String user,
                                final HttpServletResponse httpServletResponse) {
        if (user == null || experiment == null) {
            logger.error("Cannot download zip files for user with id null or experiment with id null!");
            throw new IncompleteDataException("Cannot download zip files for user with id null or experiment with id "
                    + "null!");
        }

        int userId = NumberParser.parseNumber(user);
        int experimentId = NumberParser.parseNumber(experiment);

        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            logger.error("Cannot download zip files for user with invalid id " + userId + " or experiment with invalid "
                    + "id " + experimentId + "!");
            throw new IncompleteDataException("Cannot download zip files for user with invalid id " + userId
                    + " or experiment with invalid id " + experimentId + "!");
        }

        try {
            List<Sb3ZipDTO> sb3ZipDTOS = fileService.getZipFiles(userId, experimentId);
            ZipOutputStream zos = getZipOutputStream(httpServletResponse, userId, experimentId, "projects");

            for (Sb3ZipDTO sb3ZipDTO : sb3ZipDTOS) {
                ZipEntry entry = new ZipEntry(sb3ZipDTO.getId() + sb3ZipDTO.getName());
                entry.setSize(sb3ZipDTO.getContent().length);
                zos.putNextEntry(entry);
                zos.write(sb3ZipDTO.getContent());
                zos.closeEntry();
            }

            zos.finish();
        } catch (IOException e) {
            logger.error("Could not download zip files due to IOException!", e);
            throw new RuntimeException("Could not download zip files due to IOException!");
        }
    }

    /**
     * Retrieves all the xml codes that were saved for the given user during the given experiment and makes them
     * available for download in a zip file. If the ids are invalid an {@link IncompleteDataException} is thrown
     * instead. If an {@link IOException} occurs, a {@link RuntimeException} is thrown.
     *
     * @param experiment The experiment id to search for.
     * @param user The user id to search for.
     * @param httpServletResponse The servlet response returning the files.
     */
    @GetMapping("/xmls")
    @Secured(Constants.ROLE_ADMIN)
    public void downloadAllXmlFiles(@RequestParam(EXPERIMENT) final String experiment,
                                    @RequestParam(USER) final String user,
                                    final HttpServletResponse httpServletResponse) {
        if (user == null || experiment == null) {
            logger.error("Cannot download xml files for user with id null or experiment with id null!");
            throw new IncompleteDataException("Cannot download xml files for user with id null or experiment with id "
                    + "null!");
        }

        int userId = NumberParser.parseNumber(user);
        int experimentId = NumberParser.parseNumber(experiment);

        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            logger.error("Cannot download xml files for user with invalid id " + userId + " or experiment with invalid "
                    + "id " + experimentId + "!");
            throw new IncompleteDataException("Cannot download xml files for user with invalid id " + userId
                    + " or experiment with invalid id " + experimentId + "!");
        }

        try {
            List<BlockEventXMLProjection> xml = eventService.getXMLForUser(userId, experimentId);
            ZipOutputStream zos = getZipOutputStream(httpServletResponse, userId, experimentId, "xml");

            for (BlockEventXMLProjection projection : xml) {
                ZipEntry entry = new ZipEntry("xml" + projection.getId() + ".xml");
                entry.setSize(projection.getXml().length());
                zos.putNextEntry(entry);
                zos.write(projection.getXml().getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }

            zos.finish();
        } catch (IOException e) {
            logger.error("Could not download xml files due to IOException!", e);
            throw new RuntimeException("Could not download xml files due to IOException!");
        }
    }

    /**
     * Retrieves all the json strings that were saved for the given user during the given experiment and makes them
     * available for download in a zip file. If the ids are invalid an {@link IncompleteDataException} is thrown
     * instead. If an {@link IOException} occurs, a {@link RuntimeException} is thrown.
     *
     * @param experiment The experiment id to search for.
     * @param user The user id to search for.
     * @param httpServletResponse The servlet response returning the files.
     */
    @GetMapping("/jsons")
    @Secured(Constants.ROLE_ADMIN)
    public void downloadAllJsonFiles(@RequestParam(EXPERIMENT) final String experiment,
                                     @RequestParam(USER) final String user,
                                     final HttpServletResponse httpServletResponse) {
        if (user == null || experiment == null) {
            logger.error("Cannot download json files for user with id null or experiment with id null!");
            throw new IncompleteDataException("Cannot download json files for user with id null or experiment with id "
                    + "null!");
        }

        int userId = NumberParser.parseNumber(user);
        int experimentId = NumberParser.parseNumber(experiment);

        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            logger.error("Cannot download json files for user with invalid id " + userId + " or experiment with "
                    + "invalid id " + experimentId + "!");
            throw new IncompleteDataException("Cannot download json files for user with invalid id " + userId
                    + " or experiment with invalid id " + experimentId + "!");
        }

        try {
            List<BlockEventJSONProjection> json = eventService.getJsonForUser(userId, experimentId);
            ZipOutputStream zos = getZipOutputStream(httpServletResponse, userId, experimentId, "json");
            writeCSVData(zos, json, Optional.empty(), false);

            for (BlockEventJSONProjection projection : json) {
                ZipEntry entry = new ZipEntry("json" + projection.getId() + ".json");
                entry.setSize(projection.getCode().length());
                zos.putNextEntry(entry);
                zos.write(projection.getCode().getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }

            zos.finish();
        } catch (IOException e) {
            logger.error("Could not download json files due to IOException!", e);
            throw new RuntimeException("Could not download json files due to IOException!");
        }
    }

    /**
     * Loads a list of {@link BlockEventProjection}s for the given page number, user and experiment from the
     * database. If the parameters are invalid an {@link IncompleteDataException} is thrown instead.
     *
     * @param experiment The experiment id to search for.
     * @param user The user id to search for.
     * @param page The current page number.
     * @return The list of block event projections.
     */
    @GetMapping("/codes")
    @Secured(Constants.ROLE_ADMIN)
    @ResponseBody
    public List<BlockEventProjection> getCodes(@RequestParam(EXPERIMENT) final String experiment,
                                               @RequestParam(USER) final String user,
                                               @RequestParam("page") final String page) {
        if (user == null || experiment == null || page == null) {
            logger.error("Cannot get codes for user with id null or experiment with id null or page null!");
            throw new IncompleteDataException("Cannot get codes for user with id null or experiment with id null or "
                    + "page null!");
        }

        int userId = NumberParser.parseNumber(user);
        int experimentId = NumberParser.parseNumber(experiment);
        int currentPage = NumberParser.parseNumber(page);

        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            logger.error("Cannot get codes for user with invalid id " + userId + " or experiment with invalid id "
                    + experimentId + "or invalid page number" + page + "!");
            throw new IncompleteDataException("Cannot get codes for user with invalid id " + userId
                    + " or experiment with invalid id " + experimentId + "or invalid page number" + page + "!");
        }

        if (currentPage < 0) {
            logger.error("Cannot get codes for invalid page number " + currentPage + "!");
            throw new IncompleteDataException("Cannot get codes for invalid page number " + currentPage + "!");
        }

        return eventService.getCodesForUser(userId, experimentId, PageRequest.of(currentPage,
                Constants.PAGE_SIZE)).getContent();
    }

    /**
     * Generates sb3 files for the desired json codes saved for the given user during the given experiment and makes
     * them available for download in a zip file. The json files loaded from the database are filtered according to the
     * specified step parameter, or the specified start, end and include parameters, if present. Every json code is put
     * in a zip file as a project.json file together with all costumes and sounds present in the experiment project file
     * as well as all files saved for the user during the experiment that were not saved as zip files, meaning they are
     * not resources that can be loaded from the Scratch library. The resulting sb3 zip file is then written into
     * another zip file made available for download containing all the created sb3 files. If the passed ids are invalid
     * an {@link IncompleteDataException} is thrown instead. If an {@link IOException} occurs, a
     * {@link RuntimeException} is thrown.
     *
     * @param experiment The experiment id to search for.
     * @param user The user id to search for.
     * @param step The step interval in minutes.
     * @param start The start of the interval in which all json files should be downloaded.
     * @param end The end of the interval in which all json files should be downloaded.
     * @param include Whether the final project should be included.
     * @param httpServletResponse The servlet response returning the files.
     */
    @GetMapping("/sb3s")
    @Secured(Constants.ROLE_ADMIN)
    public void downloadSb3Files(@RequestParam(EXPERIMENT) final String experiment,
                                 @RequestParam(USER) final String user,
                                 @RequestParam(value = "step", required = false) final String step,
                                 @RequestParam(value = "start", required = false) final String start,
                                 @RequestParam(value = "end", required = false) final String end,
                                 @RequestParam(value = "include", required = false) final String include,
                                 final HttpServletResponse httpServletResponse) {
        if (experiment == null || user == null) {
            logger.error("Cannot generate zip file with experiment or user null!");
            throw new IncompleteDataException("Cannot generate zip file with experiment or user null!");
        } else if ((start != null || end != null || include != null)
                && (start == null || end == null || include == null)) {
            logger.error("Cannot generate zip file in a set interval if not all of the needed parameters start, end "
                    + "and include are specified!");
            throw new IncompleteDataException("Cannot generate zip file in a set interval if not all of the needed "
                    + "parameters start, end and include are specified!");
        } else if (start != null && step != null) {
            logger.error("Cannot generate zip file if both step and start, end and include parameters are specified!");
            throw new IncompleteDataException("Cannot generate zip file if both step and start, end and include "
                    + "parameters are specified!");
        }

        int userId = NumberParser.parseNumber(user);
        int experimentId = NumberParser.parseNumber(experiment);
        int steps = 0;
        int startPosition = 0;
        int endPosition = 0;
        boolean includeFinalProject = true;

        if (step != null) {
            steps = NumberParser.parseNumber(step);

            if (steps < 1) {
                logger.error("Cannot generate zip file for invalid step interval " + step + "!");
                throw new IncompleteDataException("Cannot generate zip file for invalid step interval " + step + "!");
            }
        } else if (start != null) {
            startPosition = NumberParser.parseNumber(start);
            endPosition = NumberParser.parseNumber(end);
            includeFinalProject = !include.equals("false");

            if (startPosition < 1 || endPosition < 1) {
                logger.error("Cannot generate zip file for invalid start position " + start
                        + " or invalid end position " + end + "!");
                throw new IncompleteDataException("Cannot generate zip file for invalid start position " + start
                        + " or invalid end position " + end + "!");
            } else if (startPosition > endPosition) {
                logger.error("Cannot generate zip file for start position " + start + " bigger than end position "
                        + end + "!");
                throw new IncompleteDataException("Cannot generate zip file for start position " + start
                        + " bigger than end position " + end + "!");
            }
        }

        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            logger.error("Cannot generate zip file for user with invalid id " + user + " or experiment with invalid "
                    + "id " + experiment + "!");
            throw new IncompleteDataException("Cannot generate zip file for user with invalid id " + user
                    + " or experiment with invalid id " + experiment + "!");
        }

        ExperimentProjection projection = experimentService.getSb3File(experimentId);
        List<FileDTO> fileDTOS = fileService.getFileDTOs(userId, experimentId);
        List<BlockEventJSONProjection> jsons = eventService.getJsonForUser(userId, experimentId);
        Optional<Sb3ZipDTO> finalProject = fileService.findFinalProject(userId, experimentId);

        if (steps > 0) {
            Timestamp lastTimestamp = finalProject.isPresent() ? Timestamp.valueOf(finalProject.get().getDate())
                    : jsons.get(jsons.size() - 1).getDate();
            jsons = filterProjectionsByStep(jsons, steps, lastTimestamp);
        } else if (startPosition > 0) {
            if (endPosition > jsons.size()) {
                logger.error("Cannot generate zip file with invalid end position " + endPosition + " bigger than the "
                        + "amount of saved json strings " + jsons.size() + "!");
                throw new IncompleteDataException("Cannot generate zip file with invalid end position " + endPosition
                        + " bigger than the amount of saved json strings " + jsons.size() + "!");
            }

            jsons = jsons.subList(startPosition - 1, endPosition);
        }

        try {
            ZipOutputStream zos = getZipOutputStream(httpServletResponse, userId, experimentId, "zip");
            writeCSVData(zos, jsons, finalProject, includeFinalProject);

            for (int i = 0; i < jsons.size(); i++) {
                BlockEventJSONProjection json = jsons.get(i);
                ByteArrayOutputStream innerZip = new ByteArrayOutputStream();
                ZipOutputStream innerZos = new ZipOutputStream(new BufferedOutputStream(innerZip));

                if (projection.getProject() != null) {
                    writeInitialProjectData(innerZos, projection.getProject());
                }

                for (FileDTO fileDTO : fileDTOS) {
                    writeFileDataNoZips(innerZos, fileDTO);
                }

                byte[] code = json.getCode().getBytes(StandardCharsets.UTF_8);
                writeJsonData(innerZos, code);

                innerZos.flush();
                innerZos.close();
                ZipEntry createdZip = new ZipEntry("project_" + json.getId() + "_" + i + ".sb3");
                zos.putNextEntry(createdZip);
                zos.write(innerZip.toByteArray());
                zos.closeEntry();
            }

            if (finalProject.isPresent() && includeFinalProject) {
                writeFinalProjectData(zos, finalProject.get());
            }

            zos.finish();
        } catch (IOException e) {
            logger.error("Could not generate zip file due to IOException!", e);
            throw new RuntimeException("Could not generate zip file due to IOException!");
        }
    }

    /**
     * Returns a {@link ZipOutputStream} from the given {@link HttpServletResponse} output stream and sets the content
     * type, header and status of the servlet response accordingly.
     *
     * @param httpServletResponse The servlet response.
     * @param userId The user id to use to name the zip file.
     * @param experimentId The experiment id to use to name the zip file.
     * @param filetype The filetype to use to name the zip file.
     * @return The zip output stream.
     */
    private ZipOutputStream getZipOutputStream(final HttpServletResponse httpServletResponse, final int userId,
                                               final int experimentId, final String filetype) throws IOException {
        String fileEnding = filetype.equals("sb3") ? ".sb3" : ".zip";
        httpServletResponse.setContentType("application/zip");
        httpServletResponse.setHeader("Content-Disposition", "attachment;filename=" + filetype + "_user" + userId
                + "_experiment" + experimentId + fileEnding);
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
        return new ZipOutputStream(httpServletResponse.getOutputStream());
    }

    /**
     * Creates a zip file entry for a CSV file containing information on the filtered {@link BlockEventJSONProjection}s
     * for which a sb3 file will be generated. For each projection, its id, the date at which it was created and the
     * event that triggered it are written to the csv file. If the final sb3 project is present, and it is to be
     * included, its information is added as well.
     *
     * @param zos The {@link ZipOutputStream} returning the generated file to the user.
     * @param projections The filtered projections.
     * @param finalProject The {@link Optional} {@link Sb3ZipDTO} containing the information on the final project.
     * @param includeFinalProject Boolean indicating whether the final project data should be added.
     * @throws IOException if the file content could not be written correctly.
     */
    private void writeCSVData(final ZipOutputStream zos, final List<BlockEventJSONProjection> projections,
                              final Optional<Sb3ZipDTO> finalProject, final boolean includeFinalProject)
            throws IOException {
        List<String[]> data = new ArrayList<>();
        String[] header = {"id", "date", "event"};
        data.add(header);
        projections.forEach(projection -> data.add(new String[]{String.valueOf(projection.getId()),
                String.valueOf(projection.getDate()), projection.getEvent()}));

        if (finalProject.isPresent() && includeFinalProject) {
            data.add(new String[]{"final project", String.valueOf(finalProject.get().getDate()), "FINISH"});
        }

        ZipEntry entry = new ZipEntry("events.csv");
        zos.putNextEntry(entry);
        CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(zos));
        csvWriter.writeAll(data);
        csvWriter.flush();
        zos.closeEntry();
    }

    /**
     * Writes the content of the given byte[] representing the initial sb3 project loaded on experiment start to the
     * given {@link ZipOutputStream}.
     *
     * @param zos The {@link ZipOutputStream} returning the generated file to the user.
     * @param project The initial sb3 project.
     * @throws IOException if the file content could not be written correctly.
     */
    private void writeInitialProjectData(final ZipOutputStream zos, final byte[] project) throws IOException {
        InputStream file = new ByteArrayInputStream(project);
        ZipInputStream zin = new ZipInputStream(file);
        ZipEntry ze;

        while ((ze = zin.getNextEntry()) != null) {
            if (!ze.getName().equals("project.json")) {
                zos.putNextEntry(ze);
                int current;
                while ((current = zin.read()) >= 0) {
                    zos.write(current);
                }
                zos.closeEntry();
            }
        }

        zin.close();
        file.close();
    }

    /**
     * Writes the content of the given {@link FileDTO} representing a file the participant uploaded during the
     * experiment to the given {@link ZipOutputStream} if the file was not saved in a zip format.
     *
     * @param zos The {@link ZipOutputStream} returning the generated file to the user.
     * @param fileDTO The {@link FileDTO} containing the file data.
     * @throws IOException if the file content could not be written correctly.
     */
    private void writeFileDataNoZips(final ZipOutputStream zos, final FileDTO fileDTO) throws IOException {
        if (!fileDTO.getName().endsWith("zip")) {
            ZipEntry entry = new ZipEntry(fileDTO.getId() + fileDTO.getName());
            entry.setSize(fileDTO.getContent().length);
            zos.putNextEntry(entry);
            zos.write(fileDTO.getContent());
            zos.closeEntry();
        }
    }

    /**
     * Writes the content of the given {@link Sb3ZipDTO} representing the final project of a participant during the
     * experiment to the given {@link ZipOutputStream}.
     *
     * @param zos The {@link ZipOutputStream} returning the generated file to the user.
     * @param sb3ZipDTO The {@link Sb3ZipDTO} containing the file data.
     * @throws IOException if the file content could not be written correctly.
     */
    private void writeFinalProjectData(final ZipOutputStream zos, final Sb3ZipDTO sb3ZipDTO) throws IOException {
        ZipEntry lastEntry = new ZipEntry("final_project.sb3");
        lastEntry.setSize(sb3ZipDTO.getContent().length);
        zos.putNextEntry(lastEntry);
        zos.write(sb3ZipDTO.getContent());
        zos.closeEntry();
    }

    /**
     * Writes the content of the given json data to the given {@link ZipOutputStream}.
     *
     * @param zos The {@link ZipOutputStream} returning the generated file to the user.
     * @param code The byte[] containing the json data.
     * @throws IOException if the content could not be written correctly.
     */
    private void writeJsonData(final ZipOutputStream zos, final byte[] code) throws IOException {
        ZipEntry entry = new ZipEntry("project.json");
        entry.setSize(code.length);
        zos.putNextEntry(entry);
        zos.write(code);
        zos.closeEntry();
    }

    /**
     * Filters the passed {@link BlockEventJSONProjection}s according to the passed steps in minutes. Starting with the
     * first json, steps minutes are added to its timestamp. The remaining json files are traversed until one with a
     * timestamp after the calculated one is found. Its predecessor is added to filtered list and the calculated time
     * is increased by one more step. The same json file might be added multiple times if the next calculated timestamp
     * is more than one time step apart from the timestamp of the next json file. To avoid adding the same file too many
     * times, the process skips time breaks longer than a certain threshold.
     *
     * @param projections A list of {@link BlockEventJSONProjection} containing the relevant block event data.
     * @param step The time steps the files should be apart in minutes.
     * @param lastProjectStamp The timestamp of the last file the final project state saved.
     * @return The filtered {@link BlockEventJSONProjection}s.
     */
    private List<BlockEventJSONProjection> filterProjectionsByStep(final List<BlockEventJSONProjection> projections,
                                                                   final int step, final Timestamp lastProjectStamp) {
        List<BlockEventJSONProjection> filteredProjections = new ArrayList<>();
        filteredProjections.add(projections.get(0));

        if (projections.size() > 1) {
            long stepsInMillis = (long) step * Constants.MINUTES_TO_MILLIS;
            filteredProjections.addAll(addProjections(projections, stepsInMillis, lastProjectStamp));
        }

        return filteredProjections;
    }

    /**
     * Adds the passed {@link BlockEventJSONProjection}s to a list depending on their timestamp. If the timestamp of the
     * current file is after that of the current time, it is added to the list (possibly more than once), unless the
     * timestamp is after the maximum allowed time break. In that case, the project is only added once. Finally, the
     * last project file is added and the list returned.
     *
     * @param projections A list of {@link BlockEventJSONProjection} containing the relevant block event data.
     * @param stepsInMillis The regular desired time break between two projections.
     * @param lastProjectStamp The {@link Timestamp} of the last project.
     * @return The list of filtered projections.
     */
    private List<BlockEventJSONProjection> addProjections(final List<BlockEventJSONProjection> projections,
                                                          final long stepsInMillis, final Timestamp lastProjectStamp) {
        List<BlockEventJSONProjection> filteredProjections = new ArrayList<>();
        long currentTime = projections.get(0).getDate().getTime() + stepsInMillis;
        long maxAllowedTime = projections.get(0).getDate().getTime() + Constants.MAX_ALLOWED_BREAK_FACTOR
                * stepsInMillis;

        for (int i = 1; i < projections.size(); i++) {
            BlockEventJSONProjection projection = projections.get(i);
            long projectionTime = projection.getDate().getTime();

            if (projectionTime < maxAllowedTime) {
                while (projectionTime >= currentTime) {
                    filteredProjections.add(projections.get(i - 1));
                    currentTime += stepsInMillis;
                    maxAllowedTime += stepsInMillis;
                }
            } else {
                filteredProjections.add(projections.get(i - 1));
                currentTime = projections.get(i - 1).getDate().getTime() + stepsInMillis;
                maxAllowedTime = projections.get(i - 1).getDate().getTime() + Constants.MAX_ALLOWED_BREAK_FACTOR
                        * stepsInMillis;
            }
        }

        addLastProjection(projections, lastProjectStamp, currentTime, maxAllowedTime, stepsInMillis);
        return filteredProjections;
    }

    /**
     * Adds the participant's final sb3 project file to the given {@link BlockEventJSONProjection} list.
     *
     * @param projections The list of projections.
     * @param lastProjectStamp The {@link Timestamp} of the last saved project change.
     * @param currentTime The current time to look at in milliseconds.
     * @param maxAllowedTime The maximum allowed break time signifying that the participant has been inactive.
     * @param stepsInMillis The desired step size in milliseconds.
     */
    private void addLastProjection(final List<BlockEventJSONProjection> projections, final Timestamp lastProjectStamp,
                                   final long currentTime, final long maxAllowedTime, final long stepsInMillis) {
        int lastProjectionPosition = projections.size() - 1;
        int compare = lastProjectStamp.compareTo(projections.get(lastProjectionPosition).getDate());

        if (compare <= 0) {
            projections.add(projections.get(lastProjectionPosition));
        }

        long projectTime = lastProjectStamp.getTime();

        if (projectTime < maxAllowedTime) {
            while (projectTime >= currentTime) {
                projections.add(projections.get(lastProjectionPosition));
                projectTime -= stepsInMillis;
            }
        } else {
            projections.add(projections.get(lastProjectionPosition));
        }
    }

}
