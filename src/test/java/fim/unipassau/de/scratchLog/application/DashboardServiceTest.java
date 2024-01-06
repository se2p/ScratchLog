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
import fim.unipassau.de.scratchLog.persistence.entity.EventCount;
import fim.unipassau.de.scratchLog.persistence.entity.Experiment;
import fim.unipassau.de.scratchLog.persistence.entity.ExperimentData;
import fim.unipassau.de.scratchLog.persistence.entity.Participant;
import fim.unipassau.de.scratchLog.persistence.entity.User;
import fim.unipassau.de.scratchLog.persistence.projection.EventProjection;
import fim.unipassau.de.scratchLog.persistence.repository.BlockEventRepository;
import fim.unipassau.de.scratchLog.persistence.repository.ClickEventRepository;
import fim.unipassau.de.scratchLog.persistence.repository.EventCountRepository;
import fim.unipassau.de.scratchLog.persistence.repository.ExperimentDataRepository;
import fim.unipassau.de.scratchLog.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratchLog.persistence.repository.ParticipantRepository;
import fim.unipassau.de.scratchLog.persistence.repository.ResourceEventRepository;
import fim.unipassau.de.scratchLog.persistence.repository.UserRepository;
import fim.unipassau.de.scratchLog.util.enums.BlockEventSpecific;
import fim.unipassau.de.scratchLog.util.enums.ClickEventSpecific;
import fim.unipassau.de.scratchLog.util.enums.Language;
import fim.unipassau.de.scratchLog.util.enums.ResourceEventSpecific;
import fim.unipassau.de.scratchLog.util.enums.Role;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

    @Mock
    private UserRepository userRepository;

    @Mock
    private BlockEventRepository blockEventRepository;

    @Mock
    private ClickEventRepository clickEventRepository;

    @Mock
    private ResourceEventRepository resourceEventRepository;

    @Mock
    private EventCountRepository eventCountRepository;

    private static final int ID = 3;
    private static final BlockEventSpecific BLOCK_EVENT = BlockEventSpecific.CREATE;
    private static final ClickEventSpecific CLICK_EVENT = ClickEventSpecific.GREENFLAG;
    private static final ResourceEventSpecific RESOURCE_EVENT = ResourceEventSpecific.ADD_COSTUME;
    private final Experiment experiment = new Experiment(ID, "title", "description", "info", "post", true, false, "");
    private final User user1 = new User("user1", "email1", Role.PARTICIPANT, Language.ENGLISH, "password", "secret");
    private final User user2 = new User("user2", "email2", Role.PARTICIPANT, Language.ENGLISH, "password", "secret");
    private final Participant participant1 = new Participant(user1, experiment, null, null);
    private final Participant participant2 = new Participant(user2, experiment, null, null);
    private final ExperimentData experimentData = new ExperimentData(ID, 15, 10, 7);
    private final String[] stringExperimentData = new String[]{String.valueOf(experimentData.getParticipants()),
            String.valueOf(experimentData.getStarted()), String.valueOf(experimentData.getFinished())};
    private final List<Participant> participants = List.of(participant1, participant2);
    private final List<Integer> userIds = List.of(ID, ID);
    private final List<EventProjection> eventProjections1 = getBlockEventProjections(1);
    private final List<EventProjection> eventProjections2 = getBlockEventProjections(2);
    private final EventCount eventCount1 = new EventCount(ID, ID, 5, BlockEventSpecific.CREATE.toString());
    private final EventCount eventCount2 = new EventCount(ID, ID, 3, ClickEventSpecific.GREENFLAG.toString());

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
    public void testGetParticipants() {
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
    public void testGetParticipantsNone() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        assertThrows(IllegalStateException.class,
                () -> dashboardService.getParticipants(ID)
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findAllByExperiment(experiment);
    }

    @Test
    public void testGetParticipantsNoExperiment() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findAllByExperiment(experiment)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> dashboardService.getParticipants(ID)
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findAllByExperiment(experiment);
    }

    @Test
    public void testGetParticipantsInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> dashboardService.getParticipants(-3)
        );
        verify(experimentRepository, never()).getReferenceById(anyInt());
        verify(participantRepository, never()).findAllByExperiment(any());
    }

    @Test
    public void testGetBlockEventCountData() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(userRepository.getReferenceById(ID)).thenReturn(user1);
        when(blockEventRepository.findAllByUserAndExperimentAndEvent(user1, experiment, BLOCK_EVENT)).thenReturn(
                eventProjections1);
        List<Integer[]> counts = dashboardService.getBlockEventCountData(userIds, ID, BLOCK_EVENT);
        assertAll(
                () -> assertEquals(2, counts.size()),
                () -> assertEquals(2, counts.get(0).length),
                () -> assertEquals(1, counts.get(0)[0]),
                () -> assertEquals(2, counts.get(0)[1])
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(userRepository, times(2)).getReferenceById(ID);
        verify(blockEventRepository, times(2)).findAllByUserAndExperimentAndEvent(user1,
                experiment, BLOCK_EVENT);
    }

    @Test
    public void testGetBlockEventCountDataNoEvents() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(userRepository.getReferenceById(ID)).thenReturn(user1);
        List<Integer[]> counts = dashboardService.getBlockEventCountData(userIds, ID, BLOCK_EVENT);
        assertAll(
                () -> assertEquals(2, counts.size()),
                () -> assertEquals(0, counts.get(0).length),
                () -> assertEquals(0, counts.get(1).length)
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(userRepository, times(2)).getReferenceById(ID);
        verify(blockEventRepository, times(2)).findAllByUserAndExperimentAndEvent(user1,
                experiment, BLOCK_EVENT);
    }

    @Test
    public void testGetBlockEventCountDataNotFound() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(userRepository.getReferenceById(ID)).thenReturn(user1);
        when(blockEventRepository.findAllByUserAndExperimentAndEvent(user1, experiment, BLOCK_EVENT)).thenThrow(
                EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> dashboardService.getBlockEventCountData(userIds, ID, BLOCK_EVENT)
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(userRepository).getReferenceById(ID);
        verify(blockEventRepository).findAllByUserAndExperimentAndEvent(user1, experiment, BLOCK_EVENT);
    }

    @Test
    public void testGetBlockEventCountDataInvalidUserIds() {
        assertThrows(IllegalArgumentException.class,
                () -> dashboardService.getBlockEventCountData(List.of(ID, 0), ID, BLOCK_EVENT)
        );
        verify(experimentRepository, never()).getReferenceById(anyInt());
        verify(userRepository, never()).getReferenceById(anyInt());
        verify(blockEventRepository, never()).findAllByUserAndExperimentAndEvent(any(), any(), any());
    }

    @Test
    public void testGetBlockEventCountDataInvalidExperimentId() {
        assertThrows(IllegalArgumentException.class,
                () -> dashboardService.getBlockEventCountData(userIds, -1, BLOCK_EVENT)
        );
        verify(experimentRepository, never()).getReferenceById(anyInt());
        verify(userRepository, never()).getReferenceById(anyInt());
        verify(blockEventRepository, never()).findAllByUserAndExperimentAndEvent(any(), any(), any());
    }

    @Test
    public void testGetClickEventCountData() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(userRepository.getReferenceById(ID)).thenReturn(user1);
        when(clickEventRepository.findAllByUserAndExperimentAndEvent(user1, experiment, CLICK_EVENT)).thenReturn(
                eventProjections2);
        List<Integer[]> counts = dashboardService.getClickEventCountData(userIds, ID, CLICK_EVENT);
        assertAll(
                () -> assertEquals(2, counts.size()),
                () -> assertEquals(3, counts.get(0).length),
                () -> assertEquals(1, counts.get(0)[0]),
                () -> assertEquals(0, counts.get(0)[1]),
                () -> assertEquals(2, counts.get(0)[2])
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(userRepository, times(2)).getReferenceById(ID);
        verify(clickEventRepository, times(2)).findAllByUserAndExperimentAndEvent(user1,
                experiment, CLICK_EVENT);
    }

    @Test
    public void testGetClickEventCountDataNotFound() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(userRepository.getReferenceById(ID)).thenReturn(user1);
        when(clickEventRepository.findAllByUserAndExperimentAndEvent(user1, experiment, CLICK_EVENT)).thenThrow(
                EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> dashboardService.getClickEventCountData(userIds, ID, CLICK_EVENT)
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(userRepository).getReferenceById(ID);
        verify(clickEventRepository).findAllByUserAndExperimentAndEvent(user1, experiment, CLICK_EVENT);
    }

    @Test
    public void testGetResourceEventCountData() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(userRepository.getReferenceById(ID)).thenReturn(user1);
        when(resourceEventRepository.findAllByUserAndExperimentAndEvent(user1, experiment, RESOURCE_EVENT)).thenReturn(
                eventProjections2);
        List<Integer[]> counts = dashboardService.getResourceEventCountData(userIds, ID, RESOURCE_EVENT);
        assertAll(
                () -> assertEquals(2, counts.size()),
                () -> assertEquals(3, counts.get(0).length),
                () -> assertEquals(1, counts.get(0)[0]),
                () -> assertEquals(0, counts.get(0)[1]),
                () -> assertEquals(2, counts.get(0)[2])
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(userRepository, times(2)).getReferenceById(ID);
        verify(resourceEventRepository, times(2)).findAllByUserAndExperimentAndEvent(user1,
                experiment, RESOURCE_EVENT);
    }

    @Test
    public void testGetResourceEventCountDataNotFound() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(userRepository.getReferenceById(ID)).thenReturn(user1);
        when(resourceEventRepository.findAllByUserAndExperimentAndEvent(user1, experiment, RESOURCE_EVENT)).thenThrow(
                EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> dashboardService.getResourceEventCountData(userIds, ID, RESOURCE_EVENT)
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(userRepository).getReferenceById(ID);
        verify(resourceEventRepository).findAllByUserAndExperimentAndEvent(user1, experiment, RESOURCE_EVENT);
    }

    @Test
    public void testGetEventCountData() {
        when(eventCountRepository.findBlockEventCountByUserAndExperiment(ID, ID,
                BlockEventSpecific.CREATE.toString())).thenReturn(Optional.of(eventCount1));
        when(eventCountRepository.findClickEventCountByUserAndExperiment(ID, ID,
                ClickEventSpecific.GREENFLAG.toString())).thenReturn(Optional.of(eventCount2));
        List<Integer[]> counts = dashboardService.getEventCountData(userIds, ID);
        assertAll(
                () -> assertEquals(2, counts.size()),
                () -> assertEquals(6, counts.get(0).length),
                () -> assertEquals(5, counts.get(0)[0]),
                () -> assertEquals(0, counts.get(0)[1]),
                () -> assertEquals(0, counts.get(0)[2]),
                () -> assertEquals(3, counts.get(0)[3]),
                () -> assertEquals(0, counts.get(0)[4]),
                () -> assertEquals(0, counts.get(0)[5])
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(eventCountRepository, times(6)).findBlockEventCountByUserAndExperiment(anyInt(), anyInt(), anyString());
        verify(eventCountRepository, times(6)).findClickEventCountByUserAndExperiment(anyInt(), anyInt(), anyString());
    }

    private List<EventProjection> getBlockEventProjections(long minutes) {
        List<EventProjection> projections = new ArrayList<>();
        EventProjection eventProjection1 = new EventProjection() {
            @Override
            public Integer getId() {
                return 1;
            }

            @Override
            public LocalDateTime getDate() {
                return LocalDateTime.now();
            }
        };
        EventProjection eventProjection2 = new EventProjection() {
            @Override
            public Integer getId() {
                return 2;
            }

            @Override
            public LocalDateTime getDate() {
                return LocalDateTime.now().plusMinutes(minutes);
            }
        };
        EventProjection eventProjection3 = new EventProjection() {
            @Override
            public Integer getId() {
                return 3;
            }

            @Override
            public LocalDateTime getDate() {
                return LocalDateTime.now().plusMinutes(minutes);
            }
        };
        projections.add(eventProjection1);
        projections.add(eventProjection2);
        projections.add(eventProjection3);
        return projections;
    }

}
