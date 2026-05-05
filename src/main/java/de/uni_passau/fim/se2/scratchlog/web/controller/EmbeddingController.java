package de.uni_passau.fim.se2.scratchlog.web.controller;

import de.uni_passau.fim.se2.scratchlog.application.service.EmbeddingModelService;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Set;

@RestController
@RequestMapping("/embeddings/")
@Profile(Constants.PROFILE_CODE_EMBEDDINGS)
public class EmbeddingController {

    private final EmbeddingModelService embeddingModelService;

    @Autowired
    public EmbeddingController(final EmbeddingModelService embeddingModelService) {
        this.embeddingModelService = embeddingModelService;
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
    @GetMapping("/progress-variance-projection/all/latest")
    public EmbeddingModelService.ProgramProjection2D getProgressVarianceProjectionAllLatest(
        @RequestParam("experimentId") final int experimentId
    ) throws IOException {
        return embeddingModelService.getProgressVarianceProjectionAllLatest(experimentId);
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
    @GetMapping("/progress-variance-projection/timeline")
    public EmbeddingModelService.ProgramProjection2D getProgressVarianceProjectionOverTime(
        @RequestParam("experimentId") final int experimentId,
        @RequestParam(value = "userIds", defaultValue = "") final Set<Integer> userIds,
        @RequestParam(value = "stepMinutes", defaultValue = "1") final int stepMinutes
    ) throws IOException {
        return embeddingModelService.getProgressVarianceProjectionForUsers(experimentId, userIds, stepMinutes);
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
     * @return For each user a datapoint representing the latest project’s test and embedding distances.
     * @throws IOException In case the solution project cannot be parsed.
     */
    @GetMapping("/embedding-test-distance/all/latest")
    public EmbeddingModelService.ProgramProjection2D getTestVsEmbeddingDistanceAllLatest(
        @RequestParam("experimentId") final int experimentId
    ) throws IOException {
        return embeddingModelService.getEmbeddingVsTestDistanceLatestProjects(experimentId);
    }

}
