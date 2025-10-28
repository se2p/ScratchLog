package de.uni_passau.fim.se2.scratchlog.application;

import de.uni_passau.fim.se2.scratchlog.AbstractScratchLogTest;
import de.uni_passau.fim.se2.scratchlog.application.exception.IncompleteDataException;
import de.uni_passau.fim.se2.scratchlog.application.exception.NotFoundException;
import de.uni_passau.fim.se2.scratchlog.application.service.ExperimentService;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Experiment;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ExperimentRepository;
import de.uni_passau.fim.se2.scratchlog.web.dto.ExperimentDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExperimentServiceTest2 extends AbstractScratchLogTest {

    private static final String BLANK = "    ";

    @Autowired
    private ExperimentService service;

    @Autowired
    private ExperimentRepository experimentRepository;

    private Experiment experiment;

    private ExperimentDTO experimentDTO;

    private int invalidId;

    @BeforeEach
    public void setup() {
        experiment = entityUtilService.generateExperiment("Experiment");
        experimentDTO = dtoUtil.generateExperimentDTO("ExperimentDTO");
        invalidId = experiment.getId() + 50;
    }

    @Test
    public void testExistsExperiment() {
        assertTrue(service.existsExperiment(experiment.getTitle()));
    }

    @Test
    public void testSaveExperimentDoesNotExist() {
        assertFalse(service.existsExperiment("SomeOtherExperiment"));
    }

    // Tests that Experimentservice#existsExperiment returns true when the given ID is not of an existing experiment.
    @Test
    public void testExistsExperimentWithId() {
        assertTrue(service.existsExperiment(experiment.getTitle(), invalidId));
    }

    // Tests that Experimentservice#existsExperiment returns false when the given ID is of an existing experiment.
    @Test
    public void testExistsExperimentWithIdAlreadyExists() {
        assertFalse(service.existsExperiment(experiment.getTitle(), experiment.getId()));
    }

    @Test
    public void testHasProjectFileNoProject() {
        assertFalse(service.hasProjectFile(experiment.getId()));
    }

    @Test
    public void testHasProjectFileAfterUpload() {
        service.uploadSb3Project(experiment.getId(), new byte[]{});
        assertTrue(service.hasProjectFile(experiment.getId()));
    }

    @Test
    public void testHasProjectFileInvalidExperiment() {
        assertFalse(service.hasProjectFile(invalidId));
    }

    // Tests that saving an experiments saves it to the repository and returns a DTO with the inserted data.
    @Test
    public void saveExperimentIdNull() {
        ExperimentDTO saved = service.saveExperiment(experimentDTO);
        assertTrue(service.existsExperiment(experimentDTO.getTitle()));
        assertAll(
            () -> assertEquals(experimentDTO.getTitle(), saved.getTitle()),
            () -> assertEquals(experimentDTO.getDescription(), saved.getDescription()),
            () -> assertEquals(experimentDTO.getInfo(), saved.getInfo()),
            () -> assertEquals(experimentDTO.isActive(), saved.isActive()),
            () -> assertEquals(experimentDTO.isCourseExperiment(), saved.isCourseExperiment()),
            () -> assertEquals(experimentDTO.getGuiURL(), saved.getGuiURL())
        );
    }

    @Test
    public void testSaveExperimentTitleNull() {
        experimentDTO.setTitle(null);
        assertThrows(IncompleteDataException.class,
            () -> service.saveExperiment(experimentDTO)
        );
    }

    @Test
    public void testSaveExperimentTitleBlank() {
        experimentDTO.setTitle(BLANK);
        assertThrows(IncompleteDataException.class,
            () -> service.saveExperiment(experimentDTO)
        );
    }

    @Test
    public void testSaveExperimentDescriptionNull() {
        experimentDTO.setDescription(null);
        assertThrows(IncompleteDataException.class,
            () -> service.saveExperiment(experimentDTO)
        );
    }

    @Test
    public void testSaveExperimentDescriptionBlank() {
        experimentDTO.setDescription(BLANK);
        assertThrows(IncompleteDataException.class,
            () -> service.saveExperiment(experimentDTO)
        );
    }

    @Test
    public void testSaveExperimentGuiURLNull() {
        experimentDTO.setGuiURL(null);
        assertThrows(IncompleteDataException.class,
            () -> service.saveExperiment(experimentDTO)
        );
    }

    @Test
    public void testSaveExperimentGuiURLBlank() {
        experimentDTO.setGuiURL(BLANK);
        assertThrows(IncompleteDataException.class,
            () -> service.saveExperiment(experimentDTO)
        );
    }

    @Test
    public void testGetExperiment() {
        ExperimentDTO found = service.getExperiment(experiment.getId());
        assertTrue(experimentEqualsExperimentDTO(experiment, found));
    }

    @Test
    public void testGetExperimentNoSuchExperiment() {
        assertThrows(NotFoundException.class,
            () -> service.getExperiment(invalidId));
    }

    @Test
    public void testDeleteExperiment() {
        service.deleteExperiment(experiment.getId());
        assertFalse(service.existsExperiment(experiment.getTitle()));
    }

    @Test
    public void testChangeExperimentStatus() {
        ExperimentDTO changedStatus = service.changeExperimentStatus(true, experiment.getId());
        experiment = experimentRepository.findById(experiment.getId()).get(); // Refetch experiment from DB
        assertTrue(changedStatus.isActive());
        assertTrue(experimentEqualsExperimentDTO(experiment, changedStatus));
    }

    @Test
    public void testChangeExperimentStatusFalse() {
        ExperimentDTO changedStatus = service.changeExperimentStatus(false, experiment.getId());
        experiment = experimentRepository.findById(experiment.getId()).get(); // Refetch experiment from DB
        assertFalse(changedStatus.isActive());
        assertTrue(experimentEqualsExperimentDTO(experiment, changedStatus));
    }

    private boolean experimentEqualsExperimentDTO(Experiment experiment, ExperimentDTO experimentDTO) {
        return experimentDTO.getTitle().equals(experiment.getTitle())
            && experimentDTO.getDescription().equals(experiment.getDescription())
            && experimentDTO.getInfo().equals(experiment.getInfo())
            && experimentDTO.isActive() == experiment.isActive()
            && experimentDTO.isCourseExperiment() == experiment.isCourseExperiment()
            && experimentDTO.getGuiURL().equals(experiment.getGuiURL());
    }
}
