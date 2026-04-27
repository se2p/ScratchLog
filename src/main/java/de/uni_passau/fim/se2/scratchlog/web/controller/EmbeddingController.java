package de.uni_passau.fim.se2.scratchlog.web.controller;

import de.uni_passau.fim.se2.scratchlog.application.service.EmbeddingModelService;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(EmbeddingController.class);

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
    public EmbeddingModelService.ProgressVarianceProjection getProgressVarianceProjectionAllLatest(
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
    public EmbeddingModelService.ProgressVarianceProjection getProgressVarianceProjectionOverTime(
        @RequestParam("experimentId") final int experimentId,
        @RequestParam(value = "userIds", defaultValue = "") final Set<Integer> userIds,
        @RequestParam(value = "stepMinutes", defaultValue = "1") final int stepMinutes
    ) throws IOException {
        return embeddingModelService.getProgressVarianceProjectionForUsers(experimentId, userIds, stepMinutes);
    }
}
