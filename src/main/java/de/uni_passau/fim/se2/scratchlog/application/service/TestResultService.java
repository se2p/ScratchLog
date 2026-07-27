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

package de.uni_passau.fim.se2.scratchlog.application.service;

import de.uni_passau.fim.se2.scratchlog.application.service.dto.ExperimentTestResults;
import de.uni_passau.fim.se2.scratchlog.application.service.dto.UserProgramTestResults;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.TestCase;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.TestResult;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.TestResultState;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.BlockEventRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.Project;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.TestCaseRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.TestResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TestResultService {

    private final TestResultRepository testResultRepository;

    private final TestCaseRepository testCaseRepository;

    private final BlockEventRepository blockEventRepository;

    @Autowired
    public TestResultService(
        final TestResultRepository testResultRepository,
        final TestCaseRepository testCaseRepository,
        final BlockEventRepository blockEventRepository
    ) {
        this.testResultRepository = testResultRepository;
        this.testCaseRepository = testCaseRepository;
        this.blockEventRepository = blockEventRepository;
    }

    /**
     * Retrieves the test results of the latest programs of all users in the course.
     *
     * <p>Assumes the tests have already run. Does not trigger a test execution.
     *
     * @param experimentId Some experiment.
     * @return The test results of the latest program of each user.
     */
    public ExperimentTestResults getLatestTestResults(final int experimentId) {
        final List<Project> latestProjects = blockEventRepository.findLastPerUserInExperiment(experimentId);
        final Map<Integer, List<TestResult>> testResultsPerProject = testResultRepository
            .findAllByProjectIdIn(latestProjects.stream().map(Project::id).collect(Collectors.toSet()))
            .stream()
            .collect(Collectors.groupingBy(result -> result.getProject().getId()));

        final List<UserProgramTestResults> testResults = new ArrayList<>();

        for (final Project project : latestProjects) {
            final List<TestResult> testResultsForProject = testResultsPerProject.get(project.id());
            if (testResultsForProject == null) {
                continue;
            }

            final Map<String, TestResultState> testCaseResults = testResultsForProject
                .stream()
                .collect(Collectors.toMap(r -> r.getTestCase().getName(), TestResult::getResult));

            testResults.add(new UserProgramTestResults(
                project.userId(),
                project.username(),
                project.id(),
                testCaseResults
            ));
        }

        final List<String> testNames = testCaseRepository
            .findTestCasesUsedInExperiment(experimentId)
            .stream()
            .map(TestCase::getName)
            .toList();

        return new ExperimentTestResults(experimentId, testNames, testResults);
    }

}
