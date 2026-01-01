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
import de.uni_passau.fim.se2.scratchlog.spring.configuration.SecurityTestConfig;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import de.uni_passau.fim.se2.scratchlog.util.enums.Language;
import de.uni_passau.fim.se2.scratchlog.util.enums.Role;
import de.uni_passau.fim.se2.scratchlog.web.controller.ExperimentController;
import de.uni_passau.fim.se2.scratchlog.web.dto.ExperimentDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.ParticipantDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.PasswordDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.LocaleResolver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@WebMvcTest(ExperimentController.class)
@Import(SecurityTestConfig.class)
public class ExperimentControllerTest extends AbstractControllerTest {

    @Autowired
    private ExperimentController experimentController;

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

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private ResourceBundle resourceBundle;

    @Mock
    private LocaleResolver localeResolver;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private MultipartFile file;

    private MockedStatic<SecurityContextHolder> securityContextHolder;
    private static final String TITLE = "My Experiment";
    private static final String DESCRIPTION = "A description";
    private static final String INFO = "Some info text";
    private static final String POSTSCRIPT = "Some postscript";
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
    private static final int INVALID_ID = -1;
    private static final String ADMIN = "ROLE_ADMIN";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "password";
    private static final String EMAIL = "participant@part.de";
    private static final String LONG_PASSWORD = StringCreator.createLongString(55);
    private static final String PARTICIPANTS = "participants";
    private static final String PARTICIPANT1 = "participant1";
    private static final String PARTICIPANT2 = "participant2";
    private static final List<String> PARTICIPANT_LIST = List.of(PARTICIPANT1, PARTICIPANT2);
    private static final int PAGE = 3;
    private static final int LAST = 4;
    private static final String FILETYPE_SB3 = "application/octet-stream";
    private static final String FILENAME_SB3 = "project.sb3";
    private static final String FILETYPE_CSV = "text/csv";
    private static final String FILENAME_CSV = "participants.csv";
    private static final String ERROR_ATTRIBUTE = "error";
    private static final int LAST_PAGE = 3;
    private static final int ID = 1;
    private static final String GUI_URL = "scratch";
    private static final byte[] CONTENT = new byte[]{1, 2, 3};
    private final ExperimentDTO experimentDTO = new ExperimentDTO(ID, TITLE, DESCRIPTION, INFO, POSTSCRIPT, false,
            false, GUI_URL);
    private final UserDTO userDTO = new UserDTO(USERNAME, "admin1@admin.de", Role.ADMIN, Language.ENGLISH, PASSWORD,
            "secret1");
    private final UserDTO participant = new UserDTO(PARTICIPANTS, EMAIL, Role.PARTICIPANT, Language.ENGLISH, "user",
            null);
    private final UserDTO participant1 = new UserDTO(PARTICIPANT1, "participant1@part.de", Role.PARTICIPANT,
            Language.ENGLISH, "user", null);
    private final UserDTO participant2 = new UserDTO(PARTICIPANT2, "participant1@part.de", Role.PARTICIPANT,
        Language.ENGLISH, "user", null);
    private final Page<Participant> participants = new PageImpl<>(getParticipants(5));
    private final List<UserDTO> userDTOS = new ArrayList<>();
    private final ParticipantDTO participantDTO = new ParticipantDTO(ID, ID);
    private final PasswordDTO passwordDTO = new PasswordDTO(PASSWORD);

    @BeforeEach
    public void setup() {
        userDTO.setId(ID);
        participant.setId(ID + 1);
        participant1.setId(ID + 2);
        participant2.setId(ID + 3);
        participant.setSecret(null);
        userDTO.setActive(true);
        userDTO.setSecret("secret1");
        participant.setEmail(EMAIL);
        experimentDTO.setActive(false);
        experimentDTO.setCourseExperiment(false);
        experimentDTO.setCourse(null);
        experimentDTO.setId(ID);
        experimentDTO.setTitle(TITLE);
        experimentDTO.setDescription(DESCRIPTION);
        experimentDTO.setPostscript(POSTSCRIPT);
        experimentDTO.setInfo(INFO);
        securityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        userDTOS.add(participant);
        userDTOS.add(userDTO);
        participantDTO.setStart(null);
        participantDTO.setEnd(null);
        passwordDTO.setPassword(PASSWORD);
    }

    @AfterEach
    public void cleanup() {
        securityContextHolder.close();
    }

    @Test
    public void testGetExperiment() {
        when(httpServletRequest.isUserInRole(ADMIN)).thenReturn(true);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(pageService.getParticipantPage(anyInt(), anyInt())).thenReturn(participants);
        String returnString = experimentController.getExperiment(ID, model, httpServletRequest);
        assertEquals(EXPERIMENT, returnString);
        verify(httpServletRequest).isUserInRole(ADMIN);
        verify(experimentService).getExperiment(ID);
        verify(pageService).getLastParticipantPage(ID);
        verify(pageService).getParticipantPage(anyInt(), anyInt());
        verify(experimentService).hasProjectFile(ID);
        verify(model).addAttribute(EXPERIMENT_DTO, experimentDTO);
        verify(model).addAttribute(PARTICIPANTS, participants);
    }

    @Test
    public void testGetExperimentProjectFile() {
        when(httpServletRequest.isUserInRole(ADMIN)).thenReturn(true);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(pageService.getParticipantPage(anyInt(), anyInt())).thenReturn(participants);
        when(experimentService.hasProjectFile(ID)).thenReturn(true);
        String returnString = experimentController.getExperiment(ID, model, httpServletRequest);
        assertEquals(EXPERIMENT, returnString);
        verify(httpServletRequest).isUserInRole(ADMIN);
        verify(experimentService).getExperiment(ID);
        verify(pageService).getLastParticipantPage(ID);
        verify(pageService).getParticipantPage(anyInt(), anyInt());
        verify(experimentService).hasProjectFile(ID);
        verify(model).addAttribute(EXPERIMENT_DTO, experimentDTO);
        verify(model).addAttribute(PARTICIPANTS, participants);
        verify(model).addAttribute("project", true);
    }

