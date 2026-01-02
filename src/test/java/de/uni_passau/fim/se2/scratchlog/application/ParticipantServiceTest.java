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
import de.uni_passau.fim.se2.scratchlog.application.service.ParticipantService;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Course;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.CourseParticipant;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Experiment;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Participant;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.CourseExperimentRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.CourseParticipantRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.CourseRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ExperimentRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ParticipantRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.UserRepository;
import de.uni_passau.fim.se2.scratchlog.util.InactivityConfiguration;
import de.uni_passau.fim.se2.scratchlog.util.enums.Language;
import de.uni_passau.fim.se2.scratchlog.util.enums.Role;
import de.uni_passau.fim.se2.scratchlog.web.dto.ParticipantDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.UserDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParticipantServiceTest {

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

    private final InactivityConfiguration inactivityConfiguration = new InactivityConfiguration(30, 90, 180);

    private static final String USERNAME = "participant";
    private static final String PASSWORD = "participant1";
    private static final String EMAIL = "participant@participant.de";
    private static final String SECRET = "secret";
    private static final String GUI_URL = "scratch";
    private static final int ID = 1;
    private static final long MAX_DAYS = 90;
    private static final LocalDateTime MAX_TIME = LocalDateTime.now().minusDays(MAX_DAYS);
    private final UserDTO user1 = new UserDTO(USERNAME, "email1", Role.PARTICIPANT, Language.ENGLISH, "password1", "secret");
    private final UserDTO user2 = new UserDTO(USERNAME, "email2", Role.PARTICIPANT, Language.ENGLISH, "password2", "secret");
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
    private final List<UserDTO> users = List.of(user1, user2);

    @BeforeEach
    public void setup() {
        participantDTO.setUser(ID);
        participantDTO.setExperiment(ID);
        user.setId(ID);
        user.setActive(false);
        user.setSecret("secret");
        user.setRole(Role.PARTICIPANT);
        course.setActive(true);
        experiment1.setActive(true);
        experiment2.setActive(true);
        participant2.setStart(MAX_TIME);
        participant2.setEnd(MAX_TIME);

        participantService = new ParticipantService(
            inactivityConfiguration, userRepository, participantRepository, courseRepository,
            courseExperimentRepository, courseParticipantRepository, experimentRepository
        );
    }

    @Test
    public void testGetParticipant() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        when(participantRepository.findByUserAndExperiment(user, experiment1)).thenReturn(Optional.of(participant1));
        ParticipantDTO participantDTO = participantService.getParticipant(ID, ID);
        assertAll(
                () -> assertEquals(ID, participantDTO.getExperiment()),
                () -> assertEquals(ID, participantDTO.getUser()),
                () -> assertNull(participantDTO.getStart()),
                () -> assertNull(participantDTO.getEnd())
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment1);
    }

    @Test
    public void testGetParticipantNull() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        assertThrows(NotFoundException.class,
                () -> participantService.getParticipant(ID, ID)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment1);
    }

    @Test
    public void testGetParticipantEntityNotFound() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        when(participantRepository.findByUserAndExperiment(user, experiment1)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> participantService.getParticipant(ID, ID)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment1);
    }

    @Test
    public void testGetParticipants() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        when(participantRepository.findAllByExperiment(experiment1)).thenReturn(participantList);
        List<ParticipantDTO> participantDTOS = participantService.getParticipants(ID);
        assertAll(
                () -> assertEquals(participantList.size(), participantDTOS.size()),
                () -> assertEquals(participantDTOS.getFirst().getUser(), participantList.getFirst().getUser().getId())
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findAllByExperiment(experiment1);
    }

    @Test
    public void testAddAllCourseParticipantsToExperiment() {
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment2);
        when(courseExperimentRepository.existsByCourseAndExperiment(course, experiment2)).thenReturn(true);
        when(courseParticipantRepository.findAllByCourse(course)).thenReturn(courseParticipants);
        assertDoesNotThrow(() -> participantService.addAllCourseParticipantsToExperiment(ID, ID));
        verify(courseRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(courseExperimentRepository).existsByCourseAndExperiment(course, experiment2);
        verify(courseParticipantRepository).findAllByCourse(course);
        verify(participantRepository, times(3)).save(any());
        verify(userRepository, times(2)).save(any());
    }

    @Test
    public void testAddAllCourseParticipantsToExperimentConstraintViolation() {
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment2);
        when(courseExperimentRepository.existsByCourseAndExperiment(course, experiment2)).thenReturn(true);
        when(courseParticipantRepository.findAllByCourse(course)).thenReturn(courseParticipants);
        when(participantRepository.save(any())).thenThrow(ConstraintViolationException.class);
        assertThrows(ConstraintViolationException.class,
            () -> participantService.addAllCourseParticipantsToExperiment(ID, ID)
        );
        verify(courseRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(courseExperimentRepository).existsByCourseAndExperiment(course, experiment2);
        verify(courseParticipantRepository).findAllByCourse(course);
        verify(participantRepository).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testAddAllCourseParticipantsToExperimentEntityNotFound() {
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment2);
        when(courseExperimentRepository.existsByCourseAndExperiment(course,
                experiment2)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class, () -> participantService.addAllCourseParticipantsToExperiment(ID, ID));
        verify(courseRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(courseExperimentRepository).existsByCourseAndExperiment(course, experiment2);
        verify(courseParticipantRepository, never()).findAllByCourse(any());
        verify(participantRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testAddAllCourseExperimentNotCourseParticipantsToExperimentExperiment() {
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment2);
        assertThrows(IllegalStateException.class, () -> participantService.addAllCourseParticipantsToExperiment(ID, ID));
        verify(courseRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(courseParticipantRepository, never()).findAllByCourse(any());
        verify(participantRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testAddAllCourseParticipantsToExperimentExperimentInactive() {
        experiment1.setActive(false);
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        assertThrows(IllegalStateException.class, () -> participantService.addAllCourseParticipantsToExperiment(ID, ID));
        verify(courseRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(courseParticipantRepository, never()).findAllByCourse(any());
        verify(participantRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testAddAllCourseCourseParticipantsToExperimentInactive() {
        course.setActive(false);
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        assertThrows(IllegalStateException.class, () -> participantService.addAllCourseParticipantsToExperiment(ID, ID));
        verify(courseRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(courseParticipantRepository, never()).findAllByCourse(any());
        verify(participantRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testSaveParticipantsFromCSV() {
        user.setActive(true);
        user.setSecret(null);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        when(userRepository.findUserByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(participantRepository.existsByUserAndExperiment(user, experiment1)).thenReturn(false, true);
        assertDoesNotThrow(() -> participantService.saveParticipantsFromCSV(ID, users));
        verify(experimentRepository).getReferenceById(ID);
        verify(userRepository, times(2)).findUserByUsername(USERNAME);
        verify(participantRepository, times(2)).existsByUserAndExperiment(user, experiment1);
        verify(userRepository).save(user);
    }

    @Test
    public void testSaveParticipantsFromCSVUserInactive() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        when(userRepository.findUserByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(participantRepository.existsByUserAndExperiment(user, experiment1)).thenReturn(false, true);
        assertDoesNotThrow(() -> participantService.saveParticipantsFromCSV(ID, users));
        verify(experimentRepository).getReferenceById(ID);
        verify(userRepository, times(2)).findUserByUsername(USERNAME);
        verify(participantRepository, times(2)).existsByUserAndExperiment(user, experiment1);
        verify(userRepository).save(user);
    }

    @Test
    public void testSaveParticipantsFromCSVUserAdmin() {
        user.setRole(Role.ADMIN);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        when(userRepository.findUserByUsername(USERNAME)).thenReturn(Optional.of(user));
        assertThrows(IllegalStateException.class,
                () -> participantService.saveParticipantsFromCSV(ID, users)
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(userRepository).findUserByUsername(USERNAME);
        verify(participantRepository, never()).existsByUserAndExperiment(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testSaveParticipantsFromCSVUserNotFound() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        when(userRepository.findUserByUsername(USERNAME)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> participantService.saveParticipantsFromCSV(ID, users)
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(userRepository).findUserByUsername(USERNAME);
        verify(participantRepository, never()).existsByUserAndExperiment(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testSaveParticipantsFromCSVExperimentInactive() {
        experiment1.setActive(false);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        assertThrows(IllegalStateException.class,
                () -> participantService.saveParticipantsFromCSV(ID, users)
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(userRepository, never()).findUserByUsername(anyString());
        verify(participantRepository, never()).existsByUserAndExperiment(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testSaveParticipantsFromCSVExperimentNotFound() {
        Experiment exp = Mockito.mock(Experiment.class);
        when(experimentRepository.getReferenceById(ID)).thenReturn(exp);
        when(exp.isActive()).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> participantService.saveParticipantsFromCSV(ID, users)
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(userRepository, never()).findUserByUsername(anyString());
        verify(participantRepository, never()).existsByUserAndExperiment(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testSaveParticipant() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        assertDoesNotThrow(() -> participantService.saveParticipant(ID, ID));
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).save(any());
    }

    @Test
    public void testSaveParticipantNotFound() {
        when(participantRepository.save(any())).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> participantService.saveParticipant(ID, ID)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).save(any());
    }

    @Test
    public void testSaveParticipantForeignKeyViolation() {
        when(participantRepository.save(any())).thenThrow(ConstraintViolationException.class);
        assertThrows(ConstraintViolationException.class,
                () -> participantService.saveParticipant(ID, ID)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).save(any());
    }

    @Test
    public void testUpdateParticipant() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        assertTrue(participantService.updateParticipant(participantDTO));
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).save(any());
    }

    @Test
    public void testUpdateParticipantEntityNotFound() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        when(participantRepository.save(any())).thenThrow(EntityNotFoundException.class);
        assertFalse(participantService.updateParticipant(participantDTO));
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).save(any());
    }

    @Test
    public void testUpdateParticipantConstraintViolation() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        when(participantRepository.save(any())).thenThrow(ConstraintViolationException.class);
        assertFalse(participantService.updateParticipant(participantDTO));
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).save(any());
    }

    @Test
    public void testDeactivateParticipantAccounts() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        when(participantRepository.findAllByExperiment(experiment1)).thenReturn(participantList);
        when(userRepository.findById(any(Integer.class))).thenReturn(java.util.Optional.of(user));
        assertDoesNotThrow(() -> participantService.deactivateParticipantAccounts(ID));
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findAllByExperiment(experiment1);
        verify(userRepository, times(5)).findById(any(Integer.class));
        verify(userRepository, times(5)).save(user);
    }

    @Test
    public void testDeactivateParticipantAccountsSimultaneousParticipation() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        when(participantRepository.findAllByExperiment(experiment1)).thenReturn(participantList);
        when(userRepository.findById(any(Integer.class))).thenReturn(java.util.Optional.of(user));
        when(participantRepository.findAllByEndIsNullAndUser(any())).thenReturn(participantList);
        assertDoesNotThrow(() -> participantService.deactivateParticipantAccounts(ID));
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findAllByExperiment(experiment1);
        verify(userRepository, times(5)).findById(any(Integer.class));
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testDeactivateParticipantAccountsIllegalState() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        when(participantRepository.findAllByExperiment(experiment1)).thenReturn(participantList);
        assertThrows(IllegalStateException.class,
                () -> participantService.deactivateParticipantAccounts(ID)
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findAllByExperiment(experiment1);
        verify(userRepository).findById(any(Integer.class));
        verify(userRepository, never()).save(user);
    }

    @Test
    public void testDeactivateParticipantAccountsNotFound() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        when(participantRepository.findAllByExperiment(experiment1)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> participantService.deactivateParticipantAccounts(ID)
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findAllByExperiment(experiment1);
        verify(userRepository, never()).findById(any(Integer.class));
        verify(userRepository, never()).save(user);
    }

    @Test
    public void testGetExperimentIdsForParticipant() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(participantRepository.findAllByUser(user)).thenReturn(participantList);
        Map<Integer, String> experiments = participantService.getExperimentInfoForParticipant(ID);
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
        verify(userRepository).getReferenceById(ID);
        verify(participantRepository).findAllByUser(user);
    }

    @Test
    public void testGetExperimentIdsForParticipantNotFound() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(participantRepository.findAllByUser(user)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> participantService.getExperimentInfoForParticipant(ID)
        );
        verify(userRepository).getReferenceById(ID);
        verify(participantRepository).findAllByUser(user);
    }

    @Test
    public void testDeleteParticipant() {
        assertDoesNotThrow(() -> participantService.deleteParticipant(ID, ID));
        verify(participantRepository).deleteById(any());
    }

    @Test
    public void testSimultaneousParticipation() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(participantRepository.findAllByEndIsNullAndUser(user)).thenReturn(participantList);
        assertTrue(participantService.simultaneousParticipation(ID));
        verify(userRepository).getReferenceById(ID);
        verify(participantRepository).findAllByEndIsNullAndUser(user);
    }

    @Test
    public void testSimultaneousParticipationFalse() {
        List<Participant> oneParticipation = new ArrayList<>();
        oneParticipation.add(new Participant());
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(participantRepository.findAllByEndIsNullAndUser(user)).thenReturn(oneParticipation);
        assertFalse(participantService.simultaneousParticipation(ID));
        verify(userRepository).getReferenceById(ID);
        verify(participantRepository).findAllByEndIsNullAndUser(user);
    }

    @Test
    public void testSimultaneousParticipationNotFound() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(participantRepository.findAllByEndIsNullAndUser(user)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> participantService.simultaneousParticipation(ID)
        );
        verify(userRepository).getReferenceById(ID);
        verify(participantRepository).findAllByEndIsNullAndUser(user);
    }

    @Test
    public void testIsInvalidParticipant() {
        user.setActive(true);
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        when(participantRepository.findByUserAndExperiment(user, experiment1)).thenReturn(Optional.of(participant1));
        assertFalse(participantService.isInvalidParticipant(ID, ID, SECRET, true));
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment1);
    }

    @Test
    public void testIsInvalidParticipantInactive() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        when(participantRepository.findByUserAndExperiment(user, experiment1)).thenReturn(Optional.of(participant1));
        assertFalse(participantService.isInvalidParticipant(ID, ID, SECRET, false));
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment1);
    }

    @Test
    public void testIsInvalidParticipantSecretNotMatching() {
        user.setActive(true);
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        when(participantRepository.findByUserAndExperiment(user, experiment1)).thenReturn(Optional.of(participant1));
        assertTrue(participantService.isInvalidParticipant(ID, ID, PASSWORD, true));
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment1);
    }

    @Test
    public void testIsInvalidParticipantUserInactive() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        when(participantRepository.findByUserAndExperiment(user, experiment1)).thenReturn(Optional.of(participant1));
        assertTrue(participantService.isInvalidParticipant(ID, ID, SECRET, true));
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment1);
    }

    @Test
    public void testIsInvalidParticipantExperimentInactive() {
        user.setActive(true);
        experiment1.setActive(false);
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        when(participantRepository.findByUserAndExperiment(user, experiment1)).thenReturn(Optional.of(participant1));
        assertTrue(participantService.isInvalidParticipant(ID, ID, SECRET, true));
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment1);
    }

    @Test
    public void testIsInvalidParticipantNoParticipant() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        assertTrue(participantService.isInvalidParticipant(ID, ID, SECRET, true));
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment1);
    }

    @Test
    public void testIsInvalidParticipantEntityNotFound() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        when(participantRepository.findByUserAndExperiment(user, experiment1)).thenThrow(EntityNotFoundException.class);
        assertTrue(participantService.isInvalidParticipant(ID, ID, SECRET, true));
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment1);
    }

    @Test
    public void testIsInvalidParticipantSecretBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> participantService.isInvalidParticipant(ID, ID, " ", true)
        );
        verify(userRepository, never()).getReferenceById(anyInt());
        verify(experimentRepository, never()).getReferenceById(anyInt());
        verify(participantRepository, never()).findByUserAndExperiment(any(), any());
    }

    @Test
    public void testIsInvalidParticipantSecretNull() {
        assertThrows(IllegalArgumentException.class,
                () -> participantService.isInvalidParticipant(ID, ID, null, true)
        );
        verify(userRepository, never()).getReferenceById(anyInt());
        verify(experimentRepository, never()).getReferenceById(anyInt());
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
