package fim.unipassau.de.scratch1984.application;

import fim.unipassau.de.scratch1984.application.service.EventService;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.Participant;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.repository.BlockEventRepository;
import fim.unipassau.de.scratch1984.persistence.repository.EventCountRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ParticipantRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ResourceEventRepository;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
import fim.unipassau.de.scratch1984.web.dto.BlockEventDTO;
import fim.unipassau.de.scratch1984.web.dto.ResourceEventDTO;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @InjectMocks
    private EventService eventService;

    @Mock
    private EventCountRepository eventCountRepository;

    @Mock
    private BlockEventRepository blockEventRepository;

    @Mock
    private ResourceEventRepository resourceEventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExperimentRepository experimentRepository;

    private static final int ID = 1;
    private final BlockEventDTO blockEventDTO = new BlockEventDTO(1, 1, LocalDateTime.now(),
            BlockEventDTO.BlockEventType.CHANGE, BlockEventDTO.BlockEvent.CHANGE, "sprite", "meta", "xml", "json");
    private final ResourceEventDTO resourceEventDTO = new ResourceEventDTO(1, 1, LocalDateTime.now(),
            ResourceEventDTO.ResourceEventType.ADD, ResourceEventDTO.ResourceEvent.ADD_SOUND, "name", "hash",
            "filetype", ResourceEventDTO.LibraryResource.TRUE);
    private final User user = new User("participant", "email", "PARTICIPANT", "GERMAN", "password", "secret");
    private final Experiment experiment = new Experiment(ID, "title", "description", "info", true);
    private final Participant participant = new Participant(user, experiment, Timestamp.valueOf(LocalDateTime.now()), null);

    @BeforeEach
    public void setup() {
        user.setId(ID);
        resourceEventDTO.setLibraryResource(ResourceEventDTO.LibraryResource.TRUE);
    }

    @Test
    public void testSaveBlockEvent() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(participant);
        assertDoesNotThrow(
                () -> eventService.saveBlockEvent(blockEventDTO)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(blockEventRepository).save(any());
    }

    @Test
    public void testSaveBlockEventParticipantNull() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        assertDoesNotThrow(
                () -> eventService.saveBlockEvent(blockEventDTO)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(blockEventRepository, never()).save(any());
    }

    @Test
    public void testSaveBlockEventEntityNotFound() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenThrow(EntityNotFoundException.class);
        assertDoesNotThrow(
                () -> eventService.saveBlockEvent(blockEventDTO)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(blockEventRepository, never()).save(any());
    }

    @Test
    public void testSaveBlockEventConstraintViolation() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(participant);
        when(blockEventRepository.save(any())).thenThrow(ConstraintViolationException.class);
        assertDoesNotThrow(
                () -> eventService.saveBlockEvent(blockEventDTO)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(blockEventRepository).save(any());
    }

    @Test
    public void testSaveResourceEvent() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(participant);
        assertDoesNotThrow(
                () -> eventService.saveResourceEvent(resourceEventDTO)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(resourceEventRepository).save(any());
    }

    @Test
    public void testSaveResourceEventLibraryResourceFalse() {
        resourceEventDTO.setLibraryResource(ResourceEventDTO.LibraryResource.FALSE);
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(participant);
        assertDoesNotThrow(
                () -> eventService.saveResourceEvent(resourceEventDTO)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(resourceEventRepository).save(any());
    }

    @Test
    public void testSaveResourceEventLibraryResourceUnknown() {
        resourceEventDTO.setLibraryResource(ResourceEventDTO.LibraryResource.UNKNOWN);
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(participant);
        assertDoesNotThrow(
                () -> eventService.saveResourceEvent(resourceEventDTO)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(resourceEventRepository).save(any());
    }

    @Test
    public void testSaveResourceEventConstraintViolation() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(participant);
        when(resourceEventRepository.save(any())).thenThrow(ConstraintViolationException.class);
        assertDoesNotThrow(
                () -> eventService.saveResourceEvent(resourceEventDTO)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(resourceEventRepository).save(any());
    }

    @Test
    public void testSaveResourceEventEntityNotFound() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenThrow(EntityNotFoundException.class);
        assertDoesNotThrow(
                () -> eventService.saveResourceEvent(resourceEventDTO)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(resourceEventRepository, never()).save(any());
    }

    @Test
    public void testSaveResourceEventParticipantNull() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        assertDoesNotThrow(
                () -> eventService.saveResourceEvent(resourceEventDTO)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(resourceEventRepository, never()).save(any());
    }
}
