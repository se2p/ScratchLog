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

package de.uni_passau.fim.se2.scratchlog.web.controller;

import de.uni_passau.fim.se2.scratchlog.application.service.TestExecutionService;
import de.uni_passau.fim.se2.scratchlog.application.service.TestResultService;
import de.uni_passau.fim.se2.scratchlog.application.service.dto.ExperimentTestResults;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Profile(Constants.PROFILE_WHISKER)
@RestController
@RequestMapping("/whisker/test/")
public class TestController {

    private final TestResultService testResultService;

    private final TestExecutionService testExecutionService;

    @Autowired
    public TestController(
        final TestResultService testResultService,
        final TestExecutionService testExecutionService
    ) {
        this.testResultService = testResultService;
        this.testExecutionService = testExecutionService;
    }

    /**
     * Retrieves the test results of the latest project of each user in the experiment.
     *
     * @param experimentId Some experiment.
     * @return The test results for the latest project per user.
     */
    @GetMapping("latest")
    public ExperimentTestResults latestTestResults(
        @RequestParam("experimentId") final int experimentId
    ) {
        return testResultService.getLatestTestResults(experimentId);
    }

    /**
     * Triggers the test execution for all projects in the experiment for which we do not yet have test results.
     *
     * @param experimentId Some experiment.
     */
    @PutMapping("trigger/all-missing")
    public void triggerTestExecution(@RequestParam("experimentId") final int experimentId) {
        testExecutionService.queueTestRuns(experimentId);
    }

}
