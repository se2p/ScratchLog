/*
 * This file is part of ScratchLog.
 * Licenced under the GPL v3.0 or later.
 *
 * SPDX-FileCopyrightText: 2021-2026 Scratchlog contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * ScratchLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ScratchLog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.uni_passau.fim.se2.scratchlog.application;

import de.uni_passau.fim.se2.scratchlog.application.exception.NotFoundException;
import de.uni_passau.fim.se2.scratchlog.application.service.CourseService;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Course;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.CourseExperiment;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.CourseParticipant;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.CourseParticipantId;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Experiment;
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
import de.uni_passau.fim.se2.scratchlog.web.dto.CourseDTO;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    private CourseService courseService;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseParticipantRepository courseParticipantRepository;

    @Mock
    private CourseExperimentRepository courseExperimentRepository;

    @Mock
    private ExperimentRepository experimentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ParticipantRepository participantRepository;

    private final InactivityConfiguration inactivityConfiguration = new InactivityConfiguration(30, 90, 180);

    private static final String TITLE = "My Course";
    private static final String DESCRIPTION = "A description";
    private static final String CONTENT = "content";
    private static final String USERNAME = "user";
    private static final String BLANK = "    ";
    private static final int ID = 1;
    private static final int INVALID_ID = 2;
    private static final long MAX_DAYS = 180;
    private static final LocalDateTime DATE = LocalDateTime.now();
    private final CourseDTO courseDTO = new CourseDTO(ID, TITLE, DESCRIPTION, CONTENT, false, LocalDateTime.now());
    private final Course course = new Course(ID, TITLE, DESCRIPTION, CONTENT, false, DATE);
    private final Experiment experiment1 = new Experiment(ID, TITLE, DESCRIPTION, "", "", false, false, "url");
    private final Experiment experiment2 = new Experiment(ID, TITLE, DESCRIPTION, "", "", false, true, "url");
    private final UserDTO user1 = new UserDTO(USERNAME, "email1", Role.PARTICIPANT, Language.ENGLISH, "password1", "secret");
    private final UserDTO user2 = new UserDTO(USERNAME, "email2", Role.PARTICIPANT, Language.ENGLISH, "password2", "secret");
    private final User user = new User(USERNAME, "email", Role.PARTICIPANT, Language.ENGLISH, "password", "secret");
    private final CourseExperiment courseExperiment = new CourseExperiment(course, experiment2, DATE);
    private final CourseParticipant courseParticipant = new CourseParticipant(user, course, DATE);
    private final List<UserDTO> users = List.of(user1, user2);

    @BeforeEach
    public void setUp() {
        courseDTO.setId(ID);
        courseDTO.setTitle(TITLE);
        courseDTO.setDescription(DESCRIPTION);
        courseDTO.setLastChanged(LocalDateTime.now());
        course.setId(ID);
        course.setActive(false);
        course.setLastChanged(DATE);
        experiment1.setActive(false);
        experiment2.setActive(false);
        user.setRole(Role.PARTICIPANT);
        user.setId(ID);
        user.setActive(false);
        user.setSecret("secret");

        courseService = new CourseService(
            inactivityConfiguration, courseRepository, courseParticipantRepository, courseExperimentRepository,
            experimentRepository, userRepository, participantRepository
        );
    }

    @Test
    public void testExistsActiveCourse() {
        course.setActive(true);
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        assertTrue(courseService.existsActiveCourse(ID));
        verify(courseRepository).getReferenceById(ID);
    }

    @Test
    public void testExistsActiveCourseInactive() {
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        assertFalse(courseService.existsActiveCourse(ID));
        verify(courseRepository).getReferenceById(ID);
    }

    @Test
    public void testExistsActiveCourseEntityNotFound() {
        when(courseRepository.getReferenceById(ID)).thenThrow(EntityNotFoundException.class);
        assertFalse(courseService.existsActiveCourse(ID));
        verify(courseRepository).getReferenceById(ID);
    }

    @Test
    public void testExistsCourseByTitle() {
        when(courseRepository.existsByTitle(TITLE)).thenReturn(true);
        assertAll(
                () -> assertTrue(courseService.existsCourse(TITLE)),
                () -> assertFalse(courseService.existsCourse(DESCRIPTION))
        );
        verify(courseRepository).existsByTitle(TITLE);
        verify(courseRepository).existsByTitle(DESCRIPTION);
    }

    @Test
    public void testExistsCourseByTitleAndId() {
        when(courseRepository.findByTitle(TITLE)).thenReturn(Optional.of(course));
        assertAll(
                () -> assertFalse(courseService.existsCourse(ID, TITLE)),
                () -> assertTrue(courseService.existsCourse(2, TITLE))
        );
        verify(courseRepository, times(2)).findByTitle(TITLE);
    }

    @Test
    public void testExistsCourseByTitleAndIdNoCourse() {
        assertFalse(courseService.existsCourse(ID, TITLE));
        verify(courseRepository).findByTitle(TITLE);
    }

    @Test
    public void testExistsCourseExperiment() {
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        when(experimentRepository.findByTitle(TITLE)).thenReturn(Optional.of(experiment1));
        when(courseExperimentRepository.existsByCourseAndExperiment(course, experiment1)).thenReturn(true);
        assertTrue(courseService.existsCourseExperiment(ID, TITLE));
        verify(courseRepository).getReferenceById(ID);
        verify(experimentRepository).findByTitle(TITLE);
        verify(courseExperimentRepository).existsByCourseAndExperiment(course, experiment1);
    }

    @Test
    public void testExistsCourseExperimentNoEntry() {
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        when(experimentRepository.findByTitle(TITLE)).thenReturn(Optional.of(experiment1));
        assertFalse(courseService.existsCourseExperiment(ID, TITLE));
        verify(courseRepository).getReferenceById(ID);
        verify(experimentRepository).findByTitle(TITLE);
        verify(courseExperimentRepository).existsByCourseAndExperiment(course, experiment1);
    }

    @Test
    public void testExistsCourseExperimentEntityNotFound() {
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        when(experimentRepository.findByTitle(TITLE)).thenReturn(Optional.of(experiment1));
        when(courseExperimentRepository.existsByCourseAndExperiment(course, experiment1)).thenThrow(
                EntityNotFoundException.class);
        assertFalse(courseService.existsCourseExperiment(ID, TITLE));
        verify(courseRepository).getReferenceById(ID);
        verify(experimentRepository).findByTitle(TITLE);
        verify(courseExperimentRepository).existsByCourseAndExperiment(course, experiment1);
    }

    @Test
    public void testExistsCourseExperimentNull() {
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        assertFalse(courseService.existsCourseExperiment(ID, TITLE));
        verify(courseRepository).getReferenceById(ID);
        verify(experimentRepository).findByTitle(TITLE);
        verify(courseExperimentRepository, never()).existsByCourseAndExperiment(any(), any());
    }

    @Test
    public void testExistsCourseParticipant() {
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        when(userRepository.findUserByUsernameOrEmail(USERNAME, USERNAME)).thenReturn(Optional.of(user));
        when(courseParticipantRepository.existsByCourseAndUser(course, user)).thenReturn(true);
        assertTrue(courseService.existsCourseParticipant(ID, USERNAME));
        verify(courseRepository).getReferenceById(ID);
        verify(userRepository).findUserByUsernameOrEmail(USERNAME, USERNAME);
        verify(courseParticipantRepository).existsByCourseAndUser(course, user);
    }

    @Test
    public void testExistsCourseParticipantNoParticipant() {
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        when(userRepository.findUserByUsernameOrEmail(USERNAME, USERNAME)).thenReturn(Optional.of(user));
        assertFalse(courseService.existsCourseParticipant(ID, USERNAME));
        verify(courseRepository).getReferenceById(ID);
        verify(userRepository).findUserByUsernameOrEmail(USERNAME, USERNAME);
        verify(courseParticipantRepository).existsByCourseAndUser(course, user);
    }

    @Test
    public void testExistsCourseParticipantNoUser() {
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        assertFalse(courseService.existsCourseParticipant(ID, USERNAME));
        verify(courseRepository).getReferenceById(ID);
        verify(userRepository).findUserByUsernameOrEmail(USERNAME, USERNAME);
        verify(courseParticipantRepository, never()).existsByCourseAndUser(any(), any());
    }

    @Test
    public void testExistsCourseParticipantNotFound() {
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        when(userRepository.findUserByUsernameOrEmail(USERNAME, USERNAME)).thenReturn(Optional.of(user));
        when(courseParticipantRepository.existsByCourseAndUser(course, user)).thenThrow(EntityNotFoundException.class);
        assertFalse(courseService.existsCourseParticipant(ID, USERNAME));
        verify(courseRepository).getReferenceById(ID);
        verify(userRepository).findUserByUsernameOrEmail(USERNAME, USERNAME);
        verify(courseParticipantRepository).existsByCourseAndUser(course, user);
    }

    @Test
    public void testExistsCourseParticipantExperiment() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(courseExperimentRepository.findByExperiment(experiment1)).thenReturn(Optional.of(courseExperiment));
        when(courseParticipantRepository.existsByCourseAndUser(course, user)).thenReturn(true);
        assertTrue(courseService.existsCourseParticipant(ID, ID));
        verify(experimentRepository).getReferenceById(ID);
        verify(userRepository).getReferenceById(ID);
        verify(courseExperimentRepository).findByExperiment(experiment1);
        verify(courseParticipantRepository).existsByCourseAndUser(course, user);
    }

    @Test
    public void testExistsCourseParticipantNoCourseExperiment() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(courseExperimentRepository.findByExperiment(experiment1)).thenReturn(Optional.empty());
        assertFalse(courseService.existsCourseParticipant(ID, ID));
        verify(experimentRepository).getReferenceById(ID);
        verify(userRepository).getReferenceById(ID);
        verify(courseExperimentRepository).findByExperiment(experiment1);
        verify(courseParticipantRepository, never()).existsByCourseAndUser(any(), any());
    }

    @Test
    public void testExistsCourseParticipantExperimentEntityNotFound() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(courseExperimentRepository.findByExperiment(experiment1)).thenThrow(EntityNotFoundException.class);
        assertFalse(courseService.existsCourseParticipant(ID, ID));
        verify(experimentRepository).getReferenceById(ID);
        verify(userRepository).getReferenceById(ID);
        verify(courseExperimentRepository).findByExperiment(experiment1);
        verify(courseParticipantRepository, never()).existsByCourseAndUser(any(), any());
    }

    @Test
    public void testExistsInactiveExperiment() {
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        when(courseExperimentRepository.findAllByCourse(course)).thenReturn(List.of(courseExperiment));
        assertTrue(courseService.existsInactiveExperiment(ID));
        verify(courseRepository).getReferenceById(ID);
        verify(courseExperimentRepository).findAllByCourse(course);
    }

    @Test
    public void testExistsInactiveExperimentNotFound() {
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        when(courseExperimentRepository.findAllByCourse(course)).thenThrow(EntityNotFoundException.class);
        assertFalse(courseService.existsInactiveExperiment(ID));
        verify(courseRepository).getReferenceById(ID);
        verify(courseExperimentRepository).findAllByCourse(course);
    }

    @Test
    public void testIsActiveCourse() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        when(courseExperimentRepository.findByExperiment(experiment1)).thenReturn(Optional.of(courseExperiment));
        assertFalse(courseService.isActiveCourse(ID));
        verify(experimentRepository).getReferenceById(ID);
        verify(courseExperimentRepository).findByExperiment(experiment1);
    }

    @Test
    public void testSaveCourse() {
        when(courseRepository.save(any())).thenReturn(course);
        assertEquals(ID, courseService.saveCourse(courseDTO));
        verify(courseRepository).save(any());
    }

    @Test
    public void testSaveCourseInvalidTitle() {
        courseDTO.setTitle(BLANK);
        assertThrows(IllegalArgumentException.class,
                () -> courseService.saveCourse(courseDTO)
        );
        verify(courseRepository, never()).save(any());
    }

    @Test
    public void testSaveCourseTitleNull() {
        courseDTO.setTitle(null);
        assertThrows(IllegalArgumentException.class,
                () -> courseService.saveCourse(courseDTO)
        );
        verify(courseRepository, never()).save(any());
    }

    @Test
    public void testSaveCourseInvalidDescription() {
        courseDTO.setDescription(BLANK);
        assertThrows(IllegalArgumentException.class,
                () -> courseService.saveCourse(courseDTO)
        );
        verify(courseRepository, never()).save(any());
    }

    @Test
    public void testSaveCourseDescriptionNull() {
        courseDTO.setDescription(null);
        assertThrows(IllegalArgumentException.class,
                () -> courseService.saveCourse(courseDTO)
        );
        verify(courseRepository, never()).save(any());
    }

    @Test
    public void testSaveCourseLastChangedNull() {
        courseDTO.setLastChanged(null);
        assertThrows(IllegalArgumentException.class,
                () -> courseService.saveCourse(courseDTO)
        );
        verify(courseRepository, never()).save(any());
    }

    @Test
    public void testDeleteCourse() {
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        when(courseExperimentRepository.findAllByCourse(course)).thenReturn(List.of(courseExperiment));
        assertDoesNotThrow(
                () -> courseService.deleteCourse(ID)
        );
        verify(courseRepository).getReferenceById(ID);
        verify(courseExperimentRepository).findAllByCourse(course);
        verify(courseExperimentRepository).deleteAll(any());
        verify(experimentRepository).delete(courseExperiment.getExperiment());
        verify(courseRepository).deleteById(ID);
    }

    @Test
    public void testDeleteCourseEntityNotFound() {
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        when(courseExperimentRepository.findAllByCourse(course)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> courseService.deleteCourse(ID)
        );
        verify(courseRepository).getReferenceById(ID);
        verify(courseExperimentRepository).findAllByCourse(course);
        verify(courseExperimentRepository, never()).deleteAll(any());
        verify(experimentRepository, never()).delete(any());
        verify(courseRepository, never()).deleteById(anyInt());
    }

    @Test
    public void testDeleteCourseParticipant() {
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        when(userRepository.findUserByUsernameOrEmail(USERNAME, USERNAME)).thenReturn(Optional.of(user));
        when(courseExperimentRepository.findAllByCourse(course)).thenReturn(List.of(courseExperiment,
                courseExperiment));
        when(participantRepository.existsByUserAndExperiment(user, experiment2)).thenReturn(true, false);
        courseService.deleteCourseParticipant(ID, USERNAME);
        assertTrue(course.getLastChanged().isAfter(DATE));
        verify(courseRepository).getReferenceById(ID);
        verify(userRepository).findUserByUsernameOrEmail(USERNAME, USERNAME);
        verify(courseExperimentRepository).findAllByCourse(course);
        verify(participantRepository, times(2)).existsByUserAndExperiment(user, experiment2);
        verify(participantRepository).deleteById(any());
        verify(courseParticipantRepository).deleteById(any());
        verify(courseRepository).save(course);
    }

    @Test
    public void testDeleteCourseParticipantEntityNotFound() {
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        when(userRepository.findUserByUsernameOrEmail(USERNAME, USERNAME)).thenReturn(Optional.of(user));
        when(courseExperimentRepository.findAllByCourse(course)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> courseService.deleteCourseParticipant(ID, USERNAME)
        );
        verify(courseRepository).getReferenceById(ID);
        verify(userRepository).findUserByUsernameOrEmail(USERNAME, USERNAME);
        verify(courseExperimentRepository).findAllByCourse(course);
        verify(participantRepository, never()).existsByUserAndExperiment(any(), any());
        verify(participantRepository, never()).deleteById(any());
        verify(courseParticipantRepository, never()).deleteById(any());
        verify(courseRepository, never()).save(any());
    }

    @Test
    public void testDeleteCourseParticipantNoUser() {
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        assertThrows(NotFoundException.class,
                () -> courseService.deleteCourseParticipant(ID, USERNAME)
        );
        verify(courseRepository).getReferenceById(ID);
        verify(userRepository).findUserByUsernameOrEmail(USERNAME, USERNAME);
        verify(courseExperimentRepository, never()).findAllByCourse(any());
        verify(participantRepository, never()).existsByUserAndExperiment(any(), any());
        verify(participantRepository, never()).deleteById(any());
        verify(courseParticipantRepository, never()).deleteById(any());
        verify(courseRepository, never()).save(any());
    }

    @Test
    public void testDeleteCourseParticipants() {
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        when(userRepository.findUserByUsernameOrEmail(user1.getUsername(), user1.getUsername()))
            .thenReturn(Optional.of(user));
        when(userRepository.findUserByUsernameOrEmail(user2.getUsername(), user2.getUsername()))
            .thenReturn(Optional.of(user));
        courseService.deleteCourseParticipants(ID, users.stream().map(UserDTO::getUsername).toList());
        verify(courseParticipantRepository, times(2)).deleteById(new CourseParticipantId(ID, ID));
    }

    @Test
    public void testSaveCourseExperiment() {
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        when(experimentRepository.getReferenceById(INVALID_ID)).thenReturn(experiment1);
        courseService.saveCourseExperiment(ID, INVALID_ID);
        assertAll(
                () -> assertTrue(experiment1.isActive()),
                () -> assertTrue(course.isActive()),
                () -> assertTrue(course.getLastChanged().isAfter(DATE))
        );
        verify(courseRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(INVALID_ID);
        verify(courseExperimentRepository).save(any());
        verify(courseRepository).save(course);
        verify(experimentRepository).save(experiment1);
    }

    @Test
    public void testSaveCourseExperimentConstraintViolation() {
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        when(experimentRepository.getReferenceById(INVALID_ID)).thenReturn(experiment1);
        when(courseExperimentRepository.save(any())).thenThrow(ConstraintViolationException.class);
        assertThrows(ConstraintViolationException.class,
                () -> courseService.saveCourseExperiment(ID, INVALID_ID)
        );
        verify(courseRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(INVALID_ID);
        verify(courseExperimentRepository).save(any());
        verify(courseRepository, never()).save(any());
        verify(experimentRepository, never()).save(any());
    }

    @Test
    public void testSaveCourseExperimentEntityNotFound() {
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        when(experimentRepository.getReferenceById(INVALID_ID)).thenReturn(experiment1);
        when(courseExperimentRepository.save(any())).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> courseService.saveCourseExperiment(ID, INVALID_ID)
        );
        verify(courseRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(INVALID_ID);
        verify(courseExperimentRepository).save(any());
        verify(courseRepository, never()).save(any());
        verify(experimentRepository, never()).save(any());
    }

    @Test
    public void testDeleteCourseExperiment() {
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        when(experimentRepository.findByTitle(TITLE)).thenReturn(Optional.of(experiment1));
        courseService.deleteCourseExperiment(ID, TITLE);
        assertTrue(course.getLastChanged().isAfter(DATE));
        verify(courseRepository).getReferenceById(ID);
        verify(experimentRepository).findByTitle(TITLE);
        verify(courseExperimentRepository).deleteById(any());
        verify(courseRepository).save(course);
    }

    @Test
    public void testDeleteCourseExperimentEntityNotFound() {
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        when(experimentRepository.findByTitle(TITLE)).thenReturn(Optional.of(experiment1));
        when(courseRepository.save(course)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> courseService.deleteCourseExperiment(ID, TITLE)
        );
        verify(courseRepository).getReferenceById(ID);
        verify(experimentRepository).findByTitle(TITLE);
        verify(courseExperimentRepository).deleteById(any());
        verify(courseRepository).save(course);
    }

    @Test
    public void testDeleteCourseExperimentNull() {
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        assertThrows(NotFoundException.class,
                () -> courseService.deleteCourseExperiment(ID, TITLE)
        );
        verify(courseRepository).getReferenceById(ID);
        verify(experimentRepository).findByTitle(TITLE);
        verify(courseExperimentRepository, never()).deleteById(any());
        verify(courseRepository, never()).save(any());
    }

    @Test
    public void testGetCourse() {
        when(courseRepository.findById(ID)).thenReturn(Optional.of(course));
        CourseDTO foundCourse = courseService.getCourse(ID);
        assertAll(
                () -> assertEquals(course.getId(), foundCourse.getId()),
                () -> assertEquals(course.getTitle(), foundCourse.getTitle()),
                () -> assertEquals(course.getDescription(), foundCourse.getDescription()),
                () -> assertEquals(course.getContent(), foundCourse.getContent()),
                () -> assertEquals(course.isActive(), foundCourse.isActive()),
                () -> assertEquals(course.getLastChanged(), foundCourse.getLastChanged())
        );
        verify(courseRepository).findById(ID);
    }

    @Test
    public void testGetCourseNotFound() {
        assertThrows(NotFoundException.class,
                () -> courseService.getCourse(ID)
        );
        verify(courseRepository).findById(ID);
    }

    @Test
    public void testGetCourseIdForExperiment() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        when(courseExperimentRepository.findByExperiment(experiment1)).thenReturn(Optional.of(courseExperiment));
        assertEquals(course.getId(), courseService.getCourseIdForExperiment(ID));
        verify(experimentRepository).getReferenceById(ID);
        verify(courseExperimentRepository).findByExperiment(experiment1);
    }

    @Test
    public void testGetCourseIdForExperimentNoCourseExperiment() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        when(courseExperimentRepository.findByExperiment(experiment1)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> courseService.getCourseIdForExperiment(ID)
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(courseExperimentRepository).findByExperiment(experiment1);
    }

    @Test
    public void testGetCourseIdForExperimentNotFound() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment1);
        when(courseExperimentRepository.findByExperiment(experiment1)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> courseService.getCourseIdForExperiment(ID)
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(courseExperimentRepository).findByExperiment(experiment1);
    }

    @Test
    public void testChangeCourseStatus() {
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        when(courseParticipantRepository.findAllByCourse(course)).thenReturn(List.of(courseParticipant));
        when(courseRepository.save(course)).thenReturn(course);
        courseService.changeCourseStatus(true, ID);
        assertAll(
                () -> assertTrue(course.isActive()),
                () -> assertTrue(course.getLastChanged().isAfter(DATE)),
                () -> assertTrue(user.isActive())
        );
        verify(courseRepository).getReferenceById(ID);
        verify(courseParticipantRepository).findAllByCourse(course);
        verify(courseExperimentRepository, never()).findAllByCourse(any());
        verify(experimentRepository, never()).updateStatusById(anyInt(), anyBoolean());
        verify(userRepository).save(user);
        verify(courseRepository).save(course);
    }

    @Test
    public void testChangeCourseStatusInactive() {
        course.setActive(true);
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        when(courseExperimentRepository.findAllByCourse(course)).thenReturn(List.of(courseExperiment));
        when(courseParticipantRepository.findAllByCourse(course)).thenReturn(List.of(courseParticipant));
        when(courseRepository.save(course)).thenReturn(course);
        courseService.changeCourseStatus(false, ID);
        assertAll(
                () -> assertFalse(course.isActive()),
                () -> assertTrue(course.getLastChanged().isAfter(DATE)),
                () -> assertFalse(user.isActive()),
                () -> assertNull(user.getSecret())
        );
        verify(courseRepository).getReferenceById(ID);
        verify(courseParticipantRepository).findAllByCourse(course);
        verify(courseExperimentRepository).findAllByCourse(course);
        verify(experimentRepository).updateStatusById(ID, false);
        verify(userRepository).save(user);
        verify(courseRepository).save(course);
    }

    @Test
    public void testChangeCourseStatusEntityNotFound() {
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        when(courseParticipantRepository.findAllByCourse(course)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> courseService.changeCourseStatus(true, ID)
        );
        verify(courseRepository).getReferenceById(ID);
        verify(courseParticipantRepository).findAllByCourse(course);
        verify(courseExperimentRepository, never()).findAllByCourse(any());
        verify(experimentRepository, never()).updateStatusById(anyInt(), anyBoolean());
        verify(userRepository, never()).save(any());
        verify(courseRepository, never()).save(any());
    }

    @Test
    public void testDeactivateInactiveCourses() {
        course.setActive(true);
        course.setLastChanged(LocalDateTime.now().minusDays(MAX_DAYS));
        when(courseRepository.findAllByActiveIsTrue()).thenReturn(List.of(course));
        when(courseExperimentRepository.findAllByCourse(course)).thenReturn(List.of(courseExperiment,
                courseExperiment));
        courseService.deactivateInactiveCourses();
        assertFalse(course.isActive());
        verify(courseRepository).findAllByActiveIsTrue();
        verify(courseExperimentRepository).findAllByCourse(course);
        verify(courseRepository).save(course);
    }

    @Test
    public void testDeactivateInactiveCoursesLastChanged() {
        course.setActive(true);
        when(courseRepository.findAllByActiveIsTrue()).thenReturn(List.of(course));
        when(courseExperimentRepository.findAllByCourse(course)).thenReturn(List.of(courseExperiment));
        courseService.deactivateInactiveCourses();
        assertTrue(course.isActive());
        verify(courseRepository).findAllByActiveIsTrue();
        verify(courseExperimentRepository).findAllByCourse(course);
        verify(courseRepository, never()).save(any());
    }

    @Test
    public void testDeactivateInactiveCoursesExperimentActive() {
        course.setActive(true);
        course.setLastChanged(LocalDateTime.now().minusDays(MAX_DAYS));
        experiment2.setActive(true);
        when(courseRepository.findAllByActiveIsTrue()).thenReturn(List.of(course));
        when(courseExperimentRepository.findAllByCourse(course)).thenReturn(List.of(courseExperiment));
        courseService.deactivateInactiveCourses();
        assertTrue(course.isActive());
        verify(courseRepository).findAllByActiveIsTrue();
        verify(courseExperimentRepository).findAllByCourse(course);
        verify(courseRepository, never()).save(any());
    }

    @Test
    public void testDeactivateInactiveCoursesNoExperiments() {
        course.setActive(true);
        course.setLastChanged(LocalDateTime.now().minusDays(MAX_DAYS));
        when(courseRepository.findAllByActiveIsTrue()).thenReturn(List.of(course));
        courseService.deactivateInactiveCourses();
        assertTrue(course.isActive());
        verify(courseRepository).findAllByActiveIsTrue();
        verify(courseExperimentRepository).findAllByCourse(course);
        verify(courseRepository, never()).save(any());
    }

}
