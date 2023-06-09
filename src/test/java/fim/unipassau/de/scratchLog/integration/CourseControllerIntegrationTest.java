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
package fim.unipassau.de.scratchLog.integration;

import fim.unipassau.de.scratchLog.application.exception.NotFoundException;
import fim.unipassau.de.scratchLog.application.service.CourseService;
import fim.unipassau.de.scratchLog.application.service.ExperimentService;
import fim.unipassau.de.scratchLog.application.service.PageService;
import fim.unipassau.de.scratchLog.application.service.UserService;
import fim.unipassau.de.scratchLog.persistence.entity.CourseParticipant;
import fim.unipassau.de.scratchLog.persistence.projection.CourseExperimentProjection;
import fim.unipassau.de.scratchLog.persistence.projection.ExperimentTableProjection;
import fim.unipassau.de.scratchLog.spring.configuration.SecurityTestConfig;
import fim.unipassau.de.scratchLog.util.Constants;
import fim.unipassau.de.scratchLog.util.enums.Language;
import fim.unipassau.de.scratchLog.util.enums.Role;
import fim.unipassau.de.scratchLog.web.controller.CourseController;
import fim.unipassau.de.scratchLog.web.dto.CourseDTO;
import fim.unipassau.de.scratchLog.web.dto.PasswordDTO;
import fim.unipassau.de.scratchLog.web.dto.UserDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(SpringExtension.class)
@WebMvcTest(CourseController.class)
@Import(SecurityTestConfig.class)
@ActiveProfiles("test")
public class CourseControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private CourseService courseService;

    @MockBean
    private ExperimentService experimentService;

    @MockBean
    private UserService userService;

    @MockBean
    private PageService pageService;

    private static final String COURSE = "course";
    private static final String REDIRECT_COURSE = "redirect:/course?id=";
    private static final String COURSE_EDIT = "course-edit";
    private static final String SUCCESS = "redirect:/?success=true";
    private static final String REDIRECT_INVALID = "redirect:/course?invalid=true&id=";
    private static final String EXPERIMENT_TABLE = "course::course_experiment_table";
    private static final String PARTICIPANT_TABLE = "course::course_participant_table";
    private static final String COURSE_DTO = "courseDTO";
    private static final String PASSWORD_DTO = "passwordDTO";
    private static final String ID_STRING = "1";
    private static final String CURRENT = "3";
    private static final String ID_PARAM = "id";
    private static final String TITLE_PARAM = "title";
    private static final String PARTICIPANT_PARAM = "participant";
    private static final String PAGE_PARAM = "page";
    private static final String STATUS_PARAM = "stat";
    private static final String ERROR = "error";
    private static final int ID = 1;
    private static final int LAST_PAGE = 5;
    private static final String TITLE = "Title";
    private static final String DESCRIPTION = "Description";
    private static final String CONTENT = "content";
    private static final String USERNAME = "participant";
    private static final String PASSWORD = "password";
    private static final LocalDateTime CHANGED = LocalDateTime.now();
    private final PasswordDTO passwordDTO = new PasswordDTO(PASSWORD);
    private final CourseDTO courseDTO = new CourseDTO(ID, TITLE, DESCRIPTION, CONTENT, true, CHANGED);
    private final UserDTO userDTO = new UserDTO(USERNAME, "part@part.de", Role.PARTICIPANT, Language.ENGLISH,
            PASSWORD, "secret");
    private final Page<CourseExperimentProjection> experiments = new PageImpl<>(getCourseExperiments(2));
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
    }

    @AfterEach
    public void resetService() {
        reset(courseService);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testGetCourse() throws Exception {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(pageService.getCourseExperimentPage(any(PageRequest.class), anyInt())).thenReturn(experiments);
        when(pageService.getLastCourseExperimentPage(ID)).thenReturn(LAST_PAGE);
        when(pageService.getParticipantCoursePage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        when(pageService.getLastParticipantCoursePage(ID)).thenReturn(LAST_PAGE);
        mvc.perform(get("/course")
                        .param(ID_PARAM, ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(COURSE))
                .andExpect(model().attribute(COURSE_DTO, is(courseDTO)))
                .andExpect(model().attribute("experiments", is(experiments)))
                .andExpect(model().attribute("experimentPage", is(0)))
                .andExpect(model().attribute("lastExperimentPage", is(LAST_PAGE - 1)))
                .andExpect(model().attribute("participants", is(participants)))
                .andExpect(model().attribute("participantPage", is(0)))
                .andExpect(model().attribute("lastParticipantPage", is(LAST_PAGE - 1)));
        verify(courseService).getCourse(ID);
        verify(pageService).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(pageService).getLastCourseExperimentPage(ID);
        verify(pageService).getParticipantCoursePage(anyInt(), any(PageRequest.class));
        verify(pageService).getLastParticipantCoursePage(ID);
    }

    @Test
    @WithMockUser(username = "user", roles = "PARTICIPANT")
    public void testGetCourseParticipant() throws Exception {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(pageService.getCourseExperimentPage(any(PageRequest.class), anyInt())).thenReturn(experiments);
        when(pageService.getLastCourseExperimentPage(ID)).thenReturn(LAST_PAGE);
        mvc.perform(get("/course")
                        .param(ID_PARAM, ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(COURSE))
                .andExpect(model().attribute(COURSE_DTO, is(courseDTO)))
                .andExpect(model().attribute("experiments", is(experiments)))
                .andExpect(model().attribute("experimentPage", is(0)))
                .andExpect(model().attribute("lastExperimentPage", is(LAST_PAGE - 1)))
                .andExpect(model().attribute("participants", is(empty())))
                .andExpect(model().attribute("participantPage", is(0)))
                .andExpect(model().attribute("lastParticipantPage", is(0)));
        verify(courseService).getCourse(ID);
        verify(pageService).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(pageService).getLastCourseExperimentPage(ID);
        verify(pageService, never()).getParticipantCoursePage(anyInt(), any(PageRequest.class));
        verify(pageService, never()).getLastParticipantCoursePage(anyInt());
    }

    @Test
    public void testGetCourseInvalidId() throws Exception {
        mvc.perform(get("/course")
                        .param(ID_PARAM, "0")
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(courseService, never()).getCourse(anyInt());
        verify(pageService, never()).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(pageService, never()).getLastCourseExperimentPage(anyInt());
        verify(pageService, never()).getParticipantCoursePage(anyInt(), any(PageRequest.class));
        verify(pageService, never()).getLastParticipantCoursePage(anyInt());
    }

    @Test
    public void testGetCourseForm() throws Exception {
        mvc.perform(get("/course/create")
                        .flashAttr(COURSE_DTO, courseDTO)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(COURSE_EDIT));
    }

    @Test
    public void testGetEditCourseForm() throws Exception {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        mvc.perform(get("/course/edit")
                        .param(ID_PARAM, ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(COURSE_EDIT))
                .andExpect(model().attribute(COURSE_DTO, is(courseDTO)));
        verify(courseService).getCourse(ID);
    }

    @Test
    public void testGetEditCourseFormNotFound() throws Exception {
        when(courseService.getCourse(ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/course/edit")
                        .param(ID_PARAM, ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(courseService).getCourse(ID);
    }

    @Test
    public void testGetEditCourseFormInvalidId() throws Exception {
        mvc.perform(get("/course/edit")
                        .param(ID_PARAM, TITLE)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(courseService, never()).getCourse(anyInt());
    }

    @Test
    public void testUpdateCourse() throws Exception {
        when(courseService.saveCourse(courseDTO)).thenReturn(ID);
        mvc.perform(post("/course/update")
                        .flashAttr(COURSE_DTO, courseDTO)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_COURSE + ID));
        verify(courseService).existsCourse(ID, TITLE);
        verify(courseService).saveCourse(courseDTO);
    }

    @Test
    public void testUpdateCourseTitleExists() throws Exception {
        when(courseService.existsCourse(ID, TITLE)).thenReturn(true);
        mvc.perform(post("/course/update")
                        .flashAttr(COURSE_DTO, courseDTO)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(COURSE_EDIT));
        verify(courseService).existsCourse(ID, TITLE);
        verify(courseService, never()).saveCourse(any());
    }

    @Test
    public void testUpdateNewCourseTitleExists() throws Exception {
        courseDTO.setId(null);
        when(courseService.existsCourse(TITLE)).thenReturn(true);
        mvc.perform(post("/course/update")
                        .flashAttr(COURSE_DTO, courseDTO)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(COURSE_EDIT));
        verify(courseService).existsCourse(TITLE);
        verify(courseService, never()).saveCourse(any());
    }

    @Test
    public void testUpdateCourseInvalidTitleAndDescription() throws Exception {
        courseDTO.setTitle("");
        courseDTO.setDescription(null);
        mvc.perform(post("/course/update")
                        .flashAttr(COURSE_DTO, courseDTO)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(COURSE_EDIT));
        verify(courseService).existsCourse(ID, "");
        verify(courseService, never()).saveCourse(any());
    }

    @Test
    @WithMockUser(username = USERNAME, roles = "ADMIN")
    public void testDeleteCourse() throws Exception {
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(userService.matchesPassword(PASSWORD, PASSWORD)).thenReturn(true);
        mvc.perform(post("/course/delete")
                        .flashAttr(PASSWORD_DTO, passwordDTO)
                        .param(ID_PARAM, ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(SUCCESS));
        verify(userService).getUser(USERNAME);
        verify(userService).matchesPassword(PASSWORD, PASSWORD);
        verify(courseService).deleteCourse(ID);
    }

    @Test
    @WithMockUser(username = USERNAME, roles = "ADMIN")
    public void testDeleteCourseInvalidPassword() throws Exception {
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        mvc.perform(post("/course/delete")
                        .flashAttr(PASSWORD_DTO, passwordDTO)
                        .param(ID_PARAM, ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_INVALID + ID));
        verify(userService).getUser(USERNAME);
        verify(userService).matchesPassword(PASSWORD, PASSWORD);
        verify(courseService, never()).deleteCourse(anyInt());
    }

    @Test
    @WithMockUser(username = USERNAME, roles = "ADMIN")
    public void testDeleteCourseNotFound() throws Exception {
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(userService.matchesPassword(PASSWORD, PASSWORD)).thenReturn(true);
        doThrow(NotFoundException.class).when(courseService).deleteCourse(ID);
        mvc.perform(post("/course/delete")
                        .flashAttr(PASSWORD_DTO, passwordDTO)
                        .param(ID_PARAM, ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(userService).getUser(USERNAME);
        verify(userService).matchesPassword(PASSWORD, PASSWORD);
        verify(courseService).deleteCourse(ID);
    }

    @Test
    public void testDeleteCourseInvalidId() throws Exception {
        mvc.perform(post("/course/delete")
                        .flashAttr(PASSWORD_DTO, passwordDTO)
                        .param(ID_PARAM, "0")
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(courseService, never()).deleteCourse(anyInt());
    }

    @Test
    public void testChangeCourseStatus() throws Exception {
        when(courseService.changeCourseStatus(true, ID)).thenReturn(courseDTO);
        mvc.perform(get("/course/status")
                        .param(ID_PARAM, ID_STRING)
                        .param(STATUS_PARAM, "open")
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(COURSE))
                .andExpect(model().attribute(COURSE_DTO, is(courseDTO)))
                .andExpect(model().attribute("experimentPage", is(0)))
                .andExpect(model().attribute("participantPage", is(0)));
        verify(courseService).changeCourseStatus(true, ID);
        verify(pageService).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(pageService).getLastCourseExperimentPage(ID);
        verify(pageService).getParticipantCoursePage(anyInt(), any(PageRequest.class));
        verify(pageService).getLastParticipantCoursePage(ID);
    }

    @Test
    public void testChangeCourseStatusClose() throws Exception {
        when(courseService.changeCourseStatus(false, ID)).thenReturn(courseDTO);
        mvc.perform(get("/course/status")
                        .param(ID_PARAM, ID_STRING)
                        .param(STATUS_PARAM, "close")
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(COURSE))
                .andExpect(model().attribute(COURSE_DTO, is(courseDTO)))
                .andExpect(model().attribute("experimentPage", is(0)))
                .andExpect(model().attribute("participantPage", is(0)));
        verify(courseService).changeCourseStatus(false, ID);
        verify(pageService).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(pageService).getLastCourseExperimentPage(ID);
        verify(pageService).getParticipantCoursePage(anyInt(), any(PageRequest.class));
        verify(pageService).getLastParticipantCoursePage(ID);
    }

    @Test
    public void testChangeCourseStatusNotFound() throws Exception {
        when(courseService.changeCourseStatus(false, ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/course/status")
                        .param(ID_PARAM, ID_STRING)
                        .param(STATUS_PARAM, "close")
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(courseService).changeCourseStatus(false, ID);
        verify(pageService, never()).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(pageService, never()).getLastCourseExperimentPage(anyInt());
        verify(pageService, never()).getParticipantCoursePage(anyInt(), any(PageRequest.class));
        verify(pageService, never()).getLastParticipantCoursePage(anyInt());
    }

    @Test
    public void testAddParticipant() throws Exception {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(userService.getUserByUsernameOrEmail(USERNAME)).thenReturn(userDTO);
        when(courseService.saveCourseParticipant(ID, USERNAME)).thenReturn(ID);
        mvc.perform(get("/course/participant/add")
                        .param(ID_PARAM, ID_STRING)
                        .param(PARTICIPANT_PARAM, USERNAME)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_COURSE + ID));
        verify(courseService).getCourse(ID);
        verify(userService).getUserByUsernameOrEmail(USERNAME);
        verify(courseService).existsCourseParticipant(ID, USERNAME);
        verify(courseService).saveCourseParticipant(ID, USERNAME);
        verify(courseService, never()).addParticipantToCourseExperiments(anyInt(), anyInt());
    }

    @Test
    public void testAddParticipantInvalidInput() throws Exception {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        mvc.perform(get("/course/participant/add")
                        .param(ID_PARAM, ID_STRING)
                        .param(PARTICIPANT_PARAM, " ")
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(COURSE))
                .andExpect(model().attribute(ERROR, notNullValue()));
        verify(courseService).getCourse(ID);
        verify(userService, never()).getUserByUsernameOrEmail(anyString());
        verify(courseService, never()).existsCourseParticipant(anyInt(), anyString());
        verify(courseService, never()).saveCourseParticipant(anyInt(), anyString());
        verify(courseService, never()).addParticipantToCourseExperiments(anyInt(), anyInt());
    }

    @Test
    public void testAddParticipantCourseNotFound() throws Exception {
        when(courseService.getCourse(ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/course/participant/add")
                        .param(ID_PARAM, ID_STRING)
                        .param(PARTICIPANT_PARAM, USERNAME)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(courseService).getCourse(ID);
        verify(userService, never()).getUserByUsernameOrEmail(anyString());
        verify(courseService, never()).existsCourseParticipant(anyInt(), anyString());
        verify(courseService, never()).saveCourseParticipant(anyInt(), anyString());
        verify(courseService, never()).addParticipantToCourseExperiments(anyInt(), anyInt());
    }

    @Test
    public void testDeleteParticipant() throws Exception {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(userService.getUserByUsernameOrEmail(USERNAME)).thenReturn(userDTO);
        when(courseService.existsCourseParticipant(ID, USERNAME)).thenReturn(true);
        mvc.perform(get("/course/participant/delete")
                        .param(ID_PARAM, ID_STRING)
                        .param(PARTICIPANT_PARAM, USERNAME)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_COURSE + ID));
        verify(courseService).getCourse(ID);
        verify(userService).getUserByUsernameOrEmail(USERNAME);
        verify(courseService).existsCourseParticipant(ID, USERNAME);
        verify(courseService).deleteCourseParticipant(ID, USERNAME);
    }

    @Test
    public void testDeleteParticipantNotFound() throws Exception {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        mvc.perform(get("/course/participant/delete")
                        .param(ID_PARAM, ID_STRING)
                        .param(PARTICIPANT_PARAM, USERNAME)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(COURSE))
                .andExpect(model().attribute(ERROR, notNullValue()));
        verify(courseService).getCourse(ID);
        verify(userService).getUserByUsernameOrEmail(USERNAME);
        verify(courseService, never()).existsCourseParticipant(anyInt(), anyString());
        verify(courseService, never()).deleteCourseParticipant(anyInt(), anyString());
    }

    @Test
    public void testDeleteParticipantCourseInactive() throws Exception {
        courseDTO.setActive(false);
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        mvc.perform(get("/course/participant/delete")
                        .param(ID_PARAM, ID_STRING)
                        .param(PARTICIPANT_PARAM, USERNAME)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(courseService).getCourse(ID);
        verify(userService, never()).getUserByUsernameOrEmail(anyString());
        verify(courseService, never()).existsCourseParticipant(anyInt(), anyString());
        verify(courseService, never()).deleteCourseParticipant(anyInt(), anyString());
    }

    @Test
    public void testDeleteExperiment() throws Exception {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        when(experimentService.existsExperiment(TITLE)).thenReturn(true);
        when(courseService.existsCourseExperiment(ID, TITLE)).thenReturn(true);
        mvc.perform(get("/course/experiment/delete")
                        .param(ID_PARAM, ID_STRING)
                        .param(TITLE_PARAM, TITLE)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_COURSE + ID));
        verify(courseService).getCourse(ID);
        verify(experimentService).existsExperiment(TITLE);
        verify(courseService).existsCourseExperiment(ID, TITLE);
        verify(courseService).deleteCourseExperiment(ID, TITLE);
    }

    @Test
    public void testDeleteExperimentInvalidInput() throws Exception {
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        mvc.perform(get("/course/experiment/delete")
                        .param(ID_PARAM, ID_STRING)
                        .param(TITLE_PARAM, "  ")
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(COURSE))
                .andExpect(model().attribute(ERROR, notNullValue()));
        verify(courseService).getCourse(ID);
        verify(experimentService, never()).existsExperiment(anyString());
        verify(courseService, never()).existsCourseExperiment(anyInt(), anyString());
        verify(courseService, never()).deleteCourseExperiment(anyInt(), anyString());
    }

    @Test
    public void testDeleteExperimentNotFound() throws Exception {
        when(courseService.getCourse(ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/course/experiment/delete")
                        .param(ID_PARAM, ID_STRING)
                        .param(TITLE_PARAM, TITLE)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(courseService).getCourse(ID);
        verify(experimentService, never()).existsExperiment(anyString());
        verify(courseService, never()).existsCourseExperiment(anyInt(), anyString());
        verify(courseService, never()).deleteCourseExperiment(anyInt(), anyString());
    }

    @Test
    public void testGetParticipantPage() throws Exception {
        courseDTO.setContent(null);
        when(pageService.getLastParticipantCoursePage(ID)).thenReturn(LAST_PAGE);
        when(pageService.getParticipantCoursePage(anyInt(), any(PageRequest.class))).thenReturn(participants);
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        mvc.perform(get("/course/page/participant")
                        .param(ID_PARAM, ID_STRING)
                        .param(PAGE_PARAM, CURRENT)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(PARTICIPANT_TABLE))
                .andExpect(model().attribute(COURSE_DTO, is(courseDTO)))
                .andExpect(model().attribute("participants", is(participants)))
                .andExpect(model().attribute("participantPage", is(3)))
                .andExpect(model().attribute("lastParticipantPage", is(LAST_PAGE - 1)));
        verify(pageService).getLastParticipantCoursePage(ID);
        verify(pageService).getParticipantCoursePage(anyInt(), any(PageRequest.class));
        verify(courseService).getCourse(ID);
    }

    @Test
    public void testGetParticipantPageError() throws Exception {
        mvc.perform(get("/course/page/participant")
                        .param(ID_PARAM, "0")
                        .param(PAGE_PARAM, CURRENT)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is4xxClientError())
                .andExpect(view().name(ERROR));
        verify(pageService, never()).getLastParticipantCoursePage(anyInt());
        verify(pageService, never()).getParticipantCoursePage(anyInt(), any(PageRequest.class));
        verify(courseService, never()).getCourse(anyInt());
    }

    @Test
    public void testGetExperimentPage() throws Exception {
        when(pageService.getLastCourseExperimentPage(ID)).thenReturn(LAST_PAGE);
        when(pageService.getCourseExperimentPage(any(PageRequest.class), anyInt())).thenReturn(experiments);
        when(courseService.getCourse(ID)).thenReturn(courseDTO);
        mvc.perform(get("/course/page/experiment")
                        .param(ID_PARAM, ID_STRING)
                        .param(PAGE_PARAM, CURRENT)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT_TABLE))
                .andExpect(model().attribute(COURSE_DTO, is(courseDTO)))
                .andExpect(model().attribute("experiments", is(experiments)))
                .andExpect(model().attribute("experimentPage", is(3)))
                .andExpect(model().attribute("lastExperimentPage", is(LAST_PAGE - 1)));
        verify(pageService).getLastCourseExperimentPage(ID);
        verify(pageService).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(courseService).getCourse(ID);
    }

    @Test
    public void testGetNextExperimentInvalidPage() throws Exception {
        when(pageService.getLastCourseExperimentPage(ID)).thenReturn(LAST_PAGE);
        mvc.perform(get("/course/page/experiment")
                        .param(ID_PARAM, ID_STRING)
                        .param(PAGE_PARAM, "-1")
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is4xxClientError())
                .andExpect(view().name(ERROR));
        verify(pageService).getLastCourseExperimentPage(ID);
        verify(pageService, never()).getCourseExperimentPage(any(PageRequest.class), anyInt());
        verify(courseService, never()).getCourse(anyInt());
    }

    private List<CourseExperimentProjection> getCourseExperiments(int number) {
        List<CourseExperimentProjection> experiments = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            int id = i + 1;
            CourseExperimentProjection projection = () -> new ExperimentTableProjection() {
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
            experiments.add(projection);
        }
        return experiments;
    }

}
