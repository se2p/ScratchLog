package de.uni_passau.fim.se2.scratchlog.persistence.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TestResultState {

    /**
     * A passing test case.
     */
    PASS("pass"),
    /**
     * A failed test case.
     */
    FAIL("fail"),
    /**
     * A test case that resulted in a crash.
     */
    ERROR("error"),
    /**
     * A skipped test case.
     */
    SKIP("skip");

    private final String key;

    TestResultState(final String key) {
        this.key = key;
    }

    @JsonValue
    public String getKey() {
        return key;
    }
}
