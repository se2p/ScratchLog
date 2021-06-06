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
import fim.unipassau.de.scratch1984.util.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
    private static final String BLANK = "   ";
    private static final String PASSWORD = "participant1";
    private static final String EMAIL = "participant@participant.de";
    private static final String PARTICIPANT = "PARTICIPANT";
    private static final String SECRET = "secret";
    private static final int ID = 1;
    private final User user = new User(USERNAME, EMAIL, PARTICIPANT, "ENGLISH", PASSWORD, SECRET);
    private final Experiment experiment = new Experiment(ID, "title", "description", "info", true);
    private final List<Participant> participantList = getParticipants(5);
    private final Page<Participant> participants = new PageImpl<>(participantList);
    private final PageRequest pageRequest = PageRequest.of(0, Constants.PAGE_SIZE);

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
    public void testGetParticipantPage() {
        when(participantRepository.findAllByExperiment(any(), any(PageRequest.class))).thenReturn(participants);
        assertEquals(participants, participantService.getParticipantPage(ID, pageRequest));
        verify(participantRepository).findAllByExperiment(any(), any(PageRequest.class));
        verify(experimentRepository).getOne(ID);
    }

    @Test
    public void testGetParticipantPageNotFound() {
        when(participantRepository.findAllByExperiment(any(), any(PageRequest.class)))
                .thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> participantService.getParticipantPage(ID, pageRequest)
        );
        verify(participantRepository).findAllByExperiment(any(), any(PageRequest.class));
        verify(experimentRepository).getOne(ID);
    }

    @Test
    public void testGetParticipantPageInvalidPageSize() {
        PageRequest invalidRequest = PageRequest.of(0, Constants.PAGE_SIZE + 1);
        assertThrows(IllegalArgumentException.class,
                () -> participantService.getParticipantPage(ID, invalidRequest)
        );
        verify(participantRepository, never()).findAllByExperiment(any(), any(PageRequest.class));
        verify(experimentRepository, never()).getOne(ID);
    }

    @Test
    public void testGetParticipantPageInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> participantService.getParticipantPage(0, pageRequest)
        );
        verify(participantRepository, never()).findAllByExperiment(any(), any(PageRequest.class));
        verify(experimentRepository, never()).getOne(ID);
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

    private List<Participant> getParticipants(int number) {
        List<Participant> participants = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            User user = new User();
            user.setId(i + 1);
            participants.add(new Participant(user, new Experiment(), Timestamp.valueOf(LocalDateTime.now()), null));
        }
        return participants;
    }
}
