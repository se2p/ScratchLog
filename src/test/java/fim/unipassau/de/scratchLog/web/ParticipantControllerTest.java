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
package fim.unipassau.de.scratchLog.web;

import fim.unipassau.de.scratchLog.MailServerSetter;
import fim.unipassau.de.scratchLog.StringCreator;
import fim.unipassau.de.scratchLog.application.exception.NotFoundException;
import fim.unipassau.de.scratchLog.application.service.ExperimentService;
import fim.unipassau.de.scratchLog.application.service.MailService;
import fim.unipassau.de.scratchLog.application.service.PageService;
import fim.unipassau.de.scratchLog.application.service.ParticipantService;
import fim.unipassau.de.scratchLog.application.service.UserService;
import fim.unipassau.de.scratchLog.util.Constants;
import fim.unipassau.de.scratchLog.util.enums.Language;
import fim.unipassau.de.scratchLog.util.enums.Role;
import fim.unipassau.de.scratchLog.web.controller.ParticipantController;
import fim.unipassau.de.scratchLog.web.dto.ExperimentDTO;
import fim.unipassau.de.scratchLog.web.dto.ParticipantDTO;
import fim.unipassau.de.scratchLog.web.dto.UserDTO;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.time.LocalDateTime;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
public class ParticipantControllerTest {

    @InjectMocks
    private ParticipantController participantController;

    @Mock
    private UserService userService;

    @Mock
    private ExperimentService experimentService;

    @Mock
    private ParticipantService participantService;

    @Mock
    private PageService pageService;

    @Mock
    private MailService mailService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private ResourceBundle resourceBundle;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private MockedStatic<SecurityContextHolder> securityContextHolder;
    private static final String GUI_URL = "scratch";
    private static final String ERROR = "redirect:/error";
    private static final String PARTICIPANT = "participant";
    private static final String EXPERIMENT = "experiment";
    private static final String REDIRECT_EXPERIMENT = "redirect:/experiment?id=";
    private static final String REDIRECT_GUI = "redirect:" + GUI_URL + "?uid=";
    private static final String REDIRECT_FINISH = "redirect:/finish?user=";
    private static final String REDIRECT_SECRET = "redirect:/secret?user=";
    private static final String EXP_ID = "&expid=";
    private static final String SECRET_PARAM = "&secret=";
    private static final String RESTART = "&restart=true";
    private static final String EXPERIMENT_PARAM = "&experiment=";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String EMAIL = "participant@participant.de";
    private static final String BLANK = "   ";
    private static final String ID_STRING = "1";
    private static final String SECRET = "secret";
    private static final String INFO = "info";
    private static final String POSTSCRIPT = "postscript";
    private static final String ERROR_ATTRIBUTE = "error";
    private static final String LONG_INPUT = StringCreator.createLongString(101);
    private static final int ID = 1;
    private final UserDTO newUser = new UserDTO(PARTICIPANT, EMAIL, Role.PARTICIPANT, Language.ENGLISH, "password",
            "secret");
    private final UserDTO userDTO = new UserDTO(PARTICIPANT, EMAIL, Role.PARTICIPANT, Language.ENGLISH, "password",
            "secret");
    private final ExperimentDTO experimentDTO = new ExperimentDTO(ID, "title", "description", INFO, POSTSCRIPT, true,
            false, GUI_URL);
    private final ParticipantDTO participantDTO = new ParticipantDTO(ID, ID);

