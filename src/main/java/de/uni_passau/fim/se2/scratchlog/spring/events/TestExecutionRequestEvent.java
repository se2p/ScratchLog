package de.uni_passau.fim.se2.scratchlog.spring.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TestExecutionRequestEvent extends ApplicationEvent {

    private final int experimentId;

    private final int blockEventId;

    public TestExecutionRequestEvent(final Object source, final int experimentId, final int blockEventId) {
        super(source);

        this.experimentId = experimentId;
        this.blockEventId = blockEventId;
    }

}
