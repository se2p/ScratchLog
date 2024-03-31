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

import fim.unipassau.de.scratchLog.application.exception.IncompleteDataException;
import fim.unipassau.de.scratchLog.application.exception.NotFoundException;
import fim.unipassau.de.scratchLog.application.service.ExperimentService;
import fim.unipassau.de.scratchLog.application.service.UserService;
import fim.unipassau.de.scratchLog.spring.configuration.SecurityTestConfig;
import fim.unipassau.de.scratchLog.util.Constants;
import fim.unipassau.de.scratchLog.util.enums.Language;
import fim.unipassau.de.scratchLog.util.enums.Role;
import fim.unipassau.de.scratchLog.web.controller.SecretController;
import fim.unipassau.de.scratchLog.web.dto.ExperimentDTO;
import fim.unipassau.de.scratchLog.web.dto.UserDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.ui.Model;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import static fim.unipassau.de.scratchLog.util.CommonAssertions.assertInvalidIdException;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(SecretController.class)
@Import(SecurityTestConfig.class)
public class SecretControllerTest extends AbstractControllerTest {

    @Autowired
    private SecretController secretController;

    @MockBean
    private UserService userService;

    @MockBean
    private ExperimentService experimentService;

    @Mock
    private Model model;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private PrintWriter writer;

    private static final int ID = 1;
    private static final String SECRET = "secret";
    private static final String GUI_URL = "scratch";
    private final ExperimentDTO experiment = new ExperimentDTO(ID, "experiment", "my experiment", "info", "no", true,
            false, GUI_URL);
    private final UserDTO user1 = new UserDTO("participant1", "part1@part.de", Role.PARTICIPANT, Language.ENGLISH,
            "password1", SECRET);
    private final UserDTO user2 = new UserDTO("participant2", "part2@part.de", Role.PARTICIPANT, Language.ENGLISH,
            "password2", SECRET);
    private List<UserDTO> users;

    @BeforeEach
    public void setUp() {
        user1.setId(ID);
        user1.setSecret(SECRET);
        user2.setId(ID + 1);
        users = Arrays.asList(user1, user2);
        experiment.setActive(true);
    }

    @Test
    public void testDisplaySecret() {
        when(experimentService.getExperiment(ID)).thenReturn(experiment);
        when(userService.getUserById(ID)).thenReturn(user1);
        assertEquals(SECRET, secretController.displaySecret(ID, ID, model));
        verify(experimentService).getExperiment(ID);
        verify(userService).getUserById(ID);
        verify(model, times(3)).addAttribute(anyString(), any());
    }

    @Test
    public void testDisplaySecretExperimentInactive() {
        experiment.setActive(false);
        when(experimentService.getExperiment(ID)).thenReturn(experiment);
        assertEquals(SECRET, secretController.displaySecret(ID, ID, model));
        verify(experimentService).getExperiment(ID);
        verify(userService, never()).getUserById(anyInt());
        verify(model, times(4)).addAttribute(anyString(), any());
    }

