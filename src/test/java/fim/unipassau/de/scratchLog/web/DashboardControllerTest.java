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

import fim.unipassau.de.scratchLog.application.service.DashboardService;
import fim.unipassau.de.scratchLog.util.Constants;
import fim.unipassau.de.scratchLog.web.controller.DashboardController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DashboardControllerTest {

    @InjectMocks
    private DashboardController dashboardController;

    @Mock
    private DashboardService dashboardService;

    @Mock
    private Model model;

    private static final String DASHBOARD = "dashboard";
    private static final String ID_STRING = "5";
    private static final int ID = 5;

    @Test
    public void testGetDashboard() {
        when(dashboardService.existsExperiment(ID)).thenReturn(true);
        assertEquals(DASHBOARD, dashboardController.getDashboard(ID_STRING, model));
        verify(dashboardService).existsExperiment(ID);
    }

    @Test
    public void testGetDashboardNoExperiment() {
        assertEquals(Constants.ERROR, dashboardController.getDashboard(ID_STRING, model));
        verify(dashboardService).existsExperiment(ID);
    }

    @Test
    public void testGetDashboardInvalidId() {
        assertEquals(Constants.ERROR, dashboardController.getDashboard(DASHBOARD, model));
        verify(dashboardService, never()).existsExperiment(anyInt());
    }

}
