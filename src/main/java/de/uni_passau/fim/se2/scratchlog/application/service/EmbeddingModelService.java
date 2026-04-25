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
import de.uni_passau.fim.se2.scratchlog.spring.configuration.CodeEmbeddingConfiguration;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Profile(Constants.PROFILE_CODE_EMBEDDINGS)
public class EmbeddingModelService {

    private final CodeEmbeddingConfiguration configuration;

    private final RestClient restClient;

    private final WholeProgramJsonProcessor<GgnnAnalyzerOutput> ggnnProgramPreprocessor;

    @Autowired
    public EmbeddingModelService(
        final CodeEmbeddingConfiguration codeEmbeddingConfiguration
    ) {
        this.configuration = codeEmbeddingConfiguration;
        this.restClient = RestClient.create(configuration.getEmbeddingConnectorUrl());

        MLPreprocessorCommonOptions mlOptions = new MLPreprocessorCommonOptions(
            MLOutputPath.console(),
            true,
            true,
            false,
            ActorNameNormalizer.getDefault()
        );
        final GgnnProgramPreprocessor ggnnProgramPreprocessor = new GgnnProgramPreprocessor(
            mlOptions,
            GgnnOutputFormat.JSON_GRAPH,
            "project"
        );
        this.ggnnProgramPreprocessor = new WholeProgramJsonProcessor<>(mlOptions, ggnnProgramPreprocessor);
    }

    /**
     * Returns the progress variance projection for the given projects.
     *
     * @param templateProject The template given to all students at the beginning of a session
     * @param solutionProject The example solution of a session.
     * @param studentProjects Some student projects. The same IDs will be used in the response.
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

        final Map<Integer, WholeProgramOutput<GgnnAnalyzerOutput>> studentPrograms
            = HashMap.newHashMap(studentProgramJsons.size());
        for (final var entry : studentProgramJsons.entrySet()) {
            studentPrograms.put(entry.getKey(), processProgramForGgnn(entry.getValue()));
        }

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
