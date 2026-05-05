package de.uni_passau.fim.se2.scratchlog.application.service.dto;

import de.uni_passau.fim.se2.scratchlog.persistence.entity.TestResultState;

import java.util.Map;

/**
 * The test results for a user program.
 *
 * @param userId The ID of a user.
 * @param username The name of a user.
 * @param blockEventId The ID of the block event (ie program) these test results are for.
 * @param testResults A map of test case name to result.
 */
public record UserProgramTestResults(
    int userId,
    String username,
    int blockEventId,
    Map<String, TestResultState> testResults
) {
}
