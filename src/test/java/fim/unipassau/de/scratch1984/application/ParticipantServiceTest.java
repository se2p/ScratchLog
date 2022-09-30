package fim.unipassau.de.scratch1984.application;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.exception.StoreException;
import fim.unipassau.de.scratch1984.application.service.ParticipantService;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.Participant;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ParticipantRepository;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
import fim.unipassau.de.scratch1984.web.dto.ParticipantDTO;
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
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParticipantServiceTest {

    @InjectMocks
    private ParticipantService participantService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExperimentRepository experimentRepository;

    @Mock
    private ParticipantRepository participantRepository;

    private static final String USERNAME = "participant";
    private static final String PASSWORD = "participant1";
    private static final String EMAIL = "participant@participant.de";
    private static final String PARTICIPANT = "PARTICIPANT";
    private static final String SECRET = "secret";
    private static final String GUI_URL = "scratch";
    private static final int ID = 1;
    private final User user = new User(USERNAME, EMAIL, PARTICIPANT, "ENGLISH", PASSWORD, SECRET);
    private final Experiment experiment = new Experiment(ID, "title", "description", "info", "postscript", true,
            false, GUI_URL);
    private final Participant participant = new Participant(user, experiment, null, null);
    private final ParticipantDTO participantDTO = new ParticipantDTO(ID, ID);
    private final List<Participant> participantList = getParticipants(5);

    @BeforeEach
    public void setup() {
        participantDTO.setUser(ID);
        participantDTO.setExperiment(ID);
        user.setId(ID);
    }

    @Test
    public void testGetParticipant() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(participant);
        ParticipantDTO participantDTO = participantService.getParticipant(ID, ID);
        assertAll(
                () -> assertEquals(ID, participantDTO.getExperiment()),
                () -> assertEquals(ID, participantDTO.getUser()),
                () -> assertNull(participantDTO.getStart()),
                () -> assertNull(participantDTO.getEnd())
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
    }

    @Test
    public void testGetParticipantNull() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        assertThrows(NotFoundException.class,
                () -> participantService.getParticipant(ID, ID)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
    }

    @Test
    public void testGetParticipantEntityNotFound() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> participantService.getParticipant(ID, ID)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
    }

    @Test
    public void testGetParticipantInvalidUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> participantService.getParticipant(ID, 0)
        );
        verify(userRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).getOne(anyInt());
        verify(participantRepository, never()).findByUserAndExperiment(any(), any());
    }

    @Test
    public void testGetParticipantInvalidExperimentId() {
        assertThrows(IllegalArgumentException.class,
                () -> participantService.getParticipant(-1, ID)
        );
        verify(userRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).getOne(anyInt());
        verify(participantRepository, never()).findByUserAndExperiment(any(), any());
    }

    @Test
    public void testSaveParticipant() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        assertDoesNotThrow(() -> participantService.saveParticipant(ID, ID));
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).save(any());
    }

    @Test
    public void testSaveParticipantNotFound() {
        when(participantRepository.save(any())).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> participantService.saveParticipant(ID, ID)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).save(any());
    }

    @Test
    public void testSaveParticipantForeignKeyViolation() {
        when(participantRepository.save(any())).thenThrow(ConstraintViolationException.class);
        assertThrows(StoreException.class,
                () -> participantService.saveParticipant(ID, ID)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).save(any());
    }

    @Test
    public void testUpdateParticipant() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        assertTrue(participantService.updateParticipant(participantDTO));
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).save(any());
    }

    @Test
    public void testUpdateParticipantEntityNotFound() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(participantRepository.save(any())).thenThrow(EntityNotFoundException.class);
        assertFalse(participantService.updateParticipant(participantDTO));
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).save(any());
    }

    @Test
    public void testUpdateParticipantConstraintViolation() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(participantRepository.save(any())).thenThrow(ConstraintViolationException.class);
        assertFalse(participantService.updateParticipant(participantDTO));
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).save(any());
    }

    @Test
    public void testUpdateParticipantInvalidUserId() {
        participantDTO.setUser(-1);
        assertThrows(IllegalArgumentException.class,
                () -> participantService.updateParticipant(participantDTO)
        );
        verify(userRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).getOne(anyInt());
        verify(participantRepository, never()).save(any());
    }

    @Test
    public void testUpdateParticipantInvalidExperimentId() {
        participantDTO.setExperiment(0);
        assertThrows(IllegalArgumentException.class,
                () -> participantService.updateParticipant(participantDTO)
        );
        verify(userRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).getOne(anyInt());
        verify(participantRepository, never()).save(any());
    }

    @Test
    public void testDeactivateParticipantAccounts() {
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(participantRepository.findAllByExperiment(experiment)).thenReturn(participantList);
        when(userRepository.findById(any(Integer.class))).thenReturn(java.util.Optional.of(user));
        assertDoesNotThrow(() -> participantService.deactivateParticipantAccounts(ID));
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findAllByExperiment(experiment);
        verify(userRepository, times(5)).findById(any(Integer.class));
        verify(userRepository, times(5)).save(user);
    }

    @Test
    public void testDeactivateParticipantAccountsSimultaneousParticipation() {
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(participantRepository.findAllByExperiment(experiment)).thenReturn(participantList);
        when(userRepository.findById(any(Integer.class))).thenReturn(java.util.Optional.of(user));
        when(participantRepository.findAllByEndIsNullAndUser(any())).thenReturn(participantList);
        assertDoesNotThrow(() -> participantService.deactivateParticipantAccounts(ID));
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findAllByExperiment(experiment);
        verify(userRepository, times(5)).findById(any(Integer.class));
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testDeactivateParticipantAccountsIllegalState() {
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(participantRepository.findAllByExperiment(experiment)).thenReturn(participantList);
        assertThrows(IllegalStateException.class,
                () -> participantService.deactivateParticipantAccounts(ID)
        );
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findAllByExperiment(experiment);
        verify(userRepository).findById(any(Integer.class));
        verify(userRepository, never()).save(user);
    }

    @Test
    public void testDeactivateParticipantAccountsNotFound() {
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(participantRepository.findAllByExperiment(experiment)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> participantService.deactivateParticipantAccounts(ID)
        );
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findAllByExperiment(experiment);
        verify(userRepository, never()).findById(any(Integer.class));
        verify(userRepository, never()).save(user);
    }

    @Test
    public void testDeactivateParticipantAccountsInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> participantService.deactivateParticipantAccounts(0)
        );
        verify(experimentRepository, never()).getOne(ID);
        verify(participantRepository, never()).findAllByExperiment(experiment);
        verify(userRepository, never()).findById(any(Integer.class));
        verify(userRepository, never()).save(user);
    }

    @Test
    public void testGetExperimentIdsForParticipant() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(participantRepository.findAllByUser(user)).thenReturn(participantList);
        HashMap<Integer, String> experiments = participantService.getExperimentInfoForParticipant(ID);
        assertAll(
                () -> assertEquals(5, experiments.size()),
                () -> assertTrue(experiments.containsKey(1)),
                () -> assertTrue(experiments.containsValue("Title 0")),
                () -> assertTrue(experiments.containsKey(2)),
                () -> assertTrue(experiments.containsValue("Title 1")),
                () -> assertTrue(experiments.containsKey(3)),
                () -> assertTrue(experiments.containsValue("Title 2")),
                () -> assertTrue(experiments.containsKey(4)),
                () -> assertTrue(experiments.containsValue("Title 3")),
                () -> assertTrue(experiments.containsKey(5)),
                () -> assertTrue(experiments.containsValue("Title 4"))
        );
        verify(userRepository).getOne(ID);
        verify(participantRepository).findAllByUser(user);
    }

    @Test
    public void testGetExperimentIdsForParticipantNotFound() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(participantRepository.findAllByUser(user)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> participantService.getExperimentInfoForParticipant(ID)
        );
        verify(userRepository).getOne(ID);
        verify(participantRepository).findAllByUser(user);
    }

    @Test
    public void testGetExperimentIdsForParticipantIdInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> participantService.getExperimentInfoForParticipant(0)
        );
        verify(userRepository, never()).getOne(ID);
        verify(participantRepository, never()).findAllByUser(user);
    }

    @Test
    public void testDeleteParticipant() {
        assertDoesNotThrow(() -> participantService.deleteParticipant(ID, ID));
        verify(participantRepository).deleteById(any());
    }

    @Test
    public void testDeleteParticipantInvalidUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> participantService.deleteParticipant(0, ID)
        );
        verify(participantRepository, never()).deleteById(any());
    }

    @Test
    public void testDeleteParticipantInvalidExperimentId() {
        assertThrows(IllegalArgumentException.class,
                () -> participantService.deleteParticipant(ID, -1)
        );
        verify(participantRepository, never()).deleteById(any());
    }

    @Test
    public void testSimultaneousParticipation() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(participantRepository.findAllByEndIsNullAndUser(user)).thenReturn(participantList);
        assertTrue(participantService.simultaneousParticipation(ID));
        verify(userRepository).getOne(ID);
        verify(participantRepository).findAllByEndIsNullAndUser(user);
    }

    @Test
    public void testSimultaneousParticipationFalse() {
        List<Participant> oneParticipation = new ArrayList<>();
        oneParticipation.add(new Participant());
        when(userRepository.getOne(ID)).thenReturn(user);
        when(participantRepository.findAllByEndIsNullAndUser(user)).thenReturn(oneParticipation);
        assertFalse(participantService.simultaneousParticipation(ID));
        verify(userRepository).getOne(ID);
        verify(participantRepository).findAllByEndIsNullAndUser(user);
    }

    @Test
    public void testSimultaneousParticipationNotFound() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(participantRepository.findAllByEndIsNullAndUser(user)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> participantService.simultaneousParticipation(ID)
        );
        verify(userRepository).getOne(ID);
        verify(participantRepository).findAllByEndIsNullAndUser(user);
    }

    @Test
    public void testSimultaneousParticipationInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> participantService.simultaneousParticipation(0)
        );
        verify(userRepository, never()).getOne(anyInt());
        verify(participantRepository, never()).findAllByEndIsNullAndUser(any());
    }

    private List<Participant> getParticipants(int number) {
        List<Participant> participants = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            User user = new User();
            user.setId(i + 1);
            Experiment experiment = new Experiment();
            experiment.setId(i + 1);
            experiment.setTitle("Title " + i);
            participants.add(new Participant(user, experiment, Timestamp.valueOf(LocalDateTime.now()), null));
        }
        return participants;
    }
}
