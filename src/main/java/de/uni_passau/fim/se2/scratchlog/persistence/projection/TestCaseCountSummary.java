package de.uni_passau.fim.se2.scratchlog.persistence.projection;

import de.uni_passau.fim.se2.scratchlog.persistence.entity.TestResultState;

public record TestCaseCountSummary(int blockEventId, TestResultState result, long count) {
}
