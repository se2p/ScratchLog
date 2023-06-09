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

import com.opencsv.CSVWriter;
import fim.unipassau.de.scratchLog.application.exception.IncompleteDataException;
import fim.unipassau.de.scratchLog.application.exception.NotFoundException;
import fim.unipassau.de.scratchLog.application.service.EventService;
import fim.unipassau.de.scratchLog.application.service.ExperimentService;
import fim.unipassau.de.scratchLog.application.service.FileService;
import fim.unipassau.de.scratchLog.application.service.UserService;
import fim.unipassau.de.scratchLog.persistence.projection.BlockEventJSONProjection;
import fim.unipassau.de.scratchLog.persistence.projection.BlockEventProjection;
import fim.unipassau.de.scratchLog.persistence.projection.BlockEventXMLProjection;
import fim.unipassau.de.scratchLog.persistence.projection.ExperimentProjection;
import fim.unipassau.de.scratchLog.persistence.projection.FileProjection;
import fim.unipassau.de.scratchLog.util.Constants;
import fim.unipassau.de.scratchLog.util.NumberParser;
import fim.unipassau.de.scratchLog.web.dto.CodesDataDTO;
import fim.unipassau.de.scratchLog.web.dto.EventCountDTO;
import fim.unipassau.de.scratchLog.web.dto.FileDTO;
import fim.unipassau.de.scratchLog.web.dto.Sb3ZipDTO;
import jakarta.servlet.http.HttpServletResponse;
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

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(ResultController.class);

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
            LOGGER.error("Cannot return result page for user with id null or experiment with id null!");
            return new ModelAndView(Constants.ERROR);
        }

        int userId = NumberParser.parseNumber(user);
        int experimentId = NumberParser.parseNumber(experiment);

        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            LOGGER.error("Cannot return result page for user with invalid id " + userId + " or experiment with invalid "
                    + "id " + experimentId + "!");
            return new ModelAndView(Constants.ERROR);
        }
        if (!userService.existsParticipant(userId, experimentId)) {
            LOGGER.error("Could not find participant entry for user with id " + userId + " for experiment with id "
                    + experimentId);
            return new ModelAndView(Constants.ERROR);
        }

        try {
            List<EventCountDTO> blockEvents = eventService.getBlockEventCounts(userId, experimentId);
            List<EventCountDTO> clickEvents = eventService.getClickEventCounts(userId, experimentId);
            List<EventCountDTO> resourceEvents = eventService.getResourceEventCounts(userId, experimentId);
            List<FileProjection> files = fileService.getFiles(userId, experimentId);
            List<Integer> zipIds = fileService.getZipIds(userId, experimentId);
            CodesDataDTO codesDataDTO = eventService.getCodesData(userId, experimentId);

            model.addAttribute("codeCount", Math.max(codesDataDTO.getCount(), 0));
            model.addAttribute("pageSize", Constants.PAGE_SIZE);
            model.addAttribute("blockEvents", blockEvents);
            model.addAttribute("clickEvents", clickEvents);
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
            LOGGER.error("Cannot download file with invalid id null!");
            return Constants.ERROR;
        }

        int fileId = NumberParser.parseNumber(id);

        if (fileId < Constants.MIN_ID) {
            LOGGER.error("Cannot download file with invalid id " + fileId + "!");
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
     * file as well as all files saved for the user during the experiment.
     *
     * @param experiment The id of the experiment.
     * @param user The id of the user.
     * @param json The block event id to search for.
     * @param httpServletResponse The servlet response.
     * @throws IncompleteDataException if the passed user, experiment or json ids are invalid.
     * @throws RuntimeException if an {@link IOException} occurs during the sb3 file creation.
     */
    @GetMapping("/generate")
    @Secured(Constants.ROLE_ADMIN)
    public void generateZipFile(@RequestParam(EXPERIMENT) final String experiment,
                                @RequestParam(USER) final String user, @RequestParam("json") final String json,
                                final HttpServletResponse httpServletResponse) {
        if (json == null || experiment == null || user == null) {
            throw new IncompleteDataException("Cannot generate zip file with JSON, experiment or user null!");
        }

        int userId = NumberParser.parseNumber(user);
        int experimentId = NumberParser.parseNumber(experiment);
        int jsonId = NumberParser.parseNumber(json);

        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID || jsonId < Constants.MIN_ID) {
            throw new IncompleteDataException("Cannot generate zip file for user with invalid id " + user
                    + " or experiment with invalid id " + experiment + "or json with invalid id " + json + "!");
        }

        ExperimentProjection projection = experimentService.getSb3File(experimentId);
        List<FileDTO> fileDTOS = fileService.getFileDTOs(userId, experimentId);
        byte[] code = eventService.findJsonById(jsonId).getBytes(StandardCharsets.UTF_8);

        try (ZipOutputStream zos = getZipOutputStream(httpServletResponse, userId, experimentId, "sb3")) {
            Set<String> fileNames = new HashSet<>();

            if (projection.getProject() != null) {
                writeInitialProjectData(zos, projection.getProject());
            }

            for (FileDTO fileDTO : fileDTOS) {
                writeFileData(zos, fileDTO, fileNames);
            }

            writeJsonData(zos, code);
            zos.finish();
        } catch (IOException e) {
            throw new RuntimeException("Could not generate zip file due to IOException!", e);
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
            LOGGER.error("Cannot download zip file with invalid id null!");
            return Constants.ERROR;
        }

        int zipId = NumberParser.parseNumber(id);

        if (zipId < Constants.MIN_ID) {
            LOGGER.error("Cannot download zip file with invalid id " + zipId + "!");
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
     * download in a zip file.
     *
     * @param experiment The experiment id to search for.
     * @param user The user id to search for.
     * @param httpServletResponse The servlet response returning the files.
     * @throws IncompleteDataException if the passed user or experiment ids are invalid.
     * @throws RuntimeException if an {@link IOException} occurs.
     */
    @GetMapping("/zips")
    @Secured(Constants.ROLE_ADMIN)
    public void downloadAllZips(@RequestParam(EXPERIMENT) final String experiment,
                                @RequestParam(USER) final String user,
                                final HttpServletResponse httpServletResponse) {
        if (user == null || experiment == null) {
            throw new IncompleteDataException("Cannot download zip files for user with id null or experiment with id "
                    + "null!");
        }

        int userId = NumberParser.parseNumber(user);
        int experimentId = NumberParser.parseNumber(experiment);

        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            throw new IncompleteDataException("Cannot download zip files for user with invalid id " + userId
                    + " or experiment with invalid id " + experimentId + "!");
        }

        try (ZipOutputStream zos = getZipOutputStream(httpServletResponse, userId, experimentId, "projects")) {
            List<Sb3ZipDTO> sb3ZipDTOS = fileService.getZipFiles(userId, experimentId);

            for (Sb3ZipDTO sb3ZipDTO : sb3ZipDTOS) {
                ZipEntry entry = new ZipEntry(sb3ZipDTO.getId() + sb3ZipDTO.getName());
                entry.setSize(sb3ZipDTO.getContent().length);
                zos.putNextEntry(entry);
                zos.write(sb3ZipDTO.getContent());
                zos.closeEntry();
            }

            zos.finish();
        } catch (IOException e) {
            throw new RuntimeException("Could not download zip files due to IOException!", e);
        }
    }

    /**
     * Retrieves all the xml codes that were saved for the given user during the given experiment and makes them
     * available for download in a zip file.
     *
     * @param experiment The experiment id to search for.
     * @param user The user id to search for.
     * @param httpServletResponse The servlet response returning the files.
     * @throws IncompleteDataException if the passed user or experiment ids are invalid.
     * @throws RuntimeException if an {@link IOException} occurs.
     */
    @GetMapping("/xmls")
    @Secured(Constants.ROLE_ADMIN)
    public void downloadAllXmlFiles(@RequestParam(EXPERIMENT) final String experiment,
                                    @RequestParam(USER) final String user,
                                    final HttpServletResponse httpServletResponse) {
        if (user == null || experiment == null) {
            throw new IncompleteDataException("Cannot download xml files for user with id null or experiment with id "
                    + "null!");
        }

        int userId = NumberParser.parseNumber(user);
        int experimentId = NumberParser.parseNumber(experiment);

        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            throw new IncompleteDataException("Cannot download xml files for user with invalid id " + userId
                    + " or experiment with invalid id " + experimentId + "!");
        }

        try (ZipOutputStream zos = getZipOutputStream(httpServletResponse, userId, experimentId, "xml")) {
            List<BlockEventXMLProjection> xml = eventService.getXMLForUser(userId, experimentId);

            for (BlockEventXMLProjection projection : xml) {
                ZipEntry entry = new ZipEntry("xml" + projection.getId() + ".xml");
                entry.setSize(projection.getXml().length());
                zos.putNextEntry(entry);
                zos.write(projection.getXml().getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }

            zos.finish();
        } catch (IOException e) {
            throw new RuntimeException("Could not download xml files due to IOException!", e);
        }
    }

    /**
     * Retrieves all the json strings that were saved for the given user during the given experiment and makes them
     * available for download in a zip file.
     *
     * @param experiment The experiment id to search for.
     * @param user The user id to search for.
     * @param httpServletResponse The servlet response returning the files.
     * @throws IncompleteDataException if the passed user or experiment ids are invalid.
     * @throws RuntimeException if an {@link IOException} occurs.
     */
    @GetMapping("/jsons")
    @Secured(Constants.ROLE_ADMIN)
    public void downloadAllJsonFiles(@RequestParam(EXPERIMENT) final String experiment,
                                     @RequestParam(USER) final String user,
                                     final HttpServletResponse httpServletResponse) {
        if (user == null || experiment == null) {
            throw new IncompleteDataException("Cannot download json files for user with id null or experiment with id "
                    + "null!");
        }

        int userId = NumberParser.parseNumber(user);
        int experimentId = NumberParser.parseNumber(experiment);

        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            throw new IncompleteDataException("Cannot download json files for user with invalid id " + userId
                    + " or experiment with invalid id " + experimentId + "!");
        }

        try (ZipOutputStream zos = getZipOutputStream(httpServletResponse, userId, experimentId, "json")) {
            List<BlockEventJSONProjection> json = eventService.getJsonForUser(userId, experimentId);
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
            throw new RuntimeException("Could not download json files due to IOException!", e);
        }
    }

    /**
     * Loads a list of {@link BlockEventProjection}s for the given page number, user and experiment from the
     * database.
     *
     * @param experiment The experiment id to search for.
     * @param user The user id to search for.
     * @param page The current page number.
     * @return The list of block event projections.
     * @throws IncompleteDataException if the passed user or experiment id or the page are invalid.
     */
    @GetMapping("/codes")
    @Secured(Constants.ROLE_ADMIN)
    @ResponseBody
    public List<BlockEventProjection> getCodes(@RequestParam(EXPERIMENT) final String experiment,
                                               @RequestParam(USER) final String user,
                                               @RequestParam("page") final String page) {
        if (user == null || experiment == null || page == null) {
            throw new IncompleteDataException("Cannot get codes for user with id null or experiment with id null or "
                    + "page null!");
        }

        int userId = NumberParser.parseNumber(user);
        int experimentId = NumberParser.parseNumber(experiment);
        int currentPage = NumberParser.parseNumber(page);

        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            throw new IncompleteDataException("Cannot get codes for user with invalid id " + userId
                    + " or experiment with invalid id " + experimentId + "or invalid page number" + page + "!");
        }

        if (currentPage < 0) {
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
     * another zip file made available for download containing all the created sb3 files.
     *
     * @param experiment The experiment id to search for.
     * @param user The user id to search for.
     * @param step The step interval in minutes.
     * @param start The start of the interval in which all json files should be downloaded.
     * @param end The end of the interval in which all json files should be downloaded.
     * @param include Whether the final project should be included.
     * @param httpServletResponse The servlet response returning the files.
     * @throws IncompleteDataException if any of the passed parameters are invalid.
     * @throws RuntimeException if an {@link IOException} occurs.
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
        checkDownloadParameters(experiment, user, step, start, end, include);
        int userId = NumberParser.parseNumber(user);
        int experimentId = NumberParser.parseNumber(experiment);
        int steps = 0;
        int startPosition = 0;
        int endPosition = 0;
        boolean includeFinalProject = true;

        if (step != null) {
            steps = getNumberFromString(step, "step interval");
        } else if (start != null) {
            startPosition = getNumberFromString(start, "start position");
            endPosition = getNumberFromString(end, "end position");
            includeFinalProject = !include.equals("false");

            if (startPosition > endPosition) {
                throw new IncompleteDataException("Cannot generate zip file for start position " + start
                        + " bigger than end position " + end + "!");
            }
        }

        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            throw new IncompleteDataException("Cannot generate zip file for user with invalid id " + user
                    + " or experiment with invalid id " + experiment + "!");
        }

        ExperimentProjection projection = experimentService.getSb3File(experimentId);
        List<FileDTO> fileDTOS = fileService.getFileDTOs(userId, experimentId);
        Optional<Sb3ZipDTO> finalProject = fileService.findFinalProject(userId, experimentId);
        List<BlockEventJSONProjection> jsons = filterJsons(steps, startPosition, endPosition, userId, experimentId,
                finalProject);

        try (ZipOutputStream zos = getZipOutputStream(httpServletResponse, userId, experimentId, "zip")) {
            writeCSVData(zos, jsons, finalProject, includeFinalProject);

            for (int i = 0; i < jsons.size(); i++) {
                createSb3File(jsons.get(i), zos, i, projection, fileDTOS);
            }

            if (finalProject.isPresent() && includeFinalProject) {
                writeFinalProjectData(zos, finalProject.get());
            }

            zos.finish();
        } catch (IOException e) {
            throw new RuntimeException("Could not generate zip file due to IOException!", e);
        }
    }

    /**
     * Checks, whether the parameters for downloading sb3 files are valid. For the parameters to be valid, both the
     * experiment and user ids have to be specified. If sb3 files in a certain range are to be downloaded, the start,
     * end and include parameters need to be present. If sb3 files are downloaded in minute intervals, the start
     * parameter cannot be specified.
     *
     * @param experiment The id of the experiment.
     * @param user The id of the user.
     * @param step The step interval in minutes.
     * @param start The start of the interval in which all json files should be downloaded.
     * @param end The end of the interval in which all json files should be downloaded.
     * @param include Whether the final project should be included.
     * @throws IncompleteDataException if the required parameters are not specified.
     */
    private void checkDownloadParameters(final String experiment, final String user, final String step,
                                         final String start, final String end, final String include) {
        if (experiment == null || user == null) {
            throw new IncompleteDataException("Cannot generate zip file with experiment or user null!");
        } else if ((start != null || end != null || include != null)
                && (start == null || end == null || include == null)) {
            throw new IncompleteDataException("Cannot generate zip file in a set interval if not all of the needed "
                    + "parameters start, end and include are specified!");
        } else if (start != null && step != null) {
            throw new IncompleteDataException("Cannot generate zip file if both step and start, end and include "
                    + "parameters are specified!");
        }
    }

    /**
     * Parses the given string to a number and checks if it is larger than one.
     *
     * @param number The string representation of the number.
     * @param parameterName The name of the parameter that is checked.
     * @return The parsed valid number.
     * @throws IllegalArgumentException if the passed number is invalid.
     */
    private int getNumberFromString(final String number, final String parameterName) {
        int num = NumberParser.parseNumber(number);

        if (num < 1) {
            throw new IncompleteDataException("Cannot generate zip file for invalid " + parameterName + " " + num
                    + "!");
        }

        return num;
    }

    /**
     * Filters the json code saved for the given user during the given experiment according to the specified parameters.
     * If the code is to be filtered in minute intervals, the jsons are filtered according to their generation time. If
     * the code within a certain range is to be returned, the jsons are filtered according to the specified start and
     * end positions.
     *
     * @param steps The step interval in minutes.
     * @param startPosition The start of the interval in which all json files should be downloaded.
     * @param endPosition The end of the interval in which all json files should be downloaded.
     * @param userId The id of the user.
     * @param experimentId The id of the experiment.
     * @param finalProject The final project saved for the user, if any.
     * @return The filtered code list.
     * @throws IllegalArgumentException if the given end position is bigger than the number of codes.
     */
    private List<BlockEventJSONProjection> filterJsons(final int steps, final int startPosition, final int endPosition,
                                                       final int userId, final int experimentId,
                                                       final Optional<Sb3ZipDTO> finalProject) {
        List<BlockEventJSONProjection> jsons = eventService.getJsonForUser(userId, experimentId);

        if (steps > 0) {
            LocalDateTime lastDateTime = finalProject.isPresent() ? finalProject.get().getDate()
                    : jsons.get(jsons.size() - 1).getDate();
            return filterProjectionsByStep(jsons, steps, lastDateTime);
        } else if (startPosition > 0) {
            if (endPosition > jsons.size()) {
                throw new IncompleteDataException("Cannot generate zip file with invalid end position " + endPosition
                        + " bigger than the amount of saved json strings " + jsons.size() + "!");
            }

            return jsons.subList(startPosition - 1, endPosition);
        }

        return jsons;
    }

    /**
     * Creates a sb3 file saved as a zip entry for the given json code. Beside the json itself, all saved files and the
     * initial project data are included in the zip file.
     *
     * @param json The json code to be used.
     * @param zos The {@link ZipOutputStream} in which the zip file should be written.
     * @param counter The file counter.
     * @param projection The initial experiment project data.
     * @param fileDTOS The saved files.
     * @throws IOException if the data could not be written correctly.
     */
    private void createSb3File(final BlockEventJSONProjection json, final ZipOutputStream zos, final int counter,
                               final ExperimentProjection projection, final List<FileDTO> fileDTOS) throws IOException {
        ByteArrayOutputStream innerZip = new ByteArrayOutputStream();

        try (ZipOutputStream innerZos = new ZipOutputStream(new BufferedOutputStream(innerZip))) {
            Set<String> fileNames = new HashSet<>();

            if (projection.getProject() != null) {
                writeInitialProjectData(innerZos, projection.getProject());
            }

            for (FileDTO fileDTO : fileDTOS) {
                writeFileData(innerZos, fileDTO, fileNames);
            }

            byte[] code = json.getCode().getBytes(StandardCharsets.UTF_8);
            writeJsonData(innerZos, code);

            innerZos.flush();
        }

        ZipEntry createdZip = new ZipEntry("project_" + json.getId() + "_" + counter + ".sb3");
        zos.putNextEntry(createdZip);
        zos.write(innerZip.toByteArray());
        zos.closeEntry();
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
        try (InputStream file = new ByteArrayInputStream(project); ZipInputStream zin = new ZipInputStream(file)) {
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
        }
    }

    /**
     * Writes the content of the given {@link FileDTO} representing a file the participant uploaded during the
     * experiment to the given {@link ZipOutputStream} if the file was not saved in a zip format.
     *
     * @param zos The {@link ZipOutputStream} returning the generated file to the user.
     * @param fileDTO The {@link FileDTO} containing the file data.
     * @param names The names of the files already added as entries.
     * @throws IOException if the file content could not be written correctly.
     */
    private void writeFileData(final ZipOutputStream zos, final FileDTO fileDTO,
                               final Set<String> names) throws IOException {
        if (!fileDTO.getName().endsWith("zip") && !names.contains(fileDTO.getName())) {
            names.add(fileDTO.getName());
            ZipEntry entry = new ZipEntry(fileDTO.getName());
            entry.setSize(fileDTO.getContent().length);
            zos.putNextEntry(entry);
            zos.write(fileDTO.getContent());
            zos.closeEntry();
        } else {
            try (InputStream file = new ByteArrayInputStream(fileDTO.getContent());
                 ZipInputStream zin = new ZipInputStream(file)) {
                ZipEntry ze = zin.getNextEntry();

                if (ze != null && !names.contains(ze.getName())) {
                    names.add(ze.getName());
                    ZipEntry entry = new ZipEntry(ze.getName());
                    zos.putNextEntry(entry);
                    int current;
                    while ((current = zin.read()) >= 0) {
                        zos.write(current);
                    }
                    zos.closeEntry();
                }
            }
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
     * first json, steps minutes are added to its datetime. The remaining json files are traversed until one with a
     * timestamp after the calculated one is found. Its predecessor is added to filtered list and the calculated time
     * is increased by one more step. The same json file might be added multiple times if the next calculated timestamp
     * is more than one time step apart from the timestamp of the next json file. To avoid adding the same file too many
     * times, the process skips time breaks longer than a certain threshold.
     *
     * @param projections A list of {@link BlockEventJSONProjection} containing the relevant block event data.
     * @param step The time steps the files should be apart in minutes.
     * @param lastDateTime The datetime of the last file the final project state saved.
     * @return The filtered {@link BlockEventJSONProjection}s.
     */
    private List<BlockEventJSONProjection> filterProjectionsByStep(final List<BlockEventJSONProjection> projections,
                                                                   final int step, final LocalDateTime lastDateTime) {
        List<BlockEventJSONProjection> filteredProjections = new ArrayList<>();
        filteredProjections.add(projections.get(0));

        if (projections.size() > 1) {
            filteredProjections.addAll(addProjections(projections, step, lastDateTime));
        }

        return filteredProjections;
    }

    /**
     * Adds the passed {@link BlockEventJSONProjection}s to a list depending on their datetime. If the datetime of the
     * current file is after that of the current time, it is added to the list (possibly more than once), unless the
     * datetime is after the maximum allowed time break. In that case, the project is only added once. Finally, the
     * last project file is added and the list returned.
     *
     * @param projections A list of {@link BlockEventJSONProjection} containing the relevant block event data.
     * @param steps The regular desired time break between two projections.
     * @param lastDateTime The {@link LocalDateTime} of the last project.
     * @return The list of filtered projections.
     */
    private List<BlockEventJSONProjection> addProjections(final List<BlockEventJSONProjection> projections,
                                                          final int steps, final LocalDateTime lastDateTime) {
        List<BlockEventJSONProjection> filteredProjections = new ArrayList<>();
        LocalDateTime currentTime = projections.get(0).getDate();
        LocalDateTime maxTime = currentTime.plusMinutes((long) Constants.MAX_ALLOWED_BREAK_FACTOR * steps);

        for (int i = 1; i < projections.size(); i++) {
            BlockEventJSONProjection projection = projections.get(i);
            LocalDateTime projectionTime = projection.getDate();

            if (projectionTime.isBefore(maxTime)) {
                while (projectionTime.isAfter(currentTime)) {
                    filteredProjections.add(projections.get(i - 1));
                    currentTime = currentTime.plusMinutes(steps);
                    maxTime = maxTime.plusMinutes(steps);
                }
            } else {
                filteredProjections.add(projections.get(i - 1));
                currentTime = projections.get(i - 1).getDate();
                maxTime = currentTime.plusMinutes((long) Constants.MAX_ALLOWED_BREAK_FACTOR * steps);
            }
        }

        addLastProjection(filteredProjections, projections.get(projections.size() - 1), lastDateTime, currentTime,
                maxTime, steps);
        return filteredProjections;
    }

    /**
     * Adds the participant's final sb3 project file to the given {@link BlockEventJSONProjection} list.
     *
     * @param filteredProjections The list of filtered projections.
     * @param lastProjection The last projection to be added.
     * @param lastProjectTime The {@link LocalDateTime} of the last saved project change.
     * @param currentTime The current time to look at.
     * @param maxTime The maximum allowed break time signifying that the participant has been inactive.
     * @param steps The desired step size in minutes.
     */
    private void addLastProjection(final List<BlockEventJSONProjection> filteredProjections,
                                   final BlockEventJSONProjection lastProjection, final LocalDateTime lastProjectTime,
                                   final LocalDateTime currentTime, final LocalDateTime maxTime, final int steps) {
        int compare = lastProjectTime.compareTo(lastProjection.getDate());

        if (compare <= 0) {
            filteredProjections.add(lastProjection);
        }

        LocalDateTime projectTime = lastProjectTime;

        if (projectTime.isBefore(maxTime)) {
            while (projectTime.isAfter(currentTime)) {
                filteredProjections.add(lastProjection);
                projectTime = projectTime.minusMinutes(steps);
            }
        } else {
            filteredProjections.add(lastProjection);
        }
    }

}
