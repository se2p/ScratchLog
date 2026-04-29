package de.uni_passau.fim.se2.scratchlog.spring.events;

import de.uni_passau.fim.se2.scratchlog.persistence.entity.BlockEvent;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.TestResult;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class TestExecutionFinishedEvent extends ApplicationEvent {

    private final BlockEvent blockEvent;

    private final List<TestResult> testResults;

    public TestExecutionFinishedEvent(
        final Object source,
        final BlockEvent blockEvent,
        final List<TestResult> testResults
    ) {
        super(source);

        this.blockEvent = blockEvent;
        this.testResults = testResults;
    }
}
