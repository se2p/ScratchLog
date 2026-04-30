package de.uni_passau.fim.se2.scratchlog.application.service;

import de.uni_passau.fim.se2.scratchlog.persistence.entity.TestResultState;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.TestCaseCountSummary;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.TestResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class TestFitnessService {

    private final TestResultRepository testResultRepository;

    @Autowired
    public TestFitnessService(final TestResultRepository testResultRepository) {
        this.testResultRepository = testResultRepository;
    }

    /**
     * Computes the test fitness of a program.
     *
     * <p>Assumes a test result already exists in the database for this project.
     *
     * <p>The test fitness is defined as {@code (#passed tests)/(#total tests in the test suite)}. A program that
     * passes all tests therefore has a fitness of {@code 1} and one that fails all tests a fitness of {@code 0}.
     *
     * @param blockEventId The id of the event that created the program.
     * @return The test fitness in range {@code [0, 1]} if it could be computed.
     */
    public Optional<Double> getTestFitness(final int blockEventId) {
        final List<TestCaseCountSummary> summary = testResultRepository.getTestResultSummaries(Set.of(blockEventId));
        if (summary.isEmpty()) {
            return Optional.empty();
        }

        final Map<TestResultState, Long> testResultStateCounts = processSummaries(summary).get(blockEventId);
        return computeFitness(testResultStateCounts);
    }

    /**
     * Computes the test fitnesses of a set of programs.
     *
     * <p>Assumes test results already exists in the database for these projects. Projects without test results are
     * skipped and thus absent from the resulting fitness map.
     *
     * <p>For a definition of how the fitness is computed, see {@link #getTestFitness(int)}.
     *
     * @param blockEventIds The ids of the events that created the programs.
     * @return The block event IDs mapped to test fitnesses with values in range {@code [0, 1]}. May not contain entries
     *         for all input IDs since the fitness cannot be computed if there are no test results yet.
     */
    public Map<Integer, Double> getTestFitnesses(final Set<Integer> blockEventIds) {
        final List<TestCaseCountSummary> summary = testResultRepository.getTestResultSummaries(blockEventIds);
        if (summary.isEmpty()) {
            return Collections.emptyMap();
        }

        final var summaryCounts = processSummaries(summary);

        final Map<Integer, Double> fitnesses = new HashMap<>();
        summaryCounts.forEach((blockEventId, testResults) -> {
            computeFitness(testResults).ifPresent(fitness -> fitnesses.put(blockEventId, fitness));
        });

        return fitnesses;
    }

    private Optional<Double> computeFitness(final Map<TestResultState, Long> testResultStateCounts) {
        final long totalTests = testResultStateCounts.values().stream().mapToLong(c -> c).sum();
        if (totalTests == 0) {
            // should not be possible, since only positive values are even added to the map
            // safety against division by zero below
            return Optional.empty();
        }

        final long passedTests = testResultStateCounts.getOrDefault(TestResultState.PASS, 0L);

        return Optional.of(passedTests / (double) totalTests);
    }

    private Map<Integer, Map<TestResultState, Long>> processSummaries(final List<TestCaseCountSummary> summaries) {
        final Map<Integer, Map<TestResultState, Long>> results = new HashMap<>();

        for (final TestCaseCountSummary summary : summaries) {
            results.compute(summary.blockEventId(), (k, v) -> {
                if (v == null) {
                    v = new EnumMap<>(TestResultState.class);
                }
                v.put(summary.result(), summary.count());

                return v;
            });
        }

        return results;
    }
}
