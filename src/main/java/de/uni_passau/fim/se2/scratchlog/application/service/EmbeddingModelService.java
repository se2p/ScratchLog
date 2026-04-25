package de.uni_passau.fim.se2.scratchlog.application.service;

import de.uni_passau.fim.se2.embedded_kittens.MLOutputPath;
import de.uni_passau.fim.se2.embedded_kittens.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.embedded_kittens.ggnn.GgnnAnalyzerOutput;
import de.uni_passau.fim.se2.embedded_kittens.ggnn.GgnnOutputFormat;
import de.uni_passau.fim.se2.embedded_kittens.ggnn.GgnnProgramPreprocessor;
import de.uni_passau.fim.se2.embedded_kittens.shared.ActorNameNormalizer;
import de.uni_passau.fim.se2.embedded_kittens.shared.WholeProgramJsonProcessor;
import de.uni_passau.fim.se2.embedded_kittens.shared.WholeProgramOutput;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.ExampleSolution;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.BlockEventRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ExperimentRepository;
import de.uni_passau.fim.se2.scratchlog.spring.configuration.CodeEmbeddingConfiguration;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@Profile(Constants.PROFILE_CODE_EMBEDDINGS)
public class EmbeddingModelService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingModelService.class);

    private final CodeEmbeddingConfiguration configuration;

    private final BlockEventRepository blockEventRepository;

    private final ExperimentRepository experimentRepository;

    private final ExperimentService experimentService;

    private final RestClient restClient;

    private final WholeProgramJsonProcessor<GgnnAnalyzerOutput> ggnnProgramPreprocessor;

    @Autowired
    public EmbeddingModelService(
        final CodeEmbeddingConfiguration codeEmbeddingConfiguration,
        final BlockEventRepository blockEventRepository,
        final ExperimentRepository experimentRepository,
        final ExperimentService experimentService
    ) {
        this.configuration = codeEmbeddingConfiguration;
        this.blockEventRepository = blockEventRepository;
        this.experimentRepository = experimentRepository;
        this.experimentService = experimentService;

        this.restClient = RestClient.create(configuration.getEmbeddingConnectorUrl());

        MLPreprocessorCommonOptions mlOptions = new MLPreprocessorCommonOptions(
            MLOutputPath.console(),
            true,
            true,
            false,
            ActorNameNormalizer.getDefault()
        );
        final GgnnProgramPreprocessor ggnnPreprocessor = new GgnnProgramPreprocessor(
            mlOptions,
            GgnnOutputFormat.JSON_GRAPH,
            "project"
        );
        this.ggnnProgramPreprocessor = new WholeProgramJsonProcessor<>(mlOptions, ggnnPreprocessor);
    }

    /**
     * Demo.
     *
     * @throws IOException ignored
     */
    @EventListener(ApplicationReadyEvent.class)
    public void testing() throws IOException {
        final int experimentId = 60;
        final Map<Integer, String> studentProjectsById = new HashMap<>();
        blockEventRepository
            .findLastPerUserInExperiment(experimentId)
            .forEach(project -> studentProjectsById.put(project.id(), project.projectJson()));

        final var starterProject = getProjectJson(experimentRepository.getExperimentStarterProject(experimentId));
        final ExampleSolution solution = experimentService.getExampleSolution(experimentId);
        if (solution == null) {
            // P-V-projection requires starter project and solution
            return;
        }
        final var solutionProject = getProjectJson(solution.getSb3Project());

        final var projection = getProgressVarianceProjection(
            starterProject,
            solutionProject,
            studentProjectsById
        );
        log.info("{}", projection);
    }

    /**
     * Returns the progress variance projection for the given projects.
     *
     * @param templateProject The template given to all students at the beginning of a session
     * @param solutionProject The example solution of a session.
     * @param studentProjects Some student projects. The same IDs will be used in the response. Can
     *                        be for example the latest projects per student, or only projects of a
     *                        single student over time.
     * @return The 2D-progress-variance-projection of the student projects.
     */
    public ProgressVarianceProjection getProgressVarianceProjection(
        final String templateProject,
        final String solutionProject,
        final Map<Integer, String> studentProjects
    ) {
        try {
            final var request = buildProgressVarianceProjectionRequest(
                templateProject, solutionProject, studentProjects
            );

            return restClient.post()
                .uri("ggnn/progress-variance-projection")
                .body(request)
                .retrieve()
                .body(ProgressVarianceProjection.class);
        } catch (Exception e) {
            // todo: actual error handling
            throw new RuntimeException(e);
        }
    }

    private ProgressVarianceProjectionRequest<GgnnAnalyzerOutput> buildProgressVarianceProjectionRequest(
        final String templateProgramJson,
        final String solutionProgramJson,
        final Map<Integer, String> studentProgramJsons
    ) throws ParsingException {
        final var templateProgram = processProgramForGgnn(templateProgramJson);
        final var solutionProgram = processProgramForGgnn(solutionProgramJson);

        final Map<Integer, WholeProgramOutput<GgnnAnalyzerOutput>> studentPrograms = studentProgramJsons
            .entrySet()
            .parallelStream()
            .map(entry -> {
                try {
                    return new AbstractMap.SimpleImmutableEntry<>(
                        entry.getKey(),
                        processProgramForGgnn(entry.getValue())
                    );
                } catch (ParsingException e) {
                    // ignore projects we cannot parse -> we cannot compute an embedding in this case
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return new ProgressVarianceProjectionRequest<>(templateProgram, solutionProgram, studentPrograms);
    }

    private WholeProgramOutput<GgnnAnalyzerOutput> processProgramForGgnn(final Program program) {
        return ggnnProgramPreprocessor.processWholeProgram(program)
            .findFirst()
            .orElseThrow();
    }

    private WholeProgramOutput<GgnnAnalyzerOutput> processProgramForGgnn(
        final String programJson
    ) throws ParsingException {
        final Program program = parseProgram(programJson);
        return processProgramForGgnn(program);
    }

    private Program parseProgram(final String programJson) throws ParsingException {
        final Scratch3Parser parser = new Scratch3Parser();
        return parser.parseString("project", programJson);
    }

    private String getProjectJson(final byte[] programSb3) throws IOException {
        if (programSb3 == null) {
            return null;
        }

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(programSb3))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if ("project.json".equals(entry.getName())) {
                    byte[] bytes = zis.readNBytes((int) entry.getSize());
                    return new String(bytes, StandardCharsets.UTF_8);
                }
            }
        }

        log.warn("Project SB3 did not contain a project.json");

        return null;
    }

    public record ProgressVarianceProjectionRequest<T>(
        WholeProgramOutput<T> templateProgram,
        WholeProgramOutput<T> solutionProgram,
        Map<Integer, WholeProgramOutput<T>> studentPrograms
    ) {
    }

    public record ProgressVarianceProjection(
        Map<String, List<Double>> projections
    ) {
    }

}
