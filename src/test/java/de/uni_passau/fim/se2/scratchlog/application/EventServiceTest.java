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

import de.uni_passau.fim.se2.scratchlog.application.service.EventService;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.BlockEvent;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.CodesData;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.EventCount;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Experiment;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Participant;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.BlockEventRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ClickEventRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.CodesDataRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.DebuggerEventRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.EventCountRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ExperimentRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ParticipantRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.DebuggerQuestionEventRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ResourceEventRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.UserRepository;
import de.uni_passau.fim.se2.scratchlog.util.enums.BlockEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.BlockEventType;
import de.uni_passau.fim.se2.scratchlog.util.enums.ClickEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.ClickEventType;
import de.uni_passau.fim.se2.scratchlog.util.enums.DebuggerEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.DebuggerEventType;
import de.uni_passau.fim.se2.scratchlog.util.enums.Language;
import de.uni_passau.fim.se2.scratchlog.util.enums.LibraryResource;
import de.uni_passau.fim.se2.scratchlog.util.enums.DebuggerQuestionEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.DebuggerQuestionEventType;
import de.uni_passau.fim.se2.scratchlog.util.enums.ResourceEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.ResourceEventType;
import de.uni_passau.fim.se2.scratchlog.util.enums.Role;
import de.uni_passau.fim.se2.scratchlog.web.dto.BlockEventDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.ClickEventDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.CodesDataDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.DebuggerEventDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.EventCountDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.DebuggerQuestionEventDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.ResourceEventDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @InjectMocks
    private EventService eventService;

    @Mock
    private EventCountRepository eventCountRepository;

    @Mock
    private BlockEventRepository blockEventRepository;

    @Mock
    private ClickEventRepository clickEventRepository;

    @Mock
    private DebuggerEventRepository debuggerEventRepository;

    @Mock
    private DebuggerQuestionEventRepository debuggerQuestionEventRepository;

    @Mock
    private ResourceEventRepository resourceEventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExperimentRepository experimentRepository;

    @Mock
    private CodesDataRepository codesDataRepository;

    private static final int ID = 1;
    private static final String GUI_URL = "scratch";
    private BlockEventDTO blockEventDTO;
    private final ClickEventDTO clickEventDTO = new ClickEventDTO(1, 1, "secret", ClickEventType.CODE,
        ClickEventSpecific.STACKCLICK, "meta", LocalDateTime.now());
    private final DebuggerEventDTO debuggerEventDTO = new DebuggerEventDTO(1, 1, "secret", DebuggerEventType.BLOCK,
        DebuggerEventSpecific.OPEN_BLOCK, "id", "name", 0, 5, LocalDateTime.now());
    private final DebuggerQuestionEventDTO debuggerQuestionEventDTO = new DebuggerQuestionEventDTO(1, 1, "secret", DebuggerQuestionEventType.QUESTION,
        DebuggerQuestionEventSpecific.RATE, 0, "type", new String[]{"value1", "value2"}, "category", "form", "id", "opcode",
        LocalDateTime.now());
    private final ResourceEventDTO resourceEventDTO = new ResourceEventDTO(1, 1, "secret", ResourceEventType.ADD,
        ResourceEventSpecific.ADD_SOUND, "name", "hash", "filetype", LibraryResource.TRUE, LocalDateTime.now());
    private final User user = new User("participant", "email", Role.PARTICIPANT, Language.GERMAN, "password",
        "secret");
    private final Experiment experiment = new Experiment(ID, "title", "description", "info", "postscript", true,
            false, GUI_URL);
    private final Participant participant = new Participant(user, experiment, LocalDateTime.now(), null);
    private final CodesData codesData = new CodesData(ID, ID, 15);
    private final BlockEvent blockEvent = new BlockEvent(user, experiment, LocalDateTime.now(), BlockEventType.CREATE,
            BlockEventSpecific.CREATE, "sprite", "", "xml", "json.txt");
    private static final String JSON = "json.txt";
    private final List<EventCount> blockEvents = getEventCounts(8, "CREATE");
    private final List<EventCount> clickEvents = getEventCounts(2, "GREENFLAG");
    private final List<EventCount> resourceEvents = getEventCounts(3, "RENAME");

    @BeforeEach
    public void setup() {
        blockEventDTO = new BlockEventDTO(1, 1, "empty", BlockEventType.CHANGE, BlockEventSpecific.CHANGE, "sprite", "meta", "xml", "{}", LocalDateTime.now());

        user.setId(ID);
        user.setActive(true);
        experiment.setActive(true);
        resourceEventDTO.setLibraryResource(LibraryResource.TRUE);
        blockEvent.setCode(JSON);
        participant.setEnd(null);
        blockEventDTO.setDate(LocalDateTime.now());
        resourceEventDTO.setDate(LocalDateTime.now());
        debuggerQuestionEventDTO.setDate(LocalDateTime.now());
    }

    @Test
    public void testSaveBlockEvent() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(Optional.of(participant));
        assertDoesNotThrow(
                () -> eventService.saveBlockEvent(blockEventDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(blockEventRepository).save(any());
    }

    @Test
    public void testSaveBlockEventParticipantNull() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        assertDoesNotThrow(
                () -> eventService.saveBlockEvent(blockEventDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(blockEventRepository, never()).save(any());
    }

    @Test
    public void testSaveBlockEventEntityNotFound() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenThrow(EntityNotFoundException.class);
        assertDoesNotThrow(
                () -> eventService.saveBlockEvent(blockEventDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(blockEventRepository, never()).save(any());
    }

    @Test
    public void testSaveBlockEventUserNull() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(any(), any())).thenReturn(Optional.of(participant));
        assertDoesNotThrow(
                () -> eventService.saveBlockEvent(blockEventDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(any(), any());
        verify(blockEventRepository, never()).save(any());
    }

    @Test
    public void testSaveBlockEventExperimentNull() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(participantRepository.findByUserAndExperiment(any(), any())).thenReturn(Optional.of(participant));
        assertDoesNotThrow(
                () -> eventService.saveBlockEvent(blockEventDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(any(), any());
        verify(blockEventRepository, never()).save(any());
    }

    @Test
    public void testSaveBlockEventDateNull() {
        blockEventDTO.setDate(null);
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(any(), any())).thenReturn(Optional.of(participant));
        assertDoesNotThrow(
                () -> eventService.saveBlockEvent(blockEventDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(blockEventRepository, never()).save(any());
    }

    @Test
    public void testSaveBlockEventUserInactive() {
        user.setActive(false);
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(any(), any())).thenReturn(Optional.of(participant));
        assertDoesNotThrow(
                () -> eventService.saveBlockEvent(blockEventDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(blockEventRepository, never()).save(any());
    }

    @Test
    public void testSaveBlockEventExperimentInactive() {
        experiment.setActive(false);
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(any(), any())).thenReturn(Optional.of(participant));
        assertDoesNotThrow(
                () -> eventService.saveBlockEvent(blockEventDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(blockEventRepository, never()).save(any());
    }

    @Test
    public void testSaveBlockEventConstraintViolation() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(Optional.of(participant));
        when(blockEventRepository.save(any())).thenThrow(ConstraintViolationException.class);
        assertDoesNotThrow(
                () -> eventService.saveBlockEvent(blockEventDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(blockEventRepository).save(any());
    }

    @Test
    public void testSaveClickEvent() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(Optional.of(participant));
        assertDoesNotThrow(
                () -> eventService.saveClickEvent(clickEventDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(clickEventRepository).save(any());
    }

    @Test
    public void testSaveClickEventNoParticipant() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        assertDoesNotThrow(
                () -> eventService.saveClickEvent(clickEventDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(clickEventRepository, never()).save(any());
    }

    @Test
    public void testSaveClickEventInvalidEvent() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(any(), any())).thenReturn(Optional.of(participant));
        assertDoesNotThrow(
                () -> eventService.saveClickEvent(clickEventDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(any(), any());
        verify(clickEventRepository, never()).save(any());
    }

    @Test
    public void testSaveClickEventUserInactive() {
        user.setActive(false);
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(Optional.of(participant));
        assertDoesNotThrow(
                () -> eventService.saveClickEvent(clickEventDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(clickEventRepository, never()).save(any());
    }

    @Test
    public void testSaveClickEventConstraintViolation() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(Optional.of(participant));
        when(clickEventRepository.save(any())).thenThrow(ConstraintViolationException.class);
        assertDoesNotThrow(
                () -> eventService.saveClickEvent(clickEventDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(clickEventRepository).save(any());
    }

    @Test
    public void testSaveDebuggerEvent() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(Optional.of(participant));
        assertDoesNotThrow(
                () -> eventService.saveDebuggerEvent(debuggerEventDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(debuggerEventRepository).save(any());
    }

    @Test
    public void testSaveDebuggerEventNoParticipant() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenThrow(EntityNotFoundException.class);
        assertDoesNotThrow(
                () -> eventService.saveDebuggerEvent(debuggerEventDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(debuggerEventRepository, never()).save(any());
    }

    @Test
    public void testSaveDebuggerEventInvalidEvent() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(participantRepository.findByUserAndExperiment(any(), any())).thenReturn(Optional.of(participant));
        assertDoesNotThrow(
                () -> eventService.saveDebuggerEvent(debuggerEventDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(any(), any());
        verify(debuggerEventRepository, never()).save(any());
    }

    @Test
    public void testSaveDebuggerEventConstraintViolation() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(Optional.of(participant));
        when(debuggerEventRepository.save(any())).thenThrow(ConstraintViolationException.class);
        assertDoesNotThrow(
                () -> eventService.saveDebuggerEvent(debuggerEventDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(debuggerEventRepository).save(any());
    }

    @Test
    public void testSaveQuestionEvent() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(Optional.of(participant));
        assertDoesNotThrow(
                () -> eventService.saveQuestionEvent(debuggerQuestionEventDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(debuggerQuestionEventRepository).save(any());
    }

    @Test
    public void testSaveQuestionEventNoParticipant() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        assertDoesNotThrow(
                () -> eventService.saveQuestionEvent(debuggerQuestionEventDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(debuggerQuestionEventRepository, never()).save(any());
    }

    @Test
    public void testSaveQuestionEventInvalidEvent() {
        debuggerQuestionEventDTO.setDate(null);
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(Optional.of(participant));
        assertDoesNotThrow(
                () -> eventService.saveQuestionEvent(debuggerQuestionEventDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(debuggerQuestionEventRepository, never()).save(any());
    }

    @Test
    public void testSaveQuestionEventExperimentInactive() {
        experiment.setActive(false);
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(Optional.of(participant));
        assertDoesNotThrow(
                () -> eventService.saveQuestionEvent(debuggerQuestionEventDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(debuggerQuestionEventRepository, never()).save(any());
    }

    @Test
    public void testSaveQuestionEventConstraintViolation() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(Optional.of(participant));
        when(debuggerQuestionEventRepository.save(any())).thenThrow(ConstraintViolationException.class);
        assertDoesNotThrow(
                () -> eventService.saveQuestionEvent(debuggerQuestionEventDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(debuggerQuestionEventRepository).save(any());
    }

    @Test
    public void testSaveResourceEvent() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(Optional.of(participant));
        assertDoesNotThrow(
                () -> eventService.saveResourceEvent(resourceEventDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(resourceEventRepository).save(any());
    }

    @Test
    public void testSaveResourceEventLibraryResourceFalse() {
        resourceEventDTO.setLibraryResource(LibraryResource.FALSE);
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(Optional.of(participant));
        assertDoesNotThrow(
                () -> eventService.saveResourceEvent(resourceEventDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(resourceEventRepository).save(any());
    }

    @Test
    public void testSaveResourceEventLibraryResourceUnknown() {
        resourceEventDTO.setLibraryResource(LibraryResource.UNKNOWN);
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(Optional.of(participant));
        assertDoesNotThrow(
                () -> eventService.saveResourceEvent(resourceEventDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(resourceEventRepository).save(any());
    }

    @Test
    public void testSaveResourceEventConstraintViolation() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(Optional.of(participant));
        when(resourceEventRepository.save(any())).thenThrow(ConstraintViolationException.class);
        assertDoesNotThrow(
                () -> eventService.saveResourceEvent(resourceEventDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(resourceEventRepository).save(any());
    }

    @Test
    public void testSaveResourceEventParticipantFinished() {
        participant.setEnd(LocalDateTime.now());
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(Optional.of(participant));
        assertDoesNotThrow(
                () -> eventService.saveResourceEvent(resourceEventDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(resourceEventRepository, never()).save(any());
    }


    @Test
    public void testSaveResourceEventNoParticipant() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        assertDoesNotThrow(
                () -> eventService.saveResourceEvent(resourceEventDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(resourceEventRepository, never()).save(any());
    }

    @Test
    public void testSaveResourceEventInvalidEvent() {
        resourceEventDTO.setDate(null);
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(Optional.of(participant));
        assertDoesNotThrow(
                () -> eventService.saveResourceEvent(resourceEventDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(resourceEventRepository, never()).save(any());
    }

    @Test
    public void testSaveResourceEventUserInactive() {
        user.setActive(false);
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(Optional.of(participant));
        assertDoesNotThrow(
                () -> eventService.saveResourceEvent(resourceEventDTO)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(resourceEventRepository, never()).save(any());
    }

    @Test
    public void testGetBlockEventCounts() {
        when(eventCountRepository.findAllBlockEventsByUserAndExperiment(ID, ID)).thenReturn(blockEvents);
        List<EventCountDTO> eventCountDTOS = eventService.getBlockEventCounts(ID, ID);
        assertAll(
                () -> assertEquals(8, eventCountDTOS.size()),
                () -> assertEquals(1, eventCountDTOS.getFirst().getUser()),
                () -> assertEquals(1, eventCountDTOS.getFirst().getExperiment()),
                () -> assertEquals(0, eventCountDTOS.getFirst().getCount()),
                () -> assertEquals("CREATE0", eventCountDTOS.getFirst().getEvent()),
                () -> assertEquals(7, eventCountDTOS.get(7).getCount()),
                () -> assertEquals("CREATE7", eventCountDTOS.get(7).getEvent())
        );
        verify(eventCountRepository).findAllBlockEventsByUserAndExperiment(ID, ID);
    }

    @Test
    public void testGetClickEventCounts() {
        when(eventCountRepository.findAllClickEventsByUserAndExperiment(ID, ID)).thenReturn(clickEvents);
        List<EventCountDTO> eventCountDTOS = eventService.getClickEventCounts(ID, ID);
        assertAll(
                () -> assertEquals(2, eventCountDTOS.size()),
                () -> assertEquals(1, eventCountDTOS.getFirst().getUser()),
                () -> assertEquals(1, eventCountDTOS.getFirst().getExperiment()),
                () -> assertEquals(0, eventCountDTOS.getFirst().getCount()),
                () -> assertEquals("GREENFLAG0", eventCountDTOS.getFirst().getEvent()),
                () -> assertEquals(1, eventCountDTOS.get(1).getCount()),
                () -> assertEquals("GREENFLAG1", eventCountDTOS.get(1).getEvent())
        );
        verify(eventCountRepository).findAllClickEventsByUserAndExperiment(ID, ID);
    }

    @Test
    public void testGetResourceEventCounts() {
        when(eventCountRepository.findAllResourceEventsByUserIdAndExperimentId(ID, ID)).thenReturn(resourceEvents);
        List<EventCountDTO> eventCountDTOS = eventService.getResourceEventCounts(ID, ID);
        assertAll(
                () -> assertEquals(3, eventCountDTOS.size()),
                () -> assertEquals(1, eventCountDTOS.getFirst().getUser()),
                () -> assertEquals(1, eventCountDTOS.getFirst().getExperiment()),
                () -> assertEquals(0, eventCountDTOS.getFirst().getCount()),
                () -> assertEquals("RENAME0", eventCountDTOS.getFirst().getEvent()),
                () -> assertEquals(2, eventCountDTOS.get(2).getCount()),
                () -> assertEquals("RENAME2", eventCountDTOS.get(2).getEvent())
        );
        verify(eventCountRepository).findAllResourceEventsByUserIdAndExperimentId(ID, ID);
    }

    @Test
    public void testGetCodesData() {
        when(codesDataRepository.findByUserAndExperiment(ID, ID)).thenReturn(Optional.of(codesData));
        CodesDataDTO codesDataDTO = eventService.getCodesData(ID, ID);
        assertAll(
                () -> assertEquals(codesData.getUser(), codesDataDTO.getUser()),
                () -> assertEquals(codesData.getExperiment(), codesDataDTO.getExperiment()),
                () -> assertEquals(codesData.getCount(), codesDataDTO.getCount())
        );
        verify(codesDataRepository).findByUserAndExperiment(ID, ID);
    }

    @Test
    public void testGetCodesDataNull() {
        CodesDataDTO codesDataDTO = eventService.getCodesData(ID, ID);
        assertAll(
                () -> assertNull(codesDataDTO.getUser()),
                () -> assertNull(codesDataDTO.getExperiment()),
                () -> assertEquals(0, codesDataDTO.getCount())
        );
        verify(codesDataRepository).findByUserAndExperiment(ID, ID);
    }

    private List<EventCount> getEventCounts(int number, String event) {
        List<EventCount> eventCounts = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            eventCounts.add(new EventCount(1, 1, i, event + i));
        }
        return eventCounts;
    }

}
