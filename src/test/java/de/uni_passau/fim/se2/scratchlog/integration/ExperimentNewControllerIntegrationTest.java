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
        assertEquals("example-solution.sb3", experimentService.getExampleSolutionName(experiment.getId()));
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
        assertEquals("example-solution-2.sb3", experimentService.getExampleSolutionName(experiment.getId()));
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
