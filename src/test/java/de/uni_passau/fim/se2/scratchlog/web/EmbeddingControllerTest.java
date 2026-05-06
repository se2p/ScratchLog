package de.uni_passau.fim.se2.scratchlog.web;

import de.uni_passau.fim.se2.scratchlog.AbstractScratchLogTest;
import de.uni_passau.fim.se2.scratchlog.application.service.EmbeddingModelService;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Experiment;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.testing_utils.RequestUtilService;
import de.uni_passau.fim.se2.scratchlog.util.enums.BlockEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.BlockEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmbeddingControllerTest extends AbstractScratchLogTest {

    @Autowired
    private RequestUtilService requestUtilService;

    private User user1;
    private User user2;
    private User user3;
    private Experiment experiment;

    @BeforeEach
    void setUp() {
        experiment = entityUtilService.generateExperimentWithStarterProjectExampleSolutionAndTests("EmbeddingController", 10);
        user1 = entityUtilService.generateUser("u1");
        user2 = entityUtilService.generateUser("u2");
        user3 = entityUtilService.generateUser("u3");
        entityUtilService.addUsersToExperiment(experiment, user1, user2, user3);
    }

    @Test
    void fetchPvpEmptyExperiment() {
        final var response = requestUtilService.get(
            "/embeddings/progress-variance-projection/all/latest",
            EmbeddingModelService.ProgramProjection2D.class,
            HttpStatus.OK,
            Map.of("experimentId", Integer.toString(experiment.getId()))
        );
        assertEquals(Collections.emptyList(), response.data());
    }

    @Test
    void fetchPvpLatestEmbeddings() {
        final var event1 = eventUtilService.generateBlockEventWithCode(user1, experiment, BlockEventType.CREATE, BlockEventSpecific.DELETE);
        final var event2 = eventUtilService.generateBlockEventWithCode(user2, experiment, BlockEventType.MOVE, BlockEventSpecific.MOVE);
        eventUtilService.generateBlockEvent(user3, experiment, BlockEventType.MOVE, BlockEventSpecific.MOVE);

        enqueueMockWebServerJsonResponse(new EmbeddingModelService.ProgressVarianceProjectionResponse(
            List.of(
                new EmbeddingModelService.Projection(event1.getId(), List.of(0d, 1d)),
                new EmbeddingModelService.Projection(event2.getId(), List.of(0d, 2d))
            )
        ));

        final var response = requestUtilService.get(
            "/embeddings/progress-variance-projection/all/latest",
            EmbeddingModelService.ProgramProjection2D.class,
            HttpStatus.OK,
            Map.of("experimentId", Integer.toString(experiment.getId()))
        );
        assertEquals(
            List.of(
                new EmbeddingModelService.DataSeries(user1.getId(), List.of(List.of(0d, 1d))),
                new EmbeddingModelService.DataSeries(user2.getId(), List.of(List.of(0d, 2d)))
            ),
            response.data());
    }
}
