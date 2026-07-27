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

package de.uni_passau.fim.se2.scratchlog.integration;

import de.uni_passau.fim.se2.scratchlog.AbstractScratchLogControllerTest;
import de.uni_passau.fim.se2.scratchlog.application.service.ExperimentService;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Experiment;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ExampleSolutionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class ExperimentNewControllerIntegrationTest extends AbstractScratchLogControllerTest {

    @Autowired
    private ExampleSolutionRepository exampleSolutionRepository;

    @Autowired
    private ExperimentService experimentService;

    private Experiment experiment;

    @BeforeEach
    void setUp() {
        experiment = entityUtilService.generateExperiment("Experiment Controller Integration Test");
    }

    @Test
    void uploadExampleSolution() throws Exception {
        mvc.perform(multipart("/experiment/example-solution/upload")
                .param("id", experiment.getId().toString())
                .file(sb3Dto("example-solution.sb3"))
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/experiment?id=" + experiment.getId()));

        assertEquals(1, exampleSolutionRepository.findExampleSolutionsByExperiment_Id(experiment.getId()).size());
        assertEquals("example-solution.sb3", experimentService.getExampleSolution(experiment.getId()).getFilename());
    }

    @Test
    void uploadExampleSolutionTwiceShouldUpdateName() throws Exception {
        mvc.perform(
            multipart("/experiment/example-solution/upload")
                .param("id", experiment.getId().toString())
                .file(sb3Dto("example-solution.sb3"))
        );
        mvc.perform(
            multipart("/experiment/example-solution/upload")
                .param("id", experiment.getId().toString())
                .file(sb3Dto("example-solution-2.sb3"))
        );

        assertEquals(1, exampleSolutionRepository.findExampleSolutionsByExperiment_Id(experiment.getId()).size());
        assertEquals("example-solution-2.sb3", experimentService.getExampleSolution(experiment.getId()).getFilename());
    }

    @Test
    void deleteExampleSolution() throws Exception {
        mvc.perform(multipart("/experiment/example-solution/upload")
                .param("id", experiment.getId().toString())
                .file(sb3Dto("example-solution.sb3"))
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/experiment?id=" + experiment.getId()));

        assertEquals(1, exampleSolutionRepository.findExampleSolutionsByExperiment_Id(experiment.getId()).size());

        mvc.perform(
            get("/experiment/example-solution/delete")
            .param("id", experiment.getId().toString())
        );

        assertEquals(0, exampleSolutionRepository.findExampleSolutionsByExperiment_Id(experiment.getId()).size());
    }

    private MockMultipartFile sb3Dto(final String name) {
        return new MockMultipartFile(
            "file",
            name,
            "application/octet-stream",
            new byte[]{1, 2, 3}
        );
    }
}