    @Test
    public void testGetExperimentNotFound() {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        String returnString = experimentController.getExperiment(ID, model, httpServletRequest);
        assertEquals(ERROR, returnString);
        verify(httpServletRequest, never()).isUserInRole(anyString());
        verify(experimentService).getExperiment(ID);
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testGetExperimentParticipant() {
        experimentDTO.setActive(true);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        assertEquals(EXPERIMENT, experimentController.getExperiment(ID, model, httpServletRequest));
        verify(securityContext).getAuthentication();
        verify(authentication).getName();
        verify(httpServletRequest).isUserInRole(ADMIN);
        verify(experimentService).getExperiment(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(pageService, never()).getParticipantPage(anyInt(), anyInt());
        verify(experimentService).hasProjectFile(ID);
        verify(model).addAttribute(EXPERIMENT_DTO, experimentDTO);
        verify(model).addAttribute("participant", participantDTO);
        verify(model, never()).addAttribute("secret", false);
    }

    @Test
    public void testGetExperimentParticipantSecretNull() {
        userDTO.setSecret(null);
        experimentDTO.setActive(true);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        assertEquals(EXPERIMENT, experimentController.getExperiment(ID, model, httpServletRequest));
        verify(securityContext).getAuthentication();
        verify(authentication).getName();
        verify(httpServletRequest).isUserInRole(ADMIN);
        verify(experimentService).getExperiment(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(pageService, never()).getParticipantPage(anyInt(), anyInt());
        verify(experimentService).hasProjectFile(ID);
        verify(model).addAttribute(EXPERIMENT_DTO, experimentDTO);
        verify(model).addAttribute("participant", participantDTO);
        verify(model).addAttribute("secret", false);
    }

    @Test
    public void testGetExperimentUserNotFound() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUser(USERNAME)).thenThrow(NotFoundException.class);
        String returnString = experimentController.getExperiment(ID, model, httpServletRequest);
        assertEquals(ERROR, returnString);
        verify(securityContext).getAuthentication();
        verify(authentication).getName();
        verify(userService, never()).existsParticipant(userDTO.getId(), ID);
        verify(httpServletRequest).isUserInRole(ADMIN);
        verify(experimentService).getExperiment(ID);
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testGetExperimentNoParticipant() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipant(ID, ID)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, experimentController.getExperiment(ID, model, httpServletRequest));
        verify(securityContext).getAuthentication();
        verify(authentication).getName();
        verify(participantService).getParticipant(ID, ID);
        verify(httpServletRequest).isUserInRole(ADMIN);
        verify(experimentService).getExperiment(ID);
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testGetExperimentParticipantInactive() {
        userDTO.setActive(false);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        assertEquals(ERROR, experimentController.getExperiment(ID, model, httpServletRequest));
        verify(securityContext).getAuthentication();
        verify(authentication).getName();
        verify(httpServletRequest).isUserInRole(ADMIN);
        verify(participantService, never()).getParticipant(ID, ID);
        verify(experimentService).getExperiment(ID);
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testGetExperimentForm() {
        assertEquals(EXPERIMENT_EDIT, experimentController.getExperimentForm(null, model));
        verify(model).addAttribute(anyString(), any());
    }

    @Test
    public void testGetExperimentFormCourse() {
        assertEquals(EXPERIMENT_EDIT, experimentController.getExperimentForm(ID, model));
        verify(model).addAttribute(anyString(), any());
    }

    @Test
    public void testGetExperimentEditForm() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        String returnString = experimentController.getEditExperimentForm(ID, model);
        assertEquals(EXPERIMENT_EDIT, returnString);
        verify(experimentService).getExperiment(ID);
        verify(model).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testGetExperimentEditFormNotFound() {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        String returnString = experimentController.getEditExperimentForm(ID, model);
        assertEquals(ERROR, returnString);
        verify(experimentService).getExperiment(ID);
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testEditExperiment() {
        when(experimentService.updateExperiment(experimentDTO)).thenReturn(experimentDTO);
        String returnString = experimentController.editExperiment(experimentDTO, bindingResult);
        assertEquals(REDIRECT_EXPERIMENT + ID, returnString);
        verify(bindingResult, never()).addError(any());
        verify(experimentService).existsExperiment(experimentDTO.getTitle(), experimentDTO.getId());
        verify(experimentService).updateExperiment(experimentDTO);
    }

    @Test
    public void testEditExperimentCourse() {
        experimentDTO.setCourse(ID);
        experimentDTO.setCourseExperiment(true);
        when(courseService.existsActiveCourse(ID)).thenReturn(true);
        when(experimentService.updateExperiment(experimentDTO)).thenReturn(experimentDTO);
        assertEquals(REDIRECT_EXPERIMENT + ID, experimentController.editExperiment(experimentDTO,
                bindingResult));
        verify(bindingResult, never()).addError(any());
        verify(courseService).existsActiveCourse(ID);
        verify(experimentService).existsExperiment(experimentDTO.getTitle(), experimentDTO.getId());
        verify(experimentService).updateExperiment(experimentDTO);
        verify(courseService).saveCourseExperiment(ID, ID);
        verify(participantService).addAllCourseParticipantsToExperiment(ID, ID);
        verify(experimentService, never()).deleteExperiment(anyInt());
    }

    @Test
    public void testEditExperimentCourseInvalid() {
        experimentDTO.setCourse(ID);
        experimentDTO.setCourseExperiment(true);
        when(courseService.existsActiveCourse(ID)).thenReturn(true);
        when(experimentService.updateExperiment(experimentDTO)).thenReturn(experimentDTO);
        doThrow(NotFoundException.class).when(participantService).addAllCourseParticipantsToExperiment(ID, ID);
        assertEquals(Constants.ERROR, experimentController.editExperiment(experimentDTO,
                bindingResult));
        verify(bindingResult, never()).addError(any());
        verify(courseService).existsActiveCourse(ID);
        verify(experimentService).existsExperiment(experimentDTO.getTitle(), experimentDTO.getId());
        verify(experimentService).updateExperiment(experimentDTO);
        verify(courseService).saveCourseExperiment(ID, ID);
        verify(participantService).addAllCourseParticipantsToExperiment(ID, ID);
        verify(experimentService).deleteExperiment(ID);
    }

    @Test
    public void testEditExperimentTitleExists() {
        when(experimentService.existsExperiment(experimentDTO.getTitle(), experimentDTO.getId())).thenReturn(true);
        when(bindingResult.hasErrors()).thenReturn(true);
        String returnString = experimentController.editExperiment(experimentDTO, bindingResult);
        assertEquals(EXPERIMENT_EDIT, returnString);
        verify(bindingResult).addError(any());
        verify(experimentService).existsExperiment(experimentDTO.getTitle(), experimentDTO.getId());
        verify(experimentService, never()).updateExperiment(any());
    }

    @Test
    public void testEditExperimentCreate() {
        experimentDTO.setId(null);
        experimentDTO.setPostscript(BLANK);
        when(experimentService.updateExperiment(experimentDTO)).thenReturn(experimentDTO);
        String returnString = experimentController.editExperiment(experimentDTO, bindingResult);
        assertEquals(REDIRECT_EXPERIMENT + experimentDTO.getId(), returnString);
        verify(bindingResult, never()).addError(any());
        verify(experimentService).existsExperiment(experimentDTO.getTitle());
        verify(experimentService).updateExperiment(experimentDTO);
    }

    @Test
    public void testEditExperimentCreateTitleExists() {
        experimentDTO.setId(null);
        when(experimentService.existsExperiment(experimentDTO.getTitle())).thenReturn(true);
        when(bindingResult.hasErrors()).thenReturn(true);
        String returnString = experimentController.editExperiment(experimentDTO, bindingResult);
        assertEquals(EXPERIMENT_EDIT, returnString);
        verify(bindingResult).addError(any());
        verify(experimentService).existsExperiment(experimentDTO.getTitle());
        verify(experimentService, never()).updateExperiment(any());
    }

    @Test
    public void testEditExperimentTitleTooLong() {
        experimentDTO.setPostscript(null);
        experimentDTO.setTitle(StringCreator.createLongString(200));
        when(bindingResult.hasErrors()).thenReturn(true);
        String returnString = experimentController.editExperiment(experimentDTO, bindingResult);
        assertEquals(EXPERIMENT_EDIT, returnString);
        verify(bindingResult).addError(any());
        verify(experimentService).existsExperiment(experimentDTO.getTitle(), experimentDTO.getId());
        verify(experimentService, never()).updateExperiment(any());
    }

    @Test
    public void testEditExperimentTitleAndDescriptionNull() {
        experimentDTO.setTitle(null);
        experimentDTO.setDescription(null);
        when(bindingResult.hasErrors()).thenReturn(true);
        String returnString = experimentController.editExperiment(experimentDTO, bindingResult);
        assertEquals(EXPERIMENT_EDIT, returnString);
        verify(bindingResult, times(2)).addError(any());
        verify(experimentService).existsExperiment(experimentDTO.getTitle(), experimentDTO.getId());
        verify(experimentService, never()).updateExperiment(any());
    }

    @Test
    public void testEditExperimentTitleAndDescriptionBlank() {
        experimentDTO.setTitle(BLANK);
        experimentDTO.setDescription(BLANK);
        when(bindingResult.hasErrors()).thenReturn(true);
        String returnString = experimentController.editExperiment(experimentDTO, bindingResult);
        assertEquals(EXPERIMENT_EDIT, returnString);
        verify(bindingResult, times(2)).addError(any());
        verify(experimentService).existsExperiment(experimentDTO.getTitle(), experimentDTO.getId());
        verify(experimentService, never()).updateExperiment(any());
    }

    @Test
    public void testEditExperimentDescriptionTooLong() {
        experimentDTO.setDescription(StringCreator.createLongString(2000));
        when(bindingResult.hasErrors()).thenReturn(true);
        String returnString = experimentController.editExperiment(experimentDTO, bindingResult);
        assertEquals(EXPERIMENT_EDIT, returnString);
        verify(bindingResult).addError(any());
        verify(experimentService).existsExperiment(experimentDTO.getTitle(), experimentDTO.getId());
        verify(experimentService, never()).updateExperiment(any());
    }

    @Test
    public void testEditExperimentInfoTooLong() {
        experimentDTO.setInfo(StringCreator.createLongString(60000));
        when(bindingResult.hasErrors()).thenReturn(true);
        assertEquals(EXPERIMENT_EDIT, experimentController.editExperiment(experimentDTO, bindingResult));
        verify(bindingResult).addError(any());
        verify(experimentService).existsExperiment(experimentDTO.getTitle(), experimentDTO.getId());
        verify(experimentService, never()).updateExperiment(any());
    }

    @Test
    public void testEditExperimentPostscriptTooLong() {
        experimentDTO.setPostscript(StringCreator.createLongString(1001));
        when(bindingResult.hasErrors()).thenReturn(true);
        assertEquals(EXPERIMENT_EDIT, experimentController.editExperiment(experimentDTO, bindingResult));
        verify(bindingResult).addError(any());
        verify(experimentService).existsExperiment(experimentDTO.getTitle(), experimentDTO.getId());
        verify(experimentService, never()).updateExperiment(any());
    }

    @Test
    public void testEditExperimentCourseExperimentNotExistent() {
        experimentDTO.setCourse(ID);
        assertEquals(Constants.ERROR, experimentController.editExperiment(experimentDTO, bindingResult));
        verify(bindingResult, never()).addError(any());
        verify(courseService).existsActiveCourse(ID);
        verify(experimentService, never()).existsExperiment(anyString(), anyInt());
        verify(experimentService, never()).updateExperiment(any());
    }

    @Test
    public void testDeleteExperiment() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(userService.matchesPassword(PASSWORD, PASSWORD)).thenReturn(true);
        assertEquals(SUCCESS, experimentController.deleteExperiment(passwordDTO, ID));
        verify(authentication, times(2)).getName();
        verify(userService).getUser(USERNAME);
        verify(userService).matchesPassword(PASSWORD, PASSWORD);
        verify(experimentService).deleteExperiment(ID);
    }

    @Test
    public void testDeleteExperimentPasswordNotMatching() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        assertEquals(INVALID + ID, experimentController.deleteExperiment(passwordDTO, ID));
        verify(authentication, times(2)).getName();
        verify(userService).getUser(USERNAME);
        verify(userService).matchesPassword(PASSWORD, PASSWORD);
        verify(experimentService, never()).deleteExperiment(anyInt());
    }

    @Test
    public void testDeleteExperimentPasswordTooLong() {
        passwordDTO.setPassword(LONG_PASSWORD);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        assertEquals(INVALID + ID, experimentController.deleteExperiment(passwordDTO, ID));
        verify(authentication, times(2)).getName();
        verify(userService).getUser(USERNAME);
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(experimentService, never()).deleteExperiment(anyInt());
    }

    @Test
    public void testDeleteExperimentNotFound() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userService.getUser(USERNAME)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, experimentController.deleteExperiment(passwordDTO, ID));
        verify(authentication, times(2)).getName();
        verify(userService).getUser(USERNAME);
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(experimentService, never()).deleteExperiment(anyInt());
    }

    @Test
    public void testDeleteExperimentAuthenticationNameNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        assertEquals(ERROR, experimentController.deleteExperiment(passwordDTO, ID));
        verify(authentication).getName();
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(experimentService, never()).deleteExperiment(anyInt());
    }

    @Test
    public void testDeleteExperimentPasswordNull() {
        passwordDTO.setPassword(null);
        assertEquals(ERROR, experimentController.deleteExperiment(passwordDTO, INVALID_ID));
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(experimentService, never()).deleteExperiment(anyInt());
    }

    @Test
    public void testChangeExperimentStatusOpen() {
        setMailServer(true);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(experimentService.changeExperimentStatus(true, ID)).thenReturn(experimentDTO);
        when(userService.reactivateUserAccounts(experimentDTO.getId())).thenReturn(userDTOS);
        when(mailService.sendEmail(anyString(), anyString(), any(), anyString())).thenReturn(true).thenReturn(false);
        when(pageService.getParticipantPage(anyInt(), anyInt())).thenReturn(participants);
        assertEquals(EXPERIMENT, experimentController.changeExperimentStatus("open", ID, model));
        verify(experimentService).getExperiment(ID);
        verify(courseService, never()).isActiveCourse(anyInt());
        verify(experimentService).changeExperimentStatus(true, ID);
        verify(userService).reactivateUserAccounts(ID);
        verify(mailService, times(2)).sendEmail(anyString(), anyString(), any(), anyString());
        verify(pageService).getLastParticipantPage(ID);
        verify(pageService).getParticipantPage(anyInt(), anyInt());
        verify(model).addAttribute(EXPERIMENT_DTO, experimentDTO);
        verify(model).addAttribute(PARTICIPANTS, participants);
    }

    @Test
    public void testChangeExperimentStatusOpenNoMailServer() {
        setMailServer(false);
        experimentDTO.setCourseExperiment(true);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(courseService.isActiveCourse(ID)).thenReturn(true);
        when(experimentService.changeExperimentStatus(true, ID)).thenReturn(experimentDTO);
        when(userService.reactivateUserAccounts(experimentDTO.getId())).thenReturn(userDTOS);
        assertEquals(REDIRECT_SECRET_LIST + ID, experimentController.changeExperimentStatus("open",
                ID, model));
        verify(experimentService).getExperiment(ID);
        verify(courseService).isActiveCourse(ID);
        verify(experimentService).changeExperimentStatus(true, ID);
        verify(userService).reactivateUserAccounts(ID);
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(pageService, never()).getLastParticipantPage(anyInt());
        verify(pageService, never()).getParticipantPage(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testChangeExperimentStatusClose() {
        when(experimentService.changeExperimentStatus(false, ID)).thenReturn(experimentDTO);
        when(pageService.getParticipantPage(anyInt(), anyInt())).thenReturn(participants);
        assertEquals(EXPERIMENT, experimentController.changeExperimentStatus("close", ID, model));
        verify(experimentService).changeExperimentStatus(false, ID);
        verify(participantService).deactivateParticipantAccounts(ID);
        verify(pageService).getLastParticipantPage(ID);
        verify(pageService).getParticipantPage(anyInt(), anyInt());
        verify(model).addAttribute(EXPERIMENT_DTO, experimentDTO);
        verify(model).addAttribute(PARTICIPANTS, participants);
    }

    @Test
    public void testChangeExperimentStatusCloseInfoNull() {
        experimentDTO.setInfo(null);
        when(experimentService.changeExperimentStatus(false, ID)).thenReturn(experimentDTO);
        when(pageService.getParticipantPage(anyInt(), anyInt())).thenReturn(participants);
        assertEquals(EXPERIMENT, experimentController.changeExperimentStatus("close", ID, model));
        verify(experimentService).changeExperimentStatus(false, ID);
        verify(participantService).deactivateParticipantAccounts(ID);
        verify(pageService).getLastParticipantPage(ID);
        verify(pageService).getParticipantPage(anyInt(), anyInt());
        verify(model).addAttribute(EXPERIMENT_DTO, experimentDTO);
        verify(model).addAttribute(PARTICIPANTS, participants);
    }

    @Test
    public void testChangeExperimentStatusOpenInactiveCourse() {
        experimentDTO.setCourseExperiment(true);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(pageService.getParticipantPage(anyInt(), anyInt())).thenReturn(participants);
        assertEquals(EXPERIMENT, experimentController.changeExperimentStatus("open", ID, model));
        verify(experimentService).getExperiment(ID);
        verify(courseService).isActiveCourse(ID);
        verify(experimentService, never()).changeExperimentStatus(anyBoolean(), anyInt());
        verify(userService, never()).reactivateUserAccounts(anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(pageService).getLastParticipantPage(ID);
        verify(pageService).getParticipantPage(anyInt(), anyInt());
        verify(model).addAttribute(EXPERIMENT_DTO, experimentDTO);
        verify(model).addAttribute(PARTICIPANTS, participants);
    }

    @Test
    public void testChangeExperimentStatusInvalid() {
        assertEquals(ERROR, experimentController.changeExperimentStatus("blabla", ID, model));
        verify(experimentService, never()).changeExperimentStatus(anyBoolean(), anyInt());
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testChangeExperimentStatusOpenNotFound() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(experimentService.changeExperimentStatus(true, ID)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, experimentController.changeExperimentStatus("open", ID, model));
        verify(experimentService).changeExperimentStatus(true, ID);
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testChangeExperimentStatusStatusNull() {
        assertEquals(ERROR, experimentController.changeExperimentStatus(null, ID, model));
        verify(experimentService, never()).changeExperimentStatus(anyBoolean(), anyInt());
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testAddParticipants() {
        setMailServer(true);
        experimentDTO.setActive(true);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT1)).thenReturn(participant1);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT2)).thenReturn(participant2);
        when(userService.updateUser(participant1)).thenReturn(participant1);
        when(userService.updateUser(participant2)).thenReturn(participant2);
        when(mailService.sendEmail(anyString(), anyString(), any(), anyString())).thenReturn(true);
        assertEquals(REDIRECT_EXPERIMENT + ID, experimentController.addParticipants(PARTICIPANT_LIST, ID, model));
        verify(participantService).addParticipants(List.of(participant1.getId(), participant2.getId()), ID);
        verify(mailService, times(2)).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, never()).addAttribute(any(), any());
    }

    @Test
    public void testAddParticipantsSingleParticipant() {
        setMailServer(true);
        experimentDTO.setActive(true);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT1)).thenReturn(participant1);
        when(userService.updateUser(participant1)).thenReturn(participant1);
        when(mailService.sendEmail(anyString(), anyString(), any(), anyString())).thenReturn(true);
        assertEquals(REDIRECT_EXPERIMENT + ID,
            experimentController.addParticipants(List.of(PARTICIPANT1), ID, model));
        verify(participantService).addParticipants(List.of(participant1.getId()), ID);
        verify(mailService).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, never()).addAttribute(any(), any());
    }

    @Test
    public void testAddParticipantsCourseExperiment() {
        setMailServer(true);
        experimentDTO.setActive(true);
        experimentDTO.setCourseExperiment(true);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT1)).thenReturn(participant1);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT2)).thenReturn(participant2);
        when(userService.updateUser(participant1)).thenReturn(participant1);
        when(userService.updateUser(participant2)).thenReturn(participant2);
        when(courseService.existsCourseParticipant(ID, participant1.getId())).thenReturn(true);
        when(courseService.existsCourseParticipant(ID, participant2.getId())).thenReturn(true);
        when(mailService.sendEmail(anyString(), anyString(), any(), anyString())).thenReturn(true);
        assertEquals(REDIRECT_EXPERIMENT + ID, experimentController.addParticipants(PARTICIPANT_LIST, ID, model));
        verify(userService).updateUser(participant1);
        verify(userService).updateUser(participant2);
        verify(participantService).addParticipants(List.of(participant1.getId(), participant2.getId()), ID);
        verify(mailService, times(2)).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, never()).addAttribute(any(), any());
    }

    @Test
    public void testAddParticipantsNoMailServer() {
        setMailServer(false);
        experimentDTO.setActive(true);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT1)).thenReturn(participant1);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT2)).thenReturn(participant2);
        when(userService.updateUser(participant1)).thenReturn(participant1);
        when(userService.updateUser(participant2)).thenReturn(participant2);
        assertEquals(REDIRECT_EXPERIMENT + ID,
            experimentController.addParticipants(PARTICIPANT_LIST, ID, model));
        verify(participantService).addParticipants(List.of(participant1.getId(), participant2.getId()), ID);
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, never()).addAttribute(any(), any());
    }

    @Test
    public void testAddParticipantsSingleParticipantNoMailServer() {
        setMailServer(false);
        experimentDTO.setActive(true);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT1)).thenReturn(participant1);
        when(userService.updateUser(participant1)).thenReturn(participant1);
        assertEquals(REDIRECT_SECRET + participant1.getId() + EXPERIMENT_PARAM + ID,
                experimentController.addParticipants(List.of(PARTICIPANT1), ID, model));
        verify(participantService).addParticipants(List.of(participant1.getId()), ID);
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, never()).addAttribute(any(), any());
    }

    @Test
    public void testAddParticipantsEmailNotSent() {
        setMailServer(true);
        experimentDTO.setActive(true);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT1)).thenReturn(participant1);
        when(userService.updateUser(participant1)).thenReturn(participant1);
        assertEquals(ERROR, experimentController.addParticipants(List.of(PARTICIPANT1), ID, model));
        verify(participantService).addParticipants(List.of(participant1.getId()), ID);
        verify(mailService).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, never()).addAttribute(any(), any());
    }

    @Test
    public void testSearchForUserEmailNull() {
        setMailServer(true);
        participant1.setEmail(null);
        experimentDTO.setActive(true);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT1)).thenReturn(participant1);
        when(userService.updateUser(participant1)).thenReturn(participant1);
        assertEquals(ERROR, experimentController.addParticipants(List.of(PARTICIPANT1), ID, model));
        verify(participantService).addParticipants(List.of(participant1.getId()), ID);
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, never()).addAttribute(any(), any());
    }

    @Test
    public void testAddParticipantsSaveParticipantNotFound() {
        experimentDTO.setActive(true);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT1)).thenReturn(participant1);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT2)).thenReturn(participant2);
        when(userService.updateUser(participant1)).thenReturn(participant1);
        when(userService.updateUser(participant2)).thenReturn(participant2);
        doThrow(NotFoundException.class).when(participantService).
                addParticipants(List.of(participant1.getId(), participant2.getId()), ID);
        assertEquals(ERROR, experimentController.addParticipants(PARTICIPANT_LIST, ID, model));
        verify(participantService).addParticipants(List.of(participant1.getId(), participant2.getId()), ID);
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, never()).addAttribute(any(), any());
    }

    @Test
    public void testAddParticipantsNoCourseParticipant() {
        experimentDTO.setActive(true);
        experimentDTO.setCourseExperiment(true);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT1)).thenReturn(participant1);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT2)).thenReturn(participant2);
        when(model.getAttribute("error")).thenReturn("error");
        assertEquals(EXPERIMENT, experimentController.addParticipants(PARTICIPANT_LIST, ID, model));
        verify(userService, never()).updateUser(participant1);
        verify(participantService, never()).addParticipants(anyList(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    @Test
    public void testAddParticipantsExperimentInactive() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT1)).thenReturn(participant1);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT2)).thenReturn(participant2);
        when(model.getAttribute("error")).thenReturn("error");
        assertEquals(EXPERIMENT, experimentController.addParticipants(PARTICIPANT_LIST, ID, model));
        verify(userService, never()).updateUser(participant1);
        verify(participantService, never()).addParticipants(anyList(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    @Test
    public void testAddParticipantsSecretNotNull() {
        participant1.setSecret("secret");
        participant2.setSecret("secret");
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT1)).thenReturn(participant1);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT2)).thenReturn(participant2);
        when(model.getAttribute("error")).thenReturn("error");
        assertEquals(EXPERIMENT, experimentController.addParticipants(PARTICIPANT_LIST, ID, model));
        verify(userService, never()).updateUser(participant1);
        verify(userService, never()).updateUser(participant2);
        verify(participantService, never()).addParticipants(anyList(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    @Test
    public void testAddParticipantsParticipantsExist() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT1)).thenReturn(participant1);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT2)).thenReturn(participant2);
        when(userService.existsParticipant(participant1.getId(), ID)).thenReturn(true);
        when(userService.existsParticipant(participant2.getId(), ID)).thenReturn(true);
        when(model.getAttribute("error")).thenReturn("error");
        assertEquals(EXPERIMENT, experimentController.addParticipants(PARTICIPANT_LIST, ID, model));
        verify(userService, never()).updateUser(participant1);
        verify(userService, never()).updateUser(participant2);
        verify(participantService, never()).addParticipants(anyList(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    @Test
    public void testAddParticipantsAdmin() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(USERNAME)).thenReturn(userDTO);
        when(model.getAttribute("error")).thenReturn("error");
        assertEquals(EXPERIMENT, experimentController.addParticipants(List.of(USERNAME), ID, model));
        verify(userService, never()).updateUser(any());
        verify(participantService, never()).addParticipants(anyList(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    @Test
    public void testAddParticipantsNull() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(model.getAttribute("error")).thenReturn("error");
        assertEquals(EXPERIMENT, experimentController.addParticipants(PARTICIPANT_LIST, ID, model));
        verify(userService, never()).updateUser(any());
        verify(participantService, never()).addParticipants(anyList(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    @Test
    public void testAddParticipantsQueryInvalid() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        assertEquals(EXPERIMENT, experimentController.addParticipants(List.of(BLANK), ID, model));
        verify(userService, never()).updateUser(any());
        verify(participantService, never()).addParticipants(anyList(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    @Test
    public void testAddParticipantsExperimentNotFound() {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, experimentController.addParticipants(PARTICIPANT_LIST, ID, model));
        verify(userService, never()).getUserByUsernameOrEmail(anyString());
        verify(userService, never()).updateUser(any());
        verify(participantService, never()).saveParticipant(anyInt(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetPage() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(pageService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        when(pageService.getParticipantPage(anyInt(), anyInt())).thenReturn(participants);
        assertEquals(EXPERIMENT, experimentController.getPage(ID, PAGE, model));
        verify(experimentService).getExperiment(ID);
        verify(pageService).getLastParticipantPage(ID);
        verify(pageService).getParticipantPage(anyInt(), anyInt());
        verify(model, times(5)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetNextPageNotFound() {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, experimentController.getPage(ID, PAGE, model));
        verify(experimentService).getExperiment(ID);
        verify(pageService, never()).getLastParticipantPage(ID);
        verify(pageService, never()).getParticipantPage(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDownloadCSVFile() throws IOException {
        when(experimentDataService.getEventData(ID)).thenReturn(new ArrayList<>());
        when(httpServletResponse.getWriter()).thenReturn(new PrintWriter(new ByteArrayOutputStream()));
        assertDoesNotThrow(
                () -> experimentController.downloadCSVFile(ID, httpServletResponse)
        );
        verify(experimentDataService).getEventData(ID);
        verify(httpServletResponse).getWriter();
    }

    @Test
    public void testDownloadCSVFileIO() throws IOException {
        when(httpServletResponse.getWriter()).thenThrow(IOException.class);
        assertThrows(RuntimeException.class,
                () -> experimentController.downloadCSVFile(ID, httpServletResponse)
        );
        verify(experimentDataService, never()).getEventData(anyInt());
        verify(httpServletResponse).getWriter();
    }

    @Test
    public void testAddParticipantsFromCSV() throws IOException {
        MockMultipartFile file = new MockMultipartFile(FILENAME_CSV, FILENAME_CSV, FILETYPE_CSV,
                new ClassPathResource(FILENAME_CSV).getInputStream());
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.existsUser(anyString())).thenReturn(true);
        when(pageService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        when(pageService.getParticipantPage(anyInt(), anyInt())).thenReturn(participants);
        assertEquals(EXPERIMENT, experimentController.addParticipantsFromCSV(file, ID, model));
    }

    @Test
    public void testAddParticipantsFromCSVCourseExperiment() throws IOException {
        experimentDTO.setCourseExperiment(true);
        MockMultipartFile file = new MockMultipartFile(FILENAME_CSV, FILENAME_CSV, FILETYPE_CSV,
                new ClassPathResource(FILENAME_CSV).getInputStream());
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.existsUser(anyString())).thenReturn(true);
        when(pageService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        when(pageService.getParticipantPage(anyInt(), anyInt())).thenReturn(participants);
        assertEquals(EXPERIMENT, experimentController.addParticipantsFromCSV(file, ID, model));
    }

    @Test
    public void testAddParticipantsFromCSVUserAdmin() throws IOException {
        experimentDTO.setCourseExperiment(true);
        MockMultipartFile file = new MockMultipartFile(FILENAME_CSV, FILENAME_CSV, FILETYPE_CSV,
                new ClassPathResource(FILENAME_CSV).getInputStream());
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.existsUser(anyString())).thenReturn(true);
        when(userService.isAdmin(PARTICIPANTS)).thenReturn(true);
        when(pageService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        when(pageService.getParticipantPage(anyInt(), anyInt())).thenReturn(participants);
        assertEquals(EXPERIMENT, experimentController.addParticipantsFromCSV(file, ID, model));
    }

    @Test
    public void testAddParticipantsFromCSVUnknownUsername() throws IOException {
        MockMultipartFile file = new MockMultipartFile(FILENAME_CSV, FILENAME_CSV, FILETYPE_CSV,
                new ClassPathResource(FILENAME_CSV).getInputStream());
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(pageService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        when(pageService.getParticipantPage(anyInt(), anyInt())).thenReturn(participants);
        assertEquals(EXPERIMENT, experimentController.addParticipantsFromCSV(file, ID, model));
    }

    @Test
    public void testAddParticipantsFromCSVInvalidFile() throws IOException {
        MockMultipartFile file = new MockMultipartFile(FILENAME_CSV, FILENAME_CSV, FILETYPE_SB3,
                new ClassPathResource(FILENAME_CSV).getInputStream());
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(pageService.getLastParticipantPage(ID)).thenReturn(LAST_PAGE);
        when(pageService.getParticipantPage(anyInt(), anyInt())).thenReturn(participants);
        assertEquals(EXPERIMENT, experimentController.addParticipantsFromCSV(file, ID, model));
    }

    @Test
    public void testAddParticipantsFromCSVNoFile() {
        assertEquals(ERROR, experimentController.addParticipantsFromCSV(null, ID, model));
        verify(experimentService, never()).getExperiment(anyInt());
        verify(userService, never()).existsUser(anyString());
        verify(userService, never()).isAdmin(anyString());
        verify(courseService, never()).saveCourseParticipants(anyInt(), any(), anyBoolean());
        verify(courseService, never()).getCourseIdForExperiment(anyInt());
        verify(participantService, never()).saveParticipantsFromCSV(anyInt(), any());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDownloadLitterBoxAnalysis() throws IOException {
        when(experimentDataService.getLitterBoxAnalysisResults(ID)).thenReturn(new ArrayList<>());
        when(httpServletResponse.getWriter()).thenReturn(new PrintWriter(new ByteArrayOutputStream()));
        assertDoesNotThrow(
                () -> experimentController.downloadLitterBoxAnalysis(ID, httpServletResponse)
        );
        verify(experimentDataService).getLitterBoxAnalysisResults(ID);
        verify(httpServletResponse).getWriter();
    }

    @Test
    public void testDownloadLitterBoxAnalysisIO() throws IOException {
        when(httpServletResponse.getWriter()).thenThrow(IOException.class);
        assertThrows(RuntimeException.class,
                () -> experimentController.downloadLitterBoxAnalysis(ID, httpServletResponse)
        );
        verify(experimentDataService, never()).getLitterBoxAnalysisResults(anyInt());
        verify(httpServletResponse).getWriter();
    }

    @Test
    public void testUploadProjectFile() throws IOException {
        when(file.getContentType()).thenReturn(FILETYPE_SB3);
        when(file.getOriginalFilename()).thenReturn(FILENAME_SB3);
        when(file.getBytes()).thenReturn(CONTENT);
        assertEquals(REDIRECT_EXPERIMENT + ID, experimentController.uploadProjectFile(file, ID, model));
        verify(experimentService).uploadSb3Project(ID, CONTENT);
        verify(file).isEmpty();
        verify(file, times(2)).getOriginalFilename();
        verify(file, times(2)).getContentType();
        verify(file).getBytes();
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testUploadProjectFileIO() throws IOException {
        when(file.getContentType()).thenReturn(FILETYPE_SB3);
        when(file.getOriginalFilename()).thenReturn(FILENAME_SB3);
        when(file.getBytes()).thenThrow(IOException.class);
        assertEquals(ERROR, experimentController.uploadProjectFile(file, ID, model));
        verify(experimentService, never()).uploadSb3Project(anyInt(), any());
        verify(file).isEmpty();
        verify(file, times(2)).getOriginalFilename();
        verify(file, times(2)).getContentType();
        verify(file).getBytes();
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testUploadProjectFileNotFound() throws IOException {
        when(file.getContentType()).thenReturn(FILETYPE_SB3);
        when(file.getOriginalFilename()).thenReturn(FILENAME_SB3);
        when(file.getBytes()).thenReturn(CONTENT);
        doThrow(NotFoundException.class).when(experimentService).uploadSb3Project(ID, CONTENT);
        assertEquals(ERROR, experimentController.uploadProjectFile(file, ID, model));
        verify(experimentService).uploadSb3Project(ID, CONTENT);
        verify(file).isEmpty();
        verify(file, times(2)).getOriginalFilename();
        verify(file, times(2)).getContentType();
        verify(file).getBytes();
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testUploadProjectFileFilenameInvalid() throws IOException {
        when(file.getContentType()).thenReturn(FILETYPE_SB3);
        when(file.getOriginalFilename()).thenReturn("name");
        when(model.getAttribute(ERROR_ATTRIBUTE)).thenReturn(ERROR_ATTRIBUTE);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        assertEquals(EXPERIMENT, experimentController.uploadProjectFile(file, ID, model));
        verify(experimentService, never()).uploadSb3Project(anyInt(), any());
        verify(file).isEmpty();
        verify(file, times(2)).getOriginalFilename();
        verify(file, times(2)).getContentType();
        verify(file, never()).getBytes();
        verify(model, times(6)).addAttribute(anyString(), any());
    }

    @Test
    public void testUploadProjectFileFilenameNull() throws IOException {
        when(file.getContentType()).thenReturn(FILETYPE_SB3);
        when(model.getAttribute(ERROR_ATTRIBUTE)).thenReturn(ERROR_ATTRIBUTE);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        assertEquals(EXPERIMENT, experimentController.uploadProjectFile(file, ID, model));
        verify(experimentService, never()).uploadSb3Project(anyInt(), any());
        verify(file).isEmpty();
        verify(file).getOriginalFilename();
        verify(file, times(2)).getContentType();
        verify(file, never()).getBytes();
        verify(model, times(6)).addAttribute(anyString(), any());
    }

    @Test
    public void testUploadProjectFileContentTypeInvalid() throws IOException {
        when(file.getContentType()).thenReturn("type");
        when(model.getAttribute(ERROR_ATTRIBUTE)).thenReturn(ERROR_ATTRIBUTE);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        assertEquals(EXPERIMENT, experimentController.uploadProjectFile(file, ID, model));
        verify(experimentService, never()).uploadSb3Project(anyInt(), any());
        verify(file).isEmpty();
        verify(file, never()).getOriginalFilename();
        verify(file, times(2)).getContentType();
        verify(file, never()).getBytes();
        verify(model, times(6)).addAttribute(anyString(), any());
    }

    @Test
    public void testUploadProjectFileContentTypeNull() throws IOException {
        when(model.getAttribute(ERROR_ATTRIBUTE)).thenReturn(ERROR_ATTRIBUTE);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        assertEquals(EXPERIMENT, experimentController.uploadProjectFile(file, ID, model));
        verify(experimentService, never()).uploadSb3Project(anyInt(), any());
        verify(file).isEmpty();
        verify(file, never()).getOriginalFilename();
        verify(file).getContentType();
        verify(file, never()).getBytes();
        verify(model, times(6)).addAttribute(anyString(), any());
    }

    @Test
    public void testUploadProjectFileEmpty() throws IOException {
        when(file.isEmpty()).thenReturn(true);
        when(model.getAttribute(ERROR_ATTRIBUTE)).thenReturn(ERROR_ATTRIBUTE);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        assertEquals(EXPERIMENT, experimentController.uploadProjectFile(file, ID, model));
        verify(experimentService, never()).uploadSb3Project(anyInt(), any());
        verify(file).isEmpty();
        verify(file, never()).getOriginalFilename();
        verify(file, never()).getContentType();
        verify(file, never()).getBytes();
        verify(model, times(6)).addAttribute(anyString(), any());
    }

    @Test
    public void testUploadProjectFileFileNull() throws IOException {
        assertEquals(ERROR, experimentController.uploadProjectFile(null, ID, model));
        verify(experimentService, never()).uploadSb3Project(anyInt(), any());
        verify(file, never()).isEmpty();
        verify(file, never()).getOriginalFilename();
        verify(file, never()).getContentType();
        verify(file, never()).getBytes();
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteProjectFile() {
        assertEquals(REDIRECT_EXPERIMENT + ID, experimentController.deleteProjectFile(ID));
        verify(experimentService).deleteSb3Project(ID);
    }

    @Test
    public void testDeleteProjectFileNotFound() {
        doThrow(NotFoundException.class).when(experimentService).deleteSb3Project(ID);
        assertEquals(ERROR, experimentController.deleteProjectFile(ID));
        verify(experimentService).deleteSb3Project(ID);
    }

    private List<Participant> getParticipants(int number) {
        List<Participant> participants = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            participants.add(new Participant(new User(), new Experiment(), LocalDateTime.now(), null));
        }
        return participants;
    }
}
