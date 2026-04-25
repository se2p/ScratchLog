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

    // todo: p-v-projection for a subset of users and over time

}
