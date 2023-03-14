package fim.unipassau.de.scratch1984.application;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.exception.StoreException;
import fim.unipassau.de.scratch1984.application.service.ParticipantService;
import fim.unipassau.de.scratch1984.persistence.entity.Course;
import fim.unipassau.de.scratch1984.persistence.entity.CourseParticipant;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.Participant;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.repository.CourseExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.CourseParticipantRepository;
import fim.unipassau.de.scratch1984.persistence.repository.CourseRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ParticipantRepository;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
import fim.unipassau.de.scratch1984.util.enums.Language;
import fim.unipassau.de.scratch1984.util.enums.Role;
import fim.unipassau.de.scratch1984.web.dto.ParticipantDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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
    private CourseRepository courseRepository;

    @Mock
    private CourseExperimentRepository courseExperimentRepository;

    @Mock
    private CourseParticipantRepository courseParticipantRepository;

    @Mock
    private ParticipantRepository participantRepository;

    private static final String USERNAME = "participant";
    private static final String PASSWORD = "participant1";
    private static final String EMAIL = "participant@participant.de";
    private static final String PARTICIPANT = "PARTICIPANT";
    private static final String SECRET = "secret";
    private static final String GUI_URL = "scratch";
    private static final int ID = 1;
    private static final long MAX_DAYS = 90;
    private static final LocalDateTime MAX_TIME = LocalDateTime.now().minusDays(MAX_DAYS);
    private final User user = new User(USERNAME, EMAIL, Role.PARTICIPANT, Language.ENGLISH, PASSWORD, SECRET);
    private final Experiment experiment1 = new Experiment(ID, "title", "description", "info", "postscript", true,
            false, GUI_URL);
    private final Experiment experiment2 = new Experiment(ID, "title", "description", "info", "postscript", true,
            true, GUI_URL);
    private final Participant participant1 = new Participant(user, experiment1, null, null);
    private final Participant participant2 = new Participant(user, experiment2, MAX_TIME, MAX_TIME);
    private final ParticipantDTO participantDTO = new ParticipantDTO(ID, ID);
    private final Course course = new Course(ID, "title", "description", "content", true,
            LocalDateTime.now());
    private final List<Participant> participantList = getParticipants(5);
    private final List<CourseParticipant> courseParticipants = getCourseParticipants(3);

    @BeforeEach
    public void setup() {
        participantDTO.setUser(ID);
        participantDTO.setExperiment(ID);
        user.setId(ID);
        user.setActive(false);
        course.setActive(true);
        experiment1.setActive(true);
        experiment2.setActive(true);
        participant2.setStart(MAX_TIME);
        participant2.setEnd(MAX_TIME);
    }

    @Test
    public void testGetParticipant() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment1);
        when(participantRepository.findByUserAndExperiment(user, experiment1)).thenReturn(Optional.of(participant1));
        ParticipantDTO participantDTO = participantService.getParticipant(ID, ID);
        assertAll(
                () -> assertEquals(ID, participantDTO.getExperiment()),
                () -> assertEquals(ID, participantDTO.getUser()),
                () -> assertNull(participantDTO.getStart()),
                () -> assertNull(participantDTO.getEnd())
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment1);
    }

    @Test
    public void testGetParticipantNull() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment1);
        assertThrows(NotFoundException.class,
                () -> participantService.getParticipant(ID, ID)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment1);
    }

    @Test
    public void testGetParticipantEntityNotFound() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment1);
        when(participantRepository.findByUserAndExperiment(user, experiment1)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> participantService.getParticipant(ID, ID)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment1);
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
    public void testSaveParticipants() {
        when(courseRepository.getOne(ID)).thenReturn(course);
        when(experimentRepository.getOne(ID)).thenReturn(experiment2);
        when(courseExperimentRepository.existsByCourseAndExperiment(course, experiment2)).thenReturn(true);
        when(courseParticipantRepository.findAllByCourse(course)).thenReturn(courseParticipants);
        assertDoesNotThrow(() -> participantService.saveParticipants(ID, ID));
        verify(courseRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(courseExperimentRepository).existsByCourseAndExperiment(course, experiment2);
        verify(courseParticipantRepository).findAllByCourse(course);
        verify(participantRepository, times(3)).save(any());
        verify(userRepository, times(2)).save(any());
    }

    @Test
    public void testSaveParticipantsConstraintViolation() {
        when(courseRepository.getOne(ID)).thenReturn(course);
        when(experimentRepository.getOne(ID)).thenReturn(experiment2);
        when(courseExperimentRepository.existsByCourseAndExperiment(course, experiment2)).thenReturn(true);
        when(courseParticipantRepository.findAllByCourse(course)).thenReturn(courseParticipants);
        when(participantRepository.save(any())).thenThrow(ConstraintViolationException.class);
        assertThrows(StoreException.class, () -> participantService.saveParticipants(ID, ID));
        verify(courseRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(courseExperimentRepository).existsByCourseAndExperiment(course, experiment2);
        verify(courseParticipantRepository).findAllByCourse(course);
        verify(participantRepository).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testSaveParticipantsEntityNotFound() {
        when(courseRepository.getOne(ID)).thenReturn(course);
        when(experimentRepository.getOne(ID)).thenReturn(experiment2);
        when(courseExperimentRepository.existsByCourseAndExperiment(course,
                experiment2)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class, () -> participantService.saveParticipants(ID, ID));
        verify(courseRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(courseExperimentRepository).existsByCourseAndExperiment(course, experiment2);
        verify(courseParticipantRepository, never()).findAllByCourse(any());
        verify(participantRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testSaveParticipantsExperimentNotCourseExperiment() {
        when(courseRepository.getOne(ID)).thenReturn(course);
        when(experimentRepository.getOne(ID)).thenReturn(experiment2);
        assertThrows(IllegalStateException.class, () -> participantService.saveParticipants(ID, ID));
        verify(courseRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(courseParticipantRepository, never()).findAllByCourse(any());
        verify(participantRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testSaveParticipantsExperimentInactive() {
        experiment1.setActive(false);
        when(courseRepository.getOne(ID)).thenReturn(course);
        when(experimentRepository.getOne(ID)).thenReturn(experiment1);
        assertThrows(IllegalStateException.class, () -> participantService.saveParticipants(ID, ID));
        verify(courseRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(courseParticipantRepository, never()).findAllByCourse(any());
        verify(participantRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testSaveParticipantsCourseInactive() {
        course.setActive(false);
        when(courseRepository.getOne(ID)).thenReturn(course);
        when(experimentRepository.getOne(ID)).thenReturn(experiment1);
        assertThrows(IllegalStateException.class, () -> participantService.saveParticipants(ID, ID));
        verify(courseRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(courseParticipantRepository, never()).findAllByCourse(any());
        verify(participantRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testSaveParticipantsInvalidIds() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> participantService.saveParticipants(0, ID)),
                () -> assertThrows(IllegalArgumentException.class, () -> participantService.saveParticipants(ID, -1))
        );
        verify(courseRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).getOne(anyInt());
        verify(courseParticipantRepository, never()).findAllByCourse(any());
        verify(participantRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testSaveParticipant() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment1);
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
    public void testSaveParticipantInvalidIds() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> participantService.saveParticipant(ID, 0)),
                () -> assertThrows(IllegalArgumentException.class, () -> participantService.saveParticipant(-1, ID))
        );
        verify(userRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).getOne(anyInt());
        verify(participantRepository, never()).save(any());
    }

    @Test
    public void testUpdateParticipant() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment1);
        assertTrue(participantService.updateParticipant(participantDTO));
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).save(any());
    }

    @Test
    public void testUpdateParticipantEntityNotFound() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment1);
        when(participantRepository.save(any())).thenThrow(EntityNotFoundException.class);
        assertFalse(participantService.updateParticipant(participantDTO));
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).save(any());
    }

    @Test
    public void testUpdateParticipantConstraintViolation() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment1);
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
        when(experimentRepository.getOne(ID)).thenReturn(experiment1);
        when(participantRepository.findAllByExperiment(experiment1)).thenReturn(participantList);
        when(userRepository.findById(any(Integer.class))).thenReturn(java.util.Optional.of(user));
        assertDoesNotThrow(() -> participantService.deactivateParticipantAccounts(ID));
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findAllByExperiment(experiment1);
        verify(userRepository, times(5)).findById(any(Integer.class));
        verify(userRepository, times(5)).save(user);
    }

    @Test
    public void testDeactivateParticipantAccountsSimultaneousParticipation() {
        when(experimentRepository.getOne(ID)).thenReturn(experiment1);
        when(participantRepository.findAllByExperiment(experiment1)).thenReturn(participantList);
        when(userRepository.findById(any(Integer.class))).thenReturn(java.util.Optional.of(user));
        when(participantRepository.findAllByEndIsNullAndUser(any())).thenReturn(participantList);
        assertDoesNotThrow(() -> participantService.deactivateParticipantAccounts(ID));
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findAllByExperiment(experiment1);
        verify(userRepository, times(5)).findById(any(Integer.class));
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testDeactivateParticipantAccountsIllegalState() {
        when(experimentRepository.getOne(ID)).thenReturn(experiment1);
        when(participantRepository.findAllByExperiment(experiment1)).thenReturn(participantList);
        assertThrows(IllegalStateException.class,
                () -> participantService.deactivateParticipantAccounts(ID)
        );
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findAllByExperiment(experiment1);
        verify(userRepository).findById(any(Integer.class));
        verify(userRepository, never()).save(user);
    }

    @Test
    public void testDeactivateParticipantAccountsNotFound() {
        when(experimentRepository.getOne(ID)).thenReturn(experiment1);
        when(participantRepository.findAllByExperiment(experiment1)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> participantService.deactivateParticipantAccounts(ID)
        );
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findAllByExperiment(experiment1);
        verify(userRepository, never()).findById(any(Integer.class));
        verify(userRepository, never()).save(user);
    }

    @Test
    public void testDeactivateParticipantAccountsInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> participantService.deactivateParticipantAccounts(0)
        );
        verify(experimentRepository, never()).getOne(ID);
        verify(participantRepository, never()).findAllByExperiment(experiment1);
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

    @Test
    public void testIsInvalidParticipant() {
        user.setActive(true);
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment1);
        when(participantRepository.findByUserAndExperiment(user, experiment1)).thenReturn(Optional.of(participant1));
        assertFalse(participantService.isInvalidParticipant(ID, ID, SECRET));
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment1);
    }

    @Test
    public void testIsInvalidParticipantSecretNotMatching() {
        user.setActive(true);
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment1);
        when(participantRepository.findByUserAndExperiment(user, experiment1)).thenReturn(Optional.of(participant1));
        assertTrue(participantService.isInvalidParticipant(ID, ID, PASSWORD));
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment1);
    }

    @Test
    public void testIsInvalidParticipantUserInactive() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment1);
        when(participantRepository.findByUserAndExperiment(user, experiment1)).thenReturn(Optional.of(participant1));
        assertTrue(participantService.isInvalidParticipant(ID, ID, SECRET));
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment1);
    }

    @Test
    public void testIsInvalidParticipantExperimentInactive() {
        user.setActive(true);
        experiment1.setActive(false);
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment1);
        when(participantRepository.findByUserAndExperiment(user, experiment1)).thenReturn(Optional.of(participant1));
        assertTrue(participantService.isInvalidParticipant(ID, ID, SECRET));
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment1);
    }

    @Test
    public void testIsInvalidParticipantNoParticipant() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment1);
        assertTrue(participantService.isInvalidParticipant(ID, ID, SECRET));
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment1);
    }

    @Test
    public void testIsInvalidParticipantEntityNotFound() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment1);
        when(participantRepository.findByUserAndExperiment(user, experiment1)).thenThrow(EntityNotFoundException.class);
        assertTrue(participantService.isInvalidParticipant(ID, ID, SECRET));
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment1);
    }

    @Test
    public void testIsInvalidParticipantSecretBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> participantService.isInvalidParticipant(ID, ID, " ")
        );
        verify(userRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).getOne(anyInt());
        verify(participantRepository, never()).findByUserAndExperiment(any(), any());
    }

    @Test
    public void testIsInvalidParticipantSecretNull() {
        assertThrows(IllegalArgumentException.class,
                () -> participantService.isInvalidParticipant(ID, ID, null)
        );
        verify(userRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).getOne(anyInt());
        verify(participantRepository, never()).findByUserAndExperiment(any(), any());
    }

    @Test
    public void testIsInvalidParticipantInvalidExperimentId() {
        assertThrows(IllegalArgumentException.class,
                () -> participantService.isInvalidParticipant(ID, 0, SECRET)
        );
        verify(userRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).getOne(anyInt());
        verify(participantRepository, never()).findByUserAndExperiment(any(), any());
    }

    @Test
    public void testIsInvalidParticipantInvalidUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> participantService.isInvalidParticipant(-9, ID, SECRET)
        );
        verify(userRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).getOne(anyInt());
        verify(participantRepository, never()).findByUserAndExperiment(any(), any());
    }

    @Test
    public void testDeactivateInactiveExperiments() {
        when(experimentRepository.findAllByActiveIsTrue()).thenReturn(List.of(experiment1, experiment2));
        when(participantRepository.findAllByExperiment(experiment1)).thenReturn(List.of(participant1));
        when(participantRepository.findAllByExperiment(experiment2)).thenReturn(List.of(participant1, participant2));
        participantService.deactivateInactiveExperiments();
        assertAll(
                () -> assertTrue(experiment1.isActive()),
                () -> assertFalse(experiment2.isActive())
        );
        verify(experimentRepository).findAllByActiveIsTrue();
        verify(participantRepository).findAllByExperiment(experiment1);
        verify(participantRepository).findAllByExperiment(experiment2);
        verify(experimentRepository).save(experiment2);
    }

    @Test
    public void testDeactivateInactiveExperimentsLastStart() {
        participant2.setEnd(LocalDateTime.now());
        when(experimentRepository.findAllByActiveIsTrue()).thenReturn(List.of(experiment1, experiment2));
        when(participantRepository.findAllByExperiment(experiment1)).thenReturn(new ArrayList<>());
        when(participantRepository.findAllByExperiment(experiment2)).thenReturn(List.of(participant2));
        participantService.deactivateInactiveExperiments();
        assertAll(
                () -> assertTrue(experiment1.isActive()),
                () -> assertFalse(experiment2.isActive())
        );
        verify(experimentRepository).findAllByActiveIsTrue();
        verify(participantRepository).findAllByExperiment(experiment1);
        verify(participantRepository).findAllByExperiment(experiment2);
        verify(experimentRepository).save(experiment2);
    }

    @Test
    public void testDeactivateInactiveExperimentsLastEnd() {
        participant2.setStart(LocalDateTime.now());
        when(experimentRepository.findAllByActiveIsTrue()).thenReturn(List.of(experiment2));
        when(participantRepository.findAllByExperiment(experiment2)).thenReturn(List.of(participant2));
        participantService.deactivateInactiveExperiments();
        assertFalse(experiment2.isActive());
        verify(experimentRepository).findAllByActiveIsTrue();
        verify(participantRepository).findAllByExperiment(experiment2);
        verify(experimentRepository).save(experiment2);
    }

    private List<Participant> getParticipants(int number) {
        List<Participant> participants = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            User user = new User();
            user.setId(i + 1);
            Experiment experiment = new Experiment();
            experiment.setId(i + 1);
            experiment.setTitle("Title " + i);
            participants.add(new Participant(user, experiment, LocalDateTime.now(), null));
        }
        return participants;
    }

    private List<CourseParticipant> getCourseParticipants(int number) {
        List<CourseParticipant> participants = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            User user = new User();
            user.setId(i + 1);

            if (user.getId() == 1) {
                user.setSecret("secret");
                user.setActive(true);
            }

            participants.add(new CourseParticipant(user, course, LocalDateTime.now()));
        }
        return participants;
    }
}
