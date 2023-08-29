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
import fim.unipassau.de.scratchLog.persistence.entity.ExperimentData;
import fim.unipassau.de.scratchLog.persistence.repository.ExperimentDataRepository;
import fim.unipassau.de.scratchLog.persistence.repository.ExperimentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    private static final int ID = 3;
    private final ExperimentData experimentData = new ExperimentData(ID, 15, 10, 7);
    private final String[] stringExperimentData = new String[]{String.valueOf(experimentData.getParticipants()),
            String.valueOf(experimentData.getStarted()), String.valueOf(experimentData.getFinished())};

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

}
