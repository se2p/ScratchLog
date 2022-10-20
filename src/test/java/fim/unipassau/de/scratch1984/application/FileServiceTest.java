package fim.unipassau.de.scratch1984.application;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.FileService;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.File;
import fim.unipassau.de.scratch1984.persistence.entity.Participant;
import fim.unipassau.de.scratch1984.persistence.entity.Sb3Zip;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.projection.FileProjection;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.FileRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ParticipantRepository;
import fim.unipassau.de.scratch1984.persistence.repository.Sb3ZipRepository;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
import fim.unipassau.de.scratch1984.web.dto.FileDTO;
import fim.unipassau.de.scratch1984.web.dto.Sb3ZipDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import java.sql.Timestamp;
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
    private final FileDTO fileDTO = new FileDTO(ID, ID, LocalDateTime.now(), "file", "png", new byte[]{1, 2, 3, 4});
    private final Sb3ZipDTO sb3ZipDTO = new Sb3ZipDTO(ID, ID, LocalDateTime.now(), "zip", new byte[]{1, 2, 3, 4});
    private final User user = new User("participant", "email", "PARTICIPANT", "GERMAN", "password", "secret");
    private final Experiment experiment = new Experiment(ID, "title", "description", "info", "postscript", true,
            GUI_URL);
    private final Participant participant = new Participant(user, experiment, Timestamp.valueOf(LocalDateTime.now()), null);
    private final File file = new File(user, experiment, Timestamp.valueOf(LocalDateTime.now()), "file", "type",
            new byte[]{1, 2, 3, 4});
    private final Sb3Zip sb3Zip = new Sb3Zip(user, experiment, Timestamp.valueOf(LocalDateTime.now()), "zip",
            new byte[]{1, 2, 3, 4});
    private final List<FileProjection> fileProjections = getFileProjections(5);
    private final List<File> files = getFiles(2);
    private final List<Integer> zips = Arrays.asList(1, 4, 10, 18);
    private final List<Sb3Zip> sb3Zips = getSb3ZipFiles(3);

    @BeforeEach
    public void setup() {
        user.setId(ID);
        experiment.setId(ID);
    }

    @Test
    public void testSaveFile() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(participant);
        assertDoesNotThrow(
                () -> fileService.saveFile(fileDTO)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(fileRepository).save(any());
    }

    @Test
    public void testSaveFileConstraintViolation() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(participant);
        when(fileRepository.save(any())).thenThrow(ConstraintViolationException.class);
        assertDoesNotThrow(
                () -> fileService.saveFile(fileDTO)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(fileRepository).save(any());
    }

    @Test
    public void testSaveFileEntityNotFound() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenThrow(EntityNotFoundException.class);
        assertDoesNotThrow(
                () -> fileService.saveFile(fileDTO)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(fileRepository, never()).save(any());
    }

    @Test
    public void testSaveFileParticipantNull() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        assertDoesNotThrow(
                () -> fileService.saveFile(fileDTO)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(fileRepository, never()).save(any());
    }

    @Test
    public void testSaveSb3Zip() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(participant);
        assertDoesNotThrow(
                () -> fileService.saveSb3Zip(sb3ZipDTO)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(sb3ZipRepository).save(any());
    }

    @Test
    public void testSaveSb3ZipConstraintViolation() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(participant);
        when(sb3ZipRepository.save(any())).thenThrow(ConstraintViolationException.class);
        assertDoesNotThrow(
                () -> fileService.saveSb3Zip(sb3ZipDTO)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(sb3ZipRepository).save(any());
    }

    @Test
    public void testSaveSb3ZipEntityNotFound() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenThrow(EntityNotFoundException.class);
        assertDoesNotThrow(
                () -> fileService.saveSb3Zip(sb3ZipDTO)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(sb3ZipRepository, never()).save(any());
    }

    @Test
    public void testSaveSb3ZipParticipantNull() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        assertDoesNotThrow(
                () -> fileService.saveSb3Zip(sb3ZipDTO)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(sb3ZipRepository, never()).save(any());
    }

    @Test
    public void testGetFiles() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(fileRepository.findFilesByUserAndExperiment(user, experiment)).thenReturn(fileProjections);
        assertEquals(fileProjections, fileService.getFiles(ID, ID));
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(fileRepository).findFilesByUserAndExperiment(user, experiment);
    }

    @Test
    public void testGetFilesEntityNotFound() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(fileRepository.findFilesByUserAndExperiment(user, experiment)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> fileService.getFiles(ID, ID)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(fileRepository).findFilesByUserAndExperiment(user, experiment);
    }

    @Test
    public void testGetFilesInvalidExperimentId() {
        assertThrows(IllegalArgumentException.class,
                () -> fileService.getFiles(ID, 0)
        );
        verify(userRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).getOne(anyInt());
        verify(fileRepository, never()).findFilesByUserAndExperiment(any(), any());
    }

    @Test
    public void testGetFilesInvalidUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> fileService.getFiles(-1, ID)
        );
        verify(userRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).getOne(anyInt());
        verify(fileRepository, never()).findFilesByUserAndExperiment(any(), any());
    }

    @Test
    public void testGetFileDTOs() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
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
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(fileRepository).findAllByUserAndExperiment(user, experiment);
    }

    @Test
    public void testGetFileDTOsEntityNotFound() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(fileRepository.findAllByUserAndExperiment(user, experiment)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> fileService.getFileDTOs(ID, ID)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(fileRepository).findAllByUserAndExperiment(user, experiment);
    }

    @Test
    public void testGetFileDTOsInvalidExperimentId() {
        assertThrows(IllegalArgumentException.class,
                () -> fileService.getFileDTOs(ID, 0)
        );
        verify(userRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).getOne(anyInt());
        verify(fileRepository, never()).findAllByUserAndExperiment(any(), any());
    }

    @Test
    public void testGetFileDTOsInvalidUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> fileService.getFileDTOs(-1, ID)
        );
        verify(userRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).getOne(anyInt());
        verify(fileRepository, never()).findAllByUserAndExperiment(any(), any());
    }

    @Test
    public void testGetZipIds() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(sb3ZipRepository.findAllIdsByUserAndExperiment(user, experiment)).thenReturn(zips);
        assertEquals(zips, fileService.getZipIds(ID, ID));
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(sb3ZipRepository).findAllIdsByUserAndExperiment(user, experiment);
    }

    @Test
    public void testGetZipIdsEntityNotFound() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(sb3ZipRepository.findAllIdsByUserAndExperiment(user, experiment)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> fileService.getZipIds(ID, ID)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(sb3ZipRepository).findAllIdsByUserAndExperiment(user, experiment);
    }

    @Test
    public void testGetZipIdsInvalidExperimentId() {
        assertThrows(IllegalArgumentException.class,
                () -> fileService.getZipIds(ID, -10)
        );
        verify(userRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).getOne(anyInt());
        verify(sb3ZipRepository, never()).findAllIdsByUserAndExperiment(any(), any());
    }

    @Test
    public void testGetZipIdsInvalidUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> fileService.getZipIds(0, ID)
        );
        verify(userRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).getOne(anyInt());
        verify(sb3ZipRepository, never()).findAllIdsByUserAndExperiment(any(), any());
    }

    @Test
    public void testFindFile() {
        when(fileRepository.findById(ID)).thenReturn(java.util.Optional.of(file));
        FileDTO fileDTO = fileService.findFile(ID);
        assertAll(
                () -> assertEquals(file.getUser().getId(), fileDTO.getUser()),
                () -> assertEquals(file.getExperiment().getId(), fileDTO.getExperiment()),
                () -> assertEquals(file.getDate().toLocalDateTime(), fileDTO.getDate()),
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
    public void testFindFileInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> fileService.findFile(0)
        );
        verify(fileRepository, never()).findById(anyInt());
    }

    @Test
    public void testFindZip() {
        when(sb3ZipRepository.findById(ID)).thenReturn(java.util.Optional.of(sb3Zip));
        Sb3ZipDTO sb3ZipDTO = fileService.findZip(ID);
        assertAll(
                () -> assertEquals(sb3Zip.getUser().getId(), sb3ZipDTO.getUser()),
                () -> assertEquals(sb3Zip.getExperiment().getId(), sb3ZipDTO.getExperiment()),
                () -> assertEquals(sb3Zip.getDate().toLocalDateTime(), sb3ZipDTO.getDate()),
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
    public void testFindZipInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> fileService.findZip(0)
        );
        verify(sb3ZipRepository, never()).findById(anyInt());
    }

    @Test
    public void testFindFinalProject() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(sb3ZipRepository.findFirstByUserAndExperiment(user, experiment)).thenReturn(java.util.Optional.of(sb3Zip));
        assertTrue(fileService.findFinalProject(ID, ID).isPresent());
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(sb3ZipRepository).findFirstByUserAndExperiment(user, experiment);
    }

    @Test
    public void testFindFinalProjectEmpty() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(sb3ZipRepository.findFirstByUserAndExperiment(user, experiment)).thenReturn(Optional.empty());
        assertTrue(fileService.findFinalProject(ID, ID).isEmpty());
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(sb3ZipRepository).findFirstByUserAndExperiment(user, experiment);
    }

    @Test
    public void testFindFinalProjectEntityNotFound() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(sb3ZipRepository.findFirstByUserAndExperiment(user, experiment)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> fileService.findFinalProject(ID, ID)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(sb3ZipRepository).findFirstByUserAndExperiment(user, experiment);
    }

    @Test
    public void testFindFinalProjectInvalidExperimentId() {
        assertThrows(IllegalArgumentException.class,
                () -> fileService.findFinalProject(ID, 0)
        );
        verify(userRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).getOne(anyInt());
        verify(sb3ZipRepository, never()).findFirstByUserAndExperiment(any(), any());
    }

    @Test
    public void testFindFinalProjectInvalidUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> fileService.findFinalProject(-1, ID)
        );
        verify(userRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).getOne(anyInt());
        verify(sb3ZipRepository, never()).findFirstByUserAndExperiment(any(), any());
    }

    @Test
    public void testGetZipFiles() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(sb3ZipRepository.findAllByUserAndExperiment(user, experiment)).thenReturn(sb3Zips);
        List<Sb3ZipDTO> sb3ZipDTOs = fileService.getZipFiles(ID, ID);
        assertAll(
                () -> assertEquals(3, sb3ZipDTOs.size()),
                () -> assertEquals(ID, sb3ZipDTOs.get(0).getUser()),
                () -> assertEquals(ID, sb3ZipDTOs.get(0).getExperiment()),
                () -> assertEquals(4, sb3ZipDTOs.get(0).getContent().length),
                () -> assertEquals("file0", sb3ZipDTOs.get(0).getName()),
                () -> assertEquals("file1", sb3ZipDTOs.get(1).getName()),
                () -> assertEquals("file2", sb3ZipDTOs.get(2).getName())
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(sb3ZipRepository).findAllByUserAndExperiment(user, experiment);
    }

    @Test
    public void testGetZipFilesEmpty() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        assertThrows(NotFoundException.class,
                () -> fileService.getZipFiles(ID, ID)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(sb3ZipRepository).findAllByUserAndExperiment(user, experiment);
    }

    @Test
    public void testGetZipFilesEntityNotFound() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(sb3ZipRepository.findAllByUserAndExperiment(user, experiment)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> fileService.getZipFiles(ID, ID)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(sb3ZipRepository).findAllByUserAndExperiment(user, experiment);
    }

    @Test
    public void testGetZipFilesInvalidExperimentId() {
        assertThrows(IllegalArgumentException.class,
                () -> fileService.getZipFiles(ID, 0)
        );
        verify(userRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).getOne(anyInt());
        verify(sb3ZipRepository, never()).findAllByUserAndExperiment(any(), any());
    }

    @Test
    public void testGetZipFilesInvalidUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> fileService.getZipFiles(-1, ID)
        );
        verify(userRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).getOne(anyInt());
        verify(sb3ZipRepository, never()).findAllByUserAndExperiment(any(), any());
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
        Timestamp time = Timestamp.valueOf(LocalDateTime.now());
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
            sb3Zips.add(new Sb3Zip(user, experiment, Timestamp.valueOf(LocalDateTime.now()), "file" + i,
                    new byte[] {1, 2, 3, 4}));
        }
        return sb3Zips;
    }
}
