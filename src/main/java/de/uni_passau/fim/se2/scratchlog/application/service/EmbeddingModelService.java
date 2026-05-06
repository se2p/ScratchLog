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
import de.uni_passau.fim.se2.litterbox.export.scratchblocks.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.ExampleSolution;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.BlockEventJSONProjection;
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

    private final CodeEmbeddingConfiguration codeEmbeddingConfiguration;

    private final BlockEventRepository blockEventRepository;

    private final ExperimentRepository experimentRepository;

    private final ExperimentService experimentService;

    private final CodeService codeService;

    private final Optional<TestFitnessService> testFitnessService;

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
        final CodeService codeService,
        final Optional<TestFitnessService> testFitnessService
    ) {
        this.codeEmbeddingConfiguration = codeEmbeddingConfiguration;
        this.jsonMapper = jsonMapper;
        this.blockEventRepository = blockEventRepository;
        this.experimentRepository = experimentRepository;
        this.experimentService = experimentService;
        this.codeService = codeService;
        this.testFitnessService = testFitnessService;

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

        ggnnCache = new ConcurrentLruCache<>(CACHE_SIZE, programJson -> {
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
    public ProgramProjection2D getProgressVarianceProjectionAllLatest(
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
    public ProgramProjection2D getProgressVarianceProjectionForUsers(
        final int experimentId,
        final Set<Integer> userIds,
        final int stepMinutes
    ) throws IOException {
        if (userIds.isEmpty()) {
            return new ProgramProjection2D(Collections.emptyList());
        }

        StopWatch watch = new StopWatch();
        watch.start();

        final Map<Integer, String> projectsByProjectId = new HashMap<>();
        final Map<Integer, List<Integer>> projectsByStudent = new HashMap<>();
        for (final int userId : userIds) {
            final List<Project> studentProjects = getProjectsForUser(experimentId, userId, stepMinutes);
            studentProjects.forEach(project -> projectsByProjectId.put(project.id(), project.projectJson()));
            projectsByStudent.put(userId, studentProjects.stream().map(Project::id).toList());
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
        if (response == null) {
            return new ProgramProjection2D(Collections.emptyList());
        }

        return convertPerStudentResponse(response, projectsByStudent);
    }

    private List<Project> getProjectsForUser(final int experimentId, final int userId, final int stepMinutes) {
        return codeService.getFilteredJsons(userId, experimentId, stepMinutes, 0, 0, Optional.empty())
            .stream()
            .sorted(Comparator.comparing(BlockEventJSONProjection::getDate))
            .map(projection -> new Project(projection.getId(), userId, null, projection.getCode()))
            .toList();
    }

    private ProgramProjection2D convertResponse(
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

        return new ProgramProjection2D(data);
    }

    private ProgramProjection2D convertPerStudentResponse(
        final ProgressVarianceProjectionResponse response,
        final Map<Integer, List<Integer>> studentProjects
    ) {
        final Map<Integer, List<Double>> rawDatapointsByProjectId = new HashMap<>();
        for (final Projection projection : response.projections()) {
            rawDatapointsByProjectId.put(projection.id(), projection.xy());
        }

        final Map<Integer, List<List<Double>>> datapoints = new HashMap<>();
        for (final var entry : studentProjects.entrySet()) {
            final List<List<Double>> studentProjections = new ArrayList<>();
            for (final var projectId : entry.getValue()) {
                studentProjections.add(rawDatapointsByProjectId.get(projectId));
            }
            datapoints.put(entry.getKey(), studentProjections);
        }

        final List<DataSeries> data = new ArrayList<>(datapoints.size());
        for (final var entry : datapoints.entrySet()) {
            data.add(new DataSeries(entry.getKey(), entry.getValue()));
        }

        return new ProgramProjection2D(data);
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
        final StopWatch watch = new StopWatch();
        watch.start();
        final var request = buildProgressVarianceProjectionRequest(
            templateProject, solutionProject, studentProjects
        );
        watch.stop();
        log.debug("preprocessing done in {}ms.", watch.getTotalTimeMillis());

        return apiRequest(
            codeEmbeddingConfiguration.getModel() + "/progress-variance-projection",
            request,
            ProgressVarianceProjectionResponse.class
        );
    }

    private ProgressVarianceProjectionRequest buildProgressVarianceProjectionRequest(
        final String templateProgramJson,
        final String solutionProgramJson,
        final Map<Integer, String> studentProgramJsons
    ) {
        final var templateProgram = getProcessedProgram(templateProgramJson);
        final var solutionProgram = getProcessedProgram(solutionProgramJson);

        final Map<Integer, String> studentPrograms = preprocessStudentProgramJsons(studentProgramJsons);

        return new ProgressVarianceProjectionRequest(
            templateProgram,
            solutionProgram,
            studentPrograms
        );
    }

    private Map<Integer, String> preprocessStudentProgramJsons(
        final Map<Integer, String> studentProgramJsons
    ) {
        return studentProgramJsons
            .entrySet()
            .parallelStream()
            .map(entry -> {
                try {
                    final var processedProject = getProcessedProgram(entry.getValue());
                    return new AbstractMap.SimpleImmutableEntry<>(
                        entry.getKey(),
                        processedProject
                    );
                } catch (RuntimeException e) {
                    // ignore projects we cannot parse -> we cannot compute an embedding in this case
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Retrieves the embedding distances between latest student projects and the example solution.
     *
     * @param experimentId Some experiment.
     * @return A mapping of user ID to embedding distance in range {@code [0, 1]}.
     * @throws IOException In case the solution project cannot be parsed.
     */
    public Map<Integer, Double> getEmbeddingDistancesAllLatest(final int experimentId) throws IOException {
        final StopWatch watch = new StopWatch();
        watch.start();

        final List<Project> projects = blockEventRepository.findLastPerUserInExperiment(experimentId);

        final Map<Integer, Integer> projectIdToUserId = new HashMap<>();
        projects.forEach(project -> projectIdToUserId.put(project.id(), project.userId()));

        final Map<Integer, Double> distancesByProjectId = getEmbeddingDistances(experimentId, projects);

        final Map<Integer, Double> distancesByUserId = new HashMap<>();
        distancesByProjectId.forEach((projectId, distance) -> {
            final int userId = projectIdToUserId.get(projectId);
            distancesByUserId.put(userId, distance);
        });

        return distancesByUserId;
    }

    /**
     * Retrieves the embedding distances between the given projects and the example solution.
     *
     * @param experimentId Some experiment.
     * @param projects Some projects in the experiment.
     * @return A mapping of project ID to embedding distance in range {@code [0, 1]}.
     * @throws IOException In case the solution project cannot be parsed.
     */
    public Map<Integer, Double> getEmbeddingDistances(
        final int experimentId, final List<Project> projects
    ) throws IOException {
        final StopWatch watch = new StopWatch();
        watch.start();

        final Map<Integer, String> projectsById = new HashMap<>();
        projects.forEach(project -> projectsById.put(project.id(), project.projectJson()));

        final var solutionProject = getSolutionProject(experimentId);
        if (solutionProject == null) {
            throw new IllegalArgumentException(
                "Embedding distance can only be computed if solution projects is given."
            );
        }
        watch.stop();
        log.debug("Database fetching finished in {}ms.", watch.getTotalTimeMillis());

        final EmbeddingDistanceResponse response = getEmbeddingDistances(
            solutionProject,
            projectsById
        );
        if (response == null) {
            return Collections.emptyMap();
        }

        final Map<Integer, Double> distances = new HashMap<>();
        for (final var distance : response.distances()) {
            distances.put(distance.id(), distance.d());
        }
        return distances;
    }

    private EmbeddingDistanceResponse getEmbeddingDistances(
        final String solutionProject,
        final Map<Integer, String> studentProjects
    ) {
        final StopWatch watch = new StopWatch();
        watch.start();
        final var request = buildEmbeddingDistanceRequest(
            solutionProject, studentProjects
        );
        watch.stop();
        log.debug("GGNN preprocessing done in {}ms.", watch.getTotalTimeMillis());

        return apiRequest(
            codeEmbeddingConfiguration.getModel() + "/embedding-distance",
            request,
            EmbeddingDistanceResponse.class
        );
    }

    private EmbeddingDistanceRequest buildEmbeddingDistanceRequest(
        final String solutionProgramJson,
        final Map<Integer, String> studentProgramJsons
    ) {
        final var solutionProgram = getProcessedProgram(solutionProgramJson);
        final Map<Integer, String> studentPrograms = preprocessStudentProgramJsons(studentProgramJsons);

        return new EmbeddingDistanceRequest(solutionProgram, studentPrograms);
    }

    /**
     * Fetches the embedding and test distances for the latest project of all users in the experiment.
     *
     * <p>The resulting datapoints will have the test distance on the x-Axis (0th list element) and the embedding
     * distance as y-Axis (1st element).
     *
     * <p>Requires the {@link Constants#PROFILE_WHISKER} profile to be active.
     *
     * @param experimentId Some experiment.
     * @return The embedding/test-distance projection for the latest projects of all users.
     * @throws IllegalStateException In case the {@link Constants#PROFILE_WHISKER} profile is not active.
     */
    public ProgramProjection2D getEmbeddingVsTestDistanceLatestProjects(final int experimentId) throws IOException {
        if (testFitnessService.isEmpty()) {
            throw new IllegalStateException(
                "Missing the Whisker test data. Cannot compute embeding/test distance projection."
            );
        }

        final List<Project> latestProjects = blockEventRepository.findLastPerUserInExperiment(experimentId);

        final Map<Integer, Double> embeddingDistances = getEmbeddingDistances(experimentId, latestProjects);
        final Map<Integer, Double> testFitnesses = testFitnessService.orElseThrow().getTestFitnesses(
            latestProjects.stream().map(Project::id).collect(Collectors.toSet())
        );

        final List<DataSeries> data = new ArrayList<>();
        for (final Project project : latestProjects) {
            final double embeddingDistance = embeddingDistances.getOrDefault(project.id(), 1.0);
            final double testFitness = testFitnesses.getOrDefault(project.id(), 0.0);
            final List<Double> datapoint = List.of(testFitness, 1 - embeddingDistance);

            data.add(new DataSeries(project.userId(), List.of(datapoint)));
        }

        return new ProgramProjection2D(data);
    }

    private WholeProgramOutput<GgnnAnalyzerOutput> processProgramForGgnn(final Program program) {
        return ggnnProgramPreprocessor.processWholeProgram(program)
            .findFirst()
            .orElseThrow();
    }

    private String getProcessedProgram(final String programJson) {
        try {
            return switch (codeEmbeddingConfiguration.getModel()) {
                case "ggnn" -> jsonMapper.writeValueAsString(ggnnCache.get(programJson));
                case "llm" -> processProgramForLlm(programJson);
                default -> throw new IllegalStateException("Unknown model: " + codeEmbeddingConfiguration.getModel());
            };
        } catch (ParsingException e) {
            throw new RuntimeException(e);
        }
    }

    private WholeProgramOutput<GgnnAnalyzerOutput> processProgramForGgnn(
        final String programJson
    ) throws ParsingException {
        final Program program = parseProgram(programJson);
        return processProgramForGgnn(program);
    }

    private String processProgramForLlm(final String programJson) throws ParsingException {
        final Program program = parseProgram(programJson);
        return processProgramForLlm(program);
    }

    private String processProgramForLlm(final Program program) {
        return ScratchBlocksVisitor.of(program);
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

    private <B, R> R apiRequest(final String path, final B body, final Class<R> responseType) {
        final StopWatch watch = new StopWatch();

        watch.start();
        var response = restClient.post()
            .uri(path)
            .body(body)
            .retrieve()
            .body(responseType);
        watch.stop();
        log.debug("Embedding API request done in {}ms.", watch.getTotalTimeMillis());

        return response;
    }

    public record ProgressVarianceProjectionRequest(
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

    public record ProgramProjection2D(List<DataSeries> data) {
    }

    /**
     * A series of data.
     *
     * @param userId The user which created the datapoints.
     * @param datapoints The series of datapoints with their x/y coordinates.
     */
    public record DataSeries(int userId, List<List<Double>> datapoints) {
    }

    private record EmbeddingDistanceRequest(
        String solutionProgram,
        Map<Integer, String> studentPrograms
    ) {
    }

    private record EmbeddingDistanceResponse(List<Distance> distances) {
    }

    private record Distance(int id, double d) {
    }

}
