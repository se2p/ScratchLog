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

import de.uni_passau.fim.se2.scratchlog.application.exception.NotFoundException;
import de.uni_passau.fim.se2.scratchlog.application.service.CodeService;
import de.uni_passau.fim.se2.scratchlog.application.service.EventService;
import de.uni_passau.fim.se2.scratchlog.application.service.ExperimentDataService;
import de.uni_passau.fim.se2.scratchlog.application.service.FileService;
import de.uni_passau.fim.se2.scratchlog.application.service.PageService;
import de.uni_passau.fim.se2.scratchlog.application.service.UserService;
import de.uni_passau.fim.se2.scratchlog.application.service.ZipExportService;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.BlockEventJSONProjection;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.BlockEventProjection;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.FileProjection;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import de.uni_passau.fim.se2.scratchlog.web.dto.CodesDataDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.EventCountDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.FileDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.Sb3ZipDTO;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The controller for result management.
 */
@Controller
@RequestMapping(value = "/result")
public class ResultController {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger log = LoggerFactory.getLogger(ResultController.class);

    /**
     * The user service to use for user management.
     */
    private final UserService userService;

    /**
     * The event service to use for event management.
     */
    private final EventService eventService;

    /**
     * The experiment data service to use for retrieving experiment data.
     */
    private final ExperimentDataService experimentDataService;

    /**
     * The code service to use for retrieving participant codes.
     */
    private final CodeService codeService;

    /**
     * The file service to use for file management.
     */
    private final FileService fileService;

    private final ZipExportService zipExportService;

    private final PageService pageService;

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

    @Autowired
    public ResultController(
        final UserService userService,
        final EventService eventService,
        final ExperimentDataService experimentDataService,
        final CodeService codeService,
        final FileService fileService,
        final ZipExportService zipExportService,
        final PageService pageService) {
        this.userService = userService;
        this.eventService = eventService;
        this.experimentDataService = experimentDataService;
        this.codeService = codeService;
        this.fileService = fileService;
        this.zipExportService = zipExportService;
        this.pageService = pageService;
    }

    /**
     * Returns the result page containing the result information for the user with the given id during the experiment
     * with the given id. If the passed parameters are invalid, the user is not a participant in the given experiment,
     * or no corresponding user or experiment could be found, the user is redirected to the error page instead.
     *
     * @param experimentId The experiment id.
     * @param userId The user id.
     * @param model The model used to store information.
     * @return The result page on success, or the error page otherwise.
     */
    @GetMapping("")
    @Secured(Constants.ROLE_ADMIN)
    public ModelAndView getResult(@RequestParam(EXPERIMENT) final int experimentId,
                                  @RequestParam(USER) final int userId, final Model model) {
        if (!userService.existsParticipant(userId, experimentId)) {
            log.error(
                "Could not find participant entry for user with id {} for experiment with id {}", userId, experimentId
            );
            return new ModelAndView(Constants.ERROR);
        }

        try {
            List<EventCountDTO> blockEvents = eventService.getBlockEventCounts(userId, experimentId);
            List<EventCountDTO> clickEvents = eventService.getClickEventCounts(userId, experimentId);
            List<EventCountDTO> resourceEvents = eventService.getResourceEventCounts(userId, experimentId);
            List<EventCountDTO> jsonEvents = eventService.getJsonEventCounts(userId, experimentId);
            List<FileProjection> files = fileService.getFiles(userId, experimentId);
            List<Integer> zipIds = fileService.getZipIds(userId, experimentId);
            List<BlockEventJSONProjection> filteredJsons = codeService.getFilteredJsons(userId, experimentId, 1, 0, 0,
                    Optional.empty());
            CodesDataDTO codesDataDTO = eventService.getCodesData(userId, experimentId);

            model.addAttribute("codeCount", Math.max(codesDataDTO.getCount(), 0));
            model.addAttribute("pageSize", Constants.PAGE_SIZE);
            model.addAttribute("blockEvents", blockEvents);
            model.addAttribute("clickEvents", clickEvents);
            model.addAttribute("resourceEvents", resourceEvents);
            model.addAttribute("jsonEvents", jsonEvents);
            model.addAttribute("files", files);
            model.addAttribute("zips", zipIds);
            model.addAttribute("user", userId);
            model.addAttribute("experiment", experimentId);

            if (!filteredJsons.isEmpty()) {
                List<List<Integer>> bugCountsPerMinute = experimentDataService.getAnalyzedProgramDataCount(
                        filteredJsons);
                model.addAttribute("bugs", bugCountsPerMinute.get(0));
                model.addAttribute("smells", bugCountsPerMinute.get(1));
                model.addAttribute("perfumes", bugCountsPerMinute.get(2));
            } else {
                model.addAttribute("bugs", new ArrayList<>());
                model.addAttribute("smells", new ArrayList<>());
                model.addAttribute("perfumes", new ArrayList<>());
            }
            return new ModelAndView(RESULT);
        } catch (NotFoundException e) {
            return new ModelAndView(Constants.ERROR);
        }
    }

