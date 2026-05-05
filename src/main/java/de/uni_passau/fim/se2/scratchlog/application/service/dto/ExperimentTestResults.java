package de.uni_passau.fim.se2.scratchlog.application.service.dto;

import java.util.List;

/**
 * Collection of test results for the users in an experiment.
 *
 * @param exerciseId The experiment.
 * @param testCaseNames The names of the test cases.
 * @param userProgramTestResults The test results for user programs.
 */
public record ExperimentTestResults(
    int exerciseId,
    List<String> testCaseNames,
    List<UserProgramTestResults> userProgramTestResults
) {
}
