package de.uni_passau.fim.se2.scratchlog.application;

import de.uni_passau.fim.se2.scratchlog.AbstractScratchLogTest;
import de.uni_passau.fim.se2.scratchlog.application.exception.NotFoundException;
import de.uni_passau.fim.se2.scratchlog.application.service.ExperimentService;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Experiment;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.ExperimentProjection;
import de.uni_passau.fim.se2.scratchlog.testing_utils.DtoUtil;
import de.uni_passau.fim.se2.scratchlog.web.dto.ExperimentDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExperimentServiceTest extends AbstractScratchLogTest {

    private static final String BLANK = "    ";

    private static final byte[] PROJECT_BYTES = new byte[] {1, 2, 3};

    @Autowired
    private ExperimentService service;

    private Experiment experiment1;

    // experiment1 and experiment2Dto refer to different experiments.
    private ExperimentDTO experiment2Dto;

    private int invalidId;

    @BeforeEach
    public void setup() {
        experiment1 = entityUtilService.generateExperiment("Experiment 1");
        experiment2Dto = DtoUtil.generateExperimentDTO("Experiment 2 DTO");
        invalidId = experiment1.getId() + 50;
    }

    @Test
    public void testExistsExperiment() {
        assertTrue(service.existsExperiment(experiment1.getTitle()));
    }

    @Test
    public void testUpdateExperimentDoesNotExist() {
        assertFalse(service.existsExperiment("SomeOtherExperiment"));
    }

    @Test
    public void testHasProjectFileNoProject() {
        assertFalse(service.hasProjectFile(experiment1.getId()));
    }

    @Test
    public void testHasProjectFileAfterUpload() {
        service.uploadSb3Project(experiment1.getId(), new byte[]{});
        assertTrue(service.hasProjectFile(experiment1.getId()));
    }

    @Test
    public void testHasProjectFileInvalidExperiment() {
        assertFalse(service.hasProjectFile(invalidId));
    }

    // Tests that saving an experiments saves it to the repository and returns a DTO with the inserted data.
    @Test
    public void updateExperimentIdNull() {
        ExperimentDTO saved = service.updateExperiment(experiment2Dto);
        assertTrue(service.existsExperiment(experiment2Dto.getTitle()));
        assertAll(
            () -> assertEquals(experiment2Dto.getTitle(), saved.getTitle()),
            () -> assertEquals(experiment2Dto.getDescription(), saved.getDescription()),
            () -> assertEquals(experiment2Dto.getInfo(), saved.getInfo()),
            () -> assertEquals(experiment2Dto.isActive(), saved.isActive()),
            () -> assertEquals(experiment2Dto.isCourseExperiment(), saved.isCourseExperiment()),
            () -> assertEquals(experiment2Dto.getGuiURL(), saved.getGuiURL())
        );
    }

    @Test
    public void testUpdateExperimentTitleNull() {
        experiment2Dto.setTitle(null);
        assertThrows(IllegalArgumentException.class,
            () -> service.updateExperiment(experiment2Dto)
        );
    }

    @Test
    public void testUpdateExperimentTitleBlank() {
        experiment2Dto.setTitle(BLANK);
        assertThrows(IllegalArgumentException.class,
            () -> service.updateExperiment(experiment2Dto)
        );
    }

    @Test
    public void testUpdateExperimentDescriptionNull() {
        experiment2Dto.setDescription(null);
        assertThrows(IllegalArgumentException.class,
            () -> service.updateExperiment(experiment2Dto)
        );
    }

    @Test
    public void testUpdateExperimentDescriptionBlank() {
        experiment2Dto.setDescription(BLANK);
        assertThrows(IllegalArgumentException.class,
            () -> service.updateExperiment(experiment2Dto)
        );
    }

    @Test
    public void testUpdateExperimentGuiURLNull() {
        experiment2Dto.setGuiURL(null);
        assertThrows(IllegalArgumentException.class,
            () -> service.updateExperiment(experiment2Dto)
        );
    }

    @Test
    public void testUpdateExperimentGuiURLBlank() {
        experiment2Dto.setGuiURL(BLANK);
        assertThrows(IllegalArgumentException.class,
            () -> service.updateExperiment(experiment2Dto)
        );
    }

    @Test
    public void testGetExperiment() {
        ExperimentDTO found = service.getExperiment(experiment1.getId());
        assertAll(
            () -> assertEquals(experiment1.getTitle(), found.getTitle()),
            () -> assertEquals(experiment1.getDescription(), found.getDescription()),
            () -> assertEquals(experiment1.getInfo(), found.getInfo()),
            () -> assertEquals(experiment1.isActive(), found.isActive()),
            () -> assertEquals(experiment1.isCourseExperiment(), found.isCourseExperiment()),
            () -> assertEquals(experiment1.getGuiURL(), found.getGuiURL())
        );
    }

    @Test
    public void testGetExperimentNoSuchExperiment() {
        assertThrows(NotFoundException.class,
            () -> service.getExperiment(invalidId));
    }

    @Test
    public void testDeleteExperiment() {
        service.deleteExperiment(experiment1.getId());
        assertFalse(service.existsExperiment(experiment1.getTitle()));
    }

    @Test
    public void testChangeExperimentStatus() {
        ExperimentDTO changedStatus = service.changeExperimentStatus(true, experiment1.getId());
        assertTrue(changedStatus.isActive());
    }

    @Test
    public void testChangeExperimentStatusFalse() {
        ExperimentDTO changedStatus = service.changeExperimentStatus(false, experiment1.getId());
        assertFalse(changedStatus.isActive());
    }

    @Test
    public void testChangeExperimentStatusNotFound() {
        assertThrows(NotFoundException.class, () -> service.changeExperimentStatus(true, invalidId));
    }

    // The 'correct path' of uploadSb3Project is already covered by testHasProjectFileAfterUpload above.
    @Test
    public void testUploadSb3ProjectNotFound() {
        assertThrows(NotFoundException.class, () -> service.uploadSb3Project(invalidId, PROJECT_BYTES));
    }

    @Test
    public void testUploadSb3ProjectNull() {
        assertThrows(IllegalArgumentException.class, () -> service.uploadSb3Project(experiment1.getId(), null));
    }

    @Test
    public void testDeleteSb3Project() {
        service.uploadSb3Project(experiment1.getId(), PROJECT_BYTES);
        service.deleteSb3Project(experiment1.getId());
        assertFalse(service.hasProjectFile(experiment1.getId()));
    }

    @Test
    public void testDeleteSb3ProjectNotFound() {
        assertThrows(NotFoundException.class, () -> service.deleteSb3Project(invalidId));
    }

    @Test
    public void testGetSb3File() {
        service.changeExperimentStatus(true, experiment1.getId());
        service.uploadSb3Project(experiment1.getId(), PROJECT_BYTES);
        ExperimentProjection experimentProjection = service.getSb3File(experiment1.getId(), false);
        assertArrayEquals(PROJECT_BYTES, experimentProjection.getProject());
    }

    @Test
    public void testGetSb3FileInactive() {
        service.changeExperimentStatus(false, experiment1.getId());
        assertThrows(NotFoundException.class, () -> service.getSb3File(experiment1.getId(), false));
    }

    @Test
    public void testGetSb3FileReturnInactive() {
        service.changeExperimentStatus(false, experiment1.getId());
        service.uploadSb3Project(experiment1.getId(), PROJECT_BYTES);
        ExperimentProjection experimentProjection = service.getSb3File(experiment1.getId(), true);
        assertArrayEquals(PROJECT_BYTES, experimentProjection.getProject());
    }

    @Test
    public void testGetSb3FileNotFound() {
        assertThrows(NotFoundException.class, () -> service.getSb3File(invalidId, true));
    }
}
