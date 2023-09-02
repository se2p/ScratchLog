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
import fim.unipassau.de.scratchLog.util.Constants;
import fim.unipassau.de.scratchLog.web.controller.DashboardController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(SpringExtension.class)
@WebMvcTest(DashboardController.class)
@Import(SecurityTestConfig.class)
@ActiveProfiles("test")
public class DashboardControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private DashboardService dashboardService;

    private static final String DASHBOARD = "dashboard";
    private static final String ID_STRING = "5";
    private static final String ID_PARAM = "id";
    private static final int ID = 5;

    @Test
    public void testGetDashboard() throws Exception {
        when(dashboardService.existsExperiment(ID)).thenReturn(true);
        when(dashboardService.existsParticipants(ID)).thenReturn(true);
        mvc.perform(get("/dashboard")
                        .param(ID_PARAM, ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(DASHBOARD))
                .andExpect(model().attribute("experiment", is(ID)))
                .andExpect(model().attribute("blockEvents", notNullValue()));
        verify(dashboardService).existsExperiment(ID);
        verify(dashboardService).existsParticipants(ID);
    }

    @Test
    public void testGetDashboardNoExperiment() throws Exception {
        mvc.perform(get("/dashboard")
                        .param(ID_PARAM, ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(dashboardService).existsExperiment(ID);
        verify(dashboardService, never()).existsParticipants(anyInt());
    }

}
