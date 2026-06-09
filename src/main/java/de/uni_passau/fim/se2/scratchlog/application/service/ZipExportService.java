/*
 * This file is part of ScratchLog.
 * Licenced under the GPL v3.0 or later.
 *
 * SPDX-FileCopyrightText: 2021-2026 Scratchlog contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * ScratchLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ScratchLog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.uni_passau.fim.se2.scratchlog.application.service;

import com.opencsv.CSVWriter;
import de.uni_passau.fim.se2.scratchlog.application.exception.NotFoundException;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.BlockEventJSONProjection;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.BlockEventXMLProjection;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.ExperimentProjection;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.UserRepository;
import de.uni_passau.fim.se2.scratchlog.web.dto.FileDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.ParticipantDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.Sb3ZipDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
public class ZipExportService {

    private static final Logger log = LoggerFactory.getLogger(ZipExportService.class);

    private final CodeService codeService;

    private final ExperimentService experimentService;

    private final FileService fileService;

    private final ParticipantService participantService;

    private final UserRepository userRepository;

    public ZipExportService(
        final CodeService codeService,
        final ExperimentService experimentService,
        final FileService fileService,
        final ParticipantService participantService,
        final UserRepository userRepository) {
        this.codeService = codeService;
        this.experimentService = experimentService;
        this.fileService = fileService;
        this.participantService = participantService;
        this.userRepository = userRepository;
    }

    /**
     * Exports the SB3 file for a user at a specific point in time.
     *
     * @param outputStream The data sink.
     * @param experimentId The experiment in which the event occurred.
     * @param userId The user that generated the event.
     * @param jsonId The event id.
     * @throws IOException Thrown in case writing to the output stream fails.
     */
    public void exportSb3ForEvent(
        final OutputStream outputStream, final int experimentId, final int userId, final int jsonId
    ) throws IOException {
        ExperimentProjection experiment = experimentService.getSb3File(experimentId, true);
        List<FileDTO> fileDTOS = fileService.getFileDTOs(userId, experimentId);
        byte[] code = codeService.findJsonById(jsonId).getBytes(StandardCharsets.UTF_8);

        // Keep track of already added file names to avoid adding duplicate entries.
        Set<String> fileNames = new HashSet<>();

        try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
            if (experiment.getProject() != null) {
                writeInitialProjectData(zos, experiment.getProject(), fileNames);
            }

            for (FileDTO fileDTO : fileDTOS) {
                writeFileData(zos, fileDTO, fileNames);
            }

            writeJsonData(zos, code);
            zos.finish();
        }
    }

    /**
     * Exports all SB3s for an experiment inside one zip file.
     *
     * @param outputStream The data sink.
     * @param experimentId Some experiment.
     * @param step User projects will be sampled from every {@code step} minutes.
     * @throws IOException Thrown in case writing to the output stream fails.
     * @throws IllegalArgumentException If the given experiment has no participants.
     */
    public void exportSb3sForExperiment(
        final OutputStream outputStream, final int experimentId, final int step
    ) throws IOException {
        List<ParticipantDTO> participants = participantService.getParticipants(experimentId);
        if (participants.isEmpty()) {
            throw new IllegalArgumentException("Cannot download sb3 files for experiment with no participants!");
        }

        ExperimentProjection experiment = experimentService.getSb3File(experimentId, true);

        try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
            for (ParticipantDTO participantDTO : participants) {
                writeUserSb3Entry(zos, experiment, participantDTO.getUser(), step);
            }

            zos.finish();
        }
    }

    /**
     * Exports the last project for every participant in the experiment as a ZIP file.
     *
     * @param outputStream The data sink.
     * @param experimentId Some experiment.
     * @throws IOException Thrown in case writing to the output stream fails.
     * @throws IllegalArgumentException If the given experiment has no participants.
     */
    public void exportLastSb3sForExperiment(
        final OutputStream outputStream, final int experimentId
    ) throws IOException {
        List<ParticipantDTO> participants = participantService.getParticipants(experimentId);
        if (participants.isEmpty()) {
            throw new IllegalArgumentException("Cannot download sb3 files for experiment with no participants!");
        }

        ExperimentProjection experiment = experimentService.getSb3File(experimentId, true);

        try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
            for (ParticipantDTO participantDTO : participants) {
                writeLastUserSb3Entry(zos, experiment, participantDTO.getUser());
            }

            zos.finish();
        }
    }

    /**
     * Exports all project JSON files from all participants of the given experiment as a ZIP file.
     *
     * @param outputStream The data sink.
     * @param experimentId Some experiment.
     * @throws IOException Thrown in case writing to the output stream fails.
     * @throws IllegalArgumentException If the given experiment ahs no participants.
     */
    public void exportJsonsForExperiment(
        final OutputStream outputStream, final int experimentId
    ) throws IOException {
        List<ParticipantDTO> participants = participantService.getParticipants(experimentId);
        if (participants.isEmpty()) {
            throw new IllegalArgumentException("Cannot download JSON files for experiment with no participants!");
        }

        try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
            for (ParticipantDTO participantDTO : participants) {
                int userId = participantDTO.getUser();

                try {
                    // Write all JSON files of the current participant to an inner stream.
                    ByteArrayOutputStream innerZip = new ByteArrayOutputStream();
                    exportJsonsForExperimentUser(innerZip, experimentId, userId);

                    ZipEntry entry = new ZipEntry("jsons_" + usernameForId(userId) + ".zip");
                    entry.setSize(innerZip.size());
                    zos.putNextEntry(entry);
                    zos.write(innerZip.toByteArray());
                    zos.closeEntry();
                } catch (NotFoundException e) {
                    log.error("Cannot export JSON codes for user with id {} with no saved codes.", userId);
                }
            }

            zos.finish();
        }
    }

    /**
     * Exports all SB3s for a user in an experiment.
     *
     * @param outputStream The data sink.
     * @param experimentId Some experiment.
     * @param userId Some user.
     * @throws IOException Thrown in case writing to the output stream fails.
     */
    public void exportSb3sForExperimentUser(
        final OutputStream outputStream, final int experimentId, final int userId
    ) throws IOException {
        List<Sb3ZipDTO> sb3ZipDTOS = fileService.getZipFiles(userId, experimentId);

        try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
            for (Sb3ZipDTO sb3ZipDTO : sb3ZipDTOS) {
                ZipEntry entry = new ZipEntry(sb3ZipDTO.getId() + sb3ZipDTO.getName());
                entry.setSize(sb3ZipDTO.getContent().length);
                zos.putNextEntry(entry);
                zos.write(sb3ZipDTO.getContent());
                zos.closeEntry();
            }

            zos.finish();
        }
    }

    /**
     * Exports selected SB3s for a user in an experiment.
     *
     * @param outputStream The data sink.
     * @param experimentId Some experiment.
     * @param userId Some user.
     * @param step User projects will be sampled from every {@code step} minutes.
     * @param start The start of the interval from which on projects will be sampled.
     * @param end The end of the interval to which projects will be sampled.
     * @param includeFinalProject If the final project should be included, even if it falls outside the interval.
     * @throws IOException Thrown in case writing to the output stream fails.
     */
    public void exportSb3sForExperimentUser(
        final OutputStream outputStream, final int experimentId, final int userId,
        final int step, final int start, final int end, final boolean includeFinalProject
    ) throws IOException {
        ExperimentProjection experiment = experimentService.getSb3File(experimentId, true);
        List<FileDTO> fileDTOS = fileService.getFileDTOs(userId, experimentId);
        Optional<Sb3ZipDTO> finalProject = fileService.findFinalProject(userId, experimentId);
        List<BlockEventJSONProjection> jsons = codeService.getFilteredJsons(
            userId, experimentId, step, start, end, finalProject
        );

        try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
            String username = usernameForId(userId);
            writeUserSb3Files(zos, experiment, fileDTOS, finalProject, jsons, includeFinalProject, username);
            zos.finish();
        }
    }

    /**
     * Exports all event XMLs for the given user in some experiment.
     *
     * @param outputStream The data sink.
     * @param experimentId Some experiment.
     * @param userId Some user.
     * @throws IOException Thrown in case writing to the output stream fails.
     */
    public void exportXmlsForExperimentUser(
        final OutputStream outputStream, final int experimentId, final int userId
    ) throws IOException {
        List<BlockEventXMLProjection> userXmls = codeService.getXMLForUser(userId, experimentId);

        String username = usernameForId(userId);
        try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
            for (int i = 0; i < userXmls.size(); i++) {
                BlockEventXMLProjection xml = userXmls.get(i);
                ZipEntry entry = new ZipEntry("xml_" + username + "_" + i + ".xml");
                entry.setSize(xml.getXml().length());
                zos.putNextEntry(entry);
                zos.write(xml.getXml().getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }

            zos.finish();
        }
    }

    /**
     * Exports all project JSONs for the given user in some experiment.
     *
     * @param outputStream The data sink.
     * @param experimentId Some experiment.
     * @param userId Some user.
     * @throws IOException Thrown in case writing to the output stream fails.
     * @throws NotFoundException If the experiment or user with the given ids could not be found, or no JSON code for
     *                           the given user and experiment could be found.
     */
    public void exportJsonsForExperimentUser(
        final OutputStream outputStream, final int experimentId, final int userId) throws IOException {
        List<BlockEventJSONProjection> evenJsons = codeService.getJsonForUser(userId, experimentId);

        try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
            writeCSVData(zos, evenJsons, Optional.empty(), false);

            String username = usernameForId(userId);
            for (int i = 0; i < evenJsons.size(); ++i) {
                BlockEventJSONProjection json = evenJsons.get(i);
                ZipEntry entry = new ZipEntry("json_" + username + "_" + i + ".json");
                entry.setSize(json.getCode().length());
                zos.putNextEntry(entry);
                zos.write(json.getCode().getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }

            zos.finish();
        }
    }

    /**
     * Generates sb3 files for the desired json codes saved for the given user during the given experiment and puts them
     * in a ZIP file which is made available for download.
     *
     * @param zos        The {@link ZipOutputStream} returning the generated file to the user.
     * @param experiment The initial experiment project data.
     * @param userId     The id of the user.
     * @param steps      The step interval in minutes.
     * @throws IOException if the file content could not be written correctly.
     */
    private void writeUserSb3Entry(final ZipOutputStream zos, final ExperimentProjection experiment,
                                   final int userId, final int steps) throws IOException {
        List<FileDTO> fileDTOS = fileService.getFileDTOs(userId, experiment.getId());
        Optional<Sb3ZipDTO> finalProject = fileService.findFinalProject(userId, experiment.getId());
        List<BlockEventJSONProjection> jsons = codeService.getFilteredJsons(
            userId, experiment.getId(), steps, 0, 0, finalProject
        );

        if (!jsons.isEmpty()) {
            String username = usernameForId(userId);

            ByteArrayOutputStream innerZip = new ByteArrayOutputStream();
            ZipOutputStream innerZos = new ZipOutputStream(new BufferedOutputStream(innerZip));
            writeUserSb3Files(innerZos, experiment, fileDTOS, finalProject, jsons, true, username);
            // innerZos is the zip file for a single user, which has all the needed data written to it after
            // writeUserSb3Files, so close the ZOS here to avoid malformed zip data.
            innerZos.flush();
            innerZos.close();
            ZipEntry createdZip = new ZipEntry("sb3s_" + username + ".zip");
            zos.putNextEntry(createdZip);
            zos.write(innerZip.toByteArray());
            zos.closeEntry();
        } else {
            log.info("Could not generate zip file entry for participant with no saved JSON codes.");
        }
    }

    /**
     * Generates a Sb3 file from the latest project of the specified user in the specified experiment, and appends it
     * to the given ZIP stream.
     *
     * @param zos        The {@link ZipOutputStream} to which the created Sb3 file should be appended.
     * @param experiment The data for the initial project of the experiment.
     * @param userId     The id of the user to create the entry for.
     * @throws IOException       If the Sb3 file could not be written correctly.
     * @throws NotFoundException If no user for the specified id could be found.
     */
    private void writeLastUserSb3Entry(final ZipOutputStream zos, final ExperimentProjection experiment,
                                       final int userId) throws IOException {
        List<FileDTO> fileDTOs = fileService.getFileDTOs(userId, experiment.getId());

        String json = codeService.findFirstJSON(userId, experiment.getId());
        if (json == null) {
            return;
        }

        try {
            String filename = "last_sb3_" + usernameForId(userId) + ".sb3";
            createSb3File(json, zos, filename, experiment, fileDTOs);
        } catch (NotFoundException e) {
            log.error("Could not find corresponding user for id {} while writing last Sb3 entry.", userId);
        }
    }

    /**
     * Generates sb3 files for the given list of JSON codes including the given list of file DTOs generated by a
     * specific user and the initial project information. If the final project should be included, it is also added as
     * an additional entry.
     *
     * @param zos The {@link ZipOutputStream} returning the generated file to the user.
     * @param experiment The initial experiment project data.
     * @param fileDTOS The saved file data.
     * @param finalProject The final project state saved for the user.
     * @param jsons The JSON codes used to generate sb3 files.
     * @param includeFinalProject Whether the final project should be included or not.
     * @param username The username of the user for naming the project files.
     * @throws IOException if the file content could not be written correctly.
     */
    private void writeUserSb3Files(final ZipOutputStream zos, final ExperimentProjection experiment,
                                   final List<FileDTO> fileDTOS, final Optional<Sb3ZipDTO> finalProject,
                                   final List<BlockEventJSONProjection> jsons, final boolean includeFinalProject,
                                   final String username)
        throws IOException {
        writeCSVData(zos, jsons, finalProject, includeFinalProject);

        for (int i = 0; i < jsons.size(); i++) {
            BlockEventJSONProjection json = jsons.get(i);
            String filename = "project_" + username + "_" + i + ".sb3";
            createSb3File(json.getCode(), zos, filename, experiment, fileDTOS);
        }

        // Add the final project if it should be included. If it should be included but is not present, readd the last
        // project under the name `final_project.sb3` (if there even are projects for the participants).
        Optional<BlockEventJSONProjection> lastJSON
            = jsons.stream().max(Comparator.comparing(BlockEventJSONProjection::getDate));
        if (includeFinalProject) {
            if (finalProject.isPresent()) {
                writeFinalProjectData(zos, finalProject.get());
            } else if (lastJSON.isPresent()) {
                createSb3File(lastJSON.get().getCode(), zos, "final_project.sb3", experiment, fileDTOS);
            }
        }
    }

    /**
     * Creates a zip file entry for a CSV file containing information on the filtered {@link BlockEventJSONProjection}s
     * for which a sb3 file will be generated. For each even, its id, the date at which it was created and the
     * event that triggered it are written to the csv file. If the final sb3 project is present, and it is to be
     * included, its information is added as well.
     *
     * @param zos The {@link ZipOutputStream} returning the generated file to the user.
     * @param blockEvents The filtered blockEvents.
     * @param finalProject The {@link Optional} {@link Sb3ZipDTO} containing the information on the final project.
     * @param includeFinalProject Boolean indicating whether the final project data should be added.
     * @throws IOException if the file content could not be written correctly.
     */
    private void writeCSVData(final ZipOutputStream zos, final List<BlockEventJSONProjection> blockEvents,
                              final Optional<Sb3ZipDTO> finalProject, final boolean includeFinalProject)
        throws IOException {
        ZipEntry entry = new ZipEntry("events.csv");
        zos.putNextEntry(entry);

        CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(zos));
        csvWriter.writeNext(new String[] {"id", "date", "event"});

        blockEvents.forEach(event -> csvWriter.writeNext(new String[] {
            Integer.toString(event.getId()), event.getDate().toString(), event.getEvent()
        }));

        if (finalProject.isPresent() && includeFinalProject) {
            csvWriter.writeNext(new String[] {
                "final project", finalProject.get().getDate().toString(), "FINISH"
            });
        }

        csvWriter.flush();
        zos.closeEntry();
    }

    /**
     * Creates a sb3 file saved as a zip entry for the given json code. Beside the json itself, all saved files and the
     * initial project data are included in the zip file.
     *
     * @param jsonCode The json code to be used.
     * @param zos The {@link ZipOutputStream} in which the zip file should be written.
     * @param filename The name of the file to create. Should typically end in `.sb3`.
     * @param experiment The experiment.
     * @param fileDTOS The saved files.
     * @throws IOException if the data could not be written correctly.
     */
    private void createSb3File(final String jsonCode, final ZipOutputStream zos, final String filename,
                               final ExperimentProjection experiment, final List<FileDTO> fileDTOS) throws IOException {
        ByteArrayOutputStream innerZip = new ByteArrayOutputStream();

        try (ZipOutputStream innerZos = new ZipOutputStream(new BufferedOutputStream(innerZip))) {
            // Keep track of already added file names to avoid adding duplicate entries.
            Set<String> fileNames = new HashSet<>();

            if (experiment.getProject() != null) {
                writeInitialProjectData(innerZos, experiment.getProject(), fileNames);
            }

            for (FileDTO fileDTO : fileDTOS) {
                writeFileData(innerZos, fileDTO, fileNames);
            }

            byte[] code = jsonCode.getBytes(StandardCharsets.UTF_8);
            writeJsonData(innerZos, code);
            innerZos.flush();
        }

        ZipEntry createdZip = new ZipEntry(filename);
        zos.putNextEntry(createdZip);
        zos.write(innerZip.toByteArray());
        zos.closeEntry();
    }

    /**
     * Writes the content of the given byte[] representing the initial sb3 project loaded on experiment start to the
     * given {@link ZipOutputStream}.
     *
     * @param zos The {@link ZipOutputStream} returning the generated file to the user.
     * @param project The initial sb3 project.
     * @param fileNames A set of the already seen file names to avoid adding duplicate files.
     * @throws IOException if the file content could not be written correctly.
     */
    private void writeInitialProjectData(final ZipOutputStream zos, final byte[] project,
                                         final Set<String> fileNames) throws IOException {
        try (InputStream file = new ByteArrayInputStream(project); ZipInputStream zin = new ZipInputStream(file)) {
            ZipEntry ze;

            while ((ze = zin.getNextEntry()) != null) {
                if (!ze.getName().equals("project.json") && !fileNames.contains(ze.getName())) {
                    zos.putNextEntry(ze);
                    int current;
                    while ((current = zin.read()) >= 0) {
                        zos.write(current);
                    }
                    zos.closeEntry();
                    fileNames.add(ze.getName());
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
     * Returns the username of the user with the given id, or a fallback name if the given id could not be found.
     *
     * @param userId The id of the user to retrieve the username for.
     * @return The username of the given user, or 'unknown_user_ID' in case the user cannot be found in the database.
     */
    private String usernameForId(final int userId) {
        return userRepository.findById(userId)
            .map(User::getUsername)
            .orElse("unknown_user_" + userId);
    }

}
