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

package de.uni_passau.fim.se2.scratchlog.application;

import de.uni_passau.fim.se2.scratchlog.application.exception.NotFoundException;
import de.uni_passau.fim.se2.scratchlog.application.service.FileService;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Experiment;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.File;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Participant;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Sb3Zip;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.FileProjection;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ExperimentRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.FileRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ParticipantRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.Sb3ZipRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.UserRepository;
import de.uni_passau.fim.se2.scratchlog.util.enums.Language;
import de.uni_passau.fim.se2.scratchlog.util.enums.Role;
import de.uni_passau.fim.se2.scratchlog.web.dto.FileDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.Sb3ZipDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FileServiceTest {

    @InjectMocks
    private FileService fileService;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExperimentRepository experimentRepository;

    @Mock
    private Sb3ZipRepository sb3ZipRepository;

    private static final int ID = 1;
    private static final String GUI_URL = "scratch";
    private final FileDTO fileDTO = new FileDTO(ID, ID, "secret", "file", "png", new byte[]{1, 2, 3, 4}, LocalDateTime.now());
    private final Sb3ZipDTO sb3ZipDTO = new Sb3ZipDTO(ID, ID, "secret", "zip", new byte[]{1, 2, 3, 4}, LocalDateTime.now());
    private final User user = new User("participant", "email", Role.PARTICIPANT, Language.GERMAN, "password", "secret");
    private final Experiment experiment = new Experiment(ID, "title", "description", "info", "postscript", true,
            false, GUI_URL);
    private final Participant participant = new Participant(user, experiment, LocalDateTime.now(), null);
    private final File file = new File(user, experiment, LocalDateTime.now(), "file", "type", new byte[]{1, 2, 3, 4});
    private final Sb3Zip sb3Zip = new Sb3Zip(user, experiment, LocalDateTime.now(), "zip", new byte[]{1, 2, 3, 4});
    private final List<FileProjection> fileProjections = getFileProjections(5);
    private final List<File> files = getFiles(2);
    private final List<Integer> zips = Arrays.asList(1, 4, 10, 18);
    private final List<Sb3Zip> sb3Zips = getSb3ZipFiles(3);

    @BeforeEach
    public void setup() {
        user.setId(ID);
        user.setActive(true);
        experiment.setId(ID);
        experiment.setActive(true);
        participant.setEnd(null);
    }

    @Test
    public void testSaveFile() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(Optional.of(participant));
        assertDoesNotThrow(
                () -> fileService.saveFile(fileDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(fileRepository).save(any());
    }

    @Test
    public void testSaveFileConstraintViolation() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(Optional.of(participant));
        when(fileRepository.save(any())).thenThrow(ConstraintViolationException.class);
        assertDoesNotThrow(
                () -> fileService.saveFile(fileDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(fileRepository).save(any());
    }

    @Test
    public void testSaveFileEntityNotFound() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenThrow(EntityNotFoundException.class);
        assertDoesNotThrow(
                () -> fileService.saveFile(fileDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(fileRepository, never()).save(any());
    }

    @Test
    public void testSaveFileParticipantFinished() {
        participant.setEnd(LocalDateTime.now());
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(Optional.of(participant));
        assertDoesNotThrow(
                () -> fileService.saveFile(fileDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(fileRepository, never()).save(any());
    }

    @Test
    public void testSaveFileParticipantNull() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        assertDoesNotThrow(
                () -> fileService.saveFile(fileDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(fileRepository, never()).save(any());
    }

    @Test
    public void testSaveFileUserInactive() {
        user.setActive(false);
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(Optional.of(participant));
        assertDoesNotThrow(
                () -> fileService.saveFile(fileDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(fileRepository, never()).save(any());
    }

    @Test
    public void testSaveFileExperimentInactive() {
        experiment.setActive(false);
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(Optional.of(participant));
        assertDoesNotThrow(
                () -> fileService.saveFile(fileDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(fileRepository, never()).save(any());
    }

    @Test
    public void testSaveSb3Zip() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(Optional.of(participant));
        assertDoesNotThrow(
                () -> fileService.saveSb3Zip(sb3ZipDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(sb3ZipRepository).save(any());
    }

    @Test
    public void testSaveSb3ZipConstraintViolation() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(Optional.of(participant));
        when(sb3ZipRepository.save(any())).thenThrow(ConstraintViolationException.class);
        assertDoesNotThrow(
                () -> fileService.saveSb3Zip(sb3ZipDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(sb3ZipRepository).save(any());
    }

    @Test
    public void testSaveSb3ZipEntityNotFound() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenThrow(EntityNotFoundException.class);
        assertDoesNotThrow(
                () -> fileService.saveSb3Zip(sb3ZipDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(sb3ZipRepository, never()).save(any());
    }

    @Test
    public void testSaveSb3ZipParticipantFinished() {
        participant.setEnd(LocalDateTime.now());
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(Optional.of(participant));
        assertDoesNotThrow(
                () -> fileService.saveSb3Zip(sb3ZipDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(sb3ZipRepository, never()).save(any());
    }

    @Test
    public void testSaveSb3ZipParticipantNull() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        assertDoesNotThrow(
                () -> fileService.saveSb3Zip(sb3ZipDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(sb3ZipRepository, never()).save(any());
    }

    @Test
    public void testSaveSb3ZipUserInactive() {
        user.setActive(false);
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(Optional.of(participant));
        assertDoesNotThrow(
                () -> fileService.saveSb3Zip(sb3ZipDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(sb3ZipRepository, never()).save(any());
    }

    @Test
    public void testSaveSb3ZipExperimentInactive() {
        experiment.setActive(false);
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(Optional.of(participant));
        assertDoesNotThrow(
                () -> fileService.saveSb3Zip(sb3ZipDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(sb3ZipRepository, never()).save(any());
    }

    @Test
    public void testGetFiles() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(fileRepository.findFilesByUserAndExperiment(user, experiment)).thenReturn(fileProjections);
        assertEquals(fileProjections, fileService.getFiles(ID, ID));
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(fileRepository).findFilesByUserAndExperiment(user, experiment);
    }

    @Test
    public void testGetFilesEntityNotFound() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(fileRepository.findFilesByUserAndExperiment(user, experiment)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> fileService.getFiles(ID, ID)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(fileRepository).findFilesByUserAndExperiment(user, experiment);
    }

    @Test
    public void testGetFileDTOs() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(fileRepository.findAllByUserAndExperiment(user, experiment)).thenReturn(files);
        List<FileDTO> fileDTOS = fileService.getFileDTOs(ID, ID);
        FileDTO fileDTO1 = fileDTOS.get(0);
        FileDTO fileDTO2 = fileDTOS.get(1);
        assertAll(
                () -> assertEquals(2, fileDTOS.size()),
                () -> assertEquals(ID, fileDTO1.getId()),
                () -> assertEquals("name0", fileDTO1.getName()),
                () -> assertEquals(ID + 1, fileDTO2.getId()),
                () -> assertEquals("name1", fileDTO2.getName())
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(fileRepository).findAllByUserAndExperiment(user, experiment);
    }

    @Test
    public void testGetFileDTOsEntityNotFound() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(fileRepository.findAllByUserAndExperiment(user, experiment)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> fileService.getFileDTOs(ID, ID)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(fileRepository).findAllByUserAndExperiment(user, experiment);
    }

    @Test
    public void testGetZipIds() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(sb3ZipRepository.findAllIdsByUserAndExperiment(user, experiment)).thenReturn(zips);
        assertEquals(zips, fileService.getZipIds(ID, ID));
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(sb3ZipRepository).findAllIdsByUserAndExperiment(user, experiment);
    }

    @Test
    public void testGetZipIdsEntityNotFound() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(sb3ZipRepository.findAllIdsByUserAndExperiment(user, experiment)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> fileService.getZipIds(ID, ID)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(sb3ZipRepository).findAllIdsByUserAndExperiment(user, experiment);
    }

    @Test
    public void testFindFile() {
        when(fileRepository.findById(ID)).thenReturn(java.util.Optional.of(file));
        FileDTO fileDTO = fileService.findFile(ID);
        assertAll(
                () -> assertEquals(file.getUser().getId(), fileDTO.getUser()),
                () -> assertEquals(file.getExperiment().getId(), fileDTO.getExperiment()),
                () -> assertEquals(file.getDate(), fileDTO.getDate()),
                () -> assertEquals(file.getFiletype(), fileDTO.getFiletype()),
                () -> assertEquals(file.getName(), fileDTO.getName()),
                () -> assertEquals(file.getContent(), fileDTO.getContent())
        );
        verify(fileRepository).findById(ID);
    }

    @Test
    public void testFindFileEmpty() {
        assertThrows(NotFoundException.class,
                () -> fileService.findFile(ID)
        );
        verify(fileRepository).findById(ID);
    }

    @Test
    public void testFindZip() {
        when(sb3ZipRepository.findById(ID)).thenReturn(java.util.Optional.of(sb3Zip));
        Sb3ZipDTO sb3ZipDTO = fileService.findZip(ID);
        assertAll(
                () -> assertEquals(sb3Zip.getUser().getId(), sb3ZipDTO.getUser()),
                () -> assertEquals(sb3Zip.getExperiment().getId(), sb3ZipDTO.getExperiment()),
                () -> assertEquals(sb3Zip.getDate(), sb3ZipDTO.getDate()),
                () -> assertEquals(sb3Zip.getName(), sb3ZipDTO.getName()),
                () -> assertEquals(sb3Zip.getContent(), sb3ZipDTO.getContent())
        );
        verify(sb3ZipRepository).findById(ID);
    }

    @Test
    public void testFindZipEmpty() {
        assertThrows(NotFoundException.class,
                () -> fileService.findZip(ID)
        );
        verify(sb3ZipRepository).findById(ID);
    }

    @Test
    public void testFindFinalProject() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(sb3ZipRepository.findFirstByUserAndExperimentOrderByIdDesc(user, experiment)).thenReturn(java.util.Optional.of(sb3Zip));
        assertTrue(fileService.findFinalProject(ID, ID).isPresent());
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(sb3ZipRepository).findFirstByUserAndExperimentOrderByIdDesc(user, experiment);
    }

    @Test
    public void testFindFinalProjectEmpty() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(sb3ZipRepository.findFirstByUserAndExperimentOrderByIdDesc(user, experiment)).thenReturn(Optional.empty());
        assertTrue(fileService.findFinalProject(ID, ID).isEmpty());
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(sb3ZipRepository).findFirstByUserAndExperimentOrderByIdDesc(user, experiment);
    }

    @Test
    public void testFindFinalProjectEntityNotFound() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(sb3ZipRepository.findFirstByUserAndExperimentOrderByIdDesc(user, experiment)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> fileService.findFinalProject(ID, ID)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(sb3ZipRepository).findFirstByUserAndExperimentOrderByIdDesc(user, experiment);
    }

    @Test
    public void testGetZipFiles() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(sb3ZipRepository.findAllByUserAndExperiment(user, experiment)).thenReturn(sb3Zips);
        List<Sb3ZipDTO> sb3ZipDTOs = fileService.getZipFiles(ID, ID);
        assertAll(
                () -> assertEquals(3, sb3ZipDTOs.size()),
                () -> assertEquals(ID, sb3ZipDTOs.getFirst().getUser()),
                () -> assertEquals(ID, sb3ZipDTOs.getFirst().getExperiment()),
                () -> assertEquals(4, sb3ZipDTOs.getFirst().getContent().length),
                () -> assertEquals("file0", sb3ZipDTOs.getFirst().getName()),
                () -> assertEquals("file1", sb3ZipDTOs.get(1).getName()),
                () -> assertEquals("file2", sb3ZipDTOs.get(2).getName())
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(sb3ZipRepository).findAllByUserAndExperiment(user, experiment);
    }

    @Test
    public void testGetZipFilesEmpty() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        assertThrows(NotFoundException.class,
                () -> fileService.getZipFiles(ID, ID)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(sb3ZipRepository).findAllByUserAndExperiment(user, experiment);
    }

    @Test
    public void testGetZipFilesEntityNotFound() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(sb3ZipRepository.findAllByUserAndExperiment(user, experiment)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> fileService.getZipFiles(ID, ID)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(sb3ZipRepository).findAllByUserAndExperiment(user, experiment);
    }

    private List<FileProjection> getFileProjections(int number) {
        List<FileProjection> fileProjections = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            final int id = i;
            fileProjections.add(new FileProjection() {
                @Override
                public Integer getId() {
                    return id;
                }

                @Override
                public String getName() {
                    return "some name" + id;
                }
            });
        }
        return fileProjections;
    }

    private List<File> getFiles(int number) {
        List<File> files = new ArrayList<>();
        LocalDateTime time = LocalDateTime.now();
        for (int i = 0; i < number; i++) {
            File file = new File(user, experiment, time, "name" + i, "type", new byte[]{1, 2, 3, 4});
            file.setId(i + 1);
            files.add(file);
        }
        return files;
    }

    private List<Sb3Zip> getSb3ZipFiles(int number) {
        List<Sb3Zip> sb3Zips = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            sb3Zips.add(new Sb3Zip(user, experiment, LocalDateTime.now(), "file" + i,
                    new byte[] {1, 2, 3, 4}));
        }
        return sb3Zips;
    }
}
