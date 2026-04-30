package de.uni_passau.fim.se2.scratchlog.application.service;

import de.uni_passau.fim.se2.scratchlog.persistence.entity.BlockEvent;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.TestCase;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.TestResult;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.TestSuite;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.BlockEventRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.TestCaseRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.TestResultRepository;
import de.uni_passau.fim.se2.scratchlog.spring.events.TestExecutionFinishedEvent;
import de.uni_passau.fim.se2.scratchlog.spring.events.TestExecutionRequestEvent;
import de.uni_passau.fim.se2.scratchlog.spring.configuration.WhiskerConfiguration;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Profile(Constants.PROFILE_WHISKER)
public class TestExecutionService {

    private static final Logger log = LoggerFactory.getLogger(TestExecutionService.class);

    private final ApplicationEventPublisher applicationEventPublisher;

    private final BlockEventRepository blockEventRepository;

    private final ThreadPoolTaskExecutor taskExecutor;

    private final ExperimentService experimentService;

    private final TestCaseRepository testCaseRepository;

    private final TestResultRepository testResultRepository;

    private final WhiskerService whiskerService;

    private final ZipExportService zipExportService;

    @Autowired
    public TestExecutionService(
        final ApplicationEventPublisher applicationEventPublisher,
        final WhiskerConfiguration whiskerConfiguration,
        final WhiskerService whiskerService,
        final ZipExportService zipExportService,
        final ExperimentService experimentService,
        final TestResultRepository testResultRepository,
        final TestCaseRepository testCaseRepository,
        final BlockEventRepository blockEventRepository
    ) {
        this.whiskerService = whiskerService;

        this.taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix("whisker-executor");
        taskExecutor.setCorePoolSize(whiskerConfiguration.getMaxParallel());
        taskExecutor.setMaxPoolSize(whiskerConfiguration.getMaxParallel());
        taskExecutor.initialize();

        this.applicationEventPublisher = applicationEventPublisher;
        this.blockEventRepository = blockEventRepository;
        this.experimentService = experimentService;
        this.testCaseRepository = testCaseRepository;
        this.testResultRepository = testResultRepository;
        this.zipExportService = zipExportService;
    }

    /**
     * Queues the test execution for a given block event.
     *
     * <p>Sends a {@link TestExecutionFinishedEvent} when the test execution has completed.
     *
     * @param event An application event notifying about a test execution request.
     */
    @EventListener
    public void queueTestExecution(final TestExecutionRequestEvent event) {
        taskExecutor.submit(() -> {
            final Optional<BlockEvent> blockEvent = blockEventRepository.findBlockEventById(event.getBlockEventId());
            if (blockEvent.isEmpty()) {
                return;
            }

            final List<TestResult> testResults = runTests(event.getExperimentId(), blockEvent.get());
            applicationEventPublisher.publishEvent(
                new TestExecutionFinishedEvent(this, blockEvent.get(), testResults)
            );
        });
    }

    /**
     * Queues the test execution for all block events in the experiment for which we do not yet have test results.
     *
     * @param experimentId Some experiment.
     */
    public void queueTestRuns(final int experimentId) {
        final Set<Integer> blockEventIds = blockEventRepository.findBlockEventIdsWithoutTestResults(experimentId);
        log.info("Queueing test execution for {} events in experiment {}.", blockEventIds.size(), experimentId);
        blockEventIds
            .forEach(eventId -> queueTestExecution(new TestExecutionRequestEvent(this, experimentId, eventId)));
    }

    /**
     * Queues the test execution for all block events in the experiment.
     *
     * <p>Test results for events that already have a test result will be overwritten.
     *
     * @param experimentId Some experiment.
     */
    public void queueTestRunsAll(final int experimentId) {
        final Set<Integer> blockEventIds = blockEventRepository.findBlockEventIdsWithCodeInExercise(experimentId);
        log.info(
            "Queueing test execution for {} events in experiment {}. Ignoring existing test results.",
            blockEventIds.size(),
            experimentId
        );
        blockEventIds
            .forEach(eventId -> queueTestExecution(new TestExecutionRequestEvent(this, experimentId, eventId)));
    }

    /**
     * Executes the tests for the program produced by the given block event.
     *
     * @param experimentId The experiment the event was produced in.
     * @param event A block event.
     * @return The results for the individual test cases. An empty list in case the tests could not be executed.
     */
    private List<TestResult> runTests(final int experimentId, final BlockEvent event) {
        final byte[] sb3 = zipExportService.exportSb3ForEvent(experimentId, event.getId());
        final TestSuite testSuite = experimentService.getTestSuite(experimentId);

        if (testSuite == null || sb3.length == 0) {
            // nothing to do here, we need both to be able to run the tests
            log.debug("Skipping test execution, conditions not met: {}, {}", testSuite, sb3.length);
            return Collections.emptyList();
        }

        final WhiskerService.WhiskerApiResponse result = whiskerService.runTests(
            sb3, testSuite.getTestImplementation()
        );
        if (result != null) {
            return saveTestResults(testSuite, event, result);
        } else {
            log.debug("Whisker test execution failed (experiment={}, event={}).", experimentId, event.getId());
        }

        return Collections.emptyList();
    }

    private List<TestResult> saveTestResults(
        final TestSuite testSuite,
        final BlockEvent blockEvent,
        final WhiskerService.WhiskerApiResponse response
    ) {
        final List<TestResult> testResults = response.testResult().stream().map(result -> {
            final TestResult testResult = new TestResult();
            testResult.setTestCase(getRelevantTestCase(testSuite, result.name()));
            testResult.setProject(blockEvent);
            testResult.setResult(result.result());
            return testResult;
        }).toList();

        testResultRepository.deleteAllByProject_Id(blockEvent.getId());
        return testResultRepository.saveAll(testResults);
    }

    private TestCase getRelevantTestCase(final TestSuite testSuite, final String testName) {
        return testSuite
            .getTestCases()
            .stream()
            .filter(t -> testName.equals(t.getName()))
            .findAny()
            .orElseGet(() -> {
                final TestCase testCase = new TestCase();
                testCase.setTestSuite(testSuite);
                testCase.setName(testName);
                return testCaseRepository.save(testCase);
            });
    }
}