    /**
     * Makes the file with the given id available for download, if it exists. If the given id is invalid or no file
     * could be found in the database, the user is redirected to the error page instead.
     *
     * @param fileId The file id to search for.
     * @return The file for download on success, or the error page otherwise.
     */
    @GetMapping("/file")
    @Secured(Constants.ROLE_ADMIN)
    public Object downloadFile(@RequestParam(ID) final int fileId) {
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
     * @param experimentId The id of the experiment.
     * @param userId The id of the user.
     * @param jsonId The block event id to search for.
     * @param httpServletResponse The servlet response.
     * @throws IOException In case writing to the output stream fails.
     */
    @GetMapping("/generate")
    @Secured(Constants.ROLE_ADMIN)
    public void generateZipFile(@RequestParam(EXPERIMENT) final int experimentId,
                                @RequestParam(USER) final int userId,
                                @RequestParam("json") final int jsonId,
                                final HttpServletResponse httpServletResponse) throws IOException {
        prepareZipFileResponse(httpServletResponse, userId, experimentId, "sb3");
        zipExportService.exportSb3ForEvent(httpServletResponse.getOutputStream(), experimentId, userId, jsonId);
    }

    /**
     * Retrieves the zip file with the given id and makes it available for download, if it exists. If the id is invalid
     * or no zip file could be found in the database, the user is redirected to the error page instead.
     *
     * @param zipId The zip file id to search for.
     * @return The zip file for download on success, or the error page otherwise.
     */
    @GetMapping("/zip")
    @Secured(Constants.ROLE_ADMIN)
    public Object downloadZip(@RequestParam(ID) final int zipId) {
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
     * @param experimentId The experiment id to search for.
     * @param userId The user id to search for.
     * @param httpServletResponse The servlet response returning the files.
     * @throws IOException In case writing to the output stream
     */
    @GetMapping("/zips")
    @Secured(Constants.ROLE_ADMIN)
    public void downloadAllZips(@RequestParam(EXPERIMENT) final int experimentId,
                                @RequestParam(USER) final int userId,
                                final HttpServletResponse httpServletResponse) throws IOException {
        prepareZipFileResponse(httpServletResponse, userId, experimentId, "projects");
        zipExportService.exportSb3sForExperimentUser(httpServletResponse.getOutputStream(), experimentId, userId);
    }

    /**
     * Retrieves all the xml codes that were saved for the given user during the given experiment and makes them
     * available for download in a zip file.
     *
     * @param experimentId The experiment id to search for.
     * @param userId The user id to search for.
     * @param httpServletResponse The servlet response returning the files.
     * @throws IOException In case writing to the output stream fails.
     */
    @GetMapping("/xmls")
    @Secured(Constants.ROLE_ADMIN)
    public void downloadAllXmlFiles(@RequestParam(EXPERIMENT) final int experimentId,
                                    @RequestParam(USER) final int userId,
                                    final HttpServletResponse httpServletResponse) throws IOException {
        prepareZipFileResponse(httpServletResponse, userId, experimentId, "xml");
        zipExportService.exportXmlsForExperimentUser(httpServletResponse.getOutputStream(), experimentId, userId);
    }

    /**
     * Retrieves all the json strings that were saved for the given user during the given experiment and makes them
     * available for download in a zip file.
     *
     * @param experimentId The experiment id to search for.
     * @param userId The user id to search for.
     * @param httpServletResponse The servlet response returning the files.
     * @throws IOException In case writing to the output stream fails.
     */
    @GetMapping("/jsons")
    @Secured(Constants.ROLE_ADMIN)
    public void downloadAllJsonFilesForUser(@RequestParam(EXPERIMENT) final int experimentId,
                                            @RequestParam(USER) final int userId,
                                            final HttpServletResponse httpServletResponse) throws IOException {
        prepareZipFileResponse(httpServletResponse, userId, experimentId, "json");
        zipExportService.exportJsonsForExperimentUser(httpServletResponse.getOutputStream(), experimentId, userId);
    }

    /**
     * Loads a list of {@link BlockEventProjection}s for the given page number, user and experiment from the
     * database.
     *
     * @param experimentId The experiment id to search for.
     * @param userId The user id to search for.
     * @param page The current page number.
     * @return The list of block event projections.
     */
    @GetMapping("/codes")
    @Secured(Constants.ROLE_ADMIN)
    @ResponseBody
    public List<BlockEventProjection> getCodes(@RequestParam(EXPERIMENT) final int experimentId,
                                               @RequestParam(USER) final int userId,
                                               @RequestParam("page") final int page) {
        return pageService
            .getPaginatedCodesForUser(userId, experimentId, page)
            .getContent();
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
     * @param experimentId The experiment id to search for.
     * @param userId The user id to search for.
     * @param step The step interval in minutes.
     * @param start The start of the interval in which all json files should be downloaded.
     * @param end The end of the interval in which all json files should be downloaded.
     * @param includeFinalProject Whether the final project should be included.
     * @param httpServletResponse The servlet response returning the files.
     * @throws IOException In case writing to the output stream fails.
     * @throws IllegalArgumentException if any of the passed parameters are invalid.
     */
    @GetMapping("/sb3s")
    @Secured(Constants.ROLE_ADMIN)
    @SuppressWarnings("checkstyle:finalparameters")
    public void downloadSb3Files(
        @RequestParam(EXPERIMENT) final int experimentId,
        @RequestParam(USER) final int userId,
        @RequestParam(value = "step", required = false) Integer step,
        @RequestParam(value = "start", required = false) Integer start,
        @RequestParam(value = "end", required = false) Integer end,
        @RequestParam(value = "include", required = false) Boolean includeFinalProject,
        final HttpServletResponse httpServletResponse
    ) throws IOException {
        checkDownloadParameters(step, start, end, includeFinalProject);

        // the checkDownloadParameters above expects certain parameters to be null/non-null. Therefore, we cannot use
        // defaultValue in the RequestParam annotation.
        if (step == null) {
            step = 0;
        }
        if (includeFinalProject == null) {
            includeFinalProject = true;
        }
        if (start == null) {
            start = 0;
        }
        if (end == null) {
            end = 0;
        }

        prepareZipFileResponse(httpServletResponse, userId, experimentId, "zip");
        zipExportService.exportSb3sForExperimentUser(
            httpServletResponse.getOutputStream(), experimentId, userId, step, start, end, includeFinalProject);
    }

    /**
     * Generates sb3 files for all users of an experiment, if any code was saved for them during the experiment. The
     * JSON files loaded from the database are filtered according to the specified step parameter, if present. Every
     * JSON code is put in a zip file as a project.json file together with all costumes and sounds present in the
     * experiment project file as well as all files saved for the user during the experiment that were not saved as zip
     * files, meaning they are not resources that can be loaded from the Scratch library. Any resulting sb3 files are
     * written into another zip containing all entries for a given user. All zip files generated for each experiment
     * participant is then placed in another zip file which is made available for download.
     *
     * @param experimentId The experiment id to search for.
     * @param step The step interval in minutes.
     * @param httpServletResponse The servlet response returning the files.
     * @throws IOException In case writing to the output stream fails.
     * @throws IllegalArgumentException If the experiment has no participants.
     */
    @GetMapping("/sb3s/all")
    @Secured(Constants.ROLE_ADMIN)
    public void downloadExperimentSb3Files(
        @RequestParam(EXPERIMENT) final int experimentId,
        @RequestParam(value = "step", required = false, defaultValue = "0") final int step,
        final HttpServletResponse httpServletResponse
    ) throws IOException {
        String filename = "experiment" + experimentId + "_all_sb3s" + (step != 0 ? "_step" + step : "") + ".zip";
        prepareZipFileResponse(httpServletResponse, filename);
        zipExportService.exportSb3sForExperiment(httpServletResponse.getOutputStream(), experimentId, step);
    }

    /**
     * Generates a ZIP file consisting of a Sb3 file of the last project for every participant in an experiment, if any
     * code was saved for them during the experiment. Every project includes the corresponding JSON file as well as all
     * uploaded files by the user or the initial project.
     *
     * @param experimentId The experiment id for which to download the last projects.
     * @param httpServletResponse The servlet response returning the files for download.
     */
    @GetMapping("/sb3s/last")
    @Secured(Constants.ROLE_ADMIN)
    public void downloadLastExperimentSb3Files(
        @RequestParam(EXPERIMENT) final int experimentId, final HttpServletResponse httpServletResponse
    ) throws IOException {
        String filename = "experiment" + experimentId + "_last_sb3s.zip";
        prepareZipFileResponse(httpServletResponse, filename);
        zipExportService.exportLastSb3sForExperiment(httpServletResponse.getOutputStream(), experimentId);
    }

    /**
     * Downloads all project files (all `project.json` files and `events.csv`) for every participant in the given
     * experiment for which there are saved codes in the database. The download is a zip file consisting of a zip file
     * for every participant that includes the relevant files for that participant.
     *
     * @param experimentId The experiment id to download the project files form.
     * @param httpServletResponse The servlet response returning the file for download.
     */
    @GetMapping("/jsons/all")
    @Secured(Constants.ROLE_ADMIN)
    public void downloadAllJsonFiles(
        @RequestParam(EXPERIMENT) final int experimentId, final HttpServletResponse httpServletResponse
    ) throws IOException {
        String filename = "experiment" + experimentId + "_all_jsons.zip";
        prepareZipFileResponse(httpServletResponse, filename);
        zipExportService.exportJsonsForExperiment(httpServletResponse.getOutputStream(), experimentId);
    }

    /**
     * Checks, whether the parameters for downloading sb3 files are valid. For the parameters to be valid, both the
     * experiment and user ids have to be specified. If sb3 files in a certain range are to be downloaded, the start,
     * end and include parameters need to be present. If sb3 files are downloaded in minute intervals, the start
     * parameter cannot be specified.
     *
     * @param step The step interval in minutes.
     * @param start The start of the interval in which all json files should be downloaded.
     * @param end The end of the interval in which all json files should be downloaded.
     * @param include Whether the final project should be included.
     * @throws IllegalArgumentException if the required parameters are null or invalid.
     */
    private void checkDownloadParameters(
        final Integer step, final Integer start, final Integer end, final Boolean include
    ) {
        if (step == null && start == null && end == null && Boolean.FALSE.equals(include)) {
            throw new IllegalArgumentException(
                "Either step or start and end must be specified when not including final project!"
            );
        }

        if (step != null && step <= 0) {
            throw new IllegalArgumentException("step must be >= 0");
        }
        if (start != null && start < 0) {
            throw new IllegalArgumentException("start must be >= 0");
        }
        if (end != null && end < 0) {
            throw new IllegalArgumentException("end must be >= 0");
        }

        if (start != null && step != null) {
            throw new IllegalArgumentException("Cannot generate zip file if both step and start, end and include "
                    + "parameters are specified!");
        }

        if (start != null || end != null) {
            if (start == null || end == null || include == null) {
                throw new IllegalArgumentException("Cannot generate zip file in a set interval if not all of the needed"
                    + " parameters start, end and include are specified!");
            }
        }

        if (start != null && end != null && start > end) {
            throw new IllegalArgumentException("Cannot generate zip file for start position " + start
                + " bigger than end position " + end + "!");
        }
    }

    /**
     * Sets the content type, header, status, and filename of the servlet response accordingly.
     *
     * @param httpServletResponse The servlet response to prepare for a ZIP response.
     * @param filename The filename that the response should be.
     */
    private void prepareZipFileResponse(final HttpServletResponse httpServletResponse, final String filename) {
        httpServletResponse.setContentType("application/zip");
        httpServletResponse.setHeader("Content-Disposition", "attachment;filename=" + filename);
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Same as {@link #prepareZipFileResponse(HttpServletResponse, String)}, but with a standardized format for the
     * filename consisting of the user id, experiment id, and filetype (either 'sb3' or 'zip').
     *
     * @param httpServletResponse The servlet response.
     * @param userId The user id to use to name the zip file.
     * @param experimentId The experiment id to use to name the zip file.
     * @param filetype The filetype to use to name the zip file.
     */
    private void prepareZipFileResponse(final HttpServletResponse httpServletResponse, final int userId,
                                        final int experimentId, final String filetype) {
        String fileEnding = filetype.equals("sb3") ? ".sb3" : ".zip";
        String filename = filetype + "_user" + userId + "_experiment" + experimentId + fileEnding;
        prepareZipFileResponse(httpServletResponse, filename);
    }

}
