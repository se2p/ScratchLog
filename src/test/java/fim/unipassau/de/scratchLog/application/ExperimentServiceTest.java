/*
 * Copyright (C) 2023 ScratchLog contributors
 *
 * This file is part of ScratchLog.
 *
 * ScratchLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * ScratchLog is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ScratchLog. If not, see <http://www.gnu.org/licenses/>.
 */
package fim.unipassau.de.scratchLog.application;

import fim.unipassau.de.scratchLog.application.exception.IncompleteDataException;
import fim.unipassau.de.scratchLog.application.exception.NotFoundException;
import fim.unipassau.de.scratchLog.application.exception.StoreException;
import fim.unipassau.de.scratchLog.application.service.ExperimentService;
import fim.unipassau.de.scratchLog.persistence.entity.Experiment;
import fim.unipassau.de.scratchLog.persistence.entity.ExperimentData;
import fim.unipassau.de.scratchLog.persistence.projection.ExperimentProjection;
import fim.unipassau.de.scratchLog.persistence.repository.ExperimentDataRepository;
import fim.unipassau.de.scratchLog.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratchLog.web.dto.ExperimentDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private static final String GUI_URL = "scratch";
    private final Experiment experiment = new Experiment(ID, TITLE, DESCRIPTION, "Some info text", "Some postscript",
            false, true, GUI_URL);
    private final ExperimentDTO experimentDTO = new ExperimentDTO(ID, TITLE, DESCRIPTION, "Some info text",
            "Some postscript", false, true, GUI_URL);
    private final ExperimentData experimentData = new ExperimentData(ID, 5, 3, 2);
    private final ExperimentProjection projection = new ExperimentProjection() {
        @Override
        public Integer getId() {
            return ID;
        }

        @Override
        public boolean isActive() {
            return true;
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
        experimentDTO.setGuiURL(GUI_URL);
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
        assertThrows(IllegalArgumentException.class,
                () -> experimentService.existsExperiment(null)
        );
        verify(experimentRepository, never()).existsByTitle(TITLE);
    }

    @Test
    public void testExistsExperimentTitleBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> experimentService.existsExperiment(BLANK)
        );
        verify(experimentRepository, never()).existsByTitle(TITLE);
    }

    @Test
    public void testExistsExperimentWithId() {
        when(experimentRepository.findByTitle(TITLE)).thenReturn(Optional.of(experiment));
        assertTrue(experimentService.existsExperiment(TITLE, INVALID_ID));
        verify(experimentRepository).findByTitle(TITLE);
    }

    @Test
    public void testExistsExperimentWithIdFalse() {
        when(experimentRepository.findByTitle(TITLE)).thenReturn(Optional.of(experiment));
        assertFalse(experimentService.existsExperiment(TITLE, experiment.getId()));
        verify(experimentRepository).findByTitle(TITLE);
    }

    @Test
    public void testExistsExperimentNull() {
        when(experimentRepository.findByTitle(TITLE)).thenReturn(Optional.empty());
        assertFalse(experimentService.existsExperiment(TITLE, experiment.getId()));
        verify(experimentRepository).findByTitle(TITLE);
    }

    @Test
    public void testExistsExperimentWithIdTitleNull() {
        assertThrows(IllegalArgumentException.class,
                () -> experimentService.existsExperiment(null, ID)
        );
        verify(experimentRepository, never()).findByTitle(TITLE);
    }

    @Test
    public void testExistsExperimentWithIdTitleBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> experimentService.existsExperiment(BLANK, ID)
        );
        verify(experimentRepository, never()).findByTitle(TITLE);
    }

    @Test
    public void testExistsExperimentWithIdInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> experimentService.existsExperiment(TITLE, 0)
        );
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
        assertThrows(IllegalArgumentException.class,
                () -> experimentService.hasProjectFile(0)
        );
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
                () -> assertFalse(experimentDTO.isActive()),
                () -> assertTrue(experimentDTO.isCourseExperiment()),
                () -> assertEquals(experimentDTO.getGuiURL(), experiment.getGuiURL())
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
                () -> assertFalse(experimentDTO.isActive()),
                () -> assertTrue(experimentDTO.isCourseExperiment()),
                () -> assertEquals(experimentDTO.getGuiURL(), experiment.getGuiURL())
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
    public void testSaveExperimentGuiURLNull() {
        experimentDTO.setGuiURL(null);
        assertThrows(IncompleteDataException.class,
                () -> experimentService.saveExperiment(experimentDTO)
        );
        verify(experimentRepository, never()).save(any());
    }

    @Test
    public void testSaveExperimentGuiURLBlank() {
        experimentDTO.setGuiURL(BLANK);
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
                () -> assertFalse(found.isActive()),
                () -> assertTrue(found.isCourseExperiment()),
                () -> assertEquals(experiment.getGuiURL(), found.getGuiURL())
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
                () -> assertTrue(changedStatus.isActive()),
                () -> assertTrue(changedStatus.isCourseExperiment()),
                () -> assertEquals(experiment.getGuiURL(), changedStatus.getGuiURL())
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
                () -> assertFalse(changedStatus.isActive()),
                () -> assertTrue(changedStatus.isCourseExperiment()),
                () -> assertEquals(experiment.getGuiURL(), changedStatus.getGuiURL())
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
        when(experimentDataRepository.findByExperiment(ID)).thenReturn(Optional.of(experimentData));
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
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        assertDoesNotThrow(() -> experimentService.uploadSb3Project(ID, CONTENT));
        verify(experimentRepository).getReferenceById(ID);
        verify(experimentRepository).save(any());
    }

    @Test
    public void testUploadSb3ProjectEntityNotFound() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(experimentRepository.save(any())).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> experimentService.uploadSb3Project(ID, CONTENT)
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(experimentRepository).save(any());
    }

    @Test
    public void testUploadSb3ProjectInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> experimentService.uploadSb3Project(0, CONTENT)
        );
        verify(experimentRepository, never()).getReferenceById(anyInt());
        verify(experimentRepository, never()).save(any());
    }

    @Test
    public void testUploadSb3ProjectIdNull() {
        assertThrows(IllegalArgumentException.class,
                () -> experimentService.uploadSb3Project(ID, null)
        );
        verify(experimentRepository, never()).getReferenceById(anyInt());
        verify(experimentRepository, never()).save(any());
    }

    @Test
    public void testDeleteSb3Project() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        assertDoesNotThrow(() -> experimentService.deleteSb3Project(ID));
        verify(experimentRepository).getReferenceById(ID);
        verify(experimentRepository).save(any());
    }

    @Test
    public void testDeleteSb3ProjectEntityNotFound() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(experimentRepository.save(any())).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> experimentService.deleteSb3Project(ID)
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(experimentRepository).save(any());
    }

    @Test
    public void testDeleteSb3ProjectInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> experimentService.deleteSb3Project(-1)
        );
        verify(experimentRepository, never()).getReferenceById(anyInt());
        verify(experimentRepository, never()).save(any());
    }

    @Test
    public void testGetSb3File() {
        when(experimentRepository.findExperimentById(ID)).thenReturn(Optional.of(projection));
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
        verify(experimentRepository, never()).findExperimentById(anyInt());
    }

    @Test
    public void testGetSb3FileInactive() {
        when(experimentRepository.findExperimentById(ID)).thenReturn(Optional.of(new ExperimentProjection() {
            @Override
            public Integer getId() {
                return ID;
            }

            @Override
            public boolean isActive() {
                return false;
            }

            @Override
            public byte[] getProject() {
                return new byte[0];
            }
        }));
        assertThrows(NotFoundException.class,
                () -> experimentService.getSb3File(ID)
        );
        verify(experimentRepository).findExperimentById(ID);
    }

}
