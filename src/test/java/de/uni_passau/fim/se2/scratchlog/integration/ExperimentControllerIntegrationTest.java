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

package de.uni_passau.fim.se2.scratchlog.integration;

import de.uni_passau.fim.se2.scratchlog.testing_utils.StringCreator;
import de.uni_passau.fim.se2.scratchlog.application.exception.NotFoundException;
import de.uni_passau.fim.se2.scratchlog.application.service.CourseService;
import de.uni_passau.fim.se2.scratchlog.application.service.ExperimentDataService;
import de.uni_passau.fim.se2.scratchlog.application.service.ExperimentService;
import de.uni_passau.fim.se2.scratchlog.application.service.MailService;
import de.uni_passau.fim.se2.scratchlog.application.service.PageService;
import de.uni_passau.fim.se2.scratchlog.application.service.ParticipantService;
import de.uni_passau.fim.se2.scratchlog.application.service.UserService;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Experiment;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Participant;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import de.uni_passau.fim.se2.scratchlog.util.enums.Language;
import de.uni_passau.fim.se2.scratchlog.util.enums.Role;
import de.uni_passau.fim.se2.scratchlog.web.AbstractControllerTest;
import de.uni_passau.fim.se2.scratchlog.web.controller.ExperimentController;
import de.uni_passau.fim.se2.scratchlog.web.dto.ExperimentDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.ParticipantDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.PasswordDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.UserDTO;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ExperimentController.class)
public class ExperimentControllerIntegrationTest extends AbstractControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private ExperimentService experimentService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CourseService courseService;

    @MockitoBean
    private ParticipantService participantService;

    @MockitoBean
    private PageService pageService;

    @MockitoBean
    private MailService mailService;

    @MockitoBean
    private ExperimentDataService experimentDataService;

    private static final String TITLE = "My Experiment";
    private static final String DESCRIPTION = "A description";
    private static final String INFO = "Some info text";
    private static final String POSTSCRIPT = "Some postscript";
    private static final String INFO_PARSED = "<p>Some info text</p>\n";
    private static final String GUI_URL = "scratch";
    private static final String ERROR = "redirect:/error";
    private static final String EXPERIMENT = "experiment";
    private static final String EXPERIMENT_EDIT = "experiment-edit";
    private static final String REDIRECT_EXPERIMENT = "redirect:/experiment?id=";
    private static final String INVALID = "redirect:/experiment?invalid=true&id=";
    private static final String SUCCESS = "redirect:/?success=true";
    private static final String REDIRECT_SECRET = "redirect:/secret?user=";
    private static final String REDIRECT_SECRET_LIST = "redirect:/secret/list?experiment=";
    private static final String EXPERIMENT_PARAM = "&experiment=";
    private static final String BLANK = "    ";
    private static final String EXPERIMENT_DTO = "experimentDTO";
    private static final String PASSWORD_DTO = "passwordDTO";
    private static final String ID_STRING = "1";
    private static final String ID_PARAM = "id";
    private static final String STATUS_PARAM = "stat";
    private static final String PAGE_PARAM = "page";
    private static final String PARTICIPANTS_PARAM = "participants";
    private static final String PARTICIPANTS = "participants";
    private static final String PARTICIPANT1 = "participant1";
    private static final String PARTICIPANT2 = "participant2";
    private static final String PARTICIPANT = "participant";
    private static final String PAGE = "page";
    private static final String LAST_PAGE_ATTRIBUTE = "lastPage";
    private static final String ERROR_ATTRIBUTE = "error";
    private static final String CURRENT = "3";
    private static final String FILETYPE_SB3 = "application/octet-stream";
    private static final String FILENAME_SB3 = "project.sb3";
    private static final String FILETYPE_CSV = "text/csv";
    private static final String FILENAME_CSV = "participants.csv";
    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";
    private static final String LONG_PASSWORD = StringCreator.createLongString(108);
    private static final int FIRST_PAGE = 0;
    private static final int LAST_PAGE = 3;
    private static final int PAGE_NUMBER = 3;
    private static final int ID = 1;
    private static final byte[] CONTENT = new byte[]{1, 2, 3};
    private final ExperimentDTO experimentDTO = new ExperimentDTO(ID, TITLE, DESCRIPTION, INFO, POSTSCRIPT, false,
            false, GUI_URL);
    private final UserDTO userDTO = new UserDTO(USERNAME, "admin1@admin.de", Role.ADMIN, Language.ENGLISH,
            PASSWORD, "secret1");
    private final UserDTO participant = new UserDTO(PARTICIPANT, "participant@part.de", Role.PARTICIPANT,
            Language.ENGLISH, "user", null);
    private final UserDTO participant1 = new UserDTO(PARTICIPANT1, "participant@part.de", Role.PARTICIPANT,
        Language.ENGLISH, "user", null);
    private final UserDTO participant2 = new UserDTO(PARTICIPANT2, "participant@part.de", Role.PARTICIPANT,
        Language.ENGLISH, "user", null);
    private final Page<Participant> participants = new PageImpl<>(getParticipants(5));
    private final ParticipantDTO participantDTO = new ParticipantDTO(ID, ID);
    private final PasswordDTO passwordDTO = new PasswordDTO(PASSWORD);

    @BeforeEach
    public void setup() {
        userDTO.setId(ID);
        userDTO.setActive(true);
        userDTO.setSecret("secret1");
        participant.setId(ID + 1);
        participant1.setId(ID + 2);
        participant2.setId(ID + 3);
        participant.setSecret(null);
        experimentDTO.setId(ID);
        experimentDTO.setTitle(TITLE);
        experimentDTO.setDescription(DESCRIPTION);
        experimentDTO.setPostscript(POSTSCRIPT);
        experimentDTO.setActive(false);
        experimentDTO.setCourseExperiment(false);
        experimentDTO.setCourse(null);
        participantDTO.setStart(null);
        passwordDTO.setPassword(PASSWORD);
    }

    @AfterEach
    public void resetService() {
        reset(experimentService, userService);
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void testGetExperiment() throws Exception {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(pageService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        when(pageService.getParticipantPage(anyInt(), anyInt())).thenReturn(participants);
        mvc.perform(get("/experiment")
                .param(ID_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(PASSWORD_DTO, notNullValue()))
                .andExpect(model().attribute(PARTICIPANTS, is(participants)))
                .andExpect(model().attribute(PAGE, is(FIRST_PAGE)))
                .andExpect(model().attribute(LAST_PAGE_ATTRIBUTE, is(LAST_PAGE)))
                .andExpect(model().attribute(EXPERIMENT_DTO, allOf(
                        hasProperty("id", is(ID)),
                        hasProperty("title", is(TITLE)),
                        hasProperty("description", is(DESCRIPTION)),
                        hasProperty("postscript", is(POSTSCRIPT)),
                        hasProperty("info", is(INFO_PARSED)),
                        hasProperty("active", is(false))
                )))
                .andExpect(view().name(EXPERIMENT));
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void testGetExperimentHasProject() throws Exception {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(pageService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        when(pageService.getParticipantPage(anyInt(), anyInt())).thenReturn(participants);
        when(experimentService.hasProjectFile(ID)).thenReturn(true);
        mvc.perform(get("/experiment")
                .param(ID_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute("project", is(true)))
                .andExpect(model().attribute(PASSWORD_DTO, notNullValue()))
                .andExpect(model().attribute(PARTICIPANTS, is(participants)))
                .andExpect(model().attribute(PAGE, is(FIRST_PAGE)))
                .andExpect(model().attribute(LAST_PAGE_ATTRIBUTE, is(LAST_PAGE)))
                .andExpect(model().attribute(EXPERIMENT_DTO, allOf(
                        hasProperty("id", is(ID)),
                        hasProperty("title", is(TITLE)),
                        hasProperty("description", is(DESCRIPTION)),
                        hasProperty("postscript", is(POSTSCRIPT)),
                        hasProperty("info", is(INFO_PARSED)),
                        hasProperty("active", is(false))
                )))
                .andExpect(view().name(EXPERIMENT));
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void testGetExperimentNotFound() throws Exception {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/experiment")
                .param(ID_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(experimentService).getExperiment(ID);
    }

    @Test
    @WithMockUser(username = "user", roles = {"PARTICIPANT"})
    public void testGetExperimentParticipant() throws Exception {
        experimentDTO.setActive(true);
        when(userService.getUser(anyString())).thenReturn(userDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        mvc.perform(get("/experiment")
                .param(ID_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(PASSWORD_DTO, notNullValue()))
                .andExpect(model().attribute("participant", is(participantDTO)))
                .andExpect(model().attribute("secret", nullValue()))
                .andExpect(model().attribute(PAGE, is(FIRST_PAGE)))
                .andExpect(model().attribute(LAST_PAGE_ATTRIBUTE, is(FIRST_PAGE)))
                .andExpect(model().attribute(PARTICIPANTS, empty()))
                .andExpect(model().attribute(EXPERIMENT_DTO, allOf(
                        hasProperty("id", is(ID)),
                        hasProperty("title", is(TITLE)),
                        hasProperty("description", is(DESCRIPTION)),
                        hasProperty("postscript", is(POSTSCRIPT)),
                        hasProperty("info", is(INFO_PARSED)),
                        hasProperty("active", is(true))
                )))
                .andExpect(view().name(EXPERIMENT));
    }

    @Test
    @WithMockUser(username = "user", roles = {"PARTICIPANT"})
    public void testGetExperimentParticipantSecretNull() throws Exception {
        experimentDTO.setActive(true);
        userDTO.setSecret(null);
        when(userService.getUser(anyString())).thenReturn(userDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        mvc.perform(get("/experiment")
                        .param(ID_PARAM, ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(PASSWORD_DTO, notNullValue()))
                .andExpect(model().attribute("participant", is(participantDTO)))
                .andExpect(model().attribute("secret", is(false)))
                .andExpect(model().attribute(PAGE, is(FIRST_PAGE)))
                .andExpect(model().attribute(LAST_PAGE_ATTRIBUTE, is(FIRST_PAGE)))
                .andExpect(model().attribute(PARTICIPANTS, empty()))
                .andExpect(model().attribute(EXPERIMENT_DTO, allOf(
                        hasProperty("id", is(ID)),
                        hasProperty("title", is(TITLE)),
                        hasProperty("description", is(DESCRIPTION)),
                        hasProperty("postscript", is(POSTSCRIPT)),
                        hasProperty("info", is(INFO_PARSED)),
                        hasProperty("active", is(true))
                )))
                .andExpect(view().name(EXPERIMENT));
    }

    @Test
    @WithMockUser(username = "user", roles = {"PARTICIPANT"})
    public void testGetExperimentParticipantNotFound() throws Exception {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUser(anyString())).thenThrow(NotFoundException.class);
        mvc.perform(get("/experiment")
                .param(ID_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService).getUser(anyString());
        verify(userService, never()).existsParticipant(ID, ID);
        verify(experimentService).getExperiment(ID);
    }

    @Test
    @WithMockUser(username = "user", roles = {"PARTICIPANT"})
    public void testGetExperimentNoParticipant() throws Exception {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUser(anyString())).thenReturn(userDTO);
        when(participantService.getParticipant(ID, ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/experiment")
                .param(ID_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService).getUser(anyString());
        verify(participantService).getParticipant(ID, ID);
        verify(experimentService).getExperiment(ID);
    }

    @Test
    @WithMockUser(username = "user", roles = {"PARTICIPANT"})
    public void testGetExperimentParticipantInactive() throws Exception {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        userDTO.setActive(false);
        when(userService.getUser(anyString())).thenReturn(userDTO);
        mvc.perform(get("/experiment")
                .param(ID_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService).getUser(anyString());
        verify(participantService, never()).getParticipant(anyInt(), anyInt());
        verify(experimentService).getExperiment(anyInt());
    }

    @Test
    public void testGetExperimentForm() throws  Exception {
        mvc.perform(get("/experiment/create")
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT_EDIT))
                .andExpect(model().attribute(EXPERIMENT_DTO, allOf(
                        hasProperty("courseExperiment", is(false)),
                        hasProperty("course", nullValue())
                )));
    }

    @Test
    public void testGetExperimentFormCourse() throws  Exception {
        mvc.perform(get("/experiment/create")
                        .param("course", ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT_EDIT))
                .andExpect(model().attribute(EXPERIMENT_DTO, allOf(
                        hasProperty("courseExperiment", is(true)),
                        hasProperty("course", is(ID))
                )));
    }

    @Test
    public void testGetEditExperimentForm() throws Exception {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        mvc.perform(get("/experiment/edit")
                        .param(ID_PARAM, ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(EXPERIMENT_DTO, allOf(
                        hasProperty("id", is(ID)),
                        hasProperty("title", is(TITLE)),
                        hasProperty("description", is(DESCRIPTION)),
                        hasProperty("postscript", is(POSTSCRIPT)),
                        hasProperty("info", is(INFO)),
                        hasProperty("active", is(false))
                )))
                .andExpect(view().name(EXPERIMENT_EDIT));
        verify(experimentService).getExperiment(ID);
    }

    @Test
    public void testGetEditExperimentFormNotFound() throws Exception {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/experiment/edit")
                        .param(ID_PARAM, ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(experimentService).getExperiment(ID);
    }

    @Test
    public void testGetExperimentFormInvalidId() throws Exception {
        mvc.perform(get("/experiment/edit")
                        .param(ID_PARAM, INFO)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is4xxClientError())
                .andExpect(view().name(Constants.ERROR));
        verify(experimentService, never()).getExperiment(ID);
    }

    @Test
    public void testEditExperiment() throws Exception {
        when(experimentService.updateExperiment(experimentDTO)).thenReturn(experimentDTO);
        mvc.perform(post("/experiment/update")
                .flashAttr(EXPERIMENT_DTO, experimentDTO)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_EXPERIMENT + ID));
        verify(experimentService).updateExperiment(experimentDTO);
    }

    @Test
    public void testEditExperimentCourse() throws Exception {
        experimentDTO.setCourseExperiment(true);
        experimentDTO.setCourse(ID);
        when(experimentService.updateExperiment(experimentDTO)).thenReturn(experimentDTO);
        when(courseService.existsActiveCourse(ID)).thenReturn(true);
        mvc.perform(post("/experiment/update")
                        .flashAttr(EXPERIMENT_DTO, experimentDTO)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_EXPERIMENT + ID));
        verify(courseService).existsActiveCourse(ID);
        verify(experimentService).updateExperiment(experimentDTO);
        verify(courseService).saveCourseExperiment(ID, ID);
        verify(participantService).addAllCourseParticipantsToExperiment(ID, ID);
        verify(experimentService, never()).deleteExperiment(anyInt());
    }

    @Test
    public void testEditExperimentCourseError() throws Exception {
        experimentDTO.setCourseExperiment(true);
        experimentDTO.setCourse(ID);
        when(experimentService.updateExperiment(experimentDTO)).thenReturn(experimentDTO);
        when(courseService.existsActiveCourse(ID)).thenReturn(true);
        doThrow(ConstraintViolationException.class).when(courseService).saveCourseExperiment(ID, ID);
        mvc.perform(post("/experiment/update")
                        .flashAttr(EXPERIMENT_DTO, experimentDTO)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(courseService).existsActiveCourse(ID);
        verify(experimentService).updateExperiment(experimentDTO);
        verify(courseService).saveCourseExperiment(ID, ID);
        verify(participantService, never()).addAllCourseParticipantsToExperiment(anyInt(), anyInt());
        verify(experimentService).deleteExperiment(anyInt());
    }

    @Test
    public void testEditExperimentExists() throws Exception {
        final Experiment experiment = new Experiment();
        experiment.setTitle(experimentDTO.getTitle());

        when(experimentService.updateExperiment(experimentDTO)).thenReturn(experimentDTO);
        when(experimentService.findByTitle(TITLE)).thenReturn(Optional.of(experiment));
        mvc.perform(post("/experiment/update")
                .flashAttr(EXPERIMENT_DTO, experimentDTO)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT_EDIT));
        verify(experimentService, never()).updateExperiment(experimentDTO);
    }

    @Test
    public void testEditExperimentCreate() throws Exception {
        experimentDTO.setId(null);
        when(experimentService.updateExperiment(experimentDTO)).thenReturn(experimentDTO);
        mvc.perform(post("/experiment/update")
                .flashAttr(EXPERIMENT_DTO, experimentDTO)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_EXPERIMENT + null));
        verify(experimentService).existsExperiment(TITLE);
        verify(experimentService).updateExperiment(experimentDTO);
    }

    @Test
    public void testEditExperimentCreateExists() throws Exception {
        experimentDTO.setId(null);
        when(experimentService.updateExperiment(experimentDTO)).thenReturn(experimentDTO);
        when(experimentService.existsExperiment(TITLE)).thenReturn(true);
        mvc.perform(post("/experiment/update")
                .flashAttr(EXPERIMENT_DTO, experimentDTO)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT_EDIT));
        verify(experimentService).existsExperiment(TITLE);
        verify(experimentService, never()).updateExperiment(experimentDTO);
    }

    @Test
    public void testEditExperimentInvalidInput() throws Exception {
        experimentDTO.setTitle(null);
        experimentDTO.setDescription(null);
        mvc.perform(post("/experiment/update")
                .flashAttr(EXPERIMENT_DTO, experimentDTO)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT_EDIT));
        verify(experimentService, never()).existsExperiment(anyString());
        verify(experimentService, never()).updateExperiment(experimentDTO);
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"ADMIN"})
    public void testDeleteExperiment() throws Exception {
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(userService.matchesPassword(PASSWORD, PASSWORD)).thenReturn(true);
        mvc.perform(post("/experiment/delete")
                .param(ID_PARAM, ID_STRING)
                .flashAttr(PASSWORD_DTO, passwordDTO)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(SUCCESS));
        verify(userService).getUser(USERNAME);
        verify(userService).matchesPassword(PASSWORD, PASSWORD);
        verify(experimentService).deleteExperiment(ID);
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"ADMIN"})
    public void testDeleteExperimentPasswordNotMatching() throws Exception {
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        mvc.perform(post("/experiment/delete")
                .param(ID_PARAM, ID_STRING)
                .flashAttr(PASSWORD_DTO, passwordDTO)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(INVALID + ID));
        verify(userService).getUser(USERNAME);
        verify(userService).matchesPassword(PASSWORD, PASSWORD);
        verify(experimentService, never()).deleteExperiment(anyInt());
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"ADMIN"})
    public void testDeleteExperimentPasswordTooLong() throws Exception {
        passwordDTO.setPassword(LONG_PASSWORD);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        mvc.perform(post("/experiment/delete")
                .param(ID_PARAM, ID_STRING)
                .flashAttr(PASSWORD_DTO, passwordDTO)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(INVALID + ID));
        verify(userService).getUser(USERNAME);
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(experimentService, never()).deleteExperiment(anyInt());
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"ADMIN"})
    public void testDeleteExperimentNotFound() throws Exception {
        when(userService.getUser(USERNAME)).thenThrow(NotFoundException.class);
        mvc.perform(post("/experiment/delete")
                .param(ID_PARAM, ID_STRING)
                .flashAttr(PASSWORD_DTO, passwordDTO)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService).getUser(USERNAME);
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(experimentService, never()).deleteExperiment(anyInt());
    }

    @Test
    public void testDeleteExperimentInvalidId() throws Exception {
        mvc.perform(post("/experiment/delete")
                .param(ID_PARAM, BLANK)
                .flashAttr(PASSWORD_DTO, passwordDTO)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is4xxClientError())
                .andExpect(view().name(Constants.ERROR));
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(experimentService, never()).deleteExperiment(anyInt());
    }

    @Test
    public void testChangeExperimentStatusOpen() throws Exception {
        setMailServer(true);
        experimentDTO.setActive(true);
        List<UserDTO> userDTOS = new ArrayList<>();
        userDTOS.add(participant);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(experimentService.changeExperimentStatus(true, ID)).thenReturn(experimentDTO);
        when(userService.reactivateUserAccounts(ID)).thenReturn(userDTOS);
        when(pageService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        when(pageService.getParticipantPage(anyInt(), anyInt())).thenReturn(participants);
        mvc.perform(get("/experiment/status")
                .param(STATUS_PARAM, "open")
                .param(ID_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(model().attribute(PASSWORD_DTO, notNullValue()))
                .andExpect(model().attribute(PARTICIPANTS, participants))
                .andExpect(model().attribute(PAGE, is(FIRST_PAGE)))
                .andExpect(model().attribute(LAST_PAGE_ATTRIBUTE, is(LAST_PAGE)))
                .andExpect(model().attribute("experimentDTO", allOf(
                        hasProperty("id", is(ID)),
                        hasProperty("title", is(TITLE)),
                        hasProperty("description", is(DESCRIPTION)),
                        hasProperty("postscript", is(POSTSCRIPT)),
                        hasProperty("info", is(INFO_PARSED)),
                        hasProperty("active", is(true))
                )))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT));
    }

    @Test
    public void testChangeExperimentStatusOpenNoMailServer() throws Exception {
        setMailServer(false);
        experimentDTO.setActive(true);
        List<UserDTO> userDTOS = new ArrayList<>();
        userDTOS.add(participant);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(experimentService.changeExperimentStatus(true, ID)).thenReturn(experimentDTO);
        when(userService.reactivateUserAccounts(ID)).thenReturn(userDTOS);
        mvc.perform(get("/experiment/status")
                        .param(STATUS_PARAM, "open")
                        .param(ID_PARAM, ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_SECRET_LIST + ID));
    }

    @Test
    public void testChangeExperimentStatusClose() throws Exception {
        when(experimentService.changeExperimentStatus(false, ID)).thenReturn(experimentDTO);
        when(pageService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        when(pageService.getParticipantPage(anyInt(), anyInt())).thenReturn(participants);
        mvc.perform(get("/experiment/status")
                .param(STATUS_PARAM, "close")
                .param(ID_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(model().attribute(PASSWORD_DTO, notNullValue()))
                .andExpect(model().attribute(PARTICIPANTS, participants))
                .andExpect(model().attribute(PAGE, is(FIRST_PAGE)))
                .andExpect(model().attribute(LAST_PAGE_ATTRIBUTE, is(LAST_PAGE)))
                .andExpect(model().attribute("experimentDTO", allOf(
                        hasProperty("id", is(ID)),
                        hasProperty("title", is(TITLE)),
                        hasProperty("description", is(DESCRIPTION)),
                        hasProperty("postscript", is(POSTSCRIPT)),
                        hasProperty("info", is(INFO_PARSED)),
                        hasProperty("active", is(false))
                )))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT));
    }

    @Test
    public void testChangeExperimentStatusOpenInactiveCourse() throws Exception {
        setMailServer(false);
        experimentDTO.setActive(true);
        experimentDTO.setCourseExperiment(true);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        mvc.perform(get("/experiment/status")
                        .param(STATUS_PARAM, "open")
                        .param(ID_PARAM, ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT))
                .andExpect(model().attribute(ERROR_ATTRIBUTE, notNullValue()));
    }

    @Test
    public void testChangeExperimentStatusInvalid() throws Exception {
        mvc.perform(get("/experiment/status")
                .param(STATUS_PARAM, INFO)
                .param(ID_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(experimentService, never()).changeExperimentStatus(anyBoolean(), anyInt());
    }

    @Test
    public void testChangeExperimentStatusNotFound() throws Exception {
        when(experimentService.changeExperimentStatus(false, ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/experiment/status")
                .param(STATUS_PARAM, "close")
                .param(ID_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(experimentService).changeExperimentStatus(false, ID);
    }

    @Test
    public void testAddParticipants() throws Exception {
        setMailServer(true);
        experimentDTO.setActive(true);
        participant1.setSecret("secret");
        participant2.setSecret("secret");
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT1)).thenReturn(participant1);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT2)).thenReturn(participant2);
        when(userService.updateUser(participant1)).thenReturn(participant1);
        when(userService.updateUser(participant2)).thenReturn(participant2);
        when(mailService.sendEmail(anyString(), anyString(), any(), anyString())).thenReturn(true);
        mvc.perform(post("/experiment/add")
                .param(PARTICIPANTS_PARAM, PARTICIPANT1)
                .param(PARTICIPANTS_PARAM, PARTICIPANT2)
                .param(ID_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_EXPERIMENT + ID));
        verify(participantService).addParticipants(List.of(participant1.getId(), participant2.getId()), ID);
        verify(mailService, times(2)).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testAddParticipantsSecretNull() throws Exception {
        setMailServer(true);
        experimentDTO.setActive(true);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT)).thenReturn(participant);
        when(userService.updateUser(participant)).thenReturn(participant);
        when(mailService.sendEmail(anyString(), anyString(), any(), anyString())).thenReturn(true);
        mvc.perform(post("/experiment/add")
                .param(PARTICIPANTS_PARAM, PARTICIPANT)
                .param(ID_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_EXPERIMENT + ID));
        verify(userService).updateUser(participant);
        verify(participantService).addParticipants(List.of(participant.getId()), ID);
        verify(mailService).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testAddParticipantsNoMailServer() throws Exception {
        setMailServer(false);
        experimentDTO.setActive(true);
        participant.setSecret("secret");
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT)).thenReturn(participant);
        when(userService.updateUser(participant)).thenReturn(participant);
        mvc.perform(post("/experiment/add")
                        .param(PARTICIPANTS_PARAM, PARTICIPANT)
                        .param(ID_PARAM, ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_SECRET + participant.getId() + EXPERIMENT_PARAM + ID));
        verify(participantService).addParticipants(List.of(participant.getId()), ID);
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testAddParticipantsExperimentInactive() throws Exception {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT)).thenReturn(participant);
        when(pageService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        when(pageService.getParticipantPage(anyInt(), anyInt())).thenReturn(participants);
        mvc.perform(post("/experiment/add")
                .param(PARTICIPANTS_PARAM, PARTICIPANT)
                .param(ID_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(model().attribute(PASSWORD_DTO, notNullValue()))
                .andExpect(model().attribute(PARTICIPANTS, participants))
                .andExpect(model().attribute(PAGE, FIRST_PAGE))
                .andExpect(model().attribute(LAST_PAGE_ATTRIBUTE, is(LAST_PAGE)))
                .andExpect(model().attribute("experimentDTO", experimentDTO))
                .andExpect(model().attribute(ERROR_ATTRIBUTE, notNullValue()))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT));
        verify(participantService, never()).addParticipants(List.of(participant.getId()), ID);
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testAddParticipantsParticipantExists() throws Exception {
        experimentDTO.setActive(true);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT)).thenReturn(participant);
        when(userService.existsParticipant(participant.getId(), ID)).thenReturn(true);
        when(pageService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        when(pageService.getParticipantPage(anyInt(), anyInt())).thenReturn(participants);
        mvc.perform(post("/experiment/add")
                        .param(PARTICIPANTS_PARAM, PARTICIPANT)
                        .param(ID_PARAM, ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(model().attribute(PASSWORD_DTO, notNullValue()))
                .andExpect(model().attribute(PARTICIPANTS, participants))
                .andExpect(model().attribute(PAGE, FIRST_PAGE))
                .andExpect(model().attribute(LAST_PAGE_ATTRIBUTE, is(LAST_PAGE)))
                .andExpect(model().attribute("experimentDTO", experimentDTO))
                .andExpect(model().attribute(ERROR_ATTRIBUTE, notNullValue()))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT));
        verify(participantService, never()).addParticipants(anyList(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testAddParticipantsParticipantNull() throws Exception {
        experimentDTO.setActive(true);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(pageService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        when(pageService.getParticipantPage(anyInt(), anyInt())).thenReturn(participants);
        mvc.perform(post("/experiment/add")
                        .param(PARTICIPANTS_PARAM, PARTICIPANT)
                        .param(ID_PARAM, ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(model().attribute(PASSWORD_DTO, notNullValue()))
                .andExpect(model().attribute(PARTICIPANTS, participants))
                .andExpect(model().attribute(PAGE, FIRST_PAGE))
                .andExpect(model().attribute(LAST_PAGE_ATTRIBUTE, is(LAST_PAGE)))
                .andExpect(model().attribute("experimentDTO", experimentDTO))
                .andExpect(model().attribute(ERROR_ATTRIBUTE, notNullValue()))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT));
        verify(userService).getUserByUsernameOrEmail(PARTICIPANT);
        verify(userService, never()).existsParticipant(anyInt(), anyInt());
        verify(participantService, never()).addParticipants(anyList(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testAddParticipantsInvalidQuery() throws Exception {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(pageService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        when(pageService.getParticipantPage(anyInt(), anyInt())).thenReturn(participants);
        mvc.perform(post("/experiment/add")
                .param(PARTICIPANTS_PARAM, BLANK)
                .param(ID_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(model().attribute(PASSWORD_DTO, notNullValue()))
                .andExpect(model().attribute(PARTICIPANTS, participants))
                .andExpect(model().attribute(PAGE, FIRST_PAGE))
                .andExpect(model().attribute(LAST_PAGE_ATTRIBUTE, is(LAST_PAGE)))
                .andExpect(model().attribute("experimentDTO", experimentDTO))
                .andExpect(model().attribute(ERROR_ATTRIBUTE, notNullValue()))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT));
        verify(participantService, never()).addParticipants(anyList(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testAddParticipantsExperimentNotFound() throws Exception {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        mvc.perform(post("/experiment/add")
                .param(PARTICIPANTS_PARAM, PARTICIPANT)
                .param(ID_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(experimentService).getExperiment(ID);
        verify(participantService, never()).addParticipants(List.of(participant.getId()), ID);
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testGetPage() throws Exception {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(pageService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        when(pageService.getParticipantPage(anyInt(), anyInt())).thenReturn(participants);
        mvc.perform(get("/experiment/page")
                .param(ID_PARAM, ID_STRING)
                .param(PAGE_PARAM, CURRENT)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(model().attribute(PASSWORD_DTO, notNullValue()))
                .andExpect(model().attribute(PARTICIPANTS, participants))
                .andExpect(model().attribute(PAGE, is(PAGE_NUMBER)))
                .andExpect(model().attribute(LAST_PAGE_ATTRIBUTE, is(LAST_PAGE)))
                .andExpect(model().attribute("experimentDTO", allOf(
                        hasProperty("id", is(ID)),
                        hasProperty("title", is(TITLE)),
                        hasProperty("description", is(DESCRIPTION)),
                        hasProperty("postscript", is(POSTSCRIPT)),
                        hasProperty("info", is(INFO_PARSED)),
                        hasProperty("active", is(false))
                )))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT));
    }

    @Test
    public void testGetNextPageNotFound() throws Exception {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/experiment/page")
                .param(ID_PARAM, ID_STRING)
                .param(PAGE_PARAM, CURRENT)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
    }

    @Test
    public void testGetNextInvalidId() throws Exception {
        mvc.perform(get("/experiment/page")
                .param(ID_PARAM, BLANK)
                .param(PAGE_PARAM, CURRENT)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is4xxClientError())
                .andExpect(view().name(Constants.ERROR));
    }

    @Test
    public void testGetClampToZero() throws Exception {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(pageService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        when(pageService.getParticipantPage(anyInt(), anyInt())).thenReturn(participants);
        mvc.perform(get("/experiment/page")
                .param(ID_PARAM, ID_STRING)
                .param(PAGE_PARAM, "-1")
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(model().attribute(PAGE, is(-1)))
                .andExpect(status().isOk())
                .andExpect(model().attribute(LAST_PAGE_ATTRIBUTE, is(LAST_PAGE)))
                .andExpect(view().name(EXPERIMENT));
    }

    @Test
    public void testDownloadCSVFile() throws Exception {
        mvc.perform(get("/experiment/csv")
                .param(ID_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk());
        verify(experimentDataService).getEventDataCsv(eq(ID), any());
    }

    @Test
    public void testAddCSVParticipants() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", FILENAME_CSV, FILETYPE_CSV,
                new ClassPathResource(FILENAME_CSV).getInputStream());
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.existsUser(anyString())).thenReturn(true);
        when(pageService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        when(pageService.getParticipantPage(anyInt(), anyInt())).thenReturn(participants);
        mockMvc.perform(multipart("/experiment/csv")
                .file(file)
                .param(ID_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT))
                .andExpect(model().attribute(PARTICIPANTS, is(participants)))
                .andExpect(model().attribute(PAGE, is(FIRST_PAGE)))
                .andExpect(model().attribute(LAST_PAGE_ATTRIBUTE, is(LAST_PAGE)))
                .andExpect(model().attribute(EXPERIMENT_DTO, is(experimentDTO)));
    }

    @Test
    public void testAddCSVParticipantsAdmin() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", FILENAME_CSV, FILETYPE_CSV,
                new ClassPathResource(FILENAME_CSV).getInputStream());
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(pageService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        when(pageService.getParticipantPage(anyInt(), anyInt())).thenReturn(participants);
        when(userService.getInvalidParticipantUsernames(any())).thenReturn(List.of("admin"));
        mockMvc.perform(multipart("/experiment/csv")
                        .file(file)
                        .param(ID_PARAM, ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT))
                .andExpect(model().attribute(PARTICIPANTS, is(participants)))
                .andExpect(model().attribute(PAGE, is(FIRST_PAGE)))
                .andExpect(model().attribute(LAST_PAGE_ATTRIBUTE, is(LAST_PAGE)))
                .andExpect(model().attribute(EXPERIMENT_DTO, is(experimentDTO)))
                .andExpect(model().attribute(ERROR_ATTRIBUTE, notNullValue()));
    }

    @Test
    public void testAddCSVParticipantsInvalidFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", FILENAME_SB3, FILETYPE_CSV,
                new ClassPathResource(FILENAME_CSV).getInputStream());
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(pageService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        when(pageService.getParticipantPage(anyInt(), anyInt())).thenReturn(participants);
        when(userService.parseUserListCsv(file)).thenThrow(new IllegalArgumentException("file_type"));
        mockMvc.perform(multipart("/experiment/csv")
                        .file(file)
                        .param(ID_PARAM, ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(EXPERIMENT))
                .andExpect(model().attribute(PARTICIPANTS, is(participants)))
                .andExpect(model().attribute(PAGE, is(FIRST_PAGE)))
                .andExpect(model().attribute(LAST_PAGE_ATTRIBUTE, is(LAST_PAGE)))
                .andExpect(model().attribute(EXPERIMENT_DTO, is(experimentDTO)))
                .andExpect(model().attribute(ERROR_ATTRIBUTE, notNullValue()));
    }

    @Test
    public void testAddCSVParticipantsInvalidId() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", FILENAME_CSV, FILETYPE_CSV,
                new ClassPathResource(FILENAME_CSV).getInputStream());
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mockMvc.perform(multipart("/experiment/csv")
                        .file(file)
                        .param(ID_PARAM, ID_PARAM)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is4xxClientError())
                .andExpect(view().name(Constants.ERROR));
    }

    @Test
    public void testDownloadLitterBoxAnalysis() throws Exception {
        when(experimentDataService.getLitterBoxAnalysisResults(ID)).thenReturn(new ArrayList<>());
        mvc.perform(get("/experiment/analysis")
                        .param(ID_PARAM, ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk());
        verify(experimentDataService).getLitterBoxAnalysisResults(ID);
    }

    private List<Participant> getParticipants(int number) {
        List<Participant> participants = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            participants.add(new Participant(new User(), new Experiment(), LocalDateTime.now(), null));
        }
        return participants;
    }
}
