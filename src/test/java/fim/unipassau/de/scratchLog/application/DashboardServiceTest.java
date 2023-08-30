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

package fim.unipassau.de.scratchLog.application;

import fim.unipassau.de.scratchLog.application.exception.NotFoundException;
import fim.unipassau.de.scratchLog.application.service.DashboardService;
import fim.unipassau.de.scratchLog.persistence.entity.Experiment;
import fim.unipassau.de.scratchLog.persistence.entity.ExperimentData;
import fim.unipassau.de.scratchLog.persistence.entity.Participant;
import fim.unipassau.de.scratchLog.persistence.entity.User;
import fim.unipassau.de.scratchLog.persistence.repository.ExperimentDataRepository;
import fim.unipassau.de.scratchLog.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratchLog.persistence.repository.ParticipantRepository;
import fim.unipassau.de.scratchLog.util.enums.Language;
import fim.unipassau.de.scratchLog.util.enums.Role;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceTest {

    @InjectMocks
    private DashboardService dashboardService;

    @Mock
    private ExperimentRepository experimentRepository;

    @Mock
    private ExperimentDataRepository experimentDataRepository;

    @Mock
    private ParticipantRepository participantRepository;

    private static final int ID = 3;
    private final Experiment experiment = new Experiment(ID, "title", "description", "info", "post", true, false, "");
    private final User user1 = new User("user1", "email1", Role.PARTICIPANT, Language.ENGLISH, "password", "secret");
    private final User user2 = new User("user2", "email2", Role.PARTICIPANT, Language.ENGLISH, "password", "secret");
    private final Participant participant1 = new Participant(user1, experiment, null, null);
    private final Participant participant2 = new Participant(user2, experiment, null, null);
    private final ExperimentData experimentData = new ExperimentData(ID, 15, 10, 7);
    private final String[] stringExperimentData = new String[]{String.valueOf(experimentData.getParticipants()),
            String.valueOf(experimentData.getStarted()), String.valueOf(experimentData.getFinished())};
    private final List<Participant> participants = List.of(participant1, participant2);

    @Test
    public void testExistsExperiment() {
        assertFalse(dashboardService.existsExperiment(ID));
        verify(experimentRepository).existsById(ID);
    }

    @Test
    public void testExistsExperimentInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> dashboardService.existsExperiment(0)
        );
        verify(experimentRepository, never()).existsById(anyInt());
    }

    @Test
    public void testExistsParticipants() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        assertFalse(dashboardService.existsParticipants(ID));
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).existsByExperiment(experiment);
    }

    @Test
    public void testExistsParticipantsNoExperiment() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.existsByExperiment(experiment)).thenThrow(EntityNotFoundException.class);
        assertFalse(dashboardService.existsParticipants(ID));
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).existsByExperiment(experiment);
    }

    @Test
    public void testExistsParticipantsNoExperimentInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> dashboardService.existsParticipants(0)
        );
        verify(experimentRepository, never()).getReferenceById(anyInt());
        verify(participantRepository, never()).existsByExperiment(any());
    }

    @Test
    public void testGetExperimentData() {
        when(experimentDataRepository.findByExperiment(ID)).thenReturn(Optional.of(experimentData));
        assertEquals(Arrays.toString(stringExperimentData), Arrays.toString(dashboardService.getExperimentData(ID)));
        verify(experimentDataRepository).findByExperiment(ID);
    }

    @Test
    public void testGetExperimentDataEmpty() {
        assertThrows(NotFoundException.class,
                () -> dashboardService.getExperimentData(ID)
        );
        verify(experimentDataRepository).findByExperiment(ID);
    }

    @Test
    public void testGetExperimentDataInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> dashboardService.getExperimentData(-1)
        );
        verify(experimentDataRepository, never()).findByExperiment(anyInt());
    }

    @Test
    public void getParticipants() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findAllByExperiment(experiment)).thenReturn(participants);
        List<String[]> userInfo = dashboardService.getParticipants(ID);
        assertAll(
                () -> assertEquals(2, userInfo.size()),
                () -> assertEquals(String.valueOf(user1.getId()), userInfo.get(0)[0]),
                () -> assertEquals(user1.getUsername(), userInfo.get(0)[1]),
                () -> assertEquals(String.valueOf(user2.getId()), userInfo.get(1)[0]),
                () -> assertEquals(user2.getUsername(), userInfo.get(1)[1])
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findAllByExperiment(experiment);
    }

    @Test
    public void getParticipantsNone() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        assertThrows(IllegalStateException.class,
                () -> dashboardService.getParticipants(ID)
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findAllByExperiment(experiment);
    }

    @Test
    public void getParticipantsNoExperiment() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findAllByExperiment(experiment)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> dashboardService.getParticipants(ID)
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findAllByExperiment(experiment);
    }

    @Test
    public void getParticipantsInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> dashboardService.getParticipants(-3)
        );
        verify(experimentRepository, never()).getReferenceById(anyInt());
        verify(participantRepository, never()).findAllByExperiment(any());
    }

}
