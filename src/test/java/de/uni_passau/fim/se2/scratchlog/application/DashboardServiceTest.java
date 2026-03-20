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

package de.uni_passau.fim.se2.scratchlog.application;

import de.uni_passau.fim.se2.scratchlog.application.service.DashboardService;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.EventCount;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Experiment;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.ExperimentData;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Participant;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.EventProjection;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.BlockEventRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ClickEventRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.EventCountRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ExperimentDataRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ExperimentRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ParticipantRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ResourceEventRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.UserRepository;
import de.uni_passau.fim.se2.scratchlog.util.enums.BlockEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.ClickEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.Language;
import de.uni_passau.fim.se2.scratchlog.util.enums.ResourceEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.Role;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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
    private final DashboardService.ExperimentDataDto experimentDataDto = new DashboardService.ExperimentDataDto(
        experimentData.getParticipants(),
        experimentData.getStarted(),
        experimentData.getFinished()
    );
    private final List<Participant> participants = List.of(participant1, participant2);
    private final List<Integer> userIds = List.of(ID, ID);
    private final List<EventProjection> eventProjections1 = getBlockEventProjections(1);
    private final List<EventProjection> eventProjections2 = getBlockEventProjections(2);
    private final EventCount eventCount1 = new EventCount(ID, ID, 5, BlockEventSpecific.CREATE.toString());
    private final EventCount eventCount2 = new EventCount(ID, ID, 3, ClickEventSpecific.GREENFLAG.toString());

    @BeforeEach
    void setUp() {
        user1.setId(1);
        user2.setId(2);
    }

    @Test
    public void testExistsExperiment() {
        assertFalse(dashboardService.existsExperiment(ID));
        verify(experimentRepository).existsById(ID);
    }

    @Test
    public void testExistsParticipants() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        assertFalse(dashboardService.existsParticipants(ID));
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).existsByExperiment(experiment);
    }

    @Test
    public void testGetExperimentData() {
        when(experimentDataRepository.getReferenceById(ID)).thenReturn(experimentData);
        assertEquals(experimentDataDto, dashboardService.getExperimentData(ID));
        verify(experimentDataRepository).getReferenceById(ID);
    }

    @Test
    public void testGetParticipants() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findAllByExperiment(experiment)).thenReturn(participants);
        List<DashboardService.ParticipantIdName> userInfo = dashboardService.getParticipants(ID);
        assertAll(
                () -> assertEquals(2, userInfo.size()),
                () -> assertEquals(user1.getId(), userInfo.getFirst().id()),
                () -> assertEquals(user1.getUsername(), userInfo.getFirst().username()),
                () -> assertEquals(user2.getId(), userInfo.get(1).id()),
                () -> assertEquals(user2.getUsername(), userInfo.get(1).username())
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findAllByExperiment(experiment);
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
                () -> assertEquals(2, counts.getFirst().length),
                () -> assertEquals(1, counts.getFirst()[0]),
                () -> assertEquals(2, counts.getFirst()[1])
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
                () -> assertEquals(0, counts.getFirst().length),
                () -> assertEquals(0, counts.get(1).length)
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(userRepository, times(2)).getReferenceById(ID);
        verify(blockEventRepository, times(2)).findAllByUserAndExperimentAndEvent(user1,
                experiment, BLOCK_EVENT);
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
                () -> assertEquals(3, counts.getFirst().length),
                () -> assertEquals(1, counts.getFirst()[0]),
                () -> assertEquals(0, counts.getFirst()[1]),
                () -> assertEquals(2, counts.getFirst()[2])
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(userRepository, times(2)).getReferenceById(ID);
        verify(clickEventRepository, times(2)).findAllByUserAndExperimentAndEvent(user1,
                experiment, CLICK_EVENT);
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
                () -> assertEquals(3, counts.getFirst().length),
                () -> assertEquals(1, counts.getFirst()[0]),
                () -> assertEquals(0, counts.getFirst()[1]),
                () -> assertEquals(2, counts.getFirst()[2])
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(userRepository, times(2)).getReferenceById(ID);
        verify(resourceEventRepository, times(2)).findAllByUserAndExperimentAndEvent(user1,
                experiment, RESOURCE_EVENT);
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
                () -> assertEquals(6, counts.getFirst().length),
                () -> assertEquals(5, counts.getFirst()[0]),
                () -> assertEquals(0, counts.getFirst()[1]),
                () -> assertEquals(0, counts.getFirst()[2]),
                () -> assertEquals(3, counts.getFirst()[3]),
                () -> assertEquals(0, counts.getFirst()[4]),
                () -> assertEquals(0, counts.getFirst()[5])
        );
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
