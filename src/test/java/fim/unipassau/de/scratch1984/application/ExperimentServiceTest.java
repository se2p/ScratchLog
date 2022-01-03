package fim.unipassau.de.scratch1984.application;

import fim.unipassau.de.scratch1984.application.exception.IncompleteDataException;
import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.exception.StoreException;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.ExperimentData;
import fim.unipassau.de.scratch1984.persistence.projection.ExperimentProjection;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentDataRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.web.dto.ExperimentDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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

    @Mock
    private ExperimentDataRepository experimentDataRepository;

    private static final String TITLE = "My Experiment";
    private static final String DESCRIPTION = "A description";
    private static final String BLANK = "    ";
    private static final int ID = 1;
    private static final int INVALID_ID = 2;
    private static final String[] HEADER = {"experiment", "participants", "started", "finished"};
    private static final byte[] CONTENT = new byte[]{1, 2, 3};
    private final Experiment experiment = new Experiment(ID, TITLE, DESCRIPTION, "Some info text", "Some postscript", false);
    private final ExperimentDTO experimentDTO = new ExperimentDTO(ID, TITLE, DESCRIPTION, "Some info text", "Some postscript", false);
    private final ExperimentData experimentData = new ExperimentData(ID, 5, 3, 2);
    private final PageRequest pageRequest = PageRequest.of(0, Constants.PAGE_SIZE);
    private Page<Experiment> experimentPage;
    private final ExperimentProjection projection = new ExperimentProjection() {
        @Override
        public Integer getId() {
            return ID;
        }

        @Override
        public byte[] getProject() {
            return CONTENT;
        }
    };

    @BeforeEach
    public void setup() {
        experimentDTO.setId(ID);
        experimentDTO.setTitle(TITLE);
        experimentDTO.setDescription(DESCRIPTION);
        experiment.setActive(false);
        experiment.setProject(null);
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
    public void testExistsExperimentNull() {
        when(experimentRepository.findByTitle(TITLE)).thenReturn(null);
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
    public void testHasProjectFile() {
        when(experimentRepository.existsByIdAndProjectIsNotNull(ID)).thenReturn(true);
        assertTrue(experimentService.hasProjectFile(ID));
        verify(experimentRepository).existsByIdAndProjectIsNotNull(ID);
    }

    @Test
    public void testHasProjectFileFalse() {
        assertFalse(experimentService.hasProjectFile(ID));
        verify(experimentRepository).existsByIdAndProjectIsNotNull(ID);
    }

    @Test
    public void testHasProjectFileInvalidId() {
        assertFalse(experimentService.hasProjectFile(0));
        verify(experimentRepository, never()).existsByIdAndProjectIsNotNull(anyInt());
    }

    @Test
    public void testSaveExperimentIdNull() {
        experimentDTO.setId(null);
        when(experimentRepository.save(any())).thenReturn(experiment);
        ExperimentDTO saved = experimentService.saveExperiment(experimentDTO);
        assertAll(
                () -> assertEquals(ID, saved.getId()),
                () -> assertEquals(experimentDTO.getTitle(), saved.getTitle()),
                () -> assertEquals(experimentDTO.getDescription(), saved.getDescription()),
                () -> assertEquals(experimentDTO.getInfo(), saved.getInfo()),
                () -> assertFalse(experimentDTO.isActive())
        );
        verify(experimentRepository).save(any());
        verify(experimentRepository, never()).findById(anyInt());
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
    public void testGetExperimentPage() {
        List<Experiment> experiments = getExperiments(5);
        experimentPage = new PageImpl<>(experiments);
        when(experimentRepository.findAll(any(PageRequest.class))).thenReturn(experimentPage);
        Page<Experiment> getPage = experimentService.getExperimentPage(pageRequest);
        assertAll(
                () -> assertEquals(experimentPage.getTotalElements(), getPage.getTotalElements()),
                () -> assertEquals(experimentPage.stream().findFirst(), getPage.stream().findFirst()),
                () -> assertEquals(experimentPage.getSize(), getPage.getSize())
        );
        verify(experimentRepository).findAll(any(PageRequest.class));
    }

    @Test
    public void testGetExperimentPageEmpty() {
        experimentPage = new PageImpl<>(new ArrayList<>());
        when(experimentRepository.findAll(any(PageRequest.class))).thenReturn(experimentPage);
        Page<Experiment> getPage = experimentService.getExperimentPage(pageRequest);
        assertTrue(getPage.isEmpty());
        verify(experimentRepository).findAll(any(PageRequest.class));
    }

    @Test
    public void testGetExperimentPageWrongPageSize() {
        PageRequest wrongPageSize = PageRequest.of(0, 20);
        assertThrows(IllegalArgumentException.class,
                () -> experimentService.getExperimentPage(wrongPageSize)
        );
        verify(experimentRepository, never()).findAll(any(PageRequest.class));
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

    @Test
    public void getLastPage() {
        when(experimentRepository.count()).thenReturn((long) Constants.PAGE_SIZE);
        assertEquals(0, experimentService.getLastPage());
        verify(experimentRepository).count();
    }

    @Test
    public void getLastPage4() {
        when(experimentRepository.count()).thenReturn((long) 50);
        assertEquals(4, experimentService.getLastPage());
        verify(experimentRepository).count();
    }

    @Test
    public void getLastPage5() {
        when(experimentRepository.count()).thenReturn((long) 51);
        assertEquals(5, experimentService.getLastPage());
        verify(experimentRepository).count();
    }

    @Test
    public void testGetLastPageTooManyRows() {
        when(experimentRepository.count()).thenReturn(Long.MAX_VALUE);
        assertEquals(214748364, experimentService.getLastPage());
        verify(experimentRepository).count();
    }

    @Test
    public void testGetLastParticipantPage() {
        when(experimentDataRepository.findByExperiment(ID)).thenReturn(experimentData);
        assertEquals(0, experimentService.getLastParticipantPage(ID));
        verify(experimentDataRepository).findByExperiment(ID);
    }

    @Test
    public void testGetLastParticipantPage3() {
        experimentData.setParticipants(40);
        when(experimentDataRepository.findByExperiment(ID)).thenReturn(experimentData);
        assertEquals(3, experimentService.getLastParticipantPage(ID));
        verify(experimentDataRepository).findByExperiment(ID);
    }

    @Test
    public void testGetLastParticipantPage4() {
        experimentData.setParticipants(41);
        when(experimentDataRepository.findByExperiment(ID)).thenReturn(experimentData);
        assertEquals(4, experimentService.getLastParticipantPage(ID));
        verify(experimentDataRepository).findByExperiment(ID);
    }

    @Test
    public void testGetLastParticipantPageNull() {
        assertEquals(0, experimentService.getLastParticipantPage(ID));
        verify(experimentDataRepository).findByExperiment(ID);
    }

    @Test
    public void testChangeExperimentStatus() {
        experiment.setActive(true);
        when(experimentRepository.existsById(ID)).thenReturn(true);
        when(experimentRepository.findById(ID)).thenReturn(experiment);
        ExperimentDTO changedStatus = experimentService.changeExperimentStatus(true, ID);
        assertAll(
                () -> assertEquals(experiment.getId(), changedStatus.getId()),
                () -> assertEquals(experiment.getTitle(), changedStatus.getTitle()),
                () -> assertEquals(experiment.getDescription(), changedStatus.getDescription()),
                () -> assertEquals(experiment.getInfo(), changedStatus.getInfo()),
                () -> assertTrue(changedStatus.isActive())
        );
        verify(experimentRepository).existsById(ID);
        verify(experimentRepository).updateStatusById(ID, true);
        verify(experimentRepository).findById(ID);
    }

    @Test
    public void testChangeExperimentStatusFalse() {
        when(experimentRepository.existsById(ID)).thenReturn(true);
        when(experimentRepository.findById(ID)).thenReturn(experiment);
        ExperimentDTO changedStatus = experimentService.changeExperimentStatus(false, ID);
        assertAll(
                () -> assertEquals(experiment.getId(), changedStatus.getId()),
                () -> assertEquals(experiment.getTitle(), changedStatus.getTitle()),
                () -> assertEquals(experiment.getDescription(), changedStatus.getDescription()),
                () -> assertEquals(experiment.getInfo(), changedStatus.getInfo()),
                () -> assertFalse(changedStatus.isActive())
        );
        verify(experimentRepository).existsById(ID);
        verify(experimentRepository).updateStatusById(ID, false);
        verify(experimentRepository).findById(ID);
    }

    @Test
    public void testChangeExperimentStatusNotFound() {
        assertThrows(NotFoundException.class, () -> experimentService.changeExperimentStatus(true, ID));
        verify(experimentRepository).existsById(ID);
        verify(experimentRepository, never()).updateStatusById(ID, true);
        verify(experimentRepository, never()).findById(ID);
    }

    @Test
    public void testGetExperimentData() {
        String[] dataArray = {experimentData.getExperiment().toString(),
                String.valueOf(experimentData.getParticipants()), String.valueOf(experimentData.getStarted()),
                String.valueOf(experimentData.getFinished())};
        when(experimentDataRepository.findByExperiment(ID)).thenReturn(experimentData);
        List<String[]> data = experimentService.getExperimentData(ID);
        assertAll(
                () -> assertEquals(2, data.size()),
                () -> assertEquals(Arrays.toString(HEADER), Arrays.toString(data.get(0))),
                () -> assertEquals(Arrays.toString(dataArray), Arrays.toString(data.get(1)))
        );
        verify(experimentDataRepository).findByExperiment(ID);
    }

    @Test
    public void testGetExperimentDataNull() {
        List<String[]> data = experimentService.getExperimentData(ID);
        assertAll(
                () -> assertEquals(1, data.size()),
                () -> assertEquals(Arrays.toString(HEADER), Arrays.toString(data.get(0)))
        );
        verify(experimentDataRepository).findByExperiment(ID);
    }

    @Test
    public void testGetExperimentDataInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> experimentService.getExperimentData(0)
        );
        verify(experimentDataRepository, never()).findByExperiment(anyInt());
    }

    @Test
    public void testUploadSb3Project() {
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        assertDoesNotThrow(() -> experimentService.uploadSb3Project(ID, CONTENT));
        verify(experimentRepository).getOne(ID);
        verify(experimentRepository).save(any());
    }

    @Test
    public void testUploadSb3ProjectEntityNotFound() {
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(experimentRepository.save(any())).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> experimentService.uploadSb3Project(ID, CONTENT)
        );
        verify(experimentRepository).getOne(ID);
        verify(experimentRepository).save(any());
    }

    @Test
    public void testUploadSb3ProjectInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> experimentService.uploadSb3Project(0, CONTENT)
        );
        verify(experimentRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).save(any());
    }

    @Test
    public void testUploadSb3ProjectIdNull() {
        assertThrows(IllegalArgumentException.class,
                () -> experimentService.uploadSb3Project(ID, null)
        );
        verify(experimentRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).save(any());
    }

    @Test
    public void testDeleteSb3Project() {
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        assertDoesNotThrow(() -> experimentService.deleteSb3Project(ID));
        verify(experimentRepository).getOne(ID);
        verify(experimentRepository).save(any());
    }

    @Test
    public void testDeleteSb3ProjectEntityNotFound() {
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(experimentRepository.save(any())).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> experimentService.deleteSb3Project(ID)
        );
        verify(experimentRepository).getOne(ID);
        verify(experimentRepository).save(any());
    }

    @Test
    public void testDeleteSb3ProjectInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> experimentService.deleteSb3Project(-1)
        );
        verify(experimentRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).save(any());
    }

    @Test
    public void testGetSb3File() {
        when(experimentRepository.findExperimentById(ID)).thenReturn(java.util.Optional.of(projection));
        ExperimentProjection experimentProjection = experimentService.getSb3File(ID);
        assertAll(
                () -> assertEquals(ID, experimentProjection.getId()),
                () -> assertEquals(CONTENT, experimentProjection.getProject())
        );
        verify(experimentRepository).findExperimentById(ID);
    }

    @Test
    public void testGetSb3FileEmpty() {
        when(experimentRepository.findExperimentById(ID)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> experimentService.getSb3File(ID)
        );
        verify(experimentRepository).findExperimentById(ID);
    }

    @Test
    public void testGetSb3FileInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> experimentService.getSb3File(0)
        );
        verify(experimentRepository, never()).findExperimentById(ID);
    }

    private List<Experiment> getExperiments(int number) {
        List<Experiment> experiments = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            experiments.add(new Experiment(i, "Experiment " + i, "Description for experiment " + i, "", "", false));
        }
        return experiments;
    }
}