    @Test
    public void testDisplaySecretNull() {
        user1.setSecret(null);
        when(experimentService.getExperiment(ID)).thenReturn(experiment);
        when(userService.getUserById(ID)).thenReturn(user1);
        assertEquals(Constants.ERROR, secretController.displaySecret(ID, ID, model));
        verify(userService).getUserById(ID);
        verify(experimentService).getExperiment(ID);
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDisplaySecretNotFound() {
        when(experimentService.getExperiment(ID)).thenReturn(experiment);
        when(userService.getUserById(ID)).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, secretController.displaySecret(ID, ID, model));
        verify(experimentService).getExperiment(ID);
        verify(userService).getUserById(ID);
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDisplaySecretInvalidUserId() {
        assertInvalidIdException(() -> secretController.displaySecret(0, ID, model));
        verify(experimentService, never()).getExperiment(anyInt());
        verify(userService, never()).getUserById(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDisplaySecretInvalidExperimentId() {
        assertInvalidIdException(() -> secretController.displaySecret(ID, -1, model));
        verify(experimentService, never()).getExperiment(anyInt());
        verify(userService, never()).getUserById(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDisplaySecrets() {
        when(experimentService.getExperiment(ID)).thenReturn(experiment);
        when(userService.findUnfinishedUsers(ID)).thenReturn(users);
        assertEquals(SECRET, secretController.displaySecrets(ID, model));
        verify(experimentService).getExperiment(ID);
        verify(userService).findUnfinishedUsers(ID);
        verify(model, times(3)).addAttribute(anyString(), any());
    }

    @Test
    public void testDisplaySecretsExperimentInactive() {
        experiment.setActive(false);
        when(experimentService.getExperiment(ID)).thenReturn(experiment);
        assertEquals(SECRET, secretController.displaySecrets(ID, model));
        verify(experimentService).getExperiment(ID);
        verify(userService, never()).findUnfinishedUsers(anyInt());
        verify(model, times(4)).addAttribute(anyString(), any());
    }

    @Test
    public void testDisplaySecretsNotFound() {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, secretController.displaySecrets(ID, model));
        verify(experimentService).getExperiment(ID);
        verify(userService, never()).findUnfinishedUsers(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDisplaySecretsInvalidId() {
        assertInvalidIdException(() -> secretController.displaySecrets(0, model));
        verify(experimentService, never()).getExperiment(anyInt());
        verify(userService, never()).findUnfinishedUsers(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDownloadParticipationLinks() throws IOException {
        when(userService.findUnfinishedUsers(ID)).thenReturn(users);
        when(httpServletResponse.getWriter()).thenReturn(writer);
        assertDoesNotThrow(
                () -> secretController.downloadParticipationLinks(ID, null, httpServletResponse)
        );
        verify(userService, never()).getUserById(anyInt());
        verify(userService).findUnfinishedUsers(ID);
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
        verify(httpServletResponse).getWriter();
    }

    @Test
    public void testDownloadParticipationLinksUser() throws IOException {
        when(userService.getUserById(ID)).thenReturn(user1);
        when(httpServletResponse.getWriter()).thenReturn(writer);
        assertDoesNotThrow(
                () -> secretController.downloadParticipationLinks(ID, ID, httpServletResponse)
        );
        verify(userService).getUserById(ID);
        verify(userService, never()).findUnfinishedUsers(anyInt());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
        verify(httpServletResponse).getWriter();
    }

    @Test
    public void testDownloadParticipationLinksUserInvalid() throws IOException {
        assertInvalidIdException(() -> secretController.downloadParticipationLinks(ID, -1, httpServletResponse));
        verify(userService, never()).getUserById(anyInt());
        verify(userService, never()).findUnfinishedUsers(anyInt());
        verify(httpServletResponse, never()).setStatus(anyInt());
        verify(httpServletResponse, never()).getWriter();
    }

    @Test
    public void testDownloadParticipationLinksIO() throws IOException {
        when(userService.findUnfinishedUsers(ID)).thenReturn(users);
        when(httpServletResponse.getWriter()).thenThrow(IOException.class);
        assertThrows(RuntimeException.class,
                () -> secretController.downloadParticipationLinks(ID, null, httpServletResponse)
        );
        verify(userService, never()).getUserById(anyInt());
        verify(userService).findUnfinishedUsers(ID);
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
        verify(httpServletResponse).getWriter();
    }

    @Test
    public void testDownloadParticipationLinksInvalidExperimentId() throws IOException {
        assertInvalidIdException(
                () -> secretController.downloadParticipationLinks(0, null, httpServletResponse)
        );
        verify(userService, never()).getUserById(anyInt());
        verify(userService, never()).findUnfinishedUsers(anyInt());
        verify(httpServletResponse, never()).setStatus(anyInt());
        verify(httpServletResponse, never()).getWriter();
    }
}
