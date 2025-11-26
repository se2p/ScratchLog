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

package de.uni_passau.fim.se2.scratchlog.integration;

import de.uni_passau.fim.se2.scratchlog.application.exception.NotFoundException;
import de.uni_passau.fim.se2.scratchlog.application.service.ExperimentService;
import de.uni_passau.fim.se2.scratchlog.application.service.PageService;
import de.uni_passau.fim.se2.scratchlog.application.service.ParticipantService;
import de.uni_passau.fim.se2.scratchlog.application.service.TokenService;
import de.uni_passau.fim.se2.scratchlog.application.service.UserService;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.CourseTableProjection;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.ExperimentTableProjection;
import de.uni_passau.fim.se2.scratchlog.spring.configuration.SecurityTestConfig;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import de.uni_passau.fim.se2.scratchlog.util.enums.Language;
import de.uni_passau.fim.se2.scratchlog.util.enums.Role;
import de.uni_passau.fim.se2.scratchlog.web.AbstractControllerTest;
import de.uni_passau.fim.se2.scratchlog.web.controller.HomeController;
import de.uni_passau.fim.se2.scratchlog.web.dto.ExperimentDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.UserDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(SpringExtension.class)
@WebMvcTest(HomeController.class)
@Import(SecurityTestConfig.class)
@ActiveProfiles("test")
public class HomeControllerIntegrationTest extends AbstractControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ExperimentService experimentService;

    @MockitoBean
    private PageService pageService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ParticipantService participantService;

    @MockitoBean
    private TokenService tokenService;

    private static final String INDEX = "index";
    private static final String INDEX_EXPERIMENT = "index::experiment_table";
    private static final String INDEX_COURSE = "index::course_table";
    private static final String FINISH = "experiment-finish";
    private static final String PASSWORD_RESET = "password-reset";
    private static final String CURRENT = "3";
    private static final String BLANK = "   ";
    private static final String PAGE_PARAM = "page";
    private static final String LAST_EXPERIMENT_PAGE = "lastExperimentPage";
    private static final String LAST_COURSE_PAGE = "lastCoursePage";
    private static final String EXPERIMENT_PAGE = "experimentPage";
    private static final String COURSE_PAGE = "coursePage";
    private static final String EXPERIMENTS = "experiments";
    private static final String COURSES = "courses";
    private static final String ID_STRING = "1";
    private static final String SECRET = "secret";
    private static final String THANKS = "thanks";
    private static final String EXPERIMENT = "experiment";
    private static final String USER = "user";
    private static final String GUI_URL = "scratch";
    private static final String PAGE_COURSE = "/page/course";
    private static final String PAGE_EXPERIMENT = "/page/experiment";
    private final int pageNum = 3;
    private final int lastPage = 4;
    private static final int ID = 1;
    private static final ExperimentDTO experimentDTO = new ExperimentDTO(ID, "My Experiment", "description",
            "info", "postscript", true, false, GUI_URL);
    private static final UserDTO userDTO = new UserDTO("participant", "email", Role.PARTICIPANT, Language.ENGLISH,
            "password", "");
    private final Page<ExperimentTableProjection> experimentPage = new PageImpl<>(getExperimentProjections(5));
    private final Page<CourseTableProjection> coursePage = new PageImpl<>(getCourseTableProjections(3));

    @BeforeEach
    public void setup() {
        userDTO.setId(ID);
    }

    @AfterEach
    public void resetService() {
        reset(experimentService);
    }

    @Test
    public void testGetIndexPage() throws Exception {
        mvc.perform(get("/")
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(INDEX));
    }

    @Test
    @WithMockUser(username = "participant", roles = {"ADMIN", "PARTICIPANT"})
    public void testGetIndexPageAdmin() throws Exception {
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(pageService.getExperimentPage(anyInt())).thenReturn(experimentPage);
        when(pageService.getCoursePage(anyInt())).thenReturn(coursePage);
        mvc.perform(get("/")
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(EXPERIMENTS, is(experimentPage)))
                .andExpect(model().attribute(LAST_EXPERIMENT_PAGE, is(0)))
                .andExpect(model().attribute(EXPERIMENT_PAGE, is(0)))
                .andExpect(model().attribute(COURSES, is(coursePage)))
                .andExpect(model().attribute(LAST_COURSE_PAGE, is(0)))
                .andExpect(model().attribute(COURSE_PAGE, is(0)))
                .andExpect(view().name(INDEX));
    }

    @Test
    @WithMockUser(username = "participant", roles = {"PARTICIPANT"})
    public void testGetIndexPageParticipant() throws Exception {
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(pageService.getExperimentParticipantPage(anyInt(), anyInt())).thenReturn(experimentPage);
        when(pageService.getCourseParticipantPage(anyInt(), anyInt())).thenReturn(coursePage);
        mvc.perform(get("/")
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(EXPERIMENTS, is(experimentPage)))
                .andExpect(model().attribute(LAST_EXPERIMENT_PAGE, is(0)))
                .andExpect(model().attribute(EXPERIMENT_PAGE, is(0)))
                .andExpect(model().attribute(COURSES, is(coursePage)))
                .andExpect(model().attribute(LAST_COURSE_PAGE, is(0)))
                .andExpect(model().attribute(COURSE_PAGE, is(0)))
                .andExpect(view().name(INDEX));
    }

    @Test
    @WithMockUser(username = "participant", roles = {"PARTICIPANT"})
    public void testGetIndexPageParticipantNotFound() throws Exception {
        when(userService.getUser(userDTO.getUsername())).thenThrow(NotFoundException.class);
        mvc.perform(get("/")
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void testGetCoursePage() throws Exception {
        when(userService.getUser("user")).thenReturn(userDTO);
        when(pageService.getLastCoursePage()).thenReturn(lastPage);
        when(pageService.getCoursePage(anyInt())).thenReturn(coursePage);
        mvc.perform(get(PAGE_COURSE)
                        .param(PAGE_PARAM, CURRENT)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(COURSES, is(coursePage)))
                .andExpect(model().attribute(LAST_COURSE_PAGE, is(lastPage - 1)))
                .andExpect(model().attribute(COURSE_PAGE, is(pageNum)))
                .andExpect(view().name(INDEX_COURSE));
    }

    @Test
    @WithMockUser(username = "participant", roles = {"PARTICIPANT"})
    public void testGetCoursePageParticipant() throws Exception {
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(pageService.getLastCoursePageForUser(userDTO.getId())).thenReturn(lastPage);
        when(pageService.getCourseParticipantPage(anyInt(), anyInt())).thenReturn(coursePage);
        mvc.perform(get(PAGE_COURSE)
                        .param(PAGE_PARAM, CURRENT)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(COURSES, is(coursePage)))
                .andExpect(model().attribute(LAST_COURSE_PAGE, is(lastPage - 1)))
                .andExpect(model().attribute(COURSE_PAGE, is(pageNum)))
                .andExpect(view().name(INDEX_COURSE));
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void testGetExperimentPage() throws Exception {
        when(userService.getUser("user")).thenReturn(userDTO);
        when(pageService.getLastExperimentPage()).thenReturn(lastPage);
        when(pageService.getExperimentPage(anyInt())).thenReturn(experimentPage);
        mvc.perform(get(PAGE_EXPERIMENT)
                        .param(PAGE_PARAM, CURRENT)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(EXPERIMENTS, is(experimentPage)))
                .andExpect(model().attribute(LAST_EXPERIMENT_PAGE, is(lastPage - 1)))
                .andExpect(model().attribute(EXPERIMENT_PAGE, is(pageNum)))
                .andExpect(view().name(INDEX_EXPERIMENT));
    }

    @Test
    @WithMockUser(username = "participant", roles = {"PARTICIPANT"})
    public void testGetExperimentPageParticipant() throws Exception {
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(pageService.getLastExperimentPageForUser(userDTO.getId())).thenReturn(lastPage);
        when(pageService.getExperimentParticipantPage(anyInt(), anyInt())).thenReturn(experimentPage);
        mvc.perform(get(PAGE_EXPERIMENT)
                        .param(PAGE_PARAM, CURRENT)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(EXPERIMENTS, is(experimentPage)))
                .andExpect(model().attribute(LAST_EXPERIMENT_PAGE, is(lastPage - 1)))
                .andExpect(model().attribute(EXPERIMENT_PAGE, is(pageNum)))
                .andExpect(view().name(INDEX_EXPERIMENT));
    }

    @Test
    @WithMockUser(username = "participant", roles = {"PARTICIPANT"})
    public void testGetExperimentPageParticipantNotFound() throws Exception {
        when(userService.getUser(userDTO.getUsername())).thenThrow(NotFoundException.class);
        mvc.perform(get(PAGE_EXPERIMENT)
                        .param(PAGE_PARAM, CURRENT)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void testGetExperimentPageParamInvalid() throws Exception {
        when(pageService.getLastExperimentPage()).thenReturn(lastPage);
        mvc.perform(get(PAGE_EXPERIMENT)
                        .param(PAGE_PARAM, PAGE_PARAM)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is4xxClientError())
                .andExpect(view().name(Constants.ERROR));
    }

    @Test
    public void testGetExperimentFinishPage() throws Exception {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        mvc.perform(get("/finish")
                        .param(EXPERIMENT, ID_STRING)
                        .param(USER, ID_STRING)
                        .param(SECRET, SECRET)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(THANKS, is(experimentDTO.getPostscript())))
                .andExpect(model().attribute(EXPERIMENT, is(ID)))
                .andExpect(model().attribute(USER, is(ID)))
                .andExpect(view().name(FINISH));
        verify(participantService).isInvalidParticipant(ID, ID, SECRET, false);
        verify(experimentService).getExperiment(ID);
    }

    @Test
    public void testGetExperimentFinishPageNotFound() throws Exception {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/finish")
                        .param(EXPERIMENT, ID_STRING)
                        .param(USER, ID_STRING)
                        .param(SECRET, SECRET)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(model().attribute(THANKS, nullValue()))
                .andExpect(view().name(Constants.ERROR));
        verify(participantService).isInvalidParticipant(ID, ID, SECRET, false);
        verify(experimentService).getExperiment(ID);
    }

    @Test
    public void testGetExperimentFinishPageUserIdBlank() throws Exception {
        mvc.perform(get("/finish")
                        .param(EXPERIMENT, ID_STRING)
                        .param(USER, BLANK)
                        .param(SECRET, SECRET)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is4xxClientError())
                .andExpect(model().attribute(THANKS, nullValue()))
                .andExpect(view().name(Constants.ERROR));
        verify(participantService, never()).isInvalidParticipant(anyInt(), anyInt(), anyString(), anyBoolean());
        verify(experimentService, never()).getExperiment(anyInt());
    }

    @Test
    public void testGetResetPage() throws Exception {
        setMailServer(true);
        mvc.perform(get("/reset")
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(PASSWORD_RESET));
    }

    @Test
    public void testGetResetPageNoMailServer() throws Exception {
        setMailServer(false);
        mvc.perform(get("/reset")
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
    }

    private List<ExperimentTableProjection> getExperimentProjections(int number) {
        List<ExperimentTableProjection> experiments = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            int finalI = i;
            ExperimentTableProjection projection = new ExperimentTableProjection() {
                @Override
                public Integer getId() {
                    return finalI;
                }

                @Override
                public String getTitle() {
                    return "Experiment " + finalI;
                }

                @Override
                public String getDescription() {
                    return "Description for experiment " + finalI;
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

    private List<CourseTableProjection> getCourseTableProjections(int number) {
        List<CourseTableProjection> courses = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            int finalI = i;
            CourseTableProjection projection = new CourseTableProjection() {
                @Override
                public Integer getId() {
                    return finalI;
                }

                @Override
                public String getTitle() {
                    return "Course" + finalI;
                }

                @Override
                public String getDescription() {
                    return "Description for course " + finalI;
                }

                @Override
                public boolean isActive() {
                    return false;
                }
            };
            courses.add(projection);
        }
        return courses;
    }

}