    @BeforeEach
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        userDTO.setId(ID);
        userDTO.setUsername(PARTICIPANT);
        userDTO.setEmail(EMAIL);
        userDTO.setRole(Role.PARTICIPANT);
        userDTO.setActive(true);
        userDTO.setSecret("secret");
        experimentDTO.setActive(true);
        experimentDTO.setCourseExperiment(false);
        experimentDTO.setInfo(INFO);
        participantDTO.setStart(null);
        participantDTO.setEnd(null);
        securityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
    }

    @AfterEach
    public void cleanup() {
        securityContextHolder.close();
    }

    @Test
    public void testGetParticipantForm() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.findLastId()).thenReturn(ID);
        assertEquals(PARTICIPANT, participantController.getParticipantForm(ID_STRING, model));
        verify(experimentService).getExperiment(ID);
        verify(userService).findLastId();
        verify(model, times(2)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetParticipantFormNotFound() {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, participantController.getParticipantForm(ID_STRING, model));
        verify(experimentService).getExperiment(ID);
        verify(userService, never()).findLastId();
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetParticipantFormExperimentInactive() {
        experimentDTO.setActive(false);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        assertEquals(ERROR, participantController.getParticipantForm(ID_STRING, model));
        verify(experimentService).getExperiment(ID);
        verify(userService, never()).findLastId();
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetParticipantFormCourseExperiment() {
        experimentDTO.setCourseExperiment(true);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        assertEquals(ERROR, participantController.getParticipantForm(ID_STRING, model));
        verify(experimentService).getExperiment(ID);
        verify(userService, never()).findLastId();
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetParticipantFormExperimentIdInvalid() {
        assertEquals(ERROR, participantController.getParticipantForm("0", model));
        verify(experimentService, never()).getExperiment(ID);
        verify(userService, never()).findLastId();
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetParticipantFormExperimentIdNull() {
        assertEquals(ERROR, participantController.getParticipantForm(null, model));
        verify(experimentService, never()).getExperiment(ID);
        verify(userService, never()).findLastId();
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetParticipantFormExperimentIdBlank() {
        assertEquals(ERROR, participantController.getParticipantForm(BLANK, model));
        verify(experimentService, never()).getExperiment(ID);
        verify(userService, never()).findLastId();
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testAddParticipant() {
        MailServerSetter.setMailServer(true);
        when(userService.saveUser(newUser)).thenReturn(userDTO);
        when(mailService.sendEmail(anyString(), any(), any(), anyString())).thenReturn(true);
        assertEquals(REDIRECT_EXPERIMENT + ID, participantController.addParticipant(ID_STRING, newUser, model,
                bindingResult));
        verify(userService).saveUser(newUser);
        verify(participantService).saveParticipant(userDTO.getId(), ID);
        verify(mailService).sendEmail(anyString(), any(), any(), anyString());
        verify(bindingResult, never()).addError(any());
    }

    @Test
    public void testAddParticipantNoMailServer() {
        MailServerSetter.setMailServer(false);
        when(userService.saveUser(newUser)).thenReturn(userDTO);
        assertEquals(REDIRECT_SECRET + userDTO.getId() + EXPERIMENT_PARAM + ID,
                participantController.addParticipant(ID_STRING, newUser, model, bindingResult));
        verify(userService).saveUser(newUser);
        verify(participantService).saveParticipant(userDTO.getId(), ID);
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
        verify(bindingResult, never()).addError(any());
    }

    @Test
    public void testAddParticipantMessagingException() {
        MailServerSetter.setMailServer(true);
        when(userService.saveUser(newUser)).thenReturn(userDTO);
        assertEquals(ERROR, participantController.addParticipant(ID_STRING, newUser, model, bindingResult));
        verify(userService).saveUser(newUser);
        verify(participantService).saveParticipant(userDTO.getId(), ID);
        verify(mailService).sendEmail(anyString(), any(), any(), anyString());
        verify(bindingResult, never()).addError(any());
    }

    @Test
    public void testAddParticipantNotFound() {
        when(userService.saveUser(newUser)).thenReturn(userDTO);
        doThrow(NotFoundException.class).when(participantService).saveParticipant(userDTO.getId(), ID);
        assertEquals(ERROR, participantController.addParticipant(ID_STRING, newUser, model, bindingResult));
        verify(userService).saveUser(newUser);
        verify(participantService).saveParticipant(userDTO.getId(), ID);
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
        verify(bindingResult, never()).addError(any());
    }

    @Test
    public void testAddParticipantExistsEmail() {
        when(userService.existsEmail(newUser.getEmail())).thenReturn(true);
        when(bindingResult.hasErrors()).thenReturn(true);
        assertEquals(PARTICIPANT, participantController.addParticipant(ID_STRING, newUser, model, bindingResult));
        verify(userService, never()).saveUser(any());
        verify(participantService, never()).saveParticipant(anyInt(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
        verify(bindingResult).addError(any());
    }

    @Test
    public void testAddParticipantEmailInvalid() {
        newUser.setEmail(PARTICIPANT);
        when(bindingResult.hasErrors()).thenReturn(true);
        assertEquals(PARTICIPANT, participantController.addParticipant(ID_STRING, newUser, model, bindingResult));
        verify(userService, never()).saveUser(any());
        verify(participantService, never()).saveParticipant(anyInt(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
        verify(bindingResult).addError(any());
    }

    @Test
    public void testAddParticipantUsernameExists() {
        when(userService.existsUser(PARTICIPANT)).thenReturn(true);
        when(bindingResult.hasErrors()).thenReturn(true);
        assertEquals(PARTICIPANT, participantController.addParticipant(ID_STRING, newUser, model, bindingResult));
        verify(userService, never()).saveUser(any());
        verify(participantService, never()).saveParticipant(anyInt(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
        verify(bindingResult).addError(any());
    }

    @Test
    public void testAddParticipantUsernameInvalid() {
        newUser.setUsername(BLANK);
        when(bindingResult.hasErrors()).thenReturn(true);
        assertEquals(PARTICIPANT, participantController.addParticipant(ID_STRING, newUser, model, bindingResult));
        verify(userService, never()).saveUser(any());
        verify(participantService, never()).saveParticipant(anyInt(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
        verify(bindingResult).addError(any());
    }

    @Test
    public void testAddParticipantExperimentIdInvalid() {
        assertEquals(ERROR, participantController.addParticipant("0", newUser, model, bindingResult));
        verify(userService, never()).saveUser(userDTO);
        verify(participantService, never()).saveParticipant(userDTO.getId(), ID);
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
        verify(bindingResult, never()).addError(any());
    }

    @Test
    public void testAddParticipantUserIdNotNull() {
        assertEquals(ERROR, participantController.addParticipant(ID_STRING, userDTO, model, bindingResult));
        verify(userService, never()).saveUser(userDTO);
        verify(participantService, never()).saveParticipant(userDTO.getId(), ID);
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
        verify(bindingResult, never()).addError(any());
    }

    @Test
    public void testAddParticipantUsernameNull() {
        userDTO.setUsername(null);
        assertEquals(ERROR, participantController.addParticipant(ID_STRING, userDTO, model, bindingResult));
        verify(userService, never()).saveUser(userDTO);
        verify(participantService, never()).saveParticipant(userDTO.getId(), ID);
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
        verify(bindingResult, never()).addError(any());
    }

    @Test
    public void testAddParticipantEmailNull() {
        userDTO.setEmail(null);
        assertEquals(ERROR, participantController.addParticipant(ID_STRING, userDTO, model, bindingResult));
        verify(userService, never()).saveUser(userDTO);
        verify(participantService, never()).saveParticipant(userDTO.getId(), ID);
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
        verify(bindingResult, never()).addError(any());
    }

    @Test
    public void testAddParticipantExperimentIdNull() {
        assertEquals(ERROR, participantController.addParticipant(null, userDTO, model, bindingResult));
        verify(userService, never()).saveUser(userDTO);
        verify(participantService, never()).saveParticipant(userDTO.getId(), ID);
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
        verify(bindingResult, never()).addError(any());
    }

    @Test
    public void testDeleteParticipant() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT)).thenReturn(userDTO);
        when(userService.existsParticipant(userDTO.getId(), ID)).thenReturn(true);
        when(userService.updateUser(userDTO)).thenReturn(userDTO);
        assertEquals(REDIRECT_EXPERIMENT + ID, participantController.deleteParticipant(PARTICIPANT, ID_STRING,
                model));
        verify(userService).getUserByUsernameOrEmail(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(userService).existsParticipant(userDTO.getId(), ID);
        verify(participantService).simultaneousParticipation(ID);
        verify(userService).updateUser(userDTO);
        verify(participantService).deleteParticipant(userDTO.getId(), ID);
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteParticipantSimultaneousParticipation() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT)).thenReturn(userDTO);
        when(userService.existsParticipant(userDTO.getId(), ID)).thenReturn(true);
        when(participantService.simultaneousParticipation(ID)).thenReturn(true);
        assertEquals(REDIRECT_EXPERIMENT + ID, participantController.deleteParticipant(PARTICIPANT, ID_STRING,
                model));
        verify(userService).getUserByUsernameOrEmail(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(userService).existsParticipant(userDTO.getId(), ID);
        verify(participantService).simultaneousParticipation(ID);
        verify(userService, never()).updateUser(any());
        verify(participantService).deleteParticipant(userDTO.getId(), ID);
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteParticipantUpdateUserNotFound() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT)).thenReturn(userDTO);
        when(userService.existsParticipant(userDTO.getId(), ID)).thenReturn(true);
        when(userService.updateUser(userDTO)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, participantController.deleteParticipant(PARTICIPANT, ID_STRING, model));
        verify(userService).getUserByUsernameOrEmail(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(userService).existsParticipant(userDTO.getId(), ID);
        verify(participantService).simultaneousParticipation(ID);
        verify(userService).updateUser(userDTO);
        verify(participantService, never()).deleteParticipant(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteParticipantExperimentInactiveInfoNull() {
        experimentDTO.setInfo(null);
        experimentDTO.setActive(false);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT)).thenReturn(userDTO);
        when(userService.existsParticipant(userDTO.getId(), ID)).thenReturn(true);
        when(model.getAttribute(ERROR_ATTRIBUTE)).thenReturn(ERROR_ATTRIBUTE);
        assertEquals(EXPERIMENT, participantController.deleteParticipant(PARTICIPANT, ID_STRING, model));
        verify(userService).getUserByUsernameOrEmail(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(userService).existsParticipant(userDTO.getId(), ID);
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(userService, never()).updateUser(any());
        verify(participantService, never()).deleteParticipant(anyInt(), anyInt());
        verify(model, times(5)).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteParticipantNoParticipantEntry() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT)).thenReturn(userDTO);
        when(model.getAttribute(ERROR_ATTRIBUTE)).thenReturn(ERROR_ATTRIBUTE);
        assertEquals(EXPERIMENT, participantController.deleteParticipant(PARTICIPANT, ID_STRING, model));
        verify(userService).getUserByUsernameOrEmail(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(userService).existsParticipant(userDTO.getId(), ID);
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(userService, never()).updateUser(any());
        verify(participantService, never()).deleteParticipant(anyInt(), anyInt());
        verify(model, times(5)).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteParticipantUserAdmin() {
        userDTO.setRole(Role.ADMIN);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(userService.getUserByUsernameOrEmail(PARTICIPANT)).thenReturn(userDTO);
        when(model.getAttribute(ERROR_ATTRIBUTE)).thenReturn(ERROR_ATTRIBUTE);
        assertEquals(EXPERIMENT, participantController.deleteParticipant(PARTICIPANT, ID_STRING, model));
        verify(userService).getUserByUsernameOrEmail(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(userService, never()).existsParticipant(anyInt(), anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(userService, never()).updateUser(any());
        verify(participantService, never()).deleteParticipant(anyInt(), anyInt());
        verify(model, times(5)).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteParticipantUserNull() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        assertEquals(EXPERIMENT, participantController.deleteParticipant(PARTICIPANT, ID_STRING, model));
        verify(userService).getUserByUsernameOrEmail(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(userService, never()).existsParticipant(anyInt(), anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(userService, never()).updateUser(any());
        verify(participantService, never()).deleteParticipant(anyInt(), anyInt());
        verify(model, times(5)).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteParticipantExperimentNotFound() {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, participantController.deleteParticipant(PARTICIPANT, ID_STRING, model));
        verify(userService).getUserByUsernameOrEmail(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(userService, never()).existsParticipant(anyInt(), anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(userService, never()).updateUser(any());
        verify(participantService, never()).deleteParticipant(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteParticipantExperimentIdInvalid() {
        assertEquals(ERROR, participantController.deleteParticipant(PARTICIPANT, "0", model));
        verify(userService, never()).getUserByUsernameOrEmail(anyString());
        verify(experimentService, never()).getExperiment(anyInt());
        verify(userService, never()).existsParticipant(anyInt(), anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(userService, never()).updateUser(any());
        verify(participantService, never()).deleteParticipant(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteParticipantInputTooLong() {
        assertEquals(ERROR, participantController.deleteParticipant(LONG_INPUT, ID_STRING, model));
        verify(userService, never()).getUserByUsernameOrEmail(anyString());
        verify(experimentService, never()).getExperiment(anyInt());
        verify(userService, never()).existsParticipant(anyInt(), anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(userService, never()).updateUser(any());
        verify(participantService, never()).deleteParticipant(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteParticipantInputBlank() {
        assertEquals(ERROR, participantController.deleteParticipant(BLANK, ID_STRING, model));
        verify(userService, never()).getUserByUsernameOrEmail(anyString());
        verify(experimentService, never()).getExperiment(anyInt());
        verify(userService, never()).existsParticipant(anyInt(), anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(userService, never()).updateUser(any());
        verify(participantService, never()).deleteParticipant(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteParticipantInputNull() {
        assertEquals(ERROR, participantController.deleteParticipant(null, ID_STRING, model));
        verify(userService, never()).getUserByUsernameOrEmail(anyString());
        verify(experimentService, never()).getExperiment(anyInt());
        verify(userService, never()).existsParticipant(anyInt(), anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(userService, never()).updateUser(any());
        verify(participantService, never()).deleteParticipant(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDeleteParticipantIdNull() {
        assertEquals(ERROR, participantController.deleteParticipant(PARTICIPANT, null, model));
        verify(userService, never()).getUserByUsernameOrEmail(anyString());
        verify(experimentService, never()).getExperiment(anyInt());
        verify(userService, never()).existsParticipant(anyInt(), anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(userService, never()).updateUser(any());
        verify(participantService, never()).deleteParticipant(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testStartExperiment() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(PARTICIPANT);
        when(userService.getUser(PARTICIPANT)).thenReturn(userDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        when(participantService.updateParticipant(participantDTO)).thenReturn(true);
        assertEquals(REDIRECT_GUI + ID + EXP_ID + ID + SECRET_PARAM + SECRET,
                participantController.startExperiment(ID_STRING, httpServletRequest));
        verify(httpServletRequest).isUserInRole(ROLE_ADMIN);
        verify(authentication, times(2)).getName();
        verify(userService).getUser(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService).updateParticipant(participantDTO);
    }

    @Test
    public void testStartExperimentParticipantNotUpdated() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(PARTICIPANT);
        when(userService.getUser(PARTICIPANT)).thenReturn(userDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        assertEquals(ERROR, participantController.startExperiment(ID_STRING, httpServletRequest));
        verify(httpServletRequest).isUserInRole(ROLE_ADMIN);
        verify(authentication, times(2)).getName();
        verify(userService).getUser(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService).updateParticipant(participantDTO);
    }

    @Test
    public void testStartExperimentParticipantStarted() {
        participantDTO.setStart(LocalDateTime.now());
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(PARTICIPANT);
        when(userService.getUser(PARTICIPANT)).thenReturn(userDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        assertEquals(REDIRECT_GUI + ID + EXP_ID + ID + SECRET_PARAM + SECRET + RESTART,
                participantController.startExperiment(ID_STRING, httpServletRequest));
        verify(httpServletRequest).isUserInRole(ROLE_ADMIN);
        verify(authentication, times(2)).getName();
        verify(userService).getUser(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService, never()).updateParticipant(any());
    }

    @Test
    public void testStartExperimentParticipantFinished() {
        participantDTO.setEnd(LocalDateTime.now());
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(PARTICIPANT);
        when(userService.getUser(PARTICIPANT)).thenReturn(userDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        assertEquals(ERROR, participantController.startExperiment(ID_STRING, httpServletRequest));
        verify(httpServletRequest).isUserInRole(ROLE_ADMIN);
        verify(authentication, times(2)).getName();
        verify(userService).getUser(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService, never()).updateParticipant(any());
    }

    @Test
    public void testStartExperimentParticipantInactive() {
        userDTO.setActive(false);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(PARTICIPANT);
        when(userService.getUser(PARTICIPANT)).thenReturn(userDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        assertEquals(ERROR, participantController.startExperiment(ID_STRING, httpServletRequest));
        verify(httpServletRequest).isUserInRole(ROLE_ADMIN);
        verify(authentication, times(2)).getName();
        verify(userService).getUser(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService, never()).updateParticipant(any());
    }

    @Test
    public void testStartExperimentParticipantSecretNull() {
        userDTO.setSecret(null);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(PARTICIPANT);
        when(userService.getUser(PARTICIPANT)).thenReturn(userDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        assertEquals(ERROR, participantController.startExperiment(ID_STRING, httpServletRequest));
        verify(httpServletRequest).isUserInRole(ROLE_ADMIN);
        verify(authentication, times(2)).getName();
        verify(userService).getUser(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService, never()).updateParticipant(any());
    }

    @Test
    public void testStartExperimentParticipantExperimentClosed() {
        experimentDTO.setActive(false);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(PARTICIPANT);
        when(userService.getUser(PARTICIPANT)).thenReturn(userDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        assertEquals(ERROR, participantController.startExperiment(ID_STRING, httpServletRequest));
        verify(httpServletRequest).isUserInRole(ROLE_ADMIN);
        verify(authentication, times(2)).getName();
        verify(userService).getUser(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService, never()).updateParticipant(any());
    }

    @Test
    public void testStartExperimentParticipantNotFound() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(PARTICIPANT);
        when(userService.getUser(PARTICIPANT)).thenReturn(userDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipant(ID, ID)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, participantController.startExperiment(ID_STRING, httpServletRequest));
        verify(httpServletRequest).isUserInRole(ROLE_ADMIN);
        verify(authentication, times(2)).getName();
        verify(userService).getUser(PARTICIPANT);
        verify(experimentService).getExperiment(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService, never()).updateParticipant(any());
    }

    @Test
    public void testStartExperimentParticipantAuthenticationNameNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        assertEquals(ERROR, participantController.startExperiment(ID_STRING, httpServletRequest));
        verify(httpServletRequest).isUserInRole(ROLE_ADMIN);
        verify(authentication).getName();
        verify(userService, never()).getUser(anyString());
        verify(experimentService, never()).getExperiment(anyInt());
        verify(participantService, never()).getParticipant(anyInt(), anyInt());
        verify(participantService, never()).updateParticipant(any());
    }

    @Test
    public void testStartExperimentParticipantUserAdmin() {
        when(httpServletRequest.isUserInRole(ROLE_ADMIN)).thenReturn(true);
        assertEquals(ERROR, participantController.startExperiment(ID_STRING, httpServletRequest));
        verify(httpServletRequest).isUserInRole(ROLE_ADMIN);
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(experimentService, never()).getExperiment(anyInt());
        verify(participantService, never()).getParticipant(anyInt(), anyInt());
        verify(participantService, never()).updateParticipant(any());
    }

    @Test
    public void testStartExperimentParticipantInvalidId() {
        assertEquals(ERROR, participantController.startExperiment(BLANK, httpServletRequest));
        verify(httpServletRequest, never()).isUserInRole(anyString());
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(experimentService, never()).getExperiment(anyInt());
        verify(participantService, never()).getParticipant(anyInt(), anyInt());
        verify(participantService, never()).updateParticipant(any());
    }

    @Test
    public void testStartExperimentParticipantIdNull() {
        assertEquals(ERROR, participantController.startExperiment(null, httpServletRequest));
        verify(httpServletRequest, never()).isUserInRole(anyString());
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(experimentService, never()).getExperiment(anyInt());
        verify(participantService, never()).getParticipant(anyInt(), anyInt());
        verify(participantService, never()).updateParticipant(any());
    }

    @Test
    public void testStopExperiment() {
        participantDTO.setStart(LocalDateTime.now());
        when(userService.getUserById(ID)).thenReturn(userDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        when(participantService.updateParticipant(participantDTO)).thenReturn(true);
        assertEquals(REDIRECT_FINISH + ID + EXPERIMENT_PARAM + ID + SECRET_PARAM + SECRET,
                participantController.stopExperiment(ID_STRING, ID_STRING, SECRET, httpServletRequest));
        verify(participantService).isInvalidParticipant(ID, ID, SECRET, true);
        verify(userService).getUserById(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService).simultaneousParticipation(ID);
        verify(participantService).updateParticipant(participantDTO);
        verify(userService).saveUser(userDTO);
        verify(httpServletRequest).getSession(false);
    }

    @Test
    public void testStopExperimentSimultaneousParticipation() {
        participantDTO.setStart(LocalDateTime.now());
        when(userService.getUserById(ID)).thenReturn(userDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        when(participantService.simultaneousParticipation(ID)).thenReturn(true);
        when(participantService.updateParticipant(participantDTO)).thenReturn(true);
        assertEquals(REDIRECT_FINISH + ID + EXPERIMENT_PARAM + ID + SECRET_PARAM + SECRET,
                participantController.stopExperiment(ID_STRING, ID_STRING, SECRET, httpServletRequest));
        verify(participantService).isInvalidParticipant(ID, ID, SECRET, true);
        verify(userService).getUserById(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService).simultaneousParticipation(ID);
        verify(participantService).updateParticipant(participantDTO);
        verify(userService, never()).saveUser(any());
        verify(httpServletRequest).getSession(false);
    }

    @Test
    public void testStopExperimentParticipantNotUpdated() {
        participantDTO.setStart(LocalDateTime.now());
        when(userService.getUserById(ID)).thenReturn(userDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        assertEquals(ERROR, participantController.stopExperiment(ID_STRING, ID_STRING, SECRET, httpServletRequest));
        verify(participantService).isInvalidParticipant(ID, ID, SECRET, true);
        verify(userService).getUserById(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService).simultaneousParticipation(ID);
        verify(participantService).updateParticipant(participantDTO);
        verify(userService, never()).saveUser(any());
        verify(httpServletRequest).getSession(false);
    }

    @Test
    public void testStopExperimentParticipantAlreadyFinished() {
        participantDTO.setStart(LocalDateTime.now());
        participantDTO.setEnd(LocalDateTime.now());
        when(userService.getUserById(ID)).thenReturn(userDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        assertEquals(ERROR, participantController.stopExperiment(ID_STRING, ID_STRING, SECRET, httpServletRequest));
        verify(participantService).isInvalidParticipant(ID, ID, SECRET, true);
        verify(userService).getUserById(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(participantService, never()).updateParticipant(any());
        verify(userService, never()).saveUser(any());
        verify(httpServletRequest).getSession(false);
    }

    @Test
    public void testStopExperimentParticipantNotStarted() {
        when(userService.getUserById(ID)).thenReturn(userDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        assertEquals(ERROR, participantController.stopExperiment(ID_STRING, ID_STRING, SECRET, httpServletRequest));
        verify(participantService).isInvalidParticipant(ID, ID, SECRET, true);
        verify(userService).getUserById(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(participantService, never()).updateParticipant(any());
        verify(userService, never()).saveUser(any());
        verify(httpServletRequest).getSession(false);
    }

    @Test
    public void testStopExperimentParticipantNotFound() {
        when(userService.getUserById(ID)).thenReturn(userDTO);
        when(participantService.getParticipant(ID, ID)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, participantController.stopExperiment(ID_STRING, ID_STRING, SECRET, httpServletRequest));
        verify(participantService).isInvalidParticipant(ID, ID, SECRET, true);
        verify(userService).getUserById(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(participantService, never()).updateParticipant(any());
        verify(userService, never()).saveUser(any());
        verify(httpServletRequest).getSession(false);
    }

    @Test
    public void testStopExperimentSecretBlank() {
        assertEquals(ERROR, participantController.stopExperiment(ID_STRING, ID_STRING, BLANK, httpServletRequest));
        verify(participantService, never()).isInvalidParticipant(anyInt(), anyInt(), anyString(), anyBoolean());
        verify(userService, never()).getUserById(anyInt());
        verify(participantService, never()).getParticipant(anyInt(), anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(participantService, never()).updateParticipant(any());
        verify(userService, never()).saveUser(any());
        verify(httpServletRequest, never()).getSession(anyBoolean());
    }

    @Test
    public void testStopExperimentSecretNull() {
        assertEquals(ERROR, participantController.stopExperiment(ID_STRING, ID_STRING, null, httpServletRequest));
        verify(participantService, never()).isInvalidParticipant(anyInt(), anyInt(), anyString(), anyBoolean());
        verify(userService, never()).getUserById(anyInt());
        verify(participantService, never()).getParticipant(anyInt(), anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(participantService, never()).updateParticipant(any());
        verify(userService, never()).saveUser(any());
        verify(httpServletRequest, never()).getSession(anyBoolean());
    }

    @Test
    public void testStopExperimentParticipantInvalidUserId() {
        assertEquals(ERROR, participantController.stopExperiment(ID_STRING, "0", SECRET, httpServletRequest));
        verify(participantService, never()).isInvalidParticipant(anyInt(), anyInt(), anyString(), anyBoolean());
        verify(userService, never()).getUserById(anyInt());
        verify(participantService, never()).getParticipant(anyInt(), anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(participantService, never()).updateParticipant(any());
        verify(userService, never()).saveUser(any());
        verify(httpServletRequest, never()).getSession(anyBoolean());
    }

    @Test
    public void testStopExperimentParticipantInvalidExperimentId() {
        assertEquals(ERROR, participantController.stopExperiment("-1", ID_STRING, SECRET, httpServletRequest));
        verify(participantService, never()).isInvalidParticipant(anyInt(), anyInt(), anyString(), anyBoolean());
        verify(userService, never()).getUserById(anyInt());
        verify(participantService, never()).getParticipant(anyInt(), anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(participantService, never()).updateParticipant(any());
        verify(userService, never()).saveUser(any());
        verify(httpServletRequest, never()).getSession(anyBoolean());
    }

    @Test
    public void testStopExperimentParticipantUserIdNull() {
        assertEquals(ERROR, participantController.stopExperiment(ID_STRING, null, SECRET, httpServletRequest));
        verify(participantService, never()).isInvalidParticipant(anyInt(), anyInt(), anyString(), anyBoolean());
        verify(userService, never()).getUserById(anyInt());
        verify(participantService, never()).getParticipant(anyInt(), anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(participantService, never()).updateParticipant(any());
        verify(userService, never()).saveUser(any());
        verify(httpServletRequest, never()).getSession(anyBoolean());
    }

    @Test
    public void testStopExperimentParticipantExperimentIdNull() {
        assertEquals(ERROR, participantController.stopExperiment(null, ID_STRING, SECRET, httpServletRequest));
        verify(participantService, never()).isInvalidParticipant(anyInt(), anyInt(), anyString(), anyBoolean());
        verify(userService, never()).getUserById(anyInt());
        verify(participantService, never()).getParticipant(anyInt(), anyInt());
        verify(participantService, never()).simultaneousParticipation(anyInt());
        verify(participantService, never()).updateParticipant(any());
        verify(userService, never()).saveUser(any());
        verify(httpServletRequest, never()).getSession(anyBoolean());
    }

    @Test
    public void testRestartExperiment() {
        userDTO.setActive(false);
        participantDTO.setStart(LocalDateTime.now());
        participantDTO.setEnd(LocalDateTime.now());
        when(userService.getUserById(ID)).thenReturn(userDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        when(participantService.updateParticipant(participantDTO)).thenReturn(true);
        assertAll(
                () -> assertEquals(REDIRECT_GUI + ID + EXP_ID + ID + SECRET_PARAM + SECRET + RESTART,
                        participantController.restartExperiment(ID_STRING, ID_STRING, SECRET)),
                () -> assertTrue(userDTO.isActive()),
                () -> assertNull(participantDTO.getEnd())
        );
        verify(participantService).isInvalidParticipant(ID, ID, SECRET, false);
        verify(userService).getUserById(ID);
        verify(experimentService).getExperiment(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService).updateParticipant(participantDTO);
        verify(userService).updateUser(userDTO);
    }

    @Test
    public void testRestartExperimentParticipantNotUpdated() {
        userDTO.setActive(false);
        participantDTO.setStart(LocalDateTime.now());
        participantDTO.setEnd(LocalDateTime.now());
        when(userService.getUserById(ID)).thenReturn(userDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        assertAll(
                () -> assertEquals(Constants.ERROR, participantController.restartExperiment(ID_STRING, ID_STRING,
                        SECRET)),
                () -> assertFalse(userDTO.isActive()),
                () -> assertNull(participantDTO.getEnd())
        );
        verify(participantService).isInvalidParticipant(ID, ID, SECRET, false);
        verify(userService).getUserById(ID);
        verify(experimentService).getExperiment(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService).updateParticipant(participantDTO);
        verify(userService, never()).updateUser(any());
    }

    @Test
    public void testRestartExperimentParticipantNotFinished() {
        participantDTO.setStart(LocalDateTime.now());
        when(userService.getUserById(ID)).thenReturn(userDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        assertEquals(Constants.ERROR, participantController.restartExperiment(ID_STRING, ID_STRING, SECRET));
        verify(participantService).isInvalidParticipant(ID, ID, SECRET, false);
        verify(userService).getUserById(ID);
        verify(experimentService).getExperiment(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService, never()).updateParticipant(any());
        verify(userService, never()).updateUser(any());
    }

    @Test
    public void testRestartExperimentParticipantNotStarted() {
        participantDTO.setEnd(LocalDateTime.now());
        when(userService.getUserById(ID)).thenReturn(userDTO);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        when(participantService.getParticipant(ID, ID)).thenReturn(participantDTO);
        assertEquals(Constants.ERROR, participantController.restartExperiment(ID_STRING, ID_STRING, SECRET));
        verify(participantService).isInvalidParticipant(ID, ID, SECRET, false);
        verify(userService).getUserById(ID);
        verify(experimentService).getExperiment(ID);
        verify(participantService).getParticipant(ID, ID);
        verify(participantService, never()).updateParticipant(any());
        verify(userService, never()).updateUser(any());
    }

    @Test
    public void testRestartExperimentNotFound() {
        when(userService.getUserById(ID)).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, participantController.restartExperiment(ID_STRING, ID_STRING, SECRET));
        verify(participantService).isInvalidParticipant(ID, ID, SECRET, false);
        verify(userService).getUserById(ID);
        verify(experimentService, never()).getExperiment(anyInt());
        verify(participantService, never()).getParticipant(anyInt(), anyInt());
        verify(participantService, never()).updateParticipant(any());
        verify(userService, never()).updateUser(any());
    }

    @Test
    public void testRestartExperimentInvalidParticipant() {
        when(participantService.isInvalidParticipant(ID, ID, SECRET, false)).thenReturn(true);
        assertEquals(Constants.ERROR, participantController.restartExperiment(ID_STRING, ID_STRING, SECRET));
        verify(participantService).isInvalidParticipant(ID, ID, SECRET, false);
        verify(userService, never()).getUserById(anyInt());
        verify(experimentService, never()).getExperiment(anyInt());
        verify(participantService, never()).getParticipant(anyInt(), anyInt());
        verify(participantService, never()).updateParticipant(any());
        verify(userService, never()).updateUser(any());
        verify(httpServletRequest, never()).isUserInRole(anyString());
    }

}
