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
import de.uni_passau.fim.se2.scratchlog.persistence.repository.Project;
import de.uni_passau.fim.se2.scratchlog.spring.configuration.CodeEmbeddingConfiguration;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentLruCache;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@Profile(Constants.PROFILE_CODE_EMBEDDINGS)
public class EmbeddingModelService {

    private static final int CACHE_SIZE = 5_000;

    private static final Logger log = LoggerFactory.getLogger(EmbeddingModelService.class);

    private final JsonMapper jsonMapper;

    private final BlockEventRepository blockEventRepository;

    private final ExperimentRepository experimentRepository;

    private final ExperimentService experimentService;

    private final CodeService codeService;

    private final RestClient restClient;

    private final WholeProgramJsonProcessor<GgnnAnalyzerOutput> ggnnProgramPreprocessor;

    private final ConcurrentLruCache<String, WholeProgramOutput<GgnnAnalyzerOutput>> ggnnCache;

    @Autowired
    public EmbeddingModelService(
        final CodeEmbeddingConfiguration codeEmbeddingConfiguration,
        final JsonMapper jsonMapper,
        final BlockEventRepository blockEventRepository,
        final ExperimentRepository experimentRepository,
        final ExperimentService experimentService,
        final CodeService codeService
    ) {
        this.jsonMapper = jsonMapper;
        this.blockEventRepository = blockEventRepository;
        this.experimentRepository = experimentRepository;
        this.experimentService = experimentService;
        this.codeService = codeService;

        this.restClient = RestClient.create(codeEmbeddingConfiguration.getEmbeddingConnectorUrl());

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

        ggnnCache = new ConcurrentLruCache<>(CACHE_SIZE, (programJson) -> {
            try {
                return processProgramForGgnn(programJson);
            } catch (ParsingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Computes the progress-variance-projection for the given experiment.
     *
     * <p>For all participants in the experiment and their latest code change.
     *
     * @param experimentId The experiment id.
     * @return The progress-variance projection.
     * @throws IOException In case the example/solution projects cannot be parsed.
     */
    public ProgressVarianceProjection getProgressVarianceProjectionAllLatest(
        final int experimentId
    ) throws IOException {
        StopWatch watch = new StopWatch();
        watch.start();

        final Map<Integer, Integer> projectIdToStudentId = new HashMap<>();
        final Map<Integer, String> projectsById = new HashMap<>();
        blockEventRepository
            .findLastPerUserInExperiment(experimentId)
            .forEach(project -> {
                projectsById.put(project.id(), project.projectJson());
                projectIdToStudentId.put(project.id(), project.userId());
            });

        final var starterProject = getStarterProject(experimentId);
        final var solutionProject = getSolutionProject(experimentId);
        if (starterProject == null || solutionProject == null) {
            throw new IllegalArgumentException(
                "Progress-Variance-Projection can only be constructed if start and solution projects are given."
            );
        }
        watch.stop();
        log.debug("Database fetching finished in {}ms.", watch.getTotalTimeMillis());

        final ProgressVarianceProjectionResponse response = getProgressVarianceProjection(
            starterProject,
            solutionProject,
            projectsById
        );

        return convertResponse(response, projectIdToStudentId);
    }

    /**
     * Computes the progress-variance-projection for the given experiment.
     *
     * <p>As a timeline of program states of all given users in the experiment.
     *
     * @param experimentId The experiment id.
     * @param userIds The set of users for which the progression timeline should be generated.
     * @param stepMinutes The step in minutes between program states.
     * @return The progress-variance projection.
     * @throws IOException In case the example/solution projects cannot be parsed.
     */
    public ProgressVarianceProjection getProgressVarianceProjectionForUsers(
        final int experimentId,
        final Set<Integer> userIds,
        final int stepMinutes
    ) throws IOException {
        if (userIds.isEmpty()) {
            return new ProgressVarianceProjection(Collections.emptyList());
        }

        StopWatch watch = new StopWatch();
        watch.start();

        final Map<Integer, String> projectsByProjectId = new HashMap<>();
        final Map<Integer, Integer> projectIdToStudentId = new HashMap<>();
        for (final int userId : userIds) {
            final List<Project> studentProjects = getProjectsForUser(experimentId, userId, stepMinutes);
            studentProjects.forEach(project -> {
                projectsByProjectId.put(project.id(), project.projectJson());
                projectIdToStudentId.put(project.id(), project.userId());
            });
        }

        final var starterProject = getStarterProject(experimentId);
        final var solutionProject = getSolutionProject(experimentId);
        if (starterProject == null || solutionProject == null) {
            throw new IllegalArgumentException(
                "Progress-Variance-Projection can only be constructed if start and solution projects are given."
            );
        }
        watch.stop();
        log.debug("Database fetching finished in {}ms.", watch.getTotalTimeMillis());

        final ProgressVarianceProjectionResponse response = getProgressVarianceProjection(
            starterProject,
            solutionProject,
            projectsByProjectId
        );

        return convertResponse(response, projectIdToStudentId);
    }

    private List<Project> getProjectsForUser(final int experimentId, final int userId, final int stepMinutes) {
        return codeService.getFilteredJsons(userId, experimentId, stepMinutes, 0, 0, Optional.empty())
            .stream()
            .map(projection -> new Project(projection.getId(), userId, projection.getCode()))
            .sorted(Comparator.comparing(Project::id))
            .toList();
    }

    private ProgressVarianceProjection convertResponse(
        final ProgressVarianceProjectionResponse response,
        final Map<Integer, Integer> projectIdToStudentId
    ) {
        final Map<Integer, List<List<Double>>> datapoints = new HashMap<>();
        for (final Projection projection : response.projections()) {
            final int userId = projectIdToStudentId.get(projection.id());
            datapoints.compute(userId, (k, ps) -> {
                if (ps == null) {
                    ps = new ArrayList<>();
                }
                ps.add(projection.xy());
                return ps;
            });
        }

        final List<DataSeries> data = new ArrayList<>(datapoints.size());
        for (final var entry : datapoints.entrySet()) {
            data.add(new DataSeries(entry.getKey(), entry.getValue()));
        }

        return new ProgressVarianceProjection(data);
    }

    @Nullable
    private String getStarterProject(final int experimentId) throws IOException {
        final byte[] starterProjectSb3 = experimentRepository.getExperimentStarterProject(experimentId);
        if (starterProjectSb3 == null) {
            return null;
        }

        return getProjectJson(starterProjectSb3);
    }

    @Nullable
    private String getSolutionProject(final int experimentId) throws IOException {
        final ExampleSolution solutionSb3 = experimentService.getExampleSolution(experimentId);
        if (solutionSb3 == null) {
            return null;
        }

        return getProjectJson(solutionSb3.getSb3Project());
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
    private ProgressVarianceProjectionResponse getProgressVarianceProjection(
        final String templateProject,
        final String solutionProject,
        final Map<Integer, String> studentProjects
    ) {
        try {
            StopWatch watch = new StopWatch();
            watch.start();
            final var request = buildProgressVarianceProjectionRequest(
                templateProject, solutionProject, studentProjects
            );
            watch.stop();
            log.debug("GGNN preprocessing done in {}ms.", watch.getTotalTimeMillis());

            watch.start();
            var response = restClient.post()
                .uri("ggnn/progress-variance-projection")
                .body(request)
                .retrieve()
                .body(ProgressVarianceProjectionResponse.class);
            watch.stop();
            log.debug("Embedding API request done in {}ms.", watch.getTotalTimeMillis());

            return response;
        } catch (Exception e) {
            // todo: actual error handling
            throw new RuntimeException(e);
        }
    }

    private ProgressVarianceProjectionRequest<GgnnAnalyzerOutput> buildProgressVarianceProjectionRequest(
        final String templateProgramJson,
        final String solutionProgramJson,
        final Map<Integer, String> studentProgramJsons
    ) {
        final var templateProgram = ggnnCache.get(templateProgramJson);
        final var solutionProgram = ggnnCache.get(solutionProgramJson);

        final Map<Integer, String> studentPrograms = studentProgramJsons
            .entrySet()
            .parallelStream()
            .map(entry -> {
                try {
                    final var processedProject = ggnnCache.get(entry.getValue());
                    return new AbstractMap.SimpleImmutableEntry<>(
                        entry.getKey(),
                        jsonMapper.writeValueAsString(processedProject)
                    );
                } catch (RuntimeException e) {
                    // ignore projects we cannot parse -> we cannot compute an embedding in this case
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return new ProgressVarianceProjectionRequest<>(
            jsonMapper.writeValueAsString(templateProgram),
            jsonMapper.writeValueAsString(solutionProgram),
            studentPrograms
        );
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
        String templateProgram,
        String solutionProgram,
        Map<Integer, String> studentPrograms
    ) {
    }

    private record ProgressVarianceProjectionResponse(
        List<Projection> projections
    ) {
    }

    private record Projection(int id, List<Double> xy) {
    }

    public record ProgressVarianceProjection(List<DataSeries> data) {
    }

    /**
     * A series of data.
     *
     * @param userId The user which created the datapoints.
     * @param datapoints The series of datapoints with their x/y coordinates.
     */
    public record DataSeries(int userId, List<List<Double>> datapoints) {
    }

}
