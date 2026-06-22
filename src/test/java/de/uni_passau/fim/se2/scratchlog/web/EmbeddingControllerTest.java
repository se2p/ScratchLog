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
