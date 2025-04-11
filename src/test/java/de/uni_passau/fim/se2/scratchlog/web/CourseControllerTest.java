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

package de.uni_passau.fim.se2.scratchlog.web;

import de.uni_passau.fim.se2.scratchlog.StringCreator;
import de.uni_passau.fim.se2.scratchlog.application.exception.NotFoundException;
import de.uni_passau.fim.se2.scratchlog.application.service.CourseService;
import de.uni_passau.fim.se2.scratchlog.application.service.ExperimentService;
import de.uni_passau.fim.se2.scratchlog.application.service.PageService;
import de.uni_passau.fim.se2.scratchlog.application.service.UserService;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.CourseParticipant;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.CourseExperimentProjection;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.ExperimentTableProjection;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import de.uni_passau.fim.se2.scratchlog.util.enums.Language;
import de.uni_passau.fim.se2.scratchlog.util.enums.Role;
import de.uni_passau.fim.se2.scratchlog.web.controller.CourseController;
import de.uni_passau.fim.se2.scratchlog.web.dto.CourseDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.PasswordDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.UserDTO;
import de.uni_passau.fim.se2.scratchlog.web.error_handling.InvalidIdException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.scratchlog.util.CommonAssertions.assertInvalidIdException;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourseControllerTest {

    @InjectMocks
    private CourseController courseController;

    @Mock
    private CourseService courseService;

    @Mock
    private ExperimentService experimentService;

    @Mock
    private UserService userService;

    @Mock
    private PageService pageService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private MockedStatic<SecurityContextHolder> securityContextHolder;
    private static final String COURSE = "course";
    private static final String REDIRECT_COURSE = "redirect:/course?id=";
    private static final String COURSE_EDIT = "course-edit";
    private static final String SUCCESS = "redirect:/?success=true";
    private static final String REDIRECT_INVALID = "redirect:/course?invalid=true&id=";
    private static final String EXPERIMENT_TABLE = "course::course_experiment_table";
    private static final String PARTICIPANT_TABLE = "course::course_participant_table";
    private static final String COURSE_DTO = "courseDTO";
    private static final int CURRENT = 3;
    private static final int LAST = 5;
    private static final int ID = 1;
    private static final int LAST_PAGE = 5;
    private static final String TITLE = "Title";
    private static final String DESCRIPTION = "Description";
    private static final String CONTENT = "content";
    private static final String USERNAME = "participant";
    private static final List<String> USERNAMES = List.of(USERNAME, "participant2");
    private static final String PASSWORD = "password";
    private static final String ERROR = "error";
    private static final LocalDateTime CHANGED = LocalDateTime.now();
    private final PasswordDTO passwordDTO = new PasswordDTO(PASSWORD);
    private final CourseDTO courseDTO = new CourseDTO(ID, TITLE, DESCRIPTION, CONTENT, true, CHANGED);
    private final UserDTO userDTO = new UserDTO(USERNAME, "part@part.de", Role.PARTICIPANT, Language.ENGLISH, PASSWORD,
            "secret");
    private final UserDTO userDTO2 = new UserDTO(USERNAMES.get(1), "part@part.com", Role.PARTICIPANT, Language.GERMAN,
        PASSWORD, "secret");
    private final List<UserDTO> userDTOs = USERNAMES.stream()
        .map(username -> new UserDTO(username, null, null, null, null, null)).toList();
    private final Page<CourseExperimentProjection> experiments = new PageImpl<>(getCourseExperiments(3));
    private final Page<CourseParticipant> participants = new PageImpl<>(new ArrayList<>());

    @BeforeEach
    public void setUp() {
        courseDTO.setId(ID);
        courseDTO.setTitle(TITLE);
        courseDTO.setDescription(DESCRIPTION);
        courseDTO.setContent(CONTENT);
        courseDTO.setActive(true);
        courseDTO.setLastChanged(CHANGED);
        userDTO.setRole(Role.PARTICIPANT);
        passwordDTO.setPassword(PASSWORD);
        securityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
    }

    @AfterEach
    public void cleanup() {
        securityContextHolder.close();
    }

    @Test
    public void testGetCourse() {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)).thenReturn(true);
        assertEquals(COURSE, courseController.getCourse(ID, model, httpServletRequest));
        verify(courseService).getCourse(ID);
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(pageService).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(pageService).getLastCourseExperimentPage(ID);
        verify(pageService).getParticipantCoursePage(anyInt(), any(PageRequest.class));
        verify(pageService).getLastParticipantCoursePage(ID);
        verify(model, times(8)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetCourseParticipant() {
        courseDTO.setContent(null);
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        assertEquals(COURSE, courseController.getCourse(ID, model, httpServletRequest));
        verify(courseService).getCourse(ID);
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(pageService).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(pageService).getLastCourseExperimentPage(ID);
        verify(pageService, never()).getParticipantCoursePage(anyInt(), any(PageRequest.class));
        verify(pageService, never()).getLastParticipantCoursePage(anyInt());
        verify(model, times(8)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetCourseNotFound() {
        when(courseService.getCourse(ID)).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, courseController.getCourse(ID, model, httpServletRequest));
        verify(courseService).getCourse(ID);
        verify(httpServletRequest, never()).isUserInRole(anyString());
        verify(pageService, never()).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(pageService, never()).getLastCourseExperimentPage(anyInt());
        verify(pageService, never()).getParticipantCoursePage(anyInt(), any(PageRequest.class));
        verify(pageService, never()).getLastParticipantCoursePage(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetCourseInvalidId() {
        assertInvalidIdException(() -> courseController.getCourse(-1, model, httpServletRequest));
        verify(courseService, never()).getCourse(anyInt());
        verify(httpServletRequest, never()).isUserInRole(anyString());
        verify(pageService, never()).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(pageService, never()).getLastCourseExperimentPage(anyInt());
        verify(pageService, never()).getParticipantCoursePage(anyInt(), any(PageRequest.class));
        verify(pageService, never()).getLastParticipantCoursePage(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetCourseForm() {
        assertEquals(COURSE_EDIT, courseController.getCourseForm(new CourseDTO()));
    }

    @Test
    public void testGetEditCourseForm() {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        assertEquals(COURSE_EDIT, courseController.getEditCourseForm(ID, model));
        verify(courseService).getCourse(ID);
        verify(model).addAttribute(COURSE_DTO, courseDTO);
    }

    @Test
    public void testGetEditCourseFormNotFound() {
        when(courseService.getCourse(ID)).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, courseController.getEditCourseForm(ID, model));
        verify(courseService).getCourse(ID);
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetEditCourseFormInvalidId() {
        assertInvalidIdException(() -> courseController.getEditCourseForm(0, model));
        verify(courseService, never()).getCourse(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testUpdateCourse() {
        when(courseService.saveCourse(courseDTO)).thenReturn(ID);
        assertAll(
                () -> assertEquals(REDIRECT_COURSE + ID, courseController.updateCourse(courseDTO, bindingResult)),
                () -> assertTrue(courseDTO.getLastChanged().isAfter(CHANGED))
        );
        verify(bindingResult, never()).addError(any());
        verify(courseService).existsCourse(ID, TITLE);
        verify(courseService).saveCourse(courseDTO);
    }

    @Test
    public void testUpdateNewCourse() {
        courseDTO.setId(null);
        when(courseService.saveCourse(courseDTO)).thenReturn(ID);
        assertAll(
                () -> assertEquals(REDIRECT_COURSE + ID, courseController.updateCourse(courseDTO, bindingResult)),
                () -> assertTrue(courseDTO.getLastChanged().isAfter(CHANGED))
        );
        verify(bindingResult, never()).addError(any());
        verify(courseService).existsCourse(TITLE);
        verify(courseService).saveCourse(courseDTO);
    }

    @Test
    public void testUpdateCourseExists() {
        when(courseService.existsCourse(ID, TITLE)).thenReturn(true);
        when(bindingResult.hasErrors()).thenReturn(true);
        assertAll(
                () -> assertEquals(COURSE_EDIT, courseController.updateCourse(courseDTO, bindingResult)),
                () -> assertEquals(courseDTO.getLastChanged(), CHANGED)
        );
        verify(bindingResult).addError(any());
        verify(courseService).existsCourse(ID, TITLE);
        verify(courseService, never()).saveCourse(any());
    }

    @Test
    public void testUpdateCourseInvalidTitleAndDescription() {
        String title = StringCreator.createLongString(200);
        courseDTO.setTitle(title);
        courseDTO.setDescription("");
        when(bindingResult.hasErrors()).thenReturn(true);
        assertAll(
                () -> assertEquals(COURSE_EDIT, courseController.updateCourse(courseDTO, bindingResult)),
                () -> assertEquals(courseDTO.getLastChanged(), CHANGED)
        );
        verify(bindingResult, times(2)).addError(any());
        verify(courseService).existsCourse(ID, title);
        verify(courseService, never()).saveCourse(any());
    }

    @Test
    public void testUpdateCourseContentTooLong() {
        courseDTO.setContent(StringCreator.createLongString(60000));
        when(bindingResult.hasErrors()).thenReturn(true);
        assertAll(
                () -> assertEquals(COURSE_EDIT, courseController.updateCourse(courseDTO, bindingResult)),
                () -> assertEquals(courseDTO.getLastChanged(), CHANGED)
        );
        verify(bindingResult).addError(any());
        verify(courseService).existsCourse(ID, TITLE);
        verify(courseService, never()).saveCourse(any());
    }

    @Test
    public void testDeleteCourse() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(userService.matchesPassword(PASSWORD, PASSWORD)).thenReturn(true);
        assertEquals(SUCCESS, courseController.deleteCourse(passwordDTO, ID));
        verify(authentication, times(2)).getName();
        verify(userService).getUser(USERNAME);
        verify(userService).matchesPassword(PASSWORD, PASSWORD);
        verify(courseService).deleteCourse(ID);
    }

    @Test
    public void testDeleteCourseInvalidPassword() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        assertEquals(REDIRECT_INVALID + ID, courseController.deleteCourse(passwordDTO, ID));
        verify(authentication, times(2)).getName();
        verify(userService).getUser(USERNAME);
        verify(userService).matchesPassword(PASSWORD, PASSWORD);
        verify(courseService, never()).deleteCourse(anyInt());
    }

    @Test
    public void testDeleteCoursePasswordTooLong() {
        passwordDTO.setPassword(StringCreator.createLongString(51));
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        assertEquals(REDIRECT_INVALID + ID, courseController.deleteCourse(passwordDTO, ID));
        verify(authentication, times(2)).getName();
        verify(userService).getUser(USERNAME);
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(courseService, never()).deleteCourse(anyInt());
    }

    @Test
    public void testDeleteCourseNotFound() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, courseController.deleteCourse(passwordDTO, ID));
        verify(authentication, times(2)).getName();
        verify(userService).getUser(USERNAME);
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(courseService, never()).deleteCourse(anyInt());
    }

    @Test
    public void testDeleteCourseAuthenticationNameNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        assertEquals(Constants.ERROR, courseController.deleteCourse(passwordDTO, ID));
        verify(authentication).getName();
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(courseService, never()).deleteCourse(anyInt());
    }

    @Test
    public void testDeleteCourseInvalidId() {
        assertInvalidIdException(() -> courseController.deleteCourse(passwordDTO, -1));
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(courseService, never()).deleteCourse(anyInt());
    }

    @Test
    public void testDeleteCoursePasswordNull() {
        passwordDTO.setPassword(null);
        assertEquals(Constants.ERROR, courseController.deleteCourse(passwordDTO, ID));
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(courseService, never()).deleteCourse(anyInt());
    }

    @Test
    public void testChangeCourseStatus() {
        when(courseService.changeCourseStatus(true, ID)).thenReturn(courseDTO);
        assertEquals(COURSE, courseController.changeCourseStatus("open", ID, model));
        verify(courseService).changeCourseStatus(true, ID);
        verify(pageService).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(pageService).getLastCourseExperimentPage(ID);
        verify(pageService).getParticipantCoursePage(anyInt(), any(PageRequest.class));
        verify(pageService).getLastParticipantCoursePage(ID);
        verify(model, times(8)).addAttribute(anyString(), any());
    }

    @Test
    public void testChangeCourseStatusClose() {
        when(courseService.changeCourseStatus(false, ID)).thenReturn(courseDTO);
        assertEquals(COURSE, courseController.changeCourseStatus("close", ID, model));
        verify(courseService).changeCourseStatus(false, ID);
        verify(pageService).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(pageService).getLastCourseExperimentPage(ID);
        verify(pageService).getParticipantCoursePage(anyInt(), any(PageRequest.class));
        verify(pageService).getLastParticipantCoursePage(ID);
        verify(model, times(8)).addAttribute(anyString(), any());
    }

    @Test
    public void testChangeCourseStatusInvalidStatus() {
        assertEquals(Constants.ERROR, courseController.changeCourseStatus("bla", ID, model));
        verify(courseService, never()).changeCourseStatus(anyBoolean(), anyInt());
        verify(pageService, never()).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(pageService, never()).getLastCourseExperimentPage(anyInt());
        verify(pageService, never()).getParticipantCoursePage(anyInt(), any(PageRequest.class));
        verify(pageService, never()).getLastParticipantCoursePage(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testChangeCourseStatusNotFound() {
        when(courseService.changeCourseStatus(true, ID)).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, courseController.changeCourseStatus("open", ID, model));
        verify(courseService).changeCourseStatus(true, ID);
        verify(pageService, never()).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(pageService, never()).getLastCourseExperimentPage(anyInt());
        verify(pageService, never()).getParticipantCoursePage(anyInt(), any(PageRequest.class));
        verify(pageService, never()).getLastParticipantCoursePage(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testChangeCourseStatusInvalidId() {
        assertInvalidIdException(() -> courseController.changeCourseStatus("open", -1, model));
        verify(courseService, never()).changeCourseStatus(anyBoolean(), anyInt());
        verify(pageService, never()).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(pageService, never()).getLastCourseExperimentPage(anyInt());
        verify(pageService, never()).getParticipantCoursePage(anyInt(), any(PageRequest.class));
        verify(pageService, never()).getLastParticipantCoursePage(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testAddParticipants() {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(userService.getUserByUsernameOrEmail(USERNAMES.get(0))).thenReturn(userDTO);
        when(userService.getUserByUsernameOrEmail(USERNAMES.get(1))).thenReturn(userDTO2);
        assertEquals(REDIRECT_COURSE + ID, courseController.addParticipants(USERNAMES, null, ID, model));
        verify(courseService).saveCourseParticipants(ID, userDTOs, false);
        verify(courseService, never()).addParticipantToCourseExperiments(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testAddParticipantsAddToExperiments() {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(userService.getUserByUsernameOrEmail(USERNAMES.get(0))).thenReturn(userDTO);
        when(userService.getUserByUsernameOrEmail(USERNAMES.get(1))).thenReturn(userDTO2);
        assertEquals(REDIRECT_COURSE + ID, courseController.addParticipants(USERNAMES, "on", ID, model));
        // Verify that the users actually get added to the experiment.
        verify(courseService).saveCourseParticipants(ID, userDTOs, true);
    }

    @Test
    public void testAddParticipantsNotFound() {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(userService.getUserByUsernameOrEmail(USERNAMES.get(0))).thenReturn(userDTO);
        when(userService.getUserByUsernameOrEmail(USERNAMES.get(1))).thenReturn(userDTO2);
        doThrow(NotFoundException.class).when(courseService).saveCourseParticipants(ID, userDTOs, true);
        assertEquals(Constants.ERROR, courseController.addParticipants(USERNAMES, "on", ID, model));
        verify(courseService).getCourse(ID);
        verify(userService).getUserByUsernameOrEmail(USERNAME);
        verify(courseService).existsCourseParticipant(ID, USERNAME);
        verify(courseService, never()).addParticipantToCourseExperiments(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testAddParticipantsInactiveExperiment() {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(userService.getUserByUsernameOrEmail(USERNAMES.get(0))).thenReturn(userDTO);
        when(userService.getUserByUsernameOrEmail(USERNAMES.get(1))).thenReturn(userDTO2);
        when(courseService.existsInactiveExperiment(ID)).thenReturn(true);
        assertEquals(COURSE, courseController.addParticipants(USERNAMES, "on", ID, model));
        verify(courseService).getCourse(ID);
        verify(userService).getUserByUsernameOrEmail(USERNAME);
        verify(courseService).existsCourseParticipant(ID, USERNAME);
        verify(courseService).existsInactiveExperiment(ID);
        verify(courseService, never()).saveCourseParticipant(anyInt(), anyString());
        verify(courseService, never()).addParticipantToCourseExperiments(anyInt(), anyInt());
        verify(model, times(9)).addAttribute(anyString(), any());
    }

    @Test
    public void testAddParticipantsExists() {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(userService.getUserByUsernameOrEmail(USERNAMES.get(0))).thenReturn(userDTO);
        when(courseService.existsCourseParticipant(ID, USERNAMES.get(0))).thenReturn(true);
        // Mocking the second user throws an UnnecessaryStubbingException for this test.
        when(model.getAttribute(ERROR)).thenReturn(USERNAME);
        assertEquals(COURSE, courseController.addParticipants(USERNAMES, "on", ID, model));
        verify(courseService).getCourse(ID);
        verify(userService).getUserByUsernameOrEmail(USERNAME);
        verify(courseService).existsCourseParticipant(ID, USERNAME);
        verify(courseService, never()).saveCourseParticipant(anyInt(), anyString());
        verify(courseService, never()).addParticipantToCourseExperiments(anyInt(), anyInt());
        verify(model, times(9)).addAttribute(anyString(), any());
    }

    @Test
    public void testAddParticipantsAdmin() {
        userDTO.setRole(Role.ADMIN);
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(userService.getUserByUsernameOrEmail(anyString())).thenReturn(userDTO);
        when(userService.getUserByUsernameOrEmail(USERNAMES.get(0))).thenReturn(userDTO);
        // Mocking the second user throws an UnnecessaryStubbingException for this test.
        when(model.getAttribute(ERROR)).thenReturn(USERNAME);
        assertEquals(COURSE, courseController.addParticipants(USERNAMES, "on", ID, model));
        verify(courseService).getCourse(ID);
        verify(userService).getUserByUsernameOrEmail(USERNAME);
        verify(courseService, never()).existsCourseParticipant(anyInt(), anyString());
        verify(courseService, never()).saveCourseParticipant(anyInt(), anyString());
        verify(courseService, never()).addParticipantToCourseExperiments(anyInt(), anyInt());
        verify(model, times(9)).addAttribute(anyString(), any());
    }

    @Test
    public void testAddParticipantsNoUser() {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(model.getAttribute(ERROR)).thenReturn(USERNAME);
        assertEquals(COURSE, courseController.addParticipants(USERNAMES, "on", ID, model));
        verify(courseService).getCourse(ID);
        verify(userService).getUserByUsernameOrEmail(USERNAME);
        verify(courseService, never()).existsCourseParticipant(anyInt(), anyString());
        verify(courseService, never()).saveCourseParticipant(anyInt(), anyString());
        verify(courseService, never()).addParticipantToCourseExperiments(anyInt(), anyInt());
        verify(model, times(9)).addAttribute(anyString(), any());
    }

    @Test
    public void testAddParticipantsCourseInactive() {
        courseDTO.setActive(false);
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        assertEquals(Constants.ERROR, courseController.addParticipants(USERNAMES, "on", ID, model));
        verify(courseService).getCourse(ID);
        verify(userService, never()).getUserByUsernameOrEmail(anyString());
        verify(courseService, never()).existsCourseParticipant(anyInt(), anyString());
        verify(courseService, never()).saveCourseParticipant(anyInt(), anyString());
        verify(courseService, never()).addParticipantToCourseExperiments(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteParticipants() {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(userService.getUserByUsernameOrEmail(USERNAMES.get(0))).thenReturn(userDTO);
        when(courseService.existsCourseParticipant(ID, USERNAMES.get(0))).thenReturn(true);
        when(userService.getUserByUsernameOrEmail(USERNAMES.get(1))).thenReturn(userDTO2);
        when(courseService.existsCourseParticipant(ID, USERNAMES.get(1))).thenReturn(true);
        assertEquals(REDIRECT_COURSE + ID, courseController.deleteParticipants(USERNAMES, ID, model));
        verify(courseService).deleteCourseParticipants(ID, USERNAMES);
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteParticipantsNotFound() {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(userService.getUserByUsernameOrEmail(anyString())).thenReturn(userDTO);
        when(courseService.existsCourseParticipant(anyInt(), anyString())).thenReturn(true);
        doThrow(NotFoundException.class).when(courseService).deleteCourseParticipants(ID, USERNAMES);
        assertEquals(Constants.ERROR, courseController.deleteParticipants(USERNAMES, ID, model));
    }

    @Test
    public void testDeleteParticipantsNotExistent() {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(userService.getUserByUsernameOrEmail(USERNAME)).thenReturn(userDTO);
        when(model.getAttribute(ERROR)).thenReturn(USERNAME);
        assertEquals(COURSE, courseController.deleteParticipants(USERNAMES, ID, model));
        verify(courseService).getCourse(ID);
        verify(userService).getUserByUsernameOrEmail(USERNAME);
        verify(courseService).existsCourseParticipant(ID, USERNAME);
        verify(courseService, never()).deleteCourseParticipant(anyInt(), anyString());
        verify(model, times(9)).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteParticipantsInvalidId() {
        assertInvalidIdException(() -> courseController.deleteParticipants(USERNAMES, 0, model));
        verify(courseService, never()).getCourse(anyInt());
        verify(userService, never()).getUserByUsernameOrEmail(anyString());
        verify(courseService, never()).existsCourseParticipant(anyInt(), anyString());
        verify(courseService, never()).deleteCourseParticipant(anyInt(), anyString());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteExperiment() {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(experimentService.existsExperiment(TITLE)).thenReturn(true);
        when(courseService.existsCourseExperiment(ID, TITLE)).thenReturn(true);
        assertEquals(REDIRECT_COURSE + ID, courseController.deleteExperiment(TITLE, ID, model));
        verify(courseService).getCourse(ID);
        verify(experimentService).existsExperiment(TITLE);
        verify(courseService).existsCourseExperiment(ID, TITLE);
        verify(courseService).deleteCourseExperiment(ID, TITLE);
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteExperimentNoEntry() {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(experimentService.existsExperiment(TITLE)).thenReturn(true);
        when(model.getAttribute(ERROR)).thenReturn(TITLE);
        assertEquals(COURSE, courseController.deleteExperiment(TITLE, ID, model));
        verify(courseService).getCourse(ID);
        verify(experimentService).existsExperiment(TITLE);
        verify(courseService).existsCourseExperiment(ID, TITLE);
        verify(courseService, never()).deleteCourseExperiment(anyInt(), anyString());
        verify(model, times(9)).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteExperimentNotExistent() {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(model.getAttribute(ERROR)).thenReturn(TITLE);
        assertEquals(COURSE, courseController.deleteExperiment(TITLE, ID, model));
        verify(courseService).getCourse(ID);
        verify(experimentService).existsExperiment(TITLE);
        verify(courseService, never()).existsCourseExperiment(anyInt(), anyString());
        verify(courseService, never()).deleteCourseExperiment(anyInt(), anyString());
        verify(model, times(9)).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteExperimentCourseInactive() {
        courseDTO.setActive(false);
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        assertEquals(Constants.ERROR, courseController.deleteExperiment(TITLE, ID, model));
        verify(courseService).getCourse(ID);
        verify(experimentService, never()).existsExperiment(anyString());
        verify(courseService, never()).existsCourseExperiment(anyInt(), anyString());
        verify(courseService, never()).deleteCourseExperiment(anyInt(), anyString());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetParticipantPage() {
        when(pageService.getLastParticipantCoursePage(ID)).thenReturn(LAST_PAGE);
        when(pageService.getParticipantCoursePage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        ModelAndView mv = courseController.getParticipantPage(ID, CURRENT);
        assertAll(
                () -> assertEquals(PARTICIPANT_TABLE, mv.getViewName()),
                () -> assertEquals(courseDTO, mv.getModel().get(COURSE_DTO)),
                () -> assertEquals(LAST_PAGE - 1, mv.getModel().get("lastParticipantPage")),
                () -> assertEquals(3, mv.getModel().get("participantPage"))
        );
        verify(pageService).getLastParticipantCoursePage(ID);
        verify(pageService).getParticipantCoursePage(anyInt(), any(PageRequest.class));
        verify(courseService).getCourse(ID);
    }

    @Test
    public void testGetParticipantPageInvalidPageNumber() {
        when(pageService.getLastParticipantCoursePage(ID)).thenReturn(LAST_PAGE);
        assertThrows(InvalidIdException.class, () -> courseController.getParticipantPage(ID, LAST));
        verify(pageService).getLastParticipantCoursePage(ID);
        verify(pageService, never()).getParticipantCoursePage(anyInt(), any(PageRequest.class));
        verify(courseService, never()).getCourse(anyInt());
    }

    @Test
    public void testGetParticipantPageInvalidId() {
        assertInvalidIdException(() -> courseController.getParticipantPage(-1, LAST).getViewName());
        verify(pageService, never()).getLastParticipantCoursePage(anyInt());
        verify(pageService, never()).getParticipantCoursePage(anyInt(), any(PageRequest.class));
        verify(courseService, never()).getCourse(anyInt());
    }

    @Test
    public void testGetExperimentPage() {
        courseDTO.setContent(null);
        when(pageService.getLastCourseExperimentPage(ID)).thenReturn(LAST_PAGE);
        when(pageService.getCourseExperimentPage(any(PageRequest.class), anyInt())).thenReturn(experiments);
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        ModelAndView mv = courseController.getExperimentPage(ID, CURRENT);
        assertAll(
                () -> assertEquals(EXPERIMENT_TABLE, mv.getViewName()),
                () -> assertEquals(courseDTO, mv.getModel().get(COURSE_DTO)),
                () -> assertEquals(LAST_PAGE - 1, mv.getModel().get("lastExperimentPage")),
                () -> assertEquals(3, mv.getModel().get("experimentPage"))
        );
        verify(pageService).getLastCourseExperimentPage(ID);
        verify(pageService).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(courseService).getCourse(ID);
    }

    @Test
    public void testGetExperimentPageInvalidPage() {
        when(pageService.getLastCourseExperimentPage(ID)).thenReturn(LAST_PAGE);
        assertThrows(InvalidIdException.class, () -> courseController.getExperimentPage(ID, -1));
        verify(pageService).getLastCourseExperimentPage(ID);
        verify(pageService, never()).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(courseService, never()).getCourse(anyInt());
    }

    @Test
    public void testGetExperimentPageInvalidId() {
        assertInvalidIdException(() -> courseController.getExperimentPage(0, CURRENT).getViewName());
        verify(pageService, never()).getLastCourseExperimentPage(anyInt());
        verify(pageService, never()).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(courseService, never()).getCourse(anyInt());
    }

    private List<CourseExperimentProjection> getCourseExperiments(int number) {
        List<CourseExperimentProjection> experiments = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            int id = i + 1;
            CourseExperimentProjection projection = new CourseExperimentProjection() {
                @Override
                public ExperimentTableProjection getExperiment() {
                    return new ExperimentTableProjection() {
                        @Override
                        public Integer getId() {
                            return id;
                        }

                        @Override
                        public String getTitle() {
                            return "Experiment " + id;
                        }

                        @Override
                        public String getDescription() {
                            return "Some description";
                        }

                        @Override
                        public boolean isActive() {
                            return false;
                        }
                    };
                }
            };
            experiments.add(projection);
        }
        return experiments;
    }

}
