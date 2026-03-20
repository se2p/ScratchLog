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

package de.uni_passau.fim.se2.scratchlog;

import de.uni_passau.fim.se2.scratchlog.application.service.ParticipantService;
import de.uni_passau.fim.se2.scratchlog.application.service.ZipExportService;
import de.uni_passau.fim.se2.scratchlog.web.dto.ParticipantDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Main class starting the application.
 */
@SpringBootApplication
@EnableScheduling
@EnableJdbcHttpSession
public class ScratchLogApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ScratchLogApplication.class);

    private final ApplicationContext applicationContext;

    private final Optional<ParticipantService> participantService;

    private final Optional<ZipExportService> zipExportService;

    public ScratchLogApplication(
        final ApplicationContext applicationContext,
        final Optional<ParticipantService> participantService,
        final Optional<ZipExportService> zipExportService
    ) {
        this.applicationContext = applicationContext;
        this.participantService = participantService;
        this.zipExportService = zipExportService;
    }

    /**
     * Main class starting the application with the given arguments.
     *
     * @param args The arguments passed on application startup.
     */
    public static void main(final String[] args) {
        SpringApplication.run(ScratchLogApplication.class, args);
    }

    /**
     * Optionally runs the SB3 exporter and exits the application instead of starting the webserver.
     *
     * <p>CLI flags for the SB3 exporter:
     * {@code java -jar scratchLog.jar sb3exporter $outputDirectory experimentId...} where
     * {@code $outputDirectory} needs to be a directory (it may not exist yet), and
     * {@code experimentId} is the ID of an experiment. This argument can be repeated to create
     * multiple export zip files for multiple experiments.
     *
     * @param args The command line arguments.
     * @throws Exception In case something goes wrong.
     */
    @Override
    public void run(final String... args) throws Exception {
        if (args.length > 0 && "sb3exporter".equals(args[0])) {
            runExporter(args);
            SpringApplication.exit(applicationContext, () -> 0);
        }
    }

    private void runExporter(final String... args) throws IOException {
        final List<String> argsList = Arrays.asList(args);

        if (zipExportService.isEmpty() || participantService.isEmpty()) {
            throw new IllegalStateException("Internal error: Need the export services.");
        }

        final Path outputDir = Path.of(argsList.get(1));
        if (!outputDir.toFile().exists()) {
            Files.createDirectories(outputDir);
        } else if (outputDir.toFile().isFile()) {
            throw new IllegalArgumentException("Expected an output directory, not a file!");
        }

        for (final String arg : argsList.subList(2, argsList.size() - 1)) {
            final int experimentId;
            try {
                experimentId = Integer.parseInt(arg);
            } catch (NumberFormatException e) {
                log.warn("Invalid experiment id '{}'. Skipping...", arg);
                continue;
            }

            final Path experimentOutputDir = outputDir.resolve("experiment_" + experimentId);
            Files.createDirectories(experimentOutputDir);
            exportExperimentZip(experimentOutputDir, experimentId);
        }
    }

    private void exportExperimentZip(final Path outputDir, final int experimentId) {
        final Set<Integer> participants = participantService.orElseThrow().getParticipants(experimentId).stream()
            .map(ParticipantDTO::getUser)
            .collect(Collectors.toUnmodifiableSet());

        for (final int user : participants) {
            final Path outputFile = outputDir.resolve(String.format("user_%d.zip", user));
            log.info("Exporting data for experiment {} and user {} to {}", experimentId, user, outputFile);

            try (FileOutputStream fos = new FileOutputStream(outputFile.toFile())) {
                zipExportService.orElseThrow().exportSb3sForExperimentUser(fos, experimentId, user, 0, 0, 0, true);
            } catch (IOException e) {
                log.error("Could not write to zip file {}", outputFile, e);
            }
        }
    }

}
