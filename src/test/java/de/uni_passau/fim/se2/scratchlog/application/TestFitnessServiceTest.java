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

package de.uni_passau.fim.se2.scratchlog.application;

import de.uni_passau.fim.se2.scratchlog.AbstractScratchLogTest;
import de.uni_passau.fim.se2.scratchlog.application.service.TestFitnessService;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.BlockEvent;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Experiment;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.TestCase;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.TestResult;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.TestResultState;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.TestSuite;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.TestCaseRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.TestResultRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.TestSuiteRepository;
import de.uni_passau.fim.se2.scratchlog.util.enums.BlockEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.BlockEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestFitnessServiceTest extends AbstractScratchLogTest {

    @Autowired
    private TestFitnessService testFitnessService;

    @Autowired
    private TestSuiteRepository testSuiteRepository;

    @Autowired
    private TestCaseRepository testCaseRepository;

    @Autowired
    private TestResultRepository testResultRepository;

    private TestSuite testSuite;

    private BlockEvent blockEvent;

    private BlockEvent blockEvent2;

    @BeforeEach
    void setUp() {
        final User user = entityUtilService.generateUser("testFitness");
        final Experiment experiment = entityUtilService.generateExperiment("testFitness");
        blockEvent = eventUtilService.generateBlockEvent(
            user, experiment, BlockEventType.CREATE, BlockEventSpecific.CREATE
        );
        blockEvent2 = eventUtilService.generateBlockEvent(
            user, experiment, BlockEventType.DELETE, BlockEventSpecific.DELETE
        );

        testSuite = testSuiteRepository.save(
            new TestSuite(null, experiment, "whisker-test.js", "", Collections.emptySet())
        );
    }

    @Test
    void noFitnessIfNoTestResults() {
        final Optional<Double> fitness = testFitnessService.getTestFitness(blockEvent.getId());
        assertEquals(Optional.empty(), fitness);
    }

    @Test
    void fitnessOneAllTestsPassing() {
        setUpTestResults(blockEvent, 5, 0, 0);

        final Optional<Double> fitness = testFitnessService.getTestFitness(blockEvent.getId());
        assertTrue(fitness.isPresent());
        assertEquals(1.0, fitness.get());
    }

    @Test
    void fitnessZeroNoTestsPassing() {
        setUpTestResults(blockEvent, 0, 4, 6);

        final Optional<Double> fitness = testFitnessService.getTestFitness(blockEvent.getId());
        assertTrue(fitness.isPresent());
        assertEquals(0.0, fitness.get());
    }

    @Test
    void fitnessNonZero() {
        setUpTestResults(blockEvent, 5, 9, 6);

        final Optional<Double> fitness = testFitnessService.getTestFitness(blockEvent.getId());
        assertTrue(fitness.isPresent());
        assertEquals(0.25, fitness.get());
    }

    @Test
    void multipleFitnessesEmpty() {
        final var fitnesses = testFitnessService.getTestFitnesses(Collections.emptySet());
        assertEquals(Collections.emptyMap(), fitnesses);
    }

    @Test
    void multipleFitnessesOneMissing() {
        setUpTestResults(blockEvent, 4, 7, 9);

        final var fitnesses = testFitnessService.getTestFitnesses(Set.of(blockEvent.getId(), blockEvent2.getId()));
        assertEquals(1, fitnesses.size());
        assertEquals(0.2, fitnesses.get(blockEvent.getId()));
    }

    @Test
    void multipleFitnesses() {
        setUpTestResults(blockEvent, 4, 7, 9);
        setUpTestResults(blockEvent2, 1, 10, 9);

        final var fitnesses = testFitnessService.getTestFitnesses(Set.of(blockEvent.getId(), blockEvent2.getId()));
        assertEquals(2, fitnesses.size());
        assertEquals(0.2, fitnesses.get(blockEvent.getId()));
        assertEquals(0.05, fitnesses.get(blockEvent2.getId()));
    }

    private void setUpTestResults(final BlockEvent event, final int passing, final int failing, final int skipped) {
        List<TestCase> testCases = new ArrayList<>();
        for (int i = 1; i <= passing + failing + skipped; ++i) {
            testCases.add(new TestCase(null, testSuite, String.format("test-case-%d", i)));
        }
        testCases = testCaseRepository.saveAll(testCases);

        final List<TestResult> results = new ArrayList<>();
        for (int i = 0; i < passing; ++ i) {
            results.add(new TestResult(null, testCases.get(i), event, TestResultState.PASS));
        }
        for (int i = passing; i < passing + failing; ++ i) {
            results.add(new TestResult(null, testCases.get(i), event, TestResultState.FAIL));
        }
        for (int i = passing + failing; i < passing + failing + skipped; ++ i) {
            results.add(new TestResult(null, testCases.get(i), event, TestResultState.SKIP));
        }
        testResultRepository.saveAll(results);
    }
}
