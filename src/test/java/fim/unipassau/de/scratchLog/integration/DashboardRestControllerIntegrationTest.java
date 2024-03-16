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

package fim.unipassau.de.scratchLog.integration;

import fim.unipassau.de.scratchLog.application.service.DashboardService;
import fim.unipassau.de.scratchLog.spring.configuration.SecurityTestConfig;
import fim.unipassau.de.scratchLog.util.enums.BlockEventSpecific;
import fim.unipassau.de.scratchLog.util.enums.ClickEventSpecific;
import fim.unipassau.de.scratchLog.util.enums.ResourceEventSpecific;
import fim.unipassau.de.scratchLog.web.AbstractControllerTest;
import fim.unipassau.de.scratchLog.web.controller.DashboardRestController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardRestController.class)
@Import(SecurityTestConfig.class)
public class DashboardRestControllerIntegrationTest extends AbstractControllerTest {

    @MockBean
    private DashboardService dashboardService;

    private static final String ID_STRING = "5";
    private static final String ID_PARAM = "id";
    private static final String USER_PARAM = "users";
    private static final String EVENT_PARAM = "event";
    private static final String userIds = "1,5";
    private static final int ID = 5;
    private static final String[] experimentData = new String[]{"11", "7", "5"};
    private static final List<String[]> participantData = new ArrayList<>();
    private static final List<Integer[]> eventData = new ArrayList<>();

    @Test
    public void testGetExperimentData() throws Exception {
        when(dashboardService.getExperimentData(ID)).thenReturn(experimentData);
        mvc.perform(get("/dashboard/data")
                        .param(ID_PARAM, ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(content().string("[\"11\",\"7\",\"5\"]"));
        verify(dashboardService).getExperimentData(ID);
    }

    @Test
    public void testGetParticipantData() throws Exception {
        when(dashboardService.getParticipants(ID)).thenReturn(participantData);
        mvc.perform(get("/dashboard/data/participants")
                        .param(ID_PARAM, ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
        verify(dashboardService).getParticipants(ID);
    }

    @Test
    public void testGetBlockEventData() throws Exception {
        when(dashboardService.getBlockEventCountData(anyList(), anyInt(), any())).thenReturn(eventData);
        mvc.perform(get("/dashboard/data/event/block")
                        .param(ID_PARAM, ID_STRING)
                        .param(USER_PARAM, userIds)
                        .param(EVENT_PARAM, String.valueOf(BlockEventSpecific.CHANGE))
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
        verify(dashboardService).getBlockEventCountData(anyList(), anyInt(), any());
    }

    @Test
    public void testGetClickEventData() throws Exception {
        when(dashboardService.getClickEventCountData(anyList(), anyInt(), any())).thenReturn(eventData);
        mvc.perform(get("/dashboard/data/event/click")
                        .param(ID_PARAM, ID_STRING)
                        .param(USER_PARAM, userIds)
                        .param(EVENT_PARAM, String.valueOf(ClickEventSpecific.STOPALL))
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
        verify(dashboardService).getClickEventCountData(anyList(), anyInt(), any());
    }

    @Test
    public void testGetResourceEventData() throws Exception {
        when(dashboardService.getResourceEventCountData(anyList(), anyInt(), any())).thenReturn(eventData);
        mvc.perform(get("/dashboard/data/event/resource")
                        .param(ID_PARAM, ID_STRING)
                        .param(USER_PARAM, userIds)
                        .param(EVENT_PARAM, String.valueOf(ResourceEventSpecific.DELETE_SOUND))
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
        verify(dashboardService).getResourceEventCountData(anyList(), anyInt(), any());
    }

    @Test
    public void testGetEventCounts() throws Exception {
        when(dashboardService.getEventCountData(anyList(), anyInt())).thenReturn(eventData);
        mvc.perform(get("/dashboard/data/event/counts")
                        .param(ID_PARAM, ID_STRING)
                        .param(USER_PARAM, userIds)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
        verify(dashboardService).getEventCountData(anyList(), anyInt());
    }

}
