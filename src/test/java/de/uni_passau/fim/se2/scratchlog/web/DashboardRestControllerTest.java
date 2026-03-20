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

import de.uni_passau.fim.se2.scratchlog.application.service.DashboardService;
import de.uni_passau.fim.se2.scratchlog.util.enums.BlockEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.ClickEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.ResourceEventSpecific;
import de.uni_passau.fim.se2.scratchlog.web.controller.DashboardRestController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DashboardRestControllerTest {

    @InjectMocks
    private DashboardRestController dashboardRestController;

    @Mock
    private DashboardService dashboardService;

    private static final int ID = 5;
    private static final List<Integer> userIds = List.of(1, 5);
    private static final DashboardService.ExperimentDataDto experimentData = new DashboardService.ExperimentDataDto(11, 7, 5);
    private static final List<DashboardService.ParticipantIdName> participantData = new ArrayList<>();
    private static final List<Integer[]> eventData = new ArrayList<>();

    @Test
    public void testGetExperimentData() {
        when(dashboardService.getExperimentData(ID)).thenReturn(experimentData);
        assertEquals(experimentData, dashboardRestController.getExperimentData(ID));
        verify(dashboardService).getExperimentData(ID);
    }

    @Test
    public void testGetParticipantData() {
        when(dashboardService.getParticipants(ID)).thenReturn(participantData);
        assertEquals(participantData, dashboardRestController.getParticipantData(ID));
        verify(dashboardService).getParticipants(ID);
    }

    @Test
    public void testGetBlockEventData() {
        when(dashboardService.getBlockEventCountData(anyList(), anyInt(), any())).thenReturn(eventData);
        assertEquals(eventData, dashboardRestController.getBlockEventData(ID, userIds,
                BlockEventSpecific.CREATE));
        verify(dashboardService).getBlockEventCountData(anyList(), anyInt(), any());
    }

    @Test
    public void testGetClickEventData() {
        when(dashboardService.getClickEventCountData(anyList(), anyInt(), any())).thenReturn(eventData);
        assertEquals(eventData, dashboardRestController.getClickEventData(ID, userIds, ClickEventSpecific.GREENFLAG));
        verify(dashboardService).getClickEventCountData(anyList(), anyInt(), any());
    }

    @Test
    public void testGetResourceEventData() {
        when(dashboardService.getResourceEventCountData(anyList(), anyInt(), any())).thenReturn(eventData);
        assertEquals(eventData, dashboardRestController.getResourceEventData(ID, userIds,
                ResourceEventSpecific.ADD_COSTUME));
        verify(dashboardService).getResourceEventCountData(anyList(), anyInt(), any());
    }

    @Test
    public void testGetEventCounts() {
        when(dashboardService.getEventCountData(anyList(), anyInt())).thenReturn(eventData);
        assertEquals(eventData, dashboardRestController.getEventCounts(ID, userIds));
        verify(dashboardService).getEventCountData(anyList(), anyInt());
    }

}
