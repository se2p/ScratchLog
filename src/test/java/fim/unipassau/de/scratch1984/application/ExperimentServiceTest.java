package fim.unipassau.de.scratch1984.application;

import fim.unipassau.de.scratch1984.application.exception.IncompleteDataException;
import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.exception.StoreException;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratch1984.web.dto.ExperimentDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExperimentServiceTest {

    @InjectMocks
    private ExperimentService experimentService;

    @Mock
    private ExperimentRepository experimentRepository;

    private static final String TITLE = "My Experiment";
    private static final String DESCRIPTION = "A description";
    private static final String BLANK = "    ";
    private static final int ID = 1;
    private static final int INVALID_ID = 2;
    private final Experiment experiment = new Experiment(ID, TITLE, DESCRIPTION, "Some info text", false);
    private final ExperimentDTO experimentDTO = new ExperimentDTO(ID, TITLE, DESCRIPTION, "Some info text", false);


    @BeforeEach
    public void setup() {
        experimentDTO.setTitle(TITLE);
        experimentDTO.setDescription(DESCRIPTION);
    }

    @Test
    public void testExistsExperiment() {
        when(experimentRepository.existsByTitle(TITLE)).thenReturn(true);
        assertTrue(experimentService.existsExperiment(TITLE));
        verify(experimentRepository).existsByTitle(TITLE);
    }

    @Test
    public void testExistsExperimentFalse() {
        assertFalse(experimentService.existsExperiment(TITLE));
        verify(experimentRepository).existsByTitle(TITLE);
    }

    @Test
    public void testExistsExperimentTitleNull() {
        assertFalse(experimentService.existsExperiment(null));
        verify(experimentRepository, never()).existsByTitle(TITLE);
    }

    @Test
    public void testExistsExperimentTitleBlank() {
        assertFalse(experimentService.existsExperiment(BLANK));
        verify(experimentRepository, never()).existsByTitle(TITLE);
    }

    @Test
    public void testExistsExperimentWithId() {
        when(experimentRepository.findByTitle(TITLE)).thenReturn(experiment);
        assertTrue(experimentService.existsExperiment(TITLE, INVALID_ID));
        verify(experimentRepository).findByTitle(TITLE);
    }

    @Test
    public void testExistsExperimentWithIdFalse() {
        when(experimentRepository.findByTitle(TITLE)).thenReturn(experiment);
        assertFalse(experimentService.existsExperiment(TITLE, experiment.getId()));
        verify(experimentRepository).findByTitle(TITLE);
    }

    @Test
    public void testExistsExperimentWithIdTitleNull() {
        assertFalse(experimentService.existsExperiment(null, ID));
        verify(experimentRepository, never()).findByTitle(TITLE);
    }

    @Test
    public void testExistsExperimentWithIdTitleBlank() {
        assertFalse(experimentService.existsExperiment(BLANK, ID));
        verify(experimentRepository, never()).findByTitle(TITLE);
    }

    @Test
    public void testExistsExperimentWithIdInvalid() {
        assertFalse(experimentService.existsExperiment(TITLE, 0));
        verify(experimentRepository, never()).findByTitle(TITLE);
    }

    @Test
    public void testSaveExperiment() {
        when(experimentRepository.save(any())).thenReturn(experiment);
        ExperimentDTO saved = experimentService.saveExperiment(experimentDTO);
        assertAll(
                () -> assertEquals(experimentDTO.getId(), saved.getId()),
                () -> assertEquals(experimentDTO.getTitle(), saved.getTitle()),
                () -> assertEquals(experimentDTO.getDescription(), saved.getDescription()),
                () -> assertEquals(experimentDTO.getInfo(), saved.getInfo()),
                () -> assertFalse(experimentDTO.isActive())
        );
        verify(experimentRepository).save(any());
    }

    @Test
    public void testSaveExperimentNotSaved() {
        Experiment emptyExperiment = new Experiment();
        when(experimentRepository.save(any())).thenReturn(emptyExperiment);
        assertThrows(StoreException.class,
                () -> experimentService.saveExperiment(experimentDTO)
        );
        verify(experimentRepository).save(any());
    }

    @Test
    public void testSaveExperimentTitleNull() {
        experimentDTO.setTitle(null);
        assertThrows(IncompleteDataException.class,
                () -> experimentService.saveExperiment(experimentDTO)
        );
        verify(experimentRepository, never()).save(any());
    }

    @Test
    public void testSaveExperimentTitleBlank() {
        experimentDTO.setTitle(BLANK);
        assertThrows(IncompleteDataException.class,
                () -> experimentService.saveExperiment(experimentDTO)
        );
        verify(experimentRepository, never()).save(any());
    }

    @Test
    public void testSaveExperimentDescriptionNull() {
        experimentDTO.setDescription(null);
        assertThrows(IncompleteDataException.class,
                () -> experimentService.saveExperiment(experimentDTO)
        );
        verify(experimentRepository, never()).save(any());
    }

    @Test
    public void testSaveExperimentDescriptionBlank() {
        experimentDTO.setDescription(BLANK);
        assertThrows(IncompleteDataException.class,
                () -> experimentService.saveExperiment(experimentDTO)
        );
        verify(experimentRepository, never()).save(any());
    }

    @Test
    public void testGetExperiment() {
        when(experimentRepository.findById(ID)).thenReturn(experiment);
        ExperimentDTO found = experimentService.getExperiment(ID);
        assertAll(
                () -> assertEquals(experiment.getId(), found.getId()),
                () -> assertEquals(experiment.getTitle(), found.getTitle()),
                () -> assertEquals(experiment.getDescription(), found.getDescription()),
                () -> assertEquals(experiment.getInfo(), found.getInfo()),
                () -> assertFalse(found.isActive())
        );
        verify(experimentRepository).findById(ID);
    }

    @Test
    public void testGetExperimentNull() {
        assertThrows(NotFoundException.class,
                () -> experimentService.getExperiment(INVALID_ID)
        );
        verify(experimentRepository).findById(INVALID_ID);
    }

    @Test
    public void testGetExperimentIdInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> experimentService.getExperiment(0)
        );
        verify(experimentRepository, never()).findById(anyInt());
    }

    @Test
    public void testDeleteExperiment() {
        experimentService.deleteExperiment(ID);
        verify(experimentRepository).deleteById(ID);
    }

    @Test
    public void testDeleteExperimentIdInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> experimentService.deleteExperiment(0)
        );
        verify(experimentRepository, never()).deleteById(anyInt());
    }
}
